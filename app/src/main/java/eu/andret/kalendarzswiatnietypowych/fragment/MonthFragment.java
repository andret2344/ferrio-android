package eu.andret.kalendarzswiatnietypowych.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;

public class MonthFragment extends Fragment {
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View month = inflater.inflate(R.layout.fragment_month, parent, false);
		if (getArguments() == null) {
			return month;
		}

		final int current = getArguments().getInt(MainActivity.MONTH, 1);
		final List<HolidayDay> holidayDays = getArguments().getParcelableArrayList(MainActivity.HOLIDAY_DAYS);
		final SharedPreferences theme = Data.getPreferences(getActivity(), Data.Prefs.THEME);
		final Data.AppColorSet color = Data.getColors(theme.getInt(getContext().getResources().getString(R.string.settings_theme_app), 1));
		final GridView grid = month.findViewById(R.id.fragment_month_grid_days);
		grid.measure(0, 0);
		final DayAdapter adapter = new DayAdapter(getActivity(), holidayDays, current);
		grid.setAdapter(adapter);
		grid.post(() -> {
			final int x = grid.getMeasuredHeight() / 6 - 1;
			for (int i = 0; i < grid.getChildCount(); i++) {
				final View v = grid.getChildAt(i);
				final ViewGroup.LayoutParams lp = v.getLayoutParams();
				lp.height = x;
				v.setLayoutParams(lp);
			}
		});
		month.findViewById(R.id.fragment_month_grid_days).setBackgroundColor(color.background);
		return month;
	}
}
