package algebras

import domain.ParkingState

trait ParkingRepository[F[_]] {

  def getState: F[ParkingState]

  def modify(update: ParkingState => ParkingState): F[ParkingState]
}
