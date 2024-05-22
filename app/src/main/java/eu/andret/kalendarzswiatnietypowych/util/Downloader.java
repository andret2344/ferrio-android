package eu.andret.kalendarzswiatnietypowych.util;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.MissingReport;
import eu.andret.kalendarzswiatnietypowych.entity.UnusualCalendar;
import java9.util.function.Supplier;

public final class Downloader {
	private Downloader() {
	}

	public static class UnusualCalendarDownloader implements Supplier<UnusualCalendar> {
		@Nullable
		@Override
		public UnusualCalendar get() {
			try {
				final String href = String.format(Locale.ROOT, "https://api.unusualcalendar.net/v2/holiday/%s", Util.getLanguageCode());
				final HttpsURLConnection con = (HttpsURLConnection) new URL(href).openConnection();
				return Util.GSON.fromJson(new InputStreamReader(con.getInputStream()), UnusualCalendar.class);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
			return null;
		}
	}

	public static class HolidayDayDownloader implements Supplier<HolidayDay> {
		private final int month;
		private final int day;

		public HolidayDayDownloader(final int month, final int day) {
			this.month = month;
			this.day = day;
		}

		@Nullable
		@Override
		public HolidayDay get() {
			try {
				final String href = String.format(Locale.ROOT, "https://api.unusualcalendar.net/v2/holiday/%s/day/%d/%d", Util.getLanguageCode(), month, day);
				final HttpsURLConnection con = (HttpsURLConnection) new URL(href).openConnection();
				return Util.GSON.fromJson(new InputStreamReader(con.getInputStream()), HolidayDay.class);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
			return null;
		}
	}

	public static class ReportsDownloader implements Supplier<MissingReport> {
		private final String userId;

		public ReportsDownloader(final String userId) {
			this.userId = userId;
		}

		@Nullable
		@Override
		public MissingReport get() {
			try {
				final String href = String.format(Locale.ROOT, "https://api.unusualcalendar.net/v2/missing/%s", userId);
				final HttpsURLConnection con = (HttpsURLConnection) new URL(href).openConnection();
				return Util.GSON.fromJson(new InputStreamReader(con.getInputStream()), MissingReport.class);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
			return null;
		}
	}
}
