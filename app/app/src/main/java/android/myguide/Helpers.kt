package android.myguide

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp


@Composable
fun MeasureStringList(strings: List<String?>) {
    val textMeasurer = rememberTextMeasurer()
    val c = Constraints(maxWidth = 222.dp.toPx().toInt())
    val measurements = remember(strings) {
        strings.associateWith { text ->
            textMeasurer.measure(
                text = AnnotatedString(text ?: ""),
                style = typography.bodySmall,
                constraints = c,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
        }
    }
    var count = 0
    strings.forEach { text ->
        val result = measurements[text]!!
        val x = count
        val str =
            if (text == null) null
            else if (result.lineCount > 1 && result.isLineEllipsized(1)) {
                val s = text.take(result.getLineEnd(1, true))
                // qqq("S "+layoutResult.lineCount+" " +s + "=="+ s.take(s.length.dec()))// +measure.first.substring(0, layoutResult.getLineEnd(1).dec())+ "---" +s)
                buildAnnotatedString {
                    val startIndex = s.length
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            textDecoration = TextDecoration.None,
                            fontSize = typography.bodySmall.fontSize,
                        )
                    ) { append(s.take(startIndex.dec()).trim()) }
                    withStyle(
                        style = SpanStyle(
                            color = Color.Transparent,
                            textDecoration = TextDecoration.None,
                            fontSize = typography.bodySmall.fontSize,
                        )
                    ) { append(".") }
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "lastThree",
                            linkInteractionListener = {
                                vm.toolbar.ellipsis(x)
                            }
                        )
                    ) {
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.None,
                                color = colorScheme.primary,
                                fontSize = typography.bodySmall.fontSize,
                            )
                        ) {
                            append("...")
                        }
                    }
                }
            } else
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            textDecoration = TextDecoration.None,
                            fontSize = typography.bodySmall.fontSize,
                        )
                    ) {
                        append(text)
                    }
                }
        if (str != null)
        vm.callback(
            count,
            str
        )
        count += 1
    }
}

@Composable
fun MeasuredFlowList(ident: Boolean) {
    val strings by vm.screen[ident]!!.measures.collectAsState()
    MeasureStringList(strings = strings)
}