/*
rule = Unused
 */

package fix.fromscala2
package privates
package scala2

class StableAccessors {
  private var s1: Int = 0 // assert: Unused
  // TODO?
  private var s2: Int = 0 // warn, never set
  // TODO: warn never get
  private var s3: Int = 0
  private var s4: Int = 0 // no warn

  def bippy(): Int = {
    s3 = 5
    s4 = 6
    s2 + s4
  }
}

object `classof something` {
  private class intrinsically
  def f = classOf[intrinsically].toString()
}
