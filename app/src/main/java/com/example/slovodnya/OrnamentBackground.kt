package com.example.slovodnya

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.floor
import kotlin.math.sqrt

/** Рисует полупрозрачный фон с вышиванкой (ромбы крестиком) сверху и снизу. */
object OrnamentBackground {

    // Прозрачность фона: 0 = полностью прозрачный, 255 = непрозрачный
    private const val BG_ALPHA = 184
    private val BG = Color.argb(BG_ALPHA, 246, 240, 227)
    private val RED = Color.rgb(181, 38, 46)
    private val BLACK = Color.rgb(34, 34, 34)

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

    fun create(context: Context, wDp: Int, hDp: Int): Bitmap {
        val density = context.resources.displayMetrics.density
        var w = (wDp * density).toInt().coerceAtLeast(1)
        var h = (hDp * density).toInt().coerceAtLeast(1)

        // не делаем слишком большую картинку (лимит виджетов по памяти)
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
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = BG }
        c.drawRoundRect(RectF(0f, 0f, w.toFloat(), h.toFloat()), corner, corner, fill)

        val inset = 4f * s
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * s
            color = RED
        }
        c.drawRoundRect(
            RectF(inset, inset, w - inset, h - inset),
            corner - inset, corner - inset, line
        )

        val p = 1.5f * s            // размер одного «крестика»
        val period = 10f * p        // ширина ромба + промежуток
        val bandH = 9f * p
        val margin = 10f * s
        val count = floor((w - 2 * margin) / period).toInt()
        if (count > 0) {
            val startX = (w - count * period) / 2f
            drawBand(c, startX, margin, count, p, period)
            drawBand(c, startX, h - margin - bandH, count, p, period)
        }
        return bmp
    }

    private fun drawBand(c: Canvas, startX: Float, y: Float, count: Int, p: Float, period: Float) {
        val paint = Paint().apply { isAntiAlias = false }
        for (i in 0 until count) {
            val x0 = startX + i * period
            for (row in MOTIF.indices) {
                for (col in MOTIF[row].indices) {
                    val ch = MOTIF[row][col]
                    if (ch == '.') continue
                    paint.color = if (ch == 'K') BLACK else RED
                    val left = x0 + col * p
                    val top = y + row * p
                    c.drawRect(left, top, left + p + 0.5f, top + p + 0.5f, paint)
                }
            }
        }
    }
}
