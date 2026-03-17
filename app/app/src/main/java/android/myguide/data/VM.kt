package android.myguide.data

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VM {
    enum class Display {
        T,
        V,
        H;
    }
    fun margin(margin: Float) = _margin.update { margin }
    fun margin(up: Boolean) = _margin.update { if (up) it + .1f else it - .1f }
    fun ratioH(): Float = ratioV.value ?: ratio.value!!
    fun ratioV(): Float = ratioV.value ?: ratio.value!!
    private val _margin = MutableStateFlow(1f)
    val adjust = MutableLiveData(false)
    val display = MutableLiveData(Display.V)
    val cycler = Cycler()
    val filter = MutableLiveData<Boolean?>(null)
    val loading = MutableLiveData(false)
    val margin: StateFlow<Float> = _margin.asStateFlow()
    val position = MutableLiveData(0.dp)
    val ratio = MutableLiveData(1f)
    val ratioH = MutableLiveData<Float?>(null)
    val ratioV = MutableLiveData<Float?>(null)
    val scale = MutableLiveData(1f)
    val sort = MutableLiveData(false)
    val stateX = MutableLiveData(0f)
    val stateY = MutableLiveData(-1f)
    val w = MutableLiveData(0.dp)
    val h = MutableLiveData(0.dp)
    val description = MutableLiveData<String>()
    val details = MutableLiveData<Details>()
}