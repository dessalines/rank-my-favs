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
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.FavListMatchInsert
import com.dessalines.rankmyfavs.db.FavListMatchViewModel
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScreen(
    navController: NavController,
    favListItemViewModel: FavListItemViewModel,
    favListMatchViewModel: FavListMatchViewModel,
    favListId: Int,
) {
    val matchup = favListItemViewModel.randomMatch(favListId).sortedBy { it.id }

    val first = matchup.getOrNull(0)
    val second = matchup.getOrNull(1)

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
                                favListMatchViewModel.insert(
                                    FavListMatchInsert(first.id, second.id, first.id),
                                )
                                navController.navigate("match/$favListId")
                            },
                        )
                        MatchItem(
                            favListItem = second,
                            onClick = {
                                favListMatchViewModel.insert(
                                    FavListMatchInsert(first.id, second.id, second.id),
                                )
                                navController.navigate("match/$favListId")
                            },
                        )
                    }
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
        favListItem = FavListItem(id = 1, favListId = 1, name = "Option 1"),
        onClick = {},
    )
}
