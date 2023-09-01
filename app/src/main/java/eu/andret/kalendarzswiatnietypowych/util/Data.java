/*
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Objects;

import eu.andret.kalendarzswiatnietypowych.R;

public final class Data {
	public static ColorSet getColors(final Context context) {
		final int colorBlack = ContextCompat.getColor(context, R.color.color_black_accent);
		final int colorWhite = ContextCompat.getColor(context, R.color.color_white_accent);
		if (Util.isDarkTheme(context)) {
			return new ColorSet(true, colorBlack, colorWhite);
		}
		return new ColorSet(false, colorWhite, colorBlack);
	}

	private Data() {
	}

	public static class ColorSet {
		private final boolean darkTheme;
		private final int backgroundColor;
		private final int foregroundColor;

		public ColorSet(final boolean darkTheme, final int backgroundColor, final int foregroundColor) {
			this.darkTheme = darkTheme;
			this.backgroundColor = backgroundColor;
			this.foregroundColor = foregroundColor;
		}

		public boolean isDarkTheme() {
			return darkTheme;
		}

		public int getBackgroundColor() {
			return backgroundColor;
		}

		public int getForegroundColor() {
			return foregroundColor;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			final ColorSet colorSet = (ColorSet) o;
			return darkTheme == colorSet.darkTheme && backgroundColor == colorSet.backgroundColor && foregroundColor == colorSet.foregroundColor;
		}

		@Override
		public int hashCode() {
			return Objects.hash(darkTheme, backgroundColor, foregroundColor);
		}

		@NonNull
		@Override
		public String toString() {
			return "ColorSet{" +
					"darkTheme=" + darkTheme +
					", backgroundColor=" + backgroundColor +
					", foregroundColor=" + foregroundColor +
					'}';
		}
	}
}
