package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapter;
import eu.andret.kalendarzswiatnietypowych.persistance.HolidayViewModel;

public class MonthFragment extends Fragment {
	private HolidayViewModel holidayViewModel;

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		holidayViewModel = new ViewModelProvider(requireActivity(), ViewModelProvider.Factory.from(HolidayViewModel.INITIALIZER))
				.get(HolidayViewModel.class);
	}

	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View month = inflater.inflate(R.layout.fragment_month, parent, false);
		if (getArguments() == null) {
			return month;
		}

		final int current = getArguments().getInt(MainActivity.MONTH, 1);

		final LocalDate before = getBefore(current);
		final LocalDate after = before.plusDays(42);

		final RecyclerView recyclerView = month.findViewById(R.id.fragment_month_grid_days);
		holidayViewModel.getHolidayDays(before, after)
				.observe(getViewLifecycleOwner(), holidayDays ->
						recyclerView.setAdapter(new DayAdapter(getContext(), current, holidayDays)));
		return month;
	}

	private LocalDate getBefore(final int month) {
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, 1);
		if (date.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
			return date;
		}
		return date.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
	}
}
