package program

final case class MenuLeaf[F[_], S, C](
   title: String,
   action: (S, C) => F[S]
 ) extends MenuOption[F, S, C] {

  override def execute(
                        state: S,
                        config: C
                      ): F[S] =

    action(state, config)
}