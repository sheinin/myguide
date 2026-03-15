package android.myguide.model

import android.myguide.Details
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VM {
    enum class Display {
        T,
        V,
        H;
    }
    fun ratioH(): Float = ratioV.value ?: ratio.value!!
    fun ratioV(): Float = ratioV.value ?: ratio.value!!
    val adjust = MutableLiveData(false)
    val display = MutableLiveData(Display.V)
    val cycler = Cycler()
    val filter = MutableLiveData<Boolean?>(null)
    val position = MutableLiveData(0.dp)
    val ratio = MutableLiveData(1f)
    val ratioH = MutableLiveData<Float?>(null)
    val ratioV = MutableLiveData<Float?>(null)
    val sort = MutableLiveData(false)
    val stateX = MutableLiveData(0f)
    val stateY = MutableLiveData(0f)
    val w = MutableLiveData(0.dp)
    val h = MutableLiveData(0.dp)
    val description = MutableLiveData<String>()
    val details = MutableLiveData<Details>()
}