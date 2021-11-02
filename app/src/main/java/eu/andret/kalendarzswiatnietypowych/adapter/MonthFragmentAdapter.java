package eu.andret.kalendarzswiatnietypowych.adapter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import eu.andret.kalendarzswiatnietypowych.fragment.MonthFragment;

public class MonthFragmentAdapter extends FragmentStatePagerAdapter {
	public MonthFragmentAdapter(final FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(final int id) {
		final MonthFragment fragment = new MonthFragment();
		final Bundle bundle = new Bundle();
		bundle.putInt("month", id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public int getCount() {
		return 12;
	}
}
