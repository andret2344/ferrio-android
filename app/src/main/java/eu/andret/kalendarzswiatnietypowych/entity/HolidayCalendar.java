package eu.andret.kalendarzswiatnietypowych.entity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidaysDBHelper;

public class HolidayCalendar {
	@SuppressLint("StaticFieldLeak")
	private static HolidayCalendar instance;

	private final HolidayMonth[] months = new HolidayMonth[12];
	private final Context context;

	private String language;

	public class HolidayMonth implements Comparable<HolidayMonth> {
		private final List<HolidayDay> days = new ArrayList<>();
		private final int month;

		public class HolidayDay implements Comparable<HolidayDay> {
			private final int day;
			private final List<Holiday> holidays;

			public class Holiday implements Comparable<Holiday> {
				private final int metadataId;
				private final String text;
				private final boolean usual;
				private final String externalLink;

				public Holiday(final int metadataId, final String text, final boolean usual, final String externalLink) {
					this.text = text;
					this.usual = usual;
					this.externalLink = externalLink;
					this.metadataId = metadataId;
					holidays.add(this);
				}

				public Holiday(final int hId, final String text, final boolean usual) {
					this(hId, text, usual, null);
				}

				public String getText() {
					return text;
				}

				public boolean isUsual() {
					return usual;
				}

				public String getExternalLink() {
					return externalLink;
				}

				public int getMetadataId() {
					return metadataId;
				}

				public HolidayDay getDay() {
					return HolidayDay.this;
				}

				public void report() {
					new AsyncTask<String, Void, String>() {
						@Override
						protected String doInBackground(final String... params) {
							try {
								final String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
								String data = "cmd=1";
								data += "&hid=" + metadataId;
								data += "&date=" + System.currentTimeMillis() / 1000;
								data += "&uuid=" + deviceId;
								data += "&language=" + language;
								final byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

								final HttpURLConnection conn = (HttpURLConnection) new URL("https://andret.eu/uhc/api/report.php").openConnection();
								conn.setRequestMethod("POST");
								conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
								conn.setRequestProperty("Content-Length", String.valueOf(dataBytes.length));
								conn.setDoOutput(true);
								conn.getOutputStream().write(dataBytes);
								conn.getOutputStream().close();
								final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
								final String result = reader.readLine();
								reader.close();
								return result;
							} catch (final IOException ex) {
								Log.getStackTraceString(ex);
							}
							return null;
						}
					}.execute();
				}

				@Override
				public int compareTo(@NonNull final Holiday o) {
					if (usual != o.usual) {
						return usual ? -1 : 1;
					}
					return text.compareTo(o.text);
				}

				@Override
				public boolean equals(final Object o) {
					if (this == o) {
						return true;
					}
					if (o == null || getClass() != o.getClass()) {
						return false;
					}
					final Holiday holiday = (Holiday) o;
					return usual == holiday.usual && text.equals(holiday.text);
				}

				@Override
				public int hashCode() {
					return text.hashCode() + (usual ? 1 : 0);
				}

				@Override
				public String toString() {
					return "Holiday [id=" + metadataId + ", text=\"" + text + "\", usual=" + usual + ", externalLink=\"" + externalLink + "\"]";
				}
			}

			public HolidayDay(final HolidayDay other) {
				this(other.day, other.holidays);
			}

			public HolidayDay(final int day, final List<Holiday> holidays) {
				this.day = day;
				this.holidays = holidays == null ? new ArrayList<>() : holidays;
				days.add(this);
				Collections.sort(this.holidays);
			}

			public int getDay() {
				return day;
			}

			public Holiday find(final String text) {
				for (final Holiday h : holidays) {
					if (h.text.equalsIgnoreCase(text)) {
						return h;
					}
				}
				return null;
			}

			public final long getSeed() {
				return Long.parseLong(String.valueOf(day).concat(String.valueOf(month)));
			}

			public String getDate() {
				String result = "";
				if (day < 10) {
					result += "0";
				}
				result += day + ".";
				if (month < 10) {
					result += "0";
				}
				result += month;
				return result;
			}

			public List<Holiday> getHolidays() {
				return holidays;
			}

			public boolean hasHolidays(final boolean includeUsual) {
				for (final Holiday h : holidays) {
					if (!h.usual || includeUsual) {
						return true;
					}
				}
				return false;
			}

			public List<Holiday> getHolidaysList(final boolean includeUsual) {
				final List<Holiday> list = new ArrayList<>();
				for (final Holiday h : holidays) {
					if (!h.usual || includeUsual) {
						list.add(h);
					}
				}
				return list;
			}

			public int countHolidays(final boolean includeUsual) {
				int counter = 0;
				for (final Holiday h : holidays) {
					if (!h.usual || includeUsual) {
						counter++;
					}
				}
				return counter;
			}

			public HolidayMonth getMonth() {
				return HolidayMonth.this;
			}

			@Override
			public int compareTo(@NonNull final HolidayDay another) {
				if (getMonth().getMonth() == another.getMonth().getMonth()) {
					return day - another.day;
				}
				return getMonth().compareTo(another.getMonth());
			}

			@Override
			public boolean equals(final Object o) {
				if (this == o) {
					return true;
				}
				if (o == null || getClass() != o.getClass()) {
					return false;
				}

				final HolidayDay that = (HolidayDay) o;

				return day == that.day && getMonth().equals(that.getMonth());
			}

			@Override
			public int hashCode() {
				return day;
			}

			@Override
			public String toString() {
				return "HolidayObject [day=" + day + ", holidays=" + holidays + "]";
			}
		}

		private HolidayMonth(final int month) {
			this.month = month;
		}

		public List<HolidayDay> getDays() {
			return days;
		}

		@NonNull
		public HolidayDay getDay(final int number) {
			for (final HolidayDay day : days) {
				if (day.day == number) { // FIXME
					return day;
				}
			}
			return new HolidayDay(number, new ArrayList<>());
		}

		public int getMonth() {
			return month;
		}

		@Override
		public int compareTo(@NonNull final HolidayMonth another) {
			return month - another.month;
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			final HolidayMonth that = (HolidayMonth) o;
			return month == that.month;
		}

		@Override
		public int hashCode() {
			return month;
		}

		void clear() {
			days.clear();
		}

		@Override
		public String toString() {
			return "HolidayMonth [month=" + month + ", days=" + days + "]";
		}
	}

	private HolidayCalendar(final Context context) {
		for (int i = 0; i < 12; i++) {
			months[i] = new HolidayMonth(i + 1);
		}
		this.context = context;
		final SharedPreferences prefs = Data.getPreferences(context, Data.Prefs.LANGUAGE);
		language = prefs.getString("selected", null);
		clear();
		HolidaysDBHelper.getInstance(context).reload(language);
	}

	/**
	 * @param month month number 1-12
	 * @return HolidayMonth object representing month
	 */
	public HolidayMonth getMonth(final int month) {
		return months[month - 1];
	}

	void clear() {
		for (final HolidayMonth month : months) {
			month.clear();
		}
	}

	public void refresh() {
		final SharedPreferences prefs = Data.getPreferences(context, Data.Prefs.LANGUAGE);
		language = prefs.getString("selected", null);
		HolidaysDBHelper.getInstance(context).reload(language);
	}

	public final HolidayDay getTodayHolidays() {
		final LocalDate date = LocalDate.now();
		return months[date.getMonthValue()].getDay(date.getDayOfMonth());
	}

	public final List<HolidayDay> getHolidayDaysInDateRange(final LocalDate begin, final LocalDate end, final boolean fillEmpties) {
		final List<HolidayDay> holidays = new ArrayList<>();
		for (LocalDate date = begin; date.until(end, ChronoUnit.DAYS) > 0; date = date.minusDays(1)) {
			final HolidayMonth hm = months[date.getMonthValue()];
			final HolidayDay hd = hm.getDay(date.getDayOfMonth());
			if (hd != null) {
				holidays.add(hd);
			} else if (fillEmpties) {
				holidays.add(hm.new HolidayDay(date.getDayOfMonth(), null));
			}
		}
		return holidays;
	}

	public static HolidayCalendar getNewInstance(final Context context) {
		instance = new HolidayCalendar(context);
		return instance;
	}

	public static HolidayCalendar getInstance(final Context context) {
		if (instance == null) {
			instance = new HolidayCalendar(context);
		}
		return instance;
	}

	public HolidayMonth[] getAllMonths() {
		return months;
	}

	public List<HolidayDay> getAllDays() {
		final List<HolidayDay> days = new ArrayList<>();
		for (final HolidayMonth hm : months) {
			days.addAll(hm.getDays());
		}
		return days;
	}

	public List<Holiday> getAllHolidays() {
		final List<Holiday> days = new ArrayList<>();
		for (final HolidayDay hd : getAllDays()) {
			days.addAll(hd.getHolidays());
		}
		return days;
	}

	@Override
	public String toString() {
		return "HolidayCalendar [language=" + language + ", months=" + Arrays.asList(months) + "]";
	}
}
