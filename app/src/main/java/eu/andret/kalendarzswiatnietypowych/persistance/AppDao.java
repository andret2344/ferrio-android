package eu.andret.kalendarzswiatnietypowych.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import java.util.Optional;

import eu.andret.kalendarzswiatnietypowych.entity.FloatingHoliday;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;

@Dao
public interface AppDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertHolidays(List<Holiday> holidays);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertFloatingHolidays(List<FloatingHoliday> floatingHolidays);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertHolidayDays(List<HolidayDay> holidayDays);

	@Query("SELECT * FROM holiday_day WHERE month == :monthFrom AND day >= :dayFrom OR month > :monthFrom AND month < :monthTo OR month == :monthTo AND day <= :dayTo")
	LiveData<List<HolidayDay>> getHolidayDays(int monthFrom, int dayFrom, int monthTo, int dayTo);

	@Query("SELECT * FROM holiday_day WHERE month == :month AND day == :day")
	LiveData<Optional<HolidayDay>> getHolidayDay(int month, int day);
}
