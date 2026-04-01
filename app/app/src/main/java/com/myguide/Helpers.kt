package com.myguide

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import kotlin.math.roundToInt


fun qqq(q: String) {
    println("qqq $q")
}

@Composable
fun GetScreenSize() {
    with(LocalDensity.current) {
        screenHeight = LocalWindowInfo.current.containerSize.height.toDp()
        screenWidth = LocalWindowInfo.current.containerSize.width.toDp()
    }
}

suspend fun checkFirstRun(context: Context) {
    val key = booleanPreferencesKey("example_key")
    val run = context.dataStore.data.first()
    if (run[key] == null) {
        db.updateItemList { list ->
            list.filter { it.pic != null }
                .map {
                    db.updateItem(
                        context.resources.getIdentifier(
                            it.pic,
                            "drawable",
                            context.packageName
                        ),
                        it.pic!!
                    )
                }
        }
        db.updateShopList { list ->
            list.map {
                db.updateShop(
                    context.resources.getIdentifier(
                        it.id,
                        "drawable",
                        context.packageName
                    ), it.id
                )
            }
        }
        context.dataStore.edit { it[key] = false }
    }
}

fun sleep(delay: Long = 0, callback: (() -> Unit)) {
    Handler(Looper.getMainLooper()).postDelayed({ callback.invoke() }, delay)
}

fun Dp.toPx(): Float {
    return with(density) {
        this@toPx.toPx()
    }
}

fun Int.toDp(): Dp {
    return with(density) {
        this@toDp.toDp()
    }
}

fun Float.toDp(): Dp {
    return with(density) {
        this@toDp.toDp()
    }
}

fun Dp.round(): String {
    return this@round.value.roundToInt().toString() + ".dp"
}


object UI {
    val MAP_WIDTH = 400.dp
    var TITLE_HEIGHT = 0
    var ITEM_HEIGHT = 0
    val mapViewWidth = (screenWidth - 16.dp).toPx().toInt()
    val MARGIN = 8.dp.toPx().toInt()
    val BUTTON = 36.dp.toPx().toInt()
    const val COLUMNS: Int = 2
}

/*

lateinit var frames: List<Map<Long, List<Pair<Int, List<Int>>>>>


fun parseFrameColorMap(json: String): Map<Long, List<Pair<Int, List<Int>>>> {
    val root = JSONObject(json)
    val result = mutableMapOf<Long, List<Pair<Int, List<Int>>>>()

    root.keys().forEach { colorHex ->
        val rowObject = root.getJSONObject(colorHex)
        val rows = rowObject.keys().asSequence()
            .map { it.toInt() }
            .sorted()
            .map { row ->
                val valuesArray = rowObject.getJSONArray(row.toString())
                val values = List(valuesArray.length()) { idx -> valuesArray.getInt(idx) }
                row to values
            }
            .toList()
        result[hexToArgbLong(colorHex)] = rows
    }

    return result
}

private fun hexToArgbLong(hex: String): Long {
    val raw = hex.removePrefix("#")
    val argb = when (raw.length) {
        6 -> "FF$raw"
        8 -> raw
        else -> error("Invalid color: $hex")
    }
    return argb.toLong(16)
}

var i by remember { mutableIntStateOf(0) }
LaunchedEffect(Unit) {
    while (true) {
        delay(1000L)

        i = if (i >= 2) 0 else i + 1
    }
}
Canvas(
modifier = Modifier.size(
(m * 157).toDp(),
(m * 98).toDp()
)
) {
    frames.take(i.inc()).map{ frame ->
        frame.map {
            val color = it.key
            it.value.map { y ->
                y.second.map { x ->
                    drawRect(
                        color = Color(color),
                        topLeft =
                            Offset(
                                m * x.toFloat(),
                                m * y.first.toFloat()
                            ),
                        size = Size(
                            m * 1f,
                            m * 1f
                        )
                    )
                }
            }
        }
    }
}

frames = listOf(
parseFrameColorMap(this.assets.open("frame.json").bufferedReader().use { it.readText() }),
parseFrameColorMap(this.assets.open("frame1.json").bufferedReader().use { it.readText() }),
parseFrameColorMap(this.assets.open("frame2.json").bufferedReader().use { it.readText() })
)*/