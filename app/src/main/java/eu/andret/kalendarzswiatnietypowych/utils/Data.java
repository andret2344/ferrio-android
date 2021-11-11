/*
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Data {
	public SharedPreferences getPreferences(final Context context, final PreferenceType type) {
		return context.getSharedPreferences(type.getName(), Context.MODE_PRIVATE);
	}

	public AppColorSet getColors(final Context context) {
		final int colorBlack = context.getResources().getColor(R.color.color_black_accent);
		final int colorWhite = context.getResources().getColor(R.color.color_white_accent);
		if (Util.isDarkTheme(context)) {
			return new AppColorSet(true, colorBlack, colorWhite);
		}
		return new AppColorSet(false, colorWhite, colorBlack);
	}

	@Value
	public static class AppColorSet {
		boolean darkTheme;
		int backgroundColor;
		int foregroundColor;
	}

	public enum PreferenceType {
		THEME,
		LANGUAGE;

		public String getName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}
}
