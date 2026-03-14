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

    class Cycler {
        val isMap = MutableLiveData(true)
        data class XY(
            var x: Dp,
            var y: Dp,
            var w: Dp,
            var h: Dp
        )
        private val _description = MutableStateFlow<List<AnnotatedString?>>(emptyList())
        val description = _description.asStateFlow()
        private val _toggle = MutableStateFlow<List<Boolean?>>(emptyList())
        val toggle = _toggle.asStateFlow()
        private val _details = MutableStateFlow<List<Details>>(emptyList())
        val details = _details.asStateFlow()
        private val _xy = MutableStateFlow<List<XY>>(emptyList())
        val xy = _xy.asStateFlow()
        init { reset() }
        fun reset() {
            _description.value = emptyList()
            _details.value = emptyList()
            _toggle.value = emptyList()
            _xy
            repeat(batch) {
                _description.value += null
                _details.value +=
                    Details(
                        id = "",
                        title = "",
                        origin = null,
                        //description = null,
                        drawable = null,
                        level = 0
                    )
                _toggle.value += null
                _xy.value += XY(0.dp, 0.dp, 0.dp, 0.dp)
            }
        }
        fun updateExpandable(index: Int, e: AnnotatedString?) {
            _description.update {
                it.mapIndexed { ix, it ->
                    if (ix == index) e
                    else it
                }
            }
        }
        fun updateDetails(index: Int, details: Details) {
            _details.update {
                it.mapIndexed { ix, it ->
                    if (ix == index)
                        it.copy(
                            id = details.id,
                            title = details.title,
                            origin = details.origin,
                            //description = details.description,
                            drawable = details.drawable,
                            level = details.level
                        )
                    else it
                }
            }
        }
        fun updateToggle(index: Int, toggle: Boolean?) {
            _toggle.update {
                it.mapIndexed { ix, it ->
                    if (ix == index) toggle
                    else it
                }
            }
        }
        fun updateXY(index: Int, xy: XY) {
            _xy.update {
                it.mapIndexed { ix, it ->
                    if (ix == index)
                        it.copy(
                            x = xy.x,
                            y = xy.y,
                            w = xy.w,
                            h = xy.h
                        )
                    else it
                }
            }
        }
    }
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
    //val description: AnnotatedString?,
    val drawable: Int?,
    val level: Int
)