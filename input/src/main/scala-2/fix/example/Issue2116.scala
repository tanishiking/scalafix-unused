/*
rule = Unused
 */
package fix.example

// See https://github.com/scalameta/scalameta/issues/2116
package example

import scala.concurrent.ExecutionContext

abstract class Issue2116 {

  def check(
      includeDocs: Boolean = false, // assert: Unused
      includeCommitCharacter: Boolean =
        false // unreported because there's named argument
  )(implicit loc: ExecutionContext): Unit = {} // assert: Unused
}

class Issue2116_2 extends Issue2116 {

  implicit val ec = scala.concurrent.ExecutionContext.global

  check(
    includeCommitCharacter = true
  )

}
