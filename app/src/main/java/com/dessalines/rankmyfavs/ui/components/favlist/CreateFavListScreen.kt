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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavList
import com.dessalines.rankmyfavs.db.FavListInsert
import com.dessalines.rankmyfavs.db.FavListViewModel
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFavListScreen(
    navController: NavController,
    favListViewModel: FavListViewModel,
) {
    val scrollState = rememberScrollState()
    var favList: FavList? = null

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.create_list),
                navController = navController,
                onClickBack = {
                    navController.navigate("favLists")
                },
            )
        },
        content = { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .verticalScroll(scrollState)
                        // TODO can you remove these?
//                    .background(color = MaterialTheme.colorScheme.surface)
                        .imePadding(),
            ) {
                FavListForm(
                    onChange = { favList = it },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    favList?.let {
                        val insert = FavListInsert(name = it.name, description = it.description)
                        val insertedId = favListViewModel.insert(insert)
                        navController.navigate("favListDetails/$insertedId")
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
