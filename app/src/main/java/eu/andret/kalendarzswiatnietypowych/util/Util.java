package eu.andret.kalendarzswiatnietypowych.util;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.Month;
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.R;

public final class Util {
	private static final Random RANDOM = new Random();

	public static class MonthDayPair {
		private final Month month;
		private final int day;

		public MonthDayPair(final Month month, final int day) {
			this.month = month;
			this.day = day;
		}

		public Month getMonth() {
			return month;
		}

		public int getDay() {
			return day;
		}
	}

	private Util() {
	}

	public static void createAlertWithImage(final Context context, final int img, final int title, final int text) {
		final Builder alert = new Builder(context);
		alert.setTitle(title);
		final LinearLayout layout = new LinearLayout(context);
		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 30, 0, 0);
		layout.setLayoutParams(params);
		layout.setOrientation(LinearLayout.VERTICAL);
		final ImageView image = new ImageView(context);
		image.setImageResource(img);
		layout.addView(image);
		final TextView tv = new TextView(context);
		tv.setText(text);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.drawer_list_name_text));
		layout.addView(tv);
		final LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(30, 20, 30, 20);
		tv.setLayoutParams(llp);
		alert.setView(layout);
		alert.setPositiveButton(R.string.ok, null);
		alert.show();
	}

	@NonNull
	public static MonthDayPair calculateDates(final int id) {
		final LocalDate now = LocalDate.now();
		if (now.isLeapYear()) {
			if (id < 60) {
				final LocalDate date = LocalDate.ofYearDay(now.getYear(), id);
				return new MonthDayPair(date.getMonth(), date.getDayOfMonth());
			}
			if (id == 60) {
				return new MonthDayPair(Month.FEBRUARY, 30);
			}
			final LocalDate date = LocalDate.ofYearDay(now.getYear(), id - 1);
			return new MonthDayPair(date.getMonth(), date.getDayOfMonth());
		}
		if (id < 60) {
			final LocalDate date = LocalDate.ofYearDay(now.getYear(), id);
			return new MonthDayPair(date.getMonth(), date.getDayOfMonth());
		}
		if (id == 60) {
			return new MonthDayPair(Month.FEBRUARY, 29);
		}
		if (id == 61) {
			return new MonthDayPair(Month.FEBRUARY, 30);
		}
		final LocalDate date = LocalDate.ofYearDay(now.getYear(), id - 2);
		return new MonthDayPair(date.getMonth(), date.getDayOfMonth());
	}

	public static int randomizeColor(final boolean dark, final long seed) {
		RANDOM.setSeed(seed);
		return Color.rgb(randomize(dark), randomize(dark), randomize(dark));
	}

	public static int randomize(final boolean dark) {
		return RANDOM.nextInt(127) + (dark ? 0 : 127);
	}

	public static boolean isDarkTheme(final Context context) {
		return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
				== Configuration.UI_MODE_NIGHT_YES;
	}
}
