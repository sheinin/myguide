package com.myguide.views

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberScrollable2DState
import androidx.compose.foundation.gestures.scrollable2D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.myguide.current
import com.myguide.density
import com.myguide.screen


@Composable
fun Main(innerPadding: PaddingValues) {
    val ident = current.observeAsState()
    Column(Modifier
        .fillMaxSize()
        .padding(innerPadding)) {
        Toolbar()
        Box {
            val visibleState = remember(ident.value == false) {
                MutableTransitionState(!ident.value!!)
            }
            val visibleState1 = remember(ident.value == true) {
                MutableTransitionState(ident.value!!)
            }
            LaunchedEffect(visibleState) {
                snapshotFlow { visibleState.currentState == visibleState.targetState }
                    .collect { isIdle ->
                        if (isIdle && !visibleState.targetState)
                            screen[current.value ?: true]!!.query()
                    }
            }
            LaunchedEffect(visibleState1) {
                snapshotFlow { visibleState1.currentState == visibleState1.targetState }
                    .collect { isIdle ->
                        if (isIdle && !visibleState1.targetState)
                            screen[current.value ?: true]!!.query()
                    }
            }
            androidx.compose.animation.AnimatedVisibility(
                visibleState = visibleState,
                enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
                exit = slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
            ) {
                CompositionLocalProvider(
                    LocalDensity provides
                            Density(
                                density = density.density,
                                fontScale =
                                    (
                                            screen[false]!!.vm.ratioV.observeAsState().value
                                                ?: screen[false]!!.vm.ratio.observeAsState().value!!
                                            )
                                            * screen[false]!!.vm.scale.observeAsState().value!!
                            )
                ) {
                    //Scrollable2DExample()
                    View(screen = screen[false]!!)
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visibleState = visibleState1,
                enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
                exit = slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
            ) {
                CompositionLocalProvider(
                    LocalDensity provides
                            Density(
                                density = density.density,
                                fontScale = screen[true]!!.vm.scale.observeAsState().value!!
                            )
                ) { View(screen = screen[true]!!) }
            }
        }
    }
}


@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun Scrollable2DExample() {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // State that updates our offsets as the user scrolls in 2D
    val scrollState = rememberScrollable2DState { delta ->
        // delta: Offset where positive x = right, positive y = down
        offsetX += delta.x
        offsetY += delta.y
        delta
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .scrollable2D(
                state = scrollState,
                enabled = true
            ),
        color = MaterialTheme.colorScheme.background
    ) {
        Canvas(modifier = Modifier) {
            // Draw a simple grid, offset by scroll
            val step = 50f
            val width = size.width
            val height = size.height

            // Vertical lines
            var x = (offsetX % step) - step
            while (x < width) {
                drawLine(
                    color = Color.LightGray,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.dp.toPx()
                )
                x += step
            }

            // Horizontal lines
            var y = (offsetY % step) - step
            while (y < height) {
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += step
            }

            // A red dot at the origin (0,0) relative to the scroll
            val origin = Offset(
                x = width / 2f - offsetX,
                y = height / 2f - offsetY
            )
            drawCircle(
                color = Color.Red,
                radius = 8.dp.toPx(),
                center = origin
            )
        }
    }
}
