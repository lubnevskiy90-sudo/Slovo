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

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        DailyUpdate.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        DailyUpdate.cancel(context)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) render(context, manager, id)
        DailyUpdate.schedule(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle
    ) {
        render(context, manager, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE_FAV -> {
                Prefs.toggleFavorite(context, Words.today(context).w)
                updateAll(context)
            }
            ACTION_TOGGLE_MODE -> {
                Prefs.setWidgetMode(context, 1 - Prefs.widgetMode(context))
                updateAll(context)
            }
            DailyUpdate.ACTION_DAILY,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                updateAll(context)
                DailyUpdate.schedule(context)
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_FAV = "com.example.slovodnya.TOGGLE_FAV"
        const val ACTION_TOGGLE_MODE = "com.example.slovodnya.TOGGLE_MODE"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, WordWidgetProvider::class.java)
            )
            for (id in ids) render(context, manager, id)
        }

        private fun vis(on: Boolean): Int = if (on) View.VISIBLE else View.GONE

        private fun render(context: Context, manager: AppWidgetManager, id: Int) {
            val pal = Themes.current(context)
            val mode = Prefs.widgetMode(context)

            val opts = manager.getAppWidgetOptions(id)
            val landscape =
                context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val wDp = (if (landscape) opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
                       else opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0))
                .let { if (it > 0) it else 250 }
            val hDp = (if (landscape) opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
                       else opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0))
                .let { if (it > 0) it else 140 }
            val tall = hDp >= 180

            val views = RemoteViews(context.packageName, R.layout.widget_word)
            views.setImageViewBitmap(R.id.bg, OrnamentBackground.create(context, wDp, hDp, pal))

            // кнопка переключения слово / прымаўка
            views.setTextViewText(R.id.modeLabel, if (mode == 0) "слова" else "прымаўка")
            views.setTextColor(R.id.modeArrow, pal.main)
            views.setTextColor(R.id.modeLabel, pal.sub)
            views.setOnClickPendingIntent(
                R.id.modeBtn,
                PendingIntent.getBroadcast(
                    context, 4,
                    Intent(context, WordWidgetProvider::class.java).setAction(ACTION_TOGGLE_MODE),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )

            if (mode == 0) {
                val word = Words.today(context)
                views.setViewVisibility(R.id.wordImg, View.VISIBLE)
                views.setViewVisibility(R.id.translation, View.VISIBLE)
                views.setViewVisibility(R.id.proverb, View.GONE)
                views.setViewVisibility(R.id.proverbMeaning, View.GONE)
                views.setViewVisibility(R.id.fav, View.VISIBLE)

                views.setImageViewBitmap(
                    R.id.wordImg,
                    StressText.renderWord(context, word.title, pal.text, 28f, wDp - 104)
                )
                views.setContentDescription(R.id.wordImg, word.title)
                views.setTextViewText(R.id.translation, word.t)
                views.setTextColor(R.id.translation, pal.text)

                val exampleText = if (word.a.isNotEmpty()) "${word.e} — ${word.a}" else word.e
                views.setViewVisibility(R.id.example, vis(word.e.isNotEmpty() && tall))
                views.setTextViewText(R.id.example, exampleText)
                views.setTextColor(R.id.example, pal.sub)

                val isFav = Prefs.isFavorite(context, word.w)
                views.setTextViewText(R.id.fav, if (isFav) "★" else "☆")
                views.setTextColor(R.id.fav, pal.main)
                views.setOnClickPendingIntent(
                    R.id.fav,
                    PendingIntent.getBroadcast(
                        context, 1,
                        Intent(context, WordWidgetProvider::class.java).setAction(ACTION_TOGGLE_FAV),
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            } else {
                val pr = Proverbs.today(context)
                views.setViewVisibility(R.id.wordImg, View.GONE)
                views.setViewVisibility(R.id.translation, View.GONE)
                views.setViewVisibility(R.id.example, View.GONE)
                views.setViewVisibility(R.id.fav, View.GONE)
                views.setViewVisibility(R.id.proverb, View.VISIBLE)
                views.setViewVisibility(R.id.proverbMeaning, vis(tall && pr.m.isNotEmpty()))

                views.setTextViewText(R.id.proverb, pr.p)
                views.setTextColor(R.id.proverb, pal.text)
                views.setTextViewText(R.id.proverbMeaning, pr.m)
                views.setTextColor(R.id.proverbMeaning, pal.sub)
            }

            // тап по виджету открывает приложение на нужной вкладке
            val open = Intent(context, MainActivity::class.java).putExtra("tab", mode)
            views.setOnClickPendingIntent(
                R.id.root,
                PendingIntent.getActivity(
                    context, 10 + mode, open,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

            manager.updateAppWidget(id, views)
        }
    }
}
