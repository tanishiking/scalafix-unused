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
  // this is because `U` doesn't have symbol
  // see: https://github.com/lampepfl/dotty/blob/e7a641c5ad61fa683423954fa9263079a890c809/tests/semanticdb/expect/Prefixes.expect.scala#L27
  // after fix the issue, merge Prefixes.scala and this file
  //
  // import c.N._
  // def k3: U = ???

  def n2: M.T = ???

  import M._
  def n3: T = ???
}
