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
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Data.Prefs;

public class DayFragment extends Fragment {
	private static final Random RANDOM = new Random();

	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View dayView = inflater.inflate(R.layout.fragment_day, parent, false);
		if (getArguments() == null) {
			return dayView;
		}
		final SharedPreferences theme = Data.getPreferences(getActivity(), Data.Prefs.THEME);
		final Data.AppColorSet color = Data.getColors(theme.getInt(getContext().getResources().getString(R.string.settings_theme_app), 1));
		final HolidayDay holidayDay = getArguments().getParcelable("holidayDay");
		if (holidayDay.countHolidays(theme.getBoolean(getContext().getResources().getString(R.string.settings_usual_holidays), false)) == 0) {
			dayView.findViewById(R.id.fragment_day_image_sad).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setBackgroundColor(Color.GRAY);
		}
		int c = color.background;
		if (theme.getBoolean(getContext().getResources().getString(R.string.settings_theme_colorized), false)) {
			RANDOM.setSeed(holidayDay.getSeed());
			final boolean dark = Data.getColors(Data.getPreferences(getActivity(), Prefs.THEME).getInt(getContext().getResources().getString(R.string.settings_theme_app), 1)).dark;
			c = Color.rgb(RANDOM.nextInt(127) + (dark ? 0 : 127), RANDOM.nextInt(127) + (dark ? 0 : 127), RANDOM.nextInt(127) + (dark ? 0 : 127));
			dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(c);
		} else {
			dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(color.background);
		}
		final ListView listView = (ListView) dayView.findViewById(R.id.fragment_day_list_holidays);
		listView.setAdapter(new HolidayAdapter(getActivity(), holidayDay, c, true));
		return dayView;
	}
}
