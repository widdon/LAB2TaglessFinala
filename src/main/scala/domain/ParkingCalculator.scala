package domain

object ParkingCalculator {

  def calculateHours(entryHour: Int, currentHour: Int): Double =
    currentHour - entryHour

  def normalizeHours(hours: Double, config: ParkingConfig): Double =
    if (config.roundUpToHour)
      math.ceil(hours)
    else
      hours

  def calculateCost(entryHour: Int, currentHour: Int, config: ParkingConfig): Double = {

    val hours = calculateHours(entryHour, currentHour)

    val normalized = normalizeHours(hours, config)

    normalized * config.hourlyRate
  }
}