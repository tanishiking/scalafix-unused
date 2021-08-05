/*
rule = Unused
*/
package fix.example
package prefixes3

class C {
  type T
  def m1: T = ???

  object N {
    type U
  }
  def k1: N.U = ???
}

object M {
  type T
  def n1: T = ???
}

object O extends C {
  def o1: T = ???
}

object Test {
  val c: C = ???
  def m2: c.T = ???
  def k2: c.N.U = ???
  // TODO: fix c.N._ shouldn't be reported
  // after fix the issue, merge Prefixes.scala and this file
  // import c.N._
  // def k3: U = ???

  def n2: M.T = ???

  import M._
  def n3: T = ???
}
