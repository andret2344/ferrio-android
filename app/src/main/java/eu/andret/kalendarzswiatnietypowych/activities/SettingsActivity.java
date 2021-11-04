package eu.andret.kalendarzswiatnietypowych.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

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
		Util.applyTheme(this);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		final int[] prefs = {R.string.settings_theme_app, R.string.settings_theme_widgets, R.string.settings_theme_colorized, R.string.settings_usual_holidays, R.string.settings_display_shortcuts};
		final PrefsFragment prefsFragment = new PrefsFragment();
		final Bundle bundle = new Bundle();
		bundle.putIntArray("data", prefs);
		prefsFragment.setArguments(bundle);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, prefsFragment)
				.commit();
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
							final ListPreference list = (ListPreference) pref;
							pref.setSummary(list.getEntry());
						}

						pref.setOnPreferenceChangeListener((preference, value) -> {
							final SharedPreferences prefs = Data.getPreferences(getActivity(), Data.Prefs.THEME);
							final SharedPreferences.Editor editor = prefs.edit();
							if (value instanceof Boolean) {
								editor.putBoolean(current, (Boolean) value);
							} else if (value instanceof Integer) {
								editor.putInt(current, (int) value);
							}
							editor.apply();
							if (pref instanceof ListPreference) {
								final ListPreference list = (ListPreference) pref;
								pref.setSummary(list.getEntries()[list.findIndexOfValue(String.valueOf(value))]);
								if (current.equals(getActivity().getResources().getString(R.string.settings_theme_app))) {
									getActivity().recreate();
								}
							}
							return true;
						});
					});
		}
	}
}
