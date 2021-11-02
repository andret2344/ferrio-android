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

import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.HolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Data.Prefs;
import eu.andret.kalendarzswiatnietypowych.utils.Util;

public class DayFragment extends Fragment {
	private static final Random RANDOM = new Random();
	private int id;

	@Override
	public void setArguments(final Bundle args) {
		super.setArguments(args);
		id = args.getInt("id");
	}

	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View dayView = inflater.inflate(R.layout.fragment_day, parent, false);
		final Util.MonthDayPair date = new Util(getActivity()).calculateDates(id + 1);
		final SharedPreferences theme = Data.getPreferences(getActivity(), Data.Prefs.THEME);
		final Data.AppColorSet color = Data.getColors(theme.getInt(getContext().getResources().getString(R.string.settings_theme_app), 1));
		final HolidayDay holidays = HolidayCalendar.getInstance(getActivity()).getMonth(date.getMonth()).getDay(date.getDay());
		if (!holidays.hasHolidays(theme.getBoolean(getContext().getResources().getString(R.string.settings_usual_holidays), false))) {
			dayView.findViewById(R.id.fragment_day_image_sad).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setBackgroundColor(Color.GRAY);
		}
		int c = color.background;
		if (theme.getBoolean(getContext().getResources().getString(R.string.settings_theme_colorized), false)) {
			RANDOM.setSeed(holidays.getSeed());
			final boolean dark = Data.getColors(Data.getPreferences(getActivity(), Prefs.THEME).getInt(getContext().getResources().getString(R.string.settings_theme_app), 1)).dark;
			c = Color.rgb(RANDOM.nextInt(127) + (dark ? 0 : 127), RANDOM.nextInt(127) + (dark ? 0 : 127), RANDOM.nextInt(127) + (dark ? 0 : 127));
			dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(c);
		} else {
			dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(color.background);
		}
		((ListView) dayView.findViewById(R.id.fragment_day_list_holidays)).setAdapter(new HolidayAdapter(getActivity(), holidays, c, true));
		return dayView;
	}
}
