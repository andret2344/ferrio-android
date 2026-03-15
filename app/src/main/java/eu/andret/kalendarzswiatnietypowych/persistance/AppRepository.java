package eu.andret.kalendarzswiatnietypowych.persistance;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Room;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.util.ApiClient;
import eu.andret.kalendarzswiatnietypowych.util.LoadState;

public class AppRepository {
	private final AppDao appDao;
	private final LiveData<Map<Integer, HolidayDay>> holidayDayMap;
	private final LiveData<List<HolidayDay>> allHolidayDays;
	private final HolidayRemoteMediator remoteMediator;

	public AppRepository(@NonNull final Application application,
			@NonNull final ApiClient apiClient) {
		final AppDatabase database = Room.databaseBuilder(application, AppDatabase.class, "ferrio")
				.fallbackToDestructiveMigrationFrom(true, 1, 2, 3)
				.enableMultiInstanceInvalidation()
				.build();
		appDao = database.appDao();

		final LiveData<List<Holiday>> allHolidays = appDao.getAllHolidays();
		holidayDayMap = Transformations.map(allHolidays, this::groupByDayMap);
		allHolidayDays = Transformations.map(allHolidays, holidays -> {
			final Map<Integer, HolidayDay> grouped = groupByDayMap(holidays);
			return new ArrayList<>(grouped.values());
		});
		remoteMediator = new HolidayRemoteMediator(appDao, apiClient);
	}

	public void refresh() {
		remoteMediator.refresh();
	}

	@NonNull
	public LiveData<LoadState> getLoadState() {
		return remoteMediator.getLoadState();
	}

	public LiveData<List<HolidayDay>> getHolidayDays(final int monthFrom, final int dayFrom,
			final int monthTo, final int dayTo) {
		return Transformations.map(holidayDayMap, map -> {
			final List<HolidayDay> result = new ArrayList<>();
			for (final HolidayDay holidayDay : map.values()) {
				if (isWithinRange(holidayDay, monthFrom, dayFrom, monthTo, dayTo)) {
					result.add(holidayDay);
				}
			}
			return result;
		});
	}

	public LiveData<Optional<HolidayDay>> getHolidayDay(final int month, final int day) {
		return Transformations.map(holidayDayMap, map ->
				Optional.ofNullable(map.get(dayKey(month, day))));
	}

	public LiveData<Map<Integer, HolidayDay>> getHolidayDayMap() {
		return holidayDayMap;
	}

	public LiveData<List<HolidayDay>> getAllHolidayDays() {
		return allHolidayDays;
	}

	@NonNull
	public List<Holiday> getHolidaysByDaySync(final int month, final int day) {
		return appDao.getHolidaysByDay(month, day);
	}

	public LiveData<Holiday> getHoliday(@NonNull final String id) {
		return appDao.getHolidayById(id);
	}

	@NonNull
	public List<HolidayDay> getHolidayDaysInDateRange(
			@NonNull final Map<Integer, HolidayDay> dayMap,
			@NonNull final LocalDate begin,
			@NonNull final LocalDate end) {
		final List<HolidayDay> result = new ArrayList<>();
		for (LocalDate date = begin; date.until(end, ChronoUnit.DAYS) > 0; date = date.plusDays(1)) {
			final int key = dayKey(date.getMonthValue(), date.getDayOfMonth());
			result.add(dayMap.getOrDefault(key, new HolidayDay(date.getMonthValue(), date.getDayOfMonth())));
		}
		return result;
	}

	@NonNull
	private Map<Integer, HolidayDay> groupByDayMap(@NonNull final List<Holiday> holidays) {
		final Map<Integer, HolidayDay> grouped = new LinkedHashMap<>();
		for (final Holiday holiday : holidays) {
			final int key = dayKey(holiday.getMonth(), holiday.getDay());
			grouped.computeIfAbsent(key, k -> new HolidayDay(holiday.getMonth(), holiday.getDay()))
					.addHoliday(holiday);
		}
		return grouped;
	}

	private static int dayKey(final int month, final int day) {
		return month * 100 + day;
	}

	private boolean isWithinRange(final HolidayDay holidayDay, final int monthFrom,
			final int dayFrom, final int monthTo, final int dayTo) {
		final int holidayDate = dayKey(holidayDay.getMonth(), holidayDay.getDay());
		final int fromDate = dayKey(monthFrom, dayFrom);
		final int toDate = dayKey(monthTo, dayTo);

		if (fromDate > toDate) {
			return holidayDate >= fromDate || holidayDate <= toDate;
		}
		return holidayDate >= fromDate && holidayDate <= toDate;
	}
}
