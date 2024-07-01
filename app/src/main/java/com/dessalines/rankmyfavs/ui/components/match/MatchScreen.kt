package com.dessalines.rankmyfavs.ui.components.match

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import org.goochjs.glicko2.Rating
import org.goochjs.glicko2.RatingCalculator
import org.goochjs.glicko2.RatingPeriodResults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScreen(
    navController: NavController,
    favListItemViewModel: FavListItemViewModel,
    favListMatchViewModel: FavListMatchViewModel,
    favListId: Int,
) {
    val first = favListItemViewModel.leastTrained(favListId)
    val second =
        if (first !== null) {
            favListItemViewModel.randomAndNot(favListId, first.id)
        } else {
            null
        }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.rate),
                navController = navController,
                onClickBack = {
                    navController.navigate("favListDetails/$favListId")
                },
            )
        },
        content = { padding ->
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
            ) {
                if (first !== null && second !== null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                    ) {
                        MatchItem(
                            favListItem = first,
                            onClick = {
                                recalculateStats(
                                    favListItemViewModel = favListItemViewModel,
                                    favListMatchViewModel = favListMatchViewModel,
                                    first = first,
                                    second = second,
                                    winner = first,
                                )
                                navController.navigate("match/$favListId")
                            },
                        )
                        MatchItem(
                            favListItem = second,
                            onClick = {
                                recalculateStats(
                                    favListItemViewModel = favListItemViewModel,
                                    favListMatchViewModel = favListMatchViewModel,
                                    first = first,
                                    second = second,
                                    winner = second,
                                )
                                navController.navigate("match/$favListId")
                            },
                        )
                    }
                } else {
                    Text(stringResource(R.string.no_more_training))
                }
            }
        },
        // TODO need this? I already have a back button
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("favListDetails/$favListId")
                },
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = stringResource(R.string.done),
                )
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
    first: FavListItem,
    second: FavListItem,
    winner: FavListItem,
) {
    // Insert the winner
    favListMatchViewModel.insert(
        FavListMatchInsert(first.id, second.id, winner.id),
    )
    val winRateFirst = calculateWinRate(favListMatchViewModel, first)
    val winRateSecond = calculateWinRate(favListMatchViewModel, second)

    // Initialize Glicko
    val ratingSystem = RatingCalculator(0.06, 0.5)
    val results = RatingPeriodResults()

    val player1 = Rating("1", ratingSystem)
    val player2 = Rating("2", ratingSystem)

    player1.rating = first.glickoRating.toDouble()
    player1.ratingDeviation = first.glickoDeviation.toDouble()
    player1.volatility = first.glickoVolatility.toDouble()

    player2.rating = second.glickoRating.toDouble()
    player2.ratingDeviation = second.glickoDeviation.toDouble()
    player2.volatility = second.glickoVolatility.toDouble()

    // Player1 beats player 2
    if (winner.id == first.id) {
        results.addResult(player1, player2)
    } else {
        results.addResult(player2, player1)
    }
    ratingSystem.updateRatings(results)

    // Update the first
    favListItemViewModel.updateStats(
        FavListItemUpdateStats(
            id = first.id,
            winRate = winRateFirst,
            glickoRating = player1.rating.toFloat(),
            glickoDeviation = player1.ratingDeviation.toFloat(),
            glickoVolatility = player1.volatility.toFloat(),
        ),
    )

    // Update the second
    favListItemViewModel.updateStats(
        FavListItemUpdateStats(
            id = second.id,
            winRate = winRateSecond,
            glickoRating = player2.rating.toFloat(),
            glickoDeviation = player2.ratingDeviation.toFloat(),
            glickoVolatility = player2.volatility.toFloat(),
        ),
    )
}

fun calculateWinRate(
    favListMatchViewModel: FavListMatchViewModel,
    item: FavListItem,
): Float {
    val matches = favListMatchViewModel.getMatchups(item.id)

    val matchCount = matches.count()
    val winCount = matches.count { it.winnerId == item.id }
    val winRate = 100F * winCount / matchCount
    return winRate
}

@Composable
fun MatchItem(
    favListItem: FavListItem,
    onClick: () -> Unit,
) {
    OutlinedCard(
        onClick = onClick,
    ) {
        Text(
            modifier = Modifier.padding(SMALL_PADDING),
            text = favListItem.name,
        )
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
