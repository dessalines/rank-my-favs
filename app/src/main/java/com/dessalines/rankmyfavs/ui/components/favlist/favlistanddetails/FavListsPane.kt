package com.dessalines.rankmyfavs.ui.components.favlist.favlistanddetails

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavList
import com.dessalines.rankmyfavs.ui.components.common.LARGE_PADDING
import com.dessalines.rankmyfavs.ui.components.common.ToolTip
import com.dessalines.rankmyfavs.utils.SelectionVisibilityState
import kotlin.collections.orEmpty

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavListsPane(
    favLists: List<FavList>?,
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
    onFavListClick: (favListId: Int) -> Unit,
    selectionState: SelectionVisibilityState<Int>,
    isListAndDetailVisible: Boolean,
    onCreateFavlistClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val tooltipPosition = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above)
    val title =
        if (!isListAndDetailVisible) stringResource(R.string.app_name) else stringResource(R.string.lists)

    // Sort the lists alphabetically by name
    val favListsSorted = favLists?.sortedBy { it.name }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.settings))
                        },
                    ) {
                        IconButton(
                            onClick = onSettingsClick,
                        ) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    }
                },
            )
        },
        modifier = Modifier.Companion.nestedScroll(scrollBehavior.nestedScrollConnection),
        content = { padding ->
            Box(
                modifier =
                    Modifier.Companion
                        .padding(padding)
                        .imePadding(),
            ) {
                LazyColumn(
                    state = listState,
                ) {
                    items(favListsSorted.orEmpty()) { favList ->
                        val selected =
                            when (selectionState) {
                                is SelectionVisibilityState.ShowSelection -> selectionState.selectedItem == favList.id
                                else -> false
                            }

                        FavListRow(
                            favList = favList,
                            onClick = { onFavListClick(favList.id) },
                            selected = selected,
                        )
                    }
                    item {
                        if (favListsSorted.isNullOrEmpty()) {
                            Text(
                                text = stringResource(R.string.no_lists),
                                modifier = Modifier.Companion.padding(horizontal = LARGE_PADDING),
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            BasicTooltipBox(
                positionProvider = tooltipPosition,
                state = rememberBasicTooltipState(isPersistent = false),
                tooltip = {
                    ToolTip(stringResource(R.string.create_list))
                },
            ) {
                FloatingActionButton(
                    modifier = Modifier.Companion.imePadding(),
                    onClick = onCreateFavlistClick,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.create_list),
                    )
                }
            }
        },
    )
}

@Composable
fun FavListRow(
    favList: FavList,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val containerColor =
        if (!selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant

    ListItem(
        headlineContent = {
            Text(favList.name)
        },
        colors = ListItemDefaults.colors(containerColor = containerColor),
        modifier =
            Modifier.Companion.clickable {
                onClick()
            },
    )
}

@Composable
@Preview
fun FavListRowPreview() {
    FavListRow(
        favList = FavList(id = 1, name = "Fav List 1"),
        onClick = {},
    )
}
