package interpreters

import algebras.ParkingRepository
import domain.ParkingState

final class ParkingRepositoryInterpreter(initialState: ParkingState
                                        ) extends ParkingRepository[IO] {

  private var state: ParkingState = initialState

  override def getState: IO[ParkingState] = IO.pure(state)

  override def modify(update: ParkingState => ParkingState): IO[ParkingState] =
  IO.delay {

    val updatedState = update(state)

    state = updatedState

    updatedState
  }
}