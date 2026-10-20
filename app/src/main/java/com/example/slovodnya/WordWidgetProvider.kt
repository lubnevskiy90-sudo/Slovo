package com.example.slovodnya

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.RemoteViews
import org.json.JSONArray
import java.util.TimeZone

class WordWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) render(context, manager, id)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle
    ) {
        render(context, manager, id)
    }

    private fun render(context: Context, manager: AppWidgetManager, id: Int) {
        val (word, meaning) = todaysWord(context)

        val opts = manager.getAppWidgetOptions(id)
        val landscape =
            context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val wDp = (if (landscape) opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
                   else opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0))
            .let { if (it > 0) it else 250 }
        val hDp = (if (landscape) opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
                   else opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0))
            .let { if (it > 0) it else 140 }

        val openApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val views = RemoteViews(context.packageName, R.layout.widget_word)
        views.setImageViewBitmap(R.id.bg, OrnamentBackground.create(context, wDp, hDp))
        views.setTextViewText(R.id.word, word)
        views.setTextViewText(R.id.translation, meaning)
        views.setOnClickPendingIntent(R.id.root, openApp)
        manager.updateAppWidget(id, views)
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
