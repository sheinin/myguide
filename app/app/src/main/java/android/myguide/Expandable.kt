package android.myguide

import android.myguide.UI.ITEM_HEIGHT
import android.myguide.UI.MARGIN
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.em
import java.lang.Integer.max
import kotlin.math.max

object Expandable {
    fun expanded(ix: Int, ratioV: Float, txt: String) : AnnotatedString =
        buildAnnotatedString {
            withStyle(ParagraphStyle(lineHeight = 1.em * ratioV)) {
                withStyle(
                    style = SpanStyle(
                        color = colorScheme.secondary,
                        fontStyle = typography.bodySmall.fontStyle,
                        fontSize = typography.bodySmall.fontSize * ratioV,
                        fontWeight = typography.bodySmall.fontWeight
                    )
                ) { append(txt) }
                withStyle(
                    style = SpanStyle(
                        color = Color.Transparent,
                        textDecoration = TextDecoration.None,
                        fontStyle = typography.bodySmall.fontStyle,
                        fontSize = typography.bodySmall.fontSize * ratioV,
                        fontWeight = typography.bodySmall.fontWeight
                    )
                ) { append("\u200A") }
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "lastThree",
                        linkInteractionListener = {
                            screen[current.value!!]!!.render.expand(ix, false)
                        }
                    )
                ) {
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.None,
                            color = colorScheme.primary,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * ratioV,
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) {
                        append("\u2026")
                    }
                }
            }
        }
    fun static(ratioV: Float, txt: String): AnnotatedString =
        buildAnnotatedString {
            withStyle(ParagraphStyle(lineHeight = 1.em * ratioV)) {
                withStyle(
                    style = SpanStyle(
                        color = colorScheme.secondary,
                        textDecoration = TextDecoration.None,
                        fontStyle = typography.bodySmall.fontStyle,
                        fontSize = typography.bodySmall.fontSize * ratioV,
                        fontWeight = typography.bodySmall.fontWeight
                    )
                ) { append(txt) }
            }
        }
    fun expandable(
        ix: Int,
        level: Int,
        margin: Float,
        ratioH: Float,
        ratioV: Float,
        scale: Float,
        txt: String?,
    ): AnnotatedString {
        if (txt == null) return static(ratioV, "")
        val result = androidx.compose.ui.text.Paragraph(
            text = txt,
            style = typography.bodySmall,
            spanStyles = listOf(
                AnnotatedString.Range(
                    SpanStyle(
                        fontStyle = typography.bodySmall.fontStyle,
                        fontSize = typography.bodySmall.fontSize,
                        fontWeight = typography.bodySmall.fontWeight,
                    ),
                    0,
                    txt.length
                )
            ),
            constraints = Constraints(
                maxWidth =
                    max(
                        MARGIN.toPx(),
                        (
                            (screenWidth - (
                                ITEM_HEIGHT +
                                        MARGIN.times(4) * margin +
                                        MARGIN.times(2) * margin * level
                                ) * ratioH)
                        ).toPx()
                    )
                    .toInt()
            ),
            density = Density(
                density = density.density,
                fontScale = 1f * scale * ratioV
            ),
            fontFamilyResolver = fontFamilyResolver,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (result.lineCount < 2) return static(ratioV, txt)
        val take =
            txt.take(
                result.getLineEnd(
                    1,
                    true
                )
            )
        if (txt.length == take.length) return static(ratioV, txt)

          /*      qqq(
                     " EXPAND " + result.lineCount
                    + txt.take(result.getLineEnd(0, true))
                    + "=="
                    + take
                    + " == "
                    + txt
                )


           */




        val str =
            buildAnnotatedString {
                withStyle(
                    ParagraphStyle(lineHeight = typography.bodySmall.fontSize)
                ) {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize,
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append(take.dropLast(1)) }
                    withStyle(
                        style = SpanStyle(
                            color = Color.Transparent,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize,
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append("\u200A") }
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "lastThree",
                            linkInteractionListener = {
                                screen[current.value!!]!!.render.expand(ix, true)
                            }
                        )
                    ) {
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.None,
                                color = colorScheme.primary,
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize,
                                fontWeight = typography.bodySmall.fontWeight,
                            )
                        ) { append("\u2026") }
                    }
                }
            }
        return str
    }
}