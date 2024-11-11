package com.dessalines.rankmyfavs.ui.components.common

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dessalines.rankmyfavs.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SimpleTopAppBar(
    text: String,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()

    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = text,
            )
        },
        actions = { actions?.let { it() } },
        navigationIcon = {
            if (onBackClick !== null) {
                BasicTooltipBox(
                    positionProvider = tooltipPosition,
                    state = rememberBasicTooltipState(isPersistent = false),
                    tooltip = {
                        ToolTip(stringResource(R.string.go_back))
                    },
                ) {
                    IconButton(
                        onClick = onBackClick,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.go_back),
                        )
                    }
                }
            }
        },
    )
}
