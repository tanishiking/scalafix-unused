/*
rule = Unused
*/
package fix.example
package ext

extension (s: String)
  def foo: Int = 42
  def #*# (i: Int): (String, Int) = (s, i)

extension (i: Int)
  def foo(x: Int): Int = 42 // assert: Unused

val a = "asd".foo

val c = "foo" #*# 23
