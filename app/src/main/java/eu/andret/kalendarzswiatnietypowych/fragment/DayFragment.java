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

import java.time.LocalDate;
import java.time.Month;
import java.util.Random;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.adapter.HolidayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.Data;
import eu.andret.kalendarzswiatnietypowych.utils.Data.Prefs;
import lombok.Getter;

public class DayFragment extends Fragment {
	private static final Random random = new Random();

	@Getter
	private int day;
	@Getter
	private int month;
	private int id;

	@Override
	public void setArguments(final Bundle args) {
		super.setArguments(args);
		day = args.getInt("day");
		month = args.getInt("month");
		id = args.getInt("id");
	}

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View dayView = inflater.inflate(R.layout.fragment_day, parent, false);
		if (day == -1 || month == -1) {
			final LocalDate date = calculateDates();
			day = date.getDayOfMonth();
			month = date.getMonthValue();
		}
		final SharedPreferences theme = Data.getPreferences(getActivity(), Data.Prefs.THEME);
		final Data.AppColorSet color = Data.getColors(Integer.parseInt(theme.getString(getContext().getResources().getString(R.string.settings_theme_app), "1")));
		final HolidayDay holidays = HolidayCalendar.getInstance(getActivity()).getMonth(month).getDay(day);
		if (!holidays.hasHolidays(theme.getBoolean(getContext().getResources().getString(R.string.settings_usual_holidays), false))) {
			dayView.findViewById(R.id.fragment_day_image_sad).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setVisibility(View.VISIBLE);
			dayView.findViewById(R.id.fragment_day_text_empty).setBackgroundColor(Color.GRAY);
		}
		int c = color.background;
		if (theme.getBoolean(getContext().getResources().getString(R.string.settings_theme_colorized), false)) {
			random.setSeed(holidays.getSeed());
			final boolean dark = Data.getColors(Integer.parseInt(Data.getPreferences(getActivity(), Prefs.THEME).getString(getContext().getResources().getString(R.string.settings_theme_app), "1"))).dark;
			c = Color.rgb(random.nextInt(127) + (dark ? 0 : 127), random.nextInt(127) + (dark ? 0 : 127), random.nextInt(127) + (dark ? 0 : 127));
			dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(c);
		} else {
			dayView.findViewById(R.id.fragment_day_relative_main).setBackgroundColor(color.background);
		}
		((ListView) dayView.findViewById(R.id.fragment_day_list_holidays)).setAdapter(new HolidayAdapter(getActivity(), holidays, c, true));
		return dayView;
	}

	private LocalDate calculateDates() {
		final LocalDate now = LocalDate.now();
		if (now.isLeapYear()) {
			if (id < 60) {
				return LocalDate.ofYearDay(now.getYear(), id + 1);
			}
			if (id == 60) {
				return LocalDate.of(now.getYear(), Month.FEBRUARY, 30);
			}
			return LocalDate.ofYearDay(now.getYear(), id);
		}
		if (id < 59) {
			return LocalDate.ofYearDay(now.getYear(), id + 1);
		}
		if (id == 59) {
			return LocalDate.of(now.getYear(), Month.FEBRUARY, 29);
		}
		if (id == 60) {
			return LocalDate.of(now.getYear(), Month.FEBRUARY, 30);
		}
		return LocalDate.ofYearDay(now.getYear(), id);
	}
}
