package com.dessalines.rankmyfavs.ui.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.asLiveData
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.AppSettingsViewModel
import com.dessalines.rankmyfavs.utils.getVersionCode
import dev.jeziellago.compose.markdowntext.MarkdownText

val DONATION_MARKDOWN =
    """
    ### Support Rank-My-Favs
    [Rank-My-Favs](https://github.com/dessalines/rank-my-favs) is free, open-source software, meaning no spying, keylogging, or advertising, ever.

    No one likes recurring donations, but they've proven to be the only way open-source software like Rank-My-Favs can stay alive. If you find yourself using Rank-My-Favs every day, please consider donating:
    - [Support on Liberapay](https://liberapay.com/dessalines).
    - [Support on Patreon](https://www.patreon.com/dessalines).
    ---
    
    """.trimIndent()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShowChangelog(appSettingsViewModel: AppSettingsViewModel) {
    val ctx = LocalContext.current
    val lastVersionCodeViewed =
        appSettingsViewModel.appSettings
            .asLiveData()
            .observeAsState()
            .value
            ?.lastVersionCodeViewed

    // Make sure its initialized
    lastVersionCodeViewed?.also { lastViewed ->
        val currentVersionCode = ctx.getVersionCode()
        val viewed = lastViewed == currentVersionCode

        var whatsChangedDialogOpen by remember { mutableStateOf(!viewed) }

        if (whatsChangedDialogOpen) {
            val scrollState = rememberScrollState()
            val markdown by appSettingsViewModel.changelog.collectAsState()
            LaunchedEffect(appSettingsViewModel) {
                appSettingsViewModel.updateChangelog(ctx)
            }

            AlertDialog(
                text = {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                    ) {
                        val markdownText = DONATION_MARKDOWN + markdown
                        MarkdownText(
                            markdown = markdownText,
                            linkColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            whatsChangedDialogOpen = false
                            appSettingsViewModel.updateLastVersionCodeViewed(currentVersionCode)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.done))
                    }
                },
                onDismissRequest = {
                    whatsChangedDialogOpen = false
                    appSettingsViewModel.updateLastVersionCodeViewed(currentVersionCode)
                },
                modifier = Modifier.semantics { testTagsAsResourceId = true },
            )
        }
    }
}
