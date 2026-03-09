package android.myguide

import android.R.attr.maxLines
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/*
@Composable
fun MeasureStringList(ident: Boolean, strings: List<Pair<Int, String?>>) {
    //qqq("MeasureStringList"+strings)
    val textMeasurer = rememberTextMeasurer()
    val measurements = remember(strings) {
        strings.associateWith {
            val c = Constraints(maxWidth = (((measures.descriptionWidth - measures.nodePadding * it.first) / fontScale * ratio).toPx()).roundToInt())
            textMeasurer.measure(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * vm.ratioV.value!!,
                            fontWeight = typography.bodySmall.fontWeight,
                        )
                    ) { append(it.second ?: "") }
                },
                style = typography.bodySmall,
                constraints = c,
                overflow = TextOverflow.Ellipsis,
                density = density,
                fontFamilyResolver = fontFamilyResolver,
                maxLines = 2,
                skipCache = true
            )
        }
    }
    strings.mapIndexed { ix, text ->
        val result = measurements[text]!!
        val s = if (result.lineCount > 1) text.second!!.take(result.getLineEnd(1, true)) else (text.second ?: "")
        val str =
            if (result.lineCount > 1 && s != text.second) {
                val take = s.dropLast(1)//.trim()
                qqq("S "+result.lineCount+" " +
                        ((measures.descriptionWidth - measures.nodePadding * text.first)) +" " +
                        take + "=="+ text.second!!.take(result.getLineEnd(0, true)) + " == "+s)
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * vm.ratioV.value!!,
                            fontWeight = typography.bodySmall.fontWeight,
                        )
                    ) { append(take) }
                    withStyle(
                        style = SpanStyle(
                            color = Color.Transparent,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * vm.ratioV.value!!,
                            fontWeight = typography.bodySmall.fontWeight,
                        )
                    ) { append("\u200A") }
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "lastThree",
                            linkInteractionListener = {
                                vm.toolbar.ellipsis(ix)
                            }
                        )
                    ) {
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.None,
                                color = colorScheme.primary,
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize * vm.ratioV.value!!,
                                fontWeight = typography.bodySmall.fontWeight,
                                baselineShift = BaselineShift.Subscript
                            )
                        ) {
                            append("\u2026")
                        }
                    }
                }
            } else
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            textDecoration = TextDecoration.None,
                            fontSize = typography.bodySmall.fontSize * vm.ratioV.value!!,
                        )
                    ) {
                        append(text.second)
                    }
                }
    }
}

@Composable
fun MeasuredFlowList(ident: Boolean) {
    val strings by vm.screen[ident]!!.measures.collectAsState()
    if (strings.isNotEmpty()) MeasureStringList(ident = ident, strings = strings)
}


 */




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

@Composable
fun getLineHeightDp(sp: TextUnit): Dp = with(density) {
    sp.toDp()
}

class Measures(
    val itemHeight: Dp,
//    val descriptionWidth: Dp,
//    val nodePadding: Dp,
    val lineHeight: Dp,
    val padding: Dp
)