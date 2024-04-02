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

object SurfaceDocTest {

  /**
   * This is a documented class
   * @param p with a well documented parameter
   * */
  class Documented(p: String)
}

import SurfaceDocTest._
/**
 */
class SurfaceDocTest extends SurfaceSpec with should.Matchers {

  test("read class doc comment") {
    val d = Surface.of[Documented]
    d.docString should not be empty
    val doc = d.docString.get
    doc should include ("a documented class")
    doc should include ("@param p with a well documented parameter")
  }
}
