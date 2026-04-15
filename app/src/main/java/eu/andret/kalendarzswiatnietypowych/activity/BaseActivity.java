package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.persistence.HolidayViewModel;

public abstract class BaseActivity extends AppCompatActivity {
	private static final AtomicBoolean adsInitialized = new AtomicBoolean(false);

	protected HolidayViewModel holidayViewModel;
	@Nullable
	private AdView managedAdView;

	/**
	 * Subclasses call this once after {@code setContentView(...)} to hand the bottom banner off
	 * to the activity lifecycle. We forward {@code pause/resume/destroy} to the AdView so the
	 * underlying WebView/IME state is torn down when the screen goes off, which mitigates an
	 * upstream ANR pattern where SCREEN_OFF races with an in-flight chromium IME init.
	 */
	protected void registerAdView(@NonNull final AdView adView) {
		managedAdView = adView;
		if (adsInitialized.compareAndSet(false, true)) {
			FerrioApplication.IO_EXECUTOR.execute(() -> MobileAds.initialize(this));
		}
		adView.loadAd(new AdRequest.Builder().build());
	}

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
		setupTheme();
		holidayViewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(HolidayViewModel.INITIALIZER))
				.get(HolidayViewModel.class);
	}

	private void setupTheme() {
		final String[] themeValues = getResources().getStringArray(R.array.preference_theme_values);
		final String themeSetting = getSharedPreferences().getString(getResources().getString(R.string.settings_key_app_theme), themeValues[0]);
		if (themeSetting.equals(themeValues[0])) {
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
		} else if (themeSetting.equals(themeValues[1])) {
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		} else if (themeSetting.equals(themeValues[2])) {
			AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
		}
	}

	@NonNull
	protected Optional<ActionBar> retrieveSupportActionBar() {
		return Optional.ofNullable(getSupportActionBar());
	}

	@NonNull
	protected SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(this);
	}

	@NonNull
	protected FerrioApplication getFerrioApplication() {
		return (FerrioApplication) getApplication();
	}

	@Override
	protected void onPause() {
		if (managedAdView != null) {
			managedAdView.pause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (managedAdView != null) {
			managedAdView.resume();
		}
	}

	@Override
	protected void onDestroy() {
		if (managedAdView != null) {
			managedAdView.destroy();
			managedAdView = null;
		}
		super.onDestroy();
	}
}
