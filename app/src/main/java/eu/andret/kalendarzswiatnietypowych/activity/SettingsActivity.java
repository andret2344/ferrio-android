package eu.andret.kalendarzswiatnietypowych.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.appbar.MaterialToolbar;

import eu.andret.kalendarzswiatnietypowych.R;

public class SettingsActivity extends UHCActivity {
	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		final MaterialToolbar toolbar = findViewById(R.id.activity_settings_toolbar);
		setSupportActionBar(toolbar);
		retrieveSupportActionBar().ifPresent(actionBar ->
				actionBar.setDisplayHomeAsUpEnabled(true));
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.activity_settings_content, new PrefsFragment())
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
	public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getOnBackPressedDispatcher().onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class PrefsFragment extends PreferenceFragmentCompat {
		@Override
		public void onCreatePreferences(@Nullable final Bundle savedInstanceState, @Nullable final String rootKey) {
			setPreferencesFromResource(R.xml.preferences, rootKey);
			if (getContext() == null) {
				return;
			}

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
	}

	public static void createAlertWithImage(final Context context, final int img, final int title, final int text) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(context);
		alert.setTitle(title);
		final LinearLayout layout = new LinearLayout(context);
		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 30, 0, 0);
		layout.setLayoutParams(params);
		layout.setOrientation(LinearLayout.VERTICAL);
		final ImageView image = new ImageView(context);
		image.setImageResource(img);
		layout.addView(image);
		final TextView tv = new TextView(context);
		tv.setText(text);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.adapter_search_main));
		layout.addView(tv);
		final LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		llp.setMargins(30, 20, 30, 20);
		tv.setLayoutParams(llp);
		alert.setView(layout);
		alert.setPositiveButton(R.string.ok, null);
		alert.show();
	}
}
