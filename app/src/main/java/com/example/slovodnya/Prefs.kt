package com.example.slovodnya

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object Prefs {
    private fun sp(c: Context): SharedPreferences =
        c.getSharedPreferences("slovodnya", Context.MODE_PRIVATE)

    fun favorites(c: Context): Set<String> =
        sp(c).getStringSet("fav", emptySet()) ?: emptySet()

    fun isFavorite(c: Context, word: String): Boolean = favorites(c).contains(word)

    /** Возвращает true, если слово добавлено, и false, если убрано. */
    fun toggleFavorite(c: Context, word: String): Boolean {
        val set = favorites(c).toMutableSet()
        val added = if (set.contains(word)) {
            set.remove(word)
            false
        } else {
            set.add(word)
            true
        }
        sp(c).edit().putStringSet("fav", set).apply()
        return added
    }

    fun theme(c: Context): Int = sp(c).getInt("theme", 0)

    fun setTheme(c: Context, index: Int) {
        sp(c).edit().putInt("theme", index).apply()
    }

    /** Что показывает виджет: 0 = слово дня, 1 = прымаўка дня. */
    fun widgetMode(c: Context): Int = sp(c).getInt("widget_mode", 0)

    fun setWidgetMode(c: Context, mode: Int) {
        sp(c).edit().putInt("widget_mode", mode).apply()
    }

    // ---------- свои прымаўки ----------
    fun customProverbs(c: Context): List<Proverb> {
        val raw = sp(c).getString("custom_proverbs", "[]") ?: "[]"
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                Proverb(o.getString("p"), o.optString("m", ""), "")
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCustomProverbs(c: Context, list: List<Proverb>) {
        val arr = JSONArray()
        for (x in list) arr.put(JSONObject().put("p", x.p).put("m", x.m))
        sp(c).edit().putString("custom_proverbs", arr.toString()).apply()
    }

    fun addCustomProverbs(c: Context, items: List<Proverb>) {
        val cur = customProverbs(c).toMutableList()
        for (x in items) if (cur.none { it.p == x.p }) cur.add(x)
        saveCustomProverbs(c, cur)
    }

    fun removeCustomProverb(c: Context, p: String) {
        saveCustomProverbs(c, customProverbs(c).filter { it.p != p })
    }

    fun onlyCustom(c: Context): Boolean = sp(c).getBoolean("only_custom", false)

    fun setOnlyCustom(c: Context, on: Boolean) {
        sp(c).edit().putBoolean("only_custom", on).apply()
    }

    /** Включать нецензурные прымаўки (по умолчанию да). */
    fun showAdult(c: Context): Boolean = sp(c).getBoolean("show_adult", true)

    fun setShowAdult(c: Context, on: Boolean) {
        sp(c).edit().putBoolean("show_adult", on).apply()
    }
}
