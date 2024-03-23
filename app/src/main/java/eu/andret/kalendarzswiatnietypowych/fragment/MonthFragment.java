package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;

public class MonthFragment extends Fragment {
	private final List<HolidayDay> holidayDays;

	public MonthFragment(@NonNull final List<HolidayDay> holidayDays) {
		this.holidayDays = holidayDays;
	}

	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View month = inflater.inflate(R.layout.fragment_month, parent, false);
		if (getArguments() == null) {
			return month;
		}

		final int current = getArguments().getInt(MainActivity.MONTH, 1);
		final RecyclerView recyclerView = month.findViewById(R.id.fragment_month_grid_days);
		final DayAdapter adapter = new DayAdapter(getContext(), current, holidayDays);
		recyclerView.setAdapter(adapter);
		return month;
	}
}
