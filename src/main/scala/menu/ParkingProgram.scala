package menu

import algebras._
import algebras.Monad.MonadOps
import domain._
import domain.ParkingError._
import services.ParkingService

final class ParkingProgram[F[_]](
                                  console: Console[F],
                                  service: ParkingService[F]
                                )(implicit monad: Monad[F]) {

  private def requestCarNumber: F[String] =

    for {
      _ <- console.printLine("Введите номер машины:")

      carNumber <- console.readLine

    } yield carNumber

  private def handleEnter(
                           state: ParkingState,
                           config: ParkingConfig
                         ): F[ParkingState] =

    for {
      carNumber <- requestCarNumber
      result <- service.enterCar(carNumber)

      updatedState <-
        result match {
          case Left(ParkingFull) =>

            for {
              _ <- console.printLine("Парковка заполнена")
              currentState <- service
                .repository
                .getState

            } yield currentState

          case Left(CarAlreadyExists) =>

            for {
              _ <- console.printLine("Машина уже находится на парковке")
              currentState <- service
                .repository
                .getState

            } yield currentState

          case Left(_) =>

            for {
              _ <- console.printLine("Ошибка въезда")
              currentState <- service
                .repository
                .getState

            } yield currentState

          case Right(spot) =>

            for {
              _ <- console.printLine(s"Машина припаркована на месте $spot")
              currentState <- service
                .repository
                .getState

            } yield currentState
        }

    } yield updatedState

  private def handleExit(
                          state: ParkingState,
                          config: ParkingConfig
                        ): F[ParkingState] =

    for {
      carNumber <- requestCarNumber
      result <- service.exitCar(carNumber)

      updatedState <-
        result match {
          case Left(CarNotFound) =>

            for {
              _ <- console.printLine("Машина не найдена")
              currentState <- service
                .repository
                .getState

            } yield currentState

          case Left(_) =>

            for {
              _ <- console.printLine("Ошибка выезда")
              currentState <- service
                .repository
                .getState

            } yield currentState

          case Right(cost) =>

            for {
              _ <- console.printLine(s"Стоимость парковки: $cost")
              currentState <- service
                .repository
                .getState

            } yield currentState
        }

    } yield updatedState

  private def handleLostTicket(
                                state: ParkingState,
                                config: ParkingConfig
                              ): F[ParkingState] =

    for {
      carNumber <- requestCarNumber
      result <- service.reportLostTicket(carNumber)

      updatedState <-
        result match {
          case Left(CarNotFound) =>

            for {
              _ <- console.printLine("Машина не найдена")
              currentState <- service
                .repository
                .getState

            } yield currentState

          case Left(_) =>

            for {
              _ <- console.printLine("Ошибка обработки штрафа")
              currentState <- service
                .repository
                .getState

            } yield currentState

          case Right(fine) =>

            for {
              _ <- console.printLine(s"Штраф за потерю билета: $fine")
              currentState <- service
                .repository
                .getState

            } yield currentState
        }

    } yield updatedState

  private def handleNextHour(
                              state: ParkingState,
                              config: ParkingConfig
                            ): F[ParkingState] =

    for {
      newHour <- service.nextHour

      _ <- console.printLine(s"Текущее время: $newHour")
      updatedState <- service
        .repository
        .getState

    } yield updatedState

  private val mainMenu: MenuTreeNode[F] =

    MenuTreeNode(
      title = "СИСТЕМА УПРАВЛЕНИЯ ПАРКОВКОЙ",
      options = Seq(
        MenuLeaf("Въезд машины", handleEnter),
        MenuLeaf("Выезд машины", handleExit),
        MenuLeaf("Потеря билета", handleLostTicket),
        MenuLeaf("Следующий час", handleNextHour)
      ),
      console = console
    )

  def run(
           initialState: ParkingState,
           config: ParkingConfig
         ): F[ParkingState] =

    mainMenu.execute(
      initialState,
      config
    )
}