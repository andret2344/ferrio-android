/**
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;

import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;

public class HolidayCalendar {
	@SuppressLint("StaticFieldLeak")
	private static HolidayCalendar instance;
	
	private final HolidayMonth[] months = new HolidayMonth[12];
	private final Context context;
	
	private int language = -1;
	
	public class HolidayMonth implements Comparable<HolidayMonth> {
		private final List<HolidayDay> days = new ArrayList<>();
		private final int month;
		
		public class HolidayDay implements Comparable<HolidayDay>, Cloneable {
			private final int day;
			private final List<Holiday> holidays;
			
			public class Holiday implements Comparable<Holiday> {
				private final int metadataId;
				private final String text;
				private final boolean usual;
				private final String externalLink;
				
				public Holiday(int metadataId, String text, boolean usual, String externalLink) {
					this.text = text;
					this.usual = usual;
					this.externalLink = externalLink;
					this.metadataId = metadataId;
					holidays.add(this);
				}
				
				public Holiday(int hId, String text, boolean usual) {
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
					new AsyncTask<String, Void, Void>() {
						@Override
						protected Void doInBackground(String... params) {
							try {
								String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
								String data = "cmd=1";
								data += "&hid=" + metadataId;
								data += "&date=" + String.valueOf(System.currentTimeMillis() / 1000);
								data += "&uuid=" + deviceId;
								data += "&language=" + language;
								byte[] dataBytes = data.getBytes("UTF-8");
								
								HttpURLConnection conn = (HttpURLConnection) new URL("https://andret.eu/uhc/api/report.php").openConnection();
								conn.setRequestMethod("POST");
								conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
								conn.setRequestProperty("Content-Length", String.valueOf(dataBytes.length));
								conn.setDoOutput(true);
								conn.getOutputStream().write(dataBytes);
								conn.getOutputStream().close();
								BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
								String result = reader.readLine();
								result.length();
								reader.close();
							} catch (IOException ex) {
								ex.printStackTrace();
							}
							return null;
						}
					}.execute();
				}
				
				@Override
				public int compareTo(@NonNull Holiday o) {
					if (usual != o.usual) {
						return usual ? -1 : 1;
					}
					return text.compareTo(o.text);
				}
				
				@Override
				public String toString() {
					return "Holiday [id=" + metadataId + ", text=\"" + text + "\", usual=" + usual + ", externalLink=\"" + externalLink + "\"]";
				}
			}
			
			public HolidayDay(int day, List<Holiday> holidays) {
				this(day, holidays, true);
				days.add(this);
			}
			
			private HolidayDay(int day, List<Holiday> holidays, boolean diff) {
				this.day = day;
				if (holidays == null) {
					holidays = new ArrayList<>();
				}
				this.holidays = holidays;
				Collections.sort(holidays);
			}
			
			public int getDay() {
				return day;
			}
			
			public Holiday find(String text) {
				for (Holiday h : holidays) {
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
			
			public boolean hasHolidays(boolean includeUsual) {
				for (Holiday h : holidays) {
					if (!h.usual || includeUsual) {
						return true;
					}
				}
				return false;
			}
			
			public List<Holiday> getHolidaysList(boolean includeUsual) {
				List<Holiday> list = new ArrayList<>();
				for (Holiday h : holidays) {
					if (!h.usual || includeUsual) {
						list.add(h);
					}
				}
				return list;
			}
			
			public int countHolidays(boolean includeUsual) {
				int counter = 0;
				for (Holiday h : holidays) {
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
			public int compareTo(@NonNull HolidayDay another) {
				if (getMonth().getMonth() == another.getMonth().getMonth()) {
					return day - another.day;
				}
				return getMonth().compareTo(another.getMonth());
			}
			
			@Override
			public Object clone() throws CloneNotSupportedException {
				super.clone();
				return new HolidayDay(day, holidays, true);
			}
			
			@Override
			public String toString() {
				return "HolidayObject [day=" + day + ", holidays=" + holidays + "]";
			}
		}
		
		private HolidayMonth(int month) {
			this.month = month;
		}
		
		public List<HolidayDay> getDays() {
			return days;
		}
		
		public HolidayDay getDay(int number) {
			for (HolidayDay day : days) {
				if (day.day == number) { // XXX
					return day;
				}
			}
			return new HolidayDay(number, new ArrayList<Holiday>());
		}
		
		public int getMonth() {
			return month;
		}
		
		@Override
		public int compareTo(@NonNull HolidayMonth another) {
			return month - another.month;
		}
		
		void clear() {
			days.clear();
		}
		
		@Override
		public String toString() {
			return "HolidayMonth [month=" + month + ", days=" + days + "]";
		}
	}
	
	{
		for (int i = 0; i < 12; i++) {
			months[i] = new HolidayMonth(i + 1);
		}
		instance = this;
	}
	
	private HolidayCalendar(Context context) {
		this.context = context;
		SharedPreferences prefs = Data.getPreferences(context, Data.Prefs.LANGUAGE);
		HolidaysDBHelper.getInstance(context).reload(language = prefs.getInt("selected", -1));
	}
	
	/**
	 * @param month month number 1-12
	 * @return HolidayMonth object representing month
	 */
	public HolidayMonth getMonth(int month) {
		return months[month - 1];
	}
	
	void clear() {
		for (HolidayMonth month : months) {
			month.clear();
		}
	}
	
	public void refresh() {
		SharedPreferences prefs = Data.getPreferences(context, Data.Prefs.LANGUAGE);
		HolidaysDBHelper.getInstance(context).reload(language = prefs.getInt("selected", -1));
	}
	
	public final HolidayDay getTodayHolidays() {
		Calendar c = Calendar.getInstance();
		return months[c.get(Calendar.MONTH)].getDay(c.get(Calendar.DAY_OF_MONTH));
	}
	
	public final List<HolidayDay> getHolidayDaysInDateRange(Calendar begin, Calendar end, boolean fillEmptys) {
		List<HolidayDay> holidays = new ArrayList<>();
		for (Calendar date = (Calendar) begin.clone(); date.before(end); date.add(Calendar.DATE, 1)) {
			HolidayMonth hm = months[date.get(Calendar.MONTH)];
			HolidayDay hd = hm.getDay(date.get(Calendar.DAY_OF_MONTH));
			if (hd != null) {
				holidays.add(hd);
			} else if (fillEmptys) {
				holidays.add(hm.new HolidayDay(date.get(Calendar.DAY_OF_MONTH), null, false));
			}
		}
		return holidays;
	}

	public static HolidayCalendar reloadInstance(Context context) {
		return instance = new HolidayCalendar(context);
	}
	
	public static HolidayCalendar getInstance(Context context) {
		if (instance == null) {
			instance = new HolidayCalendar(context);
		}
		return instance;
	}
	
	public HolidayMonth[] getAllMonths() {
		return months;
	}
	
	public List<HolidayDay> getAllDays() {
		List<HolidayDay> days = new ArrayList<>();
		for (HolidayMonth hm : months) {
			days.addAll(hm.getDays());
		}
		return days;
	}
	
	public List<Holiday> getAllHolidays() {
		List<Holiday> days = new ArrayList<>();
		for (HolidayDay hd : getAllDays()) {
			days.addAll(hd.getHolidays());
		}
		return days;
	}
	
	@Override
	public String toString() {
		return "HolidayCalendar [language=" + language + ", months=" + Arrays.asList(months) + "]";
	}
}
