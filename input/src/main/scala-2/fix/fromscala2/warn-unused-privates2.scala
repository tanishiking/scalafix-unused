/*
rule = Unused
 */
package fix.fromscala2
package privates
package scala2

trait Bing

/** * Early defs warnings disabled primarily due to scala/bug#6595. The test
  * case is here to assure we aren't issuing false positives; the ones labelled
  * "warn" don't warn.
  */
class Boppy
    extends {
      private val hmm: String = "abc" // no warn, used in early defs
      private val hom: String = "def" // no warn, used in body
      private final val him = "ghi" // no warn, might have been (was) inlined
      final val him2 = "ghi" // no warn, same
      final val himinline = him
      private val hum: String = "jkl" // should warn?
      final val ding = hmm.length
    }
    with Bing {
  val dinger = hom
  private val hummer = "def" // assert: Unused

  // TODO
  // no warn, might have been (was) inlined
  private final val bum = "ghi" // assert: Unused
  final val bum2 = "ghi"
}

class StableAccessors {
  private var s1: Int = 0 // assert: Unused
  // TODO?
  private var s2: Int = 0 // warn, never set
  // warn never get
  private var s3: Int = 0 // assert: Unused
  private var s4: Int = 0 // no warn

  def bippy(): Int = {
    s3 = 5
    s4 = 6
    s2 + s4
  }
}

object `classof something` {
  // TODO
  private class intrinsically // assert: Unused
  def f = classOf[intrinsically].toString()
}
