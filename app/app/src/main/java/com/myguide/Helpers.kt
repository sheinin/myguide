package com.myguide

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt


fun qqq(q: String) { println("qqq $q") }

@Composable
fun GetScreenSize() {
    with (LocalDensity.current) {
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
                    )
                    , it.id
                )
            }
        }
        context.dataStore.edit { it[key] = false }
    }
}

fun sleep(delay: Long = 0, callback: (() -> Unit)) { Handler(Looper.getMainLooper()).postDelayed({ callback.invoke() }, delay) }

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
    val MAP_WIDTH = 800.dp
    var TITLE_HEIGHT = 0
    var ITEM_HEIGHT = 0
    val mapViewWidth = (screenWidth - 16.dp).toPx().toInt()
    val MARGIN = 8.dp.toPx().toInt()
    val BUTTON = 36.dp.toPx().toInt()
    const val COLUMNS: Int = 2
}