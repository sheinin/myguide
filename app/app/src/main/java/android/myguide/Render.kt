package android.myguide

import android.R.attr.scrollX
import android.R.attr.scrollY
import android.myguide.Settings.Display.*
import android.view.ViewTreeObserver
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class Render(
    val activity: MainActivity,
    //val view: RelativeLayout,
   // val bind: ViewModel.Screen,
    val screen: Screen,
//    val scrollY: LockableNestedScrollView,
 //   val scrollX: HorizontalScrollView,
    //val setListIsEmpty: (Boolean?) -> Unit,
    val getSettings: () -> Settings
) {
    val bind = vm.screen[screen.ident]!!
    private val cycler = bind.cycler
    private var display: Settings.Display = LIST
    private val mapHeight = 116.dp
    private val pics = IntArray(batch) { -1 }
    private val spinners = (0 until batch).associateWith { -1 }.toMutableMap()
    val data = Data(
        point = CopyOnWriteArrayList(),
        ruler = CopyOnWriteArrayList(),
        stack = IntArray(batch) { -1 },
        vm = mutableListOf(),
        display = CopyOnWriteArrayList()
    )
    private enum class Handlers {
        FULL,
        MAP,
        NONE;
        fun set(map: Boolean): Handlers {
            qqq("set "+map)
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
    private var observer: ViewTreeObserver.OnScrollChangedListener? = null
    private var ordinal = 0
    var offset = 0
    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(handler.delay)
                    mapOf(
                        Handlers.FULL to {
                            jobQue.iterator().also {
                                if (it.hasNext()) {
                                    val next = it.next()
                                    //activity.runOnUiThread {
                                        data.vm.getOrNull(next.value)?.let { v ->
                                            //qqq("JQ "+v.y+" " +next.key+" "+v.title+ " "+next.key)
                                            cycler.updateItem(next.key, v)
                                        } ?: return@also
                                        pics[next.key] = next.value
                                    //}
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
        calibrate()
    }
    enum class DisplayType {
        DEFAULT,
        CHAIN,
        PLAYER;
        val height: Dp
            get() =
                when (this) {
                    DEFAULT -> 106.dp
                    CHAIN -> 100.dp
                    PLAYER -> 100.dp
                }
    }
    data class Data(
        var collapse: MutableMap<Int, Pair<Int, Int>> = mutableMapOf(),
        var display: CopyOnWriteArrayList<Display>,
        var point: CopyOnWriteArrayList<Int>,
        var ruler: CopyOnWriteArrayList<Dp>,
        var stack: IntArray,
        var vm: MutableList<ViewModel.Cycler.Item>
    ) {
        class Display(
            val distance: Int = 0,
            val drawable: Int,
            val ordinal: Int,
            val title: String,
            var type: DisplayType,
            var height: Dp,
            var measure: Dp = 0.dp,
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
            DisplayType.DEFAULT
        data.display.add(
            Data.Display(
                distance = 0,
                drawable = 0,
                height = displayType.height,
                title = item.title!!,
                ordinal = ordinal,
                type = displayType
            )
        )
        //qqq("?? "+item.displayName+ " "+ordinal+" "+px2dp(data.display.last().height)+item.currentTime)//.+" "
        ordinal += 1
    }
    private fun measure(ix: Int) {
        //val item = list.getOrNull(ix) ?: return

        val paragraph = androidx.compose.ui.text.Paragraph(
            text = list[ix].description!!,
            style = typography.bodyMedium,
            constraints = Constraints(maxWidth = (screenWidth - 132.dp).toPx().toInt()),
            density = density,
            fontFamilyResolver = fontFamilyResolver,
        )

        if (paragraph.lineCount > 2) {
            val m = 21.dp * (paragraph.lineCount - 2)
            // qqq("M"+list[ix].title + " "+m + " "+paragraph.lineCount)
           // data.display[ix].height += m
            data.display[ix].measure = m
        }
    }
    private fun job(position: Dp = 0.dp, callback: (() -> Unit)? = null) {
        @Suppress("UNUSED_VARIABLE")
        val ini: (Int) -> Unit
        val ms: ((Int) -> Unit)?
        var limit: Int
        var start = 0
        when (display) {
            D3 -> {
                ini = ::ini1
                ms = ::measure
                offset = 6
                /*(0 until position + screenHeight / DisplayType.D3.height * 3).map {
                    val s = start++
                    ini.invoke(s)
                    ms.invoke(s)
                }
                start = max(start - min(list.size, batch), 0)*/
            }
            LIST -> {
                ini = ::ini1
                ms = ::measure
                offset = 4
                val lim = position + screenHeight
                while (
                    data.display
                        .sumOf { h -> h.height.value.toDouble() } < lim.value.toDouble()
                    &&
                    start < list.size
                ) {
                    val s = start++
                    ini.invoke(s)
                    ms.invoke(s)
                }
                //qqq("LIM "+list.size+" "+start +" "+ min(list.size, batch)+" "+lim +" " +position + " "+screenHeight)
                start = max(start - min(list.size, batch), 0)
            }
            MAP -> {
                ini = ::ini1
                ms = ::measure
                offset = 5
                val lim = min(list.size, batch / 3)
                while (
                    data.display.size < position / (screenWidth - 90.dp) + lim &&
                    start < list.size
                ) {
                    val s = start++
                    ini.invoke(s)
                    ms.invoke(s)
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
        if (limit == 0) {
            qqq("job empty")
            activity.runOnUiThread { callback?.invoke() }
            (0 until list.size).map { vm(it) }
            return
        }
        qqq("SL "+start + " "+limit + " "+list.size)
        CoroutineScope(Dispatchers.IO).launch {
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
                        vm(data.point[it])
                        delay(1L)
                        renderYSync(it)
                    }
            }
            observe(display)
            delay(50)
            activity.runOnUiThread {
                callback?.invoke()
                val t = System.currentTimeMillis()
                (data.display.size until list.size).map {
                    ini(it)
                    ms?.invoke(it)
                }
                ruler(false)
                data.vm.withIndex().filter { it.value.title == "" }.map { vm(it.index) }
               // qqq("job ms:${(System.currentTimeMillis() - t)} pos:$position start:$start limit:$limit ruler:${data.ruler.size} static:$isStatic")
            }

        }
    }
    fun display(display: Settings.Display) {
        this.display = display
        val start = 0
        val end = min(batch, data.ruler.size)
        vm.toolbar.items.last().display = display
        bind.display.value = display
        pics.fill(-1)
        observe(display)
        if (display == MAP) {
            handler = handler.set(true)
            bind.w.value = (screenWidth - 90.dp) * data.ruler.size
            bind.h.value = mapHeight
            data.vm.mapIndexed { ix, it ->
                it.x = (screenWidth - 90.dp) * ix
                it.y = 0.dp
                it.w = screenWidth - 90.dp
                it.h = data.display[ix].type.height
            }
            (start until end).map { renderX(it) }
        } else {
            handler = handler.set(false)
            data.vm.mapIndexed { ix, it ->
                it.x = 0.dp
                it.y = data.ruler[ix]
                it.w = screenWidth
                it.h = data.display[ix].height + if (it.more!!) data.display[ix].measure else 0.dp
            }
            bind.w.value = screenWidth
            bind.h.value = height
            (start until end).map { renderYSync(it) }
        }
    }
    fun load(list: List<ListInterface>, callback: (() -> Unit)? = null) {
        this.display = bind.display.value!!
        this.list = list
        ordinal = 0
        handler = handler.set(display == MAP)
        data.vm = MutableList(this@Render.list.size) { cycler.item }
        val position = vm.toolbar.items.last().position
        qqq("POS"+position)
        if (position > 0.dp) {
            activity.runOnUiThread {
                if (display == MAP) {
                    bind.w.value = position + screenWidth
                    bind.h.value = mapHeight
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
        val list = filter.subList(data.ruler.size, filter.size)

        if (getSettings().display == D3) {
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
            if (display == MAP) {
                bind.w.postValue((screenWidth - 90.dp) * data.ruler.size)
                bind.h.postValue(mapHeight)
            }
            else {
                bind.w.postValue(screenWidth)
                bind.h.postValue(height)
            }

            qqq("RH "+height )
        }
    }
    fun listen(listen: Boolean) {
        handler = if (listen) handler.set(display == MAP)
        else Handlers.NONE
    }
    fun observe(pos: Dp) {
        //qqq("Y "+pos + display+screen.ident + " " +offset)
        when (display) {
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
    fun observe(display: Settings.Display?) {
     /*   val obs =
            when (display) {
                null -> observeX
                D3 -> observerD3
                else -> observerFull
            }
        if (observer != null) scrollY.viewTreeObserver.removeOnScrollChangedListener(observer)
        if (display != MAP) scrollY.viewTreeObserver.addOnScrollChangedListener(obs)
        observer = obs
       */
    }
    fun reset(refresh: Boolean = false) {
        data.collapse.clear()
        data.display.clear()
        data.point.clear()
        data.ruler.clear()
        data.stack = IntArray(batch) { -1 }
        data.vm.clear()
        handler = Handlers.NONE
        height = 0.dp
        job?.cancel()
        jobQue.clear()
     /*   elements.map {
            it.root.findViewById<ImageView>(R.id.iv_blur).setImageResource(R.color.transparent)
            it.root.tag = null
            it.thumbnail.alpha = 0f
        }

      */
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
        //que(index = index, ix = disp.ordinal)
        cycler.updateItem(index, data.vm[point])
    }
    private fun renderYSync(ix: Int) {
        val point = data.point.getOrNull(ix) ?: return
        val disp = data.display.find { it.ordinal == point } ?: return
        val index = ix.mod(batch)
        qqq("RS "+disp.ordinal+" "+point+" "+ix+" "+data.vm.getOrNull(point)?.title)
        data.stack[index] = ix
        cycler.updateItem(index, data.vm[point])
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
    fun ellipsis(index: Int) {
        val point = data.point[data.stack[index]]


        val disp = data.display.find { it.ordinal == point } ?: return

        qqq("E "+index+ " "+point+" " +list[point].title + " "+disp.title + " "+disp.measure)
        height = 0.dp
        if (data.vm[point].more!!) disp.height -= disp.measure
        else disp.height += disp.measure
        ruler()
        val to = min(point + batch / 2, list.size)
        vm(point, !data.vm[point].more!!)
        renderYSync(point)
        data.stack.filter { it != point }.map {
            vm(it)
            renderYSync(it)
        }/*
        (point until to).map {
            vm(it)
            renderYSync(it)
        }*/
        (to until list.size).map { vm(it) }

    }
    fun calibrate() {
        qqq("ca")
return
        data.vm = MutableList(batch) { cycler.item }
        val l = mutableListOf<ListInterface>()
        (0 until batch).map {
            l += object : ListInterface {
                override val title: String = "calibrate"
                override val origin: String = ""
                override val description: String = ""
                override val drawable: Int = 0
                override val id: String = ""
            }
            data.display.add(
                Data.Display(
                    height = 100.dp,
                    title = "",
                    ordinal = it,
                    drawable = 0,
                    type = DisplayType.DEFAULT
                )
            )

            list = l

            vm(it)

            cycler.updateItem(it, data.vm[it])
        }

    }

    private fun vm(ix: Int, more: Boolean? = null) {
        val item = list.getOrNull(ix) ?: return
        val disp = data.display.getOrNull(ix) ?: return
        //qqq(">"+item.id + " "+ix+" "+item.title  +  " "+if (display == MAP) null else more)
        val vm =
            ViewModel.Cycler.Item(
                id = item.id!!,
                title = item.title!!,
                subtitle = item.origin,
                description = item.description,
                drawable = item.drawable,
                more =
                    if (display == MAP) null
                    else if (more != null) more else data.vm.getOrNull(ix)?.more ?: false,
                x = if (display == MAP) (screenWidth - 90.dp) * disp.ordinal else 0.dp,
                y = if (display == MAP) 0.dp else data.ruler.getOrNull(ix) ?: 0.dp,
                w = screenWidth - if (display == MAP) 90.dp else 0.dp,
                h = disp.height,
            )
        data.vm.getOrNull(ix)?.also { data.vm[ix] = vm }
    }

}