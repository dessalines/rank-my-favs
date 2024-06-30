package com.dessalines.rankmyfavs.ui.components.favlistitem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItem

@Composable
fun FavListItemForm(
    favListItem: FavListItem? = null,
    onChange: (FavListItem) -> Unit,
) {
    var name by rememberSaveable {
        mutableStateOf(favListItem?.name.orEmpty())
    }

    var description by rememberSaveable {
        mutableStateOf(favListItem?.description.orEmpty())
    }

    Column {
        TextField(
            label = { Text(stringResource(R.string.title)) },
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = {
                name = it
                onChange(
                    FavListItem(
                        id = favListItem?.id ?: 0,
                        favListId = favListItem?.favListId ?: 0,
                        name,
                        description,
                    ),
                )
            },
        )

        TextField(
            label = { Text(stringResource(R.string.description)) },
            value = description,
            onValueChange = {
                description = it
                onChange(
                    FavListItem(
                        id = favListItem?.id ?: 0,
                        favListId = favListItem?.favListId ?: 0,
                        name,
                        description,
                    ),
                )
            },
        )
    }
}

@Composable
@Preview
fun FavListItemFormPreview() {
    FavListItemForm(onChange = {})
}
