package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.gson.FieldNamingPolicy;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.entity.ReportState;

public final class Util {
	public static final Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
			.create();
	private static final List<String> LANGUAGE_CODES = List.of("pl");

	private static final DateTimeFormatter DATE_TIME_FORMATTER =
			DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

	private static final String MONTH_DAY_SKELETON = "MMMMd";
	private static final String YEAR_MONTH_DAY_SKELETON = "yMMMMd";

	private static final int FEB_29_NON_LEAP_INDEX = 60;
	private static final int FEB_30_INDEX = 61;

	private Util() {
	}

	public static long calculateSeed(final int day, final int month) {
		return day * 100L + month;
	}

	@NonNull
	public static Pair<Month, Integer> calculateDates(final int id) {
		final LocalDate now = LocalDate.now();
		final int year = now.getYear();
		if (id == FEB_30_INDEX) {
			return new Pair<>(Month.FEBRUARY, 30);
		}
		if (now.isLeapYear()) {
			if (id < FEB_30_INDEX) {
				final LocalDate date = LocalDate.ofYearDay(year, id);
				return new Pair<>(date.getMonth(), date.getDayOfMonth());
			}
			final LocalDate date = LocalDate.ofYearDay(year, id - 1);
			return new Pair<>(date.getMonth(), date.getDayOfMonth());
		}
		if (id < FEB_29_NON_LEAP_INDEX) {
			final LocalDate date = LocalDate.ofYearDay(year, id);
			return new Pair<>(date.getMonth(), date.getDayOfMonth());
		}
		if (id == FEB_29_NON_LEAP_INDEX) {
			return new Pair<>(Month.FEBRUARY, 29);
		}
		final LocalDate date = LocalDate.ofYearDay(year, id - 2);
		return new Pair<>(date.getMonth(), date.getDayOfMonth());
	}

	public static int calculateIndex(final int month, final int day) {
		if (month == Month.FEBRUARY.getValue() && day == 30) {
			return FEB_30_INDEX - 1;
		}
		if (month == Month.FEBRUARY.getValue() && day == 29) {
			return FEB_29_NON_LEAP_INDEX - 1;
		}
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
		final boolean leap = date.isLeapYear();
		int id = date.getDayOfYear();
		if (id > (leap ? 60 : 59)) {
			id += leap ? 1 : 2;
		}
		return id - 1;
	}

	private static final Random COLOR_RANDOM = new Random();

	public static int randomizeColor(@NonNull final Context context, final long seed) {
		COLOR_RANDOM.setSeed(seed);
		final boolean dark = isDarkTheme(context);
		return Color.rgb(randomize(dark), randomize(dark), randomize(dark));
	}

	private static int randomize(final boolean dark) {
		return COLOR_RANDOM.nextInt(127) + (dark ? 0 : 127);
	}

	private static boolean isDarkTheme(@NonNull final Context context) {
		return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
				== Configuration.UI_MODE_NIGHT_YES;
	}

	@NonNull
	public static String getLanguageCode() {
		final String language = Locale.getDefault().getLanguage();
		if (LANGUAGE_CODES.contains(language)) {
			return language;
		}
		return "en";
	}

	@NonNull
	public static String getFormattedDateWithYear(@NonNull final Pair<Month, Integer> pair) {
		return formatDate(pair, YEAR_MONTH_DAY_SKELETON);
	}

	@NonNull
	public static String getFormattedDate(@NonNull final Pair<Month, Integer> pair) {
		return formatDate(pair, MONTH_DAY_SKELETON);
	}

	@NonNull
	private static String formatDate(@NonNull final Pair<Month, Integer> pair, @NonNull final String skeleton) {
		final Locale locale = Locale.getDefault();
		final String pattern = DateFormat.getBestDateTimePattern(locale, skeleton);
		final String dayPlaceholder = "DAY_PLACEHOLDER";
		final String patternWithPlaceholder = pattern.replaceAll("d+", "'" + dayPlaceholder + "'");
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(patternWithPlaceholder, locale);
		final LocalDate localDate = LocalDate.of(LocalDate.now().getYear(), pair.first, 1);
		return localDate.format(formatter).replace(dayPlaceholder, String.valueOf(pair.second));
	}

	public static void applyStatusBadge(@NonNull final TextView textView,
			@NonNull final ReportState state) {
		@ColorInt final int color = ContextCompat.getColor(textView.getContext(), state.getColorResId());
		textView.setText(state.getLabelResId());
		textView.setTextColor(color);
		final float cornerRadius = 6 * textView.getContext().getResources().getDisplayMetrics().density;
		final MaterialShapeDrawable background = new MaterialShapeDrawable(
				ShapeAppearanceModel.builder().setAllCornerSizes(cornerRadius).build());
		background.setTint(Color.argb(31, Color.red(color), Color.green(color), Color.blue(color)));
		textView.setBackground(background);
	}

	@Nullable
	public static String getCountryFlag(@NonNull final String countryCode) {
		final Emoji emoji = EmojiManager.getForAlias(countryCode.toLowerCase(Locale.ROOT));
		return emoji != null ? emoji.getUnicode() : null;
	}

	@ColorInt
	public static int getThemeColor(@NonNull final Context context, final int attr) {
		final TypedArray a = context.obtainStyledAttributes(new int[]{attr});
		final int color = a.getColor(0, Color.GRAY);
		a.recycle();
		return color;
	}

	@NonNull
	public static DateTimeFormatter getDateTimeFormatter() {
		return DATE_TIME_FORMATTER.withLocale(Locale.getDefault());
	}

	@NonNull
	public static String sha256(@NonNull final String input) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			final StringBuilder hexString = new StringBuilder();
			for (final byte b : hash) {
				final String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (final NoSuchAlgorithmException ex) {
			throw new RuntimeException(ex);
		}
	}
}
