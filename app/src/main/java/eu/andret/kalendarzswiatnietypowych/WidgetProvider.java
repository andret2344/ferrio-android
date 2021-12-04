package eu.andret.kalendarzswiatnietypowych;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.time.LocalDate;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;

public class WidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_black);
		remoteViews.setTextViewText(R.id.widget_text_holiday, getContent(context));

		final LocalDate now = LocalDate.now();
		final Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra(MainActivity.FROM, MainActivity.WIDGET);
		intent.putExtra(MainActivity.DAY, now.getDayOfMonth());
		intent.putExtra(MainActivity.MONTH, now.getMonthValue());
		final PendingIntent pendingIntent = getPendingIntent(context, intent);
		remoteViews.setOnClickPendingIntent(R.id.widget_relative_main, pendingIntent);

		final AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(appWidgetIds, remoteViews);
	}

	@NonNull
	@SuppressLint("UnspecifiedImmutableFlag")
	private PendingIntent getPendingIntent(@NonNull final Context context, @NonNull final Intent intent) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
			return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
	}

	private String getContent(final Context context) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final String selectedLanguageCode = preferences.getString(MainActivity.SELECTED_LANGUAGE, "en");
		final HolidaysDBHelper holidaysDBHelper = new HolidaysDBHelper(context);
		final HolidayCalendar holidayCalendar = holidaysDBHelper.getAll(selectedLanguageCode);
		final HolidayDay holidayDay = holidayCalendar.getTodayHolidays();
		holidaysDBHelper.close();
		if (holidayDay.countHolidays(preferences.getBoolean(context.getString(R.string.settings_key_usual_holidays), false)) == 0) {
			return context.getString(R.string.no_unusual_holidays);
		}
		return holidayDay.getHolidaysList(preferences.getBoolean(context.getString(R.string.settings_key_usual_holidays), false)).stream()
				.map(Holiday::getText)
				.map(text -> context.getString(R.string.pointed_text, text))
				.collect(Collectors.joining("\n\n"));
	}
}
