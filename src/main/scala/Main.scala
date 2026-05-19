import algebras._
import domain._
import interpreters._
import menu._
import services._

object Main {

  def main(args: Array[String]): Unit = {

    implicit val monad: Monad[IO] = IOMonad.ioMonad

    val parkingConfig =
      ParkingConfig(
        capacity = 10,
        hourlyRate = 100.0,
        lostTicketFine = 5000.0,
        roundUpToHour = true
      )

    val initialState = ParkingState.empty

    val consoleInterpreter = new ConsoleInterpreter

    val loggerInterpreter = new LoggerInterpreter

    val repositoryInterpreter = new ParkingRepositoryInterpreter(initialState)

    val configInterpreter = new ParkingConfigInterpreter(parkingConfig)

    val parkingService =
      new ParkingService[IO](

        console = consoleInterpreter,
        logger = loggerInterpreter,
        repository = repositoryInterpreter,
        configProvider = configInterpreter
      )

    val parkingProgram =
      new ParkingProgram[IO](

        console = consoleInterpreter,
        service = parkingService
      )

    parkingProgram
      .run(initialState, parkingConfig)
      .unsafeRun()
  }
}