package eu.andret.kalendarzswiatnietypowych.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapters.DayAdapter;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay;

public class MonthFragment extends Fragment {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View month = inflater.inflate(R.layout.fragment_month, parent, false);
		int current = getArguments().getInt("month", 1);

		Calendar c = Calendar.getInstance();
		c.set(Calendar.MONTH, current);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.get(Calendar.DAY_OF_YEAR);
		if (c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			c.add(Calendar.DATE, -1);
		}
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		c.get(Calendar.DAY_OF_YEAR);
		Calendar before = (Calendar) c.clone();

		c.set(Calendar.MONTH, current);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.get(Calendar.DAY_OF_YEAR);
		c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));

		int w = c.get(Calendar.WEEK_OF_YEAR);
		c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		c.set(Calendar.WEEK_OF_YEAR, w);
		c.get(Calendar.DAY_OF_YEAR);
		c.add(Calendar.DAY_OF_YEAR, 1);
		c.get(Calendar.DAY_OF_YEAR);
		Calendar after = (Calendar) c.clone();

		int diffDays = (int) TimeUnit.MILLISECONDS.toDays(after.getTimeInMillis() - before.getTimeInMillis());
		int diffWeeks = diffDays / 7 + (diffDays % 7 == 0 ? 0 : 1);
		if (diffWeeks < 6) {
			after.add(Calendar.WEEK_OF_MONTH, 6 - diffWeeks);
		}

		SharedPreferences theme = Data.getPreferences(getActivity(), Data.Prefs.THEME);
		Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getContext().getResources().getString(R.string.settings_theme_app), "1")));
		List<HolidayDay> holidays = HolidayCalendar.getInstance(getContext()).getHolidayDaysInDateRange(before, after, true);
		GridView grid = month.findViewById(R.id.fragment_month_grid_days);
		grid.measure(0, 0);
		DayAdapter adapter = new DayAdapter(getActivity(), holidays, current);
		grid.setAdapter(adapter);
		grid.post(() -> {
			int x = grid.getMeasuredHeight() / 6 - 1;
			for (int i = 0; i < grid.getChildCount(); i++) {
				View v = grid.getChildAt(i);
				ViewGroup.LayoutParams lp = v.getLayoutParams();
				lp.height = x;
				v.setLayoutParams(lp);
			}
		});
		month.findViewById(R.id.fragment_month_grid_days).setBackgroundColor(color.background);
		return month;
	}
}
