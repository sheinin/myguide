package android.myguide

import android.R.attr.scrollY
import android.myguide.data.VM
import android.myguide.data.VM.Type.*
import androidx.lifecycle.MutableLiveData

class Toolbar {
    class Item(
        var id: String?,
        val queryType: QueryType,
        val title: String,
        var ident: Boolean,
        var type: VM.Type,
        var scrollY: Int = 0
    )
    val crumbs = mapOf(
        false to MutableLiveData(List(3) { "" }),
        true to MutableLiveData(List(3) { "" })
    )
    var items = mutableListOf<Item>()
    var lock = false
    private fun splash() {
        crumbs[false]!!.value = List(3) { "" }
        crumbs[true]!!.value = List(3) { "" }
        current.value = null
        items.clear()
        screen[false]!!.reset()
        screen[true]!!.reset()
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
        type: VM.Type = V,
        queryType: QueryType = screen[current.value!!]!!.queryType!!.next,
      //  scrollY: Float = 0f,
        title: String
    ) {
        if (lock) return
        lock = true
        qqq("NAVIGATE items:${items.size} id:${id} title:${title} query:${queryType} scrollY:${scrollY} ${(items.getOrNull(0)?.title ?: "")}")
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
                type = type,
                title = title,
              //  scrollY = scrollY,
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
            type = type,
            queryType = queryType,
        )
        screen[!current]!!.reset()
    }
    private var cached = true
    fun goto(ix: Int) {

        current.value?.let { screen[it] }?.reset()
        val item = items[ix]
        val next = screen[!current.value!!]!!
        qqq("GOTO ${item.title} ${item.scrollY} ix:" +ix+" current:" + current.value +" next:" + next.ident + " cache:"+ cached +( ix == items.lastIndex.dec()))
       // current.value = !current.value!!
        cached = cached && ix == items.lastIndex.dec()
        items.subList(ix.inc(), items.size).clear()
        //items.last().ident = next.ident
        //current.value = next.ident
        if (cached) {
            current.value = !current.value!!
            cached = false
            lock = false
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
                type = item.type,
                queryType = item.queryType,
                scrollY = item.scrollY
            )
        }
    }
}