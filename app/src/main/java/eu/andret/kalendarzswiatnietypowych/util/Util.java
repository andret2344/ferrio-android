package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.entity.ReportState;

public final class Util {
	public static final Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
			.registerTypeAdapter(ReportState.class, new ReportStateAdapter())
			.create();
	private static final List<String> LANGUAGE_CODES = List.of("pl");

	private static final DateTimeFormatter DATE_TIME_FORMATTER =
			DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

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
		return formatDate(pair, true);
	}

	@NonNull
	public static String getFormattedDate(@NonNull final Pair<Month, Integer> pair) {
		return formatDate(pair, false);
	}

	// Drives both getFormattedDate and getFormattedDateWithYear off java.time's own locale data,
	// so the pattern fed to ofPattern is guaranteed to use only letters that DateTimeFormatter
	// understands. The day field is replaced with a placeholder so that synthetic Feb 30 (and
	// Feb 29 in non-leap years) can be rendered without constructing an invalid LocalDate.
	@NonNull
	private static String formatDate(@NonNull final Pair<Month, Integer> pair,
			final boolean withYear) {
		final Locale locale = Locale.getDefault();
		final String longPattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(
				FormatStyle.LONG, null, IsoChronology.INSTANCE, locale);
		final String pattern = withYear ? longPattern : stripYearTokens(longPattern);
		final String dayPlaceholder = "DAY_PLACEHOLDER";
		final String patternWithPlaceholder = replaceDayFieldsOutsideQuotes(pattern, dayPlaceholder);
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(patternWithPlaceholder, locale);
		final LocalDate localDate = LocalDate.of(LocalDate.now().getYear(), pair.first, 1);
		return localDate.format(formatter).replace(dayPlaceholder, localizeDigits(pair.second, locale));
	}

	// Replaces runs of 'd' that are NOT inside a single-quoted literal with a quoted placeholder.
	// Locales like pt/es return patterns with literals such as `'de'` whose 'd' must be left alone.
	@NonNull
	static String replaceDayFieldsOutsideQuotes(@NonNull final String pattern,
			@NonNull final String placeholder) {
		final StringBuilder sb = new StringBuilder(pattern.length() + placeholder.length());
		boolean inQuote = false;
		boolean inDayRun = false;
		for (int i = 0; i < pattern.length(); i++) {
			final char c = pattern.charAt(i);
			if (c == '\'') {
				inQuote = !inQuote;
				sb.append(c);
				inDayRun = false;
			} else if (c == 'd' && !inQuote) {
				if (!inDayRun) {
					sb.append('\'').append(placeholder).append('\'');
					inDayRun = true;
				}
			} else {
				sb.append(c);
				inDayRun = false;
			}
		}
		return sb.toString();
	}

	// Removes year fields from a date pattern. For each year token: always drops the immediately
	// following separator (the year suffix in patterns like "y년 M월 d일" or ", " in "MMMM d, y");
	// also drops the immediately preceding separator if no non-year field remains afterwards
	// (handles trailing-year patterns and Polish-style "y 'r.'").
	@NonNull
	static String stripYearTokens(@NonNull final String pattern) {
		final List<String> tokens = new ArrayList<>();
		final List<Boolean> isField = new ArrayList<>();
		final StringBuilder current = new StringBuilder();
		boolean inField = false;
		char fieldLetter = 0;
		boolean inQuote = false;
		for (int i = 0; i < pattern.length(); i++) {
			final char c = pattern.charAt(i);
			final boolean isLetter = !inQuote && (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z');
			if (c == '\'') {
				if (inField) {
					tokens.add(current.toString());
					isField.add(true);
					current.setLength(0);
					inField = false;
				}
				current.append(c);
				inQuote = !inQuote;
			} else if (inQuote) {
				current.append(c);
			} else if (isLetter) {
				if (inField && c == fieldLetter) {
					current.append(c);
				} else {
					if (current.length() > 0) {
						tokens.add(current.toString());
						isField.add(inField);
						current.setLength(0);
					}
					inField = true;
					fieldLetter = c;
					current.append(c);
				}
			} else {
				if (inField) {
					tokens.add(current.toString());
					isField.add(true);
					current.setLength(0);
					inField = false;
				}
				current.append(c);
			}
		}
		if (current.length() > 0) {
			tokens.add(current.toString());
			isField.add(inField);
		}
		int i = 0;
		while (i < tokens.size()) {
			if (!isField.get(i) || !isYearLetter(tokens.get(i).charAt(0))) {
				i++;
				continue;
			}
			boolean nonYearFieldAfter = false;
			for (int j = i + 1; j < tokens.size(); j++) {
				if (isField.get(j) && !isYearLetter(tokens.get(j).charAt(0))) {
					nonYearFieldAfter = true;
					break;
				}
			}
			int removeStart = i;
			int removeEnd = i;
			if (i + 1 < tokens.size() && !isField.get(i + 1)) {
				removeEnd = i + 1;
			}
			if (!nonYearFieldAfter && i > 0 && !isField.get(i - 1)) {
				removeStart = i - 1;
			}
			for (int k = removeEnd; k >= removeStart; k--) {
				tokens.remove(k);
				isField.remove(k);
			}
			i = removeStart;
		}
		final StringBuilder out = new StringBuilder();
		for (final String t : tokens) {
			out.append(t);
		}
		return out.toString();
	}

	private static boolean isYearLetter(final char c) {
		return c == 'y' || c == 'Y' || c == 'u' || c == 'r' || c == 'U';
	}

	@NonNull
	private static String localizeDigits(final int value, @NonNull final Locale locale) {
		final char zero = DecimalStyle.of(locale).getZeroDigit();
		final String ascii = String.valueOf(value);
		if (zero == '0') {
			return ascii;
		}
		final StringBuilder sb = new StringBuilder(ascii.length());
		for (int i = 0; i < ascii.length(); i++) {
			sb.append((char) (zero + ascii.charAt(i) - '0'));
		}
		return sb.toString();
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
