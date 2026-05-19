package domain

final case class ParkingConfig(
                                capacity: Int,          // Число мест на парковке
                                hourlyRate: Double,     // Тариф за час
                                lostTicketFine: Double, // Штраф за потерянный билет
                                roundUpToHour: Boolean  // Округлять ли вверх
                              )