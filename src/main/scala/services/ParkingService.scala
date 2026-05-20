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
                                )(using monad: Monad[F]) {

  import monad._

  private def findFreeSpot(occupied: Set[Int], capacity: Int): Option[Int] =
    (1 to capacity)
      .find(spot =>
        !occupied.contains(spot)
      )

  private def currentState: F[ParkingState] =

    repository.getState

  private def enterError(error: ParkingError): F[Either[ParkingError, Int]] =
    pure(Left(error))

  private def exitError(error: ParkingError): F[Either[ParkingError, Double]] =
    pure(Left(error))

  private def successfulEnter(
                               carNumber: String,
                               state: ParkingState,
                               spot: Int
                             ): F[Either[ParkingError, Int]] = {
    val updatedState =

      state.copy(

        occupiedSpots = state.occupiedSpots + spot,
        entryHours = state.entryHours + (carNumber -> state.currentHour),
        spotByCar = state.spotByCar + (carNumber -> spot)
      )

    for {

      _ <- logger.info(s"Машина $carNumber въехала")

      _ <- logger.info(s"Машине $carNumber назначено место $spot")

      _ <- repository.modify { _ =>updatedState}

    } yield Right(spot)
  }

  private def successfulExit(
                              carNumber: String,
                              state: ParkingState,
                              cost: Double
                            ): F[Either[ParkingError, Double]] = {

    val spot = state.spotByCar(carNumber)

    val updatedState =

      state.copy(

        occupiedSpots = state.occupiedSpots - spot,
        entryHours = state.entryHours - carNumber,
        spotByCar = state.spotByCar - carNumber,
        profit = state.profit + cost
      )

    for {

      _ <- logger.info(s"Стоимость парковки: $cost")

      _ <- logger.info(s"Машина $carNumber выехала")

      _ <- repository.modify { _ =>updatedState}

    } yield Right(cost)
  }

  private def successfulLostTicket(
                                    carNumber: String,
                                    state: ParkingState,
                                    fine: Double
                                  ): F[Either[ParkingError, Double]] = {

    val spot = state.spotByCar(carNumber)

    val updatedState =

      state.copy(

        occupiedSpots = state.occupiedSpots - spot,
        entryHours = state.entryHours - carNumber,
        spotByCar = state.spotByCar - carNumber,
        profit = state.profit + fine
      )

    for {

      _ <- logger.info(s"Машина $carNumber потеряла билет")

      _ <- logger.info(s"Штраф: $fine")

      _ <- repository.modify { _ =>updatedState}

    } yield Right(fine)
  }

  def enterCar(carNumber: String): F[Either[ParkingError, Int]] =

    for {
      config <- configProvider.getConfig

      state <- currentState

      result <-

        if (state.entryHours.contains(carNumber))
          enterError(CarAlreadyExists)

        else if (
          state.occupiedSpots.size >=
            config.capacity
        )
          enterError(ParkingFull)

        else

          findFreeSpot(
            state.occupiedSpots,
            config.capacity
          ) match {

            case Some(spot) =>

              successfulEnter(carNumber, state, spot
              )

            case None =>

              enterError(ParkingFull)
          }

    } yield result

  def exitCar(carNumber: String): F[Either[ParkingError, Double]] =

    for {

      config <- configProvider.getConfig

      state <- currentState

      result <-

        state.entryHours
          .get(carNumber) match {

          case None =>

            exitError(CarNotFound)

          case Some(entryHour) =>

            successfulExit(
              carNumber,
              state,
              ParkingCalculator.calculateCost(

                entryHour = entryHour,
                exitHour = state.currentHour,
                hourlyRate = config.hourlyRate,
                roundUp = config.roundUpToHour
              )
            )
        }

    } yield result

  def nextHour: F[Int] =

    for {

      state <- currentState

      newHour = state.currentHour + 1

      updatedState = state.copy(currentHour = newHour)

      _ <- repository.modify { _ =>updatedState}

      _ <- logger.info(s"Текущее время: $newHour")

    } yield newHour

  def reportLostTicket(carNumber: String): F[Either[ParkingError, Double]] =

    for {

      config <- configProvider.getConfig

      state <- currentState

      result <-

        state.entryHours
          .get(carNumber) match {

          case None =>

            exitError(CarNotFound)

          case Some(_) =>

            successfulLostTicket(
              carNumber,

              state,

              config.lostTicketFine
            )
        }

    } yield result

  def showParkingState: F[Unit] =

    for {

      state <- currentState

      _ <- console.printLine(s"Текущий час: ${state.currentHour}")

      _ <- console.printLine(s"Занято мест: ${state.occupiedSpots.size}")

      _ <- console.printLine(s"Выручка: ${state.profit}")

    } yield ()
}