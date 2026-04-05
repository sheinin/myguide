package com.myguide.data


import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.myguide.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VM {
    enum class FPS {
        FPS24,
        FPS30,
        FPS60;
        val delay: Long
            get() = when (this) {
                FPS24 -> 50L
                FPS30 -> 30L
                FPS60 -> 2500L
            }
        val drawable: Int
            get() = when (this) {
                FPS24 -> R.drawable._24fps
                FPS30 -> R.drawable._30fps
                FPS60 -> R.drawable._60fps
            }
        val next: FPS
            get() = when (this) {
                FPS24 -> FPS30
                FPS30 -> FPS60
                FPS60 -> FPS24
            }
    }
    enum class Type {
        D,
        H,
        T,
        V;
        val drawable: Int
            get() = when (this) {
                D -> R.drawable._2d
                V -> R.drawable._list
                H -> R.drawable._map
                T -> R.drawable._grid
            }
        val nextItem: Type
            get() = when (this) {
                V -> D
                else -> V
            }
        val nextShop: Type
            get() = when (this) {
                V -> H
                H -> T
                else -> V
            }
    }
    fun bitmap(bitmap: Bitmap) = _bitmap.update { bitmap }
    fun margin(margin: Float) = _margin.update { margin }
    fun margin(up: Boolean) = _margin.update { if (up) it + .1f else it - .1f }
    fun ratioH(): Float = ratioV.value ?: ratio.value!!
    fun ratioV(): Float = ratioV.value ?: ratio.value!!
    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    private val _margin = MutableStateFlow(1f)
    val adjust = MutableLiveData(false)
    val bitmap: StateFlow<Bitmap?> = _bitmap.asStateFlow()
    val dim = MutableLiveData((0f to 0f) to (0f to 0f))
    val type = MutableLiveData(Type.V)
    val fps = MutableLiveData(FPS.FPS60)
    val cycler = Cycler()
    val exp = MutableLiveData(false)
    val filter = MutableLiveData<Boolean?>(null)
    val margin: StateFlow<Float> = _margin.asStateFlow()
    val ratio = MutableLiveData(1f)
    val ratioH = MutableLiveData<Float?>(null)
    val ratioV = MutableLiveData<Float?>(null)
    val scale = MutableLiveData(1f)
    val sort = MutableLiveData(false)
    val stateX = MutableLiveData(0f)
    val scrollX = MutableLiveData(0)
    val scrollY = MutableLiveData(0)
    val w = MutableLiveData(0)
    val h = MutableLiveData(0)
    val description = MutableLiveData<String>()
    val details = MutableLiveData<Details>()
}