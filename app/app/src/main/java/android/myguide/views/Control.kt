package android.myguide.views

import android.myguide.QueryType
import android.myguide.R
import android.myguide.Screen
import android.myguide.ViewModel.Screen.Display.D3
import android.myguide.ViewModel.Screen.Display.LIST
import android.myguide.ViewModel.Screen.Display.MAP
import android.myguide.colorScheme
import android.myguide.typography
import android.myguide.vm
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun Control(screen: Screen) {
    val display by vm.screen[screen.ident]!!.display.observeAsState()
    val filter by vm.screen[screen.ident]!!.filter.observeAsState()
    val sort by vm.screen[screen.ident]!!.sort.observeAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
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
        if (screen.queryType == QueryType.ITEM || screen.queryType == QueryType.SHOPS) {
            Image(
                painter = painterResource(
                    when (filter) {
                        false -> R.drawable.east
                        true -> R.drawable.west
                        null -> R.drawable.filter
                    }
                ),
                contentDescription = "filter",
                colorFilter = ColorFilter.tint(colorScheme.primary),
                modifier = Modifier
                    .clickable(
                        onClick = {
                            vm.screen[screen.ident]!!.filter.value =
                                when (vm.screen[screen.ident]!!.filter.value) {
                                    false -> true
                                    true -> null
                                    null -> false
                                }
                        }
                    )
                    .padding(6.dp)
            )
            Spacer(Modifier.width(8.dp))
            Image(
                painter = painterResource(R.drawable.sort),
                contentDescription = "sort",
                colorFilter = ColorFilter.tint(colorScheme.primary),
                modifier = Modifier
                    .scale(scaleX = 1f, scaleY = if (sort == true) -1f else 1f)
                    .clickable(
                        onClick = {
                            vm.screen[screen.ident]!!.sort.value =
                                !vm.screen[screen.ident]!!.sort.value!!
                        }
                    )
                    .padding(6.dp)
            )
            Spacer(Modifier.width(8.dp))
            Image(
                painter = painterResource(
                    when (display!!) {
                        LIST -> R.drawable.view_list
                        MAP -> R.drawable.map
                        D3 -> R.drawable.grid
                    }
                ),
                contentDescription = "list",
                colorFilter = ColorFilter.tint(colorScheme.primary),
                modifier = Modifier
                    .clickable(
                        onClick = {
                            vm.screen[screen.ident]!!.display.value =
                                when (vm.screen[screen.ident]!!.display.value!!) {
                                    LIST -> MAP
                                    MAP -> D3
                                    D3 -> LIST
                                }
                            screen.render.display()
                        }
                    )
                    .padding(6.dp)
            )
        }
    }
}
