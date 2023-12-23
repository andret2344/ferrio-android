package eu.andret.kalendarzswiatnietypowych.fragment;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import java.time.Month;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.HolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.persistance.SharedViewModel;
import eu.andret.kalendarzswiatnietypowych.util.Data;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class DayFragment extends Fragment {
	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View dayView = inflater.inflate(R.layout.fragment_day, parent, false);
		if (getArguments() == null || getContext() == null) {
			return dayView;
		}
		final int position = getArguments().getInt("position");
		final Pair<Month, Integer> date = Util.calculateDates(position + 1);
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		final Data.ColorSet color = Data.getColors(getContext());
		final boolean includeUsual = preferences.getBoolean(getContext().getString(R.string.settings_key_usual_holidays), false);

		final SharedViewModel sharedViewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(SharedViewModel.INITIALIZER))
				.get(SharedViewModel.class);
		sharedViewModel.getHolidayDay(date.first.getValue(), date.second)
				.observe(getViewLifecycleOwner(), holidayDay -> holidayDay.ifPresent(day -> {
					final List<Holiday> holidays = day.getHolidaysList(includeUsual);
					if (holidays.isEmpty()) {
						dayView.findViewById(R.id.fragment_day_image_sad).setVisibility(View.VISIBLE);
						dayView.findViewById(R.id.fragment_day_text_empty).setVisibility(View.VISIBLE);
						dayView.findViewById(R.id.fragment_day_text_empty).setBackgroundColor(Color.GRAY);
					}
					final int backgroundColor = getBackgroundColor(preferences, color, day);
					dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(backgroundColor);
					final ListView listView = dayView.findViewById(R.id.fragment_day_list_holidays);
					listView.setAdapter(new HolidayAdapter(getContext(), holidays));
				}));

		return dayView;
	}

	private int getBackgroundColor(@NonNull final SharedPreferences preferences,
								   @NonNull final Data.ColorSet color,
								   @NonNull final HolidayDay holidayDay) {
		if (preferences.getBoolean(getString(R.string.settings_key_theme_colorized), false)) {
			return Util.randomizeColor(getContext(), holidayDay.getSeed());
		}
		return color.getBackgroundColor();
	}
}
