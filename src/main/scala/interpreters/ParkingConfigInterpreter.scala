package interpreters

import algebras.ParkingLogicProvider
import domain.ParkingConfig

final class ParkingConfigInterpreter(

                                      config: ParkingConfig

                                    ) extends ParkingLogicProvider[IO] {

  override def getConfig: IO[ParkingConfig] =
    IO.pure(config)
}