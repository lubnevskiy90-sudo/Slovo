package com.example.slovodnya

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews

class WordWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) render(context, manager, id)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle
    ) {
        render(context, manager, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_FAV) {
            Prefs.toggleFavorite(context, Words.today(context).w)
            updateAll(context)
        }
    }

    companion object {
        const val ACTION_TOGGLE_FAV = "com.example.slovodnya.TOGGLE_FAV"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, WordWidgetProvider::class.java)
            )
            for (id in ids) render(context, manager, id)
        }

        private fun render(context: Context, manager: AppWidgetManager, id: Int) {
            val word = Words.today(context)
            val pal = Themes.current(context)

            val opts = manager.getAppWidgetOptions(id)
            val landscape =
                context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val wDp = (if (landscape) opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
                       else opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0))
                .let { if (it > 0) it else 250 }
            val hDp = (if (landscape) opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
                       else opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0))
                .let { if (it > 0) it else 140 }

            val views = RemoteViews(context.packageName, R.layout.widget_word)
            views.setImageViewBitmap(R.id.bg, OrnamentBackground.create(context, wDp, hDp, pal))

            views.setTextViewText(R.id.word, word.w)
            views.setTextViewText(R.id.translation, word.t)
            views.setTextColor(R.id.word, pal.text)
            views.setTextColor(R.id.translation, pal.text)

            // пример показываем, только если виджет вытянут по высоте
            val showExample = word.e.isNotEmpty() && hDp >= 180
            views.setViewVisibility(R.id.example, if (showExample) View.VISIBLE else View.GONE)
            views.setTextViewText(R.id.example, word.e)
            views.setTextColor(R.id.example, pal.sub)

            val isFav = Prefs.isFavorite(context, word.w)
            views.setTextViewText(R.id.fav, if (isFav) "★" else "☆")
            views.setTextColor(R.id.fav, pal.main)

            val openApp = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.root, openApp)

            val toggle = PendingIntent.getBroadcast(
                context, 1,
                Intent(context, WordWidgetProvider::class.java).setAction(ACTION_TOGGLE_FAV),
                PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.fav, toggle)

            manager.updateAppWidget(id, views)
        }
    }
}
