package com.myguide.data

import androidx.compose.ui.text.AnnotatedString
import com.myguide.data.Cycler.*
import java.util.concurrent.CopyOnWriteArrayList


data class MX(
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
        other as MX
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