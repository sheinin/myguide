package com.myguide.views

import com.myguide.current
import com.myguide.density
import com.myguide.screen
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density


@Composable
fun Main(innerPadding: PaddingValues) {
    val ident = current.observeAsState()
    Column(Modifier.fillMaxSize().padding(innerPadding)) {
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
                                        screen[false]!!.vm.ratioV.observeAsState().value ?:
                                        screen[false]!!.vm.ratio.observeAsState().value!!
                                    )
                                    * screen[false]!!.vm.scale.observeAsState().value!!
                            )
                ) { View(screen = screen[false]!!) }
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