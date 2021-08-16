/*
rule = Unused
 */
package fix.example

import scala.math.Ordering
import scala.language.existentials

class Methods[T] {
  class List[T]
  type AList[T] = List[T]
  def m1 = ???
  def m2() = ???
  def m3(x: Int) = ??? // assert: Unused
  def m4(x: Int)(y: Int) = ??? // assert: Unused
  def m5(x: String) = ??? // assert: Unused
  def m5(x: Int) = ??? // assert: Unused
  def m6(x: Int) = ??? // assert: Unused
  def m6(x: List[T]) = ??? // assert: Unused
  def m6(x: scala.List[T]) = ??? // assert: Unused
  def m7a[U: Ordering](c: Methods[T], l: List[U]) = ??? // assert: Unused
  // def m7b[U <% T](l: List[U]) = ???
  def `m8().`() = ???
  class `m9().`
  def m9(x: `m9().`) = ??? // assert: Unused
  def m10(x: AList[T]) = ??? // assert: Unused
  def m11(x: Predef.type) = ??? // assert: Unused
  def m11(x: Example.type) = ??? // assert: Unused
  def m12a(x: {}) = ??? // assert: Unused
  def m12b(x: { val x: Int }) = ??? // assert: Unused
  def m13(x: Int @unchecked) = ??? // assert: Unused
  // def m14(x: T forSome { type T }) = ???
  def m15(x: => Int) = ??? // assert: Unused
  def m16(x: Int*) = ??? // assert: Unused
  object m17 { def m() = ??? }
  def m17(a: Int) = ??? // assert: Unused
  def m17(b: String) = ??? // assert: Unused
  val m18 = m17
  def m18(a: Int) = ??? // assert: Unused
  def m18(b: String) = ??? // assert: Unused
  def m19(x: Int, y: Int = 2)(z: Int = 3) = ??? // assert: Unused
}
