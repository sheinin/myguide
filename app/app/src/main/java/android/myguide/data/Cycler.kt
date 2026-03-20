package android.myguide.data

import android.myguide.batch
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.plus

class Cycler {
    data class XY(
        var x: Int = 0,
        var y: Int = 0,
        var d: Int = 0,
        var h: Int = 0,
        var i: Int = -1
    )
    private val _description = MutableStateFlow<List<AnnotatedString?>>(emptyList())
    private val _details = MutableStateFlow<List<Details>>(emptyList())
    private val _toggle = MutableStateFlow<List<Boolean?>>(emptyList())
    private val _xy = MutableStateFlow<List<XY?>>(emptyList())
    val description = _description.asStateFlow()
    val toggle = _toggle.asStateFlow()
    val details = _details.asStateFlow()
    val xy = _xy.asStateFlow()
    init { reset() }
    fun reset() {
        _description.value = emptyList()
        _details.value = emptyList()
        _toggle.value = emptyList()
        _xy.value = emptyList()
        repeat(batch) {
            _description.value += null
            _details.value +=
                Details(
                    title = "",
                    origin = null,
                    drawable = null,
                    level = 0
                )
            _toggle.value += null
            _xy.value += XY(0, 0, 0, 0)
        }
    }
    fun update(index: Int, description: AnnotatedString?) {
        _description.update {
            it.mapIndexed { ix, it ->
                if (ix == index) description
                else it
            }
        }
    }
    fun update(index: Int, details: Details) {
        _details.update {
            it.mapIndexed { ix, it ->
                if (ix == index)
                    it.copy(
                        title = details.title,
                        origin = details.origin,
                        drawable = details.drawable,
                        level = details.level
                    )
                else it
            }
        }
    }
    fun update(index: Int, toggle: Boolean?) {
        _toggle.update {
            it.mapIndexed { ix, it ->
                if (ix == index) toggle
                else it
            }
        }
    }
    fun update(index: Int, xy: XY?) {
        _xy.update {
            it.mapIndexed { ix, it ->
                if (ix == index)
                    it?.copy(
                        x = xy!!.x,
                        y = xy.y,
                        d = xy.d,
                        h = xy.h,
                        i = xy.i
                    )
                else it
            }
        }
    }
}