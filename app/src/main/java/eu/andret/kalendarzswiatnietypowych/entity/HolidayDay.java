package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.persistance.Converters;

@Entity(tableName = "holiday_day")
public class HolidayDay implements Comparable<HolidayDay> {
	@PrimaryKey
	@NonNull
	private final String id;
	private final int month;
	private final int day;
	@NonNull
	@TypeConverters(Converters.class)
	private final List<Holiday> holidays;

	public HolidayDay(@NonNull final String id, final int month, final int day, @NonNull final List<Holiday> holidays) {
		this.id = id;
		this.month = month;
		this.day = day;
		this.holidays = holidays;
	}

	public HolidayDay(@NonNull final HolidayDay holidayDay) {
		this(holidayDay.id, holidayDay.month, holidayDay.day, holidayDay.holidays);
	}

	@Ignore
	public HolidayDay(final int month, final int day, @NonNull final List<Holiday> holidays) {
		this(String.format(Locale.ROOT, "%d%d", month, day), month, day, holidays);
	}

	@Ignore
	public HolidayDay(final int month, final int day) {
		this(month, day, new ArrayList<>());
	}

	public final long getSeed() {
		return Long.parseLong(String.format(Locale.ROOT, "%d%d", day, month));
	}

	@NonNull
	public String getId() {
		return id;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}

	@NonNull
	public List<Holiday> getHolidays() {
		return holidays;
	}

	@NonNull
	public List<Holiday> getHolidaysList(final boolean includeUsual) {
		return holidays.stream()
				.filter(holiday -> !holiday.isUsual() || includeUsual)
				.collect(Collectors.toList());
	}

	public void addHoliday(@NonNull final Holiday holiday) {
		holidays.add(holiday);
	}

	public long countHolidays(final boolean includeUsual) {
		return holidays.stream()
				.filter(holiday -> !holiday.isUsual() || includeUsual)
				.count();
	}

	@Override
	public boolean equals(@Nullable final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final HolidayDay that = (HolidayDay) o;
		return month == that.month && day == that.day && holidays.equals(that.holidays);
	}

	@NonNull
	@Override
	public String toString() {
		return "HolidayDay{" +
				"month=" + month +
				", day=" + day +
				", holidays=" + holidays +
				'}';
	}

	@Override
	public int hashCode() {
		return Objects.hash(month, day, holidays);
	}

	@Override
	public int compareTo(@NonNull final HolidayDay another) {
		if (month == another.month) {
			return day - another.day;
		}
		return month - another.month;
	}
}
