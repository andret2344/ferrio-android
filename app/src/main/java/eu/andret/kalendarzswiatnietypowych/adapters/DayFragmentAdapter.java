package eu.andret.kalendarzswiatnietypowych.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import eu.andret.kalendarzswiatnietypowych.fragment.DayFragment;

public class DayFragmentAdapter extends FragmentStatePagerAdapter {
	private final int count;
	private final int month, day;
	
	public DayFragmentAdapter(FragmentManager fm) {
		this(fm, -1, -1);
	}
	
	private DayFragmentAdapter(FragmentManager fm, int month, int day) {
		super(fm);
		this.month = month;
		this.day = day;
		count = month == -1 || day == -1 ? 367 : 1;
	}
	
	@Override
	public Fragment getItem(int id) {
		DayFragment fragment = new DayFragment();
		Bundle args = new Bundle();
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
