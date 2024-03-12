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
package wvlet.airframe.surface

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should

class IntersectionTypeTest extends AnyFunSuite with should.Matchers:
  trait Label1

  test("support intersection type") {
    // ...
    val s = Surface.of[String & Label1]
    s.name shouldBe "String&Label1"
    s.fullName shouldBe "java.lang.String&wvlet.airframe.surface.IntersectionTypeTest.Label1"
    s match { case i: IntersectionSurface =>
      i.left shouldBe Surface.of[String]
      i.right shouldBe Surface.of[Label1]
    }
    s should not be Surface.of[String]
  }
