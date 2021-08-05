/*
rule = Unused
*/
package fix.example

class Access {
  private def m1 = ??? // assert: Unused
  private[this] def m2 = ???
  private[Access] def m3 = ???
  protected def m4 = ???
  protected[this] def m5 = ???
  protected[example] def m6 = ???
  def m7 = ???
}
