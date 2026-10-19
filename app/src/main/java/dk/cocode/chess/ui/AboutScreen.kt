package dk.cocode.chess.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R

/**
 * The About screen, in the cocode-apps standard's order: name, version and the button to the latest version,
 * what the app does, privacy, links, credits and licenses, made by Cocode. Each title is a screen-reader heading.
 */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull().orEmpty()
    }
    val open: (AboutLink) -> Unit = { link ->
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(aboutUrl(link)))) }
    }
    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
            )
            NameAndVersion(version, open)
            AboutText(R.string.about_section_what, R.string.about_what)
            Privacy(open)
            Links(open)
            Credits()
            MadeBy(open)
            // The Support slot goes here, after Made by Cocode: empty until the Support phase.
            TextButton(onClick = onBack) { Text(stringResource(R.string.about_back)) }
        }
    }
}

@Composable
private fun NameAndVersion(version: String, open: (AboutLink) -> Unit) {
    Title(R.string.app_name, MaterialTheme.typography.headlineSmall, top = 0)
    Text(stringResource(R.string.about_version, version), style = MaterialTheme.typography.bodySmall)
    Button(onClick = { open(AboutLink.Updates) }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.about_check_updates))
    }
    Note(R.string.about_check_updates_note)
}

@Composable
private fun Privacy(open: (AboutLink) -> Unit) {
    Title(R.string.about_section_privacy)
    listOf(
        R.string.about_privacy_no_data, R.string.about_privacy_no_permissions,
        R.string.about_privacy_no_tracking, R.string.about_privacy_on_device,
    ).forEach { Body(it) }
    LinkButton(R.string.about_privacy_link) { open(AboutLink.Privacy) }
}

@Composable
private fun Links(open: (AboutLink) -> Unit) {
    Title(R.string.about_section_links)
    LinkButton(R.string.about_website) { open(AboutLink.Website) }
    LinkButton(R.string.about_source) { open(AboutLink.Source) }
    LinkButton(R.string.about_report) { open(AboutLink.Issues) }
}

@Composable
private fun Credits() {
    Title(R.string.about_credits)
    Note(R.string.about_credits_body)
    Note(R.string.about_license)
}

@Composable
private fun MadeBy(open: (AboutLink) -> Unit) {
    Title(R.string.about_section_made_by)
    TextButton(onClick = { open(AboutLink.MadeBy) }) { Text(stringResource(R.string.about_made_by)) }
    TextButton(onClick = { open(AboutLink.LinkedIn) }) { Text(stringResource(R.string.about_linkedin)) }
}

@Composable
private fun AboutText(@StringRes title: Int, @StringRes body: Int) {
    Title(title)
    Body(body)
}

@Composable
private fun Title(
    @StringRes text: Int,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    top: Int = 12,
) = Text(
    stringResource(text), style = style, textAlign = TextAlign.Center,
    modifier = Modifier.padding(top = top.dp).semantics { heading() },
)

@Composable
private fun Body(@StringRes text: Int) = Text(stringResource(text), textAlign = TextAlign.Center)

@Composable
private fun Note(@StringRes text: Int) =
    Text(stringResource(text), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)

@Composable
private fun LinkButton(@StringRes label: Int, onClick: () -> Unit) =
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(stringResource(label)) }
