package com.example.slovodnya

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import kotlin.math.ceil

/**
 * Рисует слово с ударением. Знак ударения (U+0301) не выводится шрифтом,
 * а рисуется отдельной чёрточкой над гласной — так он стоит выше и не слипается с точкой «і».
 */
object StressText {
    private const val ACUTE = '\u0301'

    private fun parse(text: String): Pair<String, Set<Int>> {
        val sb = StringBuilder()
        val stressed = HashSet<Int>()
        for (ch in text) {
            if (ch == ACUTE) {
                if (sb.isNotEmpty()) stressed.add(sb.length - 1)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString() to stressed
    }

    fun width(paint: Paint, text: String): Float = paint.measureText(parse(text).first)

    /** Место над строкой, которое нужно оставить под ударение. */
    fun accentSpace(paint: Paint): Float = paint.textSize * 0.46f

    fun draw(canvas: Canvas, text: String, x: Float, baseline: Float, paint: Paint) {
        val (plain, stressed) = parse(text)
        canvas.drawText(plain, x, baseline, paint)
        if (stressed.isEmpty()) return

        val size = paint.textSize
        val mark = Paint(paint).apply {
            style = Paint.Style.STROKE
            strokeWidth = (size * 0.075f).coerceAtLeast(2f)
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        val bounds = Rect()
        for (i in stressed) {
            val before = paint.measureText(plain, 0, i)
            val charW = paint.measureText(plain, i, i + 1)
            paint.getTextBounds(plain, i, i + 1, bounds)
            val glyphTop = baseline + bounds.top          // верх буквы (у «і» — верх точки)
            val cx = x + before + charW / 2f
            val bottom = glyphTop - size * 0.15f           // зазор между буквой и ударением
            val h = size * 0.20f
            val w = size * 0.13f
            canvas.drawLine(cx - w / 2f, bottom, cx + w / 2f, bottom - h, mark)
        }
    }

    /** Картинка со словом для виджета. */
    fun renderWord(context: Context, text: String, color: Int, sizeSp: Float, maxWidthDp: Int): Bitmap {
        val metrics = context.resources.displayMetrics
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sizeSp, metrics)
        }
        var w = width(paint, text)
        val maxW = (maxWidthDp * metrics.density).coerceAtLeast(60f)
        if (w > maxW) {
            paint.textSize = paint.textSize * maxW / w
            w = width(paint, text)
        }
        val fm = paint.fontMetrics
        val top = accentSpace(paint)
        val h = ceil(top + (-fm.ascent) + fm.descent).toInt().coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(ceil(w).toInt() + 6, h, Bitmap.Config.ARGB_8888)
        draw(Canvas(bmp), text, 3f, top - fm.ascent, paint)
        return bmp
    }
}

/** Вид для экрана приложения: слово с ударением, нарисованным над гласной. */
class StressWordView(
    context: Context,
    private val text: String,
    sizeSp: Float,
    color: Int,
    bold: Boolean
) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sizeSp, resources.displayMetrics
        )
    }
    private val baseSize = paint.textSize

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        paint.textSize = baseSize
        val fm = paint.fontMetrics
        val h = ceil(StressText.accentSpace(paint) + (-fm.ascent) + fm.descent).toInt()
        val w = ceil(StressText.width(paint, text)).toInt() + paddingLeft + paddingRight + 4
        setMeasuredDimension(resolveSize(w, widthSpec), resolveSize(h, heightSpec))
    }

    override fun onDraw(canvas: Canvas) {
        paint.textSize = baseSize
        val avail = (width - paddingLeft - paddingRight).toFloat()
        val need = StressText.width(paint, text)
        if (need > avail && avail > 0f) paint.textSize = baseSize * avail / need
        val fm = paint.fontMetrics
        val baseline = paddingTop + StressText.accentSpace(paint) - fm.ascent
        StressText.draw(canvas, text, paddingLeft.toFloat(), baseline, paint)
    }
}
