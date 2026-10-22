package com.example.slovodnya

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.floor
import kotlin.math.sqrt

/** Рисует полупрозрачный фон с вышиванкой (ромбы крестиком) сверху и снизу. */
object OrnamentBackground {

    private val MOTIF = arrayOf(
        "....R....",
        "...RRR...",
        "..RR.RR..",
        ".RR.K.RR.",
        "RR.KKK.RR",
        ".RR.K.RR.",
        "..RR.RR..",
        "...RRR...",
        "....R...."
    )

    fun create(context: Context, wDp: Int, hDp: Int, pal: Palette): Bitmap {
        val density = context.resources.displayMetrics.density
        var w = (wDp * density).toInt().coerceAtLeast(1)
        var h = (hDp * density).toInt().coerceAtLeast(1)

        val maxPx = 1_200_000L
        var k = 1f
        if (w.toLong() * h > maxPx) {
            k = sqrt(maxPx.toFloat() / (w.toFloat() * h))
            w = (w * k).toInt()
            h = (h * k).toInt()
        }
        val s = density * k

        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        val corner = 18f * s
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = pal.bg }
        c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), corner, corner, fill)

        val inset = 4f * s
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * s
            color = pal.main
        }
        c.drawRoundRect(
            RectF(inset, inset, w - inset, h - inset),
            corner - inset, corner - inset, line
        )

        val p = 1.5f * s
        val period = 10f * p
        val bandH = 9f * p
        val margin = 10f * s
        val count = floor((w - 2 * margin) / period).toInt()
        if (count > 0) {
            val startX = (w - count * period) / 2f
            drawBand(c, startX, margin, count, p, period, pal)
            drawBand(c, startX, h - margin - bandH, count, p, period, pal)
        }
        return bmp
    }

    private fun drawBand(
        c: Canvas, startX: Float, y: Float, count: Int, p: Float, period: Float, pal: Palette
    ) {
        val paint = Paint().apply { isAntiAlias = false }
        for (i in 0 until count) {
            val x0 = startX + i * period
            for (row in MOTIF.indices) {
                for (col in MOTIF[row].indices) {
                    val ch = MOTIF[row][col]
                    if (ch == '.') continue
                    paint.color = if (ch == 'K') pal.accent else pal.main
                    val left = x0 + col * p
                    val top = y + row * p
                    c.drawRect(left, top, left + p + 0.5f, top + p + 0.5f, paint)
                }
            }
        }
    }
}
