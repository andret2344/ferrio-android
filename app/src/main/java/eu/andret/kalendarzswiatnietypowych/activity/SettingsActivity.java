package eu.andret.kalendarzswiatnietypowych.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.databinding.ActivitySettingsBinding;
import eu.andret.kalendarzswiatnietypowych.databinding.DialogAdultContentBinding;
import eu.andret.kalendarzswiatnietypowych.databinding.DialogMonthViewModeBinding;
import eu.andret.kalendarzswiatnietypowych.util.PreferenceHelper;

public class SettingsActivity extends BaseActivity {
	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setSupportActionBar(binding.activitySettingsToolbar);
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
			final PreferenceHelper prefs = new PreferenceHelper(getContext());

			Optional.ofNullable(this.<ListPreference>findPreference(prefs.appThemeKey()))
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

			Optional.ofNullable(this.<Preference>findPreference(prefs.usualHolidaysKey()))
					.ifPresent(o -> o.setOnPreferenceChangeListener((preference, newValue) -> {
						FerrioApplication.refreshWidgets(requireContext());
						return true;
					}));

			Optional.ofNullable(this.<SwitchPreference>findPreference(prefs.showAdultContentKey()))
					.ifPresent(o -> o.setOnPreferenceChangeListener((preference, newValue) -> {
						if (Boolean.TRUE.equals(newValue)) {
							showAdultContentConfirmationDialog(o, prefs);
							return false;
						}
						FerrioApplication.refreshWidgets(requireContext());
						((FerrioApplication) requireActivity().getApplicationContext())
								.getAppRepository().refresh();
						return true;
					}));

			Optional.ofNullable(this.<Preference>findPreference(prefs.monthViewModeKey()))
					.ifPresent(o -> {
						updateMonthViewModeSummary(o);
						o.setOnPreferenceClickListener(preference -> {
							showMonthViewModeDialog(preference);
							return true;
						});
					});

			Optional.ofNullable(this.<Preference>findPreference(prefs.logoutKey()))
					.ifPresent(o -> o.setOnPreferenceClickListener(preference -> {
						final FirebaseUser currentUser = firebaseAuth.getCurrentUser();
						if (currentUser != null && currentUser.isAnonymous()) {
							currentUser.getIdToken(true)
									.addOnCompleteListener(task -> {
										if (task.isSuccessful()) {
											currentUser.delete();
										}
									});
						}
						final FerrioApplication app = (FerrioApplication) requireActivity().getApplicationContext();
						app.getAppRepository().clearAll().whenCompleteAsync((unused, throwable) -> {
							if (!isAdded()) {
								return;
							}
							firebaseAuth.signOut();
							FerrioApplication.refreshWidgets(requireContext());
							final Intent intent = new Intent(requireContext(), LoginActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
							requireActivity().finishAffinity();
						}, ContextCompat.getMainExecutor(requireContext()));
						return true;
					}));
		}

		private void showAdultContentConfirmationDialog(@NonNull final SwitchPreference preference,
				@NonNull final PreferenceHelper prefs) {
			final DialogAdultContentBinding dialogBinding = DialogAdultContentBinding.inflate(getLayoutInflater());

			final AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
					.setTitle(R.string.dialog_title_adult_content)
					.setMessage(R.string.dialog_message_adult_content)
					.setView(dialogBinding.getRoot())
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(R.string.dialog_adult_content_enable, (d, which) -> {
						prefs.setShowAdultContent(true);
						prefs.setAdultContentConfirmedAt(
								OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
						final Preference.OnPreferenceChangeListener listener = preference.getOnPreferenceChangeListener();
						preference.setOnPreferenceChangeListener(null);
						preference.setChecked(true);
						preference.setOnPreferenceChangeListener(listener);
						FerrioApplication.refreshWidgets(requireContext());
						((FerrioApplication) requireActivity().getApplicationContext())
								.getAppRepository().refresh();
					})
					.create();

			dialog.setOnShowListener(d -> {
				final Button enableButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
				enableButton.setEnabled(false);
				dialogBinding.dialogAdultContentCheckboxAge.setOnCheckedChangeListener(
						(buttonView, isChecked) -> enableButton.setEnabled(isChecked));
			});

			dialog.show();
		}

		private void updateMonthViewModeSummary(@NonNull final Preference preference) {
			final PreferenceHelper prefs = new PreferenceHelper(requireContext());
			final String mode = prefs.getMonthViewMode();
			if (mode.equals(prefs.monthViewModeValueCompact())) {
				preference.setSummary(R.string.month_view_mode_compact);
			} else if (mode.equals(prefs.monthViewModeValueSimple())) {
				preference.setSummary(R.string.month_view_mode_simple);
			} else {
				preference.setSummary(R.string.month_view_mode_detailed);
			}
		}

		private void showMonthViewModeDialog(@NonNull final Preference preference) {
			final PreferenceHelper prefs = new PreferenceHelper(requireContext());
			final String currentMode = prefs.getMonthViewMode();

			final DialogMonthViewModeBinding dialogBinding = DialogMonthViewModeBinding.inflate(getLayoutInflater());

			if (currentMode.equals(prefs.monthViewModeValueCompact())) {
				dialogBinding.radioCompact.setChecked(true);
			} else if (currentMode.equals(prefs.monthViewModeValueSimple())) {
				dialogBinding.radioSimple.setChecked(true);
			} else {
				dialogBinding.radioDetailed.setChecked(true);
			}

			final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
					.setTitle(R.string.dialog_title_month_view_mode)
					.setView(dialogBinding.getRoot());

			final AlertDialog dialog = builder.show();

			final View.OnClickListener clickListener = v -> {
				final String selected;
				if (v.getId() == R.id.option_compact || v.getId() == R.id.radio_compact) {
					selected = prefs.monthViewModeValueCompact();
				} else if (v.getId() == R.id.option_simple || v.getId() == R.id.radio_simple) {
					selected = prefs.monthViewModeValueSimple();
				} else {
					selected = prefs.monthViewModeValueDetailed();
				}
				prefs.setMonthViewMode(selected);
				updateMonthViewModeSummary(preference);
				dialog.dismiss();
			};

			dialogBinding.optionDetailed.setOnClickListener(clickListener);
			dialogBinding.optionCompact.setOnClickListener(clickListener);
			dialogBinding.optionSimple.setOnClickListener(clickListener);
			dialogBinding.radioDetailed.setOnClickListener(clickListener);
			dialogBinding.radioCompact.setOnClickListener(clickListener);
			dialogBinding.radioSimple.setOnClickListener(clickListener);
		}
	}
}
