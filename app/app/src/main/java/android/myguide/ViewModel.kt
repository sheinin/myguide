package android.myguide
import androidx.lifecycle.MutableLiveData

class ViewModel() {
    val current = MutableLiveData<Boolean?>(null)
    val dialog = MutableLiveData(false)
    val toolbar = Toolbar()

}

data class Details(
    val id: String,
    val title: String,
    val origin: String?,
    val drawable: Int?,
    val level: Int
)