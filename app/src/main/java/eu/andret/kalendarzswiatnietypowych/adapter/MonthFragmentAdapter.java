package eu.andret.kalendarzswiatnietypowych.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.UnusualCalendar;
import eu.andret.kalendarzswiatnietypowych.fragment.MonthFragment;

public class MonthFragmentAdapter extends FragmentStateAdapter {
	private final List<HolidayDay> holidayDays;

	public MonthFragmentAdapter(@NonNull final FragmentManager fragmentManager, @NonNull final Lifecycle lifecycle, @NonNull final List<HolidayDay> holidayDays) {
		super(fragmentManager, lifecycle);
		this.holidayDays = holidayDays;
	}

	@NonNull
	@Override
	public Fragment createFragment(final int position) {
		final Bundle bundle = new Bundle();
		final int current = position + 1;
		bundle.putInt(MainActivity.MONTH, current);
		final LocalDate before = getBefore(current);
		final LocalDate after = getAfter(before);
		final List<HolidayDay> holidays = UnusualCalendar.getHolidayDaysInDateRange(holidayDays, before, after);
		final MonthFragment fragment = new MonthFragment(holidays);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public int getItemCount() {
		return 12;
	}

	@NonNull
	private LocalDate getBefore(final int month) {
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, 1);
		if (date.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
			return date;
		}
		return date.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
	}

	@NonNull
	private LocalDate getAfter(final LocalDate before) {
		return before.plusDays(42);
	}
}
