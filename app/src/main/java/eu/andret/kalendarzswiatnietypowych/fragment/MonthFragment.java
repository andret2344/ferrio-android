package eu.andret.kalendarzswiatnietypowych.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;

public class MonthFragment extends Fragment {
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View month = inflater.inflate(R.layout.fragment_month, parent, false);
		final int current = Optional.ofNullable(getArguments())
				.map(x -> x.getInt(MainActivity.MONTH, 1))
				.orElseThrow(() -> new UnsupportedOperationException("No passed arguments!"));

		final LocalDate before = getBefore(current);
		final LocalDate after = getAfter(current, before);
		final SharedPreferences theme = Data.getPreferences(getActivity(), Data.Prefs.THEME);
		final Data.AppColorSet color = Data.getColors(theme.getInt(getContext().getResources().getString(R.string.settings_theme_app), 1));
		final List<HolidayDay> holidays = HolidayCalendar.getInstance(getContext()).getHolidayDaysInDateRange(before, after, true);
		final GridView grid = month.findViewById(R.id.fragment_month_grid_days);
		grid.measure(0, 0);
		final DayAdapter adapter = new DayAdapter(getActivity(), holidays, current);
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
