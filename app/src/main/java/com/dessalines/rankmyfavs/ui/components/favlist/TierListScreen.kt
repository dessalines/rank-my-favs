package com.dessalines.rankmyfavs.ui.components.favlist

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.FavListViewModel
import com.dessalines.rankmyfavs.ui.components.common.ColorPickerDialog
import com.dessalines.rankmyfavs.ui.components.common.LARGE_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SimpleTopAppBar
import com.dessalines.rankmyfavs.ui.components.common.ToolTip
import com.dessalines.rankmyfavs.utils.TIER_COLORS
import com.dessalines.rankmyfavs.utils.generateRandomColor
import com.dessalines.rankmyfavs.utils.writeBitmap
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalFoundationApi::class,
)
@Composable
fun TierListScreen(
    navController: NavController,
    favListItemViewModel: FavListItemViewModel,
    favListViewModel: FavListViewModel,
    favListId: Int,
) {
    val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()

    var inputLimit by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf<Int?>(null) }
    var editTierList by remember { mutableStateOf(false) }
    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val exportPngLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("image/png"),
        ) {
            it?.let {
                scope.launch {
                    val bitmapAsync = captureController.captureAsync()
                    val bitmap = bitmapAsync.await().asAndroidBitmap()
                    writeBitmap(context.contentResolver, it, bitmap)
                }
            }
        }

    val tierList = favListItemViewModel.getFromListTiered(favListId, limit)
    val favList by favListViewModel.getById(favListId).asLiveData().observeAsState()

    LaunchedEffect(inputLimit) {
        delay(500) // limit shouldn't be updated instantly

        if (inputLimit.isBlank()) {
            limit = null // remove the limit if empty
        } else {
            inputLimit.toIntOrNull()?.let {
                if (it >= 0) {
                    limit = it
                }
            }
        }
    }

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.tier_list),
                navController = navController,
            )
        },
        content = { padding ->
            Column(
                modifier =
                Modifier
                    .padding(padding)
                    .imePadding(),
            ) {
                OutlinedTextField(
                    label = { Text(stringResource(R.string.tier_list_limit_description)) },
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SMALL_PADDING),
                    value = inputLimit,
                    onValueChange = { newLimit ->
                        inputLimit = newLimit
                    },
                    singleLine = true,
                )

                Column(
                    modifier = Modifier.capturable(captureController),
                ) {
                    TierList(tierList, editTierList)
                }
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
                if (!editTierList) {
                    Row(horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING)) {
                        FloatingActionButton(
                            modifier = Modifier.imePadding(),
                            onClick = { editTierList = true },
                            shape = CircleShape,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit_list),
                            )
                        }
                        FloatingActionButton(
                            modifier = Modifier.imePadding(),
                            onClick = {
                                exportPngLauncher.launch("${favList?.name}_tier_list.png")
                            },
                            shape = CircleShape,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = stringResource(R.string.save),
                            )
                        }
                    }
                } else {
                    FloatingActionButton(
                        modifier = Modifier.imePadding(),
                        onClick = { editTierList = false },
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = stringResource(R.string.done),
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun TierList(tierList: Map<String, List<FavListItem>>, editTierList: Boolean) {
    val scrollState = rememberScrollState()
    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(SMALL_PADDING)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
    ) {
        tierList.forEach { (tier, items) ->
            TierSection(tier, items, editTierList)
        }
    }
}

@Composable
fun TierSection(
    tier: String,
    items: List<FavListItem>,
    editTierList: Boolean
) {
    var backgroundColor by remember { mutableStateOf(TIER_COLORS[tier]
        ?: generateRandomColor()) }
    var showColorPicker by remember { mutableStateOf(false) }
    val controller = rememberColorPickerController()

    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .background(color = backgroundColor, shape = RoundedCornerShape(SMALL_PADDING))
            .padding(LARGE_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .weight(0.2f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = tier,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black,
            )
        }

        Spacer(modifier = Modifier.width(LARGE_PADDING))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier =
            Modifier
                .weight(0.8f)
                // Needs a max height, else it cant calculate the scroll correctly
                .heightIn(max = 160.dp)
                .padding(start = SMALL_PADDING),
            verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
            horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
        ) {
            items(items) { item ->
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.Black,
                )
            }
        }

        if (editTierList) {
            FloatingActionButton(
                modifier = Modifier
                    .imePadding()
                    .size(28.dp)
                    .align(Alignment.Bottom),
                onClick = {
                    showColorPicker = true
                },
                shape = RoundedCornerShape(SMALL_PADDING),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Colorize,
                    contentDescription = stringResource(R.string.edit_item),
                )
            }
        }

        if (showColorPicker) {
            ColorPickerDialog(
                controller = controller,
                onColorSelected = { backgroundColor = it },
                onDismissRequest = { showColorPicker = false },
            )
        }
    }
}

@Composable
@Preview
fun TierListPreview() {
    TierList(
        mapOf(
            "S" to
                    listOf(
                        FavListItem(
                            id = 1,
                            favListId = 1,
                            name = "Item 1",
                            winRate = 0f,
                            glickoRating = 0f,
                            glickoDeviation = 0f,
                            glickoVolatility = 0f,
                            matchCount = 0,
                        ),
                        FavListItem(
                            id = 2,
                            favListId = 1,
                            name = "Item 2",
                            winRate = 0f,
                            glickoRating = 0f,
                            glickoDeviation = 0f,
                            glickoVolatility = 0f,
                            matchCount = 0,
                        ),
                    ),
            "A" to
                    listOf(
                        FavListItem(
                            id = 3,
                            favListId = 1,
                            name = "Item 3",
                            winRate = 0f,
                            glickoRating = 0f,
                            glickoDeviation = 0f,
                            glickoVolatility = 0f,
                            matchCount = 0,
                        ),
                    ),
            "B" to
                    listOf(
                        FavListItem(
                            id = 3,
                            favListId = 1,
                            name = "Item 4",
                            winRate = 0f,
                            glickoRating = 0f,
                            glickoDeviation = 0f,
                            glickoVolatility = 0f,
                            matchCount = 0,
                        ),
                    ),
            "C" to
                    listOf(
                        FavListItem(
                            id = 3,
                            favListId = 1,
                            name = "Item 5",
                            winRate = 0f,
                            glickoRating = 0f,
                            glickoDeviation = 0f,
                            glickoVolatility = 0f,
                            matchCount = 0,
                        ),
                    ),
            "D" to
                    listOf(
                        FavListItem(
                            id = 3,
                            favListId = 1,
                            name = "Item 6",
                            winRate = 0f,
                            glickoRating = 0f,
                            glickoDeviation = 0f,
                            glickoVolatility = 0f,
                            matchCount = 0,
                        ),
                    ),
        ),
        false
    )
}
