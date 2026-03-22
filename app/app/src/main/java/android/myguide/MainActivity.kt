package android.myguide


import android.myguide.UI.ITEM_HEIGHT
import android.myguide.UI.TITLE_HEIGHT
import android.myguide.data.DB
import android.myguide.data.Repository
import android.myguide.data.StoreDatabase
import android.myguide.ui.theme.MyGuideTheme
import android.myguide.views.Main
import android.myguide.views.MyDialog
import android.myguide.views.Splash
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.MutableTransitionState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData

const val batch = 21
lateinit var colorScheme: ColorScheme
lateinit var db: DB
lateinit var density: Density
lateinit var fontFamilyResolver: FontFamily.Resolver
lateinit var state: MutableTransitionState<Boolean>
lateinit var screen: Map<Boolean, Screen>
lateinit var typography: Typography
val current = MutableLiveData<Boolean?>(null)
val dialog = MutableLiveData(false)
val toolbar = Toolbar()
var screenHeight = 0.dp
var screenWidth = 0.dp

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
        setContent {
            val configuration = LocalConfiguration.current

            // LaunchedEffect will re-run whenever the orientation changes
            LaunchedEffect(configuration.orientation) {
                toolbar.splash()
            }
            BackHandler(enabled = true) { toolbar.back() }
            GetScreenSize()
            density = LocalDensity.current
            fontFamilyResolver = LocalFontFamilyResolver.current
            state = remember { MutableTransitionState(false) }
            typography = MaterialTheme.typography
            MyGuideTheme {
                Measures {
                    screen = mapOf(
                        false to Screen(ident = false),
                        true to Screen(ident = true)
                    )
                }
                colorScheme = MaterialTheme.colorScheme
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val ident = current.observeAsState()
                    if (ident.value == null)
                        Splash(Modifier.padding(innerPadding))
                    else {
                        Main(innerPadding)
                        val dialog by dialog.observeAsState()
                        if (dialog == true) MyDialog()
                    }
                }
            }
        }
    }
}


@Composable
fun Measures(callback: () -> Unit = {}) {
    Column(
        Modifier
            .graphicsLayer {
                translationX = screenWidth.toPx()
            }
            .onGloballyPositioned { coordinates ->
                qqq("MM"+ITEM_HEIGHT+coordinates.size.height.toDp())
                ITEM_HEIGHT = coordinates.size.height
                callback()
            }
    ) {
        Text(
            "1",
            style = typography.bodyLarge,
            fontSize = typography.bodyLarge.fontSize,
            lineHeight = typography.bodyMedium.fontSize,
            modifier = Modifier.onGloballyPositioned {
                TITLE_HEIGHT = it.size.height
            }
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
