package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import java.time.LocalDate;
import java.time.Month;
import java.util.Random;

public final class Util {
	private static final Random RANDOM = new Random();

	private Util() {
	}

	@NonNull
	public static Pair<Month, Integer> calculateDates(final int id) {
		final LocalDate now = LocalDate.now();
		if (now.isLeapYear()) {
			if (id < 60) {
				final LocalDate date = LocalDate.ofYearDay(now.getYear(), id);
				return new Pair<>(date.getMonth(), date.getDayOfMonth());
			}
			if (id == 60) {
				return new Pair<>(Month.FEBRUARY, 30);
			}
			final LocalDate date = LocalDate.ofYearDay(now.getYear(), id - 1);
			return new Pair<>(date.getMonth(), date.getDayOfMonth());
		}
		if (id < 60) {
			final LocalDate date = LocalDate.ofYearDay(now.getYear(), id);
			return new Pair<>(date.getMonth(), date.getDayOfMonth());
		}
		if (id == 60) {
			return new Pair<>(Month.FEBRUARY, 29);
		}
		if (id == 61) {
			return new Pair<>(Month.FEBRUARY, 30);
		}
		final LocalDate date = LocalDate.ofYearDay(now.getYear(), id - 2);
		return new Pair<>(date.getMonth(), date.getDayOfMonth());
	}

	public static int randomizeColor(final Context context, final long seed) {
		RANDOM.setSeed(seed);
		final boolean dark = isDarkTheme(context);
		return Color.rgb(randomize(dark), randomize(dark), randomize(dark));
	}

	private static int randomize(final boolean dark) {
		return RANDOM.nextInt(127) + (dark ? 0 : 127);
	}

	static boolean isDarkTheme(final Context context) {
		return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
				== Configuration.UI_MODE_NIGHT_YES;
	}
}
