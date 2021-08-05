/*
rule = Unused
*/
package fix.example

object X {
  object Y
  // TODO: it should be reported also in Scala3
  private object Z // assert: Unused
}
