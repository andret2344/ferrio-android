package eu.andret.kalendarzswiatnietypowych.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import eu.andret.kalendarzswiatnietypowych.R;

public class SettingsActivity extends AppCompatActivity {
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		final Intent returnIntent = new Intent();
		setResult(Activity.RESULT_OK, returnIntent);
		NavUtils.navigateUpFromSameTask(this);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		final int[] prefs = {R.string.settings_key_theme_app, R.string.settings_key_theme_colorized, R.string.settings_key_usual_holidays, R.string.settings_key_display_shortcuts};
		final PrefsFragment prefsFragment = new PrefsFragment();
		final Bundle bundle = new Bundle();
		bundle.putIntArray("data", prefs);
		prefsFragment.setArguments(bundle);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, prefsFragment)
				.commit();

		PreferenceManager.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
					final String themeSettingsKey = getString(R.string.settings_key_theme_app);
					if (key.equals(themeSettingsKey)) {
						final String themeDarkKey = getString(R.string.settings_key_theme_dark);
						final String themeLightKey = getString(R.string.settings_key_theme_light);
						final String themeStoredKey = PreferenceManager.getDefaultSharedPreferences(this)
								.getString(themeSettingsKey, themeDarkKey);
						if (themeStoredKey.equals(themeDarkKey)) {
							AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
						} else if (themeStoredKey.equals(themeLightKey)) {
							AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
						}
						recreate();
					}
				});
	}

	public static class PrefsFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
			setPreferencesFromResource(R.xml.preferences, rootKey);
			Optional.of(this)
					.map(Fragment::getArguments)
					.map(bundle -> bundle.getIntArray("data"))
					.map(Arrays::stream)
					.orElse(IntStream.empty())
					.forEach(s -> {
						final String current = getResources().getString(s);
						final Preference pref = findPreference(current);
						if (pref == null) {
							return;
						}
						if (pref instanceof ListPreference) {
							final ListPreference preference = (ListPreference) pref;
							preference.setSummaryProvider((Preference.SummaryProvider<ListPreference>) ListPreference::getEntry);
						}
					});
		}
	}
}
