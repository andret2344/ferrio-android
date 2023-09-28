package eu.andret.kalendarzswiatnietypowych;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.Downloader;
import java9.util.concurrent.CompletableFuture;

public class WidgetProvider extends AppWidgetProvider {
	private static final String ACTION_SCHEDULED_UPDATE = "eu.andret.kalendarzswiatnietypowych.SCHEDULED_UPDATE";

	@Override
	public void onEnabled(final Context context) {
		super.onEnabled(context);

		final Intent intent = new Intent(ACTION_SCHEDULED_UPDATE);
		final PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final long triggerAtMillis = LocalTime.of(0, 2, 30).toNanoOfDay() / 1000;
		alarmManager.setInexactRepeating(AlarmManager.RTC, triggerAtMillis, AlarmManager.INTERVAL_DAY, alarmIntent);
	}

	@Override
	public void onDisabled(final Context context) {
		super.onDisabled(context);

		final Intent intent = new Intent(ACTION_SCHEDULED_UPDATE);
		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE));
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		super.onReceive(context, intent);

		if (ACTION_SCHEDULED_UPDATE.equals(intent.getAction())) {
			final LocalDate now = LocalDate.now();
			final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_black);

			final Downloader.HolidayDayDownloader downloader = new Downloader.HolidayDayDownloader(now.getMonthValue(), now.getDayOfMonth());
			CompletableFuture.supplyAsync(downloader).thenAccept(holidayDay ->
					remoteViews.setTextViewText(R.id.widget_text_holiday, getContent(context, holidayDay)));
		}
	}

	private String getContent(final Context context, final HolidayDay holidayDay) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (holidayDay.countHolidays(preferences.getBoolean(context.getString(R.string.settings_key_usual_holidays), false)) == 0) {
			return context.getString(R.string.no_unusual_holidays);
		}
		return holidayDay.getHolidaysList(preferences.getBoolean(context.getString(R.string.settings_key_usual_holidays), false)).stream()
				.map(Holiday::getName)
				.map(text -> context.getString(R.string.pointed_text, text))
				.collect(Collectors.joining("\n\n"));
	}
}
