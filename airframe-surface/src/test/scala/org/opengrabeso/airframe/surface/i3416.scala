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

class i3416 extends AnyFunSuite with should.Matchers {

  object O {
    class C(private[O] val id: Int) {
      private[O] def getId: Int = id
    }
  }

  test("Do not list package private methods") {
    val s = Surface.methodsOf[O.C]
    s.size shouldBe 0
  }
}
