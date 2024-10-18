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
import java.util.Locale;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.DayActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.HolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.persistance.HolidayViewModel;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class DayFragment extends Fragment {
	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View dayView = inflater.inflate(R.layout.fragment_day, parent, false);
		if (getArguments() == null || getContext() == null) {
			return dayView;
		}
		final int position = getArguments().getInt(DayActivity.POSITION);
		final Pair<Month, Integer> date = Util.calculateDates(position + 1);
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		final boolean includeUsual = preferences.getBoolean(getContext().getString(R.string.settings_key_usual_holidays), false);
		final RecyclerView recyclerView = dayView.findViewById(R.id.fragment_day_list_holidays);

		final HolidayViewModel holidayViewModel = new ViewModelProvider(this, ViewModelProvider.Factory.from(HolidayViewModel.INITIALIZER))
				.get(HolidayViewModel.class);
		final long seed = Long.parseLong(String.format(Locale.ROOT, "%d%d", date.second, date.first.getValue()));
		if (preferences.getBoolean(getString(R.string.settings_key_theme_colorized), false)) {
			dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(Util.randomizeColor(getContext(), seed));
		}
		final Runnable onElseAction = () -> {
			dayView.findViewById(R.id.fragment_day_image_sad).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setVisibility(View.VISIBLE);
		};
		holidayViewModel.getHolidayDay(date.first.getValue(), date.second).observe(getViewLifecycleOwner(), holidayDay -> {
			System.out.println("I've changed");
			holidayDay.ifPresentOrElse(day -> recyclerView.setAdapter(new HolidayAdapter(getContext(), day.getHolidaysList(includeUsual))), onElseAction);
		});
		return dayView;
	}
}
