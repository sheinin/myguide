package android.myguide

import android.R.attr.text
import android.view.ViewTreeObserver
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun Main(
    ident: Boolean,
    modifier: Modifier = Modifier,
    screen: Screen
) {
    val bind = vm.screen[ident]!!
    val dialog by vm.screen[screen.ident]!!.dialog.observeAsState()
    val display by vm.screen[screen.ident]!!.display.observeAsState()
    Box(
        modifier
            .fillMaxSize().onPlaced {
                vm.toolbar.pending()
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
                        if (display == Settings.Display.MAP) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        painterResource(R.drawable.home),
                        contentDescription = "Home",
                        colorFilter = ColorFilter.tint(colorScheme.background),
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    vm.showSplash.value = true
                                    vm.toolbar.clear()
                                }
                            )
                            .background(colorScheme.secondary, shape = CircleShape)
                            .padding(5.dp)
                    )
                    Text(
                        vm.toolbar.title[screen.ident]!!.value!!,
                        style = typography.titleLarge,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp, 0.dp)
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    Image(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(colorScheme.background),
                        modifier = Modifier.clickable(
                                onClick = { vm.toolbar.back() }
                            )
                            .background(colorScheme.secondary, shape = CircleShape)
                            .border(
                                width = 1.dp,
                                color = colorScheme.tertiary,
                                shape = CircleShape
                            )
                            .padding(5.dp)
                    )
                }
                if (vm.toolbar.crumbs[screen.ident]!!.value!![0].isNotEmpty())
                    Row(Modifier.padding(8.dp, 4.dp)) {
                        repeat(3) {
                            ArrowText(
                                vm.toolbar.crumbs[screen.ident]!!.value!![it],
                                modifier = Modifier.weight(1f)
                                    .alpha(
                                        if (vm.toolbar.crumbs[screen.ident]!!.value!![it].isNotEmpty()) 1f
                                        else 0f
                                    )
                                    .clickable(
                                        onClick = {
                                            vm.toolbar.click(it)
                                        }
                                    )
                            )
                        }
                    }
                if (display == Settings.Display.MAP) Control(screen)
            }
            val scrollStateY = rememberScrollState()
            val view = LocalView.current
            LaunchedEffect(vm.screen[screen.ident]!!.position.value) {
                if (display != Settings.Display.MAP)
                    scrollStateY.animateScrollTo(
                        vm.screen[screen.ident]!!.position.value!!.toPx().toInt()
                    )
            }
            DisposableEffect(view, display) {
                if (display == Settings.Display.MAP) return@DisposableEffect onDispose {}
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
                        if (display == Settings.Display.MAP) Color.Transparent
                        else colorScheme.surface
                    )
                    .constrainAs(scroll) {
                        if (display == Settings.Display.MAP) {
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
                val viewItem by bind.item.observeAsState()
                if (display != Settings.Display.MAP && viewItem != null)
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            viewItem!!.title,
                            style = typography.bodyLarge,
                            color = colorScheme.secondary
                        )
                        Text(
                            viewItem!!.origin!!,
                            fontStyle = FontStyle.Italic,
                            style = typography.bodyMedium,
                            color = colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        val lines = 3
                        val textMeasurer = rememberTextMeasurer()
                        val c = Constraints(maxWidth = (screenWidth - 58.dp - 24.dp).toPx().toInt())
                        val measurements = remember(viewItem!!.description!!) {
                            textMeasurer.measure(
                                text = viewItem!!.description!!,
                                style = typography.bodySmall,
                                constraints = c
                            )
                        }
                        qqq("LC "+measurements.lineCount)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Image(
                                painterResource(viewItem!!.drawable!!),
                                "item icon",
                                modifier = Modifier
                                    .size(58.dp, 62.dp)
                                    .padding(bottom = 4.dp)
                                    .background(color = colorScheme.surface)
                            )
                            Text(
                                if (measurements.lineCount <= lines) viewItem!!.description!!
                                else viewItem!!.description!!.take(measurements.getLineEnd(lines)),
                                style = typography.bodySmall,
                                color = colorScheme.secondary,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        if (measurements.lineCount > lines)
                        Text(
                            viewItem!!.description!!.substring(measurements.getLineEnd(lines)),
                            style = typography.bodySmall,
                            color = colorScheme.secondary
                        )

                    }


                if (display != Settings.Display.MAP)
                    Control(screen)
                val scrollStateX = rememberScrollState()
                val view = LocalView.current
                LaunchedEffect(vm.screen[screen.ident]!!.position.value) {
                    if (display == Settings.Display.MAP)
                        scrollStateX.animateScrollTo(
                            vm.screen[screen.ident]!!.position.value!!.toPx().toInt()
                        )
                }
                DisposableEffect(view, display) {
                    if (display == Settings.Display.LIST) return@DisposableEffect onDispose {}
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
                ) {
                    val h = bind.h.observeAsState()
                    val w = bind.w.observeAsState()
                    Box(
                        modifier = Modifier
                            .size(w.value!!, h.value!!)
                    ) {
                        val items by bind.cycler.items.collectAsStateWithLifecycle()
                        val expandable by bind.cycler.expandable.collectAsStateWithLifecycle()
                        repeat(batch) { index ->
                            val item = items[index]
                            if (
                                item.title.isNotEmpty() //&&
                           //     expandable[index]?.isNotEmpty() == true
                            )
                                RenderItem(
                                    index = index,
                                    screen = screen,
                                    viewItem = item,
                                    x = scrollStateX,
                                    y = scrollStateY
                                )
                        }
                    }
                }

            }
        }
        if (dialog == true) MyDialog(screen)
    }
}

@Composable
fun RenderItem(
    index: Int,
    screen: Screen,
    viewItem: ViewItem,
    x: ScrollState,
    y: ScrollState
) {
    val expandable by vm.screen[screen.ident]!!.cycler.expandable.collectAsStateWithLifecycle()
    val more by vm.screen[screen.ident]!!.cycler.more.collectAsStateWithLifecycle()
    val xy by vm.screen[screen.ident]!!.cycler.xy.collectAsStateWithLifecycle()
    val display by vm.screen[screen.ident]!!.display.observeAsState()
  //  qqq("TITLE " + " "+ xy[index].y+  " " +viewItem?.title)
    Row(
        modifier = Modifier
            .offset(xy[index].x, xy[index].y)
            .size(xy[index].w, xy[index].h)
            .clickable(
                onClick = {
                    if (viewItem.description == null) return@clickable
                    vm.toolbar.items.last().position =
                        if (display == Settings.Display.MAP) x.value.toDp()
                        else y.value.toDp()
                    vm.toolbar.navigate(
                        id = viewItem.id,
                        viewItem = viewItem,
                        title = viewItem.title,
                        queryType =
                            if (
                                screen.queryType == QueryType.SHOP
                                || screen.queryType == QueryType.ITEMS
                            ) QueryType.ITEM
                            else QueryType.SHOP
                    )
                }
            )
            .padding(
                8.dp + 16.dp * viewItem.level,
                0.dp,
                8.dp,
                0.dp
            )
            .background(
                color = colorScheme.surfaceContainer
            )
            .padding(if (viewItem.description == null) 0.dp else 2.dp)
    ) {
        if (display != Settings.Display.MAP && viewItem.drawable != null)
            Image(
                painterResource(viewItem.drawable),
                "item icon",
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        color = colorScheme.surface
                    )
            )
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth()
        ) {
            Row {
                Text(
                    viewItem.title,
                    color = colorScheme.secondary,
                    style = typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (viewItem.description == null) {
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
            if (viewItem.origin != null)
                Text(
                    viewItem.origin,
                    color = colorScheme.secondary,
                    style = typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            if (viewItem.description != null) {
                val display by vm.screen[screen.ident]!!.display.observeAsState()
                val txt by
                remember(more[index], viewItem.description) {
                    mutableStateOf(
                        buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = colorScheme.secondary,
                                    fontSize = typography.bodySmall.fontSize,
                                )
                            ) { append(viewItem.description) }
                            if (more[index] == true && display != Settings.Display.MAP) {
                                withStyle(
                                    style = SpanStyle(
                                        color = Color.Transparent,
                                        textDecoration = TextDecoration.None,
                                        fontSize = typography.bodySmall.fontSize,
                                    )
                                ) { append(".") }
                                withLink(
                                    LinkAnnotation.Clickable(
                                        tag = "lastThree",
                                        linkInteractionListener = {
                                            screen.render.ellipsis(screen.render.data.stack[index])
                                        }
                                    )
                                ) {
                                    withStyle(
                                        style = SpanStyle(
                                            textDecoration = TextDecoration.None,
                                            color = colorScheme.primary,
                                            fontSize = typography.bodySmall.fontSize
                                        )
                                    ) { append("...") }
                                }
                            }
                        }
                    )
                }
                var desc by remember(
                    more[index],
                    viewItem.description,
                    display,
                    expandable[index]
                ) {
                    mutableStateOf(
                        if (display == Settings.Display.MAP || more[index] == true) txt
                        else (expandable[index] ?: txt)
                    )
                }
                //qqq("D "+index+more[index]+item.title+ "--"+expandable[index])
                Text(
                    desc,
                    onTextLayout = { textLayoutResult ->
                        // The width is available in pixels (px), convert to Dp for use in modifiers
                        val widthInPixels = textLayoutResult.size.width
                        // qqq("W " +with(density) { widthInPixels.toDp() })
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = typography.bodySmall,
                    maxLines =
                        if (display == Settings.Display.MAP || expandable[index] == null) 2
                        else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun Splash(modifier: Modifier) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                //.weight(1f)
                .clickable(
                    onClick = {
                        vm.showSplash.value = false
                        vm.toolbar.navigate(
                            queryType = QueryType.SHOPS,
                            title = "All Shops"
                        )
                    }
                )
        ) {
            Image(painter = painterResource(R.drawable.all_shops), "all shops",
                modifier = Modifier.size(screenWidth * .5f))
            Text(
                "SHOPS",
                fontWeight = FontWeight.Bold,
                style = typography.displayMedium,)
        }
        Spacer(Modifier.height(16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                //.weight(1f)
                .clickable(
                    onClick = {
                        vm.showSplash.value = false
                        vm.toolbar.navigate(
                            queryType = QueryType.ITEMS,
                            title = "All Items"
                        )
                    }
                )
        ) {
            Image(
                painter = painterResource(R.drawable.all_items), "all items",
                modifier = Modifier.size(screenWidth * .5f)
            )
            Text(
                "ITEMS",
                fontWeight = FontWeight.Bold,
                style = typography.displayMedium
            )
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
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
fun Control(screen: Screen) {
    val display by vm.screen[screen.ident]!!.display.observeAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .background(colorScheme.background)
            .padding(8.dp)
    ) {
        Text(
            screen.queryType?.title ?: "",
            style = typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        if (screen.queryType == QueryType.ITEM || screen.queryType == QueryType.SHOPS)
            Image(
                painter = painterResource(R.drawable.list),
                contentDescription = "list",
                colorFilter = ColorFilter.tint(colorScheme.background),
                modifier = Modifier.clickable(
                    onClick = {
                        //    isMap = true
                        vm.screen[screen.ident]!!.display.value = Settings.Display.LIST
                        screen.render.display(Settings.Display.LIST)
                    }
                )
                    .padding(8.dp, 0.dp)
                    .background(
                        if (display != Settings.Display.MAP) colorScheme.primary
                        else colorScheme.secondary,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = colorScheme.tertiary,
                        shape = CircleShape
                    )
                    .padding(5.dp)
            )
        if (screen.queryType == QueryType.ITEM || screen.queryType == QueryType.SHOPS)
            Image(
                painterResource(R.drawable.map),
                "map",
                colorFilter = ColorFilter.tint(colorScheme.background),
                modifier = Modifier.clickable(
                        onClick = {
                            vm.screen[screen.ident]!!.display.value = Settings.Display.MAP
                            screen.render.display(Settings.Display.MAP)
                        }
                    )
                    .background(
                        if (display == Settings.Display.MAP) colorScheme.primary
                        else colorScheme.secondary,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = colorScheme.tertiary,
                        shape = CircleShape
                    )
                    .padding(5.dp)
            )
    }
}


@Composable
fun MyDialog(screen: Screen) {
    Dialog(
        onDismissRequest = {
            qqq("dis")
            vm.screen[screen.ident]!!.dialog.postValue(false) },
        properties = DialogProperties(

            usePlatformDefaultWidth = false // Important for custom alignment/width control
        )
    ) {
        (LocalView.current.parent as DialogWindowProvider).window.setDimAmount(0f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = { vm.screen[screen.ident]!!.dialog.value = false }
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
                    val list = vm.toolbar.items.subList(1, vm.toolbar.items.lastIndex.dec())
                    items(list.size) {
                        Text(
                            text = list[it].title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    vm.toolbar.goto(it.inc())
                                    vm.screen[screen.ident]!!.dialog.value = false
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

