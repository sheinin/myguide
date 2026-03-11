package android.myguide

import android.myguide.QueryType.ITEM
import android.myguide.QueryType.ITEMS
import android.myguide.QueryType.SHOP
import android.myguide.QueryType.SHOPS
import androidx.compose.ui.unit.dp


class Screen(
    val activity: MainActivity,
    override val ident: Boolean
) : Toolbar.ScreenTools {
    private val bind = vm.screen[ident]!!
    val render = Render(
        activity = activity,
        screen = this
    )
    private var id: String? = null
    var queryType: QueryType? = null
    override fun build(
        id: String?,
        display: ViewModel.Screen.Display,
        queryType: QueryType,
    ) {
        this.id = id
        this.queryType = queryType
        qqq(
            "SCREEN BUILD query:" + queryType
                    + " ident:" +ident+vm.toolbar.items.last().ident
                    + " id:" + id
                    + " display:" + display
            + " position:" + vm.toolbar.items.last().position
        )
        when (queryType) {
            ITEM ->
                vm.fetchItemDetails(id!!) {
                    bind.description.postValue(it.description)
                    bind.details.postValue(
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
                vm.fetchShopDetails(id!!) {
                    bind.description.postValue(it.description)
                    bind.details.postValue(
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
                bind.description.value = null
                bind.details.value = null
            }
        }
        bind.cycler.isMap.value = display.isMap
        bind.display.value = display
        bind.position.value = 0.dp
        bind.clear()
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
        bind.cycler.reset()
        when (queryType) {
            ITEM -> vm.fetchShops(id!!, ::callback)
            ITEMS -> vm.fetchTree(::callback)
            SHOP -> vm.fetchTree(id!!, ::callback)
            SHOPS -> vm.fetchShops(::callback)
            else -> {}
        }
    }

    override fun reset() {
        render.listen(false)
    }
    override fun update() {
        qqq("UPDATE SCREEN " + ident + " " + vm.toolbar.items.last().position)
        bind.position.postValue(vm.toolbar.items.last().position)
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