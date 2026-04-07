package com.myguide

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.key
import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.json.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.myguide.Expandable.expandable
import com.myguide.Expandable.expanded
import com.myguide.Expandable.static
import com.myguide.UI.BUTTON
import com.myguide.UI.COLUMNS
import com.myguide.UI.ITEM_HEIGHT
import com.myguide.UI.MARGIN
import com.myguide.UI.TITLE_HEIGHT
import com.myguide.UI.mapViewWidth
import com.myguide.data.Cycler.XY
import com.myguide.data.Details
import com.myguide.data.ListInterface
import com.myguide.data.MX
import com.myguide.data.Query.ITEM
import com.myguide.data.Query.ITEMS
import com.myguide.data.Query.SHOP
import com.myguide.data.Query.SHOPS
import com.myguide.data.VM
import com.myguide.data.VM.Type.D
import com.myguide.data.VM.Type.H
import com.myguide.data.VM.Type.T
import com.myguide.data.VM.Type.V
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.sqrt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.myguide.Screen.Tags.*

class Screen(private val context: Context, val ident: Boolean) {
    val vm = VM()
    fun build() {
        val item = toolbar.items.last()
        qqq(
            "BUILD qry:${item.query} disp:${item.type} ident:$ident id:${item.id} xy:${
                scrollY.toDp().round()
            }"
        )
        when (toolbar.items.last().query) {
            ITEM ->
                db.fetchItemDetails(toolbar.items.last().id!!) {
                    vm.description.postValue(it.description)
                    vm.details.postValue(
                        Details(
                            title = it.title!!,
                            origin = it.origin,
                            drawable = it.drawable,
                            level = 0
                        )
                    )
                    current.postValue(!(current.value ?: true))
                }

            SHOP ->
                db.fetchShopDetails(toolbar.items.last().id!!) {
                    vm.description.postValue(it.description)
                    vm.details.postValue(
                        Details(
                            title = it.title,
                            origin = it.origin,
                            drawable = it.drawable,
                            level = 0
                        )
                    )
                    current.postValue(!(current.value ?: true))
                }

            else -> {
                vm.description.value = null
                vm.details.value = null
                current.value = !(current.value ?: true)
            }
        }
        vm.type.value = item.type
        vm.h.value = screenHeight.toPx().toInt()
        vm.scrollY.value = item.scroll
        vm.scrollX.value = item.scroll
        scrollX = item.scroll
        scrollY = item.scroll
        if (item.type != H) vm.h.value = item.scroll + screenHeight.toPx().toInt()
        vm.cycler.reset()
        (0 until batch).map { vm.cycler.update(it, xy = XY()) }
    }

    fun query() {
        when (toolbar.items.lastOrNull()?.query) {
            ITEM -> db.fetchShops(toolbar.items.last().id!!, ::load)
            ITEMS -> db.fetchTree(::load)
            SHOP -> db.fetchTree(toolbar.items.last().id!!, ::load)
            SHOPS -> db.fetchShops(::load)
            null -> {}
        }
    }

    val mx = MX(
        point = CopyOnWriteArrayList(),
        ruler = CopyOnWriteArrayList(),
        stack = IntArray(batch) { -1 },
        view = MX.View(
            details = mutableListOf(),
            expand = mutableListOf(),
            toggle = mutableListOf(),
            xy = mutableListOf()
        ),
        display = CopyOnWriteArrayList()
    )
    var list: List<ListInterface> = listOf()
    var scrollX = 0
    var scrollY = 0
    private var margin = 1f
    private var handler: VM.Type? = null

    init {
        vm.adjust.observeForever {
            if (!it) {
                mx.stack = IntArray(batch) { -1 }
                return@observeForever
            }
            adjust()
        }
        vm.filter.observeForever {
            filter()
            ruler()
            mx.point.indices.map { xy(it) }
            mx.stack = IntArray(batch) { -1 }
        }
        vm.sort.observeForever { sort() }
        vm.ratio.observeForever { zoom() }
        vm.ratioH.observeForever { zoom() }
        vm.ratioV.observeForever { zoom() }
        vm.scale.observeForever { zoom() }
        CoroutineScope(Dispatchers.IO).launch {
            vm.margin.collect { m ->
                margin = m
                mx.point.map { index ->
                    mx.display[index] =
                        (if (mx.toggle[index] != null) BUTTON else ITEM_HEIGHT) +
                                margin() to measure(index)
                }
                ruler()
                zoom()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(vm.fps.value!!.delay)
                scroll()
            }
        }
    }

    private fun exp(index: Int): AnnotatedString {
        if (!mx.view.expand[index].first) {
            mx.display[index] =
                mx.display[index].first to measure(index)
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
        mx.display[index] =
            mx.display[index].first to
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
            D -> {}
            V -> {
                var height = 0
                mx.point.mapIndexed { ix, index ->
                    mx.display[index] =
                        mx.display[index].first to mx.display[index].second + measure(index)
                    mx.view.expand[index] = mx.view.expand[index].first to exp(index)
                    mx.ruler[ix] = height
                    height += mx.display[index].first + mx.display[index].second
                    xy(ix)
                }
                vm.w.postValue(screenWidth.toPx().toInt())
                vm.h.postValue(height)
                mx.stack = IntArray(batch) { -1 }
            }
            H ->
                mx.point
                    .map {
                        xy(it)
                    }

            T ->
                mx.point
                    .map { xy(it) }
        }
    }

    fun display(type: VM.Type) {
        qqq("DISPL $handler ${type} ${list.size}")
        handler = type
        toolbar.items.last().type = type
        vm.cycler.reset()
        vm.scrollX.postValue(0)
        vm.scrollY.postValue(0)
        vm.type.postValue(type)
        scrollX = 0
        scrollY = 0
        when (type) {
            D -> {
                mx.point.indices.map {
                    mx.view.expand[it] = mx.view.expand[it].first to exp(it)
                    xy(it)
                }
                vm.w.postValue(screenWidth.toPx().toInt())
                vm.h.postValue(screenHeight.toPx().toInt())
            }
            H -> {
                vm.w.postValue((mapViewWidth * mx.ruler.size * vm.ratioH()).toInt())
                vm.h.postValue(((ITEM_HEIGHT + MARGIN * 2) * vm.ratioV()).toInt())
                mx.point.mapIndexed { ix, it ->
                    mx.view.expand[it] =
                        mx.view.expand[it].first to static(vm.ratioV(), list[it].description!!)
                    xy(ix)
                }
            }
            T -> {
                mx.point.indices.map {
                    xy(it)
                }
                vm.w.postValue(screenWidth.toPx().toInt())
                vm.h.postValue(((ITEM_HEIGHT.times(2)) * (mx.point.size / COLUMNS) * vm.ratioV()).toInt())
            }
            V -> {
                mx.point.indices.map {
                    mx.view.expand[it] = mx.view.expand[it].first to exp(it)
                    xy(it)
                }
                when (vm.exp.value!!) {
                    false -> {
                        vm.w.postValue(screenWidth.toPx().toInt())
                        vm.h.postValue(mx.point.sumOf { mx.display[it].let { d -> d.first + d.second } })
                    }
                    true -> {
                        vm.dim.postValue((0f to 0f) to (mx.point.size.toFloat().unaryMinus() to 0f))
                    }
                }

            }
        }
        mx.stack = IntArray(batch) { -1 }
    }

    private fun filter() {
        val toggle = mx.toggle
        mx.point.clear()
        mx.point.addAll(
            mx.display
                .withIndex()
                .filter {
                    !toggle.any { c ->
                        c.value.first < it.index &&
                                c.value.first + c.value.second.unaryMinus() > it.index
                    } && (
                            vm.filter.value == null ||
                                    vm.filter.value == true &&
                                    list[it.index].lng > 0 ||
                                    vm.filter.value == false &&
                                    list[it.index].lng < 0
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
            D, T,
            V -> {
                mx.ruler.clear()
                var height = 0
                mx.point.map {
                    mx.ruler.add(height)
                    height += mx.display[it].first + mx.display[it].second
                }
                vm.w.postValue(screenWidth.toPx().toInt())
                vm.h.postValue(height)
            }
            H -> {
                vm.w.postValue((mapViewWidth * mx.point.size * vm.ratioH()).toInt())
                vm.h.postValue((ITEM_HEIGHT * vm.ratioH()).toInt())
            }
        }
    }
    private fun scroll() {
        when (handler) {
            D -> {
                fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
                    val dx = x2 - x1
                    val dy = y2 - y1
                    return sqrt(dx * dx + dy * dy)
                }
                var c = 0
                val x = scrollX.toDouble() / ((ITEM_HEIGHT + MARGIN * 2) )
                val y = scrollY.toDouble() / ((ITEM_HEIGHT + MARGIN * 2) )
                val l = mx.point.withIndex()
                    .sortedBy {
                        distance(
                            list[it.value].lng,
                            list[it.value].lat,
                            x,
                            y
                        )
                    }
                    .take(batch)
                l.map {
                    //qqq("XYS $x $scrollX $scrollY ${it.index} ${list[it.value].title} ${list[it.value].origin}")
                    xy(it.index)
                    val ix = it.index
                    val index = it.value
                    val mod = c
                    c++
                    val xy = mx.view.xy.getOrNull(index) ?: return
                    val toggle = mx.view.toggle.getOrNull(index) ?: return
                    /*qqq(
                        "DDD sx:$scrollX sy:$scrollY d:$d x:${x.roundToInt()} y:${y.roundToInt()} ix:$ix index:$index mod:$mod ${list[index].lat}/${list[index].lng} ${list[index].title} ${
                            mx.view.details.getOrNull(
                                index
                            )?.title
                        } ${xy.x.toDp().round()} ${xy.y.toDp().round()}}"
                    )*/
                    mx.stack[mod] = ix
                    vm.cycler.update(mod = mod, description = mx.view.expand[index].second)
                    vm.cycler.update(mod = mod, details = mx.view.details[index])
                    vm.cycler.update(mod = mod, toggle = toggle)
                    vm.cycler.update(mod = mod, xy = xy)
                }
            }
            H -> {
                val r =
                    max(
                        (this@Screen.scrollX / ((mapViewWidth + margin()) * vm.ratioH())).toInt(),
                        0
                    )
                //qqq("SCROLL r:${r} ${scrollX.toDp().round()} S:${mx.stack.map { it }.toList()}")
                (r - batch / 2 until r + batch / 2)
                    .filter { it !in mx.stack }
                    .map { sync(it) }

            }
            T, V -> {
                val r = mx.ruler
                    .indexOfFirst {
                        it * vm.ratioV() * vm.scale.value!! > this@Screen.scrollY
                    }
                when (vm.exp.value!!) {
                    false -> {
                        fun down() {
                            var i = 0
                            while (i < batch / 2) {
                                val down = r - i
                                if (down >= 0 && !mx.stack.contains(down)) {
                                    sync(down)
                                    break
                                }
                                i += 1
                            }
                        }
                        fun up() {
                            var i = 0
                            while (i < batch / 2) {
                                val up = r + i
                                if (up in 0..mx.point.lastIndex &&
                                    !mx.stack.contains(up)
                                ) {
                                    sync(up)
                                    break
                                }
                                i += 1
                            }
                        }
                        down()
                        up()
                        //qqq("SCROLL r:${r} ${scrollY.toDp().round()} S:${mx.stack.map { it }.toList()}")

                    }
                    true -> {
                        mx.stack = IntArray(batch) { -1 }
                        val ixs = mutableListOf<Int>()
                        fun down() {
                            var i = 0
                            while (i < batch / 2) {
                                val down = r - i
                                if (down >= 0 && !mx.stack.contains(down)) {
                                    xy(down)
                                    ixs.add(down)
                                }
                                i += 1
                            }
                        }
                        fun up() {
                            var i = 0
                            while (i < batch / 2) {
                                val up = r + i
                                if (up in 0..mx.point.lastIndex &&
                                    !mx.stack.contains(up)
                                ) {
                                    xy(up)
                                    ixs.add(up)
                                }
                                i += 1
                            }
                        }
                       // qqq("SCROLL r:${r} ${scrollY.toDp().round()} S:${mx.stack.map { it }.toList()}")
                        down()
                        up()
                        pix(ixs)
                    }
                }

            }

            null -> {}
        }
    }

    private fun sort() {
        qqq("SORT")
        val comparator = compareBy<String> { it }
        val finalComparator = if (vm.sort.value!!) comparator.reversed() else comparator
        mx.point =
            CopyOnWriteArrayList(
                mx.point
                    .sortedWith { a, b ->
                        finalComparator.compare(
                            list[a].title, list[b].title
                        )
                    }
            )
        ruler()
        mx.point.indices.map { xy(it) }
        mx.stack = IntArray(batch) { -1 }
    }

    private fun zoom() {
        when (vm.type.value!!) {
            D -> {

            }
            T -> {
                val from =
                    max(
                        mx.point
                            .withIndex()
                            .indexOfLast { ITEM_HEIGHT * vm.ratioV() * it.index < this@Screen.scrollY }
                                - batch / 4,
                        0
                    )
                (from until min(from + batch, mx.point.size)).map { xy(it) }
            }
            V -> {
                val from = max(0, mx.ruler.indexOfFirst { it >= this@Screen.scrollY } - batch / 3)
                var sum = 0
                (from until min(from + batch, mx.point.size)).map { ix ->
                    val index = mx.point[ix]
                    val m = mx.display[index].second
                    mx.view.expand[index] =
                        mx.view.expand[index].first to exp(index)
                    //qqq("Z index:$index ix:$ix sum:$sum m:$m ${data.display[index].second} ${(data.display[index].second > m)}/${(data.display[index].second < m)} ${data.view.expand[index]}")
                    mx.ruler[ix] += sum
                    if (mx.display[index].second > m)
                        sum += mx.display[index].second - m
                    else if (mx.display[index].second < m)
                        sum -= m - mx.display[index].second
                    mx.view.xy[index] =
                        XY(
                            x = 0,
                            y = (mx.ruler[ix]).toInt(),
                            h = mx.display[index].first + mx.display[index].second,
                            w = screenWidth.toPx().toInt(),
                            i = ix
                        )
                }
            }

            H -> {
                val from =
                    max(
                        (this@Screen.scrollX / (mapViewWidth + margin()) / vm.ratioH()
                                ).toInt() - batch / 4,
                        0
                    )
                (from until min(from + batch, mx.point.size)).map {
                    val point = mx.point[it]
                    val index = it.mod(batch)
                    xy(point)
                    vm.cycler.update(index, mx.view.expand[point].second)
                }
            }
        }
    }
    private fun xy(ix: Int) {
        val index = mx.point[ix]
        mx.view.xy[index] =
            when (handler) {
                D ->
                    XY(
                        x = list[index].lng.toInt() * (ITEM_HEIGHT + MARGIN * 2) - scrollX,
                        y = list[index].lat.toInt() * (ITEM_HEIGHT + MARGIN * 2) - scrollY + screenHeight.toPx().toInt() / 3,
                        h = ITEM_HEIGHT + MARGIN * 2,
                        w = ITEM_HEIGHT + MARGIN * 2,
                        i = ix
                    )
                T ->
                    XY(
                        x = screenWidth.toPx().toInt() / COLUMNS * ix.mod(COLUMNS),
                        y = (((ITEM_HEIGHT.times(2)) * (ix / COLUMNS)) * vm.ratioV()).toInt(),
                        h = ITEM_HEIGHT * 2,
                        w = screenWidth.toPx().toInt() / COLUMNS,
                        i = ix
                    )
                V ->
                    when (vm.exp.value!!) {
                        false ->
                            XY(
                                x = 0,
                                y = mx.ruler[ix].toInt(),
                                h = mx.display[index].first + mx.display[index].second,
                                w = screenWidth.toPx().toInt(),
                                i = ix
                            )
                        true ->
                            XY(
                                x = 0,
                                y = mx.ruler[ix].toInt() - scrollY,
                                h = mx.display[index].first + mx.display[index].second,
                                w = screenWidth.toPx().toInt(),
                                i = ix
                            )
                    }
                else ->
                    XY(
                        x = (mapViewWidth * ix * vm.ratioH()).toInt(),
                        y = 0,
                        h = ITEM_HEIGHT + MARGIN * 2,
                        w = mapViewWidth,
                        i = ix
                    )
            }
    }

    fun load(l: List<ListInterface>) {
        list = l
        mx.display.clear()
        mx.stack = IntArray(batch) { -1 }
        mx.toggle.clear()
        mx.view.details = MutableList(list.size) { Details() }
        mx.view.expand = MutableList(list.size) { false to buildAnnotatedString { } }
        mx.view.toggle =
            toolbar.items.lastOrNull()?.toggle?.toMutableList() ?: MutableList(list.size) { false }
        qqq("LOAD ${mx.view.toggle}  " + list.size)
        mx.view.xy = MutableList(list.size) { null }
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
                mx.toggle[count] =
                    count to
                            if (mx.view.toggle[count]) (i - count).unaryMinus() else (i - count)
            }
            count++
        }
        list.mapIndexed { index, it ->
            mx.display.add(
                (if (mx.toggle[index] != null) BUTTON else ITEM_HEIGHT) + margin() to
                        measure(index)
            )
            mx.view.expand[index] = false to expandable(
                index = index,
                level = list[index].level,
                margin = margin,
                ratioH = vm.ratioH(),
                ratioV = vm.ratioV(),
                scale = vm.scale.value!!,
                txt = it.description
            )
            mx.view.details[index] =
                Details(
                    title = it.title!!.trim(),
                    origin = it.origin,
                    drawable = it.drawable,
                    level = it.level
                )
        }
        filter()
        ruler()
        mx.point.map { index ->
            val item = list[index]
            mx.view.details[index] =
                Details(
                    title = item.title!!.trim(),
                    origin = item.origin,
                    drawable = item.drawable,
                    level = item.level
                )
        }
        display(toolbar.items.last().type)
        this@Screen.scrollX = vm.scrollX.value!!
        this@Screen.scrollY = vm.scrollY.value!!
        toolbar.lock = false
        handler = vm.type.value
    }
    fun listen(listen: Boolean) {
        handler =
            if (listen) vm.type.value!!
            else null
    }

    enum class Tags(val key: String) {
        COLUMN("COLUMN"),
        IMAGE("IMAGE"),
        ROW("ROW"),
        TEXT("TEXT");
    }

    fun traverse(
        element: JsonElement,
        index: Int,
        x: Int,
        y: Int,
        w: Int,
        h: Int,
        tag: Tags? = null
    ): Offset? {
        when (element) {
            is JsonObject -> {
                val e = Tags.valueOf(element["element"]?.jsonPrimitive?.content?.uppercase()!!)
                val w1 = element["w"]?.jsonPrimitive?.int ?: w
                val h1 = element["h"]?.jsonPrimitive?.int ?: h
                val bg =  element["background"]?.jsonPrimitive?.content?.toColorInt()
                qqq("E $e $w1 $h1 x:$x y:$y w:$w h:$h")
                when (e) {
                    COLUMN, ROW ->
                        pixBox(
                            w1,
                            h1,
                            bg
                        )
                    TEXT ->
                        pixTextAutoHeight(
                            w1,
                            when (element["text"]!!.jsonPrimitive.content) {
                                "%DESCRIPTION%" ->
                                    expandable(
                                        index = index,
                                        level = list[index].level,
                                        margin = margin,
                                        ratioH = vm.ratioH(),
                                        ratioV = vm.ratioV(),
                                        scale = vm.scale.value!!,
                                        txt = list[index].description
                                    ).toString()
                                "%TITLE%" -> list[index].title ?: ""
                                "%ORIGIN%" -> list[index].origin ?: ""
                                else -> ""
                            },
                            bg,
                            size = element["size"]?.jsonPrimitive?.int
                        )
                    IMAGE ->
                        pixImage(
                            w1,
                            h1,
                            list[index].drawable?.let { context.getDrawable(it) },
                            bg
                        )
                }.also { bmp ->
                    bitmap = mergeBitmaps(
                        bitmap,
                        bmp,
                        x,
                        y
                    )
                    val children =
                        element["children"]?.jsonArray ?: return Offset(bmp.width.toFloat(), bmp.height.toFloat())
                    if (e == COLUMN) {
                        var yy = y
                        children.map {
                            yy += traverse(
                                element = it, index = index,
                                x = x,
                                y = yy,
                                w = w,
                                h = h - yy,
                                tag = e
                            )!!.y.toInt()
                        }
                    }
                    else if (e == ROW) {
                        var xx = x
                        children.map {
                            xx += traverse(
                                element = it,
                                index = index,
                                x = xx,
                                y = y,
                                w = w - xx,
                                h = h,
                                tag = e
                            )!!.x.toInt()
                        }
                    }
                    return Offset(bmp.width.toFloat(), bmp.height.toFloat())
                }
            }
            is JsonArray -> {
                for (item in element) {
                    traverse(item, index, w, h, w, h, tag)
                }
            }
            is JsonPrimitive -> {}
        }
        return null
    }

    private var bitmap = createBitmap(screenWidth.toPx().toInt(), screenHeight.toPx().toInt())
    @SuppressLint("UseKtx")
    fun pix(elements: List<Int>) {
        var scr = createBitmap(screenWidth.toPx().toInt(), screenHeight.toPx().toInt())
        elements.map { ix ->
            val index = mx.point.getOrNull(ix) ?: return
            val xy = mx.view.xy.getOrNull(index) ?: return
            val root = Json.parseToJsonElement(json)
            bitmap = createBitmap(xy.w, xy.h)
            traverse(root, index, 0, 0, xy.w, xy.h)
            scr = mergeBitmaps(scr, bitmap, xy.x, xy.y)
        }
        vm.bitmap(scr)
    }
    private fun sync(ix: Int) {
        val index = mx.point.getOrNull(ix) ?: return
        val mod =
            ix.mod(batch)
        val xy = mx.view.xy.getOrNull(index) ?: return
        val toggle = mx.view.toggle.getOrNull(index) ?: return
       /*
        qqq(
            "RS ix:$ix index:$index mod:$mod ${xy.x.toDp().round()} ${xy.y.toDp().round()} ${xy.w.toDp().round()} ${xy.h.toDp().round()} ${
                mx.view.details.getOrNull(
                    index
                )?.title
            }"
        )
        */
        mx.stack[mod] = ix
        vm.cycler.update(mod = mod, description = mx.view.expand[index].second)
        vm.cycler.update(mod = mod, details = mx.view.details[index])
        vm.cycler.update(mod = mod, toggle = toggle)
        vm.cycler.update(mod = mod, xy = xy)
    }

    fun expand(index: Int, expand: Boolean) {
        mx.view.expand[index] = expand to exp(index)
        ruler()
        mx.point.indices
            .map { xy(it) }
        mx.stack
            .map { sync(it) }
    }

    fun toggle(ix: Int) {
        val index = mx.point[ix]
        mx.view.toggle[index] = mx.toggle[index]!!.second > 0
        mx.toggle[index] =
            mx.toggle[index]!!.first to mx.toggle[index]!!.second.unaryMinus()
        filter()
        ruler()
        mx.point.indices
            .map { xy(it) }
        mx.stack = IntArray(batch) { -1 }
        if (mx.point.size < batch)
            (mx.point.size until batch)
                .map { mod -> vm.cycler.update(mod = mod, xy = XY()) }
    }
}
