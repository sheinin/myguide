package android.myguide

import android.myguide.ViewModel.Screen.Display.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.getOrNull
import kotlin.collections.set
import kotlin.collections.withIndex
import kotlin.ranges.until

class Render(
    val activity: MainActivity,
    val screen: Screen
) {
    val bind = vm.screen[screen.ident]!!
    private val cycler = bind.cycler
    //private var display: Settings.Display = LIST
    private val pics = IntArray(batch) { -1 }
    private val spinners = (0 until batch).associateWith { -1 }.toMutableMap()
    val data = Data(
        point = CopyOnWriteArrayList(),
        ruler = CopyOnWriteArrayList(),
        stack = IntArray(batch) { -1 },
        vm = Data.VM(
            details = mutableListOf(),
            expand = mutableListOf(),
            toggle = mutableListOf(),
            xy = mutableListOf()
        ),
        display = CopyOnWriteArrayList()
    )
    private enum class Handlers {
        FULL,
        MAP,
        NONE;
        fun set(map: Boolean): Handlers {
            return if (map) MAP
            else FULL
        }
        val delay: Long
            get() = if (this == NONE || this == MAP) 1000L else 1L
    }
    private var handler = Handlers.FULL
    private var list: List<ListInterface> = listOf()
    private var job: Job? = null
    private var jobQue = ConcurrentHashMap<Int, Int>()
    private var ordinal = 0
    var offset = 0
    init {
        fun refresh() {
            if (screen.ident != vm.toolbar.last?.ident) return
            qqq("ref "+vm.ratioV.value +"?:"+ vm.ratio.value!!)
            data.stack.withIndex().filter { it.value != -1 }.map { stack ->
                val m = measure(stack.value)
                if (
                    data.display[stack.value].height !=
                    data.display[stack.value].type.height + m
                ) {
                    data.display[stack.value].height = data.display[stack.value].type.height + m
                    ruler()
                    data.stack
                        .filter { it >= stack.value }
                        .map {
                            //xy(it)
                            data.vm.xy[data.point[it]] =
                                ViewModel.Cycler.XY(
                                    x = 0.dp,
                                    y = data.ruler[it],
                                    w = screenWidth,
                                    h = data.display.find { d -> d.ordinal == it }!!.height,
                                )
                            cycler.updateXY(it, data.vm.xy[data.point[it]])
                        }
                }
                expandable(stack.value)
                cycler.updateExpandable(stack.index, data.vm.expand[stack.value])
            }
        }
        vm.adjust.observeForever {
            if (screen.ident != vm.toolbar.last?.ident) list.indices.map { i ->
                data.display[i].height = data.display[i].type.height + measure(i)
                expandable(i)
            } else list.indices.filter { i -> i !in data.stack }.map { i ->
                data.display[i].height = data.display[i].type.height + measure(i)
                expandable(i)
            }
        }
        vm.ratio.observeForever { refresh() }
        vm.ratioH.observeForever { refresh() }
        vm.ratioV.observeForever { refresh() }
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(handler.delay)
                    mapOf(
                        Handlers.FULL to {
                            jobQue.iterator().also {
                                if (it.hasNext()) {
                                    val next = it.next()
                                    cycler.updateExpandable(next.key, data.vm.expand[next.value])
                                    cycler.updateDetails(next.key, data.vm.details[next.value])
                                    cycler.updateToggle(next.key, data.vm.toggle[next.value])
                                    cycler.updateXY(next.key, data.vm.xy[next.value])
                                    pics[next.key] = next.value
                                    it.remove()
                                }
                            }
                        },
                        Handlers.MAP to {},
                        Handlers.NONE to null
                    )[handler]?.invoke()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(500)
                pics.filter { it != -1 }.minOrNull()?.let { ix ->
                    val index = pics.indexOf(ix)
                    activity.runOnUiThread {
                        data.stack.getOrNull(index)?.also { i ->
                            data.point.getOrNull(i)?.let { pt ->
                                data.display.find { it.ordinal == pt }
                            }?.also { d ->
                            ///!!!    picasso(d.lock, d.path, index, ix)

                            }
                            pics[index] = -1
                        }
                    }
                }
            }
        }
    }
    enum class DisplayType {
        DEFAULT,
        NODE;
        val height: Dp
            get() =
                when (this) {
                    DEFAULT -> measures.itemHeight
                    NODE -> 44.dp
                }
    }
    data class Data(
        var collapse: MutableMap<Int, Pair<Int, Int>> = mutableMapOf(),
        var display: CopyOnWriteArrayList<Display>,
        var point: CopyOnWriteArrayList<Int>,
        var ruler: CopyOnWriteArrayList<Dp>,
        var stack: IntArray,
        var vm: VM,
    ) {
        class Display(
            val ordinal: Int,
            var type: DisplayType,
            var height: Dp
        )
        data class VM(
            var details: MutableList<Details>,
            var expand: MutableList<AnnotatedString?>,
            var toggle: MutableList<Boolean?>,
            var xy: MutableList<ViewModel.Cycler.XY>
        )
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Data
            if (display != other.display) return false
            if (ruler != other.ruler) return false
            if (!stack.contentEquals(other.stack)) return false
            if (vm != other.vm) return false
            return true
        }
        override fun hashCode(): Int {
            var result = display.hashCode()
            result = 31 * result + ruler.hashCode()
            result = 31 * result + stack.contentHashCode()
            result = 31 * result + vm.hashCode()
            return result
        }
    }
    private fun ini1(ix: Int) {
        val item = list.getOrNull(ix) ?: return
        val displayType =
            if (item.description == null) DisplayType.NODE else DisplayType.DEFAULT
        data.display.add(
            Data.Display(
                height = displayType.height + measure(ix),
                ordinal = ordinal,
                type = displayType
            )
        )
        //qqq("?? "+item.displayName+ " "+ordinal+" "+px2dp(data.display.last().height)+item.currentTime)//.+" "
        ordinal += 1
    }
    private fun measure(ix: Int): Dp {
        if (list[ix].description == null || bind.display.value == MAP) return 0.dp
        val s = list[ix].title!!.trim()
        val p = androidx.compose.ui.text.Paragraph(
            text = s,
            style = typography.bodyLarge.copy(
                fontSize = typography.bodyLarge.fontSize * (vm.ratioV.value ?: vm.ratio.value!!) 
            ),
            constraints = Constraints(
                maxWidth =
                    (screenWidth - (
                            measures.itemHeight +
                            measures.padding.times(4) +
                            measures.padding.times(2) * list[ix].level
                    ) * (vm.ratioH.value ?: vm.ratio.value!!)).toPx().toInt()),
            density = density,
            fontFamilyResolver = fontFamilyResolver,
        )
        if (p.lineCount > 1) {
       //     qqq(screen.ident.toString() + " MEASURE "
         //           + p.getLineHeight(1).toInt().toDp()
           //         + " " + measures.lineHeight + " "+p.lineCount + s.take(p.getLineEnd(0))+"=="+" "+s)
           // qqq("?"+s.take(p.getLineEnd(1)))
            return p.getLineHeight(1).toDp() * p.lineCount.dec() - 12.dp
        }
        return 0.dp
    }
    private var limit = 0
    private var start = 0
    private fun expandable(ix: Int, expand: Boolean = false) {
        val str = list[ix].description ?: return
        if (expand) {
            data.vm.expand[ix] = buildAnnotatedString {
                withStyle(ParagraphStyle(lineHeight = 1.em * (vm.ratioV.value ?: vm.ratio.value!!))) {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * (vm.ratioV.value ?: vm.ratio.value!!),
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append(list[ix].description) }
                    withStyle(
                        style = SpanStyle(
                            color = Color.Transparent,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * (vm.ratioV.value ?: vm.ratio.value!!),
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append("\u200A") }
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "lastThree",
                            linkInteractionListener = {
                                screen.render.expand(ix, false)
                            }
                        )
                    ) {
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.None,
                                color = colorScheme.primary,
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize * (vm.ratioV.value ?: vm.ratio.value!!),
                                fontWeight = typography.bodySmall.fontWeight
                            )
                        ) {
                            append("\u2026")
                        }
                    }
                }
            }
            return
        }
        fun static(): AnnotatedString =
            buildAnnotatedString {
                withStyle(ParagraphStyle(lineHeight = 1.em * (vm.ratioV.value ?: vm.ratio.value!!))) {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * (vm.ratioV.value ?: vm.ratio.value!!),
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append(str) }
                }
            }
        if (bind.display.value == MAP) {
            data.vm.expand[ix] = static()
            return
        }
        val result = androidx.compose.ui.text.Paragraph(
            text = str,
            style = typography.bodySmall,
            spanStyles = listOf(
                AnnotatedString.Range(
                    SpanStyle(
                        fontStyle = typography.bodySmall.fontStyle,
                        fontSize = typography.bodySmall.fontSize * (vm.ratioV.value ?: vm.ratio.value!!),
                        fontWeight = typography.bodySmall.fontWeight,
                    ),
                    0,
                    str.length
                )
            ),
            constraints = Constraints(
                maxWidth = (
                        (screenWidth - (
                                measures.itemHeight +
                                        measures.padding.times(4) +
                                        measures.padding.times(2) * list[ix].level
                                ) * (vm.ratioH.value ?: vm.ratio.value!!))
                        ).toPx()
                    .toInt()
            ),
            density = density,
            fontFamilyResolver = fontFamilyResolver,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (result.lineCount < 2) {
            data.vm.expand[ix] = static()
            return
        }
        val take =
            str.take(
                result.getLineEnd(
                    1,
                    true
                )
            )
        /*
        qqq(
            screen.ident.toString()
            + " EXPAND " + result.lineCount
            + str.take(result.getLineEnd(0, true))
            + "=="
            + take
            + " == "
            + str
        )


         */

        data.vm.expand[ix] =
            if (take == str) static()
            else
                buildAnnotatedString {
                    withStyle(ParagraphStyle(lineHeight = 1.em * (vm.ratioV.value ?: vm.ratio.value!!))) {
                        withStyle(
                            style = SpanStyle(
                                color = colorScheme.secondary,
                                textDecoration = TextDecoration.None,
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize * (vm.ratioV.value ?: vm.ratio.value!!),
                                fontWeight = typography.bodySmall.fontWeight
                            )
                        ) { append(take.dropLast(1)) }
                        withStyle(
                            style = SpanStyle(
                                color = Color.Transparent,
                                textDecoration = TextDecoration.None,
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize * (vm.ratioV.value ?: vm.ratio.value!!),
                                fontWeight = typography.bodySmall.fontWeight
                            )
                        ) { append("\u200A") }
                        withLink(
                            LinkAnnotation.Clickable(
                                tag = "lastThree",
                                linkInteractionListener = {
                                    vm.toolbar.expand(ix, true)
                                }
                            )
                        ) {
                            withStyle(
                                style = SpanStyle(
                                    textDecoration = TextDecoration.None,
                                    color = colorScheme.primary,
                                    fontStyle = typography.bodySmall.fontStyle,
                                    fontSize = typography.bodySmall.fontSize * (vm.ratioV.value ?: vm.ratio.value!!),
                                    fontWeight = typography.bodySmall.fontWeight,
                                )
                            ) {
                                append("\u2026")
                            }
                        }
                    }
                }
        //qqq("STR "+str)
    }
    private fun job(position: Dp = 0.dp, callback: (() -> Unit)? = null) {
        @Suppress("UNUSED_VARIABLE")
        when (bind.display.value!!) {
            D3 -> {
                offset = 6
                /*(0 until position + screenHeight / DisplayType.D3.height * 3).map {
                    val s = start++
                    ini.invoke(s)
                    ms.invoke(s)
                }
                start = max(start - min(list.size, batch), 0)*/
            }
            LIST -> {
                offset = 6
                val lim = position + screenHeight
                while (
                    data.display
                        .sumOf { h -> h.height.value.toDouble() } < lim.value.toDouble()
                    &&
                    start < list.size
                ) {
                    val s = start++
                    ini1(s)
                    //measure(s)
                }
                //qqq("LIM "+list.size+" "+start +" "+ min(list.size, batch)+" "+lim +" " +position + " "+screenHeight)
                start = max(start - min(list.size, batch), 0)
            }
            MAP -> {
                offset = 5
                val lim = min(list.size, batch / 3)
                while (
                    data.display.size < position / (screenWidth - 90.dp) + lim &&
                    start < list.size
                ) {
                    val s = start++
                    ini1(s)
                    //measure(s)
                }
                start = max(start - lim, 0)
            }
        }
        ruler()
        limit = min(
            start + batch,
            data.point.size
        )
        start = max(0, limit - batch)
        /*if (limit == 0) {
            activity.runOnUiThread { callback?.invoke() }
            (0 until list.size).map { vm(it) }
            return
        }*/
        qqq("SL "+start + " "+limit + " "+list.size)
        CoroutineScope(Dispatchers.IO).launch {
            //delay(10L)
            if (data.point.isEmpty()) return@launch
            when (bind.display.value!!) {
                D3 ->
                    (start until limit).map {
                        vm(data.point[it])
                        delay(1L)
                        //    activity.runOnUiThread { renderYD3(it) }
                    }

                MAP ->
                    (start until limit).map {
                        vm(data.point[it])
                        delay(1L)
                        renderX(it)
                    }
                LIST ->
                    (start until limit).map {
                        expandable(it)
                        vm(data.point[it])
                        delay(1L)
                        renderYSync(it)
                    }
            }
            //////////callback?.invoke()
            (data.display.size until list.size).map {
                ini1(it)
                //measure(it)
            }
            ruler(false)
            data.vm.details.withIndex().filter { it.value.title == "" }.map {
                expandable(it.index)
                vm(it.index)
            }
        }
    }
    fun display() {
        val start = 0
        val end = min(batch / 3, data.ruler.size)
        vm.toolbar.items.last().display = bind.display.value!!
        pics.fill(-1)
        if (bind.display.value == MAP) {
            handler = handler.set(true)
            bind.w.value = (screenWidth - 90.dp) * data.ruler.size
            bind.h.value = measures.itemHeight
            list.indices.map {
                data.vm.expand[it] = buildAnnotatedString {
                    withStyle(ParagraphStyle(lineHeight = 1.em * (vm.ratioV.value ?: vm.ratio.value!!))) {
                        withStyle(
                            style = SpanStyle(
                                color = colorScheme.secondary,
                                textDecoration = TextDecoration.None,
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize * (vm.ratioV.value ?: vm.ratio.value!!),
                                fontWeight = typography.bodySmall.fontWeight
                            )
                        ) { append(list[it].description) }
                    }
                }
            }
            data.vm.xy.mapIndexed { ix, it ->
                it.x = (screenWidth - 90.dp) * ix
                it.y = 0.dp
                it.w = screenWidth - 90.dp
                it.h = data.display[ix].type.height
            }
        } else {
            handler = handler.set(false)
            list.indices.map { expandable(it) }
            data.vm.xy.mapIndexed { ix, it ->
                it.x = 0.dp
                it.y = data.ruler[ix]
                it.w = screenWidth
                it.h = data.display[ix].height
            }
            bind.w.value = screenWidth
            bind.h.value = height
        }
        (start until end).map {
            val point = data.point[it]
            val index = it.mod(batch)
            data.stack[index] = it
            cycler.updateExpandable(index, data.vm.expand[point])
            cycler.updateXY(index, data.vm.xy[point])
        }
    }
    fun load(list: List<ListInterface>, callback: (() -> Unit)? = null) {
        qqq("LOAD "+list.size)
        this.list = list
        ordinal = 0
        handler = handler.set(bind.display.value == MAP)
        data.vm.details = MutableList(this@Render.list.size) {
            Details(
                id = "",
                title = "",
                origin = null,
                drawable = null,
                level = 0
            )
        }
        data.vm.expand = MutableList(this@Render.list.size) { null }
        data.vm.xy = MutableList(this@Render.list.size) {
            ViewModel.Cycler.XY(0.dp, 0.dp, 0.dp, 0.dp)
        }
        data.vm.toggle = MutableList(this@Render.list.size) { null }
        val position = vm.toolbar.items.last().position
        if (position > 0.dp) {
            activity.runOnUiThread {
                if (bind.display.value == MAP) {
                    bind.w.value = position + screenWidth
                    bind.h.value = measures.itemHeight
                } else {
                    bind.w.value = screenWidth
                    bind.h.value = position + screenHeight
                }
                bind.position.value = vm.toolbar.items.last().position
                job(position, callback)
            }
        } else job(callback = callback)
    }
    private var height = 0.dp
    private fun ruler(reset: Boolean = true) {
        if (reset) {
            height = 0.dp
            data.point.clear()
            data.ruler.clear()
        }
        val filter = data.display.filter {
                !data.collapse.any { c ->
                    c.value.first < it.ordinal
                            && c.value.first + c.value.second.unaryMinus() > it.ordinal
                }
        }
        qqq("FIL "+filter.size + " "+data.ruler.size + " "+ list.size)
        val list = filter.subList(data.ruler.size, filter.size)

        if (bind.display.value == D3) {
      /*      list.map { d ->
                data.point.add(d.ordinal)
                data.ruler.add(height + divider)
                if (data.ruler.size.mod(3) == 0) height += DisplayType.D3.height + divider
            }
            val h = filter.size / 3 * (DisplayType.D3.height + divider) + dp2px(16f)
            view.post { view.layoutParams = FrameLayout.LayoutParams(screenWidth,  h) }

       */
        } else {
            list.map { d ->
                data.point.add(d.ordinal)
                data.ruler.add(height)
                height += d.height + 8.dp
            }
            if (bind.display.value == MAP) {
                bind.w.postValue((screenWidth - 90.dp) * data.ruler.size)
                bind.h.postValue(measures.itemHeight)
            }
            else {
                bind.w.postValue(screenWidth)
                bind.h.postValue(height)
            }
        }
    }
    fun listen(listen: Boolean) {
        handler = if (listen) handler.set(bind.display.value == MAP)
        else Handlers.NONE
    }
    fun observe(pos: Dp) {
        when (bind.display.value) {
            LIST ->
                max(data.ruler.indexOfLast { it < pos } - offset, 0).also { f ->
                    (f..min(data.ruler.size, f + batch).dec()).filter {
                        !data.stack.contains(it)
                    }.map { renderY(it) }
                }
            else ->
                max((pos / (screenWidth - 90.dp)).toInt() - batch / 2, 0).also { from ->
                    (from .. min(data.ruler.size, from + batch).dec())
                        .filter { !data.stack.contains(it) }
                        .map { renderX(it) }
                }
        }
    }
    fun reset() {
        data.collapse.clear()
        data.display.clear()
        data.point.clear()
        data.ruler.clear()
        data.stack = IntArray(batch) { -1 }
        data.vm.details.clear()
        data.vm.toggle.clear()
        bind.cycler.reset()
        handler = Handlers.NONE
        height = 0.dp
        job?.cancel()
        jobQue.clear()
        offset = 0
        ordinal = 0
        pics.fill(-1)
        spinners.clear()
    }

    private fun renderX(ix: Int) {
        val point = data.point.getOrNull(ix) ?: return
        val disp = data.display.find { it.ordinal == point } ?: return
        val index = ix.mod(batch)
        data.stack[index] = ix
        //qqq("RX ix:$ix point:"+data.point.getOrNull(ix) + " index:"+index +data.vm[point].title+ "  "+data.vm[point].x)
        cycler.updateDetails(index, data.vm.details[point])
        cycler.updateExpandable(index, data.vm.expand[point])
        cycler.updateXY(index, data.vm.xy[point])
    }
    private fun renderYSync(ix: Int) {
        val point = data.point.getOrNull(ix) ?: return
        val index = ix.mod(batch)

        val disp = data.display.find { it.ordinal == ix }!!
        qqq("RS "+point+" "+ix+" "+data.vm.details.getOrNull(point)?.title + " "+disp.height + " "+disp.type.height+ " "+data.vm.xy[point].y  + data.vm.expand[point])
        data.stack[index] = ix
        cycler.updateExpandable(index, data.vm.expand[point])
        cycler.updateDetails(index, data.vm.details[point])
        cycler.updateToggle(index, data.vm.toggle[point])
        cycler.updateXY(index, data.vm.xy[point])
    }
    private fun renderY(ix: Int) {
        val point = data.point.getOrNull(ix) ?: return
        val disp = data.display.find { it.ordinal == point } ?: return
        val index = ix.mod(batch)
      //  qqq("RF "+disp.ordinal+" "+point+" "+ix+" "+data.vm.getOrNull(point)?.title)
        data.stack[index] = ix
        que(index = index, ix = disp.ordinal)
    }

    private fun que(index: Int, ix: Int) {
        jobQue.remove(index)
        jobQue[index] = ix
      //  activity.picasso.cancelRequest(elements[index].thumbnail)
        spinners[index] = ix
    }
    private fun xy(ix: Int) {

        val disp = data.display.find { it.ordinal == ix }!!
//        qqq("XY "+ix + " "+ data.ruler[ix] + " "+disp.height + " "+disp.type)
        data.vm.xy[ix] = when (bind.display.value) {
            LIST -> {
                ViewModel.Cycler.XY(
                    x = 0.dp,
                    y = data.ruler[ix],
                    w = screenWidth,
                    h = disp.height,
                )
            }
            else ->
                ViewModel.Cycler.XY(
                    x = (screenWidth - 90.dp) * ix,
                    y = 0.dp,
                    w = screenWidth - 90.dp,
                    h = DisplayType.DEFAULT.height,
                )
        }
    }
    fun expand(ix: Int, expand: Boolean) {
        qqq("E "+ix + " " + expand + " "+data.point.indexOf(ix))
      //  data.vm.toggle[ix] = !data.vm.toggle[ix]!!
        val disp = data.display.find { it.ordinal == ix }!!
        if (expand) {
            val s = list[ix].description!! + " \u2026"
            val p = androidx.compose.ui.text.Paragraph(
                text = s,
                style = typography.bodySmall,
                spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * (vm.ratioV.value ?: vm.ratio.value!!),
                        ),
                        0,
                        s.length
                    )
                ),
                constraints = Constraints(
                    maxWidth = (
                            (screenWidth - (
                                    measures.itemHeight +
                                            measures.padding.times(4) +
                                            measures.padding.times(2) * list[ix].level
                                    ) * (vm.ratioH.value ?: vm.ratio.value!!))
                            ).toPx().toInt()
                ),
                density = density,
                fontFamilyResolver = fontFamilyResolver,
            )
            data.display[ix].height += 14.dp * p.lineCount.minus(2)
