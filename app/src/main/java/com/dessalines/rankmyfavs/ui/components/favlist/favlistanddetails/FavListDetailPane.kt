package com.dessalines.rankmyfavs.ui.components.favlist.favlistanddetails

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Reviews
import androidx.compose.material.icons.outlined.SaveAs
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavList
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.MIN_CONFIDENCE_BOUND
import com.dessalines.rankmyfavs.db.sampleFavListItem
import com.dessalines.rankmyfavs.ui.components.common.AreYouSureDialog
import com.dessalines.rankmyfavs.ui.components.common.LARGE_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar
import com.dessalines.rankmyfavs.ui.components.common.ToolTip
import com.dessalines.rankmyfavs.ui.components.favlistitem.calculateConfidence
import com.dessalines.rankmyfavs.utils.convertFavlistToMarkdown
import com.dessalines.rankmyfavs.utils.writeData
import com.floern.castingcsv.castingCSV
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavListDetailPane(
    favList: FavList?,
    favListItems: List<FavListItem>?,
    listState: LazyListState,
    isListAndDetailVisible: Boolean,
    onBackClick: () -> Unit,
    onClearStats: () -> Unit,
    onDelete: () -> Unit,
    onCreateItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onImportListClick: () -> Unit,
    onTierListClick: () -> Unit,
    onItemDetailsClick: (itemId: Int) -> Unit,
    onMatchClick: () -> Unit,
) {
    val ctx = LocalContext.current
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    val showClearStatsDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    var showMoreDropdown by remember { mutableStateOf(false) }

    var showSearchBar by remember { mutableStateOf(false) }
    var searchFilter by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // For exporting the csv
    val exportCsvLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/csv"),
        ) {
            it?.also {
                val csv = castingCSV().toCSV(favListItems.orEmpty())
                writeData(ctx, it, csv)
            }
        }

    // For exporting the markdown list
    val exportMarkdownLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/markdown"),
        ) {
            it?.also {
                val markdown =
                    convertFavlistToMarkdown(favList?.name.orEmpty(), favListItems.orEmpty())
                writeData(ctx, it, markdown)
            }
        }

    val clearStatsMessage = stringResource(R.string.clear_stats)

    val (titleText, onBackClick) =
        if (isListAndDetailVisible) {
            Pair(stringResource(R.string.items), null)
        } else {
            Pair(favList?.name.orEmpty(), onBackClick)
        }

    Scaffold(
        topBar = {
            if (!showSearchBar) {
                SimpleTopAppBar(
                    text = titleText,
                    onBackClick = onBackClick,
                    scrollBehavior = scrollBehavior,
                    actions = {
                        BasicTooltipBox(
                            positionProvider = tooltipPosition,
                            state = rememberBasicTooltipState(isPersistent = false),
                            tooltip = {
                                ToolTip(stringResource(R.string.search))
                            },
                        ) {
                            IconButton(
                                onClick = {
                                    showSearchBar = true
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = stringResource(R.string.search),
                                )
                            }
                        }
                        BasicTooltipBox(
                            positionProvider = tooltipPosition,
                            state = rememberBasicTooltipState(isPersistent = false),
                            tooltip = {
                                ToolTip(stringResource(id = R.string.create_item))
                            },
                        ) {
                            IconButton(
                                onClick = onCreateItemClick,
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
                                text = { Text(stringResource(R.string.edit_list)) },
                                onClick = {
                                    showMoreDropdown = false
                                    onEditClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = stringResource(R.string.edit_list),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.import_list)) },
                                onClick = {
                                    showMoreDropdown = false
                                    onImportListClick()
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
                                text = { Text(stringResource(R.string.export_list_as_markdown)) },
                                onClick = {
                                    showMoreDropdown = false
                                    favList?.let { exportMarkdownLauncher.launch(it.name) }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.SaveAs,
                                        contentDescription = stringResource(R.string.export_list_as_markdown),
                                    )
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.tier_list)) },
                                onClick = {
                                    showMoreDropdown = false
                                    onTierListClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.List,
                                        contentDescription = stringResource(R.string.tier_list),
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
                    },
                )
            } else {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        TextField(
                            value = searchFilter,
                            onValueChange = { searchFilter = it },
                            singleLine = true,
                            modifier = Modifier.focusRequester(focusRequester),
                            colors =
                                TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                ),
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { showSearchBar = false },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.hide_search_bar),
                            )
                        }
                    },
                )
                // Focus when the searchbar is expanded
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = { padding ->

            if (favListItems.isNullOrEmpty()) {
                Text(
                    text = stringResource(R.string.no_items),
                    modifier =
                        Modifier.padding(
                            horizontal = LARGE_PADDING,
                            vertical = padding.calculateTopPadding(),
                        ),
                )
            }

            AreYouSureDialog(
                show = showClearStatsDialog,
                title = clearStatsMessage,
                onYes = onClearStats,
            )

            AreYouSureDialog(
                show = showDeleteDialog,
                title = stringResource(R.string.delete),
                onYes = onDelete,
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
                    item {
                        FavListDetails(favList)
                    }

                    itemsIndexed(
                        key = { _, item -> item.id },
                        items =
                            favListItems.orEmpty().filter {
                                it.name.contains(
                                    searchFilter,
                                    ignoreCase = true,
                                )
                            },
                    ) { index, favListItem ->
                        FavListItemRow(
                            favListItem = favListItem,
                            index = index + 1,
                            onClick = {
                                onItemDetailsClick(favListItem.id)
                            },
                        )
                    }
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
                        modifier = Modifier.imePadding(),
                        onClick = onMatchClick,
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
}

@Composable
fun FavListDetails(favList: FavList?) {
    if (!favList?.description.isNullOrBlank()) {
        HorizontalDivider()
        MarkdownText(
            markdown = favList.description,
            linkColor = MaterialTheme.colorScheme.primary,
            modifier =
                Modifier
                    .padding(
                        top = 0.dp,
                        bottom = SMALL_PADDING,
                        start = LARGE_PADDING,
                        end = LARGE_PADDING,
                    ).fillMaxWidth(),
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
    val rank =
        if (calculateConfidence(favListItem.glickoDeviation) >= MIN_CONFIDENCE_BOUND) index.toString() else "?"
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
