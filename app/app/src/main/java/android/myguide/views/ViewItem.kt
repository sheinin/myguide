package android.myguide.views

import android.myguide.Details
import android.myguide.R
import android.myguide.Screen
import android.myguide.ViewModel
import android.myguide.ViewModel.Screen.*
import android.myguide.ViewModel.Screen.Display.*
import android.myguide.colorScheme
import android.myguide.fontScale
import android.myguide.measures
import android.myguide.typography
import android.myguide.vm
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
    display: Display?,
    expand: AnnotatedString?,
    toggle: Boolean?,
    xy: ViewModel.Cycler.XY,
    callback: (Int) -> Unit
) {
    val ratio by vm.ratio.observeAsState()
    val ratioH by vm.ratioH.observeAsState()
    val ratioV by vm.ratioV.observeAsState()
    if (details.title.isEmpty() || xy == ViewModel.Cycler.XY(0.dp, 0.dp, 0.dp, 0.dp)) return
    Row(
       // verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .offset(xy.x, xy.y * (ratioV ?: ratio!!))
            .size(xy.w, xy.h * (ratioV ?: ratio!!))
            .then(
                if (details.origin == null) Modifier
                else Modifier.clickable(onClick = { callback(index) })
            )
            .padding(
                start = measures.padding * (ratioH ?: ratio!!) + measures.padding.times(2) * (ratioH ?: ratio!!) * details.level,
                end = measures.padding * (ratioH ?: ratio!!)
            )
            .background(
                color = colorScheme.surfaceContainer
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
            Row {
                Text(
                    details.title,
                 //   onTextLayout = { textLayoutResult ->
                   //     qqq("WT "+textLayoutResult.size.width.toDp() + textLayoutResult.size.height.toDp() + details.title)
                   // },
                    color = colorScheme.secondary,
                    style = typography.bodyLarge,
                    fontSize = typography.bodyLarge.fontSize * (ratioV ?: ratio!!),
                    lineHeight = 1.em * fontScale,
                    maxLines = if (display == MAP) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )
                if (details.origin == null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorScheme.secondary),
                        modifier = Modifier
                            .size(
                                36.dp * (ratioH ?: ratio!!),
                                36.dp * (ratioV ?: ratio!!)
                            )
                            .padding(
                                horizontal = 6.dp * (ratioH ?: ratio!!),
                                vertical = 6.dp * (ratioV ?: ratio!!)
                            )
                            .clickable(
                                onClick = {
                                    screen.render.toggle(index)
                                }
                            )
                            .rotate(if (toggle == true) 90f else -90f)
                    )
                }
            }
            if (details.origin != null)
                Text(
                    details.origin,
               //     onTextLayout = { textLayoutResult ->
                 //       qqq("Wo "+textLayoutResult.size.width.toDp() + textLayoutResult.size.height.toDp() + details.title)
                   // },
                    color = colorScheme.secondary,
                    style = typography.bodyMedium,
                    fontSize = typography.bodyMedium.fontSize * (ratioV ?: ratio!!),
                    fontStyle = FontStyle.Italic,
                    lineHeight = 1.em * fontScale,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            if (expand != null)
                Text(
                    expand,
                 //   onTextLayout = { textLayoutResult ->
                   //     qqq("W "+textLayoutResult.size.width.toDp() + textLayoutResult.size.height.toDp() + expand)
                    //},
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = typography.bodySmall,
                    //lineHeight = 1.em,
                    maxLines =
                        if (display == MAP) 2
                        else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                )
        }
    }
}
