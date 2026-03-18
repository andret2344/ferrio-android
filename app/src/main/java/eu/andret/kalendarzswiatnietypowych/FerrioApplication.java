package eu.andret.kalendarzswiatnietypowych;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.MobileAds;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import eu.andret.kalendarzswiatnietypowych.persistance.AppRepository;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.widget.TransparentWidgetProvider;
import eu.andret.kalendarzswiatnietypowych.widget.WidgetProvider;

public class FerrioApplication extends Application {
	private AppRepository appRepository;
	private ApiClient apiClient;

	@Override
	public void onCreate() {
		super.onCreate();
		MobileAds.initialize(this);
		apiClient = new ApiClient();
		appRepository = new AppRepository(this, apiClient);
		refreshWidgets(this);
		scheduleMidnightWidgetRefresh();
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
