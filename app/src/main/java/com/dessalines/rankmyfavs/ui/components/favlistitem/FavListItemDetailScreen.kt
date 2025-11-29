package com.dessalines.rankmyfavs.ui.components.favlistitem

import android.widget.Toast
import androidx.annotation.Keep
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Reviews
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.breens.beetablescompose.BeeTablesCompose
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.DEFAULT_GLICKO_DEVIATION
import com.dessalines.rankmyfavs.db.DEFAULT_GLICKO_RATING
import com.dessalines.rankmyfavs.db.DEFAULT_GLICKO_VOLATILITY
import com.dessalines.rankmyfavs.db.DEFAULT_MATCH_COUNT
import com.dessalines.rankmyfavs.db.DEFAULT_WIN_RATE
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemUpdateStats
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.FavListMatchViewModel
import com.dessalines.rankmyfavs.db.sampleFavListItem
import com.dessalines.rankmyfavs.ui.components.common.AreYouSureDialog
import com.dessalines.rankmyfavs.ui.components.common.BackButton
import com.dessalines.rankmyfavs.ui.components.common.LARGE_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.ToolTip
import com.dessalines.rankmyfavs.utils.GLICKO_WIKI_URL
import com.dessalines.rankmyfavs.utils.numToString
import com.dessalines.rankmyfavs.utils.openLink
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavListItemDetailScreen(
    navController: NavController,
    favListItemViewModel: FavListItemViewModel,
    favListMatchViewModel: FavListMatchViewModel,
    id: Int,
) {
    val ctx = LocalContext.current
    val tooltipPosition = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()

    val favListItem by favListItemViewModel.getById(id).asLiveData().observeAsState()

    val showDeleteDialog = remember { mutableStateOf(false) }
    val showClearStatsDialog = remember { mutableStateOf(false) }

    val deletedMessage = stringResource(R.string.item_deleted)
    val clearStatsMessage = stringResource(R.string.clear_stats)

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text(favListItem?.name.orEmpty()) },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    BackButton(
                        onBackClick = { navController.navigateUp() },
                    )
                },
                actions = {
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.what_do_these_numbers_mean))
                        },
                    ) {
                        IconButton(onClick = { openLink(GLICKO_WIKI_URL, ctx) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Help,
                                contentDescription = stringResource(R.string.what_do_these_numbers_mean),
                            )
                        }
                    }
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.edit_item))
                        },
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate("editItem/$id")
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit_item),
                            )
                        }
                    }
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(clearStatsMessage)
                        },
                    ) {
                        IconButton(
                            onClick = {
                                showClearStatsDialog.value = true
                            },
                        ) {
                            Icon(
                                Icons.Outlined.ClearAll,
                                contentDescription = stringResource(R.string.clear_stats),
                            )
                        }
                    }
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.delete))
                        },
                    ) {
                        IconButton(
                            onClick = {
                                showDeleteDialog.value = true
                            },
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete),
                            )
                        }
                    }
                },
            )
        },
        content = { padding ->
            AreYouSureDialog(
                show = showDeleteDialog,
                title = stringResource(R.string.delete),
                onYes = {
                    favListItem?.let {
                        favListItemViewModel.delete(it)
                        navController.navigateUp()
                        Toast.makeText(ctx, deletedMessage, Toast.LENGTH_SHORT).show()
                    }
                },
            )

            AreYouSureDialog(
                show = showClearStatsDialog,
                title = clearStatsMessage,
                onYes = {
                    // Update the stats for that row
                    favListItemViewModel.updateStats(
                        FavListItemUpdateStats(
                            id = id,
                            winRate = DEFAULT_WIN_RATE,
                            glickoRating = DEFAULT_GLICKO_RATING,
                            glickoDeviation = DEFAULT_GLICKO_DEVIATION,
                            glickoVolatility = DEFAULT_GLICKO_VOLATILITY,
                            matchCount = DEFAULT_MATCH_COUNT,
                        ),
                    )
                    favListMatchViewModel.deleteMatchesForItem(id)
                    Toast.makeText(ctx, clearStatsMessage, Toast.LENGTH_SHORT).show()
                },
            )

            Box(
                modifier = Modifier.padding(padding).imePadding().nestedScroll(scrollBehavior.nestedScrollConnection),
            ) {
                LazyColumn(
                    state = listState,
                ) {
                    favListItem?.let {
                        item {
                            FavListItemDetails(it)
                        }

                        item {
                            Stats(it)
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
                    ToolTip(stringResource(R.string.rate))
                },
            ) {
                FloatingActionButton(
                    modifier = Modifier.imePadding(),
                    onClick = {
                        navController.navigate("match?favListItemId=$id")
                    },
                    shape = CircleShape,
                ) {
                    Icon(
                        Icons.Outlined.Reviews,
                        contentDescription = stringResource(R.string.rate),
                    )
                }
            }
        },
    )
}

@Keep
data class StatItem(
    val key: String,
    val v: String,
)

@Composable
fun Stats(favListItem: FavListItem) {
    val titles = listOf("Key", "V")
    val data =
        listOf(
            StatItem(
                key = stringResource(R.string.rating),
                v = numToString(favListItem.glickoRating, 0),
            ),
            StatItem(
                key = stringResource(R.string.confidence),
                v = calculateConfidenceStr(favListItem.glickoDeviation),
            ),
            StatItem(
                key = stringResource(R.string.deviation),
                v = numToString(favListItem.glickoDeviation, 0),
            ),
//            StatItem(
//                key = stringResource(R.string.Volatility),
//                v = numToString(favListItem.glickoVolatility, 2),
//            ),
            StatItem(
                key = stringResource(R.string.win_rate),
                v = numToString(favListItem.winRate, 0) + "%",
            ),
            StatItem(
                key = stringResource(R.string.match_count),
                v = favListItem.matchCount.toString(),
            ),
        )
    Row(
        modifier = Modifier.padding(horizontal = LARGE_PADDING),
    ) {
        BeeTablesCompose(data = data, headerTableTitles = titles, enableTableHeaderTitles = false)
    }
}

fun calculateConfidence(deviation: Float) = ((1500F - deviation) / 1500F * 100F)

fun calculateConfidenceStr(deviation: Float): String = numToString(calculateConfidence(deviation), 1) + "%"

@Composable
@Preview
fun StatsPreview() {
    Stats(sampleFavListItem)
}

@Composable
fun FavListItemDetails(favListItem: FavListItem) {
    if (!favListItem.description.isNullOrBlank()) {
        MarkdownText(
            markdown = favListItem.description,
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
fun FavListItemDetailsPreview() {
    FavListItemDetails(sampleFavListItem)
}
