package android.myguide

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
        data class Item(
            val id: String,
            val title: String,
            val subtitle: String?,
            val description: String?,
            val drawable: Int?,
            var x: Dp,
            var y: Dp,
            val w: Dp,
            val h: Dp
        )
        val item =
            Item(
                id = "",
                title = "",
                subtitle = "",
                description = "",
                drawable = null,
                x = 0.dp,
                y = 0.dp,
                w = 0.dp,
                h = 0.dp
            )
        private val _items = MutableStateFlow<List<Item>>(emptyList())
        val items = _items.asStateFlow()
        init {
            reset()
        }
        fun reset() {
            _items.value = emptyList()
            repeat(batch) {
                _items.value += item
            }
        }
        fun updateItem(index: Int, item: Item) {
            _items.update {
                it.mapIndexed { ix, it ->
                    if (ix == index)
                        it.copy(
                            id = item.id,
                            title = item.title,
                            subtitle = item.subtitle,
                            description = item.description,
                            drawable = item.drawable,
                            x = item.x,
                            y = item.y,
                            w = item.w,
                            h = item.h
                        )
                    else it
                }
            }
        }
    }

    class Screen {
        val display = MutableLiveData(Settings.Display.LIST)
        val cycler = Cycler()
        val x = MutableLiveData(0.dp)
        val y = MutableLiveData(0.dp)
        val w = MutableLiveData(0.dp)
        val h = MutableLiveData(0.dp)
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