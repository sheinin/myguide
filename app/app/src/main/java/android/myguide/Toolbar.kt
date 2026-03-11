package android.myguide

import android.myguide.QueryType.*
import android.myguide.ViewModel.Screen.*
import android.myguide.ViewModel.Screen.Display.*
import android.view.View
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData

class Toolbar {
    interface ScreenTools {
        fun build(
            id: String? = null,
            display: Display = LIST,
            queryType: QueryType = SHOPS,
        )
        fun query()
        fun reset()
        fun update()
        val ident: Boolean
    }
    class Item(
        var id: String?,
        val queryType: QueryType,
        val title: String,
        var ident: Boolean,
        var display: Display,
        var position: Dp
    )
    private lateinit var activity: MainActivity
    private var next: Screen? = null
    private var tracker = true
    var items = mutableListOf<Item>()
    val crumbs = mapOf(
        false to MutableLiveData(List(3) { "" }),
        true to MutableLiveData(List(3) { "" })
    )
    private fun splash() {
       crumbs[false]!!.value = List(3) { "" }
       crumbs[true]!!.value = List(3) { "" }
       items.clear()
       vm.showSplash.value = true
    }
    fun back(vi: View? = null) {
        val i = items.lastIndex.dec()
        if (i == -1) splash()
        else if (vi == null) goto(i)
        else goto(i)
    }
    fun expand(ix: Int, expand: Boolean) {
        screen[items.last().ident]!!.render.expand(ix, expand)
    }
    fun click(ix: Int) {
        when (ix) {
            -1 -> splash()
            1 if items.size > 4 -> vm.screen[tracker]!!.dialog.postValue(true)
            2 if items.size > 4 -> goto(items.lastIndex.dec())
            else -> goto(ix)
        }
    }
    fun init(activity: MainActivity) { this.activity = activity }
    val last: ScreenTools?
        get() =
            if (items.isEmpty()) null
            else screen[items.last().ident]
    fun navigate(
        id: String? = null,
        display: Display = LIST,
        queryType: QueryType = SHOPS,
        title: String
    ) {
        qqq("NAVIGATE items:"+ items.size+" id:"+id+" title:"+title+" query:"+queryType + " "+items.getOrNull(0)?.title ?: "")
        items.mapIndexed { ix, it ->
            if (it.queryType == queryType && it.id == id) {
                goto(ix)
                return
            }
        }
        val current = screen[tracker]
        tracker = !tracker
        val item =
            Item(
                id = id,
                ident = tracker,
                display = display,
                title = title,
                position = 0.dp,
                queryType = queryType
            )
        next = screen[tracker]!!
        crumbs[tracker]!!.value =
            listOf(
                items.getOrNull(0)?.title ?: "",
                if (items.size > 3) activity.resources.getString(R.string.ellipsis) else items.getOrNull(1)?.title ?: "",
                if (items.size > 2) items.last().title else ""
            )
        cached = true
        items.add(item)
        vm.current.value = next!!.ident
        next!!.build(
            id = id,
            display = display,
            queryType = queryType,
        )
        current!!.reset()
    }
    fun pending() {
        sleep {
            next?.query()
            next = null
        }
    }
    private var cached = true
    fun goto(ix: Int) {
        val current = screen[tracker]!!
        val item = items[ix]
        val next = screen[!tracker]!!
        //qqq("GOTO ix:" +ix+" current:" + current.ident +" next:" + next.ident + " cache:"+ cached +( ix == items.lastIndex.dec()))
        cached = cached && ix == items.lastIndex.dec()
        items.subList(ix.inc(), items.size).clear()
        tracker = !tracker
        items.last().ident = next.ident
        vm.current.value = next.ident
        current.reset()
        if (item.display.isMap) vm.mapShowing.value = true
        if (cached) {
            cached = false
            next.update()
        } else {
            crumbs[next.ident]!!.value =
                listOf(
                    if (items.size > 1) items[0].title else  "",
                    if (items.size > 4) activity.resources.getString(R.string.ellipsis)
                    else if (items.size == 2) ""
                    else items.getOrNull(1)?.title ?: "",
                    if (items.size > 3) items[items.lastIndex.dec()].title else ""
                )
            next.build(
                id = item.id,
                display = item.display,
                queryType = item.queryType
            )
            next.query()
        }
    }
}