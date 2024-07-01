package com.dessalines.rankmyfavs.ui.components.favlist

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Reviews
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavList
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.FavListViewModel
import com.dessalines.rankmyfavs.db.sampleFavListItem
import com.dessalines.rankmyfavs.ui.components.common.LARGE_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar
import com.dessalines.rankmyfavs.utils.numToString
import dev.jeziellago.compose.markdowntext.MarkdownText

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
                item {
                    if (favListItems.isNullOrEmpty()) {
                        Text(
                            text = stringResource(R.string.no_items),
                            modifier = Modifier.padding(horizontal = LARGE_PADDING),
                        )
                    }
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
                            Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.create_item),
                        )
                    }
                    IconButton(
                        onClick = {
                            navController.navigate("editFavList/$id")
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.edit_list),
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
                            Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.delete),
                        )
                    }
                },
                floatingActionButton = {
                    if ((favListItems?.count() ?: 0) >= 2) {
                        FloatingActionButton(
                            onClick = {
                                navController.navigate("match/$id")
                            },
                            shape = CircleShape,
                        ) {
                            Icon(
                                Icons.Outlined.Reviews,
                                contentDescription = stringResource(R.string.rate),
                            )
                        }
                    }
                },
            )
        },
    )
}

@Composable
fun FavListDetails(favList: FavList) {
    if (!favList.description.isNullOrBlank()) {
        MarkdownText(
            markdown = favList.description,
            linkColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 0.dp, bottom = SMALL_PADDING, start = LARGE_PADDING, end = LARGE_PADDING),
        )
        HorizontalDivider()
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
        trailingContent = {
            Text(
                text = numToString(favListItem.glickoRating, 0),
                style = MaterialTheme.typography.labelMedium,
            )
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
        favListItem = sampleFavListItem,
        onClick = {},
    )
}
