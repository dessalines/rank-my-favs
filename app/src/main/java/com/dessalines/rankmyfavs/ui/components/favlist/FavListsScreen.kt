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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.dessalines.rankmyfavs.db.FavListViewModel
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavListsScreen(
    navController: NavController,
    favListViewModel: FavListViewModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val scrollState = rememberScrollState()

    val favLists by favListViewModel.getAll.asLiveData().observeAsState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.app_name),
                navController = navController,
                showBack = false,
            )
        },
        content = { padding ->
            LazyColumn(
                modifier =
                    Modifier
                        .padding(padding)
//                        .verticalScroll(scrollState)
                        .imePadding(),
            ) {
                items(favLists.orEmpty()) { favList ->
                    FavListRow(
                        favList = favList,
                        onClick = { navController.navigate("favListDetails/${favList.id}") },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("createFavList")
                },
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.create_list),
                )
            }
        },
    )
}

@Composable
fun FavListRow(
    favList: FavList,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(favList.name)
        },
        modifier =
            Modifier.clickable {
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
