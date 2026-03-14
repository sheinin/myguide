package android.myguide.views


import android.R.attr.track
import android.myguide.R
import android.myguide.colorScheme
import android.myguide.fontScale
import android.myguide.screen
import android.myguide.typography
import android.myguide.vmm
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        val mode = remember { mutableStateOf<Boolean?>(null) }
        val ratio by vmm.ratio.observeAsState()
        val ratioH by vmm.ratioH.observeAsState()
        val ratioV by vmm.ratioV.observeAsState()
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
                            vmm.ratioH.value = vmm.ratio.value
                            vmm.ratioV.value = vmm.ratio.value
                        }
                        mode.value = when (mode.value) {
                            false -> true
                            true -> null
                            null -> false
                        }
                        if (mode.value == null) {
                            vmm.ratio.value = 1f
                            vmm.ratioH.value = null
                            vmm.ratioV.value = null
                        }
                    }
                )
                .padding(6.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "%.2f".format(
                when (mode.value) {
                    false -> vmm.ratioH.value ?: ""
                    true -> vmm.ratioV.value ?: ""
                    null -> vmm.ratio.value!!
                }
            ),
            style = typography.labelSmall,
            modifier = Modifier.width(32.dp * fontScale)
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
                            false -> vmm.ratioH.value = vmm.ratioH.value!! - .01f
                            true -> vmm.ratioV.value = vmm.ratioH.value!! - .01f
                            null -> vmm.ratio.value = vmm.ratio.value!! - .01f
                        }
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
                vmm.adjust[vmm.current.value!!]!!.value = false
                when (mode.value) {
                    false -> vmm.ratioH.value = it
                    true -> vmm.ratioV.value = it
                    null -> vmm.ratio.value = it
                }
            },
            onValueChangeFinished = { screen[vmm.current.value]!!.vm},//vmm.adjust[vmm.current.value]!!.value = true },
            valueRange = 0.5f..2.5f,
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
                            false -> vmm.ratioH.value = vmm.ratioH.value!! + .01f
                            true -> vmm.ratioV.value = vmm.ratioH.value!! + .01f
                            null -> vmm.ratio.value = vmm.ratio.value!! + .01f
                        }
                    }
                )
        )
    }
}