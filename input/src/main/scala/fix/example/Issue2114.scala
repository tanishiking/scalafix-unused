/*
rule = Unused
*/
package fix.example

// See https://github.com/scalameta/scalameta/issues/2114
package example
object Issue2144 {
  class Test(a: Boolean, b: Int = 1, c: Int = 2)
  val x = new Test(a = true, c = 1)
}
