/*
rule = Unused
Unused.params = false
Unused.locals = true
Unused.imports = false
Unused.privates = false
Unused.patvars = false
*/
package fix

trait Ord[T]:
  def compare(x: T, y: T): Int
  extension (x: T) def < (y: T) = compare(x, y) < 0
  extension (x: T) def > (y: T) = compare(x, y) > 0

object Pos:
  def locals1() =
    val x = 1 /* assert: Unused
    ^^^^^^^^^
  Unused definition 'x'
    */
    println(1)
  
  def localsGivenAlias() =
    // TODO: false-negative
    given x: Int = 1
    println(1)
  
  def localsAnonymousGiven() =
    given Int = 1 /* aasert: Unused
    ^^^^^^^^^^^^^^^^
Unused definition ''
  */
    println(1)
  
  def localEnum =
    enum Foo: /* assert: Unused
    ^
Unused definition 'Foo'
  */
      case A, B
  
  def localGiven =
    given intOrd: Ord[Int] with
      def compare(x: Int, y: Int) = /* assert: Unused
      ^
Unused definition 'compare'
      */
        if x < y then -1 else if x > y then +1 else 0
    println(1)
  
  def localExtension =
    extension (x: Int)
      def incl = x + 1 /* assert: Unused
      ^^^^^^^^^^^^^^^^
Unused definition 'incl'
      */
    println(1)
end Pos 

object Neg:
  def locals1() =
    val x = 1
    println(x)
  
  def localsGivenAlias() =
    given x: Int = 1
    println(x)
  
  def localsAnonymousGiven() =
    given Int = 1
    println(given_Int) // TODO: test with synthetics
  
  def localEnum =
    // TODO: false-positive
    enum Foo: // assert: Unused
      case A, B
    println(Foo.A)
  
  def localGiven =
    given intOrd: Ord[Int] with
      // TODO false positive
      def compare(x: Int, y: Int) = // assert: Unused
        if x < y then -1 else if x > y then +1 else 0
    def compare[T: Ord](a: T, b: T) = ???
    compare(1, 2)(intOrd)
  
  def localExtension =
    extension (x: Int)
      def incl = x + 1
    println(1.incl)
end Neg

