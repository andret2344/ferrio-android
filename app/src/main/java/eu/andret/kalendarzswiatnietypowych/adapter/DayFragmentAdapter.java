package eu.andret.kalendarzswiatnietypowych.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.time.Month;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.UnusualCalendar;
import eu.andret.kalendarzswiatnietypowych.fragment.DayFragment;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class DayFragmentAdapter extends FragmentStateAdapter {
	private final List<HolidayDay> holidayDays;

	public DayFragmentAdapter(@NonNull final FragmentManager fragmentManager, @NonNull final Lifecycle lifecycle, final List<HolidayDay> holidayDays) {
		super(fragmentManager, lifecycle);
		this.holidayDays = holidayDays;
	}

	@NonNull
	@Override
	public Fragment createFragment(final int position) {
		final DayFragment dayFragment = new DayFragment();
		final Bundle bundle = new Bundle();
		final Pair<Month, Integer> date = Util.calculateDates(position + 1);
		final HolidayDay holidayDay = UnusualCalendar.getOrCreateDay(holidayDays, date.first.getValue(), date.second);
		bundle.putParcelable(MainActivity.HOLIDAY_DAY, holidayDay);
		dayFragment.setArguments(bundle);
		return dayFragment;
	}

	@Override
	public int getItemCount() {
		return 367;
	}
}
