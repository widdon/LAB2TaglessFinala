package interpreters

import algebras.Console

final class ConsoleInterpreter

  extends Console[IO] {

  override def readLine: IO[String] =
    IO.delay(
      scala.io.StdIn.readLine()
    )

  override def printLine(text: String): IO[Unit] =
    IO.delay(
      println(text)
    )
}