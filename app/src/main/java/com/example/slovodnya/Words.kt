package com.example.slovodnya

import android.content.Context
import org.json.JSONArray
import java.util.TimeZone

data class Word(
    val w: String,
    val t: String,
    val e: String,
    val k: String = "",    // вид примера: «Прыказка», «Цытата»… (пусто = «Пример»)
    val a: String = "",    // источник примера (необязательно)
    val en: String = "",   // перевод на английский
    val ei: String = "",   // транскрипция английского слова (IPA)
    val de: String = "",   // перевод на немецкий
    val di: String = "",   // транскрипция немецкого слова (IPA)
    val er: String = "",   // чтение английского слова русскими буквами
    val dr: String = "",   // чтение немецкого слова русскими буквами
    val s: String = ""     // слово с ударением
) {
    val kindLabel: String get() = if (k.isEmpty()) "Пример" else k

    /** Слово с ударением (если есть) и с большой буквы. */
    val title: String
        get() = (if (s.isNotEmpty()) s else w).replaceFirstChar { it.titlecase() }
}

data class Proverb(val p: String, val m: String, val a: String)

private fun dayNumber(): Long {
    val now = System.currentTimeMillis()
    return (now + TimeZone.getDefault().getOffset(now)) / 86_400_000L
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
                o.optString("k", ""), o.optString("a", ""),
                o.optString("en", ""), o.optString("ei", ""),
                o.optString("de", ""), o.optString("di", ""),
                o.optString("er", ""), o.optString("dr", ""),
                o.optString("s", "")
            )
        }
        cache = list
        return list
    }

    fun today(context: Context): Word {
        val list = all(context)
        return list[(dayNumber() % list.size).toInt()]
    }

    fun find(context: Context, w: String): Word? = all(context).firstOrNull { it.w == w }
}

object Proverbs {
    private var builtin: List<Proverb>? = null

    private fun loadBuiltin(context: Context): List<Proverb> {
        builtin?.let { return it }
        val json = context.assets.open("proverbs.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        val list = List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Proverb(o.getString("p"), o.getString("m"), o.optString("a", ""))
        }
        builtin = list
        return list
    }

    private var adult: List<Proverb>? = null

    private fun loadAdult(context: Context): List<Proverb> {
        adult?.let { return it }
        val json = context.assets.open("proverbs_adult.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        val list = List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            Proverb(o.getString("p"), o.optString("m", ""), o.optString("a", ""))
        }
        adult = list
        return list
    }

    fun all(context: Context): List<Proverb> {
        val custom = Prefs.customProverbs(context)
        if (Prefs.onlyCustom(context) && custom.isNotEmpty()) return custom
        val base = loadBuiltin(context) +
            (if (Prefs.showAdult(context)) loadAdult(context) else emptyList())
        return base + custom
    }

    fun today(context: Context): Proverb {
        val list = all(context)
        return list[(dayNumber() % list.size).toInt()]
    }
}
