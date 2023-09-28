package eu.andret.kalendarzswiatnietypowych.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class HolidayDay implements Comparable<HolidayDay>, Parcelable {
	private final int month;
	private final int day;
	@NonNull
	private final List<Holiday> holidays;

	public HolidayDay(final int month, final int day, @NonNull final List<Holiday> holidays) {
		this.month = month;
		this.day = day;
		this.holidays = holidays;
	}

	protected HolidayDay(final Parcel in) {
		month = in.readInt();
		day = in.readInt();
		holidays = Arrays.stream(in.readParcelableArray(Holiday.class.getClassLoader()))
				.map(Holiday.class::cast)
				.collect(Collectors.toList());
	}

	public HolidayDay(final int month, final int day) {
		this(month, day, new ArrayList<>());
	}

	public final long getSeed() {
		return Long.parseLong(String.format(Locale.ROOT, "%d%d", day, month));
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
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
	public boolean equals(final Object o) {
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel parcel, final int flags) {
		parcel.writeInt(month);
		parcel.writeInt(day);
		parcel.writeParcelableArray(holidays.toArray(new Holiday[0]), flags);
	}

	public static final Parcelable.Creator<HolidayDay> CREATOR = new Parcelable.Creator<>() {
		@NonNull
		@Override
		public HolidayDay createFromParcel(final Parcel in) {
			return new HolidayDay(in);
		}

		@NonNull
		@Override
		public HolidayDay[] newArray(final int size) {
			return new HolidayDay[size];
		}
	};
}
