/*
rule = Unused
 */
package fix.example

object X {
  object Y
  private object Z // assert: Unused
}
