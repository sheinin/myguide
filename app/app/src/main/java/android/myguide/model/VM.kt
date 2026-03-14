package android.myguide.model

import android.myguide.Details
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData

class VM {
    enum class Display {
        D3,
        LIST,
        MAP;
        val isMap: Boolean
            get() = this == MAP
    }
    val display = MutableLiveData(Display.LIST)
    val cycler = Cycler()
    val filter = MutableLiveData<Boolean?>(null)
    val position = MutableLiveData(0.dp)
    val sort = MutableLiveData(false)
    val stateY = MutableLiveData(0)
    val w = MutableLiveData(0.dp)
    val h = MutableLiveData(0.dp)
    val description = MutableLiveData<String>()
    val details = MutableLiveData<Details>()
}