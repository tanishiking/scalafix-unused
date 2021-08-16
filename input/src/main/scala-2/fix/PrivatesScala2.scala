/*
rule = Unused
 */
package fix

// TODO: support scala3
case class PrivateClass(
    private val x: Int,
    private val y: Int // assert: Unused
) {
  def foo = x
}
