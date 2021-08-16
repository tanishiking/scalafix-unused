/*
rule = Unused
 */
package fix.example
package overrides

trait A { def foo: Int }
class B() extends A { def foo: Int = 2 }
