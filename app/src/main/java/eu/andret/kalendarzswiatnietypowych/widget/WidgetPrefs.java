package eu.andret.kalendarzswiatnietypowych.widget;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.Locale;

public final class WidgetPrefs {
	private static final String PREFS_NAME = "widget_prefs";
	private static final String KEY_DAYS_OFFSET = "widget_%d_days_offset";
	private static final String KEY_COLORIZED = "widget_%d_colorized";
	private static final String KEY_FONT_SIZE = "widget_%d_font_size";

	private WidgetPrefs() {
	}

	@NonNull
	private static SharedPreferences getPrefs(@NonNull final Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	public static int getDaysOffset(@NonNull final Context context, final int appWidgetId) {
		return getPrefs(context).getInt(String.format(Locale.ROOT, KEY_DAYS_OFFSET, appWidgetId), 0);
	}

	public static boolean isColorized(@NonNull final Context context, final int appWidgetId) {
		return getPrefs(context).getBoolean(String.format(Locale.ROOT, KEY_COLORIZED, appWidgetId), false);
	}

	public static int getFontSize(@NonNull final Context context, final int appWidgetId) {
		return getPrefs(context).getInt(String.format(Locale.ROOT, KEY_FONT_SIZE, appWidgetId), 0);
	}

	public static void save(@NonNull final Context context, final int appWidgetId,
			final int daysOffset, final boolean colorized, final int fontSize) {
		getPrefs(context).edit()
				.putInt(String.format(Locale.ROOT, KEY_DAYS_OFFSET, appWidgetId), daysOffset)
				.putBoolean(String.format(Locale.ROOT, KEY_COLORIZED, appWidgetId), colorized)
				.putInt(String.format(Locale.ROOT, KEY_FONT_SIZE, appWidgetId), fontSize)
				.commit();
	}

	public static void delete(@NonNull final Context context, final int appWidgetId) {
		getPrefs(context).edit()
				.remove(String.format(Locale.ROOT, KEY_DAYS_OFFSET, appWidgetId))
				.remove(String.format(Locale.ROOT, KEY_COLORIZED, appWidgetId))
				.remove(String.format(Locale.ROOT, KEY_FONT_SIZE, appWidgetId))
				.apply();
	}
}
