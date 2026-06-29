package dk.cocode.chess.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dk.cocode.chess.R

@Composable
fun AboutScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull().orEmpty()
    }
    val open: (String) -> Unit = { url ->
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
    }
    val apk = stringResource(R.string.url_apk)
    val github = stringResource(R.string.url_github)
    val cocode = stringResource(R.string.url_cocode)
    val linkedin = stringResource(R.string.url_linkedin)
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
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.about_version, version), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.about_tagline), textAlign = TextAlign.Center)
            Button(onClick = { open(apk) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_download))
            }
            OutlinedButton(onClick = { open(github) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_source))
            }
            OutlinedButton(onClick = { open(cocode) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.about_website))
            }
            TextButton(onClick = { open(linkedin) }) {
                Text(stringResource(R.string.about_linkedin))
            }
            Text(
                stringResource(R.string.about_credits),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            Text(stringResource(R.string.about_license), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onBack) { Text(stringResource(R.string.about_back)) }
        }
    }
}
