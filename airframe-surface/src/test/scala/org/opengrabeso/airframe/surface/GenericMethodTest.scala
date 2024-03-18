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

  case class Gen[X](value: X) {
    def pass(x: X): X = x
    def myself: Gen[X] = this
    def wrap(x: X): Gen[X] = Gen[X](value)
    def unwrap(x: Gen[X]): X = x.value
  }

  test("Methods of generic type") {
    val typeSurface = Surface.of[Gen[String]]
    val methods = Surface.methodsOf[Gen[String]]
    methods.size shouldBe 4
    val pass = methods.find(_.name == "pass").get
    pass.returnType shouldBe Surface.of[String]
    /*
    val myself = methods.find(_.name == "pass").get
    myself.returnType shouldBe typeSurface
    val wrap = methods.find(_.name == "pass").get
    wrap.returnType shouldBe typeSurface
    val unwrap = methods.find(_.name == "pass").get
    unwrap.returnType shouldBe Surface.of[String]

     */
  }

}
