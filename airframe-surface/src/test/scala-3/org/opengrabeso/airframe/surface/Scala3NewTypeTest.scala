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

import org.opengrabeso.airframe.surface.Scala3NewTypeTest.RawType
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should

object Scala3NewTypeTest {
  class RawTypeImpl(x: Double, y: Double)
  type RawType = RawTypeImpl
}

class Scala3NewTypeTest extends AnyFunSuite with should.Matchers:
  trait Label1

  opaque type MyEnv = String

  object O:
    opaque type NestedOpaque = Double
    opaque type NestedAlias = Double

  test("Opaque types in a nested object") {
    val s = Surface.of[O.NestedOpaque]
  }

  test("Alias types in a nested object") {
    val s = Surface.of[O.NestedAlias]
  }
