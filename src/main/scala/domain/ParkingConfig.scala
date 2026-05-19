package domain

final case class ParkingConfig(

                                capacity: Int,

                                hourlyRate: Double,

                                lostTicketFine: Double,

                                roundUpToHour: Boolean
                              )