/*
rule = Unused
*/
package fix.example

import scala.concurrent.Future // assert: Unused

object Example { self =>
  new scala.collection.mutable.Stack[Int]()
  def main(args: Array[String]): Unit = {
    println(1)
  }
  val x = scala.reflect.classTag[Int]
}
