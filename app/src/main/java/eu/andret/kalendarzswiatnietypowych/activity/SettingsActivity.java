package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NavUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
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
		public void onCreatePreferences(@Nullable final Bundle savedInstanceState,
				@Nullable final String rootKey) {
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

			Optional.ofNullable(this.<Preference>findPreference(getContext().getString(R.string.settings_key_usual_holidays)))
					.ifPresent(o -> o.setOnPreferenceChangeListener((preference, newValue) -> {
						FerrioApplication.refreshWidgets(requireContext());
						return true;
					}));

			Optional.ofNullable(this.<Preference>findPreference(getContext().getString(R.string.settings_key_month_view_mode)))
					.ifPresent(o -> {
						updateMonthViewModeSummary(o);
						o.setOnPreferenceClickListener(preference -> {
							showMonthViewModeDialog(preference);
							return true;
						});
					});

			Optional.ofNullable(this.<Preference>findPreference(getContext().getString(R.string.settings_key_logout)))
					.ifPresent(o -> o.setOnPreferenceClickListener(preference -> {
						requireActivity().finishAffinity();
						firebaseAuth.signOut();
						FerrioApplication.refreshWidgets(requireContext());
						final Intent intent = new Intent(getContext(), LoginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						return true;
					}));
		}

		private void updateMonthViewModeSummary(@NonNull final Preference preference) {
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
			final String defaultMode = requireContext().getString(R.string.month_view_mode_value_compact);
			final String mode = prefs.getString(requireContext().getString(R.string.settings_key_month_view_mode), defaultMode);
			if (mode.equals(requireContext().getString(R.string.month_view_mode_value_compact))) {
				preference.setSummary(R.string.month_view_mode_compact);
			} else if (mode.equals(requireContext().getString(R.string.month_view_mode_value_simple))) {
				preference.setSummary(R.string.month_view_mode_simple);
			} else {
				preference.setSummary(R.string.month_view_mode_detailed);
			}
		}

		private void showMonthViewModeDialog(@NonNull final Preference preference) {
			final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
			final String defaultMode = requireContext().getString(R.string.month_view_mode_value_compact);
			final String currentMode = prefs.getString(requireContext().getString(R.string.settings_key_month_view_mode), defaultMode);

			final View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_view_mode, null);

			final RadioButton radioDetailed = dialogView.findViewById(R.id.radio_detailed);
			final RadioButton radioCompact = dialogView.findViewById(R.id.radio_compact);
			final RadioButton radioSimple = dialogView.findViewById(R.id.radio_simple);

			if (currentMode.equals(requireContext().getString(R.string.month_view_mode_value_compact))) {
				radioCompact.setChecked(true);
			} else if (currentMode.equals(requireContext().getString(R.string.month_view_mode_value_simple))) {
				radioSimple.setChecked(true);
			} else {
				radioDetailed.setChecked(true);
			}

			final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
					.setTitle(R.string.dialog_title_month_view_mode)
					.setView(dialogView);

			final AlertDialog dialog = builder.show();

			final View.OnClickListener clickListener = v -> {
				final String selected;
				if (v.getId() == R.id.option_compact || v.getId() == R.id.radio_compact) {
					selected = requireContext().getString(R.string.month_view_mode_value_compact);
				} else if (v.getId() == R.id.option_simple || v.getId() == R.id.radio_simple) {
					selected = requireContext().getString(R.string.month_view_mode_value_simple);
				} else {
					selected = requireContext().getString(R.string.month_view_mode_value_detailed);
				}
				prefs.edit().putString(requireContext().getString(R.string.settings_key_month_view_mode), selected).apply();
				updateMonthViewModeSummary(preference);
				dialog.dismiss();
			};

			dialogView.findViewById(R.id.option_detailed).setOnClickListener(clickListener);
			dialogView.findViewById(R.id.option_compact).setOnClickListener(clickListener);
			dialogView.findViewById(R.id.option_simple).setOnClickListener(clickListener);
			radioDetailed.setOnClickListener(clickListener);
			radioCompact.setOnClickListener(clickListener);
			radioSimple.setOnClickListener(clickListener);
		}
	}
}
