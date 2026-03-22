package android.myguide.views

import android.myguide.R
import android.myguide.UI.BUTTON
import android.myguide.UI.MARGIN
import android.myguide.colorScheme
import android.myguide.current
import android.myguide.data.VM
import android.myguide.data.VM.Type.*
import android.myguide.screen
import android.myguide.toDp
import android.myguide.typography
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em


@Composable
fun Control(
    control: Boolean,
    filter: Boolean?,
    sort: Boolean?,
    type: VM.Type?,
    ratioH: Float,
    ratioV: Float,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MARGIN.toDp() * ratioH,
                vertical = MARGIN.toDp() * ratioV
            )
    ) {
        Text(
            title,
            style = typography.titleMedium,
            fontWeight = FontWeight.Bold,
            lineHeight = 1.em,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        if (control) {
            Image(
                painter = painterResource(
                    when (filter) {
                        false -> R.drawable._east
                        true -> R.drawable._west
                        null -> R.drawable._filter
                    }
                ),
                contentDescription = "filter",
                colorFilter = ColorFilter.tint(colorScheme.primary),
                modifier = Modifier
                    .clickable(
                        onClick = {
                            screen[current.value!!]!!.vm.filter.value =
                                when (screen[current.value!!]!!.vm.filter.value) {
                                    false -> true
                                    true -> null
                                    null -> false
                                }
                        }
                    )
                    .size(
                        width = BUTTON.toDp() * ratioH,
                        height = BUTTON.toDp() * ratioH
                    )
                    .padding(
                        horizontal = 6.dp * ratioH,
                        vertical = 6.dp * ratioV
                    )
            )
            Spacer(Modifier.width(8.dp))
            Image(
                painter = painterResource(R.drawable._sort),
                contentDescription = "sort",
                colorFilter = ColorFilter.tint(colorScheme.primary),
                modifier = Modifier
                    .scale(scaleX = 1f, scaleY = if (sort == true) -1f else 1f)
                    .clickable(
                        onClick = {
                            screen[current.value!!]!!.vm.sort.value =
                                !screen[current.value!!]!!.vm.sort.value!!
                        }
                    )
                    .size(
                        width = BUTTON.toDp() * ratioH,
                        height = BUTTON.toDp() * ratioH
                    )
                    .padding(
                        horizontal = 6.dp * ratioH,
                        vertical = 6.dp * ratioV
                    )
            )
            Spacer(Modifier.width(8.dp))
            Image(
                painter = painterResource(
                    when (type!!) {
                        V -> R.drawable.view_list
                        H -> R.drawable.map
                        T -> R.drawable.grid
                    }
                ),
                contentDescription = "list",
                colorFilter = ColorFilter.tint(colorScheme.primary),
                modifier = Modifier
                    .clickable(
                        onClick = {
                            screen[current.value!!]!!.display(
                                when (screen[current.value!!]!!.vm.type.value!!) {
                                    V -> H
                                    H -> T
                                    T -> V
                                }
                            )
                        }
                    )
                    .size(
                        width = BUTTON.toDp() * ratioH,
                        height = BUTTON.toDp() * ratioH
                    )
                    .padding(
                        horizontal = 6.dp * ratioH,
                        vertical = 6.dp * ratioV
                    )
            )
        }
    }
}
