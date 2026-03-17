package android.myguide.views

import android.myguide.data.Details
import android.myguide.R
import android.myguide.UI.BUTTON
import android.myguide.UI.ITEM_HEIGHT
import android.myguide.UI.MARGIN
import android.myguide.colorScheme
import android.myguide.current
import android.myguide.data.Cycler.XY
import android.myguide.data.VM
import android.myguide.data.VM.Display.*
import android.myguide.screen
import android.myguide.screenWidth
import android.myguide.toolbar
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


@Composable
fun ViewItem(
    details: Details,
    display: VM.Display?,
    expand: AnnotatedString?,
    margin: Float,
    ratioH: Float,
    ratioV: Float,
    scale: Float,
    toggle: Boolean?,
    xy: XY,
) {
    @Composable
    fun Content() {
        if (display != H && details.drawable != null)
            Image(
                painterResource(details.drawable),
                "item icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(
                        width = ITEM_HEIGHT * ratioH,
                        height = ITEM_HEIGHT * ratioV
                    )
            )
        Column(
            horizontalAlignment =
                if (display == T) Alignment.CenterHorizontally
                else Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MARGIN * margin * ratioH),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    details.title,
                    textAlign = if (display == T) TextAlign.Center else TextAlign.Start,
                    color = colorScheme.secondary,
                    fontSize = typography.bodyLarge.fontSize,
                    lineHeight = typography.bodyLarge.fontSize,
                    maxLines =
                        when (display!!) {
                            T -> 2
                            V -> Int.MAX_VALUE
                            H -> 1
                        },
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodyLarge
                )
                if (details.origin == null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorScheme.secondary),
                        modifier = Modifier
                            .clickable(
                                onClick = { screen[current.value!!]!!.render.toggle(xy.i) }
                            )
                            .rotate(if (toggle == true) 90f else -90f)
                            .size(
                                BUTTON * ratioH,
                                BUTTON * ratioV
                            )
                            .padding(
                                horizontal = 6.dp * ratioH,
                                vertical = 6.dp * ratioV
                            )
                    )
                }
            }
            if (details.origin != null)
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
            if (expand != null && display != T)
                Text(
                    expand,
                    maxLines =
                        if (display == H) 2
                        else Int.MAX_VALUE,
                    modifier = Modifier
                        .fillMaxWidth(),
                    lineHeight = typography.bodySmall.fontSize,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodySmall,
                )
        }
    }
    //qqq("I "+xy.i+" "+xy.h+index+" "+details.title + " "+(68.dp * xy.i * ratioV * scale)+" "+itemHeight +" "+ xy.i +" "+ ratioV +" "+ scale)
    val modifier = Modifier
        .offset(
            x = xy.x,
            y = xy.y * ratioV * scale
        )
        .size(
            width = screenWidth,
            height = (xy.d + xy.h) * ratioV * scale
        )
        .padding(bottom = MARGIN * margin * ratioV)
        .then(
            if (details.origin == null) Modifier
            else
                Modifier
                    .clickable(
                        onClick = {
                            toolbar.navigate(
                                id =
                                    screen[current.value!!]!!.render.let {
                                        it.list[it.data.point[xy.i]].id
                                    },
                                title = details.title
                            )
                        }
                    )
        )
    if (display != T)
        Row(
            modifier
                .padding(
                    start = MARGIN * margin * ratioH
                            + MARGIN * margin.times(2) * ratioH * details.level,
                    end = MARGIN * margin * ratioH
                )
                .background(color = colorScheme.surfaceContainer)
        ) { Content() }
    else
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(
                    horizontal = MARGIN * margin * ratioH,
                    vertical = MARGIN * margin * ratioV
                )
                .background(color = colorScheme.surfaceContainer)
        ) { Content() }
}
