package android.myguide


import android.myguide.model.Cycler
import android.myguide.model.VM
import android.myguide.model.VM.Display.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.getOrNull
import kotlin.collections.mapIndexed
import kotlin.collections.set
import kotlin.collections.withIndex
import kotlin.ranges.until

class Render(
    val ident: Boolean,
    val vm: VM
) {
    private val cycler = vm.cycler
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
    private var handler: VM.Display? = null
    private var list: List<ListInterface> = listOf()
    init {
        vm.adjust.observeForever { adjust ->
            if (!adjust) {
                handler = null
                return@observeForever
            }
            handler = vm.display.value
            adjust()
        }
        vm.ratio.observeForever { zoom() }
        vm.ratioH.observeForever { zoom() }
        vm.ratioV.observeForever { zoom() }
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(1)
                scroll()
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
        var toggle: MutableMap<Int, Pair<Int, Int>> = mutableMapOf(),
        var display: CopyOnWriteArrayList<Display>,
        var point: CopyOnWriteArrayList<Int>,
        var ruler: CopyOnWriteArrayList<Dp>,
        var stack: IntArray,
        var view: View,
    ) {
        class Display(
            var type: DisplayType,
            var height: Dp
        )
        data class View(
            var details: MutableList<Details>,
            var expand: MutableList<AnnotatedString?>,
            var toggle: MutableList<Boolean?>,
            var xy: MutableList<Cycler.XY>
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
    private fun adjust() {
        data.stack = IntArray(batch) { -1 }
        when (vm.display.value!!) {
            V ->
                data.point
                    .map {
                        data.display[it].height =
                            data.display[it].type.height + measure(it)
                        expandable(it)
                        xy(it)
                    }
            H ->
                data.point
                    .map {
                        expandable(it)
                        xy(it)
                    }
            T ->
                data.point
                    .map { xy(it) }
        }
        ruler()
    }
    private fun scroll() {
        when (handler) {
            T -> {
                direction ?: return
                fun go(ix: Int) {
                    val point = data.point.getOrNull(ix) ?: return
                    val index = ix.mod(batch)
                    data.stack[index] = ix
                    cycler.updateXY(index, data.view.xy[point])
                    //      qqq("JOB dir:$direction p:" + point + " ix:" + ix + " " + scroll + " " + data.view.xy[point].y + " " + data.view.details.getOrNull(point)?.title)
                }

                val mn = data.stack.min()
                /*qqq("S "+data.point
                    .withIndex()
                    .indexOfFirst {
                        measures.itemHeight * it.index / 2 > scroll
                    } +" "+ mn + " "+ data.point
                    .withIndex()
                    .indexOfLast {
                        measures.itemHeight * it.index / 2 < scroll
                    } + " "+direction)
    */

                if (direction!!)
                    if (
                        data.point
                            .withIndex()
                            .indexOfFirst {
                                measures.itemHeight * it.index / 2 > scroll
                            } - batch / 4 < mn
                    ) go(mn.dec())
                    else direction = null
                else
                    if (
                        data.point
                            .withIndex()
                            .indexOfLast { measures.itemHeight * it.index / 2 < scroll }
                            .let { it / 2 > mn && it / 2 > batch / 3 }
                    ) go(data.stack.max().inc())
                    else direction = null
            }
            V -> {
                direction ?: return
                fun go(ix: Int) {
                    val point = data.point.getOrNull(ix) ?: return
                    val index = ix.mod(batch)
                    data.stack[index] = ix
                    cycler.updateExpandable(index, data.view.expand[point])
                    cycler.updateDetails(index, data.view.details[point])
                    cycler.updateToggle(index, data.view.toggle[point])
                    cycler.updateXY(index, data.view.xy[point])
                    //qqq(
                    //    "JOB dir:$direction p:" + point + " ix:" + ix + " " + scroll + " " + data.view.xy[point].y + " " + data.view.details.getOrNull(
                    //        point
                    //    )?.title
                   // )
                }
                val mn = data.stack.min()
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
                            .let { it > mn && it > batch / 2 }
                    ) go(data.stack.max().inc())
                    else direction = null
            }
            H -> {
                direction ?: return
                val mx = data.stack.max()
                val mn = data.stack.min()
                val r =
                    max(
                        (scroll / ((measures.mapViewWidth + measures.padding) * vm.ratioH())).toInt(),
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
                    cycler.updateDetails(index, data.view.details[point])
                    cycler.updateExpandable(index, data.view.expand[point])
                    cycler.updateXY(index, data.view.xy[point])
                    qqq(
                        "MAP dir:$direction mn/mx:$mn/$mx r:$r p:" + point + " ix:" + ix + " " + scroll.round() + " " + " " + data.view.xy[point].x.round() + " " + data.view.details.getOrNull(
                            point
                        )?.title
                    )
                }
                if (ix == null) {
                   qqq("ECHO MAP dir:$direction mn/mx:$mn/$mx r:$r ix:" + ix + " " + scroll.round() + " ")
                    direction = null
                }
            }
            null -> {}
        }
    }
    private fun zoom() {
        when (vm.display.value!!) {
            T -> {
                val from =
                    max(
                        data.point
                            .withIndex()
                            .indexOfLast { measures.itemHeight * vm.ratioV() * it.index < scroll }
                                - batch / 4,
                        0
                    )
                (from until min(from + batch, data.point.size)).map {
                    xy(it)
                    cycler.updateXY(it.mod(batch), data.view.xy[data.point[it]])
                }
            }
            V -> {
                val from = max(0, data.ruler.indexOfFirst { it > scroll } - batch / 3)
                var sum = 0.dp
                (from until min(from + batch, data.point.size)).map {
                    val point = data.point[it]
                    val index = it.mod(batch)
                    val m = measure(point)
                    data.view.xy[point] =
                        Cycler.XY(
                            x = 0.dp,
                            y = data.ruler[it] + sum,
                            w = screenWidth,
                            h = data.display[point].type.height + m
                        )
                    if (data.display[point].height != data.display[point].type.height + m)
                        sum += m * vm.ratioV()
                    expandable(point)
                    cycler.updateExpandable(index, data.view.expand[point])
                    cycler.updateXY(index, data.view.xy[point])
                }
            }
            H -> {
                val from =
                    max(
                        (scroll / (measures.mapViewWidth + measures.padding) / vm.ratioH()
                        ).toInt() - batch / 4,
                        0
                    )

                (from until min(from + batch, data.point.size)).map {
                    val point = data.point[it]
                    val index = it.mod(batch)
                    xy(point)


                    expandable(point)
                    cycler.updateExpandable(index, data.view.expand[point])
                    cycler.updateXY(index, data.view.xy[point])
                   // listen()
                }
                qqq("MAP $from $scroll ")

            }
        }
        ruler()
    }
    private fun measure(ix: Int): Dp {
        if (list[ix].description == null || vm.display.value == H) return 0.dp
        val s = list[ix].title!!.trim()
        val p = androidx.compose.ui.text.Paragraph(
            text = s,
            style = typography.bodyLarge.copy(
                fontSize = typography.bodyLarge.fontSize * vm.ratioV()
            ),
            constraints = Constraints(
                maxWidth =
                    (screenWidth - (
                            measures.itemHeight +
                            measures.padding.times(4) +
                            measures.padding.times(2) * list[ix].level
                    ) * vm.ratioH()).toPx().toInt()),
            density = density,
            fontFamilyResolver = fontFamilyResolver,
        )
        //qqq(ident.toString() + " MEASURE "
          //      + " "+p.lineCount + s.take(p.getLineEnd(0))+"=="+" "+s)
        if (p.lineCount > 1)
            return p.getLineHeight(1).toDp() * p.lineCount.dec() - 12.dp
        return 0.dp
    }
    private fun expandable(ix: Int, expand: Boolean = false) {
        if (vm.display.value == H) {
            data.view.expand[ix] = buildAnnotatedString {
                withStyle(
                    ParagraphStyle(
                        lineHeight = 1.em * vm.ratioV()
                    )
                ) {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * (vm.ratioV.value
                                ?: vm.ratio.value!!),
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append(list[ix].description) }
                }
            }
            return
        }
        val str = list.getOrNull(ix)?.description ?: return
        if (expand) {
            data.view.expand[ix] = buildAnnotatedString {
                withStyle(ParagraphStyle(lineHeight = 1.em * vm.ratioV())) {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * vm.ratioV(),
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append(list[ix].description) }
                    withStyle(
                        style = SpanStyle(
                            color = Color.Transparent,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * vm.ratioV(),
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append("\u200A") }
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "lastThree",
                            linkInteractionListener = {
                                expand(ix, false)
                            }
                        )
                    ) {
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.None,
                                color = colorScheme.primary,
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize * vm.ratioV(),
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
                withStyle(ParagraphStyle(lineHeight = 1.em * vm.ratioV())) {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * vm.ratioV(),
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append(str) }
                }
            }
        if (vm.display.value == H) {
            data.view.expand[ix] = static()
            return
        }
        val result = androidx.compose.ui.text.Paragraph(
            text = str,
            style = typography.bodySmall,
            spanStyles = listOf(
                AnnotatedString.Range(
                    SpanStyle(
                        fontStyle = typography.bodySmall.fontStyle,
                        fontSize = typography.bodySmall.fontSize * vm.ratioV(),
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
                                ) * vm.ratioH())
                        ).toPx()
                    .toInt()
            ),
            density = density,
            fontFamilyResolver = fontFamilyResolver,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (result.lineCount < 2) {
            data.view.expand[ix] = static()
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
        data.view.expand[ix] =
            buildAnnotatedString {
                withStyle(ParagraphStyle(lineHeight = 1.em * vm.ratioV())) {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * vm.ratioV(),
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append(take.dropLast(1)) }
                    withStyle(
                        style = SpanStyle(
                            color = Color.Transparent,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * vm.ratioV(),
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append("\u200A") }
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = "lastThree",
                            linkInteractionListener = {
                                expand(ix, true)
                            }
                        )
                    ) {
                        withStyle(
                            style = SpanStyle(
                                textDecoration = TextDecoration.None,
                                color = colorScheme.primary,
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize * vm.ratioV(),
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
    private fun job() {
        /*@Suppress("UNUSED_VARIABLE")
        when (vm.display.value!!) {
            D3 -> {
                offset = 6
                (0 until position + screenHeight / DisplayType.D3.height * 3).map {
                    val s = start++
                    ini.invoke(s)
                    ms.invoke(s)
                }
                start = max(start - min(list.size, batch), 0)
            }
            LIST -> {
                offset = 6
                val lim = position + screenHeight
                while (
                    data.display
                        .sumOf { h -> h.height.value.toDouble() } < lim.value.toDouble()
                    ||
                    //start < list.size
                    start <= min(list.size, batch).dec()
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
                    //data.display.size < position / (screenWidth - 90.dp) + lim &&
                    start < min(list.size, batch)
                ) {
                    val s = start++
                    ini1(s)
                }
                start = max(start - lim, 0)
            }
        }

             */

        var limit = 0
        var start = 0

        list.indices.map {
            val item = list[it]
            var m = 0.dp
            val displayType =
                if (item.description == null)
                    DisplayType.NODE
                else {
                    m = measure(it)
                    DisplayType.DEFAULT
                }
            data.display.add(
                Data.Display(
                    height = displayType.height + m,
                    type = displayType
                )
            )
           // qqq("?? "+item.title+ " "+it+" "+item.description + " "+m+" "+displayType)
        }


        filter()
        ruler()

        qqq("SL "+data.point.size+ident+start + " "+limit + " "+list.size +" "+data.point.size)
        data.point.indices.map {
            expandable(it)
            vm(data.point[it])
        }
        limit = min(
            batch,
            data.point.size
        )
        start = max(0, limit - batch)
        when (vm.display.value!!) {
            T ->
                (start until limit).map {
                    vm(data.point[it])
                    //    activity.runOnUiThread { renderYD3(it) }
                }
            H -> (start until limit).map { renderX(it) }
            V -> (start until limit).map { renderYSync(it) }
        }
        sleep { lock = false }
    }
    private fun filter() {
        val toggle = data.toggle
        data.point.clear()
        data.point.addAll(
            data.display.withIndex().filter {
                !toggle.any { c ->
                    c.value.first < it.index
                            && c.value.first + c.value.second.unaryMinus() > it.index
                }
            }.map { it.index }.toList()
        )
    }
    private fun xy(ix: Int) {
        val point = data.point[ix]
        data.view.xy[point] =
            when (vm.display.value!!) {
                T ->
                    Cycler.XY(
                        x = screenWidth / measures.tableColumns * ix.mod(measures.tableColumns),
                        y = ((measures.itemHeight.times(2)) * (ix / measures.tableColumns)) * vm.ratioV(),
                        w = screenWidth / measures.tableColumns,
                        h = measures.itemHeight * 2,
                    )
                V ->
                    Cycler.XY(
                        x = 0.dp,
                        y = data.ruler[ix],
                        w = screenWidth,
                        h = data.display[point].height,
                    )
                H ->
                    Cycler.XY(
                        x = measures.mapViewWidth * ix * vm.ratioH(),
                        y = 0.dp,
                        w = (measures.mapViewWidth - measures.padding) * vm.ratioH(),
                        h = DisplayType.DEFAULT.height,
                    )
            }
    }
    fun display() {
        handler = vm.display.value
        toolbar.items.last().display = vm.display.value!!
        when (vm.display.value!!) {
            H -> {
                vm.w.value = measures.mapViewWidth * data.ruler.size * vm.ratioH()
                vm.h.value = measures.itemHeight * vm.ratioV()
                list.indices.map {
                    expandable(it)
                    xy(it)
                }
            }
            T -> {
                list.indices.map {
                    xy(it)
                }
                vm.w.value = screenWidth
                vm.h.value = (measures.itemHeight.times(2)) * (data.point.size / measures.tableColumns) * vm.ratioV()
            }
            V -> {
                ruler()
                list.indices.map {
                    expandable(it)
                    xy(it)
                }
                vm.w.value = screenWidth
                vm.h.value = height
            }
        }
        (0 until min(batch, data.point.size)).map {
            val point = data.point[it]
            val index = it.mod(batch)
            data.stack[index] = it
            cycler.updateExpandable(index, data.view.expand[point])
            cycler.updateXY(index, data.view.xy[point])
        }
    }
    fun load(list: List<ListInterface>) {
        qqq("LOAD $ident "+list.size)
        this.list = list
        handler = vm.display.value
        data.view.details = MutableList(this@Render.list.size) {
            Details(
                id = "",
                title = "",
                origin = null,
                drawable = null,
                level = 0
            )
        }
        data.view.expand = MutableList(this@Render.list.size) { null }
        data.view.xy = MutableList(this@Render.list.size) {
            Cycler.XY(0.dp, 0.dp, 0.dp, 0.dp)
        }
        data.view.toggle = MutableList(this@Render.list.size) { null }
        val position = toolbar.items.lastOrNull()?.position ?: return
        if (position > 0.dp) {
            if (vm.display.value == H) {
                vm.w.postValue(position + screenWidth)
                vm.h.postValue(measures.itemHeight)
            } else {
                vm.w.postValue(screenWidth)
                vm.h.postValue(position + screenHeight)
            }
            vm.position.postValue(toolbar.items.last().position)
            job()
        } else job()
    }
    private var height = 0.dp
    private fun ruler() {
        //data.point.clear()
        when (vm.display.value!!) {
            T -> {}
            V -> {
                data.ruler.clear()
                height = 0.dp
                data.point.map {
                 //   data.point.add(it)
                    data.ruler.add(height)
                    height += (data.display[it].height + measures.padding) * vm.ratioV()
                }
                vm.w.postValue(screenWidth)
                vm.h.postValue(height)
            }
            H -> {
                vm.w.postValue(measures.mapViewWidth * data.point.size * vm.ratioH())
                vm.h.postValue(measures.itemHeight * vm.ratioH())
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
    fun observe(p: Dp) {
        direction = scroll > p
      //  qqq("sp $ident"+scroll + " "+p + " "+direction)

        scroll = p
        /*
        when (bind.display.value) {
            LIST -> {
                //qqq("A")

                val pos = p //+ screenHeight / 3

                val mx = data.stack.max()
                val mn = data.stack.min()//min(data.stack.min() - 5, data.ruler.size)
                val v = (mx + mn) / 2 // - 10//-  batch / 4
                val a = data.ruler[mn] - data.display[data.point[mn]].height
                var r: Int =  data.ruler.withIndex().indexOfFirst { it.value > p && it.index > mx }
                //qqq("R"+r+" " )
                if (pos > 180.dp && prev != r
                    ) {
                    //r = data.ruler.withIndex().indexOfFirst { it.value > p && it.index > mx }//.inc() //+ batch//+ 5
                  //  renderY(mx.inc())
                    qqq("O pos $pos/$a mx/mn:$mx/$mn v:$v prev:$prev r:$r")
                    if (data.ruler.size - 4 < r) r =data.ruler.size
                   // prev = prev ?: r
                   // if (false)
                    (mx.inc() until  r.inc())
                        //.filter { it !in data.stack }
                        .map { renderY(it) }
                    prev = r.inc()
                }
                else {
                    qqq("A pos $pos/$a mx/mn:$mx/$mn v:$v prev:$prev r:$r")
                    /*
                    r = data.ruler.indexOfLast { it < pos }
                    if (r > -1 && prev != null) {
                        prev = prev ?: r
                        (mn downTo mn - prev!! + r.dec())
                          //  .filter { it !in data.stack }
                            .map { renderY(it) }
                        prev = r
                    }

                     */
                }

               // qqq("B")
            }
                /*max(data.ruler.indexOfLast { it < pos } - offset, 0).also { f ->
                    (f..min(data.ruler.size, f + batch).dec()).filter {
                        !data.stack.contains(it)
                    }.map { renderY(it) }
                }*/
            else ->
                max((p / (screenWidth - 90.dp)).toInt() - batch / 2, 0).also { from ->
                    (from .. min(data.ruler.size, from + batch).dec())
                        .filter { !data.stack.contains(it) }
                        .map { renderX(it) }
                }
        }

         */
    }
    fun reset() {
        data.display.clear()
        data.stack = IntArray(batch) { -1 }
        data.toggle.clear()
        data.view.expand.clear()
        data.view.details.clear()
        data.view.toggle.clear()
        data.view.xy.clear()
        handler = null
        (0 until batch).map {
            cycler.updateXY(it, Cycler.XY(0.dp, 0.dp, 0.dp, 0.dp))
        }
        /*
        data.collapse.clear()
        data.display.clear()
        data.point.clear()
        data.ruler.clear()

        data.vm.details.clear()
        data.vm.toggle.clear()
        vm.cycler.reset()
        handler = null
        height = 0.dp
        job?.cancel()
        offset = 0
        ordinal = 0

         */
    }
    private fun renderX(ix: Int) {
        val point = data.point.getOrNull(ix) ?: return
        val index = ix.mod(batch)
        data.stack[index] = ix
        //qqq("RX ix:$ix point:"+data.point.getOrNull(ix) + " index:"+index +data.vm[point].title+ "  "+data.vm[point].x)
        cycler.updateDetails(index, data.view.details[point])
        cycler.updateExpandable(index, data.view.expand[point])
        cycler.updateXY(index, data.view.xy[point])
    }
    private fun renderYSync(ix: Int) {
        val point = data.point.getOrNull(ix) ?: return
        val index = ix.mod(batch)
        ///val disp = data.display.find { it.ordinal == ix }!!
        qqq("RS "+point+" "+ix+" "+data.view.details.getOrNull(point)?.title )//+ " "+disp.height + " "+disp.type.height+ " "+data.vm.xy[point].y  + data.vm.expand[point])
        data.stack[index] = ix
        cycler.updateExpandable(index, data.view.expand[point])
        cycler.updateDetails(index, data.view.details[point])
        cycler.updateToggle(index, data.view.toggle[point])
        cycler.updateXY(index, data.view.xy[point])
    }
    fun expand(ix: Int, expand: Boolean) {
        qqq("E "+ix + " " + expand + " "+data.point.indexOf(ix))
        val disp = data.display[data.point[ix]]//.find { it.ordinal == ix }!!
        if (expand) {
            val s = list[ix].description!! + " \u2026"
            val p = androidx.compose.ui.text.Paragraph(
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
                                    measures.itemHeight +
                                            measures.padding.times(4) +
                                            measures.padding.times(2) * list[ix].level
                                    ) * vm.ratioH())
                            ).toPx().toInt()
                ),
                density = density,
                fontFamilyResolver = fontFamilyResolver,
            )
            data.display[ix].height += p.getLineHeight(1).toDp() * p.lineCount.minus(2)
        } else data.display[ix].height = disp.type.height + measure(ix)
        val start = data.point.indexOf(ix)
        expandable(ix, expand)
        ruler()
        data.stack = IntArray(batch) { -1 }
        data.point
            .mapIndexed { ix, it ->
                xy(ix)/*
                data.view.xy[it] =
                    Cycler.XY(
                        x = 0.dp,
                        y = data.ruler[ix],
                        w = screenWidth,
                        h = data.display[data.point[it]]!!.height,
                    )
                    */
            }
        (max(0, start - batch / 2) until min(data.ruler.size, start + batch / 2)).map { ix ->
            val point = data.point[ix]
            val index = ix.mod(batch)
            //qqq("RS "+point+" "+ix+" "+data.vm.details.getOrNull(point)?.title + " "+disp.height + " "+disp.type.height+ " "+data.vm.xy[point].y  + data.vm.expand[point])
            data.stack[index] = ix
            cycler.updateDetails(index, data.view.details[point])
            cycler.updateExpandable(index, data.view.expand[point])
            cycler.updateXY(index, data.view.xy[point])
        }
    }
    fun toggle(ix: Int) {
        val point = data.point[data.stack[ix]]
        data.view.toggle[point] = data.toggle[point]!!.second > 0
        data.toggle[point] =
            data.toggle[point]!!.first to data.toggle[point]!!.second.unaryMinus()
        ruler()
        data.ruler.indices.map {
            /*
            data.view.xy[data.point[it]] = Cycler.XY(
                x = 0.dp,
                y = data.ruler[it] ?: 0.dp,
                w = screenWidth,
                h = data.display[data.point[it]].height,
            )

             */
            xy(it)
        }
        (0 until batch).map {
            cycler.updateXY(it, Cycler.XY(0.dp, 0.dp, 0.dp, 0.dp))
        }
        data.stack.filter { it != -1 }.map {
            //cycler.updateXY(it.mod(batch), data.vm.xy[data.point[it]])
            //cycler.updateToggle(it.mod(batch), data.vm.toggle[data.point[it]])
            renderYSync(it)

        }
    }
    private fun vm(ix: Int, more: Boolean? = null) {
        val item = list[ix]//.getOrNull(ix) ?: return
        val disp = data.display[ix]//.getOrNull(ix) ?: return
        val ruler = data.ruler[ix]//.getOrNull(ix) ?: return
        //qqq("VM ident:$ident ix:"+ix+ " id:"+disp.type+" "+item.title + " "+disp.height+item.level+ruler +item.description)
       // data.vm.toggle.getOrNull(ix) ?: return
        /*data.view.xy[ix] =
            when (vm.display.value) {
                LIST -> {
                    Cycler.XY(
                        x = 0.dp,
                        y = ruler,
                        w = screenWidth,
                        h = disp.height,
                    )
                }
                else ->
                    Cycler.XY(
                        x = (screenWidth - 90.dp) * ix,
                        y = 0.dp,
                        w = screenWidth - 90.dp,
                        h = DisplayType.DEFAULT.height,
                    )

            }
         */
        xy(ix)
        data.view.toggle[ix] = more ?: (data.view.toggle.getOrNull(ix) ?: false)
        data.view.details[ix] =
            Details(
                id = item.id!!,
                title = item.title!!.trim(),
                origin = item.origin,
                drawable = item.drawable,
                level = item.level
            )
    }
}