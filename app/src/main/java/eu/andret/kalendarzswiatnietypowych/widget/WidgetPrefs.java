package eu.andret.kalendarzswiatnietypowych.widget;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public final class WidgetPrefs {
	private static final String PREFS_NAME = "widget_prefs";
	private static final String KEY_PREFIX = "widget_";
	private static final String SUFFIX_DAYS_OFFSET = "_days_offset";
	private static final String SUFFIX_COLORIZED = "_colorized";
	private static final String SUFFIX_FONT_SIZE_OFFSET = "_font_size_offset";

	private WidgetPrefs() {
	}

	@NonNull
	private static SharedPreferences getPrefs(@NonNull final Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	@NonNull
	private static String key(final int appWidgetId, @NonNull final String suffix) {
		return KEY_PREFIX + appWidgetId + suffix;
	}

	public static int getDaysOffset(@NonNull final Context context, final int appWidgetId) {
		return getPrefs(context).getInt(key(appWidgetId, SUFFIX_DAYS_OFFSET), 0);
	}

	public static boolean isColorized(@NonNull final Context context, final int appWidgetId) {
		return getPrefs(context).getBoolean(key(appWidgetId, SUFFIX_COLORIZED), false);
	}

	public static int getFontSizeOffset(@NonNull final Context context, final int appWidgetId) {
		return getPrefs(context).getInt(key(appWidgetId, SUFFIX_FONT_SIZE_OFFSET), 0);
	}

	public static void save(@NonNull final Context context, final int appWidgetId,
			final int daysOffset, final boolean colorized, final int fontSizeOffset) {
		getPrefs(context).edit()
				.putInt(key(appWidgetId, SUFFIX_DAYS_OFFSET), daysOffset)
				.putBoolean(key(appWidgetId, SUFFIX_COLORIZED), colorized)
				.putInt(key(appWidgetId, SUFFIX_FONT_SIZE_OFFSET), fontSizeOffset)
				.apply();
	}

	public static void delete(@NonNull final Context context, final int appWidgetId) {
		getPrefs(context).edit()
				.remove(key(appWidgetId, SUFFIX_DAYS_OFFSET))
				.remove(key(appWidgetId, SUFFIX_COLORIZED))
				.remove(key(appWidgetId, SUFFIX_FONT_SIZE_OFFSET))
				.apply();
	}
}
