package com.myguide.views


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.zIndex
import com.myguide.R
import com.myguide.UI.BUTTON
import com.myguide.UI.ITEM_HEIGHT
import com.myguide.UI.MARGIN
import com.myguide.colorScheme
import com.myguide.current
import com.myguide.data.Cycler.XY
import com.myguide.data.Details
import com.myguide.data.VM
import com.myguide.data.VM.Type.D
import com.myguide.data.VM.Type.H
import com.myguide.data.VM.Type.T
import com.myguide.data.VM.Type.V
import com.myguide.screen
import com.myguide.toDp
import com.myguide.typography


@Composable
fun ViewItem(
    details: Details,
    type: VM.Type?,
    expand: AnnotatedString?,
    margin: Float,
    ratioH: Float,
    ratioV: Float,
    scale: Float,
    toggle: Boolean?,
    xy: XY,
    modifier: Modifier
) {
    @Composable
    fun Content() {
        if (type != H && details.drawable != null)
            Image(
                painterResource(details.drawable),
                "item icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(
                        width = ITEM_HEIGHT.toDp() * ratioH,
                        height = ITEM_HEIGHT.toDp() * ratioV
                    )
            )
        Column(
            horizontalAlignment =
                if (type == T || type == D) Alignment.CenterHorizontally
                else Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MARGIN.toDp() * margin * ratioH),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    if (details.origin == null)
                        Modifier
                            .fillMaxHeight()
                    else Modifier
            ) {
                Text(
                    details.title,
                    textAlign = if (type == T) TextAlign.Center else TextAlign.Start,
                    color = colorScheme.secondary,
                    fontSize = typography.bodyLarge.fontSize,
                    lineHeight = typography.bodyLarge.fontSize,
                    maxLines =
                        when (type!!) {
                            D, T -> 2
                            V -> Int.MAX_VALUE
                            else -> 1
                        },
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodyLarge
                )
                if (details.origin == null && type != D) {
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(R.drawable._back),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorScheme.secondary),
                        modifier = Modifier
                            .clickable(
                                onClick = { screen[current.value!!]!!.toggle(xy.i) }
                            )
                            .rotate(if (toggle == true) 90f else -90f)
                            .size(
                                BUTTON.toDp() * ratioH,
                                BUTTON.toDp() * ratioV
                            )
                            .padding(
                                horizontal = MARGIN.toDp() * ratioH,
                                vertical = MARGIN.toDp() * ratioV
                            )
                    )
                }
            }
            if (details.origin != null && type != D)
                Text(
                    details.origin,
                    color = colorScheme.secondary,
                    fontSize = typography.bodyMedium.fontSize,
                    lineHeight = typography.bodyMedium.fontSize,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodyMedium,
                )
            if (expand != null && type != T && type != D)
                Text(
                    expand,
                    maxLines =
                        if (type == H) 2
                        else Int.MAX_VALUE,
                    modifier = Modifier
                        .fillMaxWidth(),
                    lineHeight = typography.bodySmall.fontSize,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodySmall,
                )
        }
    }

    val modifier = Modifier
        .offset(
            x = xy.x.toDp(),
            y = xy.y.toDp() * ratioV * scale
        )
        .size(
            width = xy.w.toDp(),
            height = xy.h.toDp() * ratioV * scale
        )
        .padding(bottom = MARGIN.toDp() * margin * ratioV)
        .then(
            if (details.origin == null) Modifier
            else modifier
        )
        .zIndex(0f)
    if (type != T && type != D)
        Row(
            modifier
                .padding(
                    start =
                        MARGIN.toDp() * margin * ratioH
                                + MARGIN.toDp() * margin * ratioH * details.level / 2,
                    end = MARGIN.toDp() * margin * ratioH
                )
                .background(color = colorScheme.surfaceContainer)
        ) { Content() }
    else
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(
                    horizontal = MARGIN.toDp() * margin * ratioH,
                    vertical = MARGIN.toDp() * margin * ratioV
                )
                .background(color = colorScheme.surfaceContainer)
        ) { Content() }
}
