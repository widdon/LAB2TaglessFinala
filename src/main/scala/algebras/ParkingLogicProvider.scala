package algebras

import domain.ParkingConfig

trait ParkingLogicProvider[F[_]] {

  def getConfig: F[ParkingConfig]
}