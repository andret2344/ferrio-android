/**
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.utils;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public final class Data {
	private Data() {}
	
	public static SharedPreferences getPreferences(Context context, Prefs type) {
		return context.getSharedPreferences(type.getName(), Context.MODE_PRIVATE);
	}
	
	public static AppColorSet getColors(int number) {
		switch (number) {
			case 0:
				return new AppColorSet(false, MyColor.WHITE, MyColor.BLACK);
			case 2:
				return new AppColorSet(false, MyColor.PINK, MyColor.BLACK);
			case 1:
			default:
				return new AppColorSet(true, MyColor.BLACK, MyColor.WHITE);
		}
	}
	
	public static class AppColorSet {
		public final int background, forground;
		public final boolean dark;
		
		private AppColorSet(boolean dark, int background, int forground) {
			this.background = background;
			this.forground = forground;
			this.dark = dark;
		}
	}
	
	public enum Prefs {
		THEME,
		FAVOURITES,
		TUTORIAL,
		LANGUAGE,
		SURVEY,
		DEFAULT;
		
		public String getName() {
			return name().toLowerCase(Locale.US);
		}
	}
	
	public static class MyColor {
		public static final int WHITE = Color.rgb(238, 238, 238);
		public static final int BLACK = Color.rgb(33, 33, 33);
		public static final int PINK = Color.rgb(238, 130, 238);
		// public static final int PINK = Color.rgb(255, 165, 103); //By Brokos
		public static final int GRAY_LIGHT = Color.rgb(200, 200, 200);
		public static final int GRAY_DARK = Color.rgb(100, 100, 100);
	}
}
