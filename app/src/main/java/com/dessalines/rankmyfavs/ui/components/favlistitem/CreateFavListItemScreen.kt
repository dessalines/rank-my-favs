package com.dessalines.rankmyfavs.ui.components.favlistitem

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemInsert
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFavListItemScreen(
    navController: NavController,
    favListItemViewModel: FavListItemViewModel,
    favListId: Int,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var favListItem: FavListItem? = null

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.create_item),
                navController = navController,
                showBack = true,
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
                FavListItemForm(
                    onChange = { favListItem = it },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    favListItem?.let {
                        val insert =
                            FavListItemInsert(
                                favListId = favListId,
                                name = it.name,
                                description = it.description,
                            )
                        val insertedId = favListItemViewModel.insert(insert)
                        navController.navigate("itemDetails/$insertedId")
                    }
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
