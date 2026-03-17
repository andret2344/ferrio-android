package eu.andret.kalendarzswiatnietypowych.fragment;

import android.content.Context;
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
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapterCompact;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapterDetailed;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapterSimple;
import eu.andret.kalendarzswiatnietypowych.adapter.DayClickListener;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.persistance.AppRepository;
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
	public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup parent,
			final Bundle savedInstanceState) {
		final View month = inflater.inflate(R.layout.fragment_month, parent, false);

		final RecyclerView recyclerView = month.findViewById(R.id.fragment_month_grid_days);
		recyclerView.setHasFixedSize(true);
		final AppRepository repository = ((FerrioApplication) requireActivity().getApplication()).getAppRepository();
		final DayClickListener listener = (DayClickListener) requireActivity();

		final Context context = requireContext();
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final boolean colorized = preferences.getBoolean(context.getString(R.string.settings_key_theme_colorized), false);
		final boolean includeUsual = preferences.getBoolean(context.getString(R.string.settings_key_usual_holidays), false);
		final String defaultMode = context.getString(R.string.month_view_mode_value_compact);
		final String mode = preferences.getString(context.getString(R.string.settings_key_month_view_mode), defaultMode);

		final ListAdapter<HolidayDayViewModel, ? extends RecyclerView.ViewHolder> dayAdapter = getHolidayDayAdapter(context, mode, listener);
		recyclerView.setAdapter(dayAdapter);

		holidayViewModel.getHolidayDayMap()
				.observe(getViewLifecycleOwner(), dayMap -> {
					final List<HolidayDayViewModel> dataSet = repository.getHolidayDaysInDateRange(dayMap, before, after)
							.stream()
							.map(holidayDay -> convert(holidayDay, colorized, includeUsual))
							.collect(Collectors.toList());
					dayAdapter.submitList(dataSet);
				});
		return month;
	}

	@NonNull
	private static ListAdapter<HolidayDayViewModel, ? extends RecyclerView.ViewHolder> getHolidayDayAdapter(
			@NonNull final Context context, @NonNull final String mode,
			@NonNull final DayClickListener listener) {
		if (mode.equals(context.getString(R.string.month_view_mode_value_compact))) {
			return new DayAdapterCompact(listener);
		}
		if (mode.equals(context.getString(R.string.month_view_mode_value_simple))) {
			return new DayAdapterSimple(listener);
		}
		return new DayAdapterDetailed(listener);
	}

	private LocalDate getBefore() {
		final LocalDate date = LocalDate.of(LocalDate.now().getYear(), currentMonth, 1);
		if (date.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
			return date.minusWeeks(1);
		}
		return date.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
	}

	@NonNull
	private HolidayDayViewModel convert(@NonNull final HolidayDay holidayDay,
			final boolean colorized, final boolean includeUsual) {
		final Context context = requireContext();
		final HolidayDayViewModel holidayDayViewModel = new HolidayDayViewModel(holidayDay);
		if (holidayDay.getMonth() != currentMonth) {
			holidayDayViewModel.cardBackgroundColor = ContextCompat.getColor(context, R.color.tile_other_month);
		} else if (colorized) {
			holidayDayViewModel.cardBackgroundColor = Util.randomizeColor(context, holidayDay.getSeed());
		} else {
			holidayDayViewModel.cardBackgroundColor = ContextCompat.getColor(context, R.color.tile_current_month);
		}

		final LocalDate now = LocalDate.now();
		holidayDayViewModel.strokeColor = ContextCompat.getColor(context, R.color.today_outline);
		if (holidayDay.getDay() == now.getDayOfMonth() && holidayDay.getMonth() == now.getMonthValue()) {
			holidayDayViewModel.strokeWidth = 4;
		}
		final List<Holiday> holidaysList = holidayDay.getHolidaysList(includeUsual);

		holidayDayViewModel.date = String.valueOf(holidayDay.getDay());
		holidayDayViewModel.holidayCount = holidaysList.size();
		if (holidaysList.isEmpty()) {
			holidayDayViewModel.sadImageVisibility = View.VISIBLE;
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
			holidayDayViewModel.holidayText = context.getString(R.string.ellipsis_text, result);
		}

		if (holidaysCountIndicator > 0) {
			holidayDayViewModel.moreText = context.getString(R.string.see_more, holidaysCountIndicator);
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
		private String date;
		private int holidayCount;
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

		public String getDate() {
			return date;
		}

		public int getHolidayCount() {
			return holidayCount;
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
