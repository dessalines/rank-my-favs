package com.dessalines.rankmyfavs.ui.components.favlistitem

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavListItemDetailScreen(
    navController: NavController,
    favListItemViewModel: FavListItemViewModel,
    id: Int,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val scrollState = rememberScrollState()

    val favListItem = favListItemViewModel.getById(id)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SimpleTopAppBar(
                text = favListItem.name,
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
                    FavListItemDetails(favListItem)
                }
                // TODO Show matches?
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(
                        onClick = {
                            favListItemViewModel.delete(favListItem)
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
    favListItem.description?.let {
        // TODO do markdown here
        Text(it)
    }
}

@Composable
@Preview
fun FavListItemDetailsPreview() {
    FavListItemDetails(FavListItem(id = 1, favListId = 1, name = "Fav List 1", description = "ok"))
}
