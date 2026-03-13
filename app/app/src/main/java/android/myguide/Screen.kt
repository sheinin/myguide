package android.myguide

import android.myguide.QueryType.ITEM
import android.myguide.QueryType.ITEMS
import android.myguide.QueryType.SHOP
import android.myguide.QueryType.SHOPS
import android.myguide.ViewModel.Cycler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update


class Screen(
    val activity: MainActivity,
    override val ident: Boolean
) : Toolbar.ScreenTools {
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
        private val _measures = MutableStateFlow<List<Pair<Int, String?>>>(emptyList())
        fun clear() { _measures.update { emptyList() } }
    }
    val vm = VM()
    private var id: String? = null
    val render =
        Render(
            ident = ident,
            vm = vm
        )
    var queryType: QueryType? = null
    override fun build(
        id: String?,
        display: VM.Display,
        queryType: QueryType,
    ) {
        this.id = id
        this.queryType = queryType
        qqq(
            "SCREEN BUILD query:" + queryType
                    + " ident:" +ident+vmm.toolbar.items.last().ident
                    + " id:" + id
                    + " display:" + display
            + " position:" + vmm.toolbar.items.last().position
        )
        when (queryType) {
            ITEM ->
                vmm.fetchItemDetails(id!!) {
                    vm.description.postValue(it.description)
                    vm.details.postValue(
                        Details(
                            id = it.id,
                            title = it.title!!,
                            origin = it.origin,
                            drawable = it.drawable,
                            level = 0
                        )
                    )
                }
            SHOP ->
                vmm.fetchShopDetails(id!!) {
                    vm.description.postValue(it.description)
                    vm.details.postValue(
                        Details(
                            id = it.id,
                            title = it.title,
                            origin = it.origin,
                            drawable = it.drawable,
                            level = 0
                        )
                    )
                }
            else -> {
                vm.description.value = null
                vm.details.value = null
            }
        }
        vm.cycler.isMap.value = display.isMap
        vm.display.value = display
        vm.position.value = 0.dp
        vm.clear()
        render.reset()
    }
    override fun query() {
        fun callback(list: List<ListInterface>) {
            var count = 0
            while (count <= list.lastIndex) {
                 if ((list.getOrNull(count.inc())?.level ?: -1) > list[count].level) {
                    var i =
                        list.withIndex().indexOfFirst { (ix, it) ->
                            ix > count &&
                            it.level <= list[count].level || ix == list.size
                        }
                    if (i == -1) i = list.size
                    render.data.collapse[count] = count to i - count
                }
                count++
            }
            val l = mutableListOf<ListInterface>()
            repeat(1) {
                l.addAll(list)
            }
            render.load(l)
        }
        render.reset()
        vm.cycler.reset()
        when (queryType) {
            ITEM -> vmm.fetchShops(id!!, ::callback)
            ITEMS -> vmm.fetchTree(::callback)
            SHOP -> vmm.fetchTree(id!!, ::callback)
            SHOPS -> vmm.fetchShops(::callback)
            else -> {}
        }
    }
    override fun reset() {
        render.listen(false)
    }
    override fun update() {
        qqq("UPDATE SCREEN " + ident + " " + vmm.toolbar.items.last().position)
        vm.position.postValue(vmm.toolbar.items.last().position)
        render.listen(true)
    }
}

enum class QueryType {
    ITEM,
    ITEMS,
    SHOP,
    SHOPS;
    val next: QueryType
        get() = when (this) {
            ITEM -> SHOP
            ITEMS -> ITEM
            SHOP -> ITEM
            SHOPS -> SHOP
        }
    val title: String
        get() = when (this) {
            ITEM -> "Available at These Shops:"
            ITEMS -> "All Items:"
            SHOP -> "Available Items:"
            SHOPS -> "All Shops:"
        }
}