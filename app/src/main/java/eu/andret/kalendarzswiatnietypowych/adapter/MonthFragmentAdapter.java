package eu.andret.kalendarzswiatnietypowych.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.fragment.MonthFragment;

public class MonthFragmentAdapter extends FragmentStateAdapter {
	private final HolidayCalendar holidayCalendar;

	public MonthFragmentAdapter(@NonNull final FragmentManager fragmentManager, @NonNull final Lifecycle lifecycle, @NonNull final HolidayCalendar holidayCalendar) {
		super(fragmentManager, lifecycle);
		this.holidayCalendar = holidayCalendar;
	}

	@NonNull
	@Override
	public Fragment createFragment(final int position) {
		final MonthFragment fragment = new MonthFragment();
		final Bundle bundle = new Bundle();
		final int current = position + 1;
		bundle.putInt(MainActivity.MONTH, current);
		final LocalDate before = getBefore(current);
		final LocalDate after = getAfter(current, before);
		final List<HolidayDay> holidays = holidayCalendar.getHolidayDaysInDateRange(before, after, true);
		bundle.putParcelableArrayList(MainActivity.HOLIDAY_DAYS, new ArrayList<>(holidays));
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public int getItemCount() {
		return 12;
	}

	private LocalDate getBefore(final int month) {
		LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, 1);
		if (date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
			date = date.minusDays(1);
		}
		while (!date.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
			date = date.minusDays(1);
		}
		return date;
	}

	private LocalDate getLastDay(final int month) {
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, 1);
		return date.plusDays(date.lengthOfMonth()).minusDays(1);
	}

	private LocalDate getAfter(final int month, final LocalDate before) {
		LocalDate date = getLastDay(month);
		while (!date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
			date = date.plusDays(1);
		}
		date = date.plusDays(1);
		final long diffDays = before.until(date, ChronoUnit.DAYS);
		final long diffWeeks = diffDays / 7 + (diffDays % 7 == 0 ? 0 : 1);
		if (diffWeeks < 6) {
			date = date.plusDays(7 * (6 - diffWeeks));
		}
		return date;
	}
}
