package algebras

import domain.ParkingState

trait ParkingRepository[F[_]] {

  def getState: F[ParkingState]

  def updateState(
                   state: ParkingState
                 ): F[Unit]
}