package com.dessalines.rankmyfavs

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.asLiveData
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.dessalines.rankmyfavs.db.TierListRepository
import com.dessalines.rankmyfavs.db.TierListViewModel
import com.dessalines.rankmyfavs.db.TierListViewModelFactory
import com.dessalines.rankmyfavs.ui.components.about.AboutScreen
import com.dessalines.rankmyfavs.ui.components.common.ShowChangelog
import com.dessalines.rankmyfavs.ui.components.favlist.CreateFavListScreen
import com.dessalines.rankmyfavs.ui.components.favlist.EditFavListScreen
import com.dessalines.rankmyfavs.ui.components.favlist.ImportListScreen
import com.dessalines.rankmyfavs.ui.components.favlist.TierListScreen
import com.dessalines.rankmyfavs.ui.components.favlist.favlistanddetails.FavListsAndDetailScreen
import com.dessalines.rankmyfavs.ui.components.favlistitem.CreateFavListItemScreen
import com.dessalines.rankmyfavs.ui.components.favlistitem.EditFavListItemScreen
import com.dessalines.rankmyfavs.ui.components.favlistitem.FavListItemDetailScreen
import com.dessalines.rankmyfavs.ui.components.match.MatchScreen
import com.dessalines.rankmyfavs.ui.components.settings.SettingsScreen
import com.dessalines.rankmyfavs.ui.theme.RankMyFavsTheme

class RankMyFavsApplication : Application() {
    private val database by lazy { AppDB.getDatabase(this) }
    val appSettingsRepository by lazy { AppSettingsRepository(database.appSettingsDao()) }
    val favListRepository by lazy { FavListRepository(database.favListDao()) }
    val favListItemRepository by lazy { FavListItemRepository(database.favListItemDao()) }
    val tierListRepository by lazy { TierListRepository(database.tierListDao()) }

    val favListMatchRepository by lazy { FavListMatchRepository(database.favListMatchDao()) }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
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

    private val tierListViewModel: TierListViewModel by viewModels {
        TierListViewModelFactory((application as RankMyFavsApplication).tierListRepository)
    }

    private val favListMatchViewModel: FavListMatchViewModel by viewModels {
        FavListMatchViewModelFactory((application as RankMyFavsApplication).favListMatchRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settings by appSettingsViewModel.appSettings
                .asLiveData()
                .observeAsState()

            val startDestination = "favLists"

            RankMyFavsTheme(
                settings = settings,
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    ShowChangelog(appSettingsViewModel = appSettingsViewModel)

                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                    ) {
                        composable(
                            route = "favLists?favListId={favListId}",
                            arguments =
                                listOf(
                                    navArgument("favListId") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    },
                                ),
                        ) {
                            val favListId = it.arguments?.getString("favListId")?.toInt()

                            FavListsAndDetailScreen(
                                navController = navController,
                                favListViewModel = favListViewModel,
                                favListItemViewModel = favListItemViewModel,
                                favListMatchViewModel = favListMatchViewModel,
                                favListId = favListId,
                            )
                        }
                        composable(
                            route = "createFavList",
                        ) {
                            CreateFavListScreen(
                                navController = navController,
                                favListViewModel = favListViewModel,
                            )
                        }
                        composable(
                            route = "editFavList/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.IntType }),
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            val id = it.arguments?.getInt("id") ?: 0
                            EditFavListScreen(
                                navController = navController,
                                favListViewModel = favListViewModel,
                                id = id,
                            )
                        }

                        composable(
                            route = "createItem/{favListId}",
                            arguments =
                                listOf(
                                    navArgument("favListId") {
                                        type = NavType.IntType
                                    },
                                ),
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            val favListId = it.arguments?.getInt("favListId") ?: 0
                            CreateFavListItemScreen(
                                navController = navController,
                                favListItemViewModel = favListItemViewModel,
                                favListId = favListId,
                            )
                        }

                        composable(
                            route = "editItem/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.IntType }),
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            val id = it.arguments?.getInt("id") ?: 0
                            EditFavListItemScreen(
                                navController = navController,
                                favListItemViewModel = favListItemViewModel,
                                id = id,
                            )
                        }

                        composable(
                            route = "itemDetails/{id}",
                            arguments = listOf(navArgument("id") { type = NavType.IntType }),
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            val id = it.arguments?.getInt("id") ?: 0

                            FavListItemDetailScreen(
                                navController = navController,
                                favListItemViewModel = favListItemViewModel,
                                favListMatchViewModel = favListMatchViewModel,
                                id = id,
                            )
                        }

                        composable(
                            route = "importList/{favListId}",
                            arguments =
                                listOf(
                                    navArgument("favListId") {
                                        type = NavType.IntType
                                    },
                                ),
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            val favListId = it.arguments?.getInt("favListId") ?: 0
                            ImportListScreen(
                                navController = navController,
                                favListItemViewModel = favListItemViewModel,
                                favListId = favListId,
                            )
                        }

                        composable(
                            route = "tierList/{favListId}",
                            arguments =
                                listOf(
                                    navArgument("favListId") {
                                        type = NavType.IntType
                                    },
                                ),
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            val favListId = it.arguments?.getInt("favListId") ?: 0
                            TierListScreen(
                                navController = navController,
                                favListItemViewModel = favListItemViewModel,
                                favListViewModel = favListViewModel,
                                favListId = favListId,
                                tierListViewModel = tierListViewModel,
                            )
                        }

                        composable(
                            route = "match?favListId={favListId}&favListItemId={favListItemId}",
                            arguments =
                                listOf(
                                    navArgument("favListId") {
                                        type = NavType.StringType
                                        nullable = true
                                    },
                                    navArgument("favListItemId") {
                                        type = NavType.StringType
                                        nullable = true
                                    },
                                ),
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
                        ) {
                            val favListId = it.arguments?.getString("favListId")?.toInt()
                            val favListItemId =
                                it.arguments?.getString("favListItemId")?.toInt()

                            MatchScreen(
                                navController = navController,
                                favListItemViewModel = favListItemViewModel,
                                favListMatchViewModel = favListMatchViewModel,
                                favListId = favListId,
                                favListItemId = favListItemId,
                            )
                        }

                        composable(
                            route = "settings",
                        ) {
                            SettingsScreen(
                                navController = navController,
                                appSettingsViewModel = appSettingsViewModel,
                            )
                        }
                        composable(
                            route = "about",
                            enterTransition = enterAnimation(),
                            exitTransition = exitAnimation(),
                            popEnterTransition = enterAnimation(),
                            popExitTransition = exitAnimation(),
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
}

private fun enterAnimation(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
    {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
        )
    }

private fun exitAnimation(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
    {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
        )
    }
