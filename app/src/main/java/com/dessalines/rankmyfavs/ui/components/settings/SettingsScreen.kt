package com.dessalines.rankmyfavs.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.DataThresholding
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.AppSettingsViewModel
import com.dessalines.rankmyfavs.db.DEFAULT_MIN_CONFIDENCE
import com.dessalines.rankmyfavs.db.DEFAULT_THEME
import com.dessalines.rankmyfavs.db.DEFAULT_THEME_COLOR
import com.dessalines.rankmyfavs.db.MIN_CONFIDENCE_BOUND
import com.dessalines.rankmyfavs.db.SettingsUpdate
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar
import com.dessalines.rankmyfavs.utils.ThemeColor
import com.dessalines.rankmyfavs.utils.ThemeMode
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ListPreferenceType
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceTheme
import me.zhanghai.compose.preference.SliderPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    appSettingsViewModel: AppSettingsViewModel,
) {
    val settings by appSettingsViewModel.appSettings.asLiveData().observeAsState()

    var minConfidenceState = (settings?.minConfidence ?: DEFAULT_MIN_CONFIDENCE).toFloat()
    var minConfidenceSliderState by remember { mutableFloatStateOf(minConfidenceState) }

    var themeState = ThemeMode.entries[settings?.theme ?: DEFAULT_THEME]
    var themeColorState = ThemeColor.entries[settings?.themeColor ?: DEFAULT_THEME_COLOR]

    fun updateSettings() {
        appSettingsViewModel.updateSettings(
            SettingsUpdate(
                id = 1,
                minConfidence = minConfidenceState.toInt(),
                theme = themeState.ordinal,
                themeColor = themeColorState.ordinal,
            ),
        )
    }

    val ctx = LocalContext.current

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.settings),
                navController = navController,
            )
        },
        content = { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .imePadding(),
            ) {
                ProvidePreferenceTheme {
                    Preference(
                        title = { Text(stringResource(R.string.about)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                            )
                        },
                        onClick = { navController.navigate("about") },
                    )
                    SliderPreference(
                        value = minConfidenceState,
                        sliderValue = minConfidenceSliderState,
                        onValueChange = {
                            minConfidenceState = it
                            updateSettings()
                        },
                        onSliderValueChange = { minConfidenceSliderState = it },
                        valueRange = MIN_CONFIDENCE_BOUND.toFloat()..99f,
                        title = {
                            val confidenceStr = stringResource(R.string.min_confidence, minConfidenceSliderState.toInt().toString())
                            Text(confidenceStr)
                        },
                        summary = {
                            Text(stringResource(R.string.min_confidence_summary))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.DataThresholding,
                                contentDescription = null,
                            )
                        },
                    )
                    ListPreference(
                        type = ListPreferenceType.DROPDOWN_MENU,
                        value = themeState,
                        onValueChange = {
                            themeState = it
                            updateSettings()
                        },
                        values = ThemeMode.entries,
                        valueToText = {
                            AnnotatedString(ctx.getString(it.resId))
                        },
                        title = {
                            Text(stringResource(R.string.theme))
                        },
                        summary = {
                            Text(stringResource(themeState.resId))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                            )
                        },
                    )

                    ListPreference(
                        type = ListPreferenceType.DROPDOWN_MENU,
                        value = themeColorState,
                        onValueChange = {
                            themeColorState = it
                            updateSettings()
                        },
                        values = ThemeColor.entries,
                        valueToText = {
                            AnnotatedString(ctx.getString(it.resId))
                        },
                        title = {
                            Text(stringResource(R.string.theme_color))
                        },
                        summary = {
                            Text(stringResource(themeColorState.resId))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Colorize,
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
        },
    )
}
