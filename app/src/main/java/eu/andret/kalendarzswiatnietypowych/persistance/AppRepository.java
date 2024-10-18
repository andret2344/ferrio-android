package eu.andret.kalendarzswiatnietypowych.persistance;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Room;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.entity.FloatingHoliday;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.UnusualCalendar;

public class AppRepository {
	private final AppDao holidayDao;
	private final MediatorLiveData<List<HolidayDay>> mergedHolidays = new MediatorLiveData<>();

	public AppRepository(final Application application) {
		final AppDatabase database = Room.databaseBuilder(application, AppDatabase.class, "uhc")
				.enableMultiInstanceInvalidation()
				.build();
		holidayDao = database.appDao();

		// Initialize mergedHolidays
		final LiveData<List<HolidayDay>> fixedHolidays = holidayDao.getAllHolidayDays();
		final LiveData<List<FloatingHoliday>> floatingHolidays = holidayDao.getAllFloatingHolidays();

		mergedHolidays.addSource(fixedHolidays, fixed -> mergeHolidays(fixed, floatingHolidays.getValue()));
		mergedHolidays.addSource(floatingHolidays, floating -> mergeHolidays(fixedHolidays.getValue(), floating));
	}

	public LiveData<List<HolidayDay>> getHolidayDays(final int monthFrom, final int dayFrom, final int monthTo, final int dayTo) {
		return Transformations.map(mergedHolidays, originalList -> originalList.stream()
				.filter(holidayDay -> isWithinRange(holidayDay, monthFrom, dayFrom, monthTo, dayTo))
				.collect(Collectors.toList()));
	}

	public LiveData<Optional<HolidayDay>> getHolidayDay(final int month, final int day) {
		return Transformations.map(mergedHolidays, originalList -> originalList.stream()
				.filter(holidayDay -> holidayDay.getMonth() == month && holidayDay.getDay() == day)
				.findAny());
	}

	public LiveData<List<HolidayDay>> getAllHolidayDays() {
		return mergedHolidays;
	}

	public LiveData<Holiday> getHoliday(final int id) {
		return Transformations.map(mergedHolidays, originalList -> originalList.stream()
				.map(HolidayDay::getHolidays)
				.flatMap(Collection::stream)
				.filter(holiday -> holiday.getId() == id)
				.findAny()
				.orElse(null));
	}

	public void updateCalendarData(@NonNull final UnusualCalendar calendar) {
		Executors.newSingleThreadExecutor().execute(() -> {
			holidayDao.deleteAllHolidays();
			holidayDao.deleteAllHolidayDays();
			holidayDao.deleteAllFloatingHolidays();
			calendar.getFixed()
					.stream()
					.map(HolidayDay::getHolidays)
					.flatMap(Collection::stream)
					.forEach(holidayDao::insertHoliday);
			calendar.getFixed().forEach(holidayDao::insertHolidayDay);
			calendar.getFloating().forEach(holidayDao::insertFloatingHoliday);
		});
	}

	private void mergeHolidays(final List<HolidayDay> fixedHolidays, final List<FloatingHoliday> floatingHolidays) {
		if (fixedHolidays != null && floatingHolidays != null) {
			Executors.newSingleThreadExecutor().execute(() -> {
				final List<HolidayDay> allHolidays = new ArrayList<>();

				// Deep copy fixedHolidays to avoid modifying the original list
				for (final HolidayDay fixedHoliday : fixedHolidays) {
					allHolidays.add(new HolidayDay(fixedHoliday));
				}

				// Process floating holidays
				try (final Context context = Context.enter()) {
					context.setOptimizationLevel(-1);
					final Scriptable scope = context.initStandardObjects();

					for (final FloatingHoliday floatingHoliday : floatingHolidays) {
						try {
							final Object calculated = context.evaluateString(scope, floatingHoliday.getScript(), "<cmd>", 1, null);
							if (calculated != null) {
								final String[] split = calculated.toString().split("\\.");
								final int day = Integer.parseInt(split[0]);
								final int month = Integer.parseInt(split[1]);
								final HolidayDay holidayDay = findOrCreateHolidayDay(allHolidays, month, day);
								holidayDay.addHoliday(new Holiday(floatingHoliday));
							}
						} catch (final EcmaError ex) {
							Log.e("AppRepository", "Error evaluating script for floating holiday ID " + floatingHoliday.getId(), ex);
						}
					}
				}

				mergedHolidays.postValue(allHolidays);
			});
		}
	}

	private HolidayDay findOrCreateHolidayDay(final List<HolidayDay> holidays, final int month, final int day) {
		for (final HolidayDay holidayDay : holidays) {
			if (holidayDay.getMonth() == month && holidayDay.getDay() == day) {
				return holidayDay;
			}
		}
		// If not found, create a new one
		final HolidayDay newHolidayDay = new HolidayDay(month, day, new ArrayList<>());
		holidays.add(newHolidayDay);
		return newHolidayDay;
	}

	private boolean isWithinRange(final HolidayDay holidayDay, final int monthFrom, final int dayFrom, final int monthTo, final int dayTo) {
		final int holidayDate = holidayDay.getMonth() * 100 + holidayDay.getDay();
		final int fromDate = monthFrom * 100 + dayFrom;
		final int toDate = monthTo * 100 + dayTo;

		if (fromDate > toDate) {
			return holidayDate >= fromDate || holidayDate <= toDate;
		}
		return holidayDate >= fromDate && holidayDate <= toDate;
	}
}
