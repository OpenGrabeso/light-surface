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

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should

import scala.jdk.CollectionConverters.*

class Scala3EnumTest extends AnyFunSuite with should.Matchers:

  enum Color:
    case Red, Green, Blue

  test("scala 3 enum") {
    val s = Surface.of[Color]

    s.name shouldBe "Color"
    s.fullName shouldBe "org.opengrabeso.airframe.surface.Scala3EnumTest.Color"
    s.params shouldBe empty

    s.isEnum shouldBe true
  }
