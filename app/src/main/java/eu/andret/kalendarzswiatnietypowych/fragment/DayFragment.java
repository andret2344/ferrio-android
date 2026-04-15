package eu.andret.kalendarzswiatnietypowych.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Month;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.HolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.persistence.HolidayViewModel;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class DayFragment extends Fragment {
	private HolidayAdapter holidayAdapter;

	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent,
			final Bundle savedInstanceState) {
		final View dayView = inflater.inflate(R.layout.fragment_day, parent, false);
		if (getArguments() == null || getContext() == null) {
			return dayView;
		}
		final int position = getArguments().getInt(DayActivity.POSITION);
		final Pair<Month, Integer> date = Util.calculateDates(position + 1);
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		final boolean includeUsual = preferences.getBoolean(getContext().getString(R.string.settings_key_usual_holidays), false);
		final RecyclerView recyclerView = dayView.findViewById(R.id.fragment_day_list_holidays);
		final View backgroundView = dayView.findViewById(R.id.fragment_day_relative_main);
		final View sadImage = dayView.findViewById(R.id.fragment_day_image_sad);
		final View emptyText = dayView.findViewById(R.id.fragment_day_text_empty);

		final HolidayViewModel holidayViewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(HolidayViewModel.INITIALIZER))
				.get(HolidayViewModel.class);

		holidayAdapter = new HolidayAdapter();
		recyclerView.setAdapter(holidayAdapter);

		final long seed = Util.calculateSeed(date.second, date.first.getValue());
		if (preferences.getBoolean(getString(R.string.settings_key_theme_colorized), false)) {
			backgroundView.setBackgroundColor(Util.randomizeColor(getContext(), seed));
		}
		holidayViewModel.getHolidayDay(date.first.getValue(), date.second).observe(getViewLifecycleOwner(), holidayDay -> {
			final List<Holiday> holidaysList = holidayDay.getHolidaysList(includeUsual);
			holidayAdapter.submitList(holidaysList);
			if (holidaysList.isEmpty()) {
				sadImage.setVisibility(View.VISIBLE);
				emptyText.setVisibility(View.VISIBLE);
			}
		});
		return dayView;
	}
}
