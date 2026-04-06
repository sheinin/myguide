package com.myguide

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withClip


fun pixBox(w: Int, h: Int, background: Int? = null): Bitmap {
    return createBitmap(w, h).also { background?.also { c -> it.eraseColor(c) } }
}

fun pixImage(w: Int, h: Int, drawable: Drawable?, background: Int? = null): Bitmap {
    val bitmap = createBitmap(w, h)
        .also { background?.also { c -> it.eraseColor(c) } }
    val canvas = Canvas(bitmap)
    drawable?.setBounds(0, 0, w, h)
    drawable?.draw(canvas)
    return bitmap
}



fun pixTextAutoHeight(
    w: Int,
    text: String,
    background: Int? = null,
    horizontalPadding: Int = 0,
    verticalPadding: Int = 0
): Bitmap {
    val contentWidth = (w - horizontalPadding * 2).coerceAtLeast(1)

    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.LEFT
    }

    // 1) Measure multiline text with wrapping width
    val layout = StaticLayout.Builder
        .obtain(text, 0, text.length, paint, contentWidth)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setIncludePad(false)
        .setLineSpacing(0f, 1f)
        .build()

    // 2) Create bitmap with measured height
    val h = (layout.height + verticalPadding * 2).coerceAtLeast(1)
    val bitmap = createBitmap(w, h).also { bg ->
        background?.let { bg.eraseColor(it) }
    }

    // 3) Draw from top-left (with optional padding)
    val canvas = Canvas(bitmap)
    canvas.translate(horizontalPadding.toFloat(), verticalPadding.toFloat())
    layout.draw(canvas)

    return bitmap
}

fun pixText(w: Int, h: Int, text: String, background: Int? = null): Bitmap {
    val mutableBitmap = createBitmap(w, h).also { background?.also { c -> it.eraseColor(c) } }
    val canvas = Canvas(mutableBitmap)
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.LEFT
    }

    val layout = StaticLayout.Builder
        .obtain(text, 0, text.length, paint, w)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setIncludePad(false)
        .setLineSpacing(0f, 1f)
        .build()

    // Draw from the top-left corner and clip to bitmap bounds.
    canvas.withClip(0, 0, w, h) {
        layout.draw(this)
    }
    return mutableBitmap
}


fun mergeBitmaps(back: Bitmap, front: Bitmap, x: Int, y: Int): Bitmap {
    val result = createBitmap(back.width, back.height, back.config!!)
    val canvas = Canvas(result)
    canvas.drawBitmap(back, 0f, 0f, null)
    canvas.drawBitmap(front, x.toFloat(), y.toFloat(), null)
    return result
}