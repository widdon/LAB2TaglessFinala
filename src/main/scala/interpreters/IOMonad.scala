package interpreters

import algebras.Monad

object IOMonad:

  given Monad[IO] with
    override def pure[A](value: A): IO[A] =
      IO.pure(value)

    override def flatMap[A, B](fa: IO[A])(f: A => IO[B]): IO[B] =
      fa.flatMap(f)