package eu.andret.kalendarzswiatnietypowych.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.fragment.DayFragment;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class DayFragmentAdapter extends FragmentStateAdapter {
	private final HolidayCalendar holidayCalendar;

	public DayFragmentAdapter(@NonNull final FragmentManager fragmentManager, @NonNull final Lifecycle lifecycle, final HolidayCalendar holidayCalendar) {
		super(fragmentManager, lifecycle);
		this.holidayCalendar = holidayCalendar;
	}

	@NonNull
	@Override
	public Fragment createFragment(final int position) {
		final DayFragment dayFragment = new DayFragment();
		final Bundle bundle = new Bundle();
		final Util.MonthDayPair date = Util.calculateDates(position + 1);
		final HolidayDay holidayDay = holidayCalendar.getDay(date.getMonth().getValue(), date.getDay());
		bundle.putParcelable("holidayDay", holidayDay);
		dayFragment.setArguments(bundle);
		return dayFragment;
	}

	@Override
	public int getItemCount() {
		return 367;
	}
}
