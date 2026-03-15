package android.myguide

import android.myguide.QueryType.*
import android.myguide.model.VM
import android.myguide.model.VM.Display.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData

class Toolbar {
    class Item(
        var id: String?,
        val queryType: QueryType,
        val title: String,
        var ident: Boolean,
        var display: VM.Display,
        var position: Dp
    )
    var items = mutableListOf<Item>()
    val crumbs = mapOf(
        false to MutableLiveData(List(3) { "" }),
        true to MutableLiveData(List(3) { "" })
    )
    private fun splash() {
       crumbs[false]!!.value = List(3) { "" }
       crumbs[true]!!.value = List(3) { "" }
       items.clear()
       current.value = null
    }
    fun back() {
        val i = items.lastIndex.dec()
        if (i == -1) splash()
        else goto(i)
    }
    fun click(ix: Int) {
        when (ix) {
            -1 -> splash()
            1 if items.size > 4 -> dialog.postValue(true)
            2 if items.size > 4 -> goto(items.lastIndex.dec())
            else -> goto(ix)
        }
    }
    fun navigate(
        id: String? = null,
        display: VM.Display = V,
        queryType: QueryType = SHOPS,
        title: String
    ) {
        qqq("NAVIGATE items:"+ items.size+" id:"+id+" title:"+title+" query:"+queryType + " "+(items.getOrNull(0)?.title ?: ""))
        items.mapIndexed { ix, it ->
            if (it.queryType == queryType && it.id == id) {
                goto(ix)
                return
            }
        }
        val current =  !(current.value ?: true)
        val item =
            Item(
                id = id,
                ident = current,
                display = display,
                title = title,
                position = 0.dp,
                queryType = queryType
            )
        cached = true
        crumbs[current]!!.value =
            listOf(
                items.getOrNull(0)?.title ?: "",
                if (items.size > 3) "• • •" else items.getOrNull(1)?.title ?: "",
                if (items.size > 2) items.last().title else ""
            )
        items.add(item)
        screen[current]!!.build(
            id = id,
            display = display,
            queryType = queryType,
        )
        screen[!current]!!.reset()
    }
    private var cached = true
    fun goto(ix: Int) {
        current.value?.let { screen[it] }?.reset()
        val item = items[ix]
        val next = screen[!current.value!!]!!
        current.value = !current.value!!
        //qqq("GOTO ix:" +ix+" current:" + current.ident +" next:" + next.ident + " cache:"+ cached +( ix == items.lastIndex.dec()))
        cached = cached && ix == items.lastIndex.dec()
        items.subList(ix.inc(), items.size).clear()
        items.last().ident = next.ident
        current.value = next.ident
        if (cached) {
            cached = false
            next.update()
        } else {
            crumbs[next.ident]!!.value =
                listOf(
                    if (items.size > 1) items[0].title else  "",
                    if (items.size > 4) "• • •"
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