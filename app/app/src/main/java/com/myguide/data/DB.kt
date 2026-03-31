package com.myguide.data

import android.R.attr.level
import com.myguide.qqq
import com.myguide.toDp
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
                    sx += mx
                    mx = 0
                    my = maxOf(my, y)
                    x = 0
                    y = 0
                }
                if (lv != it.level) {
                    y++
                    x = 0
                    it.lat = y.toDouble()
                    it.lng = x.plus(sx).toDouble()
                    lv = it.level

                }
                else {
                    if (x > 3) {
                        y++
                        x = 0
                        it.lat = y.toDouble()
                        it.lng = x.plus(sx).toDouble()
                        //mx = maxOf(mx, x)
                    } else {
                        it.lat = y.toDouble()
                        it.lng = x.plus(sx).toDouble()
                        x++
                        mx = maxOf(mx, x)
                    }
                }
                qqq("ITEM ${it.title} ${it.level} ${it.lat} ${it.lng} x:$x y:$y mx:$mx sx:$sx lv:$lv")
            }
            qqq("MX$mx $sx MY$my")
            l.map {
                it.lat -= my / 2
                it.lng -= sx.plus(mx) / 2
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


data class Cell(val row: Int, val col: Int)

fun generateNonAdjacentRandomPoints(
    width: Float = 1000f,
    height: Float = 1000f,
    rows: Int = 5,
    cols: Int = 8,
    count: Int = 40, // desired number (max possible may be smaller)
    includeDiagonalsAsAdjacent: Boolean = true,
    random: Random = Random.Default
): List<Pair<Float, Float>> {

    // 1. All possible logical cells
    val allCells = mutableListOf<Cell>()
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            allCells += Cell(r, c)
        }
    }

    // 2. Shuffle for randomness
    allCells.shuffle(random)

    // 3. Helper to check adjacency
    fun isAdjacent(a: Cell, b: Cell): Boolean {
        val dr = kotlin.math.abs(a.row - b.row)
        val dc = kotlin.math.abs(a.col - b.col)
        return if (includeDiagonalsAsAdjacent) {
            // any neighbor in 3x3 block (excluding itself)
            (dr <= 1 && dc <= 1) && !(dr == 0 && dc == 0)
        } else {
            // only up, down, left, right
            (dr + dc == 1)
        }
    }

    val chosen = mutableListOf<Cell>()

    // 4. Greedy selection of non-adjacent cells
    for (cell in allCells) {
        if (chosen.size >= count) break

        val hasAdjacent = chosen.any { existing -> isAdjacent(existing, cell) }
        if (!hasAdjacent) {
            chosen += cell
        }
    }

    // 5. Convert logical grid cells to XY in 1000x1000
    val cellW = width / cols
    val cellH = height / rows

    return chosen.map { cell ->
        val x = (cell.col + 0.5f) * cellW
        val y = (cell.row + 0.5f) * cellH
        x to y
    }
}