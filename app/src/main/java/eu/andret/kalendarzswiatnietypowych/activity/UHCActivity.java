package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.UHCApplication;
import eu.andret.kalendarzswiatnietypowych.persistance.HolidayViewModel;

public abstract class UHCActivity extends AppCompatActivity {
	protected HolidayViewModel holidayViewModel;

	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupTheme();
		holidayViewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(HolidayViewModel.INITIALIZER))
				.get(HolidayViewModel.class);
	}

	private void setupTheme() {
		final String[] themeValues = getResources().getStringArray(R.array.preference_theme_values);
		final String themeSetting = getSharedPreferences().getString(getResources().getString(R.string.settings_key_app_theme), themeValues[0]);
		switch (themeSetting) {
			case "system":
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
				break;
			case "light":
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
				break;
			case "dark":
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
				break;
			default:
				break;
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
	protected UHCApplication getUHCApplication() {
		return (UHCApplication) getApplication();
	}
}
