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
        fun zoom() {
            qqq("AB "+ident  + " " + scroll +(scroll * (vmm.ratioV.value ?: vmm.ratio.value!!)))//+ " "+x+" "+(height / h))
            when (vm.display.value!!) {
                LIST, D3 -> {
                    ruler()
                    val x = max(0, data.ruler.indexOfFirst { it > scroll } - batch / 3)
                    (0 until batch).map {
                        val stack = it + x
                        val point = data.point.getOrNull(stack)
                        val ruler = data.ruler.getOrNull(stack)
                        if (
                            ruler != null &&
                            point != null
                        ) {
                            data.display[point].height =
                                data.display[point].type.height + measure(point)
                            data.view.xy[stack] =
                                Cycler.XY(
                                    x = 0.dp,
                                    y = ruler,
                                    w = screenWidth,
                                    h = data.display[point].height,
                                )
                            val index = stack.mod(batch)
                            expandable(stack)
                            cycler.updateXY(index, data.view.xy[point])
                            cycler.updateExpandable(index, data.view.expand[stack])
                        }
                    }

                }
                MAP -> {
                    val x = max((scroll / measures.mapViewWidth).toInt(), 0)
                    (0 until batch).map {
                        val stack = it + x
                        val point = data.point.getOrNull(stack)
                        val ruler = data.ruler.getOrNull(stack)
                        if (
                            ruler != null &&
                            point != null
                        ) {
                            data.view.xy[stack] =
                                Cycler.XY(
                                    x = 0.dp,
                                    y = ruler,
                                    w = screenWidth,
                                    h = data.display[point].height,
                                )
                            val index = stack.mod(batch)
                            expandable(stack)
                            cycler.updateXY(index, data.view.xy[point])
                            cycler.updateExpandable(index, data.view.expand[stack])
                        }
                    }
                }
            }
        }
        vmm.adjust[ident]!!.observeForever {
            if (it) {
                handler = vm.display.value
                /*if (ident != vmm.toolbar.last?.ident)
                    data.display.indices.map { i ->
                        data.display[i].height = data.display[i].type.height + measure(i)
                        expandable(i)
                    }
                else {*/
                list.indices
                    .filter { i -> i !in data.stack }
                    .map { i ->
                        val point = data.point.getOrNull(i)
                        val ruler = data.ruler.getOrNull(i)
                        if (point != null && ruler != null) {
                            data.display[point].height =
                                data.display[point].type.height + measure(point)
                            data.view.xy[point] =
                                Cycler.XY(
                                    x = 0.dp,
                                    y = ruler,
                                    w = screenWidth,
                                    h = data.display[point].height,
                                )
                            expandable(i)
                        }
                    }
              //  zoom.postValue(true)
                sleep { zoom() }

            }
            else handler = null
        }
        vmm.ratio.observeForever { zoom() }// zoom.value = true }
        vmm.ratioH.observeForever { zoom() }//zoom.value = true }
        vmm.ratioV.observeForever { zoom() }//zoom.value = true }
        fun full() {
            direction ?: return
            val mx = data.stack.max()
            val mn = data.stack.min()
            var r: Int = data.ruler.withIndex()
                .indexOfLast { it.value < scroll }
            val ix =
                (
                    if (direction ?: true && r < mn) mn.dec()
                    else if (r > mn && r > batch / 3) mx.inc()
                    else null
                )
            ix?.also { ix ->
                val point = data.point.getOrNull(ix) ?: return
                val index = ix.mod(batch)
                data.stack[index] = ix
                cycler.updateExpandable(index, data.view.expand[point])
                cycler.updateDetails(index, data.view.details[point])
                cycler.updateToggle(index, data.view.toggle[point])
                cycler.updateXY(index, data.view.xy[point])
                qqq("JOB dir:$direction mn/mx:$mn/$mx r:$r p:" + point + " ix:" + ix + " " + scroll + " " + data.view.xy[point].y + " " + data.view.details.getOrNull(point)?.title)
            }
            if (ix == null) {
            //    qqq("ECHO dir:$direction mn/mx:$mn/$mx r:$r ix:" + ix + " " + scroll + " ")
                direction = null
            }
        }
        fun map() {
            direction ?: return
            val mx = data.stack.max()
            val mn = data.stack.min()
            var r = max(
                (scroll / measures.mapViewWidth).toInt(),
                0
            )
            val ix =
                (
                    if (direction ?: true && r < mn) mn.dec()
                    else if (r > mn) mx.inc()
                    else null
                )
            ix?.also { ix ->
                    //.map { ix ->
                        val point = data.point.getOrNull(ix) ?: return
                        val index = ix.mod(batch)
                        data.stack[index] = ix
                       // qqq("RX ix:$ix point:"+data.point.getOrNull(ix) + " index:"+index +"  ")
                        cycler.updateDetails(index, data.view.details[point])
                        cycler.updateExpandable(index, data.view.expand[point])
                        cycler.updateXY(index, data.view.xy[point])
                    //}
                qqq("MAP dir:$direction mn/mx:$mn/$mx r:$r p:" + point + " ix:" + ix + " " + scroll + " " + data.view.xy[point].y + " " + data.view.details.getOrNull(point)?.title)

            }
            if (ix == null) {
                   // qqq("ECHO MAP dir:$direction mn/mx:$mn/$mx r:$r ix:" + ix + " " + scroll + " ")
                direction = null
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(1)
                handler?.also {
                    mapOf(
                        LIST to ::full,
                        MAP to ::map,
                        D3 to ::full
                    )[it]!!.invoke()
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
    private fun measure(ix: Int): Dp {
        if (list[ix].description == null || vm.display.value == MAP) return 0.dp
        val s = list[ix].title!!.trim()
        val p = androidx.compose.ui.text.Paragraph(
            text = s,
            style = typography.bodyLarge.copy(
                fontSize = typography.bodyLarge.fontSize * (vmm.ratioV.value ?: vmm.ratio.value!!)
            ),
            constraints = Constraints(
                maxWidth =
                    (screenWidth - (
                            measures.itemHeight +
                            measures.padding.times(4) +
                            measures.padding.times(2) * list[ix].level
                    ) * (vmm.ratioH.value ?: vmm.ratio.value!!)).toPx().toInt()),
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
    private fun expandable(ix: Int, expand: Boolean = false) {
        val str = list.getOrNull(ix)?.description ?: return
        if (expand) {
            data.view.expand[ix] = buildAnnotatedString {
                withStyle(ParagraphStyle(lineHeight = 1.em * (vmm.ratioV.value ?: vmm.ratio.value!!))) {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * (vmm.ratioV.value ?: vmm.ratio.value!!),
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append(list[ix].description) }
                    withStyle(
                        style = SpanStyle(
                            color = Color.Transparent,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * (vmm.ratioV.value ?: vmm.ratio.value!!),
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
                                fontSize = typography.bodySmall.fontSize * (vmm.ratioV.value ?: vmm.ratio.value!!),
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
                withStyle(ParagraphStyle(lineHeight = 1.em * (vmm.ratioV.value ?: vmm.ratio.value!!))) {
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.secondary,
                            textDecoration = TextDecoration.None,
                            fontStyle = typography.bodySmall.fontStyle,
                            fontSize = typography.bodySmall.fontSize * (vmm.ratioV.value ?: vmm.ratio.value!!),
                            fontWeight = typography.bodySmall.fontWeight
                        )
                    ) { append(str) }
                }
            }
        if (vm.display.value == MAP) {
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
                        fontSize = typography.bodySmall.fontSize * (vmm.ratioV.value ?: vmm.ratio.value!!),
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
                                ) * (vmm.ratioH.value ?: vmm.ratio.value!!))
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
       //     if (take == str) static()
        //    else
                buildAnnotatedString {
                    withStyle(ParagraphStyle(lineHeight = 1.em * (vmm.ratioV.value ?: vmm.ratio.value!!))) {
                        withStyle(
                            style = SpanStyle(
                                color = colorScheme.secondary,
                                textDecoration = TextDecoration.None,
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize * (vmm.ratioV.value ?: vmm.ratio.value!!),
                                fontWeight = typography.bodySmall.fontWeight
                            )
                        ) { append(take.dropLast(1)) }
                        withStyle(
                            style = SpanStyle(
                                color = Color.Transparent,
                                textDecoration = TextDecoration.None,
                                fontStyle = typography.bodySmall.fontStyle,
                                fontSize = typography.bodySmall.fontSize * (vmm.ratioV.value ?: vmm.ratio.value!!),
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
                                    fontSize = typography.bodySmall.fontSize * (vmm.ratioV.value ?: vmm.ratio.value!!),
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
            //qqq("?? "+item.title+ " "+ix+" "+item.description)
        }
        ruler()

        qqq("SL "+ident+start + " "+limit + " "+list.size +" "+data.point.size)
        list.indices.map {
            expandable(it)
            vm(data.point[it])
        }
        limit = min(
            batch,
            data.point.size
        )
        start = max(0, limit - batch)
        when (vm.display.value!!) {
            D3 ->
                (start until limit).map {
                    vm(data.point[it])
                    //    activity.runOnUiThread { renderYD3(it) }
                }
            MAP -> (start until limit).map { renderX(it) }
            LIST -> (start until limit).map { renderYSync(it) }
        }
        sleep { lock = false }
    }
    fun display() {
        handler = vm.display.value
        vmm.toolbar.items.last().display = vm.display.value!!
        when (vm.display.value!!) {
            MAP -> {
                vm.w.value = measures.mapViewWidth * data.ruler.size
                vm.h.value = measures.itemHeight
                list.indices.map {
                    data.view.expand[it] = buildAnnotatedString {
                        withStyle(
                            ParagraphStyle(
                                lineHeight = 1.em * (vmm.ratioV.value ?: vmm.ratio.value!!)
                            )
                        ) {
                            withStyle(
                                style = SpanStyle(
                                    color = colorScheme.secondary,
                                    textDecoration = TextDecoration.None,
                                    fontStyle = typography.bodySmall.fontStyle,
                                    fontSize = typography.bodySmall.fontSize * (vmm.ratioV.value
                                        ?: vmm.ratio.value!!),
                                    fontWeight = typography.bodySmall.fontWeight
                                )
                            ) { append(list[it].description) }
                        }
                    }
                }
                data.view.xy.mapIndexed { ix, it ->
                    it.x = measures.mapViewWidth * ix
                    it.y = 0.dp
                    it.w = measures.mapViewWidth - measures.padding
                    it.h = data.display[ix].type.height
                }
            }
            D3, LIST -> {
                list.indices.map { expandable(it) }
                data.view.xy.mapIndexed { ix, it ->
                    it.x = 0.dp
                    it.y = data.ruler[ix]
                    it.w = screenWidth
                    it.h = data.display[ix].height
                }
                vm.w.value = screenWidth
                vm.h.value = height
            }
        }
        (0 until min(batch, data.point.size)).map {
        //(start until end).map {
            val point = data.point[it]
            val index = it.mod(batch)
            data.stack[index] = it
            cycler.updateExpandable(index, data.view.expand[point])
            cycler.updateXY(index, data.view.xy[point])
        }
    }
    fun load(list: List<ListInterface>, callback: (() -> Unit)? = null) {
        //job?.cancel()
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
        val position = vmm.toolbar.items.lastOrNull()?.position ?: return
        if (position > 0.dp) {
            if (vm.display.value == MAP) {
                vm.w.postValue(position + screenWidth)
                vm.h.postValue(measures.itemHeight)
            } else {
                vm.w.postValue(screenWidth)
                vm.h.postValue(position + screenHeight)
            }
            vm.position.postValue(vmm.toolbar.items.last().position)
            job()
        } else job()
    }
    private var height = 0.dp
    private fun ruler(reset: Boolean = true) {
        if (reset) {
            height = 0.dp
            data.point.clear()
            data.ruler.clear()
        }
        val toggle = data.toggle
        val filter =
            data.display.withIndex().filter {
                !toggle.any { c ->
                    c.value.first < it.index//.ordinal
                            && c.value.first + c.value.second.unaryMinus() > it.index//.ordinal
                }
            }//.map { it.value }.toList()
        val list = filter.subList(data.ruler.size, filter.size)
        when (vm.display.value!!) {
            D3 -> {}
            LIST -> {
                list.map { d ->
                    data.point.add(d.index)//.ordinal)
                    data.ruler.add(height)
                    height += (d.value.height + 8.dp) * (vmm.ratioV.value ?: vmm.ratio.value!!)
                }
                vm.w.postValue(screenWidth)
                vm.h.postValue(height)
            }
            MAP -> {
                list.map { d ->
                    data.point.add(d.index)
                    data.ruler.add(height)
                    height += (d.value.height + 8.dp) * (vmm.ratioV.value ?: vmm.ratio.value!!)
                }
                vm.w.postValue((screenWidth - 90.dp) * data.ruler.size)
                vm.h.postValue(measures.itemHeight)
            }
        }

      /*      list.map { d ->
                data.point.add(d.ordinal)
                data.ruler.add(height + divider)
                if (data.ruler.size.mod(3) == 0) height += DisplayType.D3.height + divider
            }
            val h = filter.size / 3 * (DisplayType.D3.height + divider) + dp2px(16f)
            view.post { view.layoutParams = FrameLayout.LayoutParams(screenWidth,  h) }

       */
        //qqq("RULER :"+ data.ruler)
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
        data.stack = IntArray(batch) { -1 }
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
        //qqq("RS "+point+" "+ix+" "+data.vm.details.getOrNull(point)?.title + " "+disp.height + " "+disp.type.height+ " "+data.vm.xy[point].y  + data.vm.expand[point])
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
                            fontSize = typography.bodySmall.fontSize * (vmm.ratioV.value ?: vmm.ratio.value!!),
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
                                    ) * (vmm.ratioH.value ?: vmm.ratio.value!!))
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
                data.view.xy[it] =
                    Cycler.XY(
                        x = 0.dp,
                        y = data.ruler[ix],
                        w = screenWidth,
                        h = data.display[data.point[it]]!!.height,
                    )
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
            data.view.xy[data.point[it]] = Cycler.XY(
                x = 0.dp,
                y = data.ruler[it] ?: 0.dp,
                w = screenWidth,
                h = data.display[data.point[it]].height,
            )
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
        val item = list.getOrNull(ix) ?: return
        val disp = data.display.getOrNull(ix) ?: return
        val ruler = data.ruler.getOrNull(ix) ?: return
        //qqq("VM ident:$ident ix:"+ix+ " id:"+disp.type+" "+item.title + " "+disp.height+item.level+ruler +item.description)
       // data.vm.toggle.getOrNull(ix) ?: return
        data.view.xy[ix] =
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