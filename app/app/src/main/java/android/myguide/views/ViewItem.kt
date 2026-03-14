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
import android.myguide.vmm
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
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
    ratio: Float?,
    ratioH: Float?,
    ratioV: Float?,
    toggle: Boolean?,
    xy: Cycler.XY,
    callback: (Int) -> Unit
) {
    if (details.title.isEmpty() || xy == Cycler.XY(0.dp, 0.dp, 0.dp, 0.dp)) return
    Row(
        modifier = Modifier
            .offset(xy.x, xy.y)
            .size(xy.w, xy.h * (ratioV ?: ratio!!))
            .background(color = colorScheme.surfaceContainer)
            .padding(
                start = measures.padding * (ratioH ?: ratio!!)
                        + measures.padding.times(2) * (ratioH ?: ratio!!) * details.level,
                end = measures.padding * (ratioH ?: ratio!!)
            )
            .then(
                if (details.origin == null) Modifier
                else Modifier.clickable(onClick = { callback(index) })
            )
    ) {
        if (display != MAP && details.drawable != null)
            Image(
                painterResource(details.drawable),
                "item icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(
                        width = measures.itemHeight * (ratioH ?: ratio!!),
                        height = measures.itemHeight * (ratioV ?: ratio!!)
                    )
            )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp * (ratioH ?: ratio!!)),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    details.title,
                    color = colorScheme.secondary,
                    fontSize = typography.bodyLarge.fontSize * (ratioV ?: ratio!!),
                    lineHeight = 1.em * fontScale,
                    maxLines = if (display == MAP) 1 else Int.MAX_VALUE,
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
                                horizontal = 6.dp * (ratioH ?: ratio!!),
                                vertical = 6.dp * (ratioV ?: ratio!!)
                            )
                            .rotate(if (toggle == true) 90f else -90f)
                            .size(
                                36.dp * (ratioH ?: ratio!!),
                                36.dp * (ratioV ?: ratio!!)
                            )
                    )
                }
            }
            if (details.origin != null)
                Text(
                    details.origin,
                    color = colorScheme.secondary,
                    fontSize = typography.bodyMedium.fontSize * (ratioV ?: ratio!!),
                    fontStyle = FontStyle.Italic,
                    lineHeight = 1.em * fontScale,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodyMedium,
                )
            if (expand != null)
                Text(
                    expand,
                    maxLines =
                        if (display == MAP) 2
                        else Int.MAX_VALUE,
                    modifier = Modifier
                        .fillMaxWidth(),
                    overflow = TextOverflow.Ellipsis,
                    style = typography.bodySmall,
                )
        }
    }
}
