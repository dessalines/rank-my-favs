package com.dessalines.rankmyfavs.ui.components.favlist

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Reviews
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavList
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.FavListViewModel
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavListDetailScreen(
    navController: NavController,
    favListViewModel: FavListViewModel,
    favListItemViewModel: FavListItemViewModel,
    id: Int,
) {
    val ctx = LocalContext.current

    val favList = favListViewModel.getById(id)
    val favListItems by favListItemViewModel.getFromList(id).asLiveData().observeAsState()

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = favList.name,
                navController = navController,
                onClickBack = {
                    navController.navigate("favLists")
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
                    FavListDetails(favList)
                }
                items(favListItems.orEmpty()) { favListItem ->
                    FavListItemRow(
                        favListItem = favListItem,
                        onClick = {
                            navController.navigate("itemDetails/${favListItem.id}")
                        },
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("createItem/${favList.id}")
                        },
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.create_item),
                        )
                    }
                    val deletedMessage = stringResource(R.string.list_deleted)
                    IconButton(
                        onClick = {
                            favListViewModel.delete(favList)
                            navController.navigateUp()
                            Toast.makeText(ctx, deletedMessage, Toast.LENGTH_SHORT).show()
                        },
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                        )
                    }
                    IconButton(
                        onClick = {
                            navController.navigate("match/$id")
                        },
                    ) {
                        Icon(
                            // TODO find a good icon for this
                            Icons.Filled.Reviews,
                            contentDescription = stringResource(R.string.rate),
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("editFavList/$id")
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
fun FavListDetails(favList: FavList) {
    if (!favList.description.isNullOrBlank()) {
        Text(favList.description)
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
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(favListItem.name)
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
        FavListItem(id = 1, favListId = 1, name = "Fav List 1", description = "ok"),
        onClick = {},
    )
}
