package dk.cocode.chess.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import dk.cocode.chess.viewmodel.ResourceTexts
import dk.cocode.chess.viewmodel.Texts

/** The app's text in the language the screen is in now; it is looked up again when the language changes. */
@Composable
internal fun rememberTexts(): Texts {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(context, configuration) { ResourceTexts(context.resources) }
}
