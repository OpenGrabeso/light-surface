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

trait MT:
  case class B(min: Int = 0, max: Int = 0)

object MT extends MT

/**
 * Test and fix ported from https://github.com/wvlet/airframe/pull/3941
 * */
class TraitInheritedClassTest extends AnyFunSuite with should.Matchers {
  test("reproduce the crash with trait-inherited case class") {
    // This should not crash the compiler during the erasure phase
    val schema = Surface.of[MT.B]
    schema should not be null
    schema.name shouldBe "B"
    schema.fullName shouldBe "org.opengrabeso.airframe.surface.MT.B"
    schema.params.length shouldBe 2
    schema.params(0).name shouldBe "min"
    schema.params(1).name shouldBe "max"
  }

  test("original issue scenario should work") {
    // This reproduces the exact scenario from the issue
    val schema = Surface.of[MT.B]

    // Should be able to print the schema without crashing
    val schemaString = schema.toString
    schemaString.contains("B") shouldBe true

    // Should have proper parameters with default values (though they may be None due to trait limitation)
    schema.params.foreach { param =>
      param.surface should not be null
      param.surface.name shouldBe "Int"
    }
  }
}
