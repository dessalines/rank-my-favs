package com.dessalines.rankmyfavs.ui.components.favlist

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Reviews
import androidx.compose.material.icons.outlined.SaveAs
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
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
import com.dessalines.rankmyfavs.db.MIN_CONFIDENCE_BOUND
import com.dessalines.rankmyfavs.db.sampleFavListItem
import com.dessalines.rankmyfavs.ui.components.common.AreYouSureDialog
import com.dessalines.rankmyfavs.ui.components.common.LARGE_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar
import com.dessalines.rankmyfavs.ui.components.common.ToolTip
import com.dessalines.rankmyfavs.ui.components.favlistitem.calculateConfidence
import com.dessalines.rankmyfavs.utils.writeData
import com.floern.castingcsv.castingCSV
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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val favList by favListViewModel.getById(id).asLiveData().observeAsState()
    val favListItems by favListItemViewModel.getFromList(id).asLiveData().observeAsState()

    val showClearStatsDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    var showMoreDropdown by remember { mutableStateOf(false) }

    // For exporting the csv
    val contentResolver = ctx.contentResolver
    val exportCsvLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/csv"),
        ) {
            it?.also {
                val csv = castingCSV().toCSV(favListItems.orEmpty())
                writeData(contentResolver, it, csv)
            }
        }

    val clearStatsMessage = stringResource(R.string.clear_stats)
    val deletedMessage = stringResource(R.string.list_deleted)

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = favList?.name.orEmpty(),
                navController = navController,
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = { padding ->

            if (favListItems.isNullOrEmpty()) {
                Text(
                    text = stringResource(R.string.no_items),
                    modifier = Modifier.padding(horizontal = LARGE_PADDING, vertical = padding.calculateTopPadding()),
                )
            }

            AreYouSureDialog(
                show = showClearStatsDialog,
                title = clearStatsMessage,
                onYes = {
                    favListItemViewModel.clearStatsForList(favListId = id)
                    favListMatchViewModel.deleteMatchesForList(favListId = id)
                    Toast.makeText(ctx, clearStatsMessage, Toast.LENGTH_SHORT).show()
                },
            )

            AreYouSureDialog(
                show = showDeleteDialog,
                title = stringResource(R.string.delete),
                onYes = {
                    favList?.let {
                        favListViewModel.delete(it)
                        navController.navigateUp()
                        Toast.makeText(ctx, deletedMessage, Toast.LENGTH_SHORT).show()
                    }
                },
            )

            Box(
                modifier =
                    Modifier
                        .padding(padding)
                        .imePadding(),
            ) {
                LazyColumn(
                    state = listState,
                ) {
                    // Unfortunately showing these details messes up the scroll remember position.
                    // Comment it for now
//                item {
//                    FavListDetails(favList)
//                }

                    itemsIndexed(
                        key = { _, item -> item.id },
                        items = favListItems.orEmpty(),
                    ) { index, favListItem ->
                        FavListItemRow(
                            favListItem = favListItem,
                            index = index + 1,
                            onClick = {
                                navController.navigate("itemDetails/${favListItem.id}")
                            },
                        )
                    }
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
                            ToolTip(stringResource(R.string.more_actions))
                        },
                    ) {
                        IconButton(
                            onClick = {
                                showMoreDropdown = true
                            },
                        ) {
                            Icon(
                                Icons.Outlined.MoreVert,
                                contentDescription = stringResource(R.string.more_actions),
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showMoreDropdown,
                        onDismissRequest = { showMoreDropdown = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.import_list)) },
                            onClick = {
                                showMoreDropdown = false
                                navController.navigate("importList/${favList?.id}")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.FileOpen,
                                    contentDescription = stringResource(R.string.import_list),
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.export_list_as_csv)) },
                            onClick = {
                                showMoreDropdown = false
                                favList?.let { exportCsvLauncher.launch(it.name) }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.SaveAs,
                                    contentDescription = stringResource(R.string.export_list_as_csv),
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.clear_stats)) },
                            onClick = {
                                showMoreDropdown = false
                                showClearStatsDialog.value = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.ClearAll,
                                    contentDescription = stringResource(R.string.clear_stats),
                                )
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            onClick = {
                                showMoreDropdown = false
                                showDeleteDialog.value = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                )
                            },
                        )
                    }
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(id = R.string.create_item))
                        },
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate("createItem/${favList?.id}")
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
    index: Int,
    onClick: () -> Unit,
) {
    // Only show the rank if its above the min confidence bound
    val rank = if (calculateConfidence(favListItem.glickoDeviation) >= MIN_CONFIDENCE_BOUND) index.toString() else "?"
    ListItem(
        headlineContent = {
            Text(favListItem.name)
        },
        trailingContent = {
            Text(
                text = rank,
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
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
        index = 23,
        onClick = {},
    )
}
