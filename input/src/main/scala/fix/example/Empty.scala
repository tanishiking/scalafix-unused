/*
rule = Unused
 */
package fix.example

class A {
  def b: B = ???
}

class B {
  def a: A = ???
}
