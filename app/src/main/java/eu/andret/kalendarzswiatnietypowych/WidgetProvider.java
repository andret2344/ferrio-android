package eu.andret.kalendarzswiatnietypowych;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.Downloader;
import java9.util.concurrent.CompletableFuture;

public class WidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(@NonNull final Context context, @NonNull final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		final LocalDate now = LocalDate.now();
		final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_black);

		final Downloader.HolidayDayDownloader downloader = new Downloader.HolidayDayDownloader(now.getMonthValue(), now.getDayOfMonth());
		CompletableFuture.supplyAsync(downloader)
				.thenApply(holidayDay -> getContent(context, holidayDay))
				.thenAccept(text -> remoteViews.setTextViewText(R.id.widget_text_holiday, text))
				.thenRun(() -> Arrays.stream(appWidgetIds)
						.forEach(appWidgetId -> appWidgetManager.updateAppWidget(appWidgetId, remoteViews)));
	}

	@NonNull
	private String getContent(@NonNull final Context context, @NonNull final HolidayDay holidayDay) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final boolean includeUsual = preferences.getBoolean(context.getString(R.string.settings_key_usual_holidays), false);
		if (holidayDay.countHolidays(includeUsual) == 0) {
			return context.getString(R.string.no_unusual_holidays);
		}
		return holidayDay.getHolidaysList(includeUsual)
				.stream()
				.map(Holiday::getName)
				.map(text -> context.getString(R.string.pointed_text, text))
				.collect(Collectors.joining("\n\n"));
	}
}
