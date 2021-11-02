package eu.andret.kalendarzswiatnietypowych.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

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
		final Util util = new Util(this);
		util.applyTheme();
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		final int[] prefs = {R.string.settings_theme_app, R.string.settings_theme_widgets, R.string.settings_theme_colorized, R.string.settings_usual_holidays, R.string.settings_display_shortcuts};
		final PrefsFragment prefsFragment = new PrefsFragment();
		final Bundle bundle = new Bundle();
		bundle.putIntArray("data", prefs);
		prefsFragment.setArguments(bundle);
		getFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, prefsFragment)
				.commit();
	}

	public class PrefsFragment extends PreferenceFragment {
		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
			for (final int s : getArguments().getIntArray("data")) {
				final String current = getResources().getString(s);
				final Preference pref = findPreference(current);
				if (pref instanceof ListPreference) {
					final ListPreference list = (ListPreference) pref;
					pref.setSummary(list.getEntry());
				}

				pref.setOnPreferenceChangeListener((preference, value) -> {
					final SharedPreferences prefs = Data.getPreferences(getActivity(), Data.Prefs.THEME);
					final SharedPreferences.Editor editor = prefs.edit();
					if (value instanceof Boolean) {
						editor.putBoolean(current, (Boolean) value);
					} else {
						editor.putString(current, value.toString());
					}
					editor.apply();
					if (pref instanceof ListPreference) {
						final ListPreference list = (ListPreference) pref;
						pref.setSummary(list.getEntries()[list.findIndexOfValue(String.valueOf(value))]);
						if (current.equals(getActivity().getResources().getString(R.string.settings_theme_app))) {
							recreate();
						}
					}
					return true;
				});
			}
		}
	}
}