//            qqq("EX "+data.point[ix]+" "+data.display[data.point[ix]].height)
        } else data.display[ix].height = disp.type.height + measure(ix)
        val start = data.point.indexOf(ix)
        expandable(ix, expand)
        ruler()
        data.stack = IntArray(batch) { -1 }
        data.point
            .mapIndexed { ix, it ->
                data.vm.xy[it] =
                    ViewModel.Cycler.XY(
                        x = 0.dp,
                        y = data.ruler[ix],
                        w = screenWidth,
                        h = data.display.find { d -> d.ordinal == it }!!.height,
                    )
            }
        (max(0, start - batch / 2) until min(data.ruler.size, start + batch / 2)).map { ix ->
            val point = data.point[ix]
            val index = ix.mod(batch)
            //qqq("RS "+point+" "+ix+" "+data.vm.details.getOrNull(point)?.title + " "+disp.height + " "+disp.type.height+ " "+data.vm.xy[point].y  + data.vm.expand[point])
            data.stack[index] = ix
            cycler.updateDetails(index, data.vm.details[point])
            cycler.updateExpandable(index, data.vm.expand[point])
            cycler.updateXY(index, data.vm.xy[point])
        }
    }
    fun toggle(ix: Int) {
        val point = data.point[data.stack[ix]]
        data.vm.toggle[point] = data.collapse[point]!!.second > 0
        data.collapse[point] =
            data.collapse[point]!!.first to data.collapse[point]!!.second.unaryMinus()
        ruler()
        data.ruler.indices.map {
            data.vm.xy[data.point[it]] = ViewModel.Cycler.XY(
                x = 0.dp,
                y = data.ruler[it] ?: 0.dp,
                w = screenWidth,
                h = data.display[data.point[it]].height,
            )
        }
        (0 until batch).map {
            cycler.updateXY(it, ViewModel.Cycler.XY(0.dp, 0.dp, 0.dp, 0.dp))
        }
        data.stack.filter { it != -1 }.map { renderYSync(it) }
    }
    private fun vm(ix: Int, more: Boolean? = null) {
        val item = list.getOrNull(ix) ?: return
        //qqq("VM ix:"+ix+ " id:"+item.id+" "+item.title + " "+item.level+disp.type+data.ruler.getOrNull(ix))
        xy(ix)
        data.vm.toggle[ix] =
            if (bind.display.value == MAP) null
            else more ?: (data.vm.toggle.getOrNull(ix) ?: false)
        data.vm.details[ix] =
            Details(
                id = item.id!!,
                title = item.title!!.trim(),
                origin = item.origin,
                drawable = item.drawable,
                level = item.level
            )
    }
}