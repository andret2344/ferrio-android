package eu.andret.kalendarzswiatnietypowych.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

public final class Util {
	public static final Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.create();
	private static final Random RANDOM = new Random();
	public static final List<Integer> NETWORK_CAPABILITIES = List.of(
			NetworkCapabilities.TRANSPORT_WIFI,
			NetworkCapabilities.TRANSPORT_CELLULAR,
			NetworkCapabilities.TRANSPORT_ETHERNET);
	private static final List<String> LANGUAGE_CODES = List.of("pl");

	private Util() {
	}

	@NonNull
	public static Pair<Month, Integer> calculateDates(final int id) {
		final LocalDate now = LocalDate.now();
		final int year = now.getYear();
		if (id == 61) {
			return new Pair<>(Month.FEBRUARY, 30);
		}
		if (now.isLeapYear()) {
			if (id < 61) {
				final LocalDate date = LocalDate.ofYearDay(year, id);
				return new Pair<>(date.getMonth(), date.getDayOfMonth());
			}
			final LocalDate date = LocalDate.ofYearDay(year, id - 1);
			return new Pair<>(date.getMonth(), date.getDayOfMonth());
		}
		if (id < 60) {
			final LocalDate date = LocalDate.ofYearDay(year, id);
			return new Pair<>(date.getMonth(), date.getDayOfMonth());
		}
		if (id == 60) {
			return new Pair<>(Month.FEBRUARY, 29);
		}
		final LocalDate date = LocalDate.ofYearDay(year, id - 2);
		return new Pair<>(date.getMonth(), date.getDayOfMonth());
	}

	public static int randomizeColor(@NonNull final Context context, final long seed) {
		RANDOM.setSeed(seed);
		final boolean dark = isDarkTheme(context);
		return Color.rgb(randomize(dark), randomize(dark), randomize(dark));
	}

	private static int randomize(final boolean dark) {
		return RANDOM.nextInt(127) + (dark ? 0 : 127);
	}

	private static boolean isDarkTheme(@NonNull final Context context) {
		return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
				== Configuration.UI_MODE_NIGHT_YES;
	}

	public static boolean isNetworkAvailable(@NonNull final Context context) {
		final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return Optional.ofNullable(connectivityManager.getActiveNetwork())
				.map(connectivityManager::getNetworkCapabilities)
				.filter(capabilities -> NETWORK_CAPABILITIES.stream().anyMatch(capabilities::hasTransport))
				.isPresent();
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
	public static String getFormattedDate(@NonNull final Pair<Month, Integer> pair) {
		final LocalDate localDate = LocalDate.of(LocalDate.now().getYear(), pair.first, 19);
		return localDate.format(getDateTimeFormatter()).replace("19", String.valueOf(pair.second));
	}

	@NonNull
	public static DateTimeFormatter getDateTimeFormatter() {
		return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
				.withLocale(Locale.getDefault());
	}
}
