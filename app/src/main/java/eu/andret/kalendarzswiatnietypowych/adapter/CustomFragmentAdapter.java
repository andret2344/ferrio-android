package eu.andret.kalendarzswiatnietypowych.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CustomFragmentAdapter extends FragmentStateAdapter {
	private final List<Supplier<Fragment>> fragmentFactories = new ArrayList<>();

	public CustomFragmentAdapter(@NonNull final FragmentActivity fragmentActivity) {
		super(fragmentActivity);
	}

	@NonNull
	@Override
	public Fragment createFragment(final int position) {
		return fragmentFactories.get(position).get();
	}

	@Override
	public int getItemCount() {
		return fragmentFactories.size();
	}

	public void addFragment(@NonNull final Supplier<Fragment> factory) {
		fragmentFactories.add(factory);
	}
}
