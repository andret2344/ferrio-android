/*
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;

import eu.andret.kalendarzswiatnietypowych.R;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Data {
	public ColorSet getColors(final Context context) {
		final int colorBlack = context.getColor(R.color.color_black_accent);
		final int colorWhite = context.getColor(R.color.color_white_accent);
		if (Util.isDarkTheme(context)) {
			return new ColorSet(true, colorBlack, colorWhite);
		}
		return new ColorSet(false, colorWhite, colorBlack);
	}

	@Value
	public static class ColorSet {
		boolean darkTheme;
		int backgroundColor;
		int foregroundColor;
	}
}
