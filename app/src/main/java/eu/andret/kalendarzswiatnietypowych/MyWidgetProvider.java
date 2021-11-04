package eu.andret.kalendarzswiatnietypowych;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.time.LocalDate;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.activities.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidaysDBHelper;

public class MyWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		final boolean dark = Data.getPreferences(context, Data.Prefs.THEME).getString(context.getResources().getString(R.string.settings_theme_widgets), "1").equals("1");
		final LocalDate now = LocalDate.now();
		final int day = now.getDayOfMonth();
		final int month = now.getMonthValue();
		final Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra(MainActivity.FROM, MainActivity.WIDGET);
		intent.putExtra(MainActivity.DAY, day);
		intent.putExtra(MainActivity.MONTH, month);
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), dark ? R.layout.widget_dark : R.layout.widget_light);
		final SharedPreferences theme = Data.getPreferences(context, Data.Prefs.THEME);
		final SharedPreferences prefs = Data.getPreferences(context, Data.Prefs.LANGUAGE);
		final String selectedLanguageCode = prefs.getString(MainActivity.SELECTED_LANGUAGE, "en");
		final HolidaysDBHelper holidaysDBHelper = new HolidaysDBHelper(context);
		final HolidayCalendar holidayCalendar = holidaysDBHelper.getAll(selectedLanguageCode);
		final HolidayDay holidayDay = holidayCalendar.getTodayHolidays();
		holidaysDBHelper.close();
		final String output;
		if (holidayDay.countHolidays(theme.getBoolean(context.getResources().getString(R.string.settings_usual_holidays), false)) == 0) {
			output = context.getResources().getString(R.string.no_unusual_holidays);
		} else {
			output = holidayDay.getHolidaysList(theme.getBoolean(context.getResources().getString(R.string.settings_usual_holidays), false)).stream()
					.map(Holiday::getText)
					.map(text -> context.getResources().getString(R.string.pointed_text, text))
					.collect(Collectors.joining("\n\n"));
		}
		remoteViews.setTextViewText(R.id.widget_text_holiday, output);
		final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.widget_relative_main, pendingIntent);
		for (final int i : appWidgetIds) {
			intent.putExtra("widgetID", i);
			appWidgetManager.updateAppWidget(i, remoteViews);
		}
	}
}
