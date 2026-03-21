package android.myguide

import android.myguide.QueryType.ITEM
import android.myguide.QueryType.ITEMS
import android.myguide.QueryType.SHOP
import android.myguide.QueryType.SHOPS
import android.myguide.data.Details
import android.myguide.data.ListInterface
import android.myguide.data.VM

class Screen(val ident: Boolean) {
    private var id: String? = null
    val vm = VM()
    val render = Render(vm = vm)
    var queryType: QueryType? = null
    fun build(
        id: String?,
        type: VM.Type,
        queryType: QueryType,
        scrollY: Int = 0
    ) {
        this.id = id
        this.queryType = queryType
        qqq(
            "BUILD qry:$queryType disp:$type ident:$ident id:$id xy:${scrollY.toDp().round()}"
        )
        when (queryType) {
            ITEM ->
                db.fetchItemDetails(id!!) {
                    vm.description.postValue(it.description)
                    vm.details.postValue(
                        Details(
                            title = it.title!!,
                            origin = it.origin,
                            drawable = it.drawable,
                            level = 0
                        )
                    )
                    current.postValue(!(current.value ?: true))
                }
            SHOP ->
                db.fetchShopDetails(this.id!!) {
                    qqq("db"+it.title)
                    vm.description.postValue(it.description)
                    vm.details.postValue(
                        Details(
                            title = it.title,
                            origin = it.origin,
                            drawable = it.drawable,
                            level = 0
                        )
                    )
                    current.postValue(!(current.value ?: true))
                }
            else -> {
                vm.description.value = null
                vm.details.value = null
                current.value = !(current.value ?: true)
            }
        }
     //   render.data.view.toggle = toggle?.toMutableList() ?: mutableListOf()
        vm.type.value = type
        vm.loading.value = true
        vm.h.value = screenHeight.toPx().toInt()
        vm.scrollY.value = scrollY
        vm.h.value = scrollY + screenHeight.toPx().toInt()
        vm.cycler.reset()
    }
    fun query() {
        vm.loading.postValue(false)
        when (queryType) {
            ITEM -> db.fetchShops(id!!, render::load)
            ITEMS -> db.fetchTree(render::load)
            SHOP -> db.fetchTree(id!!, render::load)
            SHOPS -> db.fetchShops(render::load)
            else -> {}
        }
    }
    fun reset() {
        render.listen(false)
    }
    fun update() {
        qqq("UPDATE SCREEN $ident")
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