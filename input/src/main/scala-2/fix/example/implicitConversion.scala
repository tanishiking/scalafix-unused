/*
rule = Unused
*/
package fix.example

import scala.language.implicitConversions

class ImplicitConversion {
  private implicit class RichInt(x: Int) {
    def increment = x + 1
  }
  private implicit class RichString(str: String) { // assert: Unused
    def append(other: String) = str ++ other
  }
  implicit def string2Number(
      string: String // assert: Unused
  ): Int = 42
  val message = ""
  val number = 42
  val tuple = (1, 2)
  val char: Char = 'a'

  // extension methods
  message
    .stripSuffix("h")
  tuple + "Hello"

  // implicit conversions
  val x: Int = message

  // interpolators
  s"Hello $message $number"
  s"""Hello
     |$message
     |$number""".stripMargin

  val a: Int = char
  val b: Long = char

  val y = 1.increment
}
