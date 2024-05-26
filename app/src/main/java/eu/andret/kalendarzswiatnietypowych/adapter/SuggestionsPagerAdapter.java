package eu.andret.kalendarzswiatnietypowych.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class SuggestionsPagerAdapter extends FragmentStateAdapter {
	private final List<Fragment> fragmentList = new ArrayList<>();

	public SuggestionsPagerAdapter(@NonNull final FragmentActivity fragmentActivity) {
		super(fragmentActivity);
	}

	@NonNull
	@Override
	public Fragment createFragment(final int position) {
		return fragmentList.get(position);
	}

	@Override
	public int getItemCount() {
		return fragmentList.size();
	}

	public void addFragment(final Fragment fragment) {
		fragmentList.add(fragment);
	}
}
