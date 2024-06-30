package com.dessalines.rankmyfavs.ui.components.favlistitem

import android.widget.Toast
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.FavListMatchViewModel
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavListItemDetailScreen(
    navController: NavController,
    favListItemViewModel: FavListItemViewModel,
    favListMatchViewModel: FavListMatchViewModel,
    id: Int,
) {
    val ctx = LocalContext.current

    val favListItem = favListItemViewModel.getById(id)
    val matches = favListMatchViewModel.getMatchups(id)

    val matchCount = matches.count()
    val winCount = matches.count { it.winnerId == id }
    val winRate = 100F * winCount / matchCount

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = favListItem.name,
                navController = navController,
                onClickBack = {
                    navController.navigate("favListDetails/${favListItem.favListId}")
                },
            )
        },
        content = { padding ->
            LazyColumn(
                modifier =
                    Modifier
                        .padding(padding)
                        .imePadding(),
            ) {
                item {
                    FavListItemDetails(favListItem)
                }

                // TODO Show matches?
                item {
                    Text("Win Rate: $winRate%")
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    val deletedMessage = stringResource(R.string.item_deleted)
                    IconButton(
                        onClick = {
                            favListItemViewModel.delete(favListItem)
                            navController.navigateUp()
                            Toast.makeText(ctx, deletedMessage, Toast.LENGTH_SHORT).show()
                        },
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("editItem/$id")
                        },
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_list),
                        )
                    }
                },
            )
        },
    )
}

@Composable
fun FavListItemDetails(favListItem: FavListItem) {
    if (!favListItem.description.isNullOrBlank()) {
        // TODO do markdown here
        Text(favListItem.description)
    }
}

@Composable
@Preview
fun FavListItemDetailsPreview() {
    FavListItemDetails(FavListItem(id = 1, favListId = 1, name = "Fav List 1", description = "ok"))
}
