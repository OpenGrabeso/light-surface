/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wvlet.airframe.surface.reflect

import wvlet.airframe.surface.{GenericSurface, ObjectFactory, Parameter, Surface}
import wvlet.log.LogSupport

import java.lang.reflect.{Constructor, InvocationTargetException}

/**
  * Used when we can use reflection to instantiate objects of this surface
  *
  * @param rawType
  * @param typeArgs
  * @param params
  */
class RuntimeGenericSurface(
    override val rawType: Class[_],
    override val typeArgs: Seq[Surface] = Seq.empty,
    override val params: Seq[Parameter] = Seq.empty,
    val outer: Option[AnyRef] = None,
    isStatic: Boolean
) extends GenericSurface(rawType, typeArgs, params)
    with LogSupport {
  self =>

  override def withOuter(outer: AnyRef): Surface = {
    new RuntimeGenericSurface(rawType, typeArgs, params, Some(outer), isStatic = false)
  }

  private class ReflectObjectFactory extends ObjectFactory {

    private def getPrimaryConstructorOf(cls: Class[_]): Option[Constructor[_]] = {
      val constructors = cls.getConstructors
      if (constructors.size == 0) {
        None
      } else {
        Some(constructors(0))
      }
    }

    private def getFirstParamTypeOfPrimaryConstructor(cls: Class[_]): Option[Class[_]] = {
      getPrimaryConstructorOf(cls).flatMap { constructor =>
        val constructorParamTypes = constructor.getParameterTypes
        if (constructorParamTypes.size == 0) {
          None
        } else {
          Some(constructorParamTypes(0))
        }
      }
    }
    // private val isStatic = mirror.classSymbol(rawType).isStatic
    private def outerInstance: Option[AnyRef] = {
      if (isStatic) {
        None
      } else {
        // Inner class
        outer.orElse {
          val contextClass = getFirstParamTypeOfPrimaryConstructor(rawType)
          val msg = contextClass
            .map(x =>
              s" Call Surface.of[${rawType.getSimpleName}] or bind[${rawType.getSimpleName}].toXXX where `this` points to an instance of ${x}"
            )
            .getOrElse(
              ""
            )
          throw new IllegalStateException(
            s"Cannot build a non-static class ${rawType.getName}.${msg}"
          )
        }
      }
    }

    // Create instance with Reflection
    override def newInstance(args: Seq[Any]): Any = {
      try {
        // We should not store the primary constructor reference here to avoid including java.lang.reflect.Constructor,
        // which is non-serializable, within this RuntimeGenericSurface class
        getPrimaryConstructorOf(rawType)
          .map { primaryConstructor =>
            val argList = Seq.newBuilder[AnyRef]
            if (!isStatic) {
              // Add a reference to the context instance if this surface represents an inner class
              outerInstance.foreach { x =>
                argList += x
              }
            }
            argList ++= args.map(_.asInstanceOf[AnyRef])
            val a = argList.result()
            if (a.isEmpty) {
              logger.trace(s"build ${rawType.getName} using the default constructor")
              primaryConstructor.newInstance()
            } else {
              logger.trace(s"build ${rawType.getName} with args: ${a.mkString(", ")}")
              primaryConstructor.newInstance(a: _*)
            }
          }
          .getOrElse {
            throw new IllegalStateException(s"No primary constructor is found for ${rawType}")
          }
      } catch {
        case e: InvocationTargetException =>
          logger.warn(
            s"Failed to instantiate ${self}: [${e.getTargetException.getClass.getName}] ${e.getTargetException.getMessage}\nargs:[${args
                .mkString(", ")}]"
          )
          throw e.getTargetException
        case e: Throwable =>
          logger.warn(
            s"Failed to instantiate ${self}: [${e.getClass.getName}] ${e.getMessage}\nargs:[${args
                .mkString(", ")}]"
          )
          throw e
      }
    }
  }
}
