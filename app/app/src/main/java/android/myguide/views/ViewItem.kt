package android.myguide.views

import android.myguide.Details
import android.myguide.R
import android.myguide.Screen
import android.myguide.colorScheme
import android.myguide.fontScale
import android.myguide.measures
import android.myguide.model.Cycler
import android.myguide.model.VM
import android.myguide.model.VM.Display.*
import android.myguide.typography
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em


@Composable
fun ViewItem(
    index: Int,
    screen: Screen,
    details: Details,
    display: VM.Display?,
    expand: AnnotatedString?,
    ratioH: Float,
    ratioV: Float,
    toggle: Boolean?,
    xy: Cycler.XY,
    callback: (Int) -> Unit
) {
    if (details.title.isEmpty() || xy == Cycler.XY(0.dp, 0.dp, 0.dp, 0.dp)) return
    @Composable
    fun Content() {
        if (display != H && details.drawable != null)
            Image(
                painterResource(details.drawable),
                "item icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(
                        width = measures.itemHeight * ratioH,
                        height = measures.itemHeight * ratioV
                    )
            )
        Column(
            horizontalAlignment =
                if (display == T) Alignment.CenterHorizontally
                else Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp * ratioH),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    details.title,
                    textAlign = if (display == T) TextAlign.Center else TextAlign.Start,
                    color = colorScheme.secondary,
                    fontSize = typography.bodyLarge.fontSize * ratioV,
                    lineHeight = 1.em * fontScale,
                    maxLines =
                        when (display!!) {
                            T -> 2
                            V -> Int.MAX_VALUE
                            H -> 1
                        },
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodyLarge,
                )
                if (details.origin == null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorScheme.secondary),
                        modifier = Modifier
                            .clickable(
                                onClick = { screen.render.toggle(index) }
                            )
                            .padding(
                                horizontal = 6.dp * ratioH,
                                vertical = 6.dp * ratioV
                            )
                            .rotate(if (toggle == true) 90f else -90f)
                            .size(
                                36.dp * ratioH,
                                36.dp * ratioV
                            )
                    )
                }
            }
            if (details.origin != null)
                Text(
                    details.origin,
                    color = colorScheme.secondary,
                    fontSize = typography.bodyMedium.fontSize * ratioV,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 1.em * fontScale,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodyMedium,
                )
            if (expand != null && display != T)
                Text(
                    expand,
                    maxLines =
                        if (display == H) 2
                        else Int.MAX_VALUE,
                    modifier = Modifier
                        .fillMaxWidth(),
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodySmall,
                )
        }
    }
    val modifier = Modifier
        .offset(xy.x, xy.y)
        .size(xy.w, xy.h * ratioV)
        .then(
            if (details.origin == null) Modifier
            else Modifier.clickable(onClick = { callback(index) })
        )
    if (display != T)
        Row(
            modifier
                .padding(
                    start = measures.padding * ratioH
                            + measures.padding.times(2) * ratioH * details.level,
                    end = measures.padding * ratioH
                )
                .background(color = colorScheme.surfaceContainer)
        ) { Content() }
    else// (display == D3)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(
                    horizontal = measures.padding * ratioH,
                    vertical = measures.padding * ratioV
                )
                .background(color = colorScheme.surfaceContainer)
        ) { Content() }
    /*else
        Row(
            modifier
                .padding(
                    start = measures.padding * ratioH
                            + measures.padding.times(2) * ratioH * details.level,
                    end = measures.padding * ratioH
                )
                .background(color = colorScheme.surfaceContainer)
        ) { Content() }

     */
}
