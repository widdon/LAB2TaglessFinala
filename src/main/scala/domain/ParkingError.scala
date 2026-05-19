package domain

sealed trait ParkingError

object ParkingError {

  final case object ParkingFull
    extends ParkingError

  final case object CarAlreadyExists
    extends ParkingError

  final case object CarNotFound
    extends ParkingError

  final case object FreeSpotNotFound
    extends ParkingError
}