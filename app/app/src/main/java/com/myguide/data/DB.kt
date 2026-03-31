package com.myguide.data

import android.R.attr.level
import com.myguide.qqq
import com.myguide.toDp
import kotlin.collections.forEachIndexed
import kotlin.random.Random

class DB(private val repository: Repository) {
    fun fetchItemDetails(id: String, callback: (Item) -> Unit) {
        repository.getItemDetails(id) {
            callback.invoke(it!!)
        }
    }

    fun fetchShopDetails(id: String, callback: (Shop) -> Unit) {
        repository.getShopDetails(id) {
            callback.invoke(it!!)
        }
    }

    fun fetchShops(callback: (List<ListInterface>) -> Unit) {
        repository.getShops { list ->
            /*          generateNonAdjacentRandomPoints(
                          width = 1000f,
                          height = 1000f,
                          rows = 1000,
                          cols = 1000,
                          count = list.size, // note: may return fewer if impossible
                          includeDiagonalsAsAdjacent = true
                      ).mapIndexed { ix, it ->
                          list[ix].lat = it.first.toDouble() * 10
                          list[ix].lng = it.second.toDouble() * 10
                          qqq("LL ${list[ix].title} ${list[ix].lat} ${list[ix].lng}")
                      }
          */
            callback.invoke(list.map { it.toInterface() }.toList())
        }
    }

    fun fetchShops(id: String, callback: (List<ListInterface>) -> Unit) {
        repository.getShops(id) { list ->
            /*generateNonAdjacentRandomPoints(
                width = 1000f,
                height = 1000f,
                rows = 1000,
                cols = 1000,
                count = list.size, // note: may return fewer if impossible
                includeDiagonalsAsAdjacent = true
            ).mapIndexed { ix, it ->
                qqq(">>> $ix $it")
                list[ix].lat = it.first.toDouble()
                list[ix].lng = it.second.toDouble()
            }*/
            callback.invoke(list.map { it.toInterface() }.toList())
        }
    }

    fun fetchTree(callback: (List<ListInterface>) -> Unit) {
        repository.getTree { list ->
            val l = list.map {
                it.toInterface()
            }.toList()

            var lv = -1
            var x = 0
            var y = 0
            var sx = 0
            var mx = 0
            var my = 0
            l.mapIndexed { i, it ->
                if (it.level == 0) {
                    sx += mx.inc().inc()
                    mx = 0
                    my = maxOf(my, y)
                    x = 0
                    y = 0
                }
                if (lv != it.level || it.origin.isNullOrEmpty()) {
                    y++
                    x = 0
                    it.lat = y.toDouble()
                    it.lng = sx.toDouble()
                    lv = it.level
                }
                else {
                    if (x >= 2) {
                        y++
                        x = 0
                        it.lat = y.toDouble()
                        it.lng = sx.toDouble()
                    } else {
                        x++
                        it.lat = y.toDouble()
                        it.lng = x.plus(sx).toDouble()

                        mx = maxOf(mx, x)
                    }
                }
                qqq("ITEM lvl:${it.level} lng.x:${it.lng} lat.y${it.lat} x:$x y:$y mx:$mx sx:$sx lv:$lv ${it.title} ")
            }
            qqq("MX$mx $sx MY$my")
            l.map {
                it.lat -= my.inc() / 2
                it.lng -= sx.inc().plus(mx) / 2
                qqq("LL lat:${it.lat} lng:${it.lng} ${it.title} ${it.level}")
            }



            l.map {
                //  it.lat -= my / 2
             //   it.lng -= sx.plus(mx) / 2
                qqq("LL lat:${it.lat} lng:${it.lng} ${it.title} ${it.level}")
            }
            qqq("M/M lat:${l.minOf { it.lat }}/${l.maxOf { it.lat }} lng:${l.minOf { it.lng }}/${l.maxOf { it.lng }}")
            callback.invoke(l)
        }
    }

    fun fetchTree(id: String, callback: (List<ListInterface>) -> Unit) {
        repository.getTree(id) { list ->
            callback.invoke(list.map { it.toInterface() }.toList())
        }
    }

    fun updateItemList(callback: (List<Item>) -> Unit) {
        repository.getItems {
            callback.invoke(it)
        }
    }

    fun updateShopList(callback: (List<Shop>) -> Unit) {
        repository.getShops {
            callback.invoke(it)
        }
    }

    fun updateItem(drawable: Int, pic: String) {
        repository.updateItem(drawable, pic)
    }

    fun updateShop(drawable: Int, id: String) {
        repository.updateShop(drawable, id)
    }
}

