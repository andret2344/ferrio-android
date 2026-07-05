package eu.andret.kalendarzswiatnietypowych.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.FerrioApplication;
import eu.andret.kalendarzswiatnietypowych.R;
import eu.andret.kalendarzswiatnietypowych.activity.MainActivity;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapterCompact;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapterDetailed;
import eu.andret.kalendarzswiatnietypowych.adapter.DayAdapterSimple;
import eu.andret.kalendarzswiatnietypowych.adapter.DayClickListener;
import eu.andret.kalendarzswiatnietypowych.databinding.FragmentMonthBinding;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.persistence.AppRepository;
import eu.andret.kalendarzswiatnietypowych.persistence.HolidayViewModel;
import eu.andret.kalendarzswiatnietypowych.util.PreferenceHelper;
import eu.andret.kalendarzswiatnietypowych.util.Util;

public class MonthFragment extends Fragment {
	private static final int MAX_WORDS_COUNT = 4;
	private HolidayViewModel holidayViewModel;
	private int currentMonth;
	private LocalDate before;
	private LocalDate after;

	@Nullable
	private FragmentMonthBinding binding;
	@Nullable
	private ListAdapter<HolidayDayViewModel, ? extends RecyclerView.ViewHolder> dayAdapter;
	@Nullable
	private String currentMode;
	@NonNull
	private Map<Integer, HolidayDay> latestDayMap = Collections.emptyMap();
	// Stays false until the holiday map is delivered for the first time. Prevents binding the grid
	// against the initial empty map, which would flash a "sad face" placeholder on every tile
	// before the data has loaded.
	private boolean dataLoaded;

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
		binding = FragmentMonthBinding.inflate(inflater, parent, false);
		binding.fragmentMonthGridDays.setHasFixedSize(true);

		populateWeekdayHeader(binding.fragmentMonthWeekdayHeader);
		rebuildAdapter();

		holidayViewModel.getHolidayDayMap()
				.observe(getViewLifecycleOwner(), dayMap -> {
					latestDayMap = dayMap;
					dataLoaded = true;
					submitDataset();
				});
		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		final Context context = getContext();
		if (context == null) {
			return;
		}
		final String mode = new PreferenceHelper(context).getMonthViewMode();
		if (!mode.equals(currentMode)) {
			rebuildAdapter();
		}
		submitDataset();
	}

	@Override
	public void onDestroyView() {
		binding = null;
		dayAdapter = null;
		super.onDestroyView();
	}

	private void rebuildAdapter() {
		final Context context = requireContext();
		final PreferenceHelper preferences = new PreferenceHelper(context);
		currentMode = preferences.getMonthViewMode();
		dayAdapter = getHolidayDayAdapter(preferences, currentMode, (DayClickListener) requireActivity());
		if (binding != null) {
			binding.fragmentMonthGridDays.setAdapter(dayAdapter);
		}
	}

	private void submitDataset() {
		if (!dataLoaded) {
			return;
		}
		final ListAdapter<HolidayDayViewModel, ? extends RecyclerView.ViewHolder> adapter = dayAdapter;
		if (adapter == null) {
			return;
		}
		final Context context = getContext();
		if (context == null) {
			return;
		}
		final AppRepository repository = ((FerrioApplication) requireActivity().getApplication()).getAppRepository();
		final PreferenceHelper preferences = new PreferenceHelper(context);
		final boolean colorized = preferences.isThemeColorized();
		final boolean includeUsual = preferences.includeUsualHolidays();
		final boolean showAdult = preferences.showAdultContent();
		final List<HolidayDay> daysInRange = repository.getHolidayDaysInDateRange(latestDayMap, before, after);
		final List<HolidayDayViewModel> dataSet = daysInRange.stream()
				.map(holidayDay -> convert(holidayDay, colorized, includeUsual, showAdult))
				.collect(Collectors.toList());
		adapter.submitList(dataSet);
	}

	@NonNull
	private static ListAdapter<HolidayDayViewModel, ? extends RecyclerView.ViewHolder> getHolidayDayAdapter(
			@NonNull final PreferenceHelper preferences, @NonNull final String mode,
			@NonNull final DayClickListener listener) {
		if (mode.equals(preferences.monthViewModeValueCompact())) {
			return new DayAdapterCompact(listener);
		}
		if (mode.equals(preferences.monthViewModeValueSimple())) {
			return new DayAdapterSimple(listener);
		}
		return new DayAdapterDetailed(listener);
	}

	private void populateWeekdayHeader(@NonNull final LinearLayout header) {
		header.removeAllViews();
		final Context context = header.getContext();
		final Locale locale = Locale.getDefault();
		DayOfWeek day = DayOfWeek.MONDAY;
		for (int i = 0; i < 7; i++) {
			final TextView label = new TextView(context);
			final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
			label.setLayoutParams(params);
			label.setGravity(Gravity.CENTER);
			label.setText(day.getDisplayName(TextStyle.SHORT, locale));
			header.addView(label);
			day = day.plus(1);
		}
	}

	private LocalDate getBefore() {
		final LocalDate date = LocalDate.of(LocalDate.now(ZoneId.systemDefault()).getYear(), currentMonth, 1);
		if (date.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
			return date.minusWeeks(1);
		}
		return date.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
	}

	@NonNull
	private HolidayDayViewModel convert(@NonNull final HolidayDay holidayDay,
			final boolean colorized, final boolean includeUsual, final boolean showAdult) {
		final Context context = requireContext();
		final HolidayDayViewModel holidayDayViewModel = new HolidayDayViewModel(holidayDay);
		if (holidayDay.getMonth() != currentMonth) {
			holidayDayViewModel.cardBackgroundColor = ContextCompat.getColor(context, R.color.tile_other_month);
		} else if (colorized) {
			holidayDayViewModel.cardBackgroundColor = Util.randomizeColor(context, holidayDay.getSeed());
		} else {
			holidayDayViewModel.cardBackgroundColor = ContextCompat.getColor(context, R.color.tile_current_month);
		}

		final LocalDate now = LocalDate.now(ZoneId.systemDefault());
		holidayDayViewModel.strokeColor = ContextCompat.getColor(context, R.color.today_outline);
		if (holidayDay.getDay() == now.getDayOfMonth() && holidayDay.getMonth() == now.getMonthValue()) {
			holidayDayViewModel.strokeWidth = 4;
		}
		final List<Holiday> holidaysList = holidayDay.getHolidaysList(includeUsual, showAdult);

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
