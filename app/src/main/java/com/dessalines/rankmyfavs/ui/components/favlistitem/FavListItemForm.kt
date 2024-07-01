package com.dessalines.rankmyfavs.ui.components.favlistitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING

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

    Column(
        modifier = Modifier.padding(horizontal = SMALL_PADDING),
        verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
    ) {
        OutlinedTextField(
            label = { Text(stringResource(R.string.title)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = {
                name = it
                onChange(
                    FavListItem(
                        id = favListItem?.id ?: 0,
                        favListId = favListItem?.favListId ?: 0,
                        name = name,
                        description = description,
                        winRate = favListItem?.winRate ?: 0F,
                        glickoRating = favListItem?.glickoRating ?: 0F,
                        glickoDeviation = favListItem?.glickoDeviation ?: 0F,
                        glickoVolatility = favListItem?.glickoVolatility ?: 0F,
                    ),
                )
            },
        )

        OutlinedTextField(
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth(),
            value = description,
            onValueChange = {
                description = it
                onChange(
                    FavListItem(
                        id = favListItem?.id ?: 0,
                        favListId = favListItem?.favListId ?: 0,
                        name = name,
                        description = description,
                        winRate = favListItem?.winRate ?: 0F,
                        glickoRating = favListItem?.glickoRating ?: 0F,
                        glickoDeviation = favListItem?.glickoDeviation ?: 0F,
                        glickoVolatility = favListItem?.glickoVolatility ?: 0F,
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
