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
package org.opengrabeso.airframe.surface
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.immutable.ListMap
import scala.quoted.*
import scala.reflect.ClassTag

private[surface] object CompileTimeSurfaceFactory:

  def surfaceOf[A](using tpe: Type[A], quotes: Quotes): Expr[Surface] =
    import quotes.*
    import quotes.reflect.*

    val f = new CompileTimeSurfaceFactory(using quotes)
    f.surfaceOf(tpe)

  def methodsOf[A](using tpe: Type[A], quotes: Quotes): Expr[Seq[Surface]] =
    val f = new CompileTimeSurfaceFactory(using quotes)
    f.methodsOf(tpe)

private[surface] class CompileTimeSurfaceFactory[Q <: Quotes](using quotes: Q):
  import quotes.*
  import quotes.reflect.*

  private def fullTypeNameOf(t: Type[?]): String =
    fullTypeNameOf(TypeRepr.of(using t))

  private def fullTypeNameOf(t: TypeRepr): String = t.show

  def surfaceOf(tpe: Type[?]): Expr[Surface] =
    surfaceOf(TypeRepr.of(using tpe))

  private var observedSurfaceCount = new AtomicInteger(0)
  private var seen                 = ListMap[TypeRepr, Int]()
  private val memo                 = scala.collection.mutable.Map[TypeRepr, Expr[Surface]]()

  // To reduce the byte code size, we need to memoize the generated surface bound to a variable
  private var surfaceToVar = ListMap.empty[TypeRepr, Symbol]

  private def surfaceOf(t: TypeRepr, useVarRef: Boolean = true): Expr[Surface] =
    if useVarRef && surfaceToVar.contains(t) then Ref(surfaceToVar(t)).asExprOf[Surface]
    else if seen.contains(t) then memo(t)
    else
      seen += t -> observedSurfaceCount.getAndIncrement()
      // For debugging
      // println(s"[${typeNameOf(t)}]\n  ${t}\nfull type name: ${fullTypeNameOf(t)}\nclass: ${t.getClass}")
      val surface = '{
        new org.opengrabeso.airframe.surface.GenericSurface(
          ${ clsOf(t) },
          // typeArgs = Seq.empty,
          docString = None
        )
      }

      memo += (t -> surface)
      surface

  private def clsOf(t: TypeRepr): Expr[Class[_]] =
    Literal(ClassOfConstant(t)).asExpr.asInstanceOf[Expr[Class[_]]]

  private def methodsOf(t: TypeRepr, uniqueId: String, inherited: Boolean): Expr[Seq[Surface]] =
    // Run just for collecting known surfaces. seen variable will be updated
    methodsOfInternal(t, inherited)

    // Create a var def table for replacing surfaceOf[xxx] to __s0, __s1, ...
    var surfaceVarCount = 0
    seen
      // Exclude primitive type surface
      .toSeq
      // Exclude primitive surfaces as it is already defined in Primitive object
      .map((tpe, order) =>
        (tpe, order)
      ) // first list all lazy vals, otherwise there is a risk of forward reference error across strict vals
      .sortBy(_._2)
      .reverse
      .foreach { case (tpe, order) =>
        // Update the cache so that the next call of surfaceOf method will use the local variable reference
        surfaceToVar += tpe -> Symbol.newVal(
          Symbol.spliceOwner,
          // Use alphabetically ordered variable names
          f"__${t.typeSymbol.name}_${tpe.typeSymbol.name}_s${uniqueId}${surfaceVarCount}%03X",
          TypeRepr.of[Surface],
          // Use lazy val to avoid forward reference error
          Flags.Lazy,
          Symbol.noSymbol
        )
        surfaceVarCount += 1
      }

    // print(surfaceToVar.map(v => s"  ${v._1.show} => ${v._2}").mkString(s"methodsOf ${t.show}:\n", "\n", "\n"))

    // Clear surface cache
    memo.clear()
    seen = ListMap.empty

    val surfaceDefs: List[ValDef] = surfaceToVar.toSeq.map { case (tpe, sym) =>
      ValDef(sym, Some(surfaceOf(tpe, useVarRef = false).asTerm))
    }.toList

    /**
      * Generate a code like this:
      *
      * {{ lazy val __s000 = Surface.of[A]; lazy val __s001 = Surface.of[B] ...
      *
      * ClassMethodSurface( .... ) }}
      */
    val expr = Block(
      surfaceDefs,
      methodsOfInternal(t, inherited).asTerm
    ).asExprOf[Seq[Surface]]

    // println(s"===  methodOf: ${t.typeSymbol.fullName} => \n${expr.show}")

    expr

  private def methodsOfInternal(targetType: TypeRepr, inherited: Boolean): Expr[Seq[Surface]] =
    val localMethods = targetType.typeSymbol.declaredMethods
    val methodSurfaces = localMethods.map(m => (m, m.tree)).collect { case (m, df: DefDef) =>
      val returnType = df.returnTpt.tpe
      // println(s"======= ${df.returnTpt.show}")
      surfaceOf(returnType)
    }
    val expr = Expr.ofSeq(methodSurfaces)
    expr

  def methodsOf(t: Type[?]): Expr[Seq[Surface]] =
    val repr = TypeRepr.of(using t)
    methodsOf(repr, "_" + repr.typeSymbol.name + "_", true)
