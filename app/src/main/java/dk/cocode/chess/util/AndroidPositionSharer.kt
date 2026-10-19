package dk.cocode.chess.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import dk.cocode.chess.R
import dk.cocode.chess.viewmodel.PositionSharer

/** Opens the system share sheet with the text as plain text. */
class AndroidPositionSharer(private val context: Context) : PositionSharer {
    override fun share(text: String) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        val chooser = Intent.createChooser(send, context.getString(R.string.position_share))
        // Only an activity may start one without a task of its own.
        if (context !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
