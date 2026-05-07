package eu.andret.kalendarzswiatnietypowych.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.BuildConfig;
import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.LoginActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.persistence.AppRepository;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.ApiException;
import eu.andret.kalendarzswiatnietypowych.util.PreferenceHelper;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public abstract class BaseWidgetProvider extends AppWidgetProvider {
	private static final String TAG = "Ferrio-BaseWidgetProvider";

	private static final float MIN_FONT_SIZE_SP = 8f;

	// Set to "W2" or "W3" to simulate that error path, empty to disable.
	// Gated by BuildConfig.DEBUG so release builds can never carry forced-error logic.
	static final String DEBUG_FORCE_ERROR = "";

	@LayoutRes
	protected abstract int getLayoutResId();

	@NonNull
	protected abstract Class<? extends BaseWidgetProvider> getProviderClass();

	@Override
	public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
		super.onReceive(context, intent);
		if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
			final AppWidgetManager manager = AppWidgetManager.getInstance(context);
			final int[] ids = manager.getAppWidgetIds(new ComponentName(context, getProviderClass()));
			if (ids.length > 0) {
				onUpdate(context, manager, ids);
			}
		}
	}

	@Override
	public void onUpdate(@NonNull final Context context,
			@NonNull final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		updateWidgets(context, appWidgetManager, appWidgetIds, getLayoutResId());
	}

	/**
	 * Synchronously renders an immediate placeholder for each widget, then enqueues a
	 * {@link WidgetUpdateWorker} to fetch data and render the final state. Used by
	 * {@link #onUpdate} and {@link WidgetConfigActivity} after a config change.
	 */
	static void updateWidgets(@NonNull final Context context,
			@NonNull final AppWidgetManager appWidgetManager,
			@NonNull final int[] appWidgetIds, @LayoutRes final int layoutResId) {
		final boolean signedIn = isSignedIn();
		for (final int appWidgetId : appWidgetIds) {
			try {
				if (BuildConfig.DEBUG && "W2".equals(DEBUG_FORCE_ERROR)) {
					throw new RuntimeException("Debug: forced W2 error");
				}
				if (!signedIn) {
					renderLoginRequired(context, appWidgetManager, appWidgetId, layoutResId);
				} else {
					renderLoadingState(context, appWidgetManager, appWidgetId, layoutResId);
				}
			} catch (final Exception ex) {
				Log.e(TAG, "Failed to initialize widget", ex);
				renderErrorState(context, appWidgetManager, appWidgetId, layoutResId, "W2");
			}
		}
		if (signedIn) {
			WidgetUpdateWorker.enqueue(context, appWidgetIds, layoutResId);
		}
	}

	/**
	 * Overload used by {@link WidgetConfigActivity} after a save.
	 */
	static void updateSingleWidget(@NonNull final Context context,
			@NonNull final AppWidgetManager appWidgetManager, final int appWidgetId,
			@LayoutRes final int layoutResId) {
		updateWidgets(context, appWidgetManager, new int[]{appWidgetId}, layoutResId);
	}

	private static boolean isSignedIn() {
		try {
			return FirebaseAuth.getInstance().getCurrentUser() != null;
		} catch (final Exception ex) {
			Log.e(TAG, "Firebase unavailable, treating as signed-out", ex);
			return false;
		}
	}

	private static void renderLoadingState(@NonNull final Context context,
			@NonNull final AppWidgetManager appWidgetManager, final int appWidgetId,
			@LayoutRes final int layoutResId) {
		final int daysOffset = WidgetPrefs.getDaysOffset(context, appWidgetId);
		final boolean colorized = WidgetPrefs.isColorized(context, appWidgetId);
		final int fontSizeOffset = WidgetPrefs.getFontSizeOffset(context, appWidgetId);

		final LocalDate targetDate = LocalDate.now().plusDays(daysOffset);
		final Pair<Month, Integer> datePair = new Pair<>(targetDate.getMonth(), targetDate.getDayOfMonth());
		final String dateText = Util.getFormattedDate(datePair);
		final PendingIntent pendingIntent = buildDayPendingIntent(context, targetDate, appWidgetId);

		final RemoteViews views = new RemoteViews(context.getPackageName(), layoutResId);
		views.setTextViewText(R.id.widget_text_date, dateText);
		views.setTextViewText(R.id.widget_text_holidays, context.getString(R.string.loading));
		views.setViewVisibility(R.id.widget_text_holidays, View.VISIBLE);
		views.setViewVisibility(R.id.widget_layout_login, View.GONE);
		views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
		applyColorized(views, context, colorized, targetDate, layoutResId);
		applyFontSize(context, views, fontSizeOffset);
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	/**
	 * Synchronous data fetch + final render. Called from {@link WidgetUpdateWorker}; must
	 * never run on the main thread.
	 */
	static void renderDataState(@NonNull final Context context,
			@NonNull final AppWidgetManager appWidgetManager, final int appWidgetId,
			@LayoutRes final int layoutResId) {
		if (!isSignedIn()) {
			renderLoginRequired(context, appWidgetManager, appWidgetId, layoutResId);
			return;
		}

		final FerrioApplication app = (FerrioApplication) context.getApplicationContext();
		final AppRepository repository = app.getAppRepository();
		final ApiClient apiClient = app.getApiClient();

		final int daysOffset = WidgetPrefs.getDaysOffset(context, appWidgetId);
		final boolean colorized = WidgetPrefs.isColorized(context, appWidgetId);
		final int fontSizeOffset = WidgetPrefs.getFontSizeOffset(context, appWidgetId);

		final LocalDate targetDate = LocalDate.now().plusDays(daysOffset);
		final Pair<Month, Integer> datePair = new Pair<>(targetDate.getMonth(), targetDate.getDayOfMonth());
		final String dateText = Util.getFormattedDate(datePair);
		final PendingIntent pendingIntent = buildDayPendingIntent(context, targetDate, appWidgetId);

		final int monthValue = targetDate.getMonthValue();
		final int dayOfMonth = targetDate.getDayOfMonth();

		List<Holiday> holidays = repository.getHolidaysByDaySync(monthValue, dayOfMonth);
		if (holidays.isEmpty()) {
			try {
				holidays = apiClient.getList(apiClient.buildHolidaysUrl(monthValue, dayOfMonth), Holiday.class);
			} catch (final ApiException ex) {
				throw new RuntimeException(ex);
			}
		}

		final HolidayDay holidayDay = new HolidayDay(monthValue, dayOfMonth, new ArrayList<>(holidays));
		final String content = getContent(context, holidayDay);
		final boolean empty = isHolidayListEmpty(context, holidayDay);

		final RemoteViews views = new RemoteViews(context.getPackageName(), layoutResId);
		views.setTextViewText(R.id.widget_text_date, dateText);
		views.setTextViewText(R.id.widget_text_holidays, content);
		views.setViewVisibility(R.id.widget_text_holidays, View.VISIBLE);
		views.setViewVisibility(R.id.widget_layout_login, View.GONE);
		views.setViewVisibility(R.id.widget_image_empty, empty ? View.VISIBLE : View.GONE);
		views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
		applyColorized(views, context, colorized, targetDate, layoutResId);
		applyFontSize(context, views, fontSizeOffset);
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	static void renderErrorState(@NonNull final Context context,
			@NonNull final AppWidgetManager appWidgetManager, final int appWidgetId,
			@LayoutRes final int layoutResId, @NonNull final String errorCode) {
		final RemoteViews views = new RemoteViews(context.getPackageName(), layoutResId);
		views.setTextViewText(R.id.widget_text_date,
				context.getString(R.string.widget_error_title));
		views.setTextViewText(R.id.widget_text_holidays,
				context.getString(R.string.widget_error, errorCode));
		views.setViewVisibility(R.id.widget_text_holidays, View.VISIBLE);
		views.setViewVisibility(R.id.widget_layout_login, View.GONE);
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	@Override
	public void onDeleted(@NonNull final Context context, final int[] appWidgetIds) {
		for (final int appWidgetId : appWidgetIds) {
			WidgetPrefs.delete(context, appWidgetId);
		}
	}

	private static void renderLoginRequired(@NonNull final Context context,
			@NonNull final AppWidgetManager appWidgetManager, final int appWidgetId,
			@LayoutRes final int layoutResId) {
		final int daysOffset = WidgetPrefs.getDaysOffset(context, appWidgetId);
		final boolean colorized = WidgetPrefs.isColorized(context, appWidgetId);
		final LocalDate targetDate = LocalDate.now().plusDays(daysOffset);
		final Pair<Month, Integer> datePair = new Pair<>(targetDate.getMonth(), targetDate.getDayOfMonth());
		final String dateText = Util.getFormattedDate(datePair);

		final Intent loginIntent = new Intent(context, LoginActivity.class);
		loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		final PendingIntent pendingIntent = PendingIntent.getActivity(
				context, 0, loginIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		final RemoteViews views = new RemoteViews(context.getPackageName(), layoutResId);
		views.setTextViewText(R.id.widget_text_date, dateText);
		views.setViewVisibility(R.id.widget_text_holidays, View.GONE);
		views.setViewVisibility(R.id.widget_layout_login, View.VISIBLE);
		views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
		applyColorized(views, context, colorized, targetDate, layoutResId);
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	private static void applyColorized(@NonNull final RemoteViews views,
			@NonNull final Context context,
			final boolean colorized, @NonNull final LocalDate date,
			@LayoutRes final int layoutResId) {
		if (colorized) {
			final long seed = Util.calculateSeed(date.getDayOfMonth(), date.getMonthValue());
			final int opaqueColor = Util.randomizeColor(context, seed);
			final int color = layoutResId == R.layout.widget_transparent
					? Color.argb(0x48, Color.red(opaqueColor), Color.green(opaqueColor), Color.blue(opaqueColor))
					: opaqueColor;
			views.setInt(R.id.widget_root, "setBackgroundColor", color);
		} else {
			final int bgRes = layoutResId == R.layout.widget_transparent
					? R.drawable.rounded_widget_transparent : R.drawable.rounded_widget;
			views.setInt(R.id.widget_root, "setBackgroundResource", bgRes);
		}
	}

	@NonNull
	private static PendingIntent buildDayPendingIntent(@NonNull final Context context,
			@NonNull final LocalDate date, final int appWidgetId) {
		final Intent intent = new Intent(context, DayActivity.class);
		intent.putExtra(MainActivity.DAY, date.getDayOfMonth());
		intent.putExtra(MainActivity.MONTH, date.getMonthValue());
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return PendingIntent.getActivity(
				context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
	}

	private static boolean isHolidayListEmpty(@NonNull final Context context,
			@NonNull final HolidayDay holidayDay) {
		final boolean includeUsual = new PreferenceHelper(context).includeUsualHolidays();
		return holidayDay.countHolidays(includeUsual) == 0;
	}

	private static void applyFontSize(@NonNull final Context context,
			@NonNull final RemoteViews views, final int fontSizeOffset) {
		final Resources res = context.getResources();
		final float dateSp = Math.max(MIN_FONT_SIZE_SP,
				readSpDimen(res, R.dimen.widget_text_date_size) + fontSizeOffset);
		final float holidaysSp = Math.max(MIN_FONT_SIZE_SP,
				readSpDimen(res, R.dimen.widget_text_holidays_size) + fontSizeOffset);
		views.setTextViewTextSize(R.id.widget_text_date, TypedValue.COMPLEX_UNIT_SP, dateSp);
		views.setTextViewTextSize(R.id.widget_text_holidays, TypedValue.COMPLEX_UNIT_SP, holidaysSp);
	}

	private static float readSpDimen(@NonNull final Resources res, final int dimenResId) {
		final TypedValue tv = new TypedValue();
		res.getValue(dimenResId, tv, true);
		return TypedValue.complexToFloat(tv.data);
	}

	@NonNull
	private static String getContent(@NonNull final Context context,
			@NonNull final HolidayDay holidayDay) {
		final boolean includeUsual = new PreferenceHelper(context).includeUsualHolidays();
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
			if (country != null && !country.isBlank()) {
				final String flag = Util.getCountryFlag(country);
				if (flag != null) {
					sb.append(' ').append(flag);
				}
			}
		}
		return sb.substring(1);
	}
}
