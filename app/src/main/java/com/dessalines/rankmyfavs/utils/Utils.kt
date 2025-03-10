package com.dessalines.rankmyfavs.utils
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.core.net.toUri
import com.dessalines.rankmyfavs.db.FavListItem
import com.dessalines.rankmyfavs.db.TierList
import java.io.IOException
import java.io.OutputStream
import java.util.Random

const val TAG = "com.rank-my-favs"

const val GITHUB_URL = "https://github.com/dessalines/rank-my-favs"
const val MATRIX_CHAT_URL = "https://matrix.to/#/#rank-my-favs:matrix.org"
const val DONATE_URL = "https://liberapay.com/dessalines"
const val LEMMY_URL = "https://lemmy.ml/c/rankmyfavs"
const val MASTODON_URL = "https://mastodon.social/@dessalines"
const val GLICKO_WIKI_URL = "https://en.m.wikipedia.org/wiki/Glicko_rating_system"

val TIER_COLORS =
    mapOf(
        "S" to Color(0XFFFF7F7F),
        "A" to Color(0XFFFFBF7F),
        "B" to Color(0XFFFFDF7F),
        "C" to Color(0XFFFFFF7F),
        "D" to Color(0XFFBFFF7F),
    )

fun openLink(
    url: String,
    ctx: Context,
) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    ctx.startActivity(intent)
}

fun Context.getPackageInfo(): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        packageManager.getPackageInfo(packageName, 0)
    }

fun Context.getVersionCode(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        getPackageInfo().longVersionCode.toInt()
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo().versionCode
    }

fun numToString(
    num: Float,
    decimalPlaces: Int,
): String = String.format("%.${decimalPlaces}f", num)

fun writeData(
    ctx: Context,
    uri: Uri,
    data: String,
) {
    ctx.contentResolver.openOutputStream(uri)?.use {
        val bytes = data.toByteArray()
        it.write(bytes)
    }
}

fun writeBitmap(
    contentResolver: ContentResolver,
    uri: Uri,
    bitmap: Bitmap,
) {
    try {
        val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        } ?: throw IOException("Failed to get output stream")
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun nameIsValid(name: String): Boolean = name.isNotEmpty()

fun convertFavlistToMarkdown(
    title: String,
    favListItems: List<FavListItem>,
): String {
    val items = favListItems.joinToString(separator = "\n") { "1. ${it.name}" }
    return "# $title\n\n$items"
}

fun generateRandomColor(): Color {
    val rnd = Random()
    return Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
}

fun assignTiersToItems(
    tiers: List<TierList>,
    items: List<FavListItem>,
    limit: Int? = null,
): Map<TierList, List<FavListItem>> {
    if (tiers.isEmpty()) {
        return emptyMap()
    }

    // Sort items by glickoRating in descending order
    val sortedItems = items.sortedByDescending { it.glickoRating }

    // Apply limit if provided
    val limitedItems = limit?.let { sortedItems.take(it) } ?: sortedItems

    // Calculate tier thresholds
    val tierMap = mutableMapOf<TierList, List<FavListItem>>()

    if (items.isNotEmpty()) {
        tiers.sortedBy { it.tierOrder }.forEachIndexed { index, tier ->

            val lowerBoundIndex =
                (limitedItems.size * index / tiers.size).coerceAtMost(limitedItems.size - 1)
            val upperBoundIndex =
                (limitedItems.size * (index + 1) / tiers.size).coerceAtMost(limitedItems.size)

            tierMap[tier] = limitedItems.subList(lowerBoundIndex, upperBoundIndex)
        }
    }

    return tierMap
}

fun Color.tint(factor: Float): Color = lerp(this, Color.White, factor)

sealed interface SelectionVisibilityState<out Item> {
    object NoSelection : SelectionVisibilityState<Nothing>

    data class ShowSelection<Item>(
        val selectedItem: Item,
    ) : SelectionVisibilityState<Item>
}
