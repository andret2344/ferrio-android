package eu.andret.kalendarzswiatnietypowych;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;

import com.google.android.gms.ads.MobileAds;

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
		refreshWidgets();
	}

	private void refreshWidgets() {
		final AppWidgetManager manager = AppWidgetManager.getInstance(this);
		final int[] ids = manager.getAppWidgetIds(new ComponentName(this, WidgetProvider.class));
		if (ids.length > 0) {
			final Intent intent = new Intent(this, WidgetProvider.class);
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
			sendBroadcast(intent);
		}
	}

	public AppRepository getAppRepository() {
		return appRepository;
	}

	public ApiClient getApiClient() {
		return apiClient;
	}
}
