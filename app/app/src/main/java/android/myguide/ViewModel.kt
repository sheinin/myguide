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


class ViewModel(private val repository: Repository) : ViewModel() {

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

    class Screen {
        enum class Display {
            D3,
            LIST,
            MAP;
            val isMap: Boolean
                get() = this == MAP
        }
        val display = MutableLiveData(Display.LIST)
        val dialog = MutableLiveData(false)
        val cycler = Cycler()
        val filter = MutableLiveData<Boolean?>(null)
        val position = MutableLiveData(0.dp)
        val sort = MutableLiveData(false)
        val w = MutableLiveData(0.dp)
        val h = MutableLiveData(0.dp)
        val description = MutableLiveData<String>()
        val details = MutableLiveData<Details>()
        private val _measures = MutableStateFlow<List<Pair<Int, String?>>>(emptyList())
        fun clear() { _measures.update { emptyList() } }
    }
    val adjust = MutableLiveData(false)
    val current = MutableLiveData<Boolean?>(null)
    val mapShowing = MutableLiveData(true)
    val ratio = MutableLiveData(1f)
    val ratioH = MutableLiveData<Float?>(null)
    val ratioV = MutableLiveData<Float?>(null)
    val screen = mapOf(false to Screen(), true to Screen())
    val showSplash = MutableLiveData(true)
    val toolbar = Toolbar()
    fun fetchItemDetails(id: String, callback: (Item) -> Unit) {
        repository.getItemDetails(id) {
            callback.invoke(it!!)
        }
    }
    fun fetchShopDetails(id: String, callback: (Shop) -> Unit) {
        repository.getShopDetails(id) {
            callback.invoke(it!!)
        }
    }
    fun fetchShops(callback: (List<ListInterface>) -> Unit) {
        repository.getShops { list ->
            callback.invoke(list.map { it.toInterface() }.toList())
        }
    }
    fun fetchShops(id: String, callback: (List<ListInterface>) -> Unit) {
        repository.getShops(id) { list ->
            callback.invoke(list.map { it.toInterface() }.toList())
        }
    }
    fun fetchTree(callback: (List<ListInterface>) -> Unit) {
        repository.getTree { list ->
            callback.invoke(list.map {
                it.toInterface()
             }.toList() )
        }
    }
    fun fetchTree(id: String, callback: (List<ListInterface>) -> Unit) {
        repository.getTree(id) { list ->
            callback.invoke(list.map { it.toInterface() }.toList())
        }
    }
    fun updateItemList(callback: (List<Item>) -> Unit) {
        repository.getItems {
            callback.invoke(it)
        }
    }
    fun updateShopList(callback: (List<Shop>) -> Unit) {
        repository.getShops {
            callback.invoke(it)
        }
    }
    fun updateItem(drawable: Int, pic: String) {
        repository.updateItem(drawable, pic)
    }
    fun updateShop(drawable: Int, id: String) {
        repository.updateShop(drawable, id)
    }
}

data class Details(
    val id: String,
    val title: String,
    val origin: String?,
    //val description: AnnotatedString?,
    val drawable: Int?,
    val level: Int
)