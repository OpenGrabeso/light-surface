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

import java.math.BigInteger
import scala.concurrent.Future
import scala.util.Try

object Examples {
  case class A(
      b: Boolean,
      bt: Byte,
      st: Short,
      i: Int,
      l: Long,
      f: Float,
      d: Double,
      str: String
  )

  case class B(a: A)

  type MyA = A

  trait C

  type MyInt = Int
  type MyMap = Map[Int, String]

  case class D[V](id: Int, v: V)

  trait Service[-Req, +Rep] extends (Req => Future[Rep])

  case class E(a: A)
  case class F(p0: Int = 10)
}

import org.opengrabeso.airframe.surface.Examples._

/**
  */
class SurfaceTest extends SurfaceSpec {
  test("resolve types") {
    val a = check(Surface.of[A], "A")
    assert(a.isAlias == false)
    assert(a.isOption == false)
    assert(a.isPrimitive == false)

    val b = check(Surface.of[B], "B")
    assert(b.isAlias == false)
    assert(b.isOption == false)
    assert(b.isPrimitive == false)
  }

  test("resolve primitive types") {
    checkPrimitive(Surface.of[Boolean], "Boolean")
    checkPrimitive(Surface.of[Byte], "Byte")
    checkPrimitive(Surface.of[Short], "Short")
    checkPrimitive(Surface.of[Int], "Int")
    checkPrimitive(Surface.of[Long], "Long")
    checkPrimitive(Surface.of[Float], "Float")
    checkPrimitive(Surface.of[Double], "Double")
    checkPrimitive(Surface.of[String], "String")
    checkPrimitive(Surface.of[Char], "Char")
    checkPrimitive(Surface.of[java.lang.String], "String")
  }

  test("find primitive Surfaces") {
    Primitive(classOf[Int]) shouldBe Primitive.Int
  }

  test("be equal") {
    val a1 = Surface.of[A]
    val a2 = Surface.of[A]

    // In Scala 3, Surface instance identity is not guaranteed
    // assert(a1 eq a2)

    // equality
    assert(a1 == a2)
    assert(a1.hashCode() == a2.hashCode())

    val b  = Surface.of[B]
    val a3 = b.params.head.surface

    // assert(a1 eq a3)

    // Generic surface
    val c1 = Surface.of[Seq[Int]]
    val c2 = Surface.of[Seq[Int]]
    assert(c1.equals(c2) == true)
    // assert(c1 eq c2)
    assert(c1.hashCode() == c2.hashCode())

    assert(c1 ne a1)
    assert(c1.equals(a1) == false)
    assert(c1.equals("hello") == false)
  }

  test("resolve alias") {
    val a1 = check(Surface.of[MyA], "MyA:=A")
    assert(a1.isAlias == true)
    assert(a1.isOption == false)

    val a2 = check(Surface.of[MyInt], "MyInt:=Int")
    assert(a2.isAlias == true)
    assert(a1.isOption == false)

    val a3 = check(Surface.of[MyMap], "MyMap:=Map[Int,String]")
    assert(a3.isAlias == true)
    assert(a1.isOption == false)
  }

  test("resolve trait") {
    check(Surface.of[C], "C")
  }

  test("resolve array types") {
    check(Surface.of[Array[Int]], "Array[Int]")
    check(Surface.of[Array[Byte]], "Array[Byte]")
    check(Surface.of[Array[A]], "Array[A]")
  }

  test("resolve option types") {
    val opt = check(Surface.of[Option[A]], "Option[A]")
    assert(opt.isOption == true)
  }

  test("resolve collection types") {
    check(Surface.of[Seq[A]], "Seq[A]")
    check(Surface.of[List[A]], "List[A]")
    check(Surface.of[Map[String, A]], "Map[String,A]")
    check(Surface.of[Map[String, Long]], "Map[String,Long]")
    check(Surface.of[Map[Long, B]], "Map[Long,B]")
    check(Surface.of[Set[String]], "Set[String]")
    check(Surface.of[IndexedSeq[A]], "IndexedSeq[A]")
  }

  test("resolve scala util types") {
    check(Surface.of[Either[String, Throwable]], "Either[String,Throwable]")
    check(Surface.of[Try[A]], "Try[A]")
  }

  test("resolve mutable Collection types") {
    check(Surface.of[collection.mutable.Seq[String]], "Seq[String]")
    check(Surface.of[collection.mutable.Map[Int, String]], "Map[Int,String]")
    check(Surface.of[collection.mutable.Set[A]], "Set[A]")
  }

  test("resolve tuples") {
    check(Surface.of[Tuple1[Int]], "Tuple1[Int]")
    check(Surface.of[(Int, String)], "Tuple2[Int,String]")
    check(Surface.of[(Int, String, A, Double)], "Tuple4[Int,String,A,Double]")
  }

  test("resolve java colletion type") {
    check(Surface.of[java.util.List[String]], "List[String]")
    check(Surface.of[java.util.Map[Long, String]], "Map[Long,String]")
    check(Surface.of[java.util.Set[A]], "Set[A]")
  }

  test("resolve generic type") {
    val d1 = check(Surface.of[D[String]], "D[String]")
    val d2 = check(Surface.of[D[A]], "D[A]")
    assert(d1 ne d2, "should not be the same instance")
  }

  test("resolve recursive type") {
    check(Surface.of[Service[Int, String]], "Service[Int,String]")
  }

  test("resolve generic abstract type") {
    Surface.of[D[_]].typeArgs shouldBe Seq(ExistentialType)
    val d = check(Surface.of[D[_]], "D[_]")
    d.typeArgs.length shouldBe 1
    check(Surface.of[Map[_, _]], "Map[_,_]")
  }

  test("bigint") {
    Surface.of[BigInt]
    Surface.of[BigInteger]
  }

  test("resolve types args of Map[String, Any]") {
    val s = Surface.of[Map[String, Any]]
    s.typeArgs(0).fullName shouldBe "java.lang.String"
    s.typeArgs(1).fullName shouldBe "scala.Any"
  }
}
