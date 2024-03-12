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

class GenericMethodTest extends AnyFunSuite with should.Matchers {
  class A {
    def helloX[X](v: X): String = "hello"
  }

  test("generic method") {
    val methods = Surface.methodsOf[A]
    methods.size shouldBe 1
    val m = methods(0)
    m.name shouldBe "helloX"
    m.returnType shouldBe Surface.of[String]
  }

}