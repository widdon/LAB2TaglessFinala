package interpreters

final case class IO[A](unsafeRun: () => A) {

  def map[B](f: A => B): IO[B] =

    IO(() =>
      f(unsafeRun())
    )

  def flatMap[B](f: A => IO[B]): IO[B] =

    IO(() =>
      f(unsafeRun()).unsafeRun()
    )
}

object IO {

  def pure[A](value: A): IO[A] =
    IO(() => value)

  def delay[A](body: => A): IO[A] =
    IO(() => body)
}