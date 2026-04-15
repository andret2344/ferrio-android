package eu.andret.kalendarzswiatnietypowych;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.andret.kalendarzswiatnietypowych.persistence.AppRepository;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.widget.TransparentWidgetProvider;
import eu.andret.kalendarzswiatnietypowych.widget.WidgetProvider;

public class FerrioApplication extends Application {
	/**
	 * Shared bounded pool for IO-bound work (network, Room writes). Using a dedicated pool avoids
	 * starving the common ForkJoinPool, which is sized for CPU-bound work and is also shared with
	 * parallel streams across the process.
	 */
	public static final ExecutorService IO_EXECUTOR = Executors.newFixedThreadPool(4);

	private AppRepository appRepository;
	private ApiClient apiClient;

	@Override
	public void onCreate() {
		super.onCreate();
		apiClient = new ApiClient();
		appRepository = new AppRepository(this, apiClient);
		// scheduleMidnightWidgetRefresh issues binder calls (AppWidgetManager, AlarmManager).
		// Keeping it off Application.onCreate avoids an ANR when the process is cold-started
		// by APPWIDGET_ENABLED or similar system broadcasts on a loaded system_server.
		// MobileAds.initialize is intentionally NOT called here: it posts setup work back to
		// the main Looper and triggers a first-time WebView classloader load, which blocks
		// the main thread during cold starts from widget broadcasts. Ads are initialized
		// lazily from BaseActivity.registerAdView instead.
		IO_EXECUTOR.execute(this::scheduleMidnightWidgetRefresh);
	}

	public AppRepository getAppRepository() {
		return appRepository;
	}

	public ApiClient getApiClient() {
		return apiClient;
	}

	public static void refreshWidgets(@NonNull final Context context) {
		final AppWidgetManager manager = AppWidgetManager.getInstance(context);

		final int[] ids = manager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
		if (ids.length > 0) {
			final Intent intent = new Intent(context, WidgetProvider.class);
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
			context.sendBroadcast(intent);
		}

		final int[] transparentIds = manager.getAppWidgetIds(new ComponentName(context, TransparentWidgetProvider.class));
		if (transparentIds.length > 0) {
			final Intent transparentIntent = new Intent(context, TransparentWidgetProvider.class);
			transparentIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			transparentIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, transparentIds);
			context.sendBroadcast(transparentIntent);
		}
	}

	private void scheduleMidnightWidgetRefresh() {
		final long midnightMillis = LocalDate.now()
				.plusDays(1)
				.atTime(LocalTime.MIDNIGHT)
				.atZone(ZoneId.systemDefault())
				.toInstant()
				.toEpochMilli();

		final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		final AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);

		final Intent intent = new Intent(this, WidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				widgetManager.getAppWidgetIds(new ComponentName(this, WidgetProvider.class)));
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		alarmManager.setInexactRepeating(AlarmManager.RTC,
				midnightMillis, AlarmManager.INTERVAL_DAY, pendingIntent);

		final Intent transparentIntent = new Intent(this, TransparentWidgetProvider.class);
		transparentIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		transparentIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
				widgetManager.getAppWidgetIds(new ComponentName(this, TransparentWidgetProvider.class)));
		final PendingIntent transparentPendingIntent = PendingIntent.getBroadcast(
				this, 1, transparentIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		alarmManager.setInexactRepeating(AlarmManager.RTC,
				midnightMillis, AlarmManager.INTERVAL_DAY, transparentPendingIntent);
	}
}
