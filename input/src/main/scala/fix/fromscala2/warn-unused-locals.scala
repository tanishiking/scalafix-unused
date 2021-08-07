/*
rule = Unused
*/
package fix.fromscala2
package locals

// scalac: -Wunused:locals -Werror

class Outer {
  class Inner
}

trait Locals {
  def f0 = {
    var x = 1 // assert: Unused
    var y = 2 // no warn
    y = 3
    y + y
  }
  def f1 = {
    val a = new Outer // no warn
    val b = new Outer // assert: Unused
    new a.Inner
  }
  def f2 = {
    var x = 100 // warn about it being a var
    x
  }
}

object Types {
  def l1() = {
    object HiObject { def f = this } // assert: Unused
    class Hi { // assert: Unused
      def f1: Hi = new Hi
      def f2(x: Hi) = x
    }
    class DingDongDoobie // assert: Unused
    class Bippy // no warn
    type Something = Bippy // no warn
    type OtherThing = String // assert: Unused
    (new Bippy): Something
  }
}
