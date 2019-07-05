package eu.andret.kalendarzswiatnietypowych.adapters;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import eu.andret.kalendarzswiatnietypowych.fragment.MonthFragment;

public class MonthFragmentAdapter extends FragmentStatePagerAdapter {

	public MonthFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int id) {
		MonthFragment fragment = new MonthFragment();
		Bundle args = new Bundle();
		args.putInt("month", id);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public int getCount() {
		return 12;
	}
}
