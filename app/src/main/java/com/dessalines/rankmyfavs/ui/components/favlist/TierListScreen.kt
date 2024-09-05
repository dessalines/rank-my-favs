package com.dessalines.rankmyfavs.ui.components.favlist

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.ui.components.common.LARGE_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar
import com.dessalines.rankmyfavs.ui.components.common.ToolTip
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TierListScreen(
    navController: NavController,
    favListItemViewModel: FavListItemViewModel,
    favListId: Int,
) {
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()
    var inputLimit by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf<Int?>(null) }

    val tierList = favListItemViewModel.getFromListTiered(favListId, limit)

    LaunchedEffect(inputLimit) {
        delay(500) // limit shouldn't be updated instantly

        if (inputLimit.isBlank()) {
            limit = null // remove the limit if empty
        } else {
            inputLimit.toIntOrNull()?.let {
                if (it >= 0) {
                    limit = it
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.tier_list),
                navController = navController,
            )
        },
        content = { padding ->
            Column(
                modifier =
                Modifier
                    .padding(padding)
                    .imePadding(),
            ) {
                OutlinedTextField(
                    label = { Text(stringResource(R.string.tier_list_limit_description)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SMALL_PADDING),
                    value = inputLimit,
                    onValueChange = { newLimit ->
                        inputLimit = newLimit
                    },
                    singleLine = true
                )

                TierList(tierList)
            }
        },
        floatingActionButton = {
            BasicTooltipBox(
                positionProvider = tooltipPosition,
                state = rememberBasicTooltipState(isPersistent = false),
                tooltip = {
                    ToolTip(stringResource(R.string.save))
                },
            ) {
                FloatingActionButton(
                    onClick = {
                        // TODO: take TierList component screenshot and save
                        navController.navigateUp()
                    },
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = stringResource(R.string.save),
                    )
                }
            }
        },
    )
}

@Composable
fun TierList(tierList: Map<String, List<FavListItem>>) {
    Column(
        modifier = Modifier.padding(horizontal = SMALL_PADDING),
        verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
    ) {
        tierList.forEach { (tier, items) ->
            TierSection(tier, items)
        }
    }
}

@Composable
fun TierSection(tier: String, items: List<FavListItem>) {
    val tierColors = mapOf(
        "S" to Color(0XFFFF7F7F),
        "A" to Color(0XFFFFBF7F),
        "B" to Color(0XFFFFDF7F),
        "C" to Color(0XFFFFFF7F),
        "D" to Color(0XFFBFFF7F)
    )

    val backgroundColor = tierColors[tier] ?: Color.LightGray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = backgroundColor, shape = RoundedCornerShape(SMALL_PADDING))
            .padding(LARGE_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(0.2f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tier,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.width(LARGE_PADDING))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier
                .weight(0.8f)
                .padding(start = SMALL_PADDING),
            verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
            horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
        ) {
            items(items) { item ->
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
@Preview
fun TierListPreview() {
    TierList(
        mapOf(
            "S" to listOf(
                FavListItem(id = 1, favListId = 1, name = "Item 1", winRate = 0f, glickoRating = 0f, glickoDeviation = 0f, glickoVolatility = 0f, matchCount = 0),
                FavListItem(id = 2, favListId = 1, name = "Item 2", winRate = 0f, glickoRating = 0f, glickoDeviation = 0f, glickoVolatility = 0f, matchCount = 0)
            ),
            "A" to listOf(
                FavListItem(id = 3, favListId = 1, name = "Item 3", winRate = 0f, glickoRating = 0f, glickoDeviation = 0f, glickoVolatility = 0f, matchCount = 0)
            ),
            "B" to listOf(
                FavListItem(id = 3, favListId = 1, name = "Item 4", winRate = 0f, glickoRating = 0f, glickoDeviation = 0f, glickoVolatility = 0f, matchCount = 0)
            ),
            "C" to listOf(
                FavListItem(id = 3, favListId = 1, name = "Item 5", winRate = 0f, glickoRating = 0f, glickoDeviation = 0f, glickoVolatility = 0f, matchCount = 0)
            ),
            "D" to listOf(
                FavListItem(id = 3, favListId = 1, name = "Item 6", winRate = 0f, glickoRating = 0f, glickoDeviation = 0f, glickoVolatility = 0f, matchCount = 0)
            )
        )
    )
}
