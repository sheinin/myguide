package android.myguide

import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import kotlin.text.toFloat
import android.animation.ValueAnimator
import android.graphics.drawable.TransitionDrawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject

class Toolbar {
    interface ScreenTools {
        fun build(
            id: String? = null,
            display: Settings.Display = Settings.Display.LIST,
            queryType: QueryType = QueryType.STORES,
        )
        fun getPosition(): Dp
        fun query()
        fun reset()
        fun update()
        //val root: ViewGroup
        val ident: Boolean
    }
    class Item(
        var id: String?,
        val queryType: QueryType,
        val slide: Boolean?,
        val title: String,
        var ident: Boolean,
        var display: Settings.Display,
        var position: Dp
    )
    private lateinit var activity: MainActivity

    private val _crumbs = mapOf(
        false to MutableLiveData(List(3) { "" }),
        true to MutableLiveData(List(3) { "" })
    )
    private val _title = mapOf(
        false to MutableLiveData(""),
        true to MutableLiveData("")
    )
    private var tracker = true
    var items = mutableListOf<Item>()
    val crumbs = _crumbs
    val title = _title
    fun back(vi: View? = null) {
        val i = items.lastIndex.dec()
        if (i == -1) vm.showSplash.value = true//activity.showSplash()
        else if (vi == null) goto(i)
        else goto(i)
    }
    fun blank() {
        vm.toolbar.items.add(
            Item(
                id = null,
                ident = false,
                display = Settings.Display.LIST,
                title = "",
                position = 0.dp,
                queryType = QueryType.STORES,
                slide = null
            )
        )
    }
    fun clear() {
        items.clear()
        tracker = true
    }
    fun click(ix: Int) {
        /*if (ix == -1) splash()
        else if (ix == 1 && items.size > 4)
            vmm.dialog.also { di ->
                di.show(
                    anchor = vmm.toolbar.last!!.root.findViewById(R.id.tv_middle_crumb),
                    fade = false,
                    items = items.subList(ix, items.size.dec().dec()).mapIndexed { i, it -> it.title to { if (ix + i < items.lastIndex) vmm.toolbar.goto(ix + i) } },
                    posX = 0,
                    posY = 1,
                    width = px2dp((screenWidth.toFloat() * .75).toInt()),
                    yOff = 12f
                )
            }
        else if (ix == 2 && items.size > 4) goto(items.lastIndex.dec())
        else*/ goto(ix)
    }
    fun positionGet(): Dp? = if (items.isNotEmpty()) items.last().position else null
    fun init(activity: MainActivity) { this.activity = activity }
    val last: ScreenTools?
        get() =
            if (items.isEmpty()) null
            else activity.screen[items.last().ident]
    fun navigate(
        id: String? = null,
        display: Settings.Display = Settings.Display.LIST,
        queryType: QueryType = QueryType.STORES,
        slide: Boolean? = null,
        title: String
    ) {
        items.mapIndexed { ix, it ->
            if (it.queryType == queryType && it.id == id) {
              //  goto(ix)
                return
            }
        }
        if (items.isNotEmpty()) items.last().position = last!!.getPosition()
        val current = activity.screen[tracker]
        tracker = !tracker
        val item =
            Item(
                id = id,
                ident = tracker,
                display = display,
                title = title,
                position = 0.dp,
                queryType = queryType,
                slide = slide
            )
        val next = activity.screen[tracker]!!
        crumbs[tracker]!!.value =
            listOf(
                items.getOrNull(0)?.title ?: "",
                if (items.size > 3) activity.resources.getString(R.string.ellipsis) else items.getOrNull(1)?.title ?: "",
                if (items.size > 2) items.last().title else ""
            )
        _title[tracker]!!.value = title
        cached = true
        items.add(item)
        vm.current.value = next.ident
        qqq("ID"+next.ident)
    //    if (slide != null) {
            next.build(
                id = id,
                display = display,
                queryType = queryType,
            )
            current!!.reset()
            sleep(10) {
                next.query()
               // if (slide) slideIn(current.root, next.root) { next.query() }
               // else animCross(current.root, next.root) { next.query() }
            }
      //  }
    }
    fun splash() {

        //activity.showSplash()
    }
    private var cached = true
    private fun goto(ix: Int) {
        val current = activity.screen[tracker]!!
        val item = items[ix]
        val last = items.last()
        val next = activity.screen[!tracker]!!
        qqq("GOTO ix:" +ix+" current:" + current.ident +" next:" + next.ident + " cache:"+ cached +( ix == items.lastIndex.dec()))
        cached = cached && ix == items.lastIndex.dec()
        items.subList(ix.inc(), items.size).clear()
        tracker = !tracker
        current.reset()
        if (item.display.isMap) vm.mapShowing.value = true
        if (cached) {
            cached = false
            next.update()
        //    if (last.slide == true) slideOut(current.root, next.root) { next.update() }
          //  else animCross(current.root, next.root) { next.update() }
        } else {
            _crumbs[next.ident]!!.value =
                listOf(
                    if (items.size > 1) items[0].title else  "",
                    if (items.size > 4) activity.resources.getString(R.string.ellipsis)
                    else if (items.size == 2) ""
                    else items.getOrNull(1)?.title ?: "",
                    if (items.size > 3) items[items.lastIndex.dec()].title else ""
                )
            _title[next.ident]!!.value = item.title
            next.build(
                id = item.id,
                display = item.display,
                queryType = item.queryType
            )
            sleep(1) {
                next.query()
            //    if (last.slide == true) slideOut(current.root, next.root) { next.query() }
              //  else animCross(current.root, next.root) { next.query() }
            }
        }
    }
}