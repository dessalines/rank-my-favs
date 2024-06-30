package com.dessalines.rankmyfavs.ui.components.favlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
    val snackbarHostState = remember { SnackbarHostState() }

    val scrollState = rememberScrollState()

    val favList = favListViewModel.getById(id)
    val favListItems by favListItemViewModel.getFromList(id).asLiveData().observeAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SimpleTopAppBar(
                text = favList.name,
                navController = navController,
                showBack = true,
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
                    IconButton(
                        onClick = {
                            favListViewModel.delete(favList)
                            navController.navigateUp()
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
    favList.description?.let {
        // TODO do markdown here
        Text(it)
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
    FavListItemRow(FavListItem(id = 1, favListId = 1, name = "Fav List 1", description = "ok"), onClick = {})
}
