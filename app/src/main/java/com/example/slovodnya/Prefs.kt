package com.example.slovodnya

import android.content.Context
import android.content.SharedPreferences

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
}
