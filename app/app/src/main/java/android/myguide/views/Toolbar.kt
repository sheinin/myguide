package android.myguide.views


import android.myguide.R
import android.myguide.UI.BUTTON
import android.myguide.UI.ITEM_HEIGHT
import android.myguide.colorScheme
import android.myguide.current
import android.myguide.data.VM
import android.myguide.qqq
import android.myguide.screen
import android.myguide.typography
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar() {
    val ident by current.observeAsState()
    if (ident == null) return
    val vm = screen[ident!!]!!.vm
    val mode = remember { mutableStateOf<Boolean?>(null) }
    val margin by vm.margin.collectAsStateWithLifecycle()
    val ratio by vm.ratio.observeAsState()
    val ratioH by vm.ratioH.observeAsState()
    val ratioV by vm.ratioV.observeAsState()
    val scale by vm.scale.observeAsState()
    var visible by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.select),
                contentDescription = "screen switch",
                colorFilter = ColorFilter.tint(colorScheme.secondary),
                modifier = Modifier
                    .size(BUTTON)
                    .padding(10.dp)
            )
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (current.value!!) "B" else "A",
                    color = colorScheme.background,
                    style =
                        typography.labelLarge.copy(
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            ),
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        ),
                    fontWeight = FontWeight.W900,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.back),
                contentDescription = "",
                colorFilter = ColorFilter.tint(colorScheme.secondary),
                modifier = Modifier
                    .clickable(
                        onClick = { visible = !visible }
                    )
                    .rotate(if (visible) -90f else 90f)
                    .size(
                        BUTTON,
                        BUTTON
                    )
                    .padding(6.dp)
            )
        }
        AnimatedVisibility(
            visible = visible,
            enter = EnterTransition.None,
            exit = ExitTransition.None
        ) {
            Column {
                SliderRow(
                    action = { vm.scale.value = 1f },
                    icon = R.drawable._text,
                    minus = {
                        vm.scale.value = vm.scale.value!! - .01f
                        vm.adjust.value = true

                        qqq("SCALE "+ITEM_HEIGHT*vm.scale.value!!)
                    },
                    plus = {
                        vm.scale.value = vm.scale.value!! + .01f
                        vm.adjust.postValue(true)
                    },
                    sliderValue = scale!!,
                    sliderChange = {
                        vm.adjust.value = false
                        vm.scale.value = it
                    },
                    sliderFinished =  { vm.adjust.value = true },
                    txt = "%.2f".format(vm.scale.value!!),
                    valueRange = 0.85f .. 2f
                )
                SliderRow(
                    action = {
                        if (mode.value == null) {
                            vm.ratioH.value = vm.ratio.value
                            vm.ratioV.value = vm.ratio.value
                        }
                        mode.value =
                            when (mode.value) {
                                false -> true
                                true -> null
                                null -> false
                            }
                        if (mode.value == null) {
                            vm.ratio.value = 1f
                            vm.ratioH.value = null
                            vm.ratioV.value = null
                        }
                    },
                    icon = when (mode.value) {
                        false -> R.drawable._horizontal
                        true -> R.drawable._vertical
                        null -> R.drawable._hv
                    },
                    minus = {
                        when (mode.value) {
                            false -> vm.ratioH.value = vm.ratioH.value!! - .01f
                            true -> vm.ratioV.value = vm.ratioH.value!! - .01f
                            null -> vm.ratio.value = vm.ratio.value!! - .01f
                        }
                        vm.adjust.value = true
                    },
                    plus = {
                        when (mode.value) {
                            false -> vm.ratioH.value = vm.ratioH.value!! + .01f
                            true -> vm.ratioV.value = vm.ratioH.value!! + .01f
                            null -> vm.ratio.value = vm.ratio.value!! + .01f
                        }
                        vm.adjust.value = true
                    },
                    sliderValue =
                        when (mode.value) {
                            false -> ratioH ?: ratio!!
                            true -> ratioV ?: ratio!!
                            else -> ratio!!
                        },
                    sliderChange = {
                        vm.adjust.value = false
                        when (mode.value) {
                            false -> vm.ratioH.value = it
                            true -> vm.ratioV.value = it
                            null -> vm.ratio.value = it
                        }
                    },
                    sliderFinished =  { vm.adjust.value = true },
                    txt =
                        "%.2f".format(
                            when (mode.value) {
                                false -> vm.ratioH.value ?: 1f
                                true -> vm.ratioV.value ?: 1f
                                null -> vm.ratio.value!!
                            }
                        ),
                    valueRange = 0.75f .. 1.5f
                )
                SliderRow(
                    action = { vm.margin(1f) },
                    icon = R.drawable._margin,
                    minus = {
                        vm.margin(false)
                        vm.adjust.value = true
                    },
                    plus = {
                        vm.margin(true)
                        vm.adjust.postValue(true)
                    },
                    sliderValue = margin,
                    sliderChange = {
                        vm.adjust.value = false
                        vm.margin(it)
                    },
                    sliderFinished =  { vm.adjust.value = true },
                    txt = "%.2f".format(margin),
                    valueRange = 0.5f .. 2.5f
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderRow(
    action: (() -> Unit)? = null,
    icon: Int,
    minus: () -> Unit,
    plus: () -> Unit,
    sliderValue: Float,
    sliderChange: (Float) -> Unit,
    sliderFinished: () -> Unit,
    txt: String,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            null,
            colorFilter = ColorFilter.tint(colorScheme.primary),
            modifier = Modifier
                .size(BUTTON)
                .padding(8.dp)
                .clickable(onClick = action ?: {})
        )
        Spacer(Modifier.width(16.dp))
        Text(
            txt,
            style = typography.labelSmall,
            fontSize = 12.sp,
            modifier = Modifier.width(32.dp)
        )
        Image(
            painter = painterResource(R.drawable._remove),
            null,
            colorFilter = ColorFilter.tint(colorScheme.primary),
            modifier = Modifier
                .size(BUTTON)
                .padding(6.dp)
                .clickable(
                    onClick = minus
                )
        )
        Slider(
            value = sliderValue,
            onValueChange = sliderChange,
            onValueChangeFinished = sliderFinished,
            valueRange = valueRange,
            modifier = Modifier
                .weight(1f)
                .height(20.dp),
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    thumbTrackGapSize = 0.dp,
                    colors = SliderDefaults.colors(
                        thumbColor = colorScheme.primary,
                        activeTrackColor = colorScheme.secondaryContainer,
                        inactiveTrackColor = colorScheme.secondaryContainer,
                    )
                )
            }
        )
        Image(
            painter = painterResource(R.drawable.add),
            "increase font scale",
            colorFilter = ColorFilter.tint(colorScheme.primary),
            modifier = Modifier
                .size(BUTTON)
                .padding(6.dp)
                .clickable(onClick = plus)
        )
    }
}