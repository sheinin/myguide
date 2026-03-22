package com.myguide.views

import android.content.res.Configuration
import com.myguide.R
import com.myguide.data.Query
import com.myguide.toolbar
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource


@Composable
fun Splash(modifier: Modifier) {
    when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE ->
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .fillMaxSize()
            ) {
                Content(Modifier.weight(1f))
            }
        else ->
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .fillMaxSize()
            ) {
                Content(Modifier.weight(1f))
            }
    }
}

@Composable
fun Content(modifier: Modifier) {
    Spacer(modifier)
    Image(
        painter = painterResource(R.drawable._shops),
        "all shops",
        colorFilter = ColorFilter.tint(Color.Blue),
        modifier = modifier
            .fillMaxSize()
            .clickable(
                onClick = {
                    toolbar.navigate(
                        query = Query.SHOPS,
                        title = "All Shops"
                    )
                }
            )
    )
    Spacer(modifier)
    Image(
        painter = painterResource(R.drawable._items),"all items",
        colorFilter = ColorFilter.tint(Color.Red),
        modifier = modifier
            .fillMaxSize()
            .clickable(
                onClick = {
                    toolbar.navigate(
                        query = Query.ITEMS,
                        title = "All Items"
                    )
                }
            )
    )
    Spacer(modifier)
}