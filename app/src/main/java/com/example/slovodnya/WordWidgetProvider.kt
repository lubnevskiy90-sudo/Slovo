package com.example.slovodnya

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONArray
import java.util.TimeZone

class WordWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val (word, translation) = todaysWord(context)
        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_word)
            views.setTextViewText(R.id.word, word)
            views.setTextViewText(R.id.translation, translation)
            views.setOnClickPendingIntent(R.id.root, openApp)
            manager.updateAppWidget(id, views)
        }
    }

    private fun todaysWord(context: Context): Pair<String, String> {
        val json = context.assets.open("words.json").bufferedReader().use { it.readText() }
        val words = JSONArray(json)
        val now = System.currentTimeMillis()
        val localDay = (now + TimeZone.getDefault().getOffset(now)) / 86_400_000L
        val item = words.getJSONObject((localDay % words.length()).toInt())
        return item.getString("w") to item.getString("t")
    }
}
