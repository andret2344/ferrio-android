package eu.andret.kalendarzswiatnietypowych.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Value;

@Value
public class HolidayCalendar implements Parcelable {
	public static final Parcelable.Creator<HolidayCalendar> CREATOR = new Parcelable.Creator<HolidayCalendar>() {
		@Override
		public HolidayCalendar createFromParcel(final Parcel in) {
			final List<HolidayDay> holidaysDaysRead = Arrays.stream(in.readParcelableArray(HolidayDay.class.getClassLoader()))
					.map(HolidayDay.class::cast)
					.collect(Collectors.toList());
			final HolidayCalendar holidayCalendar = new HolidayCalendar();
			holidayCalendar.holidayDays.addAll(holidaysDaysRead);
			return holidayCalendar;
		}

		@Override
		public HolidayCalendar[] newArray(final int size) {
			return new HolidayCalendar[size];
		}
	};

	List<HolidayDay> holidayDays = new ArrayList<>();

	public final HolidayDay getTodayHolidays() {
		final LocalDate now = LocalDate.now();
		return getDay(now.getDayOfMonth(), now.getMonthValue());
	}

	@NonNull
	public final HolidayDay getDay(final int month, final int day) {
		return holidayDays.stream()
				.filter(holidayDay -> holidayDay.getDay() == day)
				.filter(holidayDay -> holidayDay.getMonth() == month)
				.findAny()
				.orElse(new HolidayDay(month, day));
	}

	public final HolidayDay getOrCreateDay(final int month, final int day) {
		final HolidayDay holidayDay = getDay(month, day);
		if (holidayDay != null) {
			return holidayDay;
		}
		final HolidayDay toInsert = new HolidayDay(month, day);
		holidayDays.add(toInsert);
		return toInsert;
	}

	public final List<HolidayDay> getHolidayDaysInDateRange(final LocalDate begin, final LocalDate end) {
		final List<HolidayDay> holidayDays = new ArrayList<>();
		for (LocalDate date = begin; date.until(end, ChronoUnit.DAYS) > 0; date = date.plusDays(1)) {
			holidayDays.add(getDay(date.getMonthValue(), date.getDayOfMonth()));
		}
		return holidayDays;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel parcel, final int flags) {
		parcel.writeParcelableArray(holidayDays.toArray(new HolidayDay[0]), flags);
	}
}
