package eu.andret.kalendarzswiatnietypowych.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class HolidayDay implements Comparable<HolidayDay>, Parcelable {
	public static final Parcelable.Creator<HolidayDay> CREATOR = new Parcelable.Creator<HolidayDay>() {
		@Override
		public HolidayDay createFromParcel(final Parcel in) {
			final int monthRead = in.readInt();
			final int dayRead = in.readInt();
			final List<Holiday> holidaysRead = Arrays.stream(in.readParcelableArray(Holiday.class.getClassLoader()))
					.map(Holiday.class::cast)
					.collect(Collectors.toList());
			return new HolidayDay(monthRead, dayRead, holidaysRead);
		}

		@Override
		public HolidayDay[] newArray(final int size) {
			return new HolidayDay[size];
		}
	};

	int month;
	int day;
	List<Holiday> holidays;

	public HolidayDay(final int month, final int day) {
		this(month, day, new ArrayList<>());
	}

	public final long getSeed() {
		return Long.parseLong(String.format(Locale.ROOT, "%d%d", day, month));
	}

	public List<Holiday> getHolidaysList(final boolean includeUsual) {
		final List<Holiday> list = new ArrayList<>();
		for (final Holiday h : holidays) {
			if (!h.isUsual() || includeUsual) {
				list.add(h);
			}
		}
		return list;
	}

	public int countHolidays(final boolean includeUsual) {
		int counter = 0;
		for (final Holiday h : holidays) {
			if (!h.isUsual() || includeUsual) {
				counter++;
			}
		}
		return counter;
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
}