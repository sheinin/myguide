package android.myguide

import android.myguide.QueryType.*
import android.system.Os.listen
import androidx.compose.runtime.snapshots.Snapshot.Companion.observe
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


class Screen(
    val activity: MainActivity,
    override val ident: Boolean,
 //   val binding: ScreenBinding
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
    //var display: Settings.Display? = null
    var queryType: QueryType? = null


    override fun build(
        id: String?,
        display: Settings.Display,
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
        bind.cycler.isMap.value = display.isMap
        bind.display.value = display
        bind.position.value = 0.dp
        render.reset()
    }

    override fun getPosition(): Dp {
        return 0.dp//screen!!.getPosition()
    }

    override fun query() {
        fun callback(list: List<ListInterface>) {
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
            ITEMS -> vm.fetchItems(::callback)
            SHOP -> vm.fetchItems(id!!, ::callback)
            SHOPS -> vm.fetchShops(::callback)
            else -> {}
        }
        // qqq("SCREEN QUERY " + ident + " " +screen)
    }

    override fun reset() {
        render.listen(false)
     //   if (!binding.vm!!.isMap.value!!) activity.markers.clear()
       // screen?.reset()
    }

    override fun update() {
        qqq("UPDATE SCREEN " + ident + " " + vm.toolbar.items.last().position)
        //observe(disp)
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
            ITEM -> "Available at These Locations:"
            ITEMS -> "All Available Items:"
            SHOP -> "Available Items:"
            SHOPS -> "All Shops:"
        }
}