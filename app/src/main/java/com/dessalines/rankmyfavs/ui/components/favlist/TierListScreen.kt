package com.dessalines.rankmyfavs.ui.components.favlist

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.dessalines.rankmyfavs.R
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.FavListItemViewModel
import com.dessalines.rankmyfavs.db.FavListUpdate
import com.dessalines.rankmyfavs.db.FavListViewModel
import com.dessalines.rankmyfavs.db.TierList
import com.dessalines.rankmyfavs.db.TierListInsert
import com.dessalines.rankmyfavs.db.TierListUpdate
import com.dessalines.rankmyfavs.db.TierListViewModel
import com.dessalines.rankmyfavs.ui.components.common.BackButton
import com.dessalines.rankmyfavs.ui.components.common.ColorPickerDialog
import com.dessalines.rankmyfavs.ui.components.common.FLOATING_BUTTON_SIZE
import com.dessalines.rankmyfavs.ui.components.common.LARGE_HEIGHT
import com.dessalines.rankmyfavs.ui.components.common.LARGE_PADDING
import com.dessalines.rankmyfavs.ui.components.common.MAX_HEIGHT
import com.dessalines.rankmyfavs.ui.components.common.MEDIUM_HEIGHT
import com.dessalines.rankmyfavs.ui.components.common.MEDIUM_PADDING
import com.dessalines.rankmyfavs.ui.components.common.SMALL_HEIGHT
import com.dessalines.rankmyfavs.ui.components.common.SMALL_PADDING
import com.dessalines.rankmyfavs.ui.components.common.ToolTip
import com.dessalines.rankmyfavs.utils.TIER_COLORS
import com.dessalines.rankmyfavs.utils.assignTiersToItems
import com.dessalines.rankmyfavs.utils.generateRandomColor
import com.dessalines.rankmyfavs.utils.tint
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
    tierListViewModel: TierListViewModel,
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

    val favList by favListViewModel.getById(favListId).asLiveData().observeAsState()
    val tierList by tierListViewModel.getFromList(favListId).asLiveData().observeAsState()

    if (tierList?.isEmpty() == true && favList?.tierListInitialized == false) {
        TIER_COLORS.onEachIndexed { index, tier ->
            tierListViewModel.insert(TierListInsert(favListId, tier.key, tier.value.toArgb(), index))
        }
        favList?.let { fl ->
            favListViewModel.update(
                FavListUpdate(
                    fl.id,
                    fl.name,
                    fl.description,
                    true,
                ),
            )
        }
    }

    val favListItems by favListItemViewModel.getFromList(favListId).asLiveData().observeAsState()

    val tieredItems = assignTiersToItems(tierList.orEmpty(), favListItems.orEmpty(), limit)

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
            TopAppBar(
                title = { Text(stringResource(R.string.tier_list)) },
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
                    TierList(favListId, tieredItems, editTierList, tierListViewModel)
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
fun TierList(
    favListId: Int,
    tierList: Map<TierList, List<FavListItem>>,
    editTierList: Boolean,
    tierListViewModel: TierListViewModel?,
) {
    var showAddTierDialog by remember { mutableStateOf(false) }
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
            TierSection(favListId, tier, items, editTierList, tierListViewModel)
        }

        if (editTierList) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(SMALL_PADDING))
                        .height(MEDIUM_HEIGHT)
                        .clickable { showAddTierDialog = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.add_new_tier),
                    modifier = Modifier.size(SMALL_HEIGHT),
                    tint = MaterialTheme.colorScheme.surfaceTint,
                )
            }
        }
    }

    if (showAddTierDialog) {
        AddTierDialog(
            onDismissRequest = { showAddTierDialog = false },
            onAddTier = { tierName, color ->
                tierListViewModel?.insert(
                    TierListInsert(
                        favListId = favListId,
                        name = tierName,
                        color = color.toArgb(),
                        tierOrder = tierList.size,
                    ),
                )
                showAddTierDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TierSection(
    favListId: Int,
    tier: TierList,
    items: List<FavListItem>,
    editTierList: Boolean,
    tierListViewModel: TierListViewModel?,
) {
    val backgroundColor = Color(tier.color)
    var showColorPicker by remember { mutableStateOf(false) }
    var editTierName by remember { mutableStateOf(false) }
    val controller = rememberColorPickerController()

    // Define minimum and maximum text sizes
    val minTextSize = 8.sp
    val maxTextSize = 32.sp

    // Calculate the text size based on the length of the tier name
    val textSize = lerp(maxTextSize, minTextSize, (tier.name.length - 5) / 50f)

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
                    .fillMaxWidth()
                    .weight(0.4f),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                modifier = Modifier.wrapContentWidth(),
                onClick = { editTierName = true },
                enabled = editTierList,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = backgroundColor.tint(0.3f),
                        disabledContainerColor = Color.Transparent,
                    ),
                shape = RoundedCornerShape(SMALL_PADDING),
            ) {
                Text(
                    text = tier.name,
                    fontStyle = if (editTierList) FontStyle.Italic else FontStyle.Normal,
                    fontSize = textSize,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(LARGE_PADDING))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = LARGE_HEIGHT),
            modifier =
                Modifier
                    .weight(0.6f)
                    // Needs a max height, else it cant calculate the scroll correctly
                    .heightIn(max = MAX_HEIGHT)
                    .padding(start = SMALL_PADDING),
            verticalArrangement = Arrangement.spacedBy(SMALL_PADDING),
            horizontalArrangement = Arrangement.spacedBy(SMALL_PADDING),
        ) {
            items(items) { item ->
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = SMALL_PADDING),
                    color = Color.Black,
                )
            }
        }

        if (editTierList) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                IconButton(onClick = {
                    tierListViewModel?.swapTierOrders(tier, -1)
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = stringResource(R.string.move_up),
                    )
                }
                IconButton(onClick = {
                    tierListViewModel?.swapTierOrders(tier, 1)
                }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = stringResource(R.string.move_down),
                    )
                }

                Spacer(modifier = Modifier.height(SMALL_PADDING))

                FloatingActionButton(
                    onClick = {
                        tierListViewModel?.delete(tier)
                    },
                    shape = CircleShape,
                    modifier =
                        Modifier
                            .imePadding()
                            .size(FLOATING_BUTTON_SIZE),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete),
                    )
                }

                Spacer(modifier = Modifier.height(SMALL_PADDING))

                FloatingActionButton(
                    modifier =
                        Modifier
                            .imePadding()
                            .size(FLOATING_BUTTON_SIZE),
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
        }

        if (showColorPicker) {
            ColorPickerDialog(
                controller = controller,
                onColorSelected = {
                    tierListViewModel?.update(
                        TierListUpdate(
                            tier.id,
                            favListId,
                            tier.name,
                            it.toArgb(),
                            tier.tierOrder,
                        ),
                    )
                },
                onDismissRequest = { showColorPicker = false },
            )
        }

        if (editTierName) {
            BasicAlertDialog(onDismissRequest = { editTierName = false }) {
                Surface(
                    shape = RoundedCornerShape(MEDIUM_PADDING),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(LARGE_PADDING),
                ) {
                    Column(
                        modifier = Modifier.padding(LARGE_PADDING),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            modifier = Modifier.padding(SMALL_PADDING),
                            text = stringResource(R.string.enter_tier_name),
                        )
                        TextField(
                            modifier = Modifier.padding(SMALL_PADDING),
                            value = tier.name,
                            onValueChange = {
                                tierListViewModel?.update(
                                    TierListUpdate(
                                        tier.id,
                                        favListId,
                                        it,
                                        tier.color,
                                        tier.tierOrder,
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTierDialog(
    onDismissRequest: () -> Unit,
    onAddTier: (String, Color) -> Unit,
) {
    var tierName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(generateRandomColor()) }
    var showColorPicker by remember { mutableStateOf(false) }
    val controller = rememberColorPickerController()

    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(MEDIUM_PADDING),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(LARGE_PADDING),
        ) {
            Column(
                modifier = Modifier.padding(LARGE_PADDING),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.enter_tier_name),
                    style = MaterialTheme.typography.bodyLarge,
                )
                OutlinedTextField(
                    value = tierName,
                    onValueChange = { tierName = it },
                    label = { Text(stringResource(R.string.enter_tier_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(LARGE_PADDING))

                Text(
                    text = stringResource(R.string.pick_a_color),
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(modifier = Modifier.height(SMALL_PADDING))

                Box(
                    modifier =
                        Modifier
                            .size(MEDIUM_HEIGHT)
                            .background(selectedColor, shape = CircleShape)
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                showColorPicker = true
                            },
                )

                if (showColorPicker) {
                    ColorPickerDialog(
                        controller = controller,
                        onColorSelected = { color ->
                            selectedColor = color
                        },
                        onDismissRequest = { showColorPicker = false },
                    )
                }

                Spacer(modifier = Modifier.height(LARGE_PADDING))

                Button(
                    onClick = {
                        if (tierName.isNotBlank()) {
                            onAddTier(tierName, selectedColor)
                        }
                    },
                    enabled = tierName.isNotBlank(),
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        }
    }
}

@Composable
@Preview
fun TierListPreview() {
    val tierList =
        TIER_COLORS.entries
            .mapIndexed { index, (tierName, color) ->
                TierList(
                    id = index + 1,
                    favListId = 1,
                    name = tierName,
                    color = color.toArgb(),
                    tierOrder = index,
                ) to (
                    mapOf(
                        "S" to
                            listOf(
                                FavListItem(1, 1, "Item 1", "", 0f, 0f, 0f, 0f, 0),
                                FavListItem(2, 1, "Item 2", "", 0f, 0f, 0f, 0f, 0),
                            ),
                        "A" to
                            listOf(
                                FavListItem(3, 1, "Item 3", "", 0f, 0f, 0f, 0f, 0),
                            ),
                    )[tierName] ?: emptyList()
                )
            }.toMap()

    TierList(
        favListId = 1,
        tierList = tierList,
        editTierList = false,
        tierListViewModel = null,
    )
}

@Composable
@Preview
fun AddTierDialogPreview() {
    AddTierDialog(onDismissRequest = {}) { _, _ -> }
}
