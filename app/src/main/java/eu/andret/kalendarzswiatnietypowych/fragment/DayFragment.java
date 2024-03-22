package eu.andret.kalendarzswiatnietypowych.fragment;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.HolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class DayFragment extends Fragment {
	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View dayView = inflater.inflate(R.layout.fragment_day, parent, false);
		if (getArguments() == null || getContext() == null) {
			return dayView;
		}
		final HolidayDay holidayDay = getArguments().getParcelable(MainActivity.HOLIDAY_DAY);
		if (holidayDay == null) {
			return dayView;
		}
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		final boolean includeUsual = preferences.getBoolean(getContext().getString(R.string.settings_key_usual_holidays), false);
		final List<Holiday> holidays = holidayDay.getHolidaysList(includeUsual);
		if (holidays.isEmpty()) {
			dayView.findViewById(R.id.fragment_day_image_sad).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setBackgroundColor(Color.GRAY);
		}
		if (preferences.getBoolean(getContext().getString(R.string.settings_key_theme_colorized), false)) {
			final int backgroundColor = Util.randomizeColor(getContext(), holidayDay.getSeed());
			dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(backgroundColor);
		}
		final ListView listView = dayView.findViewById(R.id.fragment_day_list_holidays);
		listView.setAdapter(new HolidayAdapter(getContext(), holidays));
		return dayView;
	}
}
