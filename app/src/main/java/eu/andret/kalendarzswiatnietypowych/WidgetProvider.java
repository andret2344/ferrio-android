package eu.andret.kalendarzswiatnietypowych;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.LoginActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.persistance.AppRepository;
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
		if (FirebaseAuth.getInstance().getCurrentUser() == null) {
			showLoginRequired(context, appWidgetManager, appWidgetIds);
			return;
		}

		final LocalDate now = LocalDate.now();

		final Pair<Month, Integer> datePair = new Pair<>(now.getMonth(), now.getDayOfMonth());
		final String dateText = Util.getFormattedDate(datePair);
		final PendingIntent pendingIntent = buildDayPendingIntent(context, now);

		// Show date and click handler immediately so widget is never blank
		for (final int appWidgetId : appWidgetIds) {
			final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
			views.setTextViewText(R.id.widget_text_date, dateText);
			views.setTextViewText(R.id.widget_text_holidays, context.getString(R.string.loading));
			views.setViewVisibility(R.id.widget_text_holidays, View.VISIBLE);
			views.setViewVisibility(R.id.widget_layout_login, View.GONE);
			views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

		final FerrioApplication app = (FerrioApplication) context.getApplicationContext();
		final AppRepository repository = app.getAppRepository();
		final ApiClient apiClient = app.getApiClient();

		CompletableFuture.supplyAsync(() -> {
			// Try local database first
			final List<Holiday> holidays = repository.getHolidaysByDaySync(now.getMonthValue(), now.getDayOfMonth());
			if (holidays.isEmpty()) {
				// DB empty (first launch / no prior sync) — fetch from API
				return apiClient.getList(apiClient.buildHolidaysPath(now.getMonthValue(), now.getDayOfMonth()), Holiday.class);
			}
			return holidays;
		}).thenAccept(holidays -> {
			final HolidayDay holidayDay = new HolidayDay(now.getMonthValue(), now.getDayOfMonth(), new ArrayList<>(holidays));
			final String content = getContent(context, holidayDay);
			final boolean empty = isHolidayListEmpty(context, holidayDay);

			for (final int appWidgetId : appWidgetIds) {
				final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
				views.setTextViewText(R.id.widget_text_date, dateText);
				views.setTextViewText(R.id.widget_text_holidays, content);
				views.setViewVisibility(R.id.widget_text_holidays, View.VISIBLE);
				views.setViewVisibility(R.id.widget_layout_login, View.GONE);
				views.setViewVisibility(R.id.widget_image_empty, empty ? View.VISIBLE : View.GONE);
				views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
				appWidgetManager.updateAppWidget(appWidgetId, views);
			}
		}).exceptionally(ex -> {
			Log.e(TAG, "Failed to update widget", ex);
			for (final int appWidgetId : appWidgetIds) {
				final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
				views.setTextViewText(R.id.widget_text_date, dateText);
				views.setTextViewText(R.id.widget_text_holidays, context.getString(R.string.widget_error));
				views.setViewVisibility(R.id.widget_text_holidays, View.VISIBLE);
				views.setViewVisibility(R.id.widget_layout_login, View.GONE);
				views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
				appWidgetManager.updateAppWidget(appWidgetId, views);
			}
			return null;
		});
	}

	private void showLoginRequired(@NonNull final Context context,
			@NonNull final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		final LocalDate now = LocalDate.now();
		final Pair<Month, Integer> datePair = new Pair<>(now.getMonth(), now.getDayOfMonth());
		final String dateText = Util.getFormattedDate(datePair);

		final Intent loginIntent = new Intent(context, LoginActivity.class);
		loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		final PendingIntent pendingIntent = PendingIntent.getActivity(
				context, 0, loginIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		for (final int appWidgetId : appWidgetIds) {
			final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
			views.setTextViewText(R.id.widget_text_date, dateText);
			views.setViewVisibility(R.id.widget_text_holidays, View.GONE);
			views.setViewVisibility(R.id.widget_layout_login, View.VISIBLE);
			views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	@NonNull
	private PendingIntent buildDayPendingIntent(@NonNull final Context context,
			@NonNull final LocalDate now) {
		final Intent intent = new Intent(context, DayActivity.class);
		intent.putExtra(MainActivity.DAY, now.getDayOfMonth());
		intent.putExtra(MainActivity.MONTH, now.getMonthValue());
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return PendingIntent.getActivity(
				context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
	}

	private boolean isHolidayListEmpty(@NonNull final Context context,
			@NonNull final HolidayDay holidayDay) {
		final boolean includeUsual = PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(context.getString(R.string.settings_key_usual_holidays), false);
		return holidayDay.countHolidays(includeUsual) == 0;
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
		for (final Holiday holiday : holidays) {
			sb.append("\n");
			sb.append(context.getString(R.string.bullet_point))
					.append(' ')
					.append(holiday.getName());
			final String country = holiday.getCountry();
			if (country != null && !country.isEmpty()) {
				sb.append(' ').append(Util.countryCodeToFlag(country));
			}
		}
		return sb.substring(1);
	}
}
