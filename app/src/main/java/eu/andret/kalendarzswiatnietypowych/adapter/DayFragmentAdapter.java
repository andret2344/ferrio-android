package eu.andret.kalendarzswiatnietypowych.adapter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import eu.andret.kalendarzswiatnietypowych.fragment.DayFragment;

public class DayFragmentAdapter extends FragmentStatePagerAdapter {
	public DayFragmentAdapter(final FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(final int id) {
		final DayFragment dayFragment = new DayFragment();
		final Bundle bundle = new Bundle();
		bundle.putInt("id", id);
		dayFragment.setArguments(bundle);
		return dayFragment;
	}

	@Override
	public int getCount() {
		return 367;
	}
}
