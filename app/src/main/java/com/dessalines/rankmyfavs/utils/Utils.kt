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
import java.io.IOException
import java.io.OutputStream

const val TAG = "com.rank-my-favs"

const val GITHUB_URL = "https://github.com/dessalines/rank-my-favs"
const val MATRIX_CHAT_URL = "https://matrix.to/#/#rank-my-favs:matrix.org"
const val DONATE_URL = "https://liberapay.com/dessalines"
const val LEMMY_URL = "https://lemmy.ml/c/rankmyfavs"
const val MASTODON_URL = "https://mastodon.social/@dessalines"
const val GLICKO_WIKI_URL = "https://en.m.wikipedia.org/wiki/Glicko_rating_system"

const val ANIMATION_SPEED = 300

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
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
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
    contentResolver: ContentResolver,
    uri: Uri,
    data: String,
) {
    contentResolver.openOutputStream(uri)?.use {
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
