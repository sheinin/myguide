package android.myguide

import android.myguide.ui.theme.MyGuideTheme
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.properties.Delegates

const val batch = 21
var screenHeight = 0.dp
var screenWidth = 0.dp
lateinit var colorScheme: ColorScheme
lateinit var density: Density
lateinit var fontFamilyResolver: FontFamily.Resolver
var fontScale by Delegates.notNull<Float>()
lateinit var measures: Measures
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
            list.filter { it.pic != null }
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
            list.map {
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
                        ident = false
                    ),
            true to
                    Screen(
                        activity = this@MainActivity,
                        ident = true
                    )
        )
        setContent {
            GetScreenSize()
            density = LocalDensity.current
            fontFamilyResolver = LocalFontFamilyResolver.current
            fontScale =  resources.configuration.fontScale
            typography = MaterialTheme.typography
            val h =
                (getLineHeightDp(typography.bodyLarge.lineHeight) +
                        getLineHeightDp(typography.bodyMedium.lineHeight) +
                        getLineHeightDp(typography.bodySmall.lineHeight) * 2 +
                        16.dp) * fontScale
            val w = screenWidth - h - 32.dp// * fontScale
            qqq("WWW "+getLineHeightDp(typography.bodySmall.lineHeight)+" "+typography.bodySmall.lineHeight+ ": "+h + " "+w + " "+resources.configuration.fontScale)
            measures = Measures(
                descriptionWidth = w,
                itemHeight = h,
                nodePadding = 16.dp,// * fontScale,
                lineHeight = getLineHeightDp(typography.bodySmall.lineHeight)
            )
            MyGuideTheme {
                colorScheme = MaterialTheme.colorScheme
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        MeasuredFlowList(false)
                        MeasuredFlowList(true)
                        val show = vm.showSplash.observeAsState()
                        if (show.value!!)
                            Splash(Modifier.padding(innerPadding))
                        else {
                            val ident = vm.current.observeAsState()
                            AnimatedVisibility(
                                visible = ident.value == false,
                                enter = EnterTransition.None,
                                exit = ExitTransition.None
                            ) {
                                Main(
                                    ident = false,
                                    modifier = Modifier.padding(innerPadding),
                                    screen = screen[false]!!
                                )
                            }
                            AnimatedVisibility(
                                ident.value == true,
                                enter = EnterTransition.None,
                                exit = ExitTransition.None
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
