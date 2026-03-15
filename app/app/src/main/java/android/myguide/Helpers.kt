package android.myguide

import android.R.attr.padding
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import kotlin.math.roundToInt


fun qqq(q: String) { println("qqq $q") }

@Composable
fun GetScreenSize() {
    with (LocalDensity.current) {
        screenHeight = LocalWindowInfo.current.containerSize.height.toDp()
        screenWidth = LocalWindowInfo.current.containerSize.width.toDp()
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

fun Dp.round(): Int {
    return this@round.value.roundToInt()
}


@Composable
fun getLineHeightDp(sp: TextUnit): Dp = with(density) {
    sp.toDp()
}


class Measures(
    var itemHeight: Dp,
    val mapViewWidth: Dp,
    val padding: Dp,
    val tableColumns: Int
)