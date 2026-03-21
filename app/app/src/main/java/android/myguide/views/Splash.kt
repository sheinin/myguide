package android.myguide.views

import android.content.res.Configuration
import android.myguide.QueryType
import android.myguide.R
import android.myguide.screenWidth
import android.myguide.toolbar
import android.myguide.typography
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun Splash(modifier: Modifier) {
    val configuration = LocalConfiguration.current
    when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE ->

            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .fillMaxSize()
            ) {
                Button1()
                Spacer(Modifier.height(16.dp))
                Button2()
            }
        else ->
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = modifier
                    .fillMaxSize()
            ) {
                Button1()
                Spacer(Modifier.height(16.dp))
                Button2()
            }
    }
}

@Composable
fun Button1() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                onClick = {
                    toolbar.navigate(
                        queryType = QueryType.SHOPS,
                        title = "All Shops"
                    )
                }
            )
    ) {
        Image(painter = painterResource(R.drawable.all_shops), "all shops",
            modifier = Modifier.size(210.dp))
        Text(
            "SHOPS",
            fontWeight = FontWeight.Bold,
            style = typography.displayMedium,
        )
    }
}

@Composable
fun Button2() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                onClick = {
                    // showSplash.value = false
                    toolbar.navigate(
                        queryType = QueryType.ITEMS,
                        title = "All Items"
                    )
                }
            )
    ) {
        Image(
            painter = painterResource(R.drawable.all_items), "all items",
            modifier = Modifier.size(210.dp)
        )
        Text(
            "ITEMS",
            fontWeight = FontWeight.Bold,
            style = typography.displayMedium
        )
    }
}