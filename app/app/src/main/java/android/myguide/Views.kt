package android.myguide

import android.view.ViewTreeObserver
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.ui.res.painterResource
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.MotionScene
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
            val (toolbar, scroll) = createRefs()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp)
                    .constrainAs(toolbar) {
                        if (isMap) {
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
                Icon(
                    painterResource(if (isMap) R.drawable.list else R.drawable.map),
                    "",
                    modifier = Modifier.clickable(
                        onClick = { isMap = !isMap }
                    )
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
                  //  println("qqqScroll position: ${scrollState.value}")
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
                            top.linkTo(toolbar.bottom)
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
                    Box(
                        modifier = Modifier
                            .size(bind.w.value!!, bind.h.value!!)
                            .padding(end = 8.dp)
                            .background(Color(0xFF6200EE))
                    ) {
                        val items by bind.cycler.items.collectAsStateWithLifecycle()
                        repeat(batch) { index ->
                            val item = items[index]
                            if (item.title.isNotEmpty()) RenderItem(item)
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun RenderItem(item: ViewModel.Cycler.Item) {
    qqq("TITLE " + " "+item.x+ " "+item.y +" "+item.w + " "+item.h+ " " +item?.title )
    Row(
        modifier = Modifier
            .offset(item.x, item.y)
            .size(item.w, item.h)
    ) {
        Image(
            painterResource(R.drawable.ic_launcher_foreground), "",
            modifier = Modifier
                .size(60.dp)

        )
        Text(
            item.title,
            color = Color.Red,
            modifier = Modifier.padding(8.dp).fillMaxWidth()
              //
                .background(Color.Green)

        )
    }
}
