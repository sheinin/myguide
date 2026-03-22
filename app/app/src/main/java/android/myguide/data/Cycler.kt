package android.myguide.data

import android.myguide.batch
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.plus

class Cycler {
    data class XY(
        var x: Int = 0,
        var y: Int = 0,
        var h: Int = 0,
        var w: Int = 0,
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
    fun update(mod: Int, description: AnnotatedString?) {
        _description.update {
            it.mapIndexed { ix, it ->
                if (ix == mod) description
                else it
            }
        }
    }
    fun update(mod: Int, details: Details) {
        _details.update {
            it.mapIndexed { ix, it ->
                if (ix == mod)
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
    fun update(mod: Int, toggle: Boolean?) {
        _toggle.update {
            it.mapIndexed { ix, it ->
                if (ix == mod) toggle
                else it
            }
        }
    }
    fun update(mod: Int, xy: XY?) {
        _xy.update {
            it.mapIndexed { ix, it ->
                if (ix == mod)
                    it?.copy(
                        x = xy!!.x,
                        y = xy.y,
                        h = xy.h,
                        w = xy.w,
                        i = xy.i
                    )
                else it
            }
        }
    }
}