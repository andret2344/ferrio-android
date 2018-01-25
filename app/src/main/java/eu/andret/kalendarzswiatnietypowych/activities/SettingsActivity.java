package eu.andret.kalendarzswiatnietypowych.activities;

import java.util.Calendar;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TimePicker;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class SettingsActivity extends AppCompatActivity {
	private static SettingsActivity instance;
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		NavUtils.navigateUpFromSameTask(this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		Util util = new Util(this);
		util.applyTheme();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		int[] prefs = {R.string.settings_theme_app, R.string.settings_theme_widgets, R.string.settings_theme_colorized, R.string.settings_usual_holidays, R.string.settings_display_shortcuts};// ,
		// R.string.settings_theme_notification};
		PrefsFragment p = new PrefsFragment();
		Bundle args = new Bundle();
		args.putIntArray("data", prefs);
		p.setArguments(args);
		getFragmentManager().beginTransaction().replace(android.R.id.content, p).commit();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
	}
	
	public static class PrefsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
			for (final int s : getArguments().getIntArray("data")) {
				final String current = instance.getResources().getString(s);
				final Preference pref = findPreference(current);
				if (pref instanceof ListPreference) {
					ListPreference list = (ListPreference) pref;
					pref.setSummary(list.getEntry());
				}
				if (current.equals(instance.getResources().getString(R.string.settings_theme_notification))) {
					pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference preference) {
							Calendar now = Calendar.getInstance();
							new TimePickerDialog(instance, 0, new TimePickerDialog.OnTimeSetListener() {
								@Override
								public void onTimeSet(TimePicker picker, int hour, int minute) {
									String shortcut = "";
									// Log.d("AC", hour + "h " + minute + "m");
									String h, m;
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
									SharedPreferences prefs = Data.getPreferences(instance, Data.Prefs.THEME);
									SharedPreferences.Editor editor = prefs.edit();
									editor.putString(current, hour + ":" + minute);
									editor.apply();
									
								}
							}, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
							return false;
						}
					});
				}
				
				pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					@Override
					public boolean onPreferenceChange(Preference preference, Object value) {
						SharedPreferences prefs = Data.getPreferences(instance, Data.Prefs.THEME);
						SharedPreferences.Editor editor = prefs.edit();
						if (value instanceof Boolean) {
							editor.putBoolean(current, (Boolean) value);
						} else {
							editor.putString(current, value.toString());
						}
						editor.apply();
						if (pref instanceof ListPreference) {
							ListPreference list = (ListPreference) pref;
							pref.setSummary(list.getEntries()[list.findIndexOfValue(String.valueOf(value))]);
							if (current.equals(instance.getResources().getString(R.string.settings_theme_app))) {
								instance.recreate();
							}
						}
						MainActivity.getInstance().update();
						return true;
					}
				});
			}
		}
	}
}
