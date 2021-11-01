package eu.andret.kalendarzswiatnietypowych.adapter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import eu.andret.kalendarzswiatnietypowych.fragment.DayFragment;

public class DayFragmentAdapter extends FragmentStatePagerAdapter {
	private final int count;
	private final int month;
	private final int day;

	public DayFragmentAdapter(final FragmentManager fm) {
		this(fm, -1, -1);
	}

	private DayFragmentAdapter(final FragmentManager fm, final int month, final int day) {
		super(fm);
		this.month = month;
		this.day = day;
		count = month == -1 || day == -1 ? 367 : 1;
	}

	@Override
	public Fragment getItem(final int id) {
		final DayFragment fragment = new DayFragment();
		final Bundle args = new Bundle();
		args.putInt("id", id);
		args.putInt("day", day);
		args.putInt("month", month);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public int getCount() {
		return count;
	}
}
