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
package org.opengrabeso.airframe.surface.reflect

import java.{lang => jl}

import org.opengrabeso.airframe.surface.{MethodParameter, MethodSurface, Surface}
import wvlet.log.LogSupport

import scala.util.Try

/**
  * MethodSurface for JVM. This can call method through Java reflection
  */
case class ReflectMethodSurface(mod: Int, owner: Surface, name: String, returnType: Surface, args: Seq[MethodParameter])
    extends MethodSurface
    with LogSupport {
  private lazy val method: Option[jl.reflect.Method] = ReflectMethodSurface.findMethod(owner.rawType, this)

  def getMethod: Option[jl.reflect.Method] = method
}

object ReflectMethodSurface {

  def findMethod(owner: Class[_], m: MethodSurface): Option[jl.reflect.Method] = {
    // For `symbol-based method names`, we need to encode Scala method names into the bytecode format used in class files.
    val rawMethodName = scala.reflect.NameTransformer.encode(m.name)
    Try(owner.getDeclaredMethod(rawMethodName, m.args.map(_.surface.rawType): _*)).toOption
  }

}
