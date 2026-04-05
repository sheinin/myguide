package com.myguide


import android.content.Context
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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.myguide.UI.ITEM_HEIGHT
import com.myguide.UI.TITLE_HEIGHT
import com.myguide.data.DB
import com.myguide.data.Repository
import com.myguide.data.StoreDatabase
import com.myguide.ui.theme.MyGuideTheme
import com.myguide.views.Main
import com.myguide.views.MyDialog
import com.myguide.views.Splash
import kotlinx.coroutines.launch

const val batch = 21
lateinit var colorScheme: ColorScheme
lateinit var db: DB
lateinit var density: Density
lateinit var fontFamilyResolver: FontFamily.Resolver
lateinit var json: String
lateinit var state: MutableTransitionState<Boolean>
lateinit var screen: Map<Boolean, Screen>
lateinit var typography: Typography
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val current = MutableLiveData<Boolean?>(null)
val dialog = MutableLiveData(false)
val sortable = MutableLiveData(false)
val toolbar = Toolbar()
var screenHeight = 0.dp
var screenWidth = 0.dp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //try {
            // Open the file from assets
            val inputStream = assets.open("view.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)

            // Read into buffer and close stream
            inputStream.read(buffer)
            inputStream.close()

            // Convert byte array to String
            json = String(buffer, Charsets.UTF_8)
     //   } catch (e: IOException) {
       //     e.printStackTrace()
         //   null
        //}

        //    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
      //  windowInsetsController.hide(navigationBars())
        //windowInsetsController.hide(statusBars())

        val dao = StoreDatabase.getDatabase(application).storeDao()
        val repository = Repository(dao)
        db = DB(repository)
        lifecycleScope.launch {
            checkFirstRun(this@MainActivity)
        }
        setContent {
            val configuration = LocalConfiguration.current
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
                        false to Screen(context = applicationContext,ident = false),
                        true to Screen(context = applicationContext, ident = true)
                    )
                }
                colorScheme = MaterialTheme.colorScheme
                Scaffold { innerPadding ->
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
