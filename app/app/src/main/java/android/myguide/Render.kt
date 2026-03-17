package android.myguide


import android.myguide.Expandable.expandable
import android.myguide.Expandable.expanded
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
import android.myguide.data.VM.Display.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Float.max
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
        var display: CopyOnWriteArrayList<Pair<Dp, Dp>>,
        var point: CopyOnWriteArrayList<Int>,
        var ruler: CopyOnWriteArrayList<Dp>,
        var stack: IntArray,
        var view: View,
    ) {
        data class View(
            var details: MutableList<Details>,
            var expand: MutableList<AnnotatedString?>,
            var toggle: MutableList<Boolean?>,
            var xy: MutableList<XY>
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
    private var margin = 1f
    private var handler: VM.Display? = null
    init {
        vm.adjust.observeForever { adjust ->
            //return@observeForever
            if (!adjust) {
                handler = null
                return@observeForever
            }
            handler = vm.display.value
        //    qqq("ADJ")
            adjust()
        }

        CoroutineScope(Dispatchers.IO).launch {
            vm.margin.collect { m ->
                margin = m
                list.indices.map {
                    data.display[it] =
                        (if (data.toggle[it] != null) BUTTON else ITEM_HEIGHT) +
                                margin() to measure(it)
                }
                ruler()
                zoom()
            }
        }
        vm.sort.observeForever {
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
            data.stack
                .withIndex()
                .filter { it.value != -1 }
                .map {
                    xy(data.stack[it.index])
                    renderYSync(data.stack[it.index])
                }

            qqq("SOR "+data.stack.map { it }.toList())
            qqq("POINT "+data.point)
         //   vm.adjust.postValue(true)
        }
        vm.ratio.observeForever { zoom() }
        vm.ratioH.observeForever { zoom() }
        vm.ratioV.observeForever { zoom() }
        vm.scale.observeForever { zoom() }
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(1)
                scroll()
            }
        }
    }
    private fun adjust() {
        //data.stack = IntArray(batch) { -1 }
        when (vm.display.value!!) {
            V -> {
                var height = 0.dp
                //data.ruler.clear()
                data.point.map { ix ->
                    data.display[ix] = data.display[ix].first to measure(ix)
                    data.view.expand[ix] = expandable(
                        ix = ix,
                        level = list[ix].level,
                        margin = margin,
                        ratioH = vm.ratioH(),
                        ratioV = vm.ratioV(),
                        scale = vm.scale.value!!,
                        txt = list[ix].description
                    )
                    data.ruler[ix] = height
                    height += data.display[ix].first + data.display[ix].second
                    xy(ix)
                }
                vm.w.postValue(screenWidth)
                vm.h.postValue(height)
                data.stack.map { renderYSync(it) }
            }
            H ->
                data.point
                    .map {
                      //  expandable(it)
                        xy(it)
                    }
            T ->
                data.point
                    .map { xy(it) }
        }
    }
    private fun scroll() {
        when (handler) {
            H -> {
                direction ?: return
                val mx = data.stack.max()
                val mn = data.stack.min()
                val r =
                    max(
                        (scroll / ((mapViewWidth + margin()) * vm.ratioH())).toInt(),
                        0
                    )
                val ix =
                    (
                            if (direction ?: true && r < mn) mn.dec()
                            else if (r > mn) mx.inc()
                            else null
                            )
                ix?.also { ix ->
                    val point = data.point.getOrNull(ix) ?: return
                    val index = ix.mod(batch)
                    data.stack[index] = ix
                    vm.cycler.update(index, data.view.details[point])
                    vm.cycler.update(index, data.view.expand[point])
                    vm.cycler.update(index, data.view.xy[point])
                    /*qqq(
                        "MAP dir:$direction mn/mx:$mn/$mx r:$r p:" + point + " ix:" + ix + " " + scroll.round() + " " + " " + data.view.xy[point].x.round() + " " + data.view.details.getOrNull(
                            point
                        )?.title
                    )*/
                }
                if (ix == null) {
                    //qqq("ECHO MAP dir:$direction mn/mx:$mn/$mx r:$r ix:" + ix + " " + scroll.round() + " ")
                    direction = null
                }
            }
            T -> {
                direction ?: return
                fun go(ix: Int) {
                    val point = data.point.getOrNull(ix) ?: return
                    val index = ix.mod(batch)
                    vm.cycler.update(index, data.view.xy[point])
                    data.stack[index] = ix
                }
                val mn = data.stack.min()
                if (direction!!)
                    if (
                        data.point
                            .withIndex()
                            .indexOfFirst {
                                ITEM_HEIGHT * it.index / 2 > scroll
                            } - batch / 4 < mn
                    ) go(mn.dec())
                    else direction = null
                else
                    if (
                        data.point
                            .withIndex()
                            .indexOfLast { ITEM_HEIGHT * it.index / 2 < scroll }
                            .let { it / 2 > mn && it / 2 > batch / 3 }
                    ) go(data.stack.max().inc())
                    else direction = null
            }
            V -> {
                direction ?: return
                fun go(ix: Int) {
                    val index = data.point.getOrNull(ix) ?: return
                    val mod = ix.mod(batch)
                    data.stack[mod] = ix
                    vm.cycler.update(mod, data.view.expand[index])
                    vm.cycler.update(mod, data.view.details[index])
                    vm.cycler.update(mod, data.view.toggle[index])
                    vm.cycler.update(mod, data.view.xy[index])
                    qqq("JOB dir:$direction p:" + index + " ix:" + ix + " " + scroll + " " + data.view.xy[index].y + " " + data.view.details.getOrNull(index)?.title)
                }
                val mn =
                    data.point
                        .indexOfFirst { it in data.stack.map { i -> data.point[i] } }
                    //data.stack.min()
                qqq("MN"+mn)
                if (direction!!)
                    if (
                        data.ruler
                            .indexOfFirst { it > scroll } - batch / 2 < mn
                    ) go(mn.dec())
                    else direction = null
                else
                    if (
                        data.ruler
                            .indexOfLast { it < scroll }
                            .let { it > mn + batch / 2 }
                    ) go(data.point.indexOfLast { it in data.stack.map { i -> data.point[i] } }.inc())
                    else direction = null
            }
            null -> {}
        }
    }
    private fun margin(): Dp = MARGIN * margin
    private fun zoom() {
        when (vm.display.value!!) {
            T -> {
                val from =
                    max(
                        data.point
                            .withIndex()
                            .indexOfLast { ITEM_HEIGHT * vm.ratioV() * it.index < scroll }
                                - batch / 4,
                        0
                    )
                (from until min(from + batch, data.point.size)).map {
                    xy(it)
                    vm.cycler.update(it.mod(batch), data.view.xy[data.point[it]])
                }
            }
            V -> {
                val from = max(0, data.ruler.indexOfFirst { it > scroll } - batch / 3)
                var sum = 0.dp
                (from until min(from + batch, data.point.size)).map { ix ->
                    val index = data.point[ix]
                    val mod = ix.mod(batch)
                    val m = measure(index)
                    qqq("MP "+mod + " "+index +" "+data.ruler.size+" "+ix)
                    data.view.xy[index] =
                        XY(
                            x = 0.dp,
                            y = data.ruler[ix] + sum,
                            d = data.display[index].first,
                            h = m,
                            i = ix
                        )
                    //qqq("Z p:"+point+ " m:" + m + data.display[point].height + " sum:" +sum + " r:"+data.ruler[it])
                    if (data.display[index].second < m)
                        sum += m
                    else if (data.display[index].second > m)
                        sum -= m
                    data.view.expand[index] = expandable(
                        ix = index,
                        level = list[index].level,
                        margin = margin,
                        ratioH = vm.ratioH(),
                        ratioV = vm.ratioV(),
                        scale = vm.scale.value!!,
                        txt = list[index].description
                    )
                    vm.cycler.update(mod, data.view.expand[index])
                    vm.cycler.update(mod, data.view.xy[index])
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
                    vm.cycler.update(index, data.view.expand[point])
                    vm.cycler.update(index, data.view.xy[point])
                   // listen()
                }
                qqq("MAP $from $scroll ")

            }
        }
       // ruler()
    }
    private fun measure(ix: Int): Dp {
        if (list[ix].description == null || vm.display.value == H) return 0.dp
        val s = list[ix].title!!.trim()

        val k = (
                ITEM_HEIGHT +
                        margin().times(4) +
                        margin().times(2) * list[ix].level
                )
        val l = k * vm.ratioH()
        val m = screenWidth - l
        val v = m.toPx().toInt()

        qqq("PARA w:$screenWidth${ITEM_HEIGHT.round()} m:"+margin().round() + " rh:"+vm.ratioH()+" lvl:"+list[ix].level +" k:"+k+" l:"+l+" m:"+m+" v:"+v)
        val p = androidx.compose.ui.text.Paragraph(
            text = s,
            style = typography.bodyLarge,
            constraints = Constraints(
                maxHeight = Int.MAX_VALUE,
                maxWidth =
                    max(
                        MARGIN.toPx(),
                        (screenWidth - (
                            ITEM_HEIGHT +
                            margin().times(4) +
                            margin().times(2) * list[ix].level
                        ) * vm.ratioH()).toPx()
                    ).toInt()
            ),
            density = Density(
                density = density.density,
                fontScale = 1f * vm.scale.value!! * vm.ratioV()
            ),
            fontFamilyResolver = fontFamilyResolver,
        )
        //qqq(ident.toString() + " MEASURE "+p.getLineHeight(0).toDp()
          //      + " "+p.lineCount + s.take(p.getLineEnd(0))+"=="+" "+s)
        if (p.lineCount > 1)
            return TITLE_HEIGHT * p.lineCount.dec()
        return 0.dp
    }
    private fun job() {
        var start = 0
        list.indices.map {
            data.display.add(
                (if (data.toggle[it] != null) BUTTON else ITEM_HEIGHT) +
                        margin() to measure(it)
            )
        }
        filter()
        ruler()
        //qqq("SL "+data.point.size+ident+start + " "+limit + " "+list.size +" "+data.point.size)
        data.point.indices.map {
            data.view.expand[it] = expandable(
                ix = it,
                level = list[it].level,
                margin = margin,
                ratioH = vm.ratioH(),
                ratioV = vm.ratioV(),
                scale = vm.scale.value!!,
                txt = list[it].description
            )
            val ix = data.point[it]
            val item = list[ix]
            //qqq("VM ident:$ident ix:"+ix+ " id:"+disp.type+" "+item.title + " "+disp.height+item.level+ruler +item.description)
            xy(ix)
            data.view.toggle[ix] = data.view.toggle.getOrNull(ix) ?: false
            data.view.details[ix] =
                Details(
                    title = item.title!!.trim(),
                    origin = item.origin,
                    drawable = item.drawable,
                    level = item.level
                )
        }
        start = 0
        when (vm.display.value!!) {
            T ->
                (start until start + batch).map {
                    //vm(data.point[it])
                    //    activity.runOnUiThread { renderYD3(it) }
                }
            H -> (start until start + batch).map { renderX(it) }
            V -> (start until start + batch).map { renderYSync(it) }
        }
        toolbar.lock = false
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
                    }
                }
                .map { it.index }
                .toList()
        )
    }
    private fun xy(index: Int) {
        val ix = data.point[index]
        data.view.xy[ix] =
            when (vm.display.value!!) {
                T ->
                    XY(
                        x = screenWidth / COLUMNS * index.mod(COLUMNS),
                        y = ((ITEM_HEIGHT.times(2)) * (index / COLUMNS)) * vm.ratioV(),
                        d = screenWidth / COLUMNS,
                        h = ITEM_HEIGHT * 2,
                    )
                V ->
                    XY(
                        x = 0.dp,
                        y = data.ruler[index],
                        d = data.display[ix].first,
                        h = data.display[ix].second,
                        i = index
                    )
                H ->
                    XY(
                        x = mapViewWidth * index * vm.ratioH(),
                        y = 0.dp,
                        d = (mapViewWidth - margin()) * vm.ratioH(),
                        h =0.dp,
                    )
            }
    }
    fun display() {
        handler = vm.display.value
        toolbar.items.last().display = vm.display.value!!
        when (vm.display.value!!) {
            H -> {
                vm.w.value = mapViewWidth * data.ruler.size * vm.ratioH()
                vm.h.value = ITEM_HEIGHT * vm.ratioV()
                list.indices.map {
             //       expandable(it)
                    xy(it)
                }
            }
            T -> {
                list.indices.map {
                    xy(it)
                }
                vm.w.value = screenWidth
                vm.h.value = (ITEM_HEIGHT.times(2)) * (data.point.size / COLUMNS) * vm.ratioV()
            }
            V -> {
              //  ruler()
                list.indices.map {
                    expandable(
                        ix = it,
                        level = list[it].level,
                        margin = margin,
                        ratioH = vm.ratioH(),
                        ratioV = vm.ratioV(),
                        scale = vm.scale.value!!,
                        txt = list[it].description
                    )
                    xy(it)
                }
                vm.w.value = screenWidth
               // vm.h.value = height
            }
        }
        (0 until min(batch, data.point.size)).map {
            val point = data.point[it]
            val index = it.mod(batch)
            data.stack[index] = it
            vm.cycler.update(index, data.view.expand[point])
            vm.cycler.update(index, data.view.xy[point])
        }
    }
    fun load(list: List<ListInterface>) {
        //qqq("LOAD $ident "+list.size)
        this.list = list
        handler = vm.display.value
        data.display.clear()
        data.stack = IntArray(batch) { -1 }
        data.toggle.clear()
        data.view.details = MutableList(this@Render.list.size) { Details() }
        data.view.expand = MutableList(this@Render.list.size) { null }
        data.view.toggle = MutableList(this@Render.list.size) { null }
        data.view.xy = MutableList(this@Render.list.size) { XY() }
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
                data.toggle[count] = count to i - count
            }
            count++
        }
        val position = toolbar.items.lastOrNull()?.position ?: return
        if (position > 0.dp) {
            if (vm.display.value == H) {
                vm.w.postValue(position + screenWidth)
                vm.h.postValue(ITEM_HEIGHT)
            } else {
                vm.w.postValue(screenWidth)
                vm.h.postValue(position + screenHeight)
            }
            vm.position.postValue(toolbar.items.last().position)
            job()
        } else job()
    }
    //private var height = 0.dp
    private fun ruler() {
        when (vm.display.value!!) {
            T -> {}
            V -> {
                data.ruler.clear()
                var height = 0.dp
                data.point.map {
                    data.ruler.add(height)
                    height += data.display[it].first + data.display[it].second
                }
                vm.w.postValue(screenWidth)
                vm.h.postValue(height)
            }
            H -> {
                vm.w.postValue(mapViewWidth * data.point.size * vm.ratioH())
                vm.h.postValue(ITEM_HEIGHT * vm.ratioH())
            }
        }
    }
    fun listen(listen: Boolean) {
        handler =
            if (listen) vm.display.value!!
            else null
    }
    var direction: Boolean? = null
    var scroll = 0.dp
    fun observe(scroll: Dp) {
        val s = scroll / vm.ratioV() / vm.scale.value!!
        direction = this.scroll > s
        this.scroll = s
    }
    private fun renderX(ix: Int) {
        val point = data.point.getOrNull(ix) ?: return
        val index = ix.mod(batch)
        data.stack[index] = ix
        //qqq("RX ix:$ix point:"+data.point.getOrNull(ix) + " index:"+index +data.vm[point].title+ "  "+data.vm[point].x)
        vm.cycler.update(index, data.view.details[point])
        vm.cycler.update(index, data.view.expand[point])
        vm.cycler.update(index, data.view.xy[point])
    }
    private fun renderYSync(ix: Int) {
        val index = data.point.getOrNull(ix) ?: return
        val mod = ix.mod(batch)
        ///val disp = data.display.find { it.ordinal == ix }!!
        qqq("RS "+index+" "+ix+" "+data.view.xy[index].y+data.view.details.getOrNull(index)?.title )//+ " "+disp.height + " "+disp.type.height+ " "+data.vm.xy[point].y  + data.vm.expand[point])
        data.stack[mod] = ix
        vm.cycler.update(mod, data.view.expand[index])
        vm.cycler.update(mod, data.view.details[index])
        vm.cycler.update(mod, data.view.toggle[index])
        vm.cycler.update(mod, data.view.xy[index])
    }
    fun expand(ix: Int, expand: Boolean) {
        qqq("E "+ix + " " + expand + " "+data.point.indexOf(ix))
        if (expand) {
            val s = list[ix].description!! + " \u2026"
            val p =
                androidx.compose.ui.text.Paragraph(
                    text = s,
                    style = typography.bodySmall,
                    spanStyles = listOf(
                        AnnotatedString.Range(
                            SpanStyle(
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize * vm.ratioV(),
                            ),
                            0,
                            s.length
                        )
                    ),
                    constraints = Constraints(
                        maxWidth = (
                                (screenWidth - (
                                        ITEM_HEIGHT +
                                                margin().times(4) +
                                                margin().times(2) * list[ix].level
                                        ) * vm.ratioH())
                                ).toPx().toInt()
                    ),
                    density = density,
                    fontFamilyResolver = fontFamilyResolver,
                )
            data.display[ix] =
                data.display[ix].first to
                        data.display[ix].second +
                        p.getLineHeight(1).toDp() * p.lineCount.minus(2)
        }
        else data.display[ix] =
            data.display[ix].first to data.display[ix].second + measure(ix)
        data.view.expand[ix] = expanded(
            ix,
            vm.ratioV(),
            list[ix].description!!
        )
        ruler()
        data.point.indices
            .map { xy(it) }
        data.stack
            .map { renderYSync(it) }
    }
    fun toggle(ix: Int) {
        val point = data.point[data.stack[ix]]
        data.view.toggle[point] = data.toggle[point]!!.second > 0
        data.toggle[point] =
            data.toggle[point]!!.first to data.toggle[point]!!.second.unaryMinus()
        filter()
        ruler()
        data.point.indices
            .map { xy(it) }
        (0 until min(batch, data.point.size))
            .map {
                vm.cycler.update(it, XY(0.dp, 0.dp, 0.dp, 0.dp))
                renderYSync(it)
            }
    }
}