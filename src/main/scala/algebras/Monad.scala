package algebras

trait Monad[F[_]] {

  def pure[A](value: A): F[A]

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  def map[A, B](fa: F[A])(f: A => B): F[B] =
    flatMap(fa)(a => pure(f(a)))
}

object Monad {

  def apply[F[_]](implicit monad: Monad[F]): Monad[F] =
    monad

  implicit class MonadOps[F[_], A](fa: F[A])(implicit monad: Monad[F]) {

    def flatMap[B](f: A => F[B]): F[B] =
      monad.flatMap(fa)(f)

    def map[B](f: A => B): F[B] =
      monad.map(fa)(f)
  }
}