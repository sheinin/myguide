package android.myguide

import android.view.ViewTreeObserver
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
    // Create scroll states for both directions
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    var isMap by remember { mutableStateOf(false) }

    Box(
        modifier
            .fillMaxSize()
            .padding(8.dp)
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
            val (toolbar, ctrl, scroll) = createRefs()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .constrainAs(toolbar) {
                        top.linkTo(parent.top)
                        bottom.linkTo(ctrl.top)
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp, 4.dp)
                ) {
                    Image(
                        painterResource(R.drawable.home),
                        contentDescription = "Home",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background),
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    vm.showSplash.value = true
                                    vm.toolbar.clear()
                                }
                            )
                            .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = CircleShape
                            )
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
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.background),
                        modifier = Modifier.clickable(
                                onClick = { vm.toolbar.back() }
                            )
                            .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = CircleShape
                            )
                            .padding(5.dp)
                    )
                }
                if (vm.toolbar.crumbs[screen.ident]!!.value!![0].isNotEmpty())
                    Row(Modifier.padding(8.dp, 4.dp)) {
                        ArrowText(
                            vm.toolbar.crumbs[screen.ident]!!.value!![0],
                            modifier = Modifier.weight(1f)
                        )
                        ArrowText(
                            vm.toolbar.crumbs[screen.ident]!!.value!![1],
                            modifier = Modifier.weight(1f)
                                .alpha(if (vm.toolbar.crumbs[screen.ident]!!.value!![1].isNotEmpty()) 1f else 0f)
                        )
                        ArrowText(
                            vm.toolbar.crumbs[screen.ident]!!.value!![2],
                            modifier = Modifier.weight(1f)
                                .alpha(if (vm.toolbar.crumbs[screen.ident]!!.value!![2].isNotEmpty()) 1f else 0f)
                        )
                    }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp)
                    .constrainAs(ctrl) {
                        if (isMap) {
                            top.linkTo(toolbar.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        } else {
                            top.linkTo(toolbar.bottom)
                            bottom.linkTo(scroll.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        height = Dimension.wrapContent
                    }
            ) {
                Image(
                    painter = painterResource(R.drawable.list),
                    contentDescription = "list",
                    modifier = Modifier.clickable(
                            onClick = {
                                isMap = false
                                screen.render.display(Settings.Display.LIST)
                            }
                        )
                        .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = CircleShape
                        )
                        .padding(5.dp)
                )
                Icon(
                    painterResource(R.drawable.map),
                    "map",
                    modifier = Modifier.clickable(
                            onClick = {
                                isMap = true
                                screen.render.display(Settings.Display.MAP)
                            }
                        )
                        .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = CircleShape
                        )
                        .padding(5.dp)
                )
            }
            val scrollState = rememberScrollState()
            val view = LocalView.current // Get the current view
            val dens = LocalDensity.current
            // Add listener to ViewTreeObserver
            DisposableEffect(view) {
                val listener = ViewTreeObserver.OnScrollChangedListener {
                    // This triggers on scroll changes
                    with(dens) {
                        screen.render.observeY(scrollState.value.toDp())
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
                    .background(Color.Blue)
                    .verticalScroll(scrollState)
                    .padding(8.dp)
                    .constrainAs(scroll) {
                        if (isMap) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.wrapContent
                        } else {
                            bottom.linkTo(parent.bottom)
                            top.linkTo(ctrl.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.fillToConstraints
                        }

                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    val h = bind.h.observeAsState()
                    val w = bind.w.observeAsState()
                    Box(
                        modifier = Modifier
                            .size(w.value!!, h.value!!)
                            .padding(end = 8.dp)
                            .background(Color(0xFF6200EE))
                    ) {
                        val items by
                            if (
                                screen.queryType == QueryType.SHOP
                                || screen.queryType == QueryType.ITEMS
                            ) bind.cycler.items.collectAsStateWithLifecycle()
                            else bind.cycler.items.collectAsStateWithLifecycle()
                        repeat(batch) { index ->
                            val item = items[index]
                            if (item.title.isNotEmpty()) RenderItem(screen.queryType!!, item)
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun RenderItem(queryType: QueryType, item: ViewModel.Cycler.Item) {
    //qqq("TITLE " + " "+item.x+ " "+item.y +" "+item.w + " "+item.h+ " " +item?.title )
    Row(
        modifier = Modifier
            .offset(item.x, item.y)
            .size(item.w, item.h)
            .clickable(
                onClick = {
                  //  qqq("CL"+item.id+"--"+item.title)
                    vm.toolbar.navigate(
                        id = item.id,
                        title = item.title,
                        queryType =
                            if (queryType == QueryType.SHOP || queryType == QueryType.ITEMS) QueryType.ITEM
                            else QueryType.SHOP
                    )
                }
            )
    ) {
        Image(
            painterResource(R.drawable.ic_launcher_foreground), "",
            modifier = Modifier
                .size(60.dp)
        )
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth()
                .background(Color.Green)
        ) {
            Text(
                item.title,
                color = Color.Red,
                style = typography.bodyMedium,
            )
            Text(
                item.description ?: "",
                color = Color.Red,
                maxLines = 2,
                style = typography.bodyMedium
            )
        }
    }
}


@Composable
fun Splash(modifier: Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            "STORES",
            modifier = Modifier
                .weight(1f)
                .clickable(
                    onClick = {
                        vm.showSplash.value = false
                        vm.toolbar.navigate(
                            queryType = QueryType.SHOPS,
                            title = "Stores"
                        )
                    }
                )
        )
        Text(
            "ITEMS",
            modifier = Modifier
                .weight(1f)
                .clickable(
                    onClick = {
                        vm.showSplash.value = false
                        vm.toolbar.navigate(
                            queryType = QueryType.ITEMS,
                            title = "Stores"
                        )
                    }
                )
        )
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
        // Main rounded rectangle
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
