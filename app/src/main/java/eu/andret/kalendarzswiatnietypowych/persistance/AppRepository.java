package eu.andret.kalendarzswiatnietypowych.persistance;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;
import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.entity.FloatingHoliday;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;

public class AppRepository {
	private final AppDao holidayDao;

	public AppRepository(final Application application) {
		final AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "uhc")
				.enableMultiInstanceInvalidation()
				.build();
		holidayDao = db.appDao();
	}

	public LiveData<List<HolidayDay>> getHolidayDays(final int monthFrom, final int dayFrom, final int monthTo, final int dayTo) {
		return holidayDao.getHolidayDays(monthFrom, dayFrom, monthTo, dayTo);
	}

	public LiveData<Optional<HolidayDay>> getHolidayDay(final int month, final int day) {
		return holidayDao.getHolidayDay(month, day);
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
}
