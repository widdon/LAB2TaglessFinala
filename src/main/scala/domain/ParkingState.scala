package domain

final case class ParkingState(

                               occupiedSpots: Set[Int],

                               entryHours: Map[String, Int],

                               spotByCar: Map[String, Int],

                               currentHour: Int,

                               profit: Double
                             )

object ParkingState {

  def empty: ParkingState =
    ParkingState(
      occupiedSpots = Set.empty,
      entryHours = Map.empty,
      spotByCar = Map.empty,
      currentHour = 0,
      profit = 0.0
    )
}