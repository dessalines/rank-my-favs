package com.dessalines.rankmyfavs.ui.components.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.dessalines.rankmyfavs.R

enum class AppDestinations(
    @StringRes val label: Int,
    val icon: ImageVector,
    @StringRes val contentDescription: Int,
) {
    LISTS(R.string.lists, Icons.AutoMirrored.Outlined.List, R.string.lists),
    SETTINGS(R.string.settings, Icons.Default.Settings, R.string.settings),
}
