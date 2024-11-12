package com.dessalines.rankmyfavs.ui.components.match

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemUpdateStats
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.FavListMatchInsert
import com.dessalines.rankmyfavs.db.FavListMatchViewModel
import com.dessalines.rankmyfavs.db.sampleFavListItem
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar
import com.dessalines.rankmyfavs.ui.components.common.ToolTip
import dev.jeziellago.compose.markdowntext.MarkdownText
import org.goochjs.glicko2.Rating
import org.goochjs.glicko2.RatingCalculator
import org.goochjs.glicko2.RatingPeriodResults

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
fun MatchScreen(
    navController: NavController,
    favListItemViewModel: FavListItemViewModel,
    favListMatchViewModel: FavListMatchViewModel,
    favListId: Int?,
    favListItemId: Int?,
) {
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    val scrollState = rememberScrollState()

    val first =
        if (favListId !== null) {
            favListItemViewModel.leastTrained(favListId)
        } else if (favListItemId !== null) {
            favListItemViewModel.getByIdSync(favListItemId)
        } else {
            null
        }

    fun rematchNav() =
        if (favListId !== null) {
            navController.navigate("match?favListId=$favListId") {
                popUpTo("favLists?favListId=${first?.favListId}")
            }
        } else if (favListItemId !== null) {
            navController.navigate("match?favListItemId=$favListItemId") {
                popUpTo("favLists?favListId=${first?.favListId}")
            }
        } else {
            null
        }

    val second =
        if (first !== null) {
            favListItemViewModel.closestMatch(first.favListId, first.id, first.glickoRating)
        } else {
            null
        }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.rate),
                onBackClick = { navController.navigateUp() },
                actions = {
                    if (first !== null && second !== null) {
                        BasicTooltipBox(
                            positionProvider = tooltipPosition,
                            state = rememberBasicTooltipState(isPersistent = false),
                            tooltip = {
                                ToolTip(stringResource(id = R.string.tie))
                            },
                        ) {
                            IconButton(
                                onClick = {
                                    recalculateStats(
                                        favListItemViewModel = favListItemViewModel,
                                        favListMatchViewModel = favListMatchViewModel,
                                        winner = first,
                                        loser = second,
                                        tie = true,
                                    )
                                    rematchNav()
                                },
                            ) {
                                Icon(
                                    Icons.Outlined.SkipNext,
                                    contentDescription = stringResource(R.string.tie),
                                )
                            }
                        }
                    }
                },
            )
        },
        content = { padding ->
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState),
            ) {
                if (first !== null && second !== null) {
                    FlowRow(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(SMALL_PADDING),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        MatchItem(
                            favListItem = first,
                            onClick = {
                                recalculateStats(
                                    favListItemViewModel = favListItemViewModel,
                                    favListMatchViewModel = favListMatchViewModel,
                                    winner = first,
                                    loser = second,
                                    tie = false,
                                )
                                rematchNav()
                            },
                        )
                        MatchItem(
                            favListItem = second,
                            onClick = {
                                recalculateStats(
                                    favListItemViewModel = favListItemViewModel,
                                    favListMatchViewModel = favListMatchViewModel,
                                    winner = second,
                                    loser = first,
                                    tie = false,
                                )
                                rematchNav()
                            },
                        )
                    }
                } else {
                    Text(stringResource(R.string.no_more_training))
                }
            }
        },
        floatingActionButton = {
            BasicTooltipBox(
                positionProvider = tooltipPosition,
                state = rememberBasicTooltipState(isPersistent = false),
                tooltip = {
                    ToolTip(stringResource(R.string.done))
                },
            ) {
                FloatingActionButton(
                    modifier = Modifier.imePadding(),
                    onClick = {
                        val listId = first?.favListId ?: favListId ?: 1
                        navController.navigate("favLists?favListId=$listId")
                    },
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Done,
                        contentDescription = stringResource(R.string.done),
                    )
                }
            }
        },
    )
}

/**
 * The win rate and other scores are stored on the item row.
 */
fun recalculateStats(
    favListItemViewModel: FavListItemViewModel,
    favListMatchViewModel: FavListMatchViewModel,
    winner: FavListItem,
    loser: FavListItem,
    tie: Boolean,
) {
    if (!tie) {
        // Insert the winner
        favListMatchViewModel.insert(
            FavListMatchInsert(winner.id, loser.id, winner.id),
        )
    }
    val (winRateWinner, matchCountWinner) = calculateWinRate(favListMatchViewModel, winner)
    val (winRateLoser, matchCountLoser) = calculateWinRate(favListMatchViewModel, loser)

    // Initialize Glicko
    val ratingSystem = RatingCalculator(0.06, 0.5)
    val results = RatingPeriodResults()

    val gWinner = Rating("1", ratingSystem)
    val gLoser = Rating("2", ratingSystem)

    gWinner.rating = winner.glickoRating.toDouble()
    gWinner.ratingDeviation = winner.glickoDeviation.toDouble()
    gWinner.volatility = winner.glickoVolatility.toDouble()

    gLoser.rating = loser.glickoRating.toDouble()
    gLoser.ratingDeviation = loser.glickoDeviation.toDouble()
    gLoser.volatility = loser.glickoVolatility.toDouble()

    if (!tie) {
        results.addResult(gWinner, gLoser)
    } else {
        results.addDraw(gWinner, gLoser)
    }

    ratingSystem.updateRatings(results)

    // Update the winner
    favListItemViewModel.updateStats(
        FavListItemUpdateStats(
            id = winner.id,
            winRate = winRateWinner,
            glickoRating = gWinner.rating.toFloat(),
            glickoDeviation = gWinner.ratingDeviation.toFloat(),
            glickoVolatility = gWinner.volatility.toFloat(),
            matchCount = matchCountWinner,
        ),
    )

    // Update the loser
    favListItemViewModel.updateStats(
        FavListItemUpdateStats(
            id = loser.id,
            winRate = winRateLoser,
            glickoRating = gLoser.rating.toFloat(),
            glickoDeviation = gLoser.ratingDeviation.toFloat(),
            glickoVolatility = gLoser.volatility.toFloat(),
            matchCount = matchCountLoser,
        ),
    )
}

fun calculateWinRate(
    favListMatchViewModel: FavListMatchViewModel,
    item: FavListItem,
): Pair<Float, Int> {
    val matches = favListMatchViewModel.getMatchups(item.id)

    val matchCount = matches.count()
    val winCount = matches.count { it.winnerId == item.id }
    var winRate = (100F * winCount / matchCount)

    // Need to do a nanCheck here, for ties
    if (winRate.isNaN()) {
        winRate = 0F
    }

    return Pair(winRate, matchCount)
}

@Composable
fun MatchItem(
    favListItem: FavListItem,
    onClick: () -> Unit,
) {
    OutlinedCard(
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(SMALL_PADDING),
        ) {
            Text(
                text = favListItem.name,
            )
            if (!favListItem.description.isNullOrBlank()) {
                MarkdownText(
                    markdown = favListItem.description,
                    linkColor = MaterialTheme.colorScheme.primary,
                    // There's no way to actually override this
                    onClick = onClick,
                )
            }
        }
    }
}

@Composable
@Preview
fun MatchItemPreview() {
    MatchItem(
        favListItem = sampleFavListItem,
        onClick = {},
    )
}
