package android.myguide
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.plus


class ViewModel() {
    val adjust = mapOf(
        false to MutableLiveData(false),
        true to MutableLiveData(false)
    )
    val current = MutableLiveData<Boolean?>(null)
    val dialog = MutableLiveData(false)
    val ratio = MutableLiveData(1f)
    val ratioH = MutableLiveData<Float?>(null)
    val ratioV = MutableLiveData<Float?>(null)
    val toolbar = Toolbar()

}

data class Details(
    val id: String,
    val title: String,
    val origin: String?,
    val drawable: Int?,
    val level: Int
)