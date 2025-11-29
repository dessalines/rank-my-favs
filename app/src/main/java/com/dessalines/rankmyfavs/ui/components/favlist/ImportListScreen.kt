package com.dessalines.rankmyfavs.ui.components.favlist

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItemInsert
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.ui.components.common.BackButton
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.ToolTip
import com.dessalines.rankmyfavs.ui.components.favlistitem.FavListItemForm
import com.dessalines.rankmyfavs.utils.nameIsValid

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImportListScreen(
    navController: NavController,
    favListItemViewModel: FavListItemViewModel,
    favListId: Int,
) {
    val scrollState = rememberScrollState()
    val tooltipPosition = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above)

    var listStr = ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_list)) },
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
                ImportListForm(
                    onChange = { listStr = it },
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
                        val listItems = extractLines(listStr)
                        for (item in listItems) {
                            val insert =
                                FavListItemInsert(
                                    favListId = favListId,
                                    name = item.name,
                                    description = item.description,
                                )
                            favListItemViewModel.insert(insert)
                        }
                        navController.navigateUp()
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

data class FavListItemLine(
    val name: String,
    val description: String?,
)

/**
 * This is of the form: - option 1 name | option 1 description
 */
private fun extractLines(listStr: String): List<FavListItemLine> {
    val listItems =
        listStr
            .lines()
            .map { it.trim() }
            // Remove the preceding list items if necessary
            .map {
                // Remove the markdown list start
                val removedMarkdownListStart =
                    if (it.startsWith("- ") || it.startsWith("* ")) {
                        it.substring(2)
                    } else {
                        it
                    }

                // Split it with |
                val split = removedMarkdownListStart.split("|")
                val name = split[0].trim()
                val description = split.getOrNull(1)?.trim()
                FavListItemLine(name, description)
            }.filter { nameIsValid(it.name) }
    return listItems
}

@Composable
fun ImportListForm(onChange: (String) -> Unit) {
    var listStr by rememberSaveable {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier.padding(horizontal = SMALL_PADDING),
        verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
    ) {
        OutlinedTextField(
            label = { Text(stringResource(R.string.import_list_description)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
            value = listStr,
            onValueChange = {
                listStr = it
                onChange(listStr)
            },
        )
    }
}

@Composable
@Preview
fun FavListItemFormPreview() {
    FavListItemForm(onChange = {})
}
