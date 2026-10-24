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

    private var tab = 0   // 0 = слова, 1 = прымаўкі

    override fun onResume() {
        super.onResume()
        build()
        WordWidgetProvider.updateAll(this)
        DailyUpdate.schedule(this)
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

    private fun tabButton(text: String, selected: Boolean, pal: Palette, onClick: () -> Unit): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 16f
            gravity = Gravity.CENTER
            setTypeface(null, Typeface.BOLD)
            setTextColor(if (selected) pal.onMain else pal.text)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dp(22).toFloat()
                if (selected) {
                    setColor(pal.main)
                } else {
                    setColor(pal.card)
                    setStroke(dp(1), pal.main)
                }
            }
            setOnClickListener { onClick() }
        }

    private fun cardBox(pal: Palette): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(18), dp(20), dp(18))
        background = GradientDrawable().apply {
            setColor(pal.card)
            cornerRadius = dp(18).toFloat()
            setStroke(dp(2), pal.main)
        }
    }

    private fun build() {
        val pal = Themes.current(this)
        window.setBackgroundDrawable(ColorDrawable(pal.appBg))
        window.statusBarColor = pal.appBg
        val ui = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility =
            if (pal.light) ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else ui and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(40))
        }

        // --- вкладки ---
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val lpLeft = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            .apply { marginEnd = dp(6) }
        val lpRight = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            .apply { marginStart = dp(6) }
        bar.addView(tabButton("Слова", tab == 0, pal) { tab = 0; build() }, lpLeft)
        bar.addView(tabButton("Прымаўкі", tab == 1, pal) { tab = 1; build() }, lpRight)
        root.addView(bar, params())

        if (tab == 0) addWords(root, pal) else addProverb(root, pal)
        addThemes(root, pal)

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

    // ---------------- Вкладка «Слова» ----------------
    private fun addWords(root: LinearLayout, pal: Palette) {
        val today = Words.today(this)
        val card = cardBox(pal)
        card.addView(label("СЛОВО ДНЯ", 12f, pal.sub, bold = true))
        card.addView(label(today.title, 34f, pal.text, bold = true), params(4))
        card.addView(label(today.t, 18f, pal.text), params(2))
        addTranslations(card, today, pal, true)
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
        root.addView(card, params(16))

        val favs = Prefs.favorites(this).mapNotNull { Words.find(this, it) }.sortedBy { it.w }
        root.addView(label("ИЗБРАННОЕ (${favs.size})", 12f, pal.sub, bold = true), params(28))
        if (favs.isEmpty()) {
            root.addView(
                label("Пока пусто. Нажмите ☆ у слова дня или на самом виджете.", 15f, pal.sub),
                params(8)
            )
        }
        for (w in favs) root.addView(favRow(w, pal), params(10))
    }

    private fun addTranslations(parent: LinearLayout, w: Word, pal: Palette, big: Boolean) {
        val size = if (big) 15f else 13f
        if (w.en.isNotEmpty()) {
            val line = "EN  ${w.en}  [${w.ei}]" + if (w.er.isNotEmpty()) "  ≈ ${w.er}" else ""
            parent.addView(label(line, size, pal.sub), params(if (big) 10 else 4))
        }
        if (w.de.isNotEmpty()) {
            val line = "DE  ${w.de}  [${w.di}]" + if (w.dr.isNotEmpty()) "  ≈ ${w.dr}" else ""
            parent.addView(label(line, size, pal.sub), params(2))
        }
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
        col.addView(label(w.title, 20f, pal.text, bold = true))
        col.addView(label(w.t, 15f, pal.text), params(2))
        addTranslations(col, w, pal, false)
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

    // ---------------- Вкладка «Прымаўкі» ----------------
    private fun addProverb(root: LinearLayout, pal: Palette) {
        val pr = Proverbs.today(this)
        val card = cardBox(pal)
        card.addView(label("ПРЫМАЎКА ДНЯ", 12f, pal.sub, bold = true))
        card.addView(label(pr.p, 24f, pal.text, bold = true), params(8))
        card.addView(label("ЗНАЧЕНИЕ", 12f, pal.sub, bold = true), params(18))
        card.addView(label(pr.m, 16f, pal.text), params(2))
        if (pr.a.isNotEmpty()) {
            card.addView(label("РУССКИЙ АНАЛОГ", 12f, pal.sub, bold = true), params(14))
            card.addView(label(pr.a, 16f, pal.text, italic = true), params(2))
        }
        root.addView(card, params(16))
        root.addView(
            label(
                "Новая прымаўка каждый день. Формулировки в разных регионах могут немного отличаться.",
                13f, pal.sub
            ),
            params(14)
        )
    }

    // ---------------- Тема ----------------
    private fun addThemes(root: LinearLayout, pal: Palette) {
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
    }
}
