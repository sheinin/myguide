package android.myguide

import android.view.View
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat


class Screen(
    val activity: MainActivity,
    override val ident: Boolean,
 //   val binding: ScreenBinding
) : Toolbar.ScreenTools {
    private val bind = vm.screen[ident]!!
    private fun getSettings(): Settings {
        val s = Settings().apply {
            display = Settings.Display.LIST
            sort = mutableMapOf(
                true to Settings.Sort.DISTANCE,
                false to Settings.Sort.DISTANCE
            )
        }
        return s
    }
    val render = Render(
        activity = activity,
        bind = bind,
    //    view = binding.scroller,
    //    scrollY = binding.scrollY,
    //    scrollX = binding.scrollX,
        //slider = ::slider,
       // getOpenStatus = ::getOpenStatus,
    //    setListIsEmpty = ::listIsEmpty,
        getSettings = ::getSettings
    ).also {
    //    it.calibrate(realm.query(Activity::class).find().subList(0, cyclerBatch).toMutableList())
    }



    override fun build(
        id: String?,
        display: Settings.Display,
        queryType: QueryType,
    ) {
        qqq(
            "SCREEN BUILD query:" + queryType
                    + " ident:" +ident
                    + " id:" + id
                    + " display:" + display
        )
        render.reset()
        bind.cycler.isMap.value = display.isMap
        /*screen = when (queryType.screen) {
            Toolbar.ScreenTypes.GRID -> {
                scrList.scrollAlignment(false)
                scrGrid
            }
            Toolbar.ScreenTypes.MAIN -> {
                scrList.scrollAlignment(false)
                scrMain
            }
            Toolbar.ScreenTypes.LIST -> {
                scrList
            }
            null -> null
        }
        screen?.build(
            id = id,
            display = display,
            placeMapFor = placeMapFor,
            pentaItem = pentaItem,
            queryType = queryType,
            openTabNum = openTabNum,
            timestamp = timestamp
        )

         */
    }


    override fun getPosition(): Dp {
        return 0.dp//screen!!.getPosition()
    }

    override fun query() {
        val l = vm.allItems.value!!.filter { it.info != null }.map {
            val i = it.info?.split("|")!!
            object: ListInterface {
                override val title: String = i.getOrNull(2) ?: ""
                override val subtitle = i.getOrNull(2) ?: ""
                override val description = i.getOrNull(3) ?: ""
            }
        }.toList()
        render.reset()
        render.load(l)
        // qqq("SCREEN QUERY " + ident + " " +screen)
   //     screen?.query()
    }

    override fun reset() {
        render.observe(null)
        render.listen(false)
     //   if (!binding.vm!!.isMap.value!!) activity.markers.clear()
       // screen?.reset()
    }

    override fun update() {
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
    STORE,
    STORES;
}