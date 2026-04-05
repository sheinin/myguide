package com.myguide

import android.R.attr.height
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap


fun pixBox(w: Int, h: Int, background: Int? = null): Bitmap {
    return createBitmap(w, h).also { background?.also { c -> it.eraseColor(c) } }
}
fun pixImage(w: Int, h: Int, drawable: Drawable?, background: Int? = null): Bitmap {

    // 1. Create a blank bitmap of the desired size
    val bitmap = createBitmap(w, h)
        .also { background?.also { c -> it.eraseColor(c) } }

    // 2. Create a Canvas backed by that bitmap
    val canvas = Canvas(bitmap)

    // 3. Set the drawable's bounds to match the target size
    drawable?.setBounds(0, 0, w, h)

    // 4. Draw the drawable onto the canvas
    drawable?.draw(canvas)

    return bitmap

    //return (drawable?.toBitmap(w, h) ?: createBitmap(w, h))
      //  .also { background?.also { c -> it.eraseColor(c) } }

}
fun pixText(w: Int, h: Int, text: String, background: Int? = null): Bitmap {
    val mutableBitmap = createBitmap(w, h).also { background?.also { c -> it.eraseColor(c) } }

    val canvas = Canvas(mutableBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.LEFT
    }

    // Draw from the start of the canvas; y uses baseline to avoid clipping ascenders.
    val x = 0f
    val y = -paint.ascent()

    // 4. Draw text onto the bitmap
    canvas.drawText(text, x, y, paint)

    return mutableBitmap
}


fun mergeBitmaps(back: Bitmap, front: Bitmap, x: Int, y: Int): Bitmap {
    val result = createBitmap(back.width, back.height, back.config!!)
    val canvas = Canvas(result)
    canvas.drawBitmap(back, 0f, 0f, null)
    canvas.drawBitmap(front, x.toFloat(), y.toFloat(), null)
    return result
}