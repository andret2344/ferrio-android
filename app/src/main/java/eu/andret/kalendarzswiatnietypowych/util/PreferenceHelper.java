package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import eu.andret.kalendarzswiatnietypowych.R;

/**
 * Typed wrapper around the default {@link SharedPreferences}. Centralizes preference keys and
 * defaults so call sites stop repeating {@code getBoolean(getString(R.string.settings_key_...))}.
 * Keys themselves still live in {@code strings_preferences_keys.xml} because {@code preferences.xml}
 * binds to them via {@code @string/...}.
 */
public final class PreferenceHelper {
	@NonNull
	private final SharedPreferences prefs;
	@NonNull
	private final Resources res;

	public PreferenceHelper(@NonNull final Context context) {
		this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.res = context.getResources();
	}

	public boolean isThemeColorized() {
		return prefs.getBoolean(res.getString(R.string.settings_key_theme_colorized), false);
	}

	public boolean includeUsualHolidays() {
		return prefs.getBoolean(res.getString(R.string.settings_key_usual_holidays), false);
	}

	public boolean showAdultContent() {
		return prefs.getBoolean(res.getString(R.string.settings_key_show_adult_content), false);
	}

	public void setShowAdultContent(final boolean value) {
		prefs.edit()
				.putBoolean(res.getString(R.string.settings_key_show_adult_content), value)
				.apply();
	}

	public void setAdultContentConfirmedAt(@NonNull final String iso8601Timestamp) {
		prefs.edit()
				.putString(res.getString(R.string.settings_key_adult_content_confirmed_at), iso8601Timestamp)
				.apply();
	}

	@NonNull
	public String getMonthViewMode() {
		return prefs.getString(
				res.getString(R.string.settings_key_month_view_mode),
				res.getString(R.string.month_view_mode_value_compact));
	}

	public void setMonthViewMode(@NonNull final String value) {
		prefs.edit()
				.putString(res.getString(R.string.settings_key_month_view_mode), value)
				.apply();
	}

	@NonNull
	public String getAppTheme(@NonNull final String defaultValue) {
		return prefs.getString(res.getString(R.string.settings_key_app_theme), defaultValue);
	}

	@NonNull
	public String monthViewModeValueCompact() {
		return res.getString(R.string.month_view_mode_value_compact);
	}

	@NonNull
	public String monthViewModeValueSimple() {
		return res.getString(R.string.month_view_mode_value_simple);
	}

	@NonNull
	public String monthViewModeValueDetailed() {
		return res.getString(R.string.month_view_mode_value_detailed);
	}

	@NonNull
	public String appThemeKey() {
		return res.getString(R.string.settings_key_app_theme);
	}

	@NonNull
	public String usualHolidaysKey() {
		return res.getString(R.string.settings_key_usual_holidays);
	}

	@NonNull
	public String showAdultContentKey() {
		return res.getString(R.string.settings_key_show_adult_content);
	}

	@NonNull
	public String monthViewModeKey() {
		return res.getString(R.string.settings_key_month_view_mode);
	}

	@NonNull
	public String logoutKey() {
		return res.getString(R.string.settings_key_logout);
	}
}
