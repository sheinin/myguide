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
        val viewItem =
            ViewItem(
                id = "",
                title = "",
                origin = null,
                description = null,
                drawable = null,
                level = 0
            )
        private val _expandable = MutableStateFlow<List<AnnotatedString?>>(emptyList())
        val expandable = _expandable.asStateFlow()
        private val _more = MutableStateFlow<List<Boolean?>>(emptyList())
        val more = _more.asStateFlow()
        private val _items = MutableStateFlow<List<ViewItem>>(emptyList())
        val items = _items.asStateFlow()
        private val _xy = MutableStateFlow<List<XY>>(emptyList())
        val xy = _xy.asStateFlow()
        init { reset() }
        fun reset() {
            _expandable.value = emptyList()
            _items.value = emptyList()
            _more.value = emptyList()
            _xy
            repeat(batch) {
                _expandable.value += null
                _items.value += viewItem
                _more.value += null
                _xy.value += XY(0.dp, 0.dp, 0.dp, 0.dp)
            }
        }
        fun updateExpandable(index: Int, e: AnnotatedString?) {
            _expandable.update {
              //  qqq("UE "+index+" "+e)
                it.mapIndexed { ix, it ->
                    if (ix == index) e
                    else it
                }
            }
        }
        fun updateItem(index: Int, viewItem: ViewItem) {
            _items.update {
                it.mapIndexed { ix, it ->
                    if (ix == index)
                        it.copy(
                            id = viewItem.id,
                            title = viewItem.title,
                            origin = viewItem.origin,
                            description = viewItem.description,
                            drawable = viewItem.drawable,
                            level = viewItem.level
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
        val x = MutableLiveData(0.dp)
        val y = MutableLiveData(0.dp)
        val w = MutableLiveData(0.dp)
        val h = MutableLiveData(0.dp)
        val item = MutableLiveData<ViewItem>()
        private val _measures = MutableStateFlow<List<String?>>(emptyList())
        val measures = _measures.asStateFlow()
        fun measure(list: List<String?>) {
            list.map { l -> _measures.update { it + l } }
        }
        fun clear() { _measures.update { emptyList() } }

    }
    val current = MutableLiveData<Boolean?>(null)

    fun callback(i: Int, a: AnnotatedString) {
        toolbar.last?.callback(i, a)
    }
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

data class ViewItem(
    val id: String,
    val title: String,
    val origin: String?,
    val description: String?,
    val drawable: Int?,
    val level: Int
)