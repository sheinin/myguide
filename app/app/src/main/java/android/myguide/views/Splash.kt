package android.myguide.views

import android.myguide.QueryType
import android.myguide.R
import android.myguide.screenWidth
import android.myguide.toolbar
import android.myguide.typography
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun Splash(modifier: Modifier) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(
                    onClick = {
                        //current.value = null
                        //showSplash.value = false
                        toolbar.navigate(
                            queryType = QueryType.SHOPS,
                            title = "All Shops"
                        )
                    }
                )
        ) {
            Image(painter = painterResource(R.drawable.all_shops), "all shops",
                modifier = Modifier.size(screenWidth * .5f))
            Text(
                "SHOPS",
                fontWeight = FontWeight.Bold,
                style = typography.displayMedium,
            )
        }
        Spacer(Modifier.height(16.dp))
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
                modifier = Modifier.size(screenWidth * .5f)
            )
            Text(
                "ITEMS",
                fontWeight = FontWeight.Bold,
                style = typography.displayMedium
            )
        }
    }
}
