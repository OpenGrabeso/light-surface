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

import org.scalatest.matchers.should

class MethodInheritanceTest extends SurfaceSpec with should.Matchers {

  class SimpleClass {
    def format(par: Double): String = par.toString
    def pass(par: String): String = par
  }

  class InheritedClass extends SimpleClass {
    override def pass(par: String): String = par + par
  }

  test("list methods with defining classes") {
    val m = Surface.inheritedMethodsOf[InheritedClass]

    val simpleClass = m.find(_._1.name == "SimpleClass").get
    val simpleClassFormat = simpleClass._2.find(_.name == "format").get
    val inheritedClass = m.find(_._1.name == "InheritedClass").get
    val inheritedClassFormat = inheritedClass._2.find(_.name == "format")
    val inheritedClassPass = inheritedClass._2.find(_.name == "pass").get

    inheritedClassFormat shouldBe empty

  }

}
