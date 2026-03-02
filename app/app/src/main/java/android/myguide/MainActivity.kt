package android.myguide

import android.myguide.ui.theme.MyGuideTheme
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
        vm.toolbar.init(this)
        vm.updateItemList { list ->
            list.filter { it.drawable == null && it.pic != null }
                .map {
                    vm.updateItem(
                        this.resources.getIdentifier(
                            it.pic,
                            "drawable",
                            this.packageName
                        ),
                        it.pic!!
                    )
                }
        }
        vm.updateShopList { list ->
            list.filter { it.drawable == null }
                .map {
                    vm.updateShop(
                        this.resources.getIdentifier(
                            it.id,
                            "drawable",
                            this.packageName
                        )
                        , it.id
                    )
                }
        }
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
        setContent {
            density = LocalDensity.current
            fontFamilyResolver = LocalFontFamilyResolver.current
            typography = MaterialTheme.typography
            GetScreenSize()
            MyGuideTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val show = vm.showSplash.observeAsState()
                    if (show.value!!)
                        Splash(Modifier.padding(innerPadding))
                    else {
                        val ident = vm.current.observeAsState()
                        AnimatedVisibility(
                            visible = ident.value == false,
                            enter = fadeIn(initialAlpha = 0.3f),
                            exit = fadeOut()
                        ) {
                            Main(
                                ident = false,
                                modifier = Modifier.padding(innerPadding),
                                screen = screen[false]!!
                            )
                        }
                        AnimatedVisibility(
                            ident.value == true,
                            enter = fadeIn(initialAlpha = 0.3f),
                            exit = fadeOut()
                        ) {
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

fun Int.toDp(): Dp {
    return with(density) {
        this@toDp.toDp()
    }
}