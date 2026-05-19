package menu

import algebras.Console
import algebras.Monad
import algebras.Monad.MonadOps
import domain._

final case class MenuTreeNode[F[_]](
                                     title: String,
                                     options: Seq[MenuOption[F, ParkingState, ParkingConfig]],
                                     console: Console[F]
                                   )(implicit val monad: Monad[F])

  extends MenuOption[F, ParkingState, ParkingConfig]
    with UserInteraction[F, ParkingState, ParkingConfig] {

  override def execute(
                        state: ParkingState,
                        config: ParkingConfig
                      ): F[ParkingState] =
    userInteractionLoop(state, config)

  override def show(state: ParkingState): String = {

    val items =
      options.zipWithIndex
        .map {
          case (option, index) =>
            s"${index + 1}  ${option.title}"
        }
        .mkString("\n")

    s"""
       |--- $title ---
       |Текущий час: ${state.currentHour}
       |Занято мест: ${state.occupiedSpots.size}
       |Выручка: ${state.profit}
       |
       |$items
       |0  выход
       |выбор:
       |""".stripMargin
  }

  override def handleInput(
                            input: String,
                            state: ParkingState,
                            config: ParkingConfig
                          ): F[Option[ParkingState]] = {

    input.trim.toIntOption match {

      case Some(0) =>
        for {
          _ <- console.printLine(s"Работа завершена. Итоговая выручка: ${state.profit}")
        } yield None

      case Some(index)

        if index >= 1 &&
          index <= options.size =>

        options(index - 1)
          .execute(state, config)
          .map(newState =>
            Some(newState)
          )

      case _ =>
        console.printLine("Неизвестная команда").map(_ => Some(state))
    }
  }
}