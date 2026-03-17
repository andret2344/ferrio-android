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
	}

	private void scheduleMidnightWidgetRefresh() {
		final long midnightMillis = LocalDate.now()
				.plusDays(1)
				.atTime(LocalTime.MIDNIGHT)
				.atZone(ZoneId.systemDefault())
				.toInstant()
				.toEpochMilli();

		final Intent intent = new Intent(this, WidgetProvider.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		final int[] ids = AppWidgetManager.getInstance(this)
				.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);

		final PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

		final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.RTC,
				midnightMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
	}
}
