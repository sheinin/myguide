package android.myguide

import android.myguide.QueryType.ITEM
import android.myguide.QueryType.ITEMS
import android.myguide.QueryType.SHOP
import android.myguide.QueryType.SHOPS
import android.myguide.model.VM

class Screen(val ident: Boolean) {
    private var id: String? = null
    private var lock = false
    val vm = VM()
    val render =
        Render(
            ident = ident,
            vm = vm
        )
    var queryType: QueryType? = null
    fun build(
        id: String?,
        display: VM.Display,
        queryType: QueryType,
    ) {
        this.id = id
        this.queryType = queryType
        qqq(
            "BUILD " + queryType +"/" + display
                    + " ident:" +ident
                    + " id:" + id
            + " xy:" + toolbar.items.last().position
        )
        when (queryType) {
            ITEM ->
                db.fetchItemDetails(id!!) {
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
                    current.postValue(!(current.value ?: true))
                    sleep { query() }
                }
            SHOP ->
                db.fetchShopDetails(this.id!!) {
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
                    current.postValue(!(current.value ?: true))
                    sleep { query() }
                }
            else -> {
                vm.description.value = null
                vm.details.value = null
                current.value = !(current.value ?: true)
                query()
            }
        }
        this@Screen.lock = false
        vm.display.value = display
        vm.h.value = screenHeight
        vm.stateY.value = 0f
    }
    fun query() {
        if (this@Screen.lock) return
        this@Screen.lock = true
        render.reset()
        qqq("QU $ident $id ${toolbar.items.last().ident}")
       // if (ident != toolbar.items.last().ident) return
        fun callback(list: List<ListInterface>) {
            //qqq("CB"+list.size +ident + " "+toolbar.items.last().ident)
            if (ident != toolbar.items.last().ident) return
            var count = 0
            while (count <= list.lastIndex) {
                 if ((list.getOrNull(count.inc())?.level ?: -1) > list[count].level) {
                    var i =
                        list.withIndex().indexOfFirst { (ix, it) ->
                            ix > count &&
                            it.level <= list[count].level || ix == list.size
                        }
                    if (i == -1) i = list.size
                    render.data.toggle[count] = count to i - count
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
            ITEM -> db.fetchShops(id!!, ::callback)
            ITEMS -> db.fetchTree(::callback)
            SHOP -> db.fetchTree(id!!, ::callback)
            SHOPS -> db.fetchShops(::callback)
            else -> {}
        }
    }
    fun reset() {
        render.listen(false)
    }
    fun update() {
        qqq("UPDATE SCREEN " + ident + " " + toolbar.items.last().position)
        android.myguide.lock = false
        vm.position.postValue(toolbar.items.last().position)
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