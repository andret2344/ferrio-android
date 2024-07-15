package eu.andret.kalendarzswiatnietypowych.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.fragment.MonthFragment;

public class MonthFragmentAdapter extends FragmentStateAdapter {
	public MonthFragmentAdapter(@NonNull final FragmentManager fragmentManager, @NonNull final Lifecycle lifecycle) {
		super(fragmentManager, lifecycle);
	}

	@NonNull
	@Override
	public Fragment createFragment(final int position) {
		final MonthFragment fragment = new MonthFragment();
		final Bundle bundle = new Bundle();
		bundle.putInt(MainActivity.MONTH, position + 1);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public int getItemCount() {
		return 12;
	}
}
