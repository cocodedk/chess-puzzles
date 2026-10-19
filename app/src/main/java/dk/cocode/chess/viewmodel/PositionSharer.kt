package dk.cocode.chess.viewmodel

/** Hands text to another app; behind an interface so the view model is tested without a device. */
fun interface PositionSharer {
    fun share(text: String)
}
