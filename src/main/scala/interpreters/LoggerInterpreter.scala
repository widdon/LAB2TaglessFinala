package interpreters

import algebras.Logger

final class LoggerInterpreter

  extends Logger[IO] {

  override def info(message: String): IO[Unit] = 
    IO.delay(
      println(s"[LOG] $message")
    )

  override def error(message: String): IO[Unit] = 
    IO.delay(
      println(s"[ERROR] $message")
    )
}