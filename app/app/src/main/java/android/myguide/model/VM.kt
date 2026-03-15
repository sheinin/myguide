package android.myguide.model

import android.myguide.Details
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.Dimension.Companion.ratio
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VM : ViewModel() {
    enum class Display {
        D3,
        LIST,
        MAP;
        val isMap: Boolean
            get() = this == MAP
    }
    fun ratioH(): Float = ratioV.value ?: ratio.value!!
    fun ratioV(): Float = ratioV.value ?: ratio.value!!
    val adjust = MutableLiveData(false)
    val display = MutableLiveData(Display.LIST)
    val cycler = Cycler()
    val filter = MutableLiveData<Boolean?>(null)
    val position = MutableLiveData(0.dp)
    val ratio = MutableLiveData(1f)
    val ratioH = MutableLiveData<Float?>(null)
    val ratioV = MutableLiveData<Float?>(null)
    val sort = MutableLiveData(false)
    val stateY = MutableLiveData(0)
    val w = MutableLiveData(0.dp)
    val h = MutableLiveData(0.dp)
    val description = MutableLiveData<String>()
    val details = MutableLiveData<Details>()
}