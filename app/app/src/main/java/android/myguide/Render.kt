package android.myguide

import android.myguide.Settings.Display.*
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
    val bind: ViewModel.Screen,

//    val scrollY: LockableNestedScrollView,
 //   val scrollX: HorizontalScrollView,
    //val setListIsEmpty: (Boolean?) -> Unit,
    val getSettings: () -> Settings
) {
    private val cycler = bind.cycler
    private var display: Settings.Display = LIST
    private val mapHeight = 150.dp
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
        fun set(map: Boolean): Handlers =
            if (map) MAP
            else FULL
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
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {

                delay(handler.delay)
                        jobQue.iterator().also {
                            if (it.hasNext()) {
                                val next = it.next()
                                activity.runOnUiThread {

                                    data.vm.getOrNull(next.value)?.let { v ->

                                        //qqq("JQ "+v.y+" " +next.key+" "+v.title+ " "+next.key)

                                        cycler.updateItem(next.key, v)
                                        //cycler.hidden[next.key].value = false
                                    } ?: return@runOnUiThread
                                    pics[next.key] = next.value
                                }
                                it.remove()
                            }
                        }
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
        //calibrate(vm.allItems.value!!)
    }
    enum class DisplayType {
        DEFAULT,
        CHAIN,
        PLAYER;
        val height: Dp
            get() =
                when (this) {
                    DEFAULT -> 60.dp
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
            var measure: Int = 0,
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
            text = list[ix].title!!,
            style = typography.bodyMedium,
            constraints = Constraints(maxWidth = (screenWidth - 80.dp).toPx().toInt()),
            density = density,
            fontFamilyResolver = fontFamilyResolver,
        )
        val m = 12.dp * paragraph.lineCount.dec()
       // qqq("M"+list[ix].title + " "+m + " "+paragraph.lineCount)
        data.display[ix].height += m
       // data.display[ix].measure = m

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
                offset = 2
                val x = data.display.sumOf { h -> h.height.value.toInt() }
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
                    data.display.size < position / screenWidth + lim &&
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
        ///qqq("SL "+start + " "+limit + " ")
        CoroutineScope(Dispatchers.IO).launch {
            /*when (getSettings().display) {
                Display.D3 ->
                    (start until limit).map {
                        vm(data.point[it])
                        delay(1L)
                        activity.runOnUiThread { renderYD3(it) }
                    }
                MAP ->
                    (start until limit).map {
                        vm(data.point[it])
                        delay(1L)
                        activity.runOnUiThread { renderX(it) }
                    }
                LIST ->*/
                    (start until limit).map {
                        vm(data.point[it])
                        delay(1L)
                        renderY(it)
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
        pics.fill(-1)
        observe(display)
        if (display == MAP) {
            handler = handler.set(true)
            bind.w.value = screenWidth * data.ruler.size
            bind.h.value = mapHeight
            data.vm.mapIndexed { ix, it ->
                it.x = screenWidth * ix
                it.y = 0.dp
            }
            (start until end).map { renderX(it) }
        } else {
            handler = handler.set(false)
            data.vm.mapIndexed { ix, it ->
                it.x = 0.dp
                it.y = data.ruler[ix]
            }
            bind.w.value = screenWidth
            bind.h.value = height
            (start until end).map { renderY(it) }
        }
    }
    fun load(list: List<ListInterface>, display: Settings.Display = Settings.Display.LIST, callback: (() -> Unit)? = null) {
        this.display = display
        this.list = list
        ordinal = 0
        handler = handler.set(display == MAP)
        data.vm = MutableList(this@Render.list.size) { cycler.item }
      //  (0 until batch).map { cycler.hidden[it].value = true }
        val position = vm.toolbar.positionGet()
        if ((position ?: 0.dp) > 0.dp) {
           /* if (display == MAP) {
                view.layoutParams.height = mapHeight
                view.layoutParams.width = position.second + screenWidth
                scrollX.post {
                    scrollX.scrollTo(max(position.second, 0), 0)
                    job(position.second, callback)
                }
            } else {
                view.layoutParams.height = position.second + screenHeight
                view.layoutParams.width = screenWidth
                scrollY.post {
                    scrollY.scrollTo(0, max(0, position.second))
                    job(position.second, callback)
                }
            }

            */
            vm.toolbar.items.lastOrNull()?.position = position!!
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

        if (getSettings().display == Settings.Display.D3) {
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
                bind.w.value = screenWidth * data.ruler.size
                bind.h.value = mapHeight
            }
            else {
                bind.w.value = screenWidth
                bind.h.value = height//filter.sumOf { h -> h.height } //+ dp2px(16f)// + if (isStatic) divider else dp2px(16f)
            }
        }
    }
    fun listen(listen: Boolean) {
        handler = if (listen) handler.set(display == MAP)
        else Handlers.NONE
    }
    fun observeY(y: Dp) {
        max(data.ruler.indexOfLast { it < y } - offset, 0).also { f ->
            (f .. min(data.ruler.size, f + batch).dec()).filter {
                !data.stack.contains(it)
            }.map { renderY(it) }
        }
    }
    fun observe(display: Settings.Display?) {
       /* val obs =
            when (display) {
                null -> observerNull
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
        //qqq("RX ix:$ix point:"+data.point.getOrNull(ix) + " index:"+index + " name:"+data.vm.getOrNull(point)?.line1aText?.value+data.stack.map { it }.toList().joinToString("."))
        que(index = index, ix = disp.ordinal)
    }
    private fun renderY(ix: Int) {
        val point = data.point.getOrNull(ix) ?: return
        val disp = data.display.find { it.ordinal == point } ?: return
        val index = ix.mod(batch)
        //qqq("RF "+disp.ordinal+" "+point+" "+ix+" "+data.vm.getOrNull(point)?.title)
        data.stack[index] = ix
        que(index = index, ix = disp.ordinal)
    }

    private fun que(index: Int, ix: Int) {
        jobQue.remove(index)
        jobQue[index] = ix
      //  activity.picasso.cancelRequest(elements[index].thumbnail)
        spinners[index] = ix
    }
    fun calibrate(list: List<ListInterface>) {
        this.list = list
        (0 until batch).map {
            data.display.add(
                Data.Display(
                    height = 100.dp,
                    title = "",
                    ordinal = it,
                    drawable = 0,
                    type = DisplayType.DEFAULT
                )
            )
            vm(it)
        }
        ruler()
        sleep {
            (0 until batch).map { renderY(it) }
            reset()
        }
    }

    private fun vm(ix: Int) {
        val item = list.getOrNull(ix) ?: return
        val disp = data.display.getOrNull(ix) ?: return
      //  qqq(">"+item.id + " "+ix+" "+item.title  +  " ")
        val vm =
            ViewModel.Cycler.Item(
                id = item.id!!,
                title = item.title!!,
                subtitle = item.origin,
                description = item.description,
                x = 0.dp,
                y = data.ruler.getOrNull(ix) ?: 0.dp,
                w = screenWidth,
                h = disp.height,
            )
        data.vm.getOrNull(ix)?.also { data.vm[ix] = vm }
    }

}