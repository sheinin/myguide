package android.myguide

import android.myguide.QueryType.ITEM
import android.myguide.QueryType.ITEMS
import android.myguide.QueryType.SHOP
import android.myguide.QueryType.SHOPS
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp


class Screen(
    val activity: MainActivity,
    override val ident: Boolean
) : Toolbar.ScreenTools {
    private val bind = vm.screen[ident]!!
    private fun getSettings(): Settings {
        val s = Settings().apply {
            display = this.display
            sort = mutableMapOf(
                true to Settings.Sort.DISTANCE,
                false to Settings.Sort.DISTANCE
            )
        }
        return s
    }
    val render = Render(
        activity = activity,
        screen = this,
        getSettings = ::getSettings
    ).also {
    //    it.calibrate(realm.query(Activity::class).find().subList(0, cyclerBatch).toMutableList())
    }
    private var id: String? = null
    var queryType: QueryType? = null
    override fun build(
        id: String?,
        display: Settings.Display,
        details: Details?,
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
        bind.item.value = details
        bind.cycler.isMap.value = display.isMap
        bind.display.value = display
        bind.position.value = 0.dp
        bind.clear()
        render.reset()
    }
    override fun callback(i: Int, a: AnnotatedString) {
        render.data.vm.expand[i] = a
        bind.cycler.updateExpandable(i, a)
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

    override fun start() {
        render.start()
    }
    override fun update() {
        qqq("UPDATE SCREEN " + ident + " " + vm.toolbar.items.last().position)
        bind.position.postValue(vm.toolbar.items.last().position)
        render.listen(true)
    }

}



class Settings {
    enum class Display {
        D3,
        LIST,
        MAP;
        val isMap: Boolean
            get() = this == MAP
    }
    enum class Sort {
        DATE,
        DISTANCE,
        NAME,
        SIZE;
    }
    var display = Display.LIST
    var sort = mutableMapOf(
        true to Sort.DATE,
        false to Sort.DISTANCE
    )
}

enum class QueryType {
    ITEM,
    ITEMS,
    SHOP,
    SHOPS;
    val title: String
        get() = when (this) {
            ITEM -> "Available at These Shops:"
            ITEMS -> "All Items:"
            SHOP -> "Available Items:"
            SHOPS -> "All Shops:"
        }
}