/*
rule = Unused
 */
package fix.example

class ForComprehension {
  object Foo {
    for {
      x <- Some(1 -> 2)
      y = 3
      z = 3 // assert: Unused
      (a, b) = x
    } yield (a + b + y)
  }

  for {
    a <- List(1)
    b <- List(1)
    if b > 1
    c = a + b
  } yield (a, b, c)
  for {
    a <- List(1)
    b <- List(a)
    if (
      a,
      b
    ) == (1, 2)
    (
      c,
      d
    ) <- List((a, b))
    if (
      a,
      b,
      c,
      d
    ) == (1, 2, 3, 4)
    e = (
      a,
      b,
      c,
      d
    )
    if e == (1, 2, 3, 4)
    f <- List(e) // assert: Unused
  } yield {
    (
      a,
      b,
      c,
      d,
      e
    )
  }
}
