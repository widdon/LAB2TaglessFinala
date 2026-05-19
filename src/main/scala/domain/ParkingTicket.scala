package domain

final case class ParkingTicket(
                                carNumber: String,  // Номер авто
                                spot: Int,          // Занятое место авто
                                entryHour: Int      // Час, в который вьехало авто
                              )