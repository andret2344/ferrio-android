package eu.andret.kalendarzswiatnietypowych.fragment;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapter;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.UnusualCalendar;
import eu.andret.kalendarzswiatnietypowych.persistance.HolidayViewModel;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class MonthFragment extends Fragment {
	private static final int MAX_WORDS_COUNT = 4;
	private HolidayViewModel holidayViewModel;
	private int currentMonth;
	private LocalDate before;
	private LocalDate after;

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		holidayViewModel = new ViewModelProvider(requireActivity(), ViewModelProvider.Factory.from(HolidayViewModel.INITIALIZER))
				.get(HolidayViewModel.class);
		if (getArguments() == null) {
			return;
		}

		currentMonth = getArguments().getInt(MainActivity.MONTH, 1);

		before = getBefore();
		after = before.plusDays(42);
	}

	@NonNull
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View month = inflater.inflate(R.layout.fragment_month, parent, false);

		final RecyclerView recyclerView = month.findViewById(R.id.fragment_month_grid_days);
		recyclerView.setHasFixedSize(true);
		holidayViewModel.getHolidayDays(before, after)
				.observe(getViewLifecycleOwner(), holidayDays -> {
					final List<HolidayDayViewModel> dataSet = UnusualCalendar.getHolidayDaysInDateRange(holidayDays, before, after)
							.stream()
							.map(this::convert)
							.collect(Collectors.toList());
					recyclerView.setAdapter(new DayAdapter(getContext(), dataSet));
				});
		return month;
	}

	private LocalDate getBefore() {
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), currentMonth, 1);
		if (date.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
			return date.minusWeeks(1);
		}
		return date.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
	}

	@NonNull
	public HolidayDayViewModel convert(@NonNull final HolidayDay holidayDay) {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
		final HolidayDayViewModel holidayDayViewModel = new HolidayDayViewModel(holidayDay);
		if (holidayDay.getMonth() != currentMonth) {
			holidayDayViewModel.cardBackgroundColor = ContextCompat.getColor(requireContext(), R.color.background_secondary);
		} else if (preferences.getBoolean(requireContext().getString(R.string.settings_key_theme_colorized), false)) {
			holidayDayViewModel.cardBackgroundColor = Util.randomizeColor(requireContext(), holidayDay.getSeed());
		} else {
			holidayDayViewModel.cardBackgroundColor = ContextCompat.getColor(requireContext(), R.color.background_accent);
		}

		final LocalDate now = LocalDate.now();
		holidayDayViewModel.strokeColor = ContextCompat.getColor(requireContext(), R.color.today_outline);
		if (holidayDay.getDay() == now.getDayOfMonth() && holidayDay.getMonth() == now.getMonthValue()) {
			holidayDayViewModel.strokeWidth = 4;
		}
		final boolean includeUsual = preferences.getBoolean(requireContext().getString(R.string.settings_key_usual_holidays), false);
		final boolean displayShortcuts = preferences.getBoolean(requireContext().getString(R.string.settings_key_display_shortcuts), true);
		final List<Holiday> holidaysList = holidayDay.getHolidaysList(includeUsual);

		holidayDayViewModel.smallDate = String.valueOf(holidayDay.getDay());
		if (holidaysList.isEmpty()) {
			holidayDayViewModel.sadImageVisibility = View.VISIBLE;
			return holidayDayViewModel;
		}
		if (!displayShortcuts) {
			holidayDayViewModel.bigDate = String.valueOf(holidayDay.getDay());
			holidayDayViewModel.smallDate = null;
			return holidayDayViewModel;
		}

		final Holiday displayedHoliday = holidaysList.get(0);
		final String[] words = displayedHoliday.getName().split(" ");
		final String result = Arrays.stream(words).limit(MAX_WORDS_COUNT).collect(Collectors.joining(" "));
		final boolean full = words.length <= MAX_WORDS_COUNT;
		final boolean isDisplayedUsual = displayedHoliday.isUsual();
		if (isDisplayedUsual) {
			holidayDayViewModel.typeFace = Typeface.BOLD;
		}
		int holidaysCountIndicator = holidaysList.size();
		if (full) {
			holidaysCountIndicator--;
			holidayDayViewModel.holidayText = result;
		} else {
			holidayDayViewModel.holidayText = requireContext().getString(R.string.ellipsis_text, result);
		}

		if (holidaysCountIndicator > 0) {
			holidayDayViewModel.moreText = requireContext().getString(R.string.see_more, holidaysCountIndicator);
		}

		return holidayDayViewModel;
	}

	public static class HolidayDayViewModel {
		private final String id;
		private final int day;
		private final int month;
		private int cardBackgroundColor;
		private int strokeColor;
		private int strokeWidth;
		private int sadImageVisibility = View.INVISIBLE;
		private String smallDate;
		private String bigDate;
		private int typeFace;
		private String holidayText;
		private String moreText;

		public HolidayDayViewModel(final String id, final int day, final int month) {
			this.id = id;
			this.day = day;
			this.month = month;
		}

		public HolidayDayViewModel(@NonNull final HolidayDay holidayDay) {
			this(holidayDay.getId(), holidayDay.getDay(), holidayDay.getMonth());
		}

		public int getDay() {
			return day;
		}

		public int getMonth() {
			return month;
		}

		public int getCardBackgroundColor() {
			return cardBackgroundColor;
		}

		public int getStrokeColor() {
			return strokeColor;
		}

		public int getStrokeWidth() {
			return strokeWidth;
		}

		public int getSadImageVisibility() {
			return sadImageVisibility;
		}

		public String getSmallDate() {
			return smallDate;
		}

		public String getBigDate() {
			return bigDate;
		}

		public int getTypeFace() {
			return typeFace;
		}

		public String getHolidayText() {
			return holidayText;
		}

		public String getMoreText() {
			return moreText;
		}

		public String getId() {
			return id;
		}
	}
}
