package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.time.Month;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.HolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.databinding.FragmentDayBinding;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.persistence.HolidayViewModel;
import eu.andret.kalendarzswiatnietypowych.util.PreferenceHelper;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class DayFragment extends Fragment {
	private HolidayAdapter holidayAdapter;
	@Nullable
	private FragmentDayBinding binding;

	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent,
			final Bundle savedInstanceState) {
		binding = FragmentDayBinding.inflate(inflater, parent, false);
		if (getArguments() == null || getContext() == null) {
			return binding.getRoot();
		}
		final int position = getArguments().getInt(DayActivity.POSITION);
		final Pair<Month, Integer> date = Util.calculateDates(position + 1);
		final PreferenceHelper preferences = new PreferenceHelper(getContext());
		final boolean includeUsual = preferences.includeUsualHolidays();
		final boolean showAdult = preferences.showAdultContent();

		final HolidayViewModel holidayViewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(HolidayViewModel.INITIALIZER))
				.get(HolidayViewModel.class);

		holidayAdapter = new HolidayAdapter();
		binding.fragmentDayListHolidays.setAdapter(holidayAdapter);

		final long seed = Util.calculateSeed(date.second, date.first.getValue());
		if (preferences.isThemeColorized()) {
			binding.fragmentDayRelativeMain.setBackgroundColor(Util.randomizeColor(getContext(), seed));
		}
		holidayViewModel.getHolidayDay(date.first.getValue(), date.second).observe(getViewLifecycleOwner(), holidayDay -> {
			if (binding == null) {
				return;
			}
			final List<Holiday> holidaysList = holidayDay.getHolidaysList(includeUsual, showAdult);
			holidayAdapter.submitList(holidaysList);
			if (holidaysList.isEmpty()) {
				binding.fragmentDayImageSad.setVisibility(View.VISIBLE);
				binding.fragmentDayTextEmpty.setVisibility(View.VISIBLE);
			}
		});
		return binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		binding = null;
		super.onDestroyView();
	}
}
