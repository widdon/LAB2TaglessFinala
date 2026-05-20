package domain

object ParkingCalculator {

  def calculateHours(
                      entryHour: Int,
                      exitHour: Int,
                      roundUp: Boolean
                    ): Int = {

    val rawHours =
      exitHour - entryHour

    if (roundUp)
      math.max(rawHours, 1)

    else
      rawHours
  }

  def calculateCost(
                     entryHour: Int,
                     exitHour: Int,
                     hourlyRate: Double,
                     roundUp: Boolean
                   ): Double = {

    val hours =

      calculateHours(
        entryHour = entryHour,
        exitHour = exitHour,
        roundUp = roundUp
      )

    hours * hourlyRate
  }
}