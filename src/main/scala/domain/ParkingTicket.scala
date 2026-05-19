package domain

final case class ParkingTicket(

                                carNumber: String,

                                spot: Int,

                                entryHour: Int
                              )