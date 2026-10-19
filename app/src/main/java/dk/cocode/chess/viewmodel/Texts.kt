package dk.cocode.chess.viewmodel

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * The app's text by resource id, so the view models and the speech helpers say what the resources say
 * (and so what a translation says) without holding a Context.
 */
interface Texts {
    fun string(@StringRes id: Int, vararg args: Any): String

    fun plural(@PluralsRes id: Int, quantity: Int, vararg args: Any): String
}

/** [Texts] read from [resources]. */
class ResourceTexts(private val resources: Resources) : Texts {
    override fun string(@StringRes id: Int, vararg args: Any): String = resources.getString(id, *args)

    override fun plural(@PluralsRes id: Int, quantity: Int, vararg args: Any): String =
        resources.getQuantityString(id, quantity, *args)
}
