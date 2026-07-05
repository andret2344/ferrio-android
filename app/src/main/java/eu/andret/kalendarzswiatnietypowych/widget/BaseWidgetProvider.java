package eu.andret.kalendarzswiatnietypowych.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.core.widget.RemoteViewsCompat;
import androidx.core.widget.RemoteViewsCompat.RemoteCollectionItems;

import com.google.firebase.auth.FirebaseAuth;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.BuildConfig;
import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.activity.LoginActivity;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
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
		final boolean showWeekday = WidgetPrefs.isShowWeekday(context, appWidgetId);

		final LocalDate targetDate = LocalDate.now(ZoneId.systemDefault()).plusDays(daysOffset);
		final String dateText = buildDateText(targetDate, showWeekday);
		final PendingIntent pendingIntent = buildDayPendingIntent(context, targetDate, appWidgetId);

		final RemoteViews views = new RemoteViews(context.getPackageName(), layoutResId);
		views.setTextViewText(R.id.widget_text_date, dateText);
		views.setTextViewText(R.id.widget_text_holidays, context.getString(R.string.loading));
		views.setViewVisibility(R.id.widget_text_holidays, View.VISIBLE);
		views.setViewVisibility(R.id.widget_layout_holidays, View.GONE);
		views.setViewVisibility(R.id.widget_layout_login, View.GONE);
		views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
		applyColorized(views, context, colorized, targetDate, layoutResId);
		applyDateFontSize(context, views, fontSizeOffset);
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	/**
	 * Synchronous data fetch and final render. Called from {@link WidgetUpdateWorker}; must
	 * never run on the main thread. Builds every row {@link RemoteViews} inline,
	 * so the list is fully rendered from a single {@code updateAppWidget} call — no
	 * {@code RemoteViewsService} or factory needed.
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
		final boolean showWeekday = WidgetPrefs.isShowWeekday(context, appWidgetId);

		final LocalDate targetDate = LocalDate.now(ZoneId.systemDefault()).plusDays(daysOffset);
		final String dateText = buildDateText(targetDate, showWeekday);
		final PendingIntent pendingIntent = buildDayPendingIntent(context, targetDate, appWidgetId);

		final int monthValue = targetDate.getMonthValue();
		final int dayOfMonth = targetDate.getDayOfMonth();

		final PreferenceHelper preferences = new PreferenceHelper(context);
		final boolean includeUsual = preferences.includeUsualHolidays();
		final boolean showAdult = preferences.showAdultContent();
		List<Holiday> holidays = repository.getHolidaysByDaySync(monthValue, dayOfMonth);
		if (holidays.isEmpty()) {
			try {
				holidays = apiClient.getList(apiClient.buildHolidaysUrl(monthValue, dayOfMonth, showAdult), Holiday.class);
			} catch (final ApiException ex) {
				throw new RuntimeException(ex);
			}
		}

		final float itemTextSizeSp = Math.max(MIN_FONT_SIZE_SP,
				readSpDimen(context.getResources(), R.dimen.widget_text_holidays_size)
						+ fontSizeOffset);
		final RemoteCollectionItems items = buildCollectionItems(context, layoutResId, holidays,
				includeUsual, showAdult, itemTextSizeSp);

		final RemoteViews views = new RemoteViews(context.getPackageName(), layoutResId);
		views.setTextViewText(R.id.widget_text_date, dateText);
		views.setViewVisibility(R.id.widget_text_holidays, View.GONE);
		views.setViewVisibility(R.id.widget_layout_holidays, View.VISIBLE);
		views.setViewVisibility(R.id.widget_layout_login, View.GONE);
		views.setEmptyView(R.id.widget_list_holidays, R.id.widget_empty_holidays);
		views.setPendingIntentTemplate(R.id.widget_list_holidays, pendingIntent);
		views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
		applyColorized(views, context, colorized, targetDate, layoutResId);
		applyDateFontSize(context, views, fontSizeOffset);
		RemoteViewsCompat.setRemoteAdapter(context, views, appWidgetId,
				R.id.widget_list_holidays, items);
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
		views.setViewVisibility(R.id.widget_layout_holidays, View.GONE);
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
		final boolean showWeekday = WidgetPrefs.isShowWeekday(context, appWidgetId);
		final LocalDate targetDate = LocalDate.now(ZoneId.systemDefault()).plusDays(daysOffset);
		final String dateText = buildDateText(targetDate, showWeekday);

		final Intent loginIntent = new Intent(context, LoginActivity.class);
		loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		final PendingIntent pendingIntent = PendingIntent.getActivity(
				context, 0, loginIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		final RemoteViews views = new RemoteViews(context.getPackageName(), layoutResId);
		views.setTextViewText(R.id.widget_text_date, dateText);
		views.setViewVisibility(R.id.widget_text_holidays, View.GONE);
		views.setViewVisibility(R.id.widget_layout_holidays, View.GONE);
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
	private static String buildDateText(@NonNull final LocalDate date, final boolean showWeekday) {
		final Pair<Month, Integer> datePair = new Pair<>(date.getMonth(), date.getDayOfMonth());
		final String formattedDate = Util.getFormattedDate(datePair);
		if (!showWeekday) {
			return formattedDate;
		}
		final String weekday = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
		return weekday + ", " + formattedDate;
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

	private static void applyDateFontSize(@NonNull final Context context,
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
	private static RemoteCollectionItems buildCollectionItems(@NonNull final Context context,
			@LayoutRes final int parentLayoutResId, @NonNull final List<Holiday> holidays,
			final boolean includeUsual, final boolean showAdult, final float itemTextSizeSp) {
		final int rowLayoutResId = parentLayoutResId == R.layout.widget_transparent
				? R.layout.adapter_widget_holiday_transparent
				: R.layout.adapter_widget_holiday;
		final RemoteCollectionItems.Builder builder = new RemoteCollectionItems.Builder()
				.setHasStableIds(true)
				.setViewTypeCount(1);
		long id = 0;
		for (final Holiday holiday : holidays) {
			if (!includeUsual && holiday.isUsual()) {
				continue;
			}
			if (!showAdult && holiday.isMatureContent()) {
				continue;
			}
			final RemoteViews row = new RemoteViews(context.getPackageName(), rowLayoutResId);
			row.setTextViewText(R.id.widget_item_text, buildItemText(context, holiday, itemTextSizeSp));
			row.setTextViewTextSize(R.id.widget_item_text, TypedValue.COMPLEX_UNIT_SP, itemTextSizeSp);
			row.setOnClickFillInIntent(R.id.widget_item_text, new Intent());
			builder.addItem(id++, row);
		}
		return builder.build();
	}

	@NonNull
	private static CharSequence buildItemText(@NonNull final Context context,
			@NonNull final Holiday holiday, final float itemTextSizeSp) {
		final String bulletPrefix = context.getString(R.string.bullet_point) + " ";
		final StringBuilder sb = new StringBuilder(bulletPrefix).append(holiday.getName());
		final String country = holiday.getCountry();
		if (country != null && !country.isBlank()) {
			final String flag = Util.getCountryFlag(country);
			if (flag != null) {
				sb.append(' ').append(flag);
			}
		}
		final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				itemTextSizeSp, context.getResources().getDisplayMetrics());
		final Paint paint = new Paint();
		paint.setTextSize(textSizePx);
		final int indentPx = Math.round(paint.measureText(bulletPrefix));
		final SpannableString span = new SpannableString(sb);
		span.setSpan(new LeadingMarginSpan.Standard(0, indentPx), 0, span.length(),
				Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		return span;
	}
}
