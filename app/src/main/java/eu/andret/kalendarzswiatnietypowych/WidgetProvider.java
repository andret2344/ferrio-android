package eu.andret.kalendarzswiatnietypowych;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class WidgetProvider extends AppWidgetProvider {
	private static final String TAG = "WidgetProvider";

	@Override
	public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
		super.onReceive(context, intent);
		if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
			final AppWidgetManager manager = AppWidgetManager.getInstance(context);
			final int[] ids = manager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
			if (ids.length > 0) {
				onUpdate(context, manager, ids);
			}
		}
	}

	@Override
	public void onUpdate(@NonNull final Context context,
			@NonNull final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		final LocalDate now = LocalDate.now();
		final boolean dark = isDarkTheme(context);
		final int layoutId = dark ? R.layout.widget_dark : R.layout.widget_light;

		final Pair<Month, Integer> datePair = new Pair<>(now.getMonth(), now.getDayOfMonth());
		final String dateText = Util.getFormattedDate(datePair);

		final Intent intent = new Intent(context, DayActivity.class);
		intent.putExtra(MainActivity.DAY, now.getDayOfMonth());
		intent.putExtra(MainActivity.MONTH, now.getMonthValue());
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		final PendingIntent pendingIntent = PendingIntent.getActivity(
				context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		// Show date and click handler immediately so widget is never blank
		for (final int appWidgetId : appWidgetIds) {
			final RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
			views.setTextViewText(R.id.widget_text_date, dateText);
			views.setTextViewText(R.id.widget_text_holidays, context.getString(R.string.loading));
			views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

		// Then fetch holidays and update content
		final ApiClient apiClient = ((FerrioApplication) context.getApplicationContext()).getApiClient();
		CompletableFuture.supplyAsync(() -> apiClient.getList(
						apiClient.buildHolidaysPath(now.getMonthValue(), now.getDayOfMonth()), Holiday.class))
				.thenAccept(holidays -> {
					final List<Holiday> holidayList = new ArrayList<>(holidays);
					final HolidayDay holidayDay = new HolidayDay(now.getMonthValue(), now.getDayOfMonth(), holidayList);
					final String content = getContent(context, holidayDay);

					for (final int appWidgetId : appWidgetIds) {
						final RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
						views.setTextViewText(R.id.widget_text_date, dateText);
						views.setTextViewText(R.id.widget_text_holidays, content);
						views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
						appWidgetManager.updateAppWidget(appWidgetId, views);
					}
				})
				.exceptionally(ex -> {
					Log.e(TAG, "Failed to update widget", ex);
					return null;
				});
	}

	@NonNull
	private String getContent(@NonNull final Context context,
			@NonNull final HolidayDay holidayDay) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final boolean includeUsual = preferences.getBoolean(
				context.getString(R.string.settings_key_usual_holidays), false);
		if (holidayDay.countHolidays(includeUsual) == 0) {
			return context.getString(R.string.no_unusual_holidays);
		}
		final List<Holiday> holidays = holidayDay.getHolidaysList(includeUsual);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < holidays.size(); i++) {
			if (i > 0) {
				sb.append('\n');
			}
			sb.append(context.getString(R.string.bullet_point))
					.append(' ')
					.append(holidays.get(i).getName());
		}
		return sb.toString();
	}

	private static boolean isDarkTheme(@NonNull final Context context) {
		return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
				== Configuration.UI_MODE_NIGHT_YES;
	}
}
