package com.dessalines.rankmyfavs.ui.components.favlistitem

import android.widget.Toast
import androidx.annotation.Keep
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.breens.beetablescompose.BeeTablesCompose
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.sampleFavListItem
import com.dessalines.rankmyfavs.ui.components.common.LARGE_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar
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
    id: Int,
) {
    val ctx = LocalContext.current
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val listState = rememberLazyListState()

    val favListItem = favListItemViewModel.getById(id)

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = favListItem.name,
                navController = navController,
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
                },
            )
        },
        content = { padding ->
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .padding(padding)
                        .imePadding(),
            ) {
                item {
                    FavListItemDetails(favListItem)
                }

                item {
                    Stats(favListItem)
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    val deletedMessage = stringResource(R.string.item_deleted)
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.delete))
                        },
                    ) {
                        IconButton(
                            onClick = {
                                favListItemViewModel.delete(favListItem)
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
                    BasicTooltipBox(
                        positionProvider = tooltipPosition,
                        state = rememberBasicTooltipState(isPersistent = false),
                        tooltip = {
                            ToolTip(stringResource(R.string.edit_list))
                        },
                    ) {
                        FloatingActionButton(
                            onClick = {
                                navController.navigate("editItem/$id")
                            },
                            shape = CircleShape,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit_list),
                            )
                        }
                    }
                },
            )
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
                v = calculateConfidence(favListItem.glickoDeviation),
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

fun calculateConfidence(deviation: Float): String = numToString(((1500F - deviation) / 1500F * 100F), 1) + "%"

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
            modifier = Modifier.padding(top = 0.dp, bottom = SMALL_PADDING, start = LARGE_PADDING, end = LARGE_PADDING),
        )
    }
}

@Composable
@Preview
fun FavListItemDetailsPreview() {
    FavListItemDetails(sampleFavListItem)
}
