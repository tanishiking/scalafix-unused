/*
rule = Unused
*/
package fix.contextual

trait SemiGroup[T]:
  extension (x: T) def combine (y: T): T

trait Monoid[T] extends SemiGroup[T]:
  def unit: T

trait Functor[F[_]]:
  extension [A](x: F[A])
    def map[B](f: A => B): F[B]

trait Monad[F[_]] extends Functor[F]:
  def pure[A](x: A): F[A]
  extension [A](x: F[A])
    def flatMap[B](f: A => F[B]): F[B]
    def map[B](f: A => B) = x.flatMap(f.andThen(pure))
end Monad

given Monoid[String] with
  extension (x: String) def combine (y: String): String = x.concat(y)
  def unit: String = ""

given Monoid[Int] with
  extension (x: Int) def combine (y: Int): Int = x + y
  def unit: Int = 0  

given Functor[List] with
  extension [A](xs: List[A])
    def map[B](f: A => B): List[B] =
      xs.map(f)

given listMonad: Monad[List] with
  def pure[A](x: A): List[A] = List(x)
  extension [A](xs: List[A])
    def flatMap[B](f: A => List[B]): List[B] =
      xs.flatMap(f)

def combineAll[T: Monoid](xs: List[T]): T =
  xs.foldLeft(summon[Monoid[T]].unit)(_.combine(_))

def assertTransformation[F[_]: Functor, A, B](expected: F[B], original: F[A], mapping: A => B): Unit =
  assert(expected == original.map(mapping))
