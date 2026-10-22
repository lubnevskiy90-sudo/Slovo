package com.example.slovodnya

import android.content.Context
import android.graphics.Color

data class Palette(
    val name: String,
    val light: Boolean,
    val bg: Int,       // фон виджета (полупрозрачный)
    val main: Int,     // основной цвет вышивки
    val accent: Int,   // второй цвет вышивки (центр ромба)
    val text: Int,
    val sub: Int,
    val appBg: Int,    // фон экрана приложения
    val card: Int,
    val onMain: Int    // цвет текста на кнопках
)

object Themes {
    val all = listOf(
        Palette(
            "Светлая", true,
            Color.argb(184, 246, 240, 227), Color.rgb(181, 38, 46), Color.rgb(34, 34, 34),
            Color.rgb(34, 34, 34), Color.rgb(90, 90, 90),
            Color.rgb(246, 240, 227), Color.rgb(255, 252, 245), Color.WHITE
        ),
        Palette(
            "Тёмная", false,
            Color.argb(210, 28, 28, 32), Color.rgb(214, 64, 72), Color.rgb(240, 230, 210),
            Color.rgb(244, 239, 230), Color.rgb(190, 185, 175),
            Color.rgb(24, 24, 28), Color.rgb(36, 36, 42), Color.WHITE
        ),
        Palette(
            "Красная", false,
            Color.argb(220, 150, 28, 36), Color.rgb(246, 240, 227), Color.rgb(34, 34, 34),
            Color.rgb(255, 250, 240), Color.rgb(240, 215, 205),
            Color.rgb(120, 22, 30), Color.rgb(150, 28, 36), Color.rgb(120, 22, 30)
        )
    )

    fun current(context: Context): Palette =
        all[Prefs.theme(context).coerceIn(0, all.lastIndex)]
}
