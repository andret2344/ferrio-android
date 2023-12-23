package eu.andret.kalendarzswiatnietypowych.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import eu.andret.kalendarzswiatnietypowych.fragment.DayFragment;

public class DayFragmentAdapter extends FragmentStateAdapter {
	public DayFragmentAdapter(@NonNull final FragmentManager fragmentManager, @NonNull final Lifecycle lifecycle) {
		super(fragmentManager, lifecycle);
	}

	@NonNull
	@Override
	public Fragment createFragment(final int position) {
		final DayFragment dayFragment = new DayFragment();
		final Bundle bundle = new Bundle();
		bundle.putInt("position", position);
		dayFragment.setArguments(bundle);
		return dayFragment;
	}

	@Override
	public int getItemCount() {
		return 367;
	}
}
