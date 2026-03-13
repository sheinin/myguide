package android.myguide

import android.R.attr.lineHeight
import android.R.id.toggle
import android.app.ProgressDialog.show
import android.myguide.ViewModel.Screen.Display.MAP
import android.myguide.ui.theme.MyGuideTheme
import android.myguide.views.Main
import android.myguide.views.Splash
import android.myguide.views.Toolbar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.constraintlayout.compose.Dimension.Companion.ratio
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlin.properties.Delegates

const val batch = 21
var screenHeight = 0.dp
var screenWidth = 0.dp
lateinit var colorScheme: ColorScheme
lateinit var density: Density
lateinit var fontFamilyResolver: FontFamily.Resolver
lateinit var measures: Measures
lateinit var vm: ViewModel
lateinit var typography: Typography
var fontScale by Delegates.notNull<Float>()

lateinit var screen: Map<Boolean, Screen>

class MainActivity : ComponentActivity() {
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
            BackHandler(enabled = true) { vm.toolbar.back() }
            GetScreenSize()
            density = LocalDensity.current
            fontFamilyResolver = LocalFontFamilyResolver.current
            fontScale =  resources.configuration.fontScale
            typography = MaterialTheme.typography
            Column(
                Modifier
                    .onGloballyPositioned { coordinates ->
                        measures = Measures(
                            itemHeight = coordinates.size.height.toDp(),
                            padding = 8.dp
                        )
                        // Convert the measured height from pixels (Int) to Dp
                        qqq("START f:$fontScale itemHeight:${coordinates.size.height.toDp()}")
                    }
            ) {
                Text(
                    "1",
                    style = typography.bodyLarge,
                    fontSize = typography.bodyLarge.fontSize,
                    lineHeight = 1.em * fontScale,
                )
                Text(
                    "1",
                    style = typography.bodyMedium,
                    fontSize = typography.bodyMedium.fontSize,
                    lineHeight = 1.em * fontScale,
                )
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize,
                                fontWeight = typography.bodySmall.fontWeight
                            )
                        ) { append("1\n1") }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = typography.bodySmall
                )
            }
            val h =
                getLineHeightDp(typography.bodyLarge.fontSize) +
                    getLineHeightDp(typography.bodyMedium.fontSize) +
                    getLineHeightDp(typography.bodySmall.fontSize) * 2
            qqq("START f:$fontScale itemHeight:$h w:$screenWidth h:$screenHeight}")

            MyGuideTheme {
                colorScheme = MaterialTheme.colorScheme
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (vm.showSplash.observeAsState().value!!)
                        Splash(Modifier.padding(innerPadding))
                    else {
                        Column(Modifier.fillMaxSize().padding(innerPadding)) {
                            Toolbar()
                            val ident = vm.current.observeAsState()
                            AnimatedVisibility(
                                visible = ident.value == false,
                                enter = EnterTransition.None,
                                exit = ExitTransition.None
                            ) {
                                Main(
                                    ident = false,
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


