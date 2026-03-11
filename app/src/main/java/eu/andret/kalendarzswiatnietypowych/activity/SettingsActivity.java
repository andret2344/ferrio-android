package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Optional;

import androidx.appcompat.app.AppCompatDelegate;

import eu.andret.kalendarzswiatnietypowych.R;

public class SettingsActivity extends BaseActivity {
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
				setResult(RESULT_OK, returnIntent);
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
			final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

			if (getContext() == null) {
				return;
			}

			Optional.ofNullable(this.<ListPreference>findPreference(getContext().getString(R.string.settings_key_app_theme)))
					.ifPresent(o -> o.setOnPreferenceChangeListener((preference, newValue) -> {
						switch ((String) newValue) {
							case "light":
								AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
								break;
							case "dark":
								AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
								break;
							default:
								AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
								break;
						}
						return true;
					}));

			Optional.ofNullable(this.<Preference>findPreference(getContext().getString(R.string.settings_key_logout)))
					.ifPresent(o -> o.setOnPreferenceClickListener(preference -> {
						requireActivity().finishAffinity();
						firebaseAuth.signOut();
						final Intent intent = new Intent(getContext(), LoginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						return true;
					}));
		}
	}
}
