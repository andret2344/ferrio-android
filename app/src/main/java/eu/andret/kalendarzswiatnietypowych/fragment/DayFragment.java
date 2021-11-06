package eu.andret.kalendarzswiatnietypowych.fragment;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activities.MainActivity;
import eu.andret.kalendarzswiatnietypowych.adapters.HolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

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
		final SharedPreferences theme = Data.getPreferences(getContext(), Data.Prefs.THEME);
		final Resources resources = getContext().getResources();
		final Data.AppColorSet color = Data.getColors(Util.isDarkTheme(getContext()));
		final boolean includeUsual = theme.getBoolean(resources.getString(R.string.settings_key_usual_holidays), false);
		final List<Holiday> holidays = holidayDay.getHolidaysList(includeUsual);
		if (holidays.isEmpty()) {
			dayView.findViewById(R.id.fragment_day_image_sad).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setBackgroundColor(Color.GRAY);
		}
		final int backgroundColor;
		if (theme.getBoolean(resources.getString(R.string.settings_key_theme_colorized), false)) {
			backgroundColor = Util.randomizeColor(color.dark, holidayDay.getSeed());
		} else {
			backgroundColor = color.background;
		}
		dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(backgroundColor);
		final ListView listView = dayView.findViewById(R.id.fragment_day_list_holidays);
		listView.setAdapter(new HolidayAdapter(getContext(), holidays));
		return dayView;
	}
}
