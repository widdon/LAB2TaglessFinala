package interpreters

import algebras.ParkingRepository
import domain.ParkingState

final class ParkingRepositoryInterpreter(
                                          private var state: ParkingState
                                        ) extends ParkingRepository[IO] {

  override def getState: IO[ParkingState] =
    IO.pure(state)

  override def updateState(
                            newState: ParkingState
                          ): IO[Unit] =
    IO.delay {
      state = newState
    }
}