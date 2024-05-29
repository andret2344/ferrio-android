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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.andret.kalendarzswiatnietypowych.entity.FloatingHoliday;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.UnusualCalendar;

public class AppRepository {
	private final AppDao holidayDao;
	private final MediatorLiveData<List<HolidayDay>> mergedHolidays = new MediatorLiveData<>();

	private List<HolidayDay> fixedHolidaysCache;
	private List<FloatingHoliday> floatingHolidaysCache;

	public AppRepository(final Application application) {
		final AppDatabase database = Room.databaseBuilder(application, AppDatabase.class, "uhc")
				.enableMultiInstanceInvalidation()
				.build();
		holidayDao = database.appDao();
		loadHolidays();
	}

	public LiveData<List<HolidayDay>> getHolidayDays(final int monthFrom, final int dayFrom, final int monthTo, final int dayTo) {
		return Transformations.map(mergedHolidays, originalList -> originalList.stream()
				.filter(holidayDay -> holidayDay.getMonth() == monthFrom && holidayDay.getDay() >= dayFrom
						|| holidayDay.getMonth() > (monthFrom == 12 ? 0 : monthFrom) && holidayDay.getMonth() < (monthTo == 1 ? 13 : monthTo)
						|| holidayDay.getMonth() == monthTo && holidayDay.getDay() <= dayTo)
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
		Log.d("UHC-Repository", "Update start");
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
		Log.d("UHC-Repository", "Update finished");
	}

	private void loadHolidays() {
		final LiveData<List<HolidayDay>> fixedHolidays = holidayDao.getAllHolidayDays();
		final LiveData<List<FloatingHoliday>> floatingHolidays = holidayDao.getAllFloatingHolidays();

		mergedHolidays.addSource(fixedHolidays, fixed -> {
			fixedHolidaysCache = fixed;
			mergeHolidays();
		});

		mergedHolidays.addSource(floatingHolidays, floating -> {
			floatingHolidaysCache = floating;
			mergeHolidays();
		});
	}

	private void mergeHolidays() {
		if (fixedHolidaysCache != null && floatingHolidaysCache != null) {
			floatingHolidaysCache.forEach(floatingHoliday -> {
				try (final Context context = Context.enter()) {
					context.setOptimizationLevel(-1);
					final Scriptable scope = context.initStandardObjects();
					final Object calculated = context.evaluateString(scope, floatingHoliday.getScript(), "<cmd>", 1, null);
					if (calculated != null) {
						final String[] split = calculated.toString().split("\\.");
						UnusualCalendar.getOrCreateDay(fixedHolidaysCache, Integer.parseInt(split[1]), Integer.parseInt(split[0]))
								.addHoliday(new Holiday(floatingHoliday));
					}
				} catch (final EcmaError ex) {
					// do nothing, ignore the holiday
				}
			});
			mergedHolidays.setValue(fixedHolidaysCache);
		}
	}
}
