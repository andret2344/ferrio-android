package eu.andret.kalendarzswiatnietypowych.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import java.util.Calendar;

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
		final int[] prefs = {R.string.settings_theme_app, R.string.settings_theme_widgets, R.string.settings_theme_colorized, R.string.settings_usual_holidays, R.string.settings_display_shortcuts};// ,
		// R.string.settings_theme_notification};
		final PrefsFragment p = new PrefsFragment();
		final Bundle args = new Bundle();
		args.putIntArray("data", prefs);
		p.setArguments(args);
		getFragmentManager().beginTransaction().replace(android.R.id.content, p).commit();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}

	@SuppressLint("ValidFragment")
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
				if (current.equals(SettingsActivity.this.getResources().getString(R.string.settings_theme_notification))) {
					pref.setOnPreferenceClickListener(preference -> {
						final Calendar now = Calendar.getInstance();
						new TimePickerDialog(SettingsActivity.this, 0, (picker, hour, minute) -> {
							String shortcut = "";
							// Log.d("AC", hour + "h " + minute + "m");
							final String h;
							final String m;
							if (picker.is24HourView()) {
								hour = hour == 0 ? 24 : hour;
								h = (hour < 10 ? "0" : "") + hour;
								m = (minute < 10 ? "0" : "") + minute;
							} else {
								shortcut = hour >= 12 || hour % 24 != 0 ? "PM" : "AM";
								hour = hour % 12 == 0 ? 12 : hour % 12;
								h = (hour < 10 ? "0" : "") + hour;
								m = (minute < 10 ? "0" : "") + minute;
							}

							pref.setSummary("Notification scheduled: " + h + ":" + m + " " + shortcut);
							final SharedPreferences prefs = Data.getPreferences(SettingsActivity.this, Data.Prefs.THEME);
							final SharedPreferences.Editor editor = prefs.edit();
							editor.putString(current, hour + ":" + minute);
							editor.apply();

						}, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
						return false;
					});
				}

				pref.setOnPreferenceChangeListener((preference, value) -> {
					final SharedPreferences prefs = Data.getPreferences(SettingsActivity.this, Data.Prefs.THEME);
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
						if (current.equals(SettingsActivity.this.getResources().getString(R.string.settings_theme_app))) {
							recreate();
						}
					}
					return true;
				});
			}
		}
	}
}
