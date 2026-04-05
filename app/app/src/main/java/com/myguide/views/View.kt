package com.myguide.views


import android.view.ViewTreeObserver
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberScrollable2DState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable2D
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myguide.Screen
import com.myguide.UI.ITEM_HEIGHT
import com.myguide.UI.MAP_WIDTH
import com.myguide.UI.MARGIN
import com.myguide.batch
import com.myguide.colorScheme
import com.myguide.data.VM.Type.*
import com.myguide.qqq
import com.myguide.toDp
import com.myguide.toPx
import com.myguide.toolbar
import com.myguide.typography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun View(modifier: Modifier, screen: Screen) {
    val vm = screen.vm
    val bitmap by vm.bitmap.collectAsStateWithLifecycle()
    val description by vm.description.observeAsState()
    val details by vm.cycler.details.collectAsStateWithLifecycle()
    val dim by vm.dim.observeAsState()
    val exp by vm.exp.observeAsState()
    val expand by vm.cycler.description.collectAsStateWithLifecycle()
    val h = vm.h.observeAsState()
    val margin by vm.margin.collectAsStateWithLifecycle()
    val ratio by vm.ratio.observeAsState()
    val ratioH by vm.ratioH.observeAsState()
    val ratioV by vm.ratioV.observeAsState()
    val scale by vm.scale.observeAsState()
    val scrollStateX = rememberScrollState()
    val scrollStateY = rememberScrollState()
    val stateX by vm.stateX.observeAsState()
    val stateY by vm.scrollY.observeAsState()
    val toggle by vm.cycler.toggle.collectAsStateWithLifecycle()
    val type by vm.type.observeAsState()
    val view = LocalView.current
    val viewItem by vm.details.observeAsState()
    val w = vm.w.observeAsState()
    val xy by vm.cycler.xy.collectAsStateWithLifecycle()
    var heightInfo by remember { mutableIntStateOf(0) }
    var heightView by remember { mutableIntStateOf(0) }
    val pan = object {
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        val minOffsetX = ((ITEM_HEIGHT + MARGIN * 2) * dim!!.first.first).toDp().toPx()
        val maxOffsetX = ((ITEM_HEIGHT + MARGIN * 2) * dim!!.first.second).toDp().toPx()
        val minOffsetY = ((ITEM_HEIGHT + MARGIN * 2) * dim!!.second.first).toDp().toPx()
        val maxOffsetY = ((ITEM_HEIGHT + MARGIN * 2) * dim!!.second.second).toDp().toPx()

        val scrollState = rememberScrollable2DState { delta ->
            val newX = (offset.x + delta.x).coerceIn(minOffsetX, maxOffsetX)
            val newY = (offset.y + delta.y).coerceIn(minOffsetY, maxOffsetY)
            offset = Offset(newX, newY)
            this.offsetX = offset.x
            this.offsetY = offset.y
            delta
        }
    }
    Box(
        modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.background)
            ) {
                if (toolbar.crumbs[screen.ident]!!.value!![0].isNotEmpty())
                    Row(Modifier.padding(8.dp, 4.dp)) {
                        repeat(3) {
                            ArrowText(
                                toolbar.crumbs[screen.ident]!!.value!![it],
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(
                                        if (toolbar.crumbs[screen.ident]!!.value!![it].isNotEmpty()) 1f
                                        else 0f
                                    )
                                    .clickable(
                                        onClick = {
                                            toolbar.click(it)
                                        }
                                    )
                            )
                        }
                    }
            }
            LaunchedEffect(stateY!!) {
                scrollStateY.scrollTo(stateY!!)
            }
            DisposableEffect(view, type, exp) {
                if (exp!! || type == H) return@DisposableEffect onDispose {}
                val listener = ViewTreeObserver.OnScrollChangedListener {
                    screen.scrollY = scrollStateY.value - heightInfo + heightView / 3
                }
                val vto = view.viewTreeObserver
                vto.addOnScrollChangedListener(listener)
                onDispose {
                    vto.removeOnScrollChangedListener(listener)
                }
            }
            Column(
                verticalArrangement = if (type == H) Arrangement.Center else Arrangement.Top,
                modifier = Modifier
                    .onSizeChanged { heightView = it.height }
                    .then(
                        if (type == H)
                            Modifier.fillMaxSize()
                        else
                            Modifier.fillMaxWidth()
                    )
                    .then(
                        if (!exp!! && (type == V || type == T))
                            Modifier.verticalScroll(scrollStateY) else Modifier
                    )
                    .weight(1f)
            ) {
                if (type != H && viewItem != null)
                    Column(
                        Modifier
                            .zIndex(3f)
                            .padding(8.dp)
                            .onGloballyPositioned(
                                onGloballyPositioned = {
                                    heightInfo = it.size.height
                                }
                            )
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = MARGIN.toDp() * margin * (ratioH ?: ratio!!),
                                    vertical = MARGIN.toDp() * margin * (ratioV ?: ratio!!)
                                )
                        ) {
                            Column {
                                Text(
                                    viewItem!!.title,
                                    style = typography.bodyLarge,
                                    color = colorScheme.secondary,
                                    lineHeight = 1.em * scale!!,
                                    fontSize = typography.bodyLarge.fontSize * (ratioV ?: ratio!!),
                                )
                                Text(
                                    viewItem!!.origin!!,
                                    fontStyle = FontStyle.Italic,
                                    style = typography.bodyMedium,
                                    color = colorScheme.secondary,
                                    lineHeight = 1.em * scale!!,
                                    fontSize = typography.bodyMedium.fontSize * (ratioV ?: ratio!!),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Image(
                                painterResource(viewItem!!.drawable!!),
                                "item icon",
                                modifier = Modifier
                                    .size(
                                        60.dp * (ratioH ?: ratio!!),
                                        60.dp * (ratioV ?: ratio!!)
                                    )
                            )
                        }
                        Text(
                            description!!,
                            style = typography.bodySmall,
                            lineHeight = 1.em * scale!!,
                            color = colorScheme.secondary,
                            fontSize = typography.bodySmall.fontSize * (ratioV ?: ratio!!),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                LaunchedEffect(stateX) { scrollStateX.scrollBy(stateX!!) }
                DisposableEffect(view, type, exp) {
                    if (exp!! || type != H) return@DisposableEffect onDispose {}
                    val listener = ViewTreeObserver.OnScrollChangedListener {
                        screen.scrollX = scrollStateX.value
                    }
                    val vto = view.viewTreeObserver
                    vto.addOnScrollChangedListener(listener)
                    onDispose {
                        vto.removeOnScrollChangedListener(listener)
                    }
                }
                Row(
                    modifier = Modifier
                    //    .zIndex(2f)
                        .fillMaxWidth()
                      //  .fillMaxHeight()
                        .then(
                            if (!exp!! && type == H) Modifier.horizontalScroll(scrollStateX)
                            else Modifier
                        )
                ) {
                    Box(
                        //contentAlignment =
                        //    if (exp!!) Alignment.Center
                        //else Alignment.TopStart,
                        modifier = Modifier
                            .zIndex(1f)
                            .then(
                                if (exp!!) Modifier.fillMaxSize()
                                else Modifier.size(
                                    width = w.value!!.toDp(),
                                    height = h.value!!.toDp() * (ratioV ?: ratio!!) * scale!!
                                )
                            )
                    ) {
                        if (exp!!) {
                            Canvas(modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Green.copy(alpha = .1f))
                            ) {
                                bitmap?.let {
                                    drawImage(image = it.asImageBitmap())
                                }
                            }

                        } else
                            repeat(batch) {
                                ViewItem(
                                    details = details[it],
                                    type = type,
                                    expand = expand[it],
                                    margin = margin,
                                    ratioH = ratioH ?: ratio!!,
                                    ratioV = ratioV ?: ratio!!,
                                    scale = scale!!,
                                    toggle = toggle[it],
                                    xy = xy[it]!!,
                                    modifier = Modifier.clickable(
                                        enabled = true,
                                        onClick = {
                                            toolbar.items.last().scroll =
                                                if (type == V) scrollStateX.value
                                                else scrollStateY.value - heightInfo
                                            toolbar.items.last().toggle =
                                                screen.mx.view.toggle
                                            toolbar.navigate(
                                                id = screen.list[screen.mx.point[xy[it]!!.i]].id,
                                                title = details[it].title,
                                            )
                                        }
                                    )
                                )
                            }
                        if (type == D || exp!!)
                            Surface(
                                modifier = Modifier
                                    .zIndex(2f)
                                    .fillMaxSize()
                                    .scrollable2D(
                                        state = pan.scrollState,
                                        enabled = true
                                    ),
                                color = Color.Transparent
                            ) {
                                screen.scrollY = pan.offsetY.toInt().unaryMinus()
                                screen.scrollX = pan.offsetX.toInt().unaryMinus()
                                Canvas(modifier = Modifier) {
                                    val step = MAP_WIDTH.toPx() / 18
                                    val width = size.width
                                    val height = size.height
                                    var x = (pan.offsetX % step) - step
                                    while (x < width) {
                                        drawLine(
                                            color = Color.LightGray,
                                            start = Offset(x, 0f),
                                            end = Offset(x, height),
                                            strokeWidth = .1.dp.toPx()
                                        )
                                        x += step
                                    }
                                    var y = (pan.offsetY % step) - step
                                    while (y < height) {
                                        drawLine(
                                            color = Color.LightGray,
                                            start = Offset(0f, y),
                                            end = Offset(width, y),
                                            strokeWidth = .1.dp.toPx()
                                        )
                                        y += step
                                    }
                                    val origin = Offset(
                                        x = width / 2f - pan.offsetX,
                                        y = height / 2f - pan.offsetY
                                    )
                                    drawCircle(
                                        color = Color.Red,
                                        radius = 8.dp.toPx(),
                                        center = origin
                                    )
                                }
                            }
                    }
                }
            }
        }

    }
}


@Composable
fun ArrowText(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF2196F3),
    textColor: Color = Color.White
) {
    Row(
        modifier = modifier
            .wrapContentSize()
            .padding(4.dp, 0.dp)
            .fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
