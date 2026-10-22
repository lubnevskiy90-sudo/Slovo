package com.example.slovodnya

import android.content.Context
import org.json.JSONArray
import java.util.TimeZone

data class Word(
    val w: String,
    val t: String,
    val e: String,
    val k: String = "",   // вид: «Прыказка», «Цытата»… (пусто = «Пример»)
    val a: String = ""    // источник (необязательно)
) {
    val kindLabel: String get() = if (k.isEmpty()) "Пример" else k
}

object Words {
    private var cache: List<Word>? = null

    fun all(context: Context): List<Word> {
        cache?.let { return it }
        val json = context.assets.open("words.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        val list = List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Word(
                o.getString("w"), o.getString("t"), o.optString("e", ""),
                o.optString("k", ""), o.optString("a", "")
            )
        }
        cache = list
        return list
    }

    fun today(context: Context): Word {
        val list = all(context)
        val now = System.currentTimeMillis()
        val day = (now + TimeZone.getDefault().getOffset(now)) / 86_400_000L
        return list[(day % list.size).toInt()]
    }

    fun find(context: Context, w: String): Word? = all(context).firstOrNull { it.w == w }
}
