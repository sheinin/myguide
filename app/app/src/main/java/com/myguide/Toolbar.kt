package com.myguide

import androidx.lifecycle.MutableLiveData
import com.myguide.data.Query
import com.myguide.data.VM
import com.myguide.data.VM.Type.V

class Toolbar {
    class Item(
        var id: String?,
        val query: Query,
        val title: String,
        var type: VM.Type,
        var scroll: Int = 0,
        var toggle: List<Boolean>? = null

    )

    val crumbs = mapOf(
        false to MutableLiveData(List(3) { "" }),
        true to MutableLiveData(List(3) { "" })
    )
    var items = mutableListOf<Item>()
    var lock = false
    fun splash() {
        crumbs[false]!!.value = List(3) { "" }
        crumbs[true]!!.value = List(3) { "" }
        current.value = null
        items.clear()
        screen[false]!!.listen(false)
        screen[true]!!.listen(false)
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
        query: Query? = toolbar.items.lastOrNull()?.query?.next,
        title: String
    ) {
        if (lock) return
        lock = true
        qqq(
            "NAVIGATE items:${items.size} id:${id} title:${title} query:${query} ${
                (items.getOrNull(
                    0
                )?.title ?: "")
            }"
        )
        items.mapIndexed { ix, it ->
            if (it.query == query && it.id == id) {
                goto(ix)
                return
            }
        }
        val current = !(current.value ?: true)
        val item =
            Item(
                id = id,
                type = type,
                title = title,
                query = query!!
            )
        cached = true
        crumbs[current]!!.value =
            listOf(
                items.getOrNull(0)?.title ?: "",
                if (items.size > 3) "• • •" else items.getOrNull(1)?.title ?: "",
                if (items.size > 2) items.last().title else ""
            )
        items.add(item)
        screen[current]!!.build()
        screen[!current]!!.listen(false)
    }

    private var cached = true
    fun goto(ix: Int) {
        current.value?.let { screen[it] }?.listen(false)
        val item = items[ix]
        val next = screen[!current.value!!]!!
        qqq("GOTO ${item.title} ${item.scroll} ix:" + ix + " current:" + current.value + " next:" + next.ident + " cache:" + cached + (ix == items.lastIndex.dec()))
        cached = cached && ix == items.lastIndex.dec()
        items.subList(ix.inc(), items.size).clear()
        if (cached) {
            current.value = !current.value!!
            cached = false
            lock = false
            next.listen(true)
        } else {
            crumbs[next.ident]!!.value =
                listOf(
                    if (items.size > 1) items[0].title else "",
                    if (items.size > 4) "• • •"
                    else if (items.size == 2) ""
                    else items.getOrNull(1)?.title ?: "",
                    if (items.size > 3) items[items.lastIndex.dec()].title else ""
                )
            next.build()
        }
    }
}