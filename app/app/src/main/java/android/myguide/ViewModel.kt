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
        private val _expand = MutableStateFlow<List<AnnotatedString?>>(emptyList())
        val expand = _expand.asStateFlow()
        private val _more = MutableStateFlow<List<Boolean?>>(emptyList())
        val more = _more.asStateFlow()
        private val _details = MutableStateFlow<List<Details>>(emptyList())
        val details = _details.asStateFlow()
        private val _xy = MutableStateFlow<List<XY>>(emptyList())
        val xy = _xy.asStateFlow()
        init { reset() }
        fun reset() {
            _expand.value = emptyList()
            _details.value = emptyList()
            _more.value = emptyList()
            _xy
            repeat(batch) {
                _expand.value += null
                _details.value +=
                    Details(
                        id = "",
                        title = "",
                        origin = null,
                        description = null,
                        drawable = null,
                        level = 0
                    )
                _more.value += null
                _xy.value += XY(0.dp, 0.dp, 0.dp, 0.dp)
            }
        }
        fun updateExpandable(index: Int, e: AnnotatedString?) {
            _expand.update {
                it.mapIndexed { ix, it ->
                    if (ix == index) e
                    else it
                }
            }
        }
        fun updateItem(index: Int, details: Details) {
            _details.update {
                it.mapIndexed { ix, it ->
                    if (ix == index)
                        it.copy(
                            id = details.id,
                            title = details.title,
                            origin = details.origin,
                            description = details.description,
                            drawable = details.drawable,
                            level = details.level
                        )
                    else it
                }
            }
        }
        fun updateMore(index: Int, more: Boolean?) {
            _more.update {
                it.mapIndexed { ix, it ->
                    if (ix == index) more
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
        val display = MutableLiveData(Settings.Display.LIST)
        val dialog = MutableLiveData(false)
        val cycler = Cycler()
        val position = MutableLiveData(0.dp)
        val w = MutableLiveData(0.dp)
        val h = MutableLiveData(0.dp)
        val item = MutableLiveData<Details>()
        private val _measures = MutableStateFlow<List<Pair<Int, String?>>>(emptyList())
        val measures = _measures.asStateFlow()
        fun measure(strings: List<Pair<Int, String?>>) {
            strings.map { l -> _measures.update { it + l } }
            qqq("updated")
        }
        fun clear() { _measures.update { emptyList() } }

    }
    val current = MutableLiveData<Boolean?>(null)
    val toolbar = Toolbar()
    val showSplash = MutableLiveData(true)
    val mapShowing = MutableLiveData(true)
    val screen = mapOf(false to Screen(), true to Screen())
    fun fetchItems(callback: (List<ListInterface>) -> Unit) {
        repository.getItems { list ->
            callback.invoke(list.map { it.toInterface() }.toList())
        }
    }
    fun fetchItems(id: String, callback: (List<ListInterface>) -> Unit) {
        repository.getItems(id) { list ->
            callback.invoke(list.map { it.toInterface() }.toList())
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
    val description: String?,
    val drawable: Int?,
    val level: Int
)