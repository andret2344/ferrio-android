package eu.andret.kalendarzswiatnietypowych.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.persistence.HolidayViewModel;
import eu.andret.kalendarzswiatnietypowych.util.PreferenceHelper;

public abstract class BaseActivity extends AppCompatActivity {
	protected HolidayViewModel holidayViewModel;
	@Nullable
	private AdView managedAdView;
	@Nullable
	private PreferenceHelper preferenceHelper;

	/**
	 * Subclasses call this once after {@code setContentView(...)} to hand the bottom banner off
	 * to the activity lifecycle. We forward {@code pause/resume/destroy} to the AdView so the
	 * underlying WebView/IME state is torn down when the screen goes off, which mitigates an
	 * upstream ANR pattern where SCREEN_OFF races with an in-flight chromium IME init.
	 */
	protected void registerAdView(@NonNull final AdView adView) {
		managedAdView = adView;
		getFerrioApplication().ensureAdsInitialized();
		adView.loadAd(new AdRequest.Builder().build());
	}

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
		holidayViewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(HolidayViewModel.INITIALIZER))
				.get(HolidayViewModel.class);
	}

	@NonNull
	protected Optional<ActionBar> retrieveSupportActionBar() {
		return Optional.ofNullable(getSupportActionBar());
	}

	@NonNull
	protected PreferenceHelper getPreferences() {
		if (preferenceHelper == null) {
			preferenceHelper = new PreferenceHelper(this);
		}
		return preferenceHelper;
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
