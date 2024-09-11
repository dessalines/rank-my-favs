package com.dessalines.rankmyfavs.ui.components.favlist

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
import com.dessalines.rankmyfavs.db.FavList
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.utils.nameIsValid

@Composable
fun FavListForm(
    favList: FavList? = null,
    onChange: (FavList) -> Unit,
) {
    var name by rememberSaveable {
        mutableStateOf(favList?.name.orEmpty())
    }

    var description by rememberSaveable {
        mutableStateOf(favList?.description.orEmpty())
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
            isError = !nameIsValid(name),
            onValueChange = {
                name = it
                onChange(
                    FavList(
                        id = favList?.id ?: 0,
                        name,
                        description,
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
                    FavList(
                        id = favList?.id ?: 0,
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
fun FavListFormPreview() {
    FavListForm(onChange = {})
}
