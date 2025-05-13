package com.dessalines.rankmyfavs.ui.components.favlistitem

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItemUpdateNameAndDesc
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.ui.components.common.BackButton
import com.dessalines.rankmyfavs.ui.components.common.ToolTip
import com.dessalines.rankmyfavs.utils.nameIsValid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditFavListItemScreen(
    navController: NavController,
    favListItemViewModel: FavListItemViewModel,
    id: Int,
) {
    val scrollState = rememberScrollState()
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()

    val favListItem = favListItemViewModel.getByIdSync(id)

    // Copy the favlist from the DB first
    var editedItem by remember {
        mutableStateOf(favListItem)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_item)) },
                navigationIcon = {
                    BackButton(
                        onBackClick = { navController.navigateUp() },
                    )
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
                FavListItemForm(
                    favListItem = favListItem,
                    onChange = { editedItem = it },
                )
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
                    modifier = Modifier.imePadding(),
                    onClick = {
                        editedItem?.let {
                            if (nameIsValid(it.name)) {
                                val update =
                                    FavListItemUpdateNameAndDesc(
                                        id = it.id,
                                        name = it.name,
                                        description = it.description,
                                    )
                                favListItemViewModel.updateNameAndDesc(update)
                                navController.navigateUp()
                            }
                        }
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
