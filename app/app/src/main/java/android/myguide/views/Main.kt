package android.myguide.views

import android.myguide.QueryType
import android.myguide.R
import android.myguide.Screen
import android.myguide.UI.MARGIN
import android.myguide.batch
import android.myguide.colorScheme
import android.myguide.density
import android.myguide.data.VM.Display.*
import android.myguide.qqq
import android.myguide.toolbar
import android.myguide.typography
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(screen: Screen) {
    val bind = screen.vm
    val description by bind.description.observeAsState()
    val details by bind.cycler.details.collectAsStateWithLifecycle()
    val display by bind.display.observeAsState()
    val expand by bind.cycler.description.collectAsStateWithLifecycle()
    val filter by screen.vm.filter.observeAsState()
    val h = bind.h.observeAsState()
    val margin by bind.margin.collectAsStateWithLifecycle()
    val ratio by screen.vm.ratio.observeAsState()
    val ratioH by screen.vm.ratioH.observeAsState()
    val ratioV by screen.vm.ratioV.observeAsState()
    val scale by screen.vm.scale.observeAsState()
    val scrollStateY = rememberScrollState()
    val sort by screen.vm.sort.observeAsState()
    val stateX by bind.stateX.observeAsState()
    val stateY by bind.stateY.observeAsState()
    val toggle by bind.cycler.toggle.collectAsStateWithLifecycle()
    val view = LocalView.current
    val viewItem by bind.details.observeAsState()
    val w = bind.w.observeAsState()
    val xy by bind.cycler.xy.collectAsStateWithLifecycle()
    var viewItemHeight by remember { mutableIntStateOf(0) }
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
                        control = screen.queryType == QueryType.ITEM || screen.queryType == QueryType.SHOPS,
                        filter = filter,
                        display = display,
                        ratioH = ratioH ?: ratio!!,
                        ratioV = ratioV ?: ratio!!,
                        sort = sort,
                        title = screen.queryType!!.title
                    )
            }
            LaunchedEffect(stateY!!) {
                qqq("SCRO1 "+stateY!!)
                //if (display != H)
                if (stateY != -1f)
                    scrollStateY.scrollTo(stateY!!.toInt())
                bind.stateY.value = -1f
            }
            DisposableEffect(view, display) {
                if (display == H) return@DisposableEffect onDispose {}
                val listener = ViewTreeObserver.OnScrollChangedListener {
                    with(density) {
                        screen.render.observe((scrollStateY.value - viewItemHeight).toDp())
                    }
                }
                val vto = view.viewTreeObserver
                vto.addOnScrollChangedListener(listener)
                onDispose {
                    vto.removeOnScrollChangedListener(listener)
                }
            }
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollStateY)
                    .weight(1f)
                    .then(
                        if (display == H)
                            Modifier
                                .height(h.value!!)
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
                                    viewItemHeight = it.size.height
                                }
                            )
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = MARGIN * margin * (ratioH ?: ratio!!),
                                    vertical = MARGIN * margin * (ratioV ?: ratio!!)
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
                        control = screen.queryType == QueryType.ITEM || screen.queryType == QueryType.SHOPS,
                        display = display,
                        filter = filter,
                        ratioH = ratioH ?: ratio!!,
                        ratioV = ratioV ?: ratio!!,
                        sort = sort,
                        title = screen.queryType!!.title
                    )
                val scrollStateX = rememberScrollState()
                val view = LocalView.current
                LaunchedEffect(stateX) { scrollStateX.scrollBy(stateX!!) }
                DisposableEffect(view, display) {
                    if (display != H) return@DisposableEffect onDispose {}
                    val listener = ViewTreeObserver.OnScrollChangedListener {
                        with(density) {
                            screen.render.observe(scrollStateX.value.toDp())
                        }
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
                   //     .padding(
                     //       bottom = if (display == H) margin * 4 else 0.dp
                       // )
                ) {
                    Box(
                        modifier = Modifier
                            .size(
                                width = w.value!!,
                                height = h.value!! * (ratioV ?: ratio!!) * scale!!
                            )
                            .alpha(
                                if (bind.loading.value == true) 0f
                                else 1f
                            )
                    ) {
                        repeat(batch) {
                            ViewItem(
                                details = details[it],
                                display = display,
                                expand = expand[it],
                                margin = margin,
                                ratioH = ratioH ?: ratio!!,
                                ratioV = ratioV ?: ratio!!,
                                scale = scale!!,
                                toggle = toggle[it],
                                xy = xy[it]
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


