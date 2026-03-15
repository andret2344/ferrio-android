package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HolidayDay implements Comparable<HolidayDay> {
	private final int month;
	private final int day;
	@NonNull
	private final List<Holiday> holidays;

	public HolidayDay(final int month, final int day, @NonNull final List<Holiday> holidays) {
		this.month = month;
		this.day = day;
		this.holidays = holidays;
	}

	public HolidayDay(final int month, final int day) {
		this(month, day, new ArrayList<>());
	}

	public final long getSeed() {
		return day * 100L + month;
	}

	@NonNull
	public String getId() {
		return String.format(Locale.ROOT, "%d%d", month, day);
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
		if (includeUsual) {
			return holidays;
		}
		final List<Holiday> filtered = new ArrayList<>();
		for (final Holiday holiday : holidays) {
			if (!holiday.isUsual()) {
				filtered.add(holiday);
			}
		}
		return filtered;
	}

	public void addHoliday(@NonNull final Holiday holiday) {
		holidays.add(holiday);
	}

	public int countHolidays(final boolean includeUsual) {
		if (includeUsual) {
			return holidays.size();
		}
		int count = 0;
		for (final Holiday holiday : holidays) {
			if (!holiday.isUsual()) {
				count++;
			}
		}
		return count;
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
