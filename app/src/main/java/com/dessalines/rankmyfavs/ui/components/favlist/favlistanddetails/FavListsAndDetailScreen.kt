package com.dessalines.rankmyfavs.ui.components.favlist.favlistanddetails

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.FavListMatchViewModel
import com.dessalines.rankmyfavs.db.FavListViewModel
import com.dessalines.rankmyfavs.utils.SelectionVisibilityState
import kotlinx.coroutines.launch

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalSharedTransitionApi::class,
)
@Composable
fun FavListsAndDetailScreen(
    navController: NavController,
    favListViewModel: FavListViewModel,
    favListItemViewModel: FavListItemViewModel,
    favListMatchViewModel: FavListMatchViewModel,
    favListId: Int?,
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    var selectedFavListId: Int? by rememberSaveable { mutableStateOf(favListId) }
    val favLists by favListViewModel.getAll.asLiveData().observeAsState()

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Companion.Expanded &&
            navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Companion.Expanded
    val isDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Companion.Expanded

    BackHandler(enabled = navigator.canNavigateBack()) {
        scope.launch {
            navigator.navigateBack()
        }
    }

    SharedTransitionLayout {
        AnimatedContent(targetState = isListAndDetailVisible, label = "simple sample") {
            ListDetailPaneScaffold(
                directive = navigator.scaffoldDirective,
                value = navigator.scaffoldValue,
                listPane = {
                    val currentSelectedFavlistId = selectedFavListId
                    val selectionState =
                        if (isDetailVisible && currentSelectedFavlistId != null) {
                            SelectionVisibilityState.ShowSelection(currentSelectedFavlistId)
                        } else {
                            SelectionVisibilityState.NoSelection
                        }

                    AnimatedPane {
                        FavListsPane(
                            navController = navController,
                            favLists = favLists,
                            onFavListClick = { favListId ->
                                selectedFavListId = favListId
                                scope.launch {
                                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                                }
                            },
                            selectionState = selectionState,
                            isListAndDetailVisible = isListAndDetailVisible,
                        )
                    }
                },
                detailPane = {
                    AnimatedPane {
                        selectedFavListId?.let { favListId ->

                            val favList by favListViewModel.getById(favListId).asLiveData().observeAsState()
                            val favListItems by favListItemViewModel.getFromList(favListId).asLiveData().observeAsState()
                            val clearStatsMessage = stringResource(R.string.clear_stats)
                            val deletedMessage = stringResource(R.string.list_deleted)

                            FavListDetailPane(
                                navController = navController,
                                favListId = favListId,
                                favList = favList,
                                favListItems = favListItems,
                                isListAndDetailVisible = isListAndDetailVisible,
                                onBackClick = {
                                    scope.launch {
                                        navigator.navigateBack()
                                    }
                                },
                                onClearStats = {
                                    favListItemViewModel.clearStatsForList(favListId = favListId)
                                    favListMatchViewModel.deleteMatchesForList(favListId = favListId)
                                    Toast.makeText(ctx, clearStatsMessage, Toast.LENGTH_SHORT).show()
                                },
                                onDelete = {
                                    favList?.let {
                                        favListViewModel.delete(it)
                                        navController.navigateUp()
                                        Toast.makeText(ctx, deletedMessage, Toast.LENGTH_SHORT).show()
                                    }
                                },
                            )
                        }
                    }
                },
            )
        }
    }
}
