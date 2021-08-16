/*
rule = Unused
 */
package fix.example

class Vararg {
  def add1(a: Int*) = {} // assert: Unused
  def add2(a: Seq[Int]*): Unit = {} // assert: Unused
}
