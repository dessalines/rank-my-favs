package com.dessalines.rankmyfavs.ui.components.favlist

import android.widget.Toast
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.Reviews
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavList
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.FavListMatchViewModel
import com.dessalines.rankmyfavs.db.FavListViewModel
import com.dessalines.rankmyfavs.db.sampleFavListItem
import com.dessalines.rankmyfavs.ui.components.common.LARGE_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar
import com.dessalines.rankmyfavs.ui.components.common.ToolTip
import com.dessalines.rankmyfavs.utils.numToString
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavListDetailScreen(
    navController: NavController,
    favListViewModel: FavListViewModel,
    favListItemViewModel: FavListItemViewModel,
    favListMatchViewModel: FavListMatchViewModel,
    id: Int,
) {
    val ctx = LocalContext.current
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val listState = rememberLazyListState()

    val favList by remember(id) { mutableStateOf(favListViewModel.getById(id)) }
    val favListItems by favListItemViewModel.getFromList(id).asLiveData().observeAsState()

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = favList.name,
                navController = navController,
            )
        },
        content = { padding ->

            if (favListItems.isNullOrEmpty()) {
                Text(
                    text = stringResource(R.string.no_items),
                    modifier = Modifier.padding(horizontal = LARGE_PADDING, vertical = padding.calculateTopPadding()),
                )
            }

            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .padding(padding)
                        .imePadding(),
            ) {
                // Unfortunately showing these details messes up the scroll remember position.
                // Comment it for now
//                item {
//                    FavListDetails(favList)
//                }

                items(
                    key = { it.id },
                    items = favListItems.orEmpty(),
                ) { favListItem ->
                    FavListItemRow(
                        favListItem = favListItem,
                        onClick = {
                            navController.navigate("itemDetails/${favListItem.id}")
                        },
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(id = R.string.create_item))
                        },
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate("createItem/${favList.id}")
                            },
                        ) {
                            Icon(
                                Icons.Outlined.Add,
                                contentDescription = stringResource(R.string.create_item),
                            )
                        }
                    }
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.edit_list))
                        },
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate("editFavList/$id")
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit_list),
                            )
                        }
                    }
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.import_list))
                        },
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate("importList/${favList.id}")
                            },
                        ) {
                            Icon(
                                Icons.Outlined.ImportExport,
                                contentDescription = stringResource(R.string.import_list),
                            )
                        }
                    }
                    val clearStatsMessage = stringResource(R.string.clear_stats)
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(clearStatsMessage)
                        },
                    ) {
                        IconButton(
                            onClick = {
                                favListItemViewModel.clearStatsForList(favListId = id)
                                favListMatchViewModel.deleteMatchesForList(favListId = id)
                                Toast.makeText(ctx, clearStatsMessage, Toast.LENGTH_SHORT).show()
                            },
                        ) {
                            Icon(
                                Icons.Outlined.ClearAll,
                                contentDescription = stringResource(R.string.clear_stats),
                            )
                        }
                    }
                    val deletedMessage = stringResource(R.string.list_deleted)
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.delete))
                        },
                    ) {
                        IconButton(
                            onClick = {
                                favListViewModel.delete(favList)
                                navController.navigateUp()
                                Toast.makeText(ctx, deletedMessage, Toast.LENGTH_SHORT).show()
                            },
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete),
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if ((favListItems?.count() ?: 0) >= 2) {
                        BasicTooltipBox(
                            positionProvider = tooltipPosition,
                            state = rememberBasicTooltipState(isPersistent = false),
                            tooltip = {
                                ToolTip(stringResource(R.string.rate))
                            },
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate("match/$id")
                                },
                                shape = CircleShape,
                            ) {
                                Icon(
                                    Icons.Outlined.Reviews,
                                    contentDescription = stringResource(R.string.rate),
                                )
                            }
                        }
                    }
                },
            )
        },
    )
}

@Composable
fun FavListDetails(favList: FavList) {
    if (!favList.description.isNullOrBlank()) {
        HorizontalDivider()
        MarkdownText(
            markdown = favList.description,
            linkColor = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier.padding(
                    top = 0.dp,
                    bottom = SMALL_PADDING,
                    start = LARGE_PADDING,
                    end = LARGE_PADDING,
                ),
        )
    }
}

@Composable
@Preview
fun FavListDetailsPreview() {
    FavListDetails(FavList(id = 1, name = "Fav List 1", description = "ok"))
}

@Composable
fun FavListItemRow(
    favListItem: FavListItem,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(favListItem.name)
        },
        trailingContent = {
            Text(
                text = numToString(favListItem.glickoRating, 0),
                style = MaterialTheme.typography.labelMedium,
            )
        },
        modifier =
            Modifier.clickable {
                onClick()
            },
    )
}

@Composable
@Preview
fun FavListItemRowPreview() {
    FavListItemRow(
        favListItem = sampleFavListItem,
        onClick = {},
    )
}
