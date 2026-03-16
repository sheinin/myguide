package android.myguide


import android.R.attr.translationX
import android.R.attr.visible
import android.myguide.density
import android.myguide.ui.theme.MyGuideTheme
import android.myguide.views.Main
import android.myguide.views.MyDialog
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
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.MutableLiveData
import kotlin.properties.Delegates

const val batch = 5
lateinit var colorScheme: ColorScheme
lateinit var db: DB
lateinit var density: Density
lateinit var fontFamilyResolver: FontFamily.Resolver
lateinit var measures: Measures
lateinit var typography: Typography
val current = MutableLiveData<Boolean?>(null)
val dialog = MutableLiveData(false)
val toolbar = android.myguide.Toolbar()
//var fontScale = MutableLiveData(1f)//by Delegates.notNull<Float>()
var lock = false
var screenHeight = 0.dp
var screenWidth = 0.dp
lateinit var state: MutableTransitionState<Boolean>

lateinit var screen: Map<Boolean, Screen>

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val dao = StoreDatabase.getDatabase(application).storeDao()
        val repository = Repository(dao)
        db = DB(repository)
        db.updateItemList { list ->
            list.filter { it.pic != null }
                .map {
                    db.updateItem(
                        this.resources.getIdentifier(
                            it.pic,
                            "drawable",
                            this.packageName
                        ),
                        it.pic!!
                    )
                }
        }
        db.updateShopList { list ->
            list.map {
                db.updateShop(
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
            false to Screen(ident = false),
            true to Screen(ident = true)
        )
        setContent {
            BackHandler(enabled = true) { toolbar.back() }
            GetScreenSize()
            density = LocalDensity.current
            fontFamilyResolver = LocalFontFamilyResolver.current
            state = remember { MutableTransitionState(false) }
            typography = MaterialTheme.typography
            measures = Measures(
                itemHeight = 68.dp,
                mapViewWidth = screenWidth - 8.dp * 2,
                padding = 8.dp,
                tableColumns = 3
            )
            MyGuideTheme {
                //Measures()
                colorScheme = MaterialTheme.colorScheme
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val ident = current.observeAsState()
                    if (ident.value == null)
                        Splash(Modifier.padding(innerPadding))
                    else {
                        Column(Modifier.fillMaxSize().padding(innerPadding)) {
                            Toolbar()
                            Box {
                                val visibleState = remember(ident.value == false) {
                                    MutableTransitionState(!ident.value!!)
                                }
                                val visibleState1 = remember(ident.value == true) {
                                    MutableTransitionState(ident.value!!)
                                }
                                LaunchedEffect(visibleState) {
                                    snapshotFlow { visibleState.currentState == visibleState.targetState }
                                        .collect { isIdle ->
                                            if (isIdle) {
                                                if (!visibleState.targetState) {
                                                    screen[current.value!!]!!.query()
                                                }
                                            }
                                        }
                                }
                                LaunchedEffect(visibleState1) {
                                    snapshotFlow { visibleState1.currentState == visibleState1.targetState }
                                        .collect { isIdle ->
                                            if (isIdle) {
                                                if (!visibleState1.targetState) {
                                                    qqq("END1")
                                                    screen[current.value!!]!!.query()
                                                }
                                            }
                                        }
                                }
                                androidx.compose.animation.AnimatedVisibility(
                                    visibleState = visibleState,
                                    enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
                                    exit = slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
                                ) {
                                    CompositionLocalProvider(
                                        LocalDensity provides
                                                Density(
                                                    density = density.density,
                                                    fontScale = screen[false]!!.vm.scale.observeAsState().value!!
                                                )
                                    ) { Main(screen = screen[false]!!)

                                        Measures()
                                    }
                                }
                                androidx.compose.animation.AnimatedVisibility(
                                    visibleState = visibleState1,
                                    enter = slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth }),
                                    exit = slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
                                ) {
                                    CompositionLocalProvider(
                                        LocalDensity provides
                                                Density(
                                                    density = density.density,
                                                    fontScale = screen[true]!!.vm.scale.observeAsState().value!!
                                                )
                                    ) { Main(screen = screen[true]!!)

                                        Measures()
                                    }
                                }
                            }
                        }
                        val dialog by dialog.observeAsState()
                        if (dialog == true) MyDialog()
                    }
                }
            }
        }
    }
}


@Composable
fun Measures() {
    Column(
        Modifier
            .graphicsLayer {
                translationX = screenWidth.toPx()
            }
            .onGloballyPositioned { coordinates ->
                qqq("MM"+measures.itemHeight+coordinates.size.height.toDp())
               // measures.itemHeight = coordinates.size.height.toDp()
            }
    ) {
        Text(
            "1",
            style = typography.bodyLarge,
            fontSize = typography.bodyLarge.fontSize,
            lineHeight = typography.bodyMedium.fontSize,
        )
        Text(
            "1",
            style = typography.bodyMedium,
            fontSize = typography.bodyMedium.fontSize,
            lineHeight = typography.bodyMedium.fontSize,
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
}