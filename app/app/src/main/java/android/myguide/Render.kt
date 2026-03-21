package android.myguide


import android.myguide.Expandable.expandable
import android.myguide.Expandable.expanded
import android.myguide.Expandable.static
import android.myguide.UI.BUTTON
import android.myguide.UI.COLUMNS
import android.myguide.UI.ITEM_HEIGHT
import android.myguide.UI.MARGIN
import android.myguide.UI.TITLE_HEIGHT
import android.myguide.UI.mapViewWidth
import android.myguide.data.Cycler.XY
import android.myguide.data.Details
import android.myguide.data.ListInterface
import android.myguide.data.VM
import android.myguide.data.VM.Type.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.getOrNull
import kotlin.collections.set
import kotlin.collections.withIndex
import kotlin.ranges.until

class Render(private val vm: VM) {
    data class Data(
        var toggle: MutableMap<Int, Pair<Int, Int>> = mutableMapOf(),
        var display: CopyOnWriteArrayList<Pair<Int, Int>>,
        var point: CopyOnWriteArrayList<Int>,
        var ruler: CopyOnWriteArrayList<Int>,
        var stack: IntArray,
        var view: View,
    ) {
        data class View(
            var details: MutableList<Details>,
            var expand: MutableList<Pair<Boolean, AnnotatedString>>,
            var toggle: MutableList<Boolean>,
            var xy: MutableList<XY?>
        )
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Data
            if (display != other.display) return false
            if (ruler != other.ruler) return false
            if (!stack.contentEquals(other.stack)) return false
            if (view != other.view) return false
            return true
        }
        override fun hashCode(): Int {
            var result = display.hashCode()
            result = 31 * result + ruler.hashCode()
            result = 31 * result + stack.contentHashCode()
            result = 31 * result + view.hashCode()
            return result
        }
    }
    val data = Data(
        point = CopyOnWriteArrayList(),
        ruler = CopyOnWriteArrayList(),
        stack = IntArray(batch) { -1 },
        view = Data.View(
            details = mutableListOf(),
            expand = mutableListOf(),
            toggle = mutableListOf(),
            xy = mutableListOf()
        ),
        display = CopyOnWriteArrayList()
    )
    var list: List<ListInterface> = listOf()
    var scroll = 0
    private var margin = 1f
    private var handler: VM.Type? = null
    init {
        vm.adjust.observeForever {
            if (!it) {
                data.stack = IntArray(batch) { -1 }
                return@observeForever
            }
            adjust()
        }
        vm.filter.observeForever {
            filter()
            ruler()
            data.point.indices.map { xy(it) }
            data.stack = IntArray(batch) { -1 }
        }
        vm.sort.observeForever { sort() }
        vm.ratio.observeForever { zoom() }
        vm.ratioH.observeForever { zoom() }
        vm.ratioV.observeForever { zoom() }
        vm.scale.observeForever {
          //  ruler()
            zoom()
        }
        vm.type.observeForever {
            if (data.point.isNotEmpty()) display()
        }
        CoroutineScope(Dispatchers.IO).launch {
            vm.margin.collect { m ->
                margin = m
                data.point.map { index ->
                    data.display[index] =
                        (if (data.toggle[index] != null) BUTTON else ITEM_HEIGHT) +
                                margin() to measure(index)
                }
                ruler()
                zoom()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(15)
                scroll()
            }
        }
    }
    private fun ex(index: Int): AnnotatedString {
        qqq(">"+(data.view.expand[index].first)+index)
        if (!data.view.expand[index].first) {
            data.display[index] =
                data.display[index].first to measure(index)
            return expandable(
                index = index,
                level = list[index].level,
                margin = margin,
                ratioH = vm.ratioH(),
                ratioV = vm.ratioV(),
                scale = vm.scale.value!!,
                txt = list[index].description
            )
        }
        val s = list[index].description!! + " \u2026"
        val p =
            androidx.compose.ui.text.Paragraph(
                text = s,
                style = typography.bodySmall,
                spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * vm.ratioV() * vm.scale.value!!,
                        ),
                        0,
                        s.length
                    )
                ),
                constraints = Constraints(
                    maxWidth = (
                            (screenWidth.toPx() - (
                                    ITEM_HEIGHT +
                                            margin().times(4) +
                                            margin().times(2) * list[index].level
                                    ) * vm.ratioH())
                            ).toInt()
                ),
                density = density,
                fontFamilyResolver = fontFamilyResolver,
            )
        data.display[index] =
            data.display[index].first to
                    measure(index) + p.getLineHeight(1).toInt() * p.lineCount.minus(2)
        return expanded(
            index = index,
            ratioV = vm.ratioV(),
            txt = list[index].description!!
        )
    }
    private fun adjust() {
        qqq("ADJUST")
        when (vm.type.value!!) {
            V -> {
                var height = 0
                data.point.mapIndexed { ix, index ->
                    data.display[index] =
                        data.display[index].first to data.display[index].second + measure(index)
                    data.view.expand[index] = data.view.expand[index].first to ex(index)
                    data.ruler[ix] = height
                    height += data.display[index].first + data.display[index].second
                    xy(ix)
                }
                vm.w.postValue(screenWidth.toPx().toInt())
                vm.h.postValue(height)
                data.stack = IntArray(batch) { -1 }
            }
            H ->
                data.point
                    .map {
                        xy(it)
                    }
            T ->
                data.point
                    .map { xy(it) }
        }
    }
    private fun display() {
        handler = vm.type.value
        toolbar.items.lastOrNull()?.type = vm.type.value!!
        when (vm.type.value!!) {
            H -> {
                vm.w.value = (mapViewWidth * data.ruler.size * vm.ratioH()).toInt()
                vm.h.value = ((ITEM_HEIGHT + MARGIN * 2) * vm.ratioV()).toInt()
                data.point.mapIndexed { ix, it ->
                    data.view.expand[it] = false to static(vm.ratioV(), list[it].description!!)
                    xy(ix)
                }
            }
            T -> {
                list.indices.map {
                    xy(it)
                }
                vm.w.value = screenWidth.toPx().toInt()
                vm.h.value = ((ITEM_HEIGHT.times(2)) * (data.point.size / COLUMNS) * vm.ratioV()).toInt()
            }
            V -> {
                data.point.indices.map {
                    data.view.expand[it] = false to expandable(
                        index = it,
                        level = list[it].level,
                        margin = margin,
                        ratioH = vm.ratioH(),
                        ratioV = vm.ratioV(),
                        scale = vm.scale.value!!,
                        txt = list[it].description
                    )

                    xy(it)
                }
                vm.w.value = screenWidth.toPx().toInt()
            }
        }
        data.stack = IntArray(batch) { -1 }
        qqq("DIS")
    }
    private fun filter() {
        val toggle = data.toggle
        data.point.clear()
        data.point.addAll(
            data.display
                .withIndex()
                .filter {
                    !toggle.any { c ->
                        c.value.first < it.index &&
                                c.value.first + c.value.second.unaryMinus() > it.index
                    } && (
                        vm.filter.value == null ||
                            vm.filter.value == true &&
                            list[it.index].lng!! > 0 ||
                            vm.filter.value == false &&
                            list[it.index].lng!! < 0
                    )
                }
                .map { it.index }
                .toList()
        )
    }
    private fun margin(): Int = (MARGIN * margin).toInt()
    private fun measure(ix: Int): Int {
        if (list[ix].description == null || vm.type.value == H) return 0
        val s = list[ix].title!!.trim()
        val p = androidx.compose.ui.text.Paragraph(
            text = s,
            style = typography.bodyLarge,
            constraints = Constraints(
                maxHeight = Int.MAX_VALUE,
                maxWidth =
                    max(
                        MARGIN,
                        (screenWidth.toPx().toInt() - (
                                ITEM_HEIGHT +
                                        margin().times(4) +
                                        margin() * list[ix].level / 2
                                ) * vm.ratioH()).toInt()
                    )
            ),
            density = Density(
                density = density.density,
                fontScale = 1f * vm.scale.value!! * vm.ratioV()
            ),
            fontFamilyResolver = fontFamilyResolver,
        )
        if (p.lineCount > 1)
            return TITLE_HEIGHT * p.lineCount.dec()
        return 0
    }
    private fun ruler() {
        when (vm.type.value!!) {
            T -> {}
            V -> {
                data.ruler.clear()
                var height = 0
                data.point.map {
                    data.ruler.add(height)
                    height += data.display[it].first + data.display[it].second
                }
                vm.w.postValue(screenWidth.toPx().toInt())
                vm.h.postValue(height)
            }
            H -> {
                vm.w.postValue((mapViewWidth * data.point.size * vm.ratioH()).toInt())
                vm.h.postValue((ITEM_HEIGHT * vm.ratioH()).toInt())
            }
        }
    }
    private fun scroll() {
        when (handler) {
            H -> {
                val mx = data.stack.max()
                val mn = data.stack.min()
                val r =
                    max(
                        (scroll / ((mapViewWidth + margin()) * vm.ratioH())).toInt(),
                        0
                    )
                qqq("ECHO MAP ${scroll.toDp().round()} mn/mx:$mn/$mx r:$r ${data.stack.toList()}")

                (r -  batch / 2 until r + batch / 2)
                    .filter { it !in data.stack }
                    .map { syncY(it) }

            }
            T, V -> {
                val r = data.ruler
                    .indexOfFirst {
                        it * vm.ratioV() * vm.scale.value!! > scroll
                    }
                fun down() {
                    var i = 0
                    while (i < batch / 2) {
                        val down = r - i
                        if (down >= 0 && !data.stack.contains(down)) {
                            syncY(down)
                            break
                        }
                        i += 1
                    }
                }
                fun up() {
                    var i = 0
                    while (i < batch / 2) {
                        val up = r + i
                        if (up in 0 .. data.point.lastIndex &&
                            !data.stack.contains(up)
                        ) {
                            syncY(up)
                            break
                        }
                        i += 1
                    }
                }
                //qqq("SCROLL r:${r} ${scroll.toDp().round()} S:${data.stack.map { it }.toList()}")
                down()
                up()
            }
            null -> {}
        }
    }
    private fun sort() {
        val comparator = compareBy<String> { it }
        val finalComparator = if (vm.sort.value!!) comparator.reversed() else comparator
        data.point =
            CopyOnWriteArrayList(
                data.point
                    .sortedWith { a, b ->
                        finalComparator.compare(
                            list[a].title, list[b].title
                        )
                    }
            )
        ruler()
        data.point.indices.map { xy(it) }
        data.stack = IntArray(batch) { -1 }
    }
    private fun zoom() {
        when (vm.type.value!!) {
            T -> {
                val from =
                    max(
                        data.point
                            .withIndex()
                            .indexOfLast { ITEM_HEIGHT * vm.ratioV() * it.index < scroll }
                                - batch / 4,
                        0
                    )
                (from until min(from + batch, data.point.size)).map { xy(it) }
            }
            V -> {
                val from = max(0, data.ruler.indexOfFirst { it >= scroll } - batch / 3)
                var sum = 0
                (from until min(from + batch, data.point.size)).map { ix ->
                    val index = data.point[ix]
                    val m = data.display[index].second
                    data.view.expand[index] =
                        data.view.expand[index].first to ex(index)
                    //qqq("Z index:$index ix:$ix sum:$sum m:$m ${data.display[index].second} ${(data.display[index].second > m)}/${(data.display[index].second < m)} ${data.view.expand[index]}")
                    data.ruler[ix] += sum
                    if (data.display[index].second > m)
                        sum += data.display[index].second - m
                    else if (data.display[index].second < m)
                        sum -= m - data.display[index].second
                    data.view.xy[index] =
                        XY(
                            x = 0,
                            y = (data.ruler[ix]).toInt(),
                            h = data.display[index].first + data.display[index].second,
                            w = screenWidth.toPx().toInt(),
                            i = ix
                        )
                    //vm.cycler.update(ix.mod(batch), data.view.expand[index].second)
                    //vm.cycler.update(mod, data.view.xy[index]!!)
                }
            }
            H -> {
                val from =
                    max(
                        (scroll / (mapViewWidth + margin()) / vm.ratioH()
                        ).toInt() - batch / 4,
                        0
                    )

                (from until min(from + batch, data.point.size)).map {
                    val point = data.point[it]
                    val index = it.mod(batch)
                    xy(point)


                   // expandable(point)
                    vm.cycler.update(index, data.view.expand[point].second)
                  //  vm.cycler.update(index, data.view.xy[point])
                   // listen()
                }
                qqq("MAP $from $scroll ")

            }
        }
    }
    private fun xy(ix: Int) {
        val index = data.point[ix]
        data.view.xy[index] =
            when (vm.type.value!!) {
                T ->
                    XY(
                        x = screenWidth.toPx().toInt() / COLUMNS * ix.mod(COLUMNS),
                        y = (((ITEM_HEIGHT.times(2)) * (ix / COLUMNS)) * vm.ratioV()).toInt(),
                        h = ITEM_HEIGHT * 2,
                        w = screenWidth.toPx().toInt() / COLUMNS,
                        i = ix
                    )
                V ->
                    XY(
                        x = 0,
                        y = data.ruler[ix].toInt(),
                        h = data.display[index].first + data.display[index].second,
                        w = screenWidth.toPx().toInt(),
                        i = ix
                    )
                H ->
                    XY(
                        x = (mapViewWidth * ix * vm.ratioH()).toInt(),
                        y = 0,//((mapViewWidth - margin()) * vm.ratioH()).toInt(),
                        h = ITEM_HEIGHT + MARGIN * 2,
                        w = mapViewWidth,
                        i = ix
                    )
            }
    }

    fun load(list: List<ListInterface>) {
        this.list = list
        handler = vm.type.value
        data.display.clear()
        data.stack = IntArray(batch) { -1 }
        data.toggle.clear()
        data.view.details = MutableList(this@Render.list.size) { Details() }
        data.view.expand = MutableList(this@Render.list.size) { false to buildAnnotatedString {  } }
        data.view.toggle = toolbar.items.last().toggle?.toMutableList() ?: MutableList(this@Render.list.size) { false }
        qqq("LOAD ${data.view.toggle}  "+list.size)
        data.view.xy = MutableList(this@Render.list.size) { null }
        var count = 0
        while (count <= list.lastIndex) {
            if (
                (list.getOrNull(count.inc())?.level ?: -1) > list[count].level
            ) {
                var i =
                    list
                        .withIndex()
                        .indexOfFirst { (ix, it) ->
                            ix > count && it.level <= list[count].level
                                    || ix == list.size
                        }
                if (i == -1) i = list.size
                data.toggle[count] =
                    count to
                    if (data.view.toggle[count]) (i - count).unaryMinus() else (i - count)
            }
            count++
        }
        list.mapIndexed { index, it ->
            data.display.add(
                (if (data.toggle[index] != null) BUTTON else ITEM_HEIGHT) + margin() to
                measure(index)
            )
            data.view.expand[index] = false to expandable(
                index = index,
                level = list[index].level,
                margin = margin,
                ratioH = vm.ratioH(),
                ratioV = vm.ratioV(),
                scale = vm.scale.value!!,
                txt = it.description
            )
            data.view.details[index] =
                Details(
                    title = it.title!!.trim(),
                    origin = it.origin,
                    drawable = it.drawable,
                    level = it.level
                )
        }
        filter()
        ruler()
        data.point.mapIndexed { ix, index ->
            val item = list[index]
            xy(ix)
            data.view.details[index] =
                Details(
                    title = item.title!!.trim(),
                    origin = item.origin,
                    drawable = item.drawable,
                    level = item.level
                )
        }
        scroll = vm.scrollY.value!!
        toolbar.lock = false
    }
    fun listen(listen: Boolean) {
        qqq("LISTEN "+listen)
        handler =
            if (listen) vm.type.value!!
            else null
    }
    private fun syncY(ix: Int) {
        val index = data.point.getOrNull(ix) ?: return
        val mod =
            ix.mod(batch)
            //data.stack.indices.maxByOrNull { abs(data.stack[it] - ix) } ?: 0
        val xy = data.view.xy.getOrNull(index) ?: return
        val toggle = data.view.toggle.getOrNull(index) ?: return
        //qqq("RS ix:$ix index:$index mod:$mod ${xy.x} ${xy.y} ${xy.w} ${xy.h} ${data.view.details.getOrNull(index)?.title}")
        data.stack[mod] = ix
        vm.cycler.update(index = mod, description = data.view.expand[index].second)
        vm.cycler.update(index = mod, details = data.view.details[index])
        vm.cycler.update(index = mod, toggle = toggle)
        vm.cycler.update(index = mod, xy = xy)
    }
    fun expand(index: Int, expand: Boolean) {
        qqq("E "+index + " " + expand + " "+data.point.indexOf(index))

        data.view.expand[index] = expand to ex(index)
        ruler()
        data.point.indices
            .map { xy(it) }
        data.stack
            .map { syncY(it) }
    }
    fun toggle(ix: Int) {
        val index = data.point[ix]
        data.view.toggle[index] = data.toggle[index]!!.second > 0
        data.toggle[index] =
            data.toggle[index]!!.first to data.toggle[index]!!.second.unaryMinus()
        filter()
        ruler()
        data.point.indices
            .map { xy(it) }
        data.stack = IntArray(batch) { -1 }
    }
}