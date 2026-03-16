package android.myguide.views


import android.myguide.R
import android.myguide.colorScheme
import android.myguide.current
import android.myguide.getLineHeightDp
import android.myguide.measures
import android.myguide.qqq
import android.myguide.screen
import android.myguide.typography
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar() {
    val ident by current.observeAsState()
    if (ident == null) return
    val vm = screen[ident!!]!!.vm
    val mode = remember { mutableStateOf<Boolean?>(null) }
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
                    .size(36.dp)
                    .padding(10.dp)
            )
            Text(
                if (current.value!!) "B" else "A",
                color = colorScheme.secondary,
                style = typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
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
                        36.dp,
                        36.dp
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable._text),
                        "font scale",
                        colorFilter = ColorFilter.tint(colorScheme.secondary),
                        modifier = Modifier
                            .size(36.dp)
                            .padding(10.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "%.2f".format(vm.scale.value),
                        style = typography.labelSmall,
                        fontSize = 12.sp,
                        modifier = Modifier.width(32.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.remove),
                        "decrease font scale",
                        colorFilter = ColorFilter.tint(colorScheme.primary),
                        modifier = Modifier
                            .size(36.dp)
                            .padding(6.dp)
                            .clickable(
                                onClick = {
                                    vm.scale.value = vm.scale.value!! - .01f
                                    vm.adjust.value = true

                                    qqq("SCALE "+measures.itemHeight*vm.scale.value!!)
                                }
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Slider(
                        value = scale!!,
                        onValueChange = {
                            vm.adjust.value = false
                            vm.scale.value = it

                            qqq("SCALE "+measures.itemHeight*vm.scale.value!!)
                        },
                        onValueChangeFinished = { vm.adjust.value = true },
                        valueRange = 0.75f..2f,
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
                    @Composable
                    fun t() {
                        val x=
                        getLineHeightDp(
                            typography.bodyLarge.fontSize
                        )
                        val y=getLineHeightDp(
                            typography.bodyMedium.fontSize
                        )
                        val z=getLineHeightDp(
                            typography.bodySmall.fontSize*2
                        )
                        qqq("x"+(x+y+z)+ " "+((x+y+z)*vm.scale.value!!))
                    }

                    t()
                    Spacer(Modifier.width(8.dp))
                    Image(
                        painter = painterResource(R.drawable.add),
                        "increase font scale",
                        colorFilter = ColorFilter.tint(colorScheme.primary),
                        modifier = Modifier
                            .size(36.dp)
                            .padding(6.dp)
                            .clickable(
                                onClick = {
                                    vm.scale.value = vm.scale.value!! + .01f
                                    vm.adjust.postValue(true)

                                    qqq("SCALE "+measures.itemHeight*vm.scale.value!!)
                                }
                            )
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(
                            when (mode.value) {
                                false -> R.drawable._horizontal
                                true -> R.drawable._vertical
                                null -> R.drawable._hv
                            }
                        ),
                        "mode switch",
                        colorFilter = ColorFilter.tint(colorScheme.primary),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable(
                                onClick = {
                                    if (mode.value == null) {
                                        vm.ratioH.value = vm.ratio.value
                                        vm.ratioV.value = vm.ratio.value
                                    }
                                    mode.value = when (mode.value) {
                                        false -> true
                                        true -> null
                                        null -> false
                                    }
                                    if (mode.value == null) {
                                        vm.ratio.value = 1f
                                        vm.ratioH.value = null
                                        vm.ratioV.value = null
                                    }
                                }
                            )
                            .padding(6.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "%.2f".format(
                            when (mode.value) {
                                false -> vm.ratioH.value ?: ""
                                true -> vm.ratioV.value ?: ""
                                null -> vm.ratio.value!!
                            }
                        ),
                        style = typography.labelSmall,
                        fontSize = 12.sp,
                        modifier = Modifier.width(32.dp)
                    )
                    Image(
                        painter = painterResource(R.drawable.remove),
                        "minus",
                        colorFilter = ColorFilter.tint(colorScheme.primary),
                        modifier = Modifier
                            .size(36.dp)
                            .padding(6.dp)
                            .clickable(
                                onClick = {
                                    when (mode.value) {
                                        false -> vm.ratioH.value = vm.ratioH.value!! - .01f
                                        true -> vm.ratioV.value = vm.ratioH.value!! - .01f
                                        null -> vm.ratio.value = vm.ratio.value!! - .01f
                                    }
                                    vm.adjust.value = true
                                }
                            )
                    )
                    Spacer(Modifier.width(8.dp))
                    Slider(
                        value =
                            when (mode.value) {
                                false -> ratioH ?: ratio!!
                                true -> ratioV ?: ratio!!
                                else -> ratio!!
                            },
                        onValueChange = {
                            vm.adjust.value = false
                            when (mode.value) {
                                false -> vm.ratioH.value = it
                                true -> vm.ratioV.value = it
                                null -> vm.ratio.value = it
                            }
                        },
                        onValueChangeFinished = { vm.adjust.value = true },
                        valueRange = 0.75f..2f,
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
                    Spacer(Modifier.width(8.dp))
                    Image(
                        painter = painterResource(R.drawable.add),
                        "plus",
                        colorFilter = ColorFilter.tint(colorScheme.primary),
                        modifier = Modifier
                            .size(36.dp)
                            .padding(6.dp)
                            .clickable(
                                onClick = {
                                    when (mode.value) {
                                        false -> vm.ratioH.value = vm.ratioH.value!! + .01f
                                        true -> vm.ratioV.value = vm.ratioH.value!! + .01f
                                        null -> vm.ratio.value = vm.ratio.value!! + .01f
                                    }
                                    vm.adjust.value = true
                                }
                            )
                    )
                }
            }
        }
    }
}
