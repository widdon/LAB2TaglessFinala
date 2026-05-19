package algebras

trait Logger[F[_]] {

  def info(
            message: String
          ): F[Unit]

  def error(
             message: String
           ): F[Unit]
}