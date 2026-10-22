package com.example.slovodnya

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    override fun onResume() {
        super.onResume()
        build()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun params(top: Int = 0, wrap: Boolean = false): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            if (wrap) LinearLayout.LayoutParams.WRAP_CONTENT else LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(top) }

    private fun label(
        text: String, sizeSp: Float, color: Int,
        bold: Boolean = false, italic: Boolean = false
    ): TextView = TextView(this).apply {
        this.text = text
        textSize = sizeSp
        setTextColor(color)
        val style = when {
            bold && italic -> Typeface.BOLD_ITALIC
            bold -> Typeface.BOLD
            italic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        setTypeface(null, style)
    }

    private fun pill(text: String, pal: Palette, onClick: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            setTextColor(pal.onMain)
            background = GradientDrawable().apply {
                setColor(pal.main)
                cornerRadius = dp(24).toFloat()
            }
            setPadding(dp(18), dp(8), dp(18), dp(8))
            setOnClickListener { onClick() }
        }

    private fun build() {
        val pal = Themes.current(this)
        window.setBackgroundDrawable(ColorDrawable(pal.appBg))
        window.statusBarColor = pal.appBg
        val ui = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility =
            if (pal.light) ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else ui and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

        val today = Words.today(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(40))
        }

        // --- Слово дня ---
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(18))
            background = GradientDrawable().apply {
                setColor(pal.card)
                cornerRadius = dp(18).toFloat()
                setStroke(dp(2), pal.main)
            }
        }
        card.addView(label("СЛОВО ДНЯ", 12f, pal.sub, bold = true))
        card.addView(label(today.w, 34f, pal.text, bold = true), params(4))
        card.addView(label(today.t, 18f, pal.text), params(2))
        if (today.e.isNotEmpty()) {
            card.addView(label(today.kindLabel, 12f, pal.sub, bold = true), params(14))
            card.addView(label(today.e, 16f, pal.text, italic = true), params(2))
            if (today.a.isNotEmpty()) {
                card.addView(label("— ${today.a}", 13f, pal.sub), params(2))
            }
        }
        val isFav = Prefs.isFavorite(this, today.w)
        card.addView(
            pill(if (isFav) "★  В избранном" else "☆  В избранное", pal) {
                Prefs.toggleFavorite(this, today.w)
                WordWidgetProvider.updateAll(this)
                build()
            },
            params(16, wrap = true)
        )
        root.addView(card, params())

        // --- Избранное ---
        val favs = Prefs.favorites(this).mapNotNull { Words.find(this, it) }.sortedBy { it.w }
        root.addView(label("ИЗБРАННОЕ (${favs.size})", 12f, pal.sub, bold = true), params(28))
        if (favs.isEmpty()) {
            root.addView(
                label("Пока пусто. Нажмите ☆ у слова дня или на самом виджете.", 15f, pal.sub),
                params(8)
            )
        }
        for (w in favs) root.addView(favRow(w, pal), params(10))

        // --- Тема ---
        root.addView(label("ТЕМА", 12f, pal.sub, bold = true), params(28))
        val group = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        Themes.all.forEachIndexed { i, t ->
            val rb = RadioButton(this).apply {
                id = 1000 + i
                text = t.name
                textSize = 17f
                setTextColor(pal.text)
                buttonTintList = ColorStateList.valueOf(pal.main)
                setPadding(dp(8), dp(10), dp(8), dp(10))
            }
            group.addView(rb)
        }
        group.check(1000 + Prefs.theme(this).coerceIn(0, Themes.all.lastIndex))
        group.setOnCheckedChangeListener { _, checkedId ->
            Prefs.setTheme(this, checkedId - 1000)
            WordWidgetProvider.updateAll(this)
            build()
        }
        root.addView(group, params(4))

        root.addView(
            label(
                "Виджет добавляется долгим тапом по рабочему столу: «Виджеты» → «Слова дня». " +
                    "Звёздочка на виджете добавляет слово в избранное. " +
                    "Если растянуть виджет по высоте, под словом появится пример.",
                13f, pal.sub
            ),
            params(28)
        )

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun favRow(w: Word, pal: Palette): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(8), dp(12))
            background = GradientDrawable().apply {
                setColor(pal.card)
                cornerRadius = dp(14).toFloat()
            }
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(label(w.w, 20f, pal.text, bold = true))
        col.addView(label(w.t, 15f, pal.text), params(2))
        if (w.e.isNotEmpty()) col.addView(label(w.e, 14f, pal.sub, italic = true), params(4))
        if (w.a.isNotEmpty()) col.addView(label("— ${w.a}", 12f, pal.sub), params(2))
        row.addView(col, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val remove = label("✕", 20f, pal.sub).apply {
            setPadding(dp(14), dp(10), dp(14), dp(10))
            setOnClickListener {
                Prefs.toggleFavorite(this@MainActivity, w.w)
                WordWidgetProvider.updateAll(this@MainActivity)
                build()
            }
        }
        row.addView(remove)
        return row
    }
}
