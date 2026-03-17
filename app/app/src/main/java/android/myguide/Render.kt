package android.myguide


import android.myguide.data.Cycler.XY
import android.myguide.data.Details
import android.myguide.data.ListInterface
import android.myguide.data.VM
import android.myguide.data.VM.Display.*
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
import androidx.compose.ui.unit.Density
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
    var list: List<ListInterface> = listOf()
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
        vm.scale.observeForever { zoom() }
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
                    DEFAULT -> measures.itemHeight + measures.padding
                    NODE -> 42.dp
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
    private fun adjust() {
        data.stack = IntArray(batch) { -1 }
        when (vm.display.value!!) {
            V -> {
                data.ruler.clear()
                height = 0.dp
                data.point.map {
                    data.display[it].height = measure(it)
                    expandable(it)
                    data.ruler.add(height)
                    height += data.display[it].type.height + data.display[it].height
                    xy(it)
                }
                vm.w.postValue(screenWidth)
                vm.h.postValue(height)
                data.stack.map { renderYSync(it) }
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
    }
    private fun scroll() {
        when (handler) {
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
                    cycler.update(index, data.view.details[point])
                    cycler.update(index, data.view.expand[point])
                    cycler.update(index, data.view.xy[point])
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
                    cycler.update(index, data.view.xy[point])
                    data.stack[index] = ix
                }
                val mn = data.stack.min()
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
                    cycler.update(index, data.view.expand[point])
                    cycler.update(index, data.view.details[point])
                    cycler.update(index, data.view.toggle[point])
                    cycler.update(index, data.view.xy[point])
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
                    cycler.update(it.mod(batch), data.view.xy[data.point[it]])
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
                        XY(
                            x = 0.dp,
                            y = data.ruler[it] + sum,
                            d = data.display[point].type.height,
                            h = m,
                            i = it
                        )
                    //qqq("Z p:"+point+ " m:" + m + data.display[point].height + " sum:" +sum + " r:"+data.ruler[it])
                    if (data.display[point].height < m)
                        sum += m
                    else if (data.display[point].height > m)
                        sum -= m
                    expandable(point)
                    cycler.update(index, data.view.expand[point])
                    cycler.update(index, data.view.xy[point])
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
                    cycler.update(index, data.view.expand[point])
                    cycler.update(index, data.view.xy[point])
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
        val p = androidx.compose.ui.text.Paragraph(
            text = s,
            style = typography.bodyLarge,
            constraints = Constraints(
                maxWidth =
                    (screenWidth - (
                            measures.itemHeight +
                            measures.padding.times(4) +
                            measures.padding.times(2) * list[ix].level
                    ) * vm.ratioH()).toPx().toInt()),
            density = Density(
                density = density.density,
                fontScale = 1f * vm.scale.value!! * vm.ratioV()
            ),
            fontFamilyResolver = fontFamilyResolver,
        )
        //qqq(ident.toString() + " MEASURE "+p.getLineHeight(0).toDp()
          //      + " "+p.lineCount + s.take(p.getLineEnd(0))+"=="+" "+s)
        if (p.lineCount > 1)
            return measures.titleHeight * p.lineCount.dec()// * vm.ratioV() * vm.scale.value!!// - 12.dp
        return 0.dp
    }
    private fun expandable(ix: Int, expand: Boolean = false) {
        if (vm.display.value == H) {
            data.view.expand[ix] =
                buildAnnotatedString {
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
                        fontSize = typography.bodySmall.fontSize,// * (vm.scale.value!! + vm.ratioV()),
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
            density = Density(
                density = density.density,
                fontScale = 1f * vm.scale.value!! * vm.ratioV()
            ),
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

        if (str.length == take.length) {
            data.view.expand[ix] = static()
            return
        }
/*
        qqq(
            ident.toString()
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
                withStyle(
                    ParagraphStyle(lineHeight = typography.bodySmall.fontSize)
                ) {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize,
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append(take.dropLast(1)) }
                    withStyle(
                        style = SpanStyle(
                            color = Color.Transparent,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize,
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
                                fontSize = typography.bodySmall.fontSize,
                                fontWeight = typography.bodySmall.fontWeight,
                            )
                        ) { append("\u2026") }
                    }
                }
            }
    }
    private fun job() {
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
                    height = m,
                    type = displayType
                )
            )
        }
        filter()
        ruler()
        //qqq("SL "+data.point.size+ident+start + " "+limit + " "+list.size +" "+data.point.size)
        data.point.indices.map {
            expandable(it)
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
        lock = false
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
    private fun xy(ix: Int) {
        val point = data.point[ix]
        data.view.xy[point] =
            when (vm.display.value!!) {
                T ->
                    XY(
                        x = screenWidth / measures.tableColumns * ix.mod(measures.tableColumns),
                        y = ((measures.itemHeight.times(2)) * (ix / measures.tableColumns)) * vm.ratioV(),
                        d = screenWidth / measures.tableColumns,
                        h = measures.itemHeight * 2,
                    )
                V ->
                    XY(
                        x = 0.dp,
                        y = data.ruler[ix],
                        d = data.display[point].type.height,
                        h = data.display[point].height,
                        i = ix
                    )
                H ->
                    XY(
                        x = measures.mapViewWidth * ix * vm.ratioH(),
                        y = 0.dp,
                        d = (measures.mapViewWidth - measures.padding) * vm.ratioH(),
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
              //  ruler()
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
            cycler.update(index, data.view.expand[point])
            cycler.update(index, data.view.xy[point])
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
        when (vm.display.value!!) {
            T -> {}
            V -> {
                data.ruler.clear()
                height = 0.dp
                data.point.map {
                    data.ruler.add(height)
                    height += data.display[it].type.height + data.display[it].height
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
        cycler.update(index, data.view.details[point])
        cycler.update(index, data.view.expand[point])
        cycler.update(index, data.view.xy[point])
    }
    private fun renderYSync(ix: Int) {
        val point = data.point.getOrNull(ix) ?: return
        val index = ix.mod(batch)
        ///val disp = data.display.find { it.ordinal == ix }!!
        //qqq("RS "+point+" "+ix+" "+data.view.details.getOrNull(point)?.title )//+ " "+disp.height + " "+disp.type.height+ " "+data.vm.xy[point].y  + data.vm.expand[point])
        data.stack[index] = ix
        cycler.update(index, data.view.expand[point])
        cycler.update(index, data.view.details[point])
        cycler.update(index, data.view.toggle[point])
        cycler.update(index, data.view.xy[point])
    }
    fun expand(ix: Int, expand: Boolean) {
        qqq("E "+ix + " " + expand + " "+data.point.indexOf(ix))
        val disp = data.display[data.point[ix]]
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
        //data.stack = IntArray(batch) { -1 }
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
                cycler.update(it, XY(0.dp, 0.dp, 0.dp, 0.dp))
                renderYSync(it)
            }
    }
}