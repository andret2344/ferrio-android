package eu.andret.kalendarzswiatnietypowych.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.UnusualCalendar;
import eu.andret.kalendarzswiatnietypowych.persistance.SharedViewModel;
import eu.andret.kalendarzswiatnietypowych.util.Data;
import eu.andret.kalendarzswiatnietypowych.util.SpanningGridLayoutManager;

public class MonthFragment extends Fragment {
	private SharedViewModel sharedViewModel;

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedViewModel = new ViewModelProvider(requireActivity(), ViewModelProvider.Factory.from(SharedViewModel.INITIALIZER))
				.get(SharedViewModel.class);
	}

	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View month = inflater.inflate(R.layout.fragment_month, parent, false);
		if (getArguments() == null) {
			return month;
		}

		final int current = getArguments().getInt(MainActivity.MONTH, 1);
		final Data.ColorSet color = Data.getColors(getContext());

		month.findViewById(R.id.fragment_month_grid_days).setBackgroundColor(color.getForegroundColor());

		final LocalDate before = getBefore(current);
		final LocalDate after = getAfter(current, before);

		sharedViewModel.getHolidayDays(before.getMonthValue(), before.getDayOfMonth(), after.getMonthValue(), after.getDayOfMonth())
				.observe(getViewLifecycleOwner(), holidayDays -> {
					final RecyclerView recyclerView = month.findViewById(R.id.fragment_month_grid_days);
					recyclerView.setLayoutManager(new SpanningGridLayoutManager(getContext(), 7, LinearLayoutManager.VERTICAL, false));
					final List<HolidayDay> days = UnusualCalendar.getHolidayDaysInDateRange(holidayDays, before, after);
					recyclerView.setAdapter(new DayAdapter(getContext(), current, days));
				});
		return month;
	}

	private LocalDate getBefore(final int month) {
		LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, 1);
		if (date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
			date = date.minusDays(1);
		}
		while (!date.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
			date = date.minusDays(1);
		}
		return date;
	}

	private LocalDate getLastDay(final int month) {
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, 1);
		return date.plusDays(date.lengthOfMonth()).minusDays(1);
	}

	private LocalDate getAfter(final int month, final LocalDate before) {
		LocalDate date = getLastDay(month);
		while (!date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
			date = date.plusDays(1);
		}
		date = date.plusDays(1);
		final long diffDays = before.until(date, ChronoUnit.DAYS);
		final long diffWeeks = diffDays / 7 + (diffDays % 7 == 0 ? 0 : 1);
		if (diffWeeks < 6) {
			date = date.plusDays(7 * (6 - diffWeeks));
		}
		return date;
	}
}
