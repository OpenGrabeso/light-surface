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

/**
  * Note: This interface is the same with scala-2 Surface interface, but Scala compiler requires defining Surface object
  * in the same file, so this interface is copied.
  */
trait Surface extends Serializable:
  def rawType: Class[?]
  def typeArgs: Seq[Surface]

  def docString: Option[String]

/**
  * Scala 3 implementation of Surface
  */
object Surface:
  import scala.quoted.*

  inline def of[A]: Surface             = ${ CompileTimeSurfaceFactory.surfaceOf[A] }
  inline def methodsOf[A]: Seq[Surface] = ${ CompileTimeSurfaceFactory.methodsOf[A] }
