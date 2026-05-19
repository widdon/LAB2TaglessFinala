package program

trait MenuOption[F[_], S, C] {

  def title: String

  def execute(
               state: S,
               config: C
             ): F[S]
}