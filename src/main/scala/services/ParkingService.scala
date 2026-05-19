package services

import algebras._
import algebras.Monad.MonadOps
import domain._
import domain.ParkingError._

final class ParkingService[F[_]](
                                  console: Console[F],
                                  logger: Logger[F],
                                  val repository: ParkingRepository[F],
                                  configProvider: ParkingLogicProvider[F]
                                )(implicit monad: Monad[F]) {

  import monad._

  private def findFreeSpot(
                            occupied: Set[Int],
                            capacity: Int
                          ): Option[Int] =
    (1 to capacity).find(spot => !occupied.contains(spot))

  private def calculateParkingCost(
                                    entryHour: Int,
                                    currentHour: Int,
                                    config: ParkingConfig
                                  ): Double =

    ParkingCalculator.calculateCost(entryHour, currentHour, config)

  def enterCar(carNumber: String): F[Either[ParkingError, Int]] =

    for {
      state <- repository.getState

      config <- configProvider.getConfig

      result <-
        if (state.entryHours.contains(carNumber)) {

          flatMap(
            logger.error(s"Машина $carNumber уже находится на парковке"))
            {
              _ => pure(Left(CarAlreadyExists))
            }

        } else {

          findFreeSpot(state.occupiedSpots, config.capacity
          ) match {

            case None =>
              flatMap(
                logger.error("Нет свободных мест"))
                {
                  _ => pure(Left(ParkingFull))
                }

            case Some(spot) =>

              val updatedState =
                state.copy(
                  occupiedSpots = state.occupiedSpots + spot,

                  entryHours = state.entryHours + (carNumber -> state.currentHour),

                  spotByCar = state.spotByCar + (carNumber -> spot)
                )

              for {

                _ <- repository.updateState(updatedState)

                _ <- logger.info(s"Машина $carNumber въехала")

                _ <- logger.info(s"Назначено место $spot")

              } yield Right(spot)
          }
        }

    } yield result

  def exitCar(carNumber: String): F[Either[ParkingError, Double]] =

    for {
      state <- repository.getState

      config <- configProvider.getConfig

      result <- state.entryHours.get(carNumber)
      match {

          case None =>

            flatMap(logger.error(s"Машина $carNumber не найдена"))
              {
                _ => pure(Left(CarNotFound))
              }

          case Some(entryHour) =>

            val cost = calculateParkingCost(entryHour, state.currentHour, config)

            val spot = state.spotByCar(carNumber)

            val updatedState =
              state.copy(
                occupiedSpots = state.occupiedSpots - spot,

                entryHours = state.entryHours - carNumber,

                spotByCar = state.spotByCar - carNumber,

                profit = state.profit + cost
              )

            for {

              _ <- repository.updateState(updatedState)

              _ <- logger.info(s"Машина $carNumber выехала")

              _ <- logger.info(s"Стоимость парковки: $cost")

            } yield Right(cost)
        }

    } yield result

  def reportLostTicket(carNumber: String): F[Either[ParkingError, Double]] =

    for {
      state <- repository.getState

      config <- configProvider.getConfig

      result <- state.entryHours.get(carNumber)
      match {

          case None =>

            flatMap(logger.error(s"Машина $carNumber не найдена"))
              {
                _ =>pure(Left(CarNotFound))
              }

          case Some(_) =>

            val fine = config.lostTicketFine

            val spot = state.spotByCar(carNumber)

            val updatedState =
              state.copy(
                occupiedSpots = state.occupiedSpots - spot,

                entryHours = state.entryHours - carNumber,

                spotByCar = state.spotByCar - carNumber,

                profit = state.profit + fine
              )

            for {

              _ <- repository.updateState(updatedState)

              _ <- logger.error(s"Машина $carNumber потеряла билет")

              _ <- logger.info(s"Штраф: $fine")

            } yield Right(fine)
        }

    } yield result

  def nextHour: F[Int] =

    for {

      state <- repository.getState

      newHour = state.currentHour + 1

      updatedState = state.copy(currentHour = newHour)

      _ <- repository.updateState(updatedState)

      _ <- logger.info(s"Текущее время: $newHour")

    } yield newHour

  def showParkingState: F[Unit] =

    for {

      state <- repository.getState

      config <- configProvider.getConfig

      _ <- console.printLine("===== СОСТОЯНИЕ ПАРКОВКИ =====")

      _ <- console.printLine(s"Текущий час: ${state.currentHour}")

      _ <- console.printLine(s"Занято мест: ${state.occupiedSpots.size}")

      _ <- console.printLine(s"Свободно мест: ${
          config.capacity -
            state.occupiedSpots.size
        }"
      )

      _ <- console.printLine(s"Выручка: ${state.profit}")

      _ <- console.printLine("==============================")

    } yield ()
}