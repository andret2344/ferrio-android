/**
 * Author: Andret
 * Copying and modifying allowed only keeping git link
 */
package eu.andret.kalendarzswiatnietypowych.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
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
