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

class i3419 extends AnyFunSuite with should.Matchers {

  class QPrivate private (val id: Int)

  class QProtected protected (val id: Int)

  test("Handle private constructor") {
    val s = Surface.of[QPrivate]
    //debug(s.params)
    s.params.size shouldBe 0
  }

  test("Handle protected constructor") {
    val s = Surface.of[QProtected]
    //debug(s.params)
    s.params.size shouldBe 0
  }
}
