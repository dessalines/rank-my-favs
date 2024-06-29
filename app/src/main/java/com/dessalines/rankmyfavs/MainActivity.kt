package com.dessalines.rankmyfavs

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.asLiveData
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dessalines.rankmyfavs.db.AppDB
import com.dessalines.rankmyfavs.db.AppSettingsRepository
import com.dessalines.rankmyfavs.db.AppSettingsViewModel
import com.dessalines.rankmyfavs.db.AppSettingsViewModelFactory
import com.dessalines.rankmyfavs.db.FavListItemRepository
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.FavListItemViewModelFactory
import com.dessalines.rankmyfavs.db.FavListMatchRepository
import com.dessalines.rankmyfavs.db.FavListMatchViewModel
import com.dessalines.rankmyfavs.db.FavListMatchViewModelFactory
import com.dessalines.rankmyfavs.db.FavListRepository
import com.dessalines.rankmyfavs.db.FavListViewModel
import com.dessalines.rankmyfavs.db.FavListViewModelFactory
import com.dessalines.rankmyfavs.ui.components.about.AboutScreen
import com.dessalines.rankmyfavs.ui.components.common.ShowChangelog
import com.dessalines.rankmyfavs.ui.components.settings.FavListScreen
import com.dessalines.rankmyfavs.ui.components.settings.SettingsScreen
import com.dessalines.rankmyfavs.ui.theme.RankMyFavsTheme

class RankMyFavsApplication : Application() {
    private val database by lazy { AppDB.getDatabase(this) }
    val appSettingsRepository by lazy { AppSettingsRepository(database.appSettingsDao()) }
    val favListRepository by lazy { FavListRepository(database.favListDao()) }
    val favListItemRepository by lazy { FavListItemRepository(database.favListItemDao()) }
    val favListMatchRepository by lazy { FavListMatchRepository(database.favListMatchDao()) }
}

class MainActivity : AppCompatActivity() {
    private val appSettingsViewModel: AppSettingsViewModel by viewModels {
        AppSettingsViewModelFactory((application as RankMyFavsApplication).appSettingsRepository)
    }

    private val favListViewModel: FavListViewModel by viewModels {
        FavListViewModelFactory((application as RankMyFavsApplication).favListRepository)
    }

    private val favListItemViewModel: FavListItemViewModel by viewModels {
        FavListItemViewModelFactory((application as RankMyFavsApplication).favListItemRepository)
    }

    private val favListMatchViewModel: FavListMatchViewModel by viewModels {
        FavListMatchViewModelFactory((application as RankMyFavsApplication).favListMatchRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settings by appSettingsViewModel.appSettings
                .asLiveData()
                .observeAsState()

            val startDestination = "favList"

            RankMyFavsTheme(
                settings = settings,
            ) {
                val navController = rememberNavController()

                ShowChangelog(appSettingsViewModel = appSettingsViewModel)

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                ) {
                    composable(route = "favList") {
                        FavListScreen(
                            navController = navController,
                        )
                    }
                    composable(route = "settings") {
                        SettingsScreen(
                            navController = navController,
                            appSettingsViewModel = appSettingsViewModel,
                        )
                    }
                    composable(
                        route = "about",
                    ) {
                        AboutScreen(
                            navController = navController,
                        )
                    }
                }
            }
        }
    }
}
