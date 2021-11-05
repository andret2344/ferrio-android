/*
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.util.Locale;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Data {
	public SharedPreferences getPreferences(final Context context, final Prefs type) {
		return context.getSharedPreferences(type.getName(), Context.MODE_PRIVATE);
	}

	public AppColorSet getColors(final boolean dark) {
		if (dark) {
			return new AppColorSet(true, MyColor.BLACK, MyColor.WHITE);
		}
		return new AppColorSet(false, MyColor.WHITE, MyColor.BLACK);
	}

	public static class AppColorSet {
		public final int background;
		public final int foreground;
		public final boolean dark;

		private AppColorSet(final boolean dark, final int background, final int foreground) {
			this.background = background;
			this.foreground = foreground;
			this.dark = dark;
		}
	}

	public enum Prefs {
		THEME,
		LANGUAGE;

		public String getName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	@UtilityClass
	public static class MyColor {
		public static final int WHITE = Color.rgb(238, 238, 238);
		public static final int BLACK = Color.rgb(33, 33, 33);
	}
}
