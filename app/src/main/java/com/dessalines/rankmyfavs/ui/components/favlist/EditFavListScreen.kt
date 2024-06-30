package com.dessalines.rankmyfavs.ui.components.favlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListUpdate
import com.dessalines.rankmyfavs.db.FavListViewModel
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFavListScreen(
    navController: NavController,
    favListViewModel: FavListViewModel,
    id: Int,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val favList = favListViewModel.getById(id)

    // Copy the favlist from the DB first
    var editedList by remember {
        mutableStateOf(favList)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.edit_list),
                navController = navController,
                onClickBack = {
                    navController.navigate("favListDetails/$id")
                },
            )
        },
        content = { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .imePadding(),
            ) {
                FavListForm(
                    favList = editedList,
                    onChange = { editedList = it },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val update =
                        FavListUpdate(
                            id = editedList.id,
                            name = editedList.name,
                            description = editedList.description,
                        )
                    favListViewModel.update(update)
                    navController.navigateUp()
                },
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = stringResource(R.string.save),
                )
            }
        },
    )
}
