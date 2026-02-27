package android.myguide

import android.myguide.ViewModel.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.myguide.ui.theme.MyGuideTheme
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.VerticalAlign
import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

val batch = 15
var screenHeight = 0.dp
var screenWidth = 0.dp
lateinit var density: Density
lateinit var fontFamilyResolver: FontFamily.Resolver
lateinit var vm: ViewModel
lateinit var typography: Typography

class MainActivity : ComponentActivity() {
    lateinit var screen: Map<Boolean, Screen>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dao = StoreDatabase.getDatabase(application).storeDao()
        val repository = Repository(dao)
        vm = ViewModel(repository)

        screen = mapOf(
            false to
                    Screen(
                        activity = this@MainActivity,
                        ident = false,
                    ),
            true to
                    Screen(
                        activity = this@MainActivity,
                        ident = true,
                        // binding = bind.screenB
                    )
        )


        vm.allItems.observe(this) { users ->
            //users.map { qqq("users: $it") }
            //userAdapter.submitList(users) // Use submitList here
        }
        vm.fetchItems()
        vm.toolbar.init(this)
        setContent {
            density = LocalDensity.current
            fontFamilyResolver = LocalFontFamilyResolver.current
            typography = MaterialTheme.typography
            GetScreenSize()
            MyGuideTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val show = vm.showSplash.observeAsState()
                    if (show.value!!)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                        ) {
                            Text(
                                "STORES",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        onClick = {
                                            vm.toolbar.navigate(
                                                queryType = QueryType.STORES,
                                                title = "Stores"
                                            )
                                        }
                                    )
                            )
                            Text(
                                "ITEMS",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        onClick = {
                                            vm.showSplash.value = false
                                            vm.toolbar.navigate(
                                                queryType = QueryType.ITEMS,
                                                title = "Stores"
                                            )
                                        }
                                    )
                            )
                        }
                    else {
                        val ident = vm.current.observeAsState()
                        AnimatedVisibility(ident.value == false) {
                            Main(
                                ident = false,
                                modifier = Modifier.padding(innerPadding),
                                screen = screen[false]!!
                            )
                        }
                        AnimatedVisibility(ident.value == true) {
                            Main(
                                ident = true,
                                modifier = Modifier.padding(innerPadding),
                                screen = screen[true]!!
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
    val singapore = LatLng(1.35, 103.86)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyGuideTheme {
        Greeting("Android")
    }
}


fun qqq(q: String) { println("qqq $q") }


@Composable
fun GetScreenSize() {
    with (LocalDensity.current) {
        screenHeight = LocalWindowInfo.current.containerSize.height.toDp()
        screenWidth = LocalWindowInfo.current.containerSize.width.toDp()
    }
}


fun sleep(delay: Long = 0, callback: (() -> Unit)) { Handler(Looper.getMainLooper()).postDelayed({ callback.invoke() }, delay) }


fun Dp.toPx(): Float {
    return with(density) {
        this@toPx.toPx()
    }
}