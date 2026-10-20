package com.example.slovodnya

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = TextView(this).apply {
            text = "Слова дня\n\nДобавьте виджет на рабочий стол:\nдолгий тап по экрану → Виджеты → Слова дня"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        setContentView(text)
    }
}
