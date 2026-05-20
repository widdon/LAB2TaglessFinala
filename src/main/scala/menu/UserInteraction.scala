package menu

import algebras.Console
import algebras.Monad
import algebras.Monad.MonadOps

trait UserInteraction[F[_], S, C] {
  implicit def monad: Monad[F]

  def console: Console[F]

  def show(state: S): String

  def handleInput(
                   input: String,
                   state: S,
                   config: C
                 ): F[Option[S]]

  def readInput: F[String] = console.readLine

  def userInteractionLoop(
                           state: S,
                           config: C
                         ): F[S] =

    for {

      _ <- console.printLine(
        show(state)
      )

      input <- readInput

      next <- handleInput(
        input,
        state,
        config
      )

      result <-

        next match {

          case Some(newState) =>
            userInteractionLoop(
              newState,
              config
            )

          case None =>
            monad.pure(state)
        }

    } yield result
}