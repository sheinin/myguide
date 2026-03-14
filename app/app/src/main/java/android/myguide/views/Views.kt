package android.myguide.views

import android.myguide.Screen
import android.myguide.batch
import android.myguide.colorScheme
import android.myguide.density
import android.myguide.fontScale
import android.myguide.lock
import android.myguide.measures
import android.myguide.model.VM
import android.myguide.model.VM.Display.*
import android.myguide.toDp
import android.myguide.toPx
import android.myguide.typography
import android.myguide.vmm
import android.view.ViewTreeObserver
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(
    screen: Screen
) {
    val bind = screen.vm
    val display by bind.display.observeAsState()
    val stateY by bind.stateY.observeAsState()
    Box(
        Modifier
            .fillMaxSize()
            .onPlaced {
                screen.query()
            }
    ) {
        val singapore = LatLng(1.35, 103.86)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(singapore, 10f)
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        )
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val (toolbar, scroll) = createRefs()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.background)
                    .constrainAs(toolbar) {
                        if (display == MAP) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        } else {
                            top.linkTo(parent.top)
                            bottom.linkTo(scroll.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        height = Dimension.wrapContent
                    }
            ) {
                if (vmm.toolbar.crumbs[screen.ident]!!.value!![0].isNotEmpty())
                    Row(Modifier.padding(8.dp, 4.dp)) {
                        repeat(3) {
                            ArrowText(
                                vmm.toolbar.crumbs[screen.ident]!!.value!![it],
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(
                                        if (vmm.toolbar.crumbs[screen.ident]!!.value!![it].isNotEmpty()) 1f
                                        else 0f
                                    )
                                    .clickable(
                                        onClick = {
                                            vmm.toolbar.click(it)
                                        }
                                    )
                            )
                        }
                    }
                if (display == MAP) Control(screen)
            }
            val scrollStateY = rememberScrollState()
            val view = LocalView.current
            LaunchedEffect(bind.position.value) {
                if (display != MAP)
                    scrollStateY.scrollTo(stateY!!)
            }
            DisposableEffect(view, display) {
                if (display == MAP) return@DisposableEffect onDispose {}
                val listener = ViewTreeObserver.OnScrollChangedListener {
                    with(density) {
                        screen.render.observe(scrollStateY.value.toDp())
                    }
                }
                val vto = view.viewTreeObserver
                vto.addOnScrollChangedListener(listener)
                onDispose {
                    vto.removeOnScrollChangedListener(listener)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollStateY)
                    .background(
                        if (display == MAP) Color.Transparent
                        else colorScheme.surface
                    )
                    .constrainAs(scroll) {
                        if (display == MAP) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.wrapContent
                        } else {
                            bottom.linkTo(parent.bottom)
                            top.linkTo(toolbar.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.fillToConstraints
                        }

                    }
            ) {
                val description by bind.description.observeAsState()
                val ratio by vmm.ratio.observeAsState()
                val ratioH by vmm.ratioH.observeAsState()
                val ratioV by vmm.ratioV.observeAsState()
                val viewItem by bind.details.observeAsState()
                if (display != MAP && viewItem != null)
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = measures.padding * (ratioH ?: ratio!!),
                                    vertical = measures.padding * (ratioV ?: ratio!!)
                                )
                        ) {
                            Column {
                                Text(
                                    viewItem!!.title,
                                    style = typography.bodyLarge,
                                    color = colorScheme.secondary,
                                    lineHeight = 1.em * fontScale,
                                    fontSize = typography.bodyLarge.fontSize * (ratioV ?: ratio!!)
                                )
                                Text(
                                    viewItem!!.origin!!,
                                    fontStyle = FontStyle.Italic,
                                    style = typography.bodyMedium,
                                    color = colorScheme.secondary,
                                    lineHeight = 1.em * fontScale,
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
                            lineHeight = 1.em * fontScale,
                            color = colorScheme.secondary,
                            fontSize = typography.bodySmall.fontSize * (ratioV ?: ratio!!),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                if (display != MAP)
                    Control(screen)
                val scrollStateX = rememberScrollState()
                val view = LocalView.current
                LaunchedEffect(bind.position.value) {
                 //   scrollStateX.scrollTo(
                   //  ///////////////////   bind.position.value!!.toPx().toInt()
                    //)
                }
                DisposableEffect(view, display) {
                    if (display == LIST) return@DisposableEffect onDispose {}
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
                        .padding(
                            bottom = if (display?.isMap == true) measures.padding * 4 else 0.dp
                        )
                ) {
                    val h = bind.h.observeAsState()
                    val w = bind.w.observeAsState()
                    val details by bind.cycler.details.collectAsStateWithLifecycle()
                    val display by bind.display.observeAsState()
                    val expand by bind.cycler.description.collectAsStateWithLifecycle()
                    val toggle by bind.cycler.toggle.collectAsStateWithLifecycle()
                    val xy by bind.cycler.xy.collectAsStateWithLifecycle()
                    Box(
                        modifier = Modifier
                            .size(w.value!!, h.value!!)
                    ) {
                        fun callback(index: Int) {
                            if (lock) return
                            lock = true
                            vmm.toolbar.items.last().position =
                                if (display == MAP) scrollStateX.value.toDp()
                                else scrollStateY.value.toDp()
                            vmm.toolbar.navigate(
                                id = details[index].id,
                                title = details[index].title,
                                queryType = screen.queryType!!.next
                            )
                        }
                        repeat(batch) {
                            ViewItem(
                                index = it,
                                screen = screen,
                                details = details[it],
                                display = display,
                                expand = expand[it],
                                toggle = toggle[it],
                                xy = xy[it],
                                callback = ::callback
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


@Composable
fun MyDialog() {
    Dialog(
        onDismissRequest = { vmm.dialog.postValue(false) },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        (LocalView.current.parent as DialogWindowProvider).window.setDimAmount(0f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { vmm.dialog.value = false }
                )
                .padding(top = 86.dp, start = 8.dp, end = 8.dp), // Adjust top padding as needed
            contentAlignment = Alignment.TopCenter // Aligns content to the top center
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = colorScheme.surface,
                modifier = Modifier.padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    val list = vmm.toolbar.items.subList(1, vmm.toolbar.items.lastIndex.dec())
                    items(list.size) {
                        Text(
                            text = list[it].title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vmm.toolbar.goto(it.inc())
                                    vmm.dialog.value = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

