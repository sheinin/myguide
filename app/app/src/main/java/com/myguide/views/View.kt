package com.myguide.views


import com.myguide.R
import com.myguide.Screen
import com.myguide.UI.MARGIN
import com.myguide.batch
import com.myguide.colorScheme
import com.myguide.data.Query.*
import com.myguide.data.VM.Type.*
import com.myguide.toDp
import com.myguide.toolbar
import com.myguide.typography
import android.view.ViewTreeObserver
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun View(screen: Screen) {
    val vm = screen.vm
    val description by vm.description.observeAsState()
    val details by vm.cycler.details.collectAsStateWithLifecycle()
    val display by vm.type.observeAsState()
    val expand by vm.cycler.description.collectAsStateWithLifecycle()
    val filter by vm.filter.observeAsState()
    val h = vm.h.observeAsState()
    val margin by vm.margin.collectAsStateWithLifecycle()
    val ratio by vm.ratio.observeAsState()
    val ratioH by vm.ratioH.observeAsState()
    val ratioV by vm.ratioV.observeAsState()
    val scale by vm.scale.observeAsState()
    val scrollStateY = rememberScrollState()
    val sort by vm.sort.observeAsState()
    val stateX by vm.stateX.observeAsState()
    val stateY by vm.scrollY.observeAsState()
    val toggle by vm.cycler.toggle.collectAsStateWithLifecycle()
    val view = LocalView.current
    val viewItem by vm.details.observeAsState()
    val w = vm.w.observeAsState()
    val xy by vm.cycler.xy.collectAsStateWithLifecycle()
    var heightView by remember { mutableIntStateOf(0) }
    var heightInfo by remember { mutableIntStateOf(0) }
    Box(
        Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "logo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
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
                if (display == H)
                    Control(
                        control = toolbar.items.last().query == ITEM || toolbar.items.last().query == SHOPS,
                        filter = filter,
                        type = display,
                        ratioH = ratioH ?: ratio!!,
                        ratioV = ratioV ?: ratio!!,
                        sort = sort,
                        title = toolbar.items.last().query.title
                    )
            }
            LaunchedEffect(stateY!!) {
                scrollStateY.scrollTo(stateY!!)
            }
            DisposableEffect(view, display) {
                if (display == H) return@DisposableEffect onDispose {}
                val listener = ViewTreeObserver.OnScrollChangedListener {
                    screen.scroll = scrollStateY.value - heightInfo + heightView / 3
                }
                val vto = view.viewTreeObserver
                vto.addOnScrollChangedListener(listener)
                onDispose {
                    vto.removeOnScrollChangedListener(listener)
                }
            }
            Column(
                verticalArrangement = if (display == H) Arrangement.Bottom else Arrangement.Top,
                modifier = Modifier
                    .onSizeChanged { heightView = it.height }
                    .fillMaxWidth()
                    .verticalScroll(scrollStateY)
                    .weight(1f)
                    .then(
                        if (display == H)
                            Modifier
                                .height(h.value!!.toDp())
                                .background(Color.Transparent)
                        else
                            Modifier
                                .fillMaxHeight()
                                .background(colorScheme.surface)
                    )
            ) {
                if (display != H && viewItem != null)
                    Column(
                        Modifier
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
                        Text(description!!,
                            style = typography.bodySmall,
                            lineHeight = 1.em * scale!!,
                            color = colorScheme.secondary,
                            fontSize = typography.bodySmall.fontSize * (ratioV ?: ratio!!),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                if (display != H)
                    Control(
                        control =
                            toolbar.items.last().query == ITEM ||
                            toolbar.items.last().query == SHOPS,
                        type = display,
                        filter = filter,
                        ratioH = ratioH ?: ratio!!,
                        ratioV = ratioV ?: ratio!!,
                        sort = sort,
                        title = toolbar.items.last().query.title
                    )
                val scrollStateX = rememberScrollState()
                val view = LocalView.current
                LaunchedEffect(stateX) { scrollStateX.scrollBy(stateX!!) }
                DisposableEffect(view, display) {
                    if (display != H) return@DisposableEffect onDispose {}
                    val listener = ViewTreeObserver.OnScrollChangedListener {
                        screen.scroll = scrollStateX.value
                    }
                    val vto = view.viewTreeObserver
                    vto.addOnScrollChangedListener(listener)
                    onDispose {
                        vto.removeOnScrollChangedListener(listener)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollStateX)
                ) {
                    Box(
                        modifier = Modifier
                            .size(
                                width = w.value!!.toDp(),
                                height = h.value!!.toDp() * (ratioV ?: ratio!!) * scale!!
                            )
                    ) {
                        repeat(batch) {
                            ViewItem(
                                details = details[it],
                                type = display,
                                expand = expand[it],
                                margin = margin,
                                ratioH = ratioH ?: ratio!!,
                                ratioV = ratioV ?: ratio!!,
                                scale = scale!!,
                                toggle = toggle[it],
                                xy = xy[it]!!,
                                modifier = Modifier.clickable(
                                    onClick = {
                                        toolbar.items.last().scroll =
                                            if (display == V) scrollStateX.value
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


