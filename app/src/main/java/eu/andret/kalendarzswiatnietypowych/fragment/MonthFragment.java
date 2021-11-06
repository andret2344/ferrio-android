package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activities.MainActivity;
import eu.andret.kalendarzswiatnietypowych.adapters.DayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;

public class MonthFragment extends Fragment {
	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View month = inflater.inflate(R.layout.fragment_month, parent, false);
		if (getArguments() == null) {
			return month;
		}

		final int current = getArguments().getInt(MainActivity.MONTH, 1);
		final List<HolidayDay> holidayDays = getArguments().getParcelableArrayList(MainActivity.HOLIDAY_DAYS);
		final Data.AppColorSet color = Data.getColors(getContext());
		final GridView grid = month.findViewById(R.id.fragment_month_grid_days);
		grid.measure(0, 0);
		final DayAdapter adapter = new DayAdapter(getActivity(), holidayDays, current);
		grid.setAdapter(adapter);
		grid.post(() -> {
			final int itemHeight = grid.getMeasuredHeight() / 6 - 25;
			for (int i = 0; i < grid.getChildCount(); i++) {
				final View view = grid.getChildAt(i);
				final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
				view.setLayoutParams(new AbsListView.LayoutParams(layoutParams.width, itemHeight));
			}
		});
		month.findViewById(R.id.fragment_month_grid_days).setBackgroundColor(color.getBackgroundColor());
		return month;
	}
}
