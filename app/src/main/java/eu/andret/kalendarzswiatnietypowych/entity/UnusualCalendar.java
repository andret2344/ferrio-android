package eu.andret.kalendarzswiatnietypowych.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "unusual_calendar")
public class UnusualCalendar {
	private final List<HolidayDay> fixed;
	private final List<FloatingHoliday> floating;

	public UnusualCalendar(@NonNull final List<HolidayDay> fixed, final List<FloatingHoliday> floating) {
		this.fixed = fixed;
		this.floating = floating;
	}

	@NonNull
	public List<HolidayDay> getFixed() {
		return fixed;
	}

	@NonNull
	public List<FloatingHoliday> getFloating() {
		return floating;
	}

	@NonNull
	public static List<HolidayDay> getHolidayDaysInDateRange(final List<HolidayDay> holidayDays, final LocalDate begin, final LocalDate end) {
		final List<HolidayDay> result = new ArrayList<>();
		for (LocalDate date = begin; date.until(end, ChronoUnit.DAYS) > 0; date = date.plusDays(1)) {
			result.add(getOrCreateDay(holidayDays, date.getMonthValue(), date.getDayOfMonth()));
		}
		if (end.getMonth().equals(Month.MARCH)) {
			result.add(getOrCreateDay(holidayDays, 2, 30));
		}
		return result;
	}

	@Nullable
	public static HolidayDay getDay(@NonNull final List<HolidayDay> holidayDays, final int month, final int day) {
		return holidayDays.stream()
				.filter(holidayDay -> holidayDay.getDay() == day)
				.filter(holidayDay -> holidayDay.getMonth() == month)
				.findAny()
				.orElse(null);
	}

	@NonNull
	public static HolidayDay getOrCreateDay(@NonNull final List<HolidayDay> holidayDays, final int month, final int day) {
		final HolidayDay holidayDay = getDay(holidayDays, month, day);
		if (holidayDay != null) {
			return holidayDay;
		}
		final HolidayDay toInsert = new HolidayDay(month, day);
		holidayDays.add(toInsert);
		return toInsert;
	}

	@Override
	public boolean equals(@Nullable final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final UnusualCalendar that = (UnusualCalendar) o;
		return Objects.equals(fixed, that.fixed) && Objects.equals(floating, that.floating);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fixed, floating);
	}

	@NonNull
	@Override
	public String toString() {
		return "UnusualCalendar{" +
				"fixed=" + fixed +
				", floating=" + floating +
				'}';
	}
}
