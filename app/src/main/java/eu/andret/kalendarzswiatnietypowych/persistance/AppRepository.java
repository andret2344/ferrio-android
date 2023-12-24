package eu.andret.kalendarzswiatnietypowych.persistance;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Room;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;

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
	private final AppDatabase database;

	private List<HolidayDay> fixedHolidaysCache;
	private List<FloatingHoliday> floatingHolidaysCache;

	public AppRepository(final Application application) {
		database = Room.databaseBuilder(application, AppDatabase.class, "uhc")
				.enableMultiInstanceInvalidation()
				.allowMainThreadQueries()
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

	public void insertHolidays(final List<Holiday> holidays) {
		AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> holidayDao.insertHolidays(holidays));
	}

	public void insertFloatingHoliday(final List<FloatingHoliday> floatingHolidays) {
		AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> holidayDao.insertFloatingHolidays(floatingHolidays));
	}

	public void insertHolidayDays(final List<HolidayDay> holidayDays) {
		AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> holidayDao.insertHolidayDays(holidayDays));
	}

	public void deleteAll() {
		database.clearAllTables();
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
			Log.d("UHC-AppRepository", "fixed: " + fixedHolidaysCache.size() + ", floating: " + floatingHolidaysCache.size());
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
		} else {
			Log.d("UHC-AppRepository", "fixed: null, floating: null");
		}
	}
}
