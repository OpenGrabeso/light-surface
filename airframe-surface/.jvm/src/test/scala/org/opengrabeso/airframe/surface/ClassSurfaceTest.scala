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

import org.opengrabeso.airframe.surface.tag.*

object ClassSurfaceTest {
  class A(val id: Int)(implicit val context: String)

  trait MyTag
  case class B(v: Int @@ MyTag)
}

import org.opengrabeso.airframe.surface.ClassSurfaceTest.*

class ClassSurfaceTest extends SurfaceSpec {
  test("support multiple param blocks") {
    val a = check(Surface.of[A], "A")
    debug(a.params.mkString(", "))

    assert(a.params.length == 2)

    val p0 = a.params(0)
    val p1 = a.params(1)
    assert(p0.name == "id")
    assert(p1.name == "context")
  }

  test("support tags in constructor args") {
    // TODO support this in Scala.js
    check(Surface.of[Int @@ MyTag], "Int@@MyTag")
    val b = check(Surface.of[B], "B")
    assert(b.params.length == 1)
    val p = b.params(0)
    check(p.surface, "Int@@MyTag")
  }
}