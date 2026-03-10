package android.myguide

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em


@Composable
fun ViewItem(
    index: Int,
    screen: Screen,
    details: Details,
    display: Settings.Display?,
    expand: AnnotatedString?,
    more: Boolean?,
    xy: ViewModel.Cycler.XY,
    callback: (Int) -> Unit
) {
    val ratioH by vm.ratioH.observeAsState()
    val ratioV by vm.ratioV.observeAsState()
    if (details.title.isEmpty() || xy == ViewModel.Cycler.XY(0.dp, 0.dp, 0.dp, 0.dp)) return
    Row(
       // verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .offset(xy.x, xy.y * ratioV!!)
            .size(xy.w, xy.h * ratioV!!)
            .then(
                if (details.origin == null) Modifier
                else Modifier.clickable(onClick = { callback(index) })
            )
            .padding(
                start = measures.padding * ratioH!! + measures.padding.times(2) * ratioH!! * details.level,
                end = measures.padding * ratioH!!
            )
            .background(
                color = colorScheme.surfaceContainer
            )
    ) {
        if (display != Settings.Display.MAP && details.drawable != null)
            Image(
                painterResource(details.drawable),
                "item icon",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(
                        width = measures.itemHeight * ratioH!!,
                        height = measures.itemHeight * ratioV!!
                    )
            )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp * ratioH!!,),
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
                    fontSize = typography.bodyLarge.fontSize * ratioV!!,
                    lineHeight = 1.em * fontScale,
                    maxLines = if (display == Settings.Display.MAP) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )
                if (details.origin == null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorScheme.secondary),
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    screen.render.collapse(index)
                                }
                            )
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
                    fontSize = typography.bodyMedium.fontSize * ratioV!!,
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
                        if (display == Settings.Display.MAP || more != true) 2
                        else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                )
        }
    }
}
