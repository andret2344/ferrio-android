package eu.andret.kalendarzswiatnietypowych;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.android.gms.ads.MobileAds;

import eu.andret.kalendarzswiatnietypowych.persistence.AppRepository;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.PreferenceHelper;
import eu.andret.kalendarzswiatnietypowych.widget.MidnightWidgetRefreshWorker;
import eu.andret.kalendarzswiatnietypowych.widget.TransparentWidgetProvider;
import eu.andret.kalendarzswiatnietypowych.widget.WidgetProvider;

public class FerrioApplication extends Application {
	/**
	 * Shared bounded pool for IO-bound work (network, Room writes). Using a dedicated pool avoids
	 * starving the common ForkJoinPool, which is sized for CPU-bound work and is also shared with
	 * parallel streams across the process. Threads are daemon + named so they don't keep the JVM
	 * alive in tests and show up clearly in logcat / thread dumps.
	 */
	public static final ExecutorService IO_EXECUTOR = Executors.newFixedThreadPool(4, new IoThreadFactory());

	private final AtomicBoolean adsInitialized = new AtomicBoolean(false);

	private AppRepository appRepository;
	private ApiClient apiClient;

	@Override
	public void onCreate() {
		super.onCreate();
		applyNightMode();
		apiClient = new ApiClient(this);
		appRepository = new AppRepository(this, apiClient);
		// MobileAds.initialize is intentionally NOT called here: it posts setup work back to
		// the main Looper and triggers a first-time WebView classloader load, which blocks
		// the main thread during cold starts from widget broadcasts. Ads are initialized
		// lazily from BaseActivity.registerAdView instead.
		MidnightWidgetRefreshWorker.schedule(this);
	}

	private void applyNightMode() {
		final String[] themeValues = getResources().getStringArray(R.array.preference_theme_values);
		final String themeSetting = new PreferenceHelper(this).getAppTheme(themeValues[0]);
		if (themeSetting.equals(themeValues[1])) {
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		} else if (themeSetting.equals(themeValues[2])) {
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
		} else {
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		}
	}

	public AppRepository getAppRepository() {
		return appRepository;
	}

	public ApiClient getApiClient() {
		return apiClient;
	}

	/**
	 * Lazily initializes the Google Mobile Ads SDK on the first ad-bearing screen. Posted to
	 * {@link #IO_EXECUTOR} because MobileAds.initialize() triggers a first-time WebView classloader
	 * load and posts setup back to the main Looper, which would jank cold starts.
	 */
	public void ensureAdsInitialized() {
		if (adsInitialized.compareAndSet(false, true)) {
			IO_EXECUTOR.execute(() -> MobileAds.initialize(this));
		}
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

	private static final class IoThreadFactory implements ThreadFactory {
		private final AtomicInteger counter = new AtomicInteger(1);

		@Override
		public Thread newThread(@NonNull final Runnable r) {
			final Thread t = new Thread(r, "ferrio-io-" + counter.getAndIncrement());
			t.setDaemon(true);
			return t;
		}
	}
}
