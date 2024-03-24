package eu.andret.kalendarzswiatnietypowych.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.core.app.NavUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import eu.andret.kalendarzswiatnietypowych.R;

public class SettingsActivity extends UHCActivity {
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		retrieveSupportActionBar().ifPresent(actionBar -> actionBar.setDisplayHomeAsUpEnabled(true));
		getSupportFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new PrefsFragment())
				.commit();

		getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				final Intent returnIntent = new Intent();
				setResult(Activity.RESULT_OK, returnIntent);
				NavUtils.navigateUpFromSameTask(SettingsActivity.this);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getOnBackPressedDispatcher().onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class PrefsFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
			setPreferencesFromResource(R.xml.preferences, rootKey);
			if (getContext() == null) {
				return;
			}
			final Preference aboutHolidaysPreference = findPreference(getContext().getString(R.string.settings_key_about_holidays));
			if (aboutHolidaysPreference == null) {
				return;
			}
			aboutHolidaysPreference.setOnPreferenceClickListener(preference -> {
				createAlertWithImage(getContext());
				return false;
			});

			final ListPreference themePreference = findPreference(getContext().getString(R.string.settings_key_app_theme));

			if (themePreference == null) {
				return;
			}

			themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
				final SettingsActivity activity = (SettingsActivity) getContext();
				activity.recreate();
				return true;
			});
		}

		private void createAlertWithImage(final Context context) {
			final View view = LayoutInflater.from(context).inflate(R.layout.image_alert, null);

			new AlertDialog.Builder(context)
					.setTitle(R.string.about_holidays)
					.setView(view)
					.setPositiveButton(R.string.ok, null)
					.create()
					.show();
		}
	}
}
