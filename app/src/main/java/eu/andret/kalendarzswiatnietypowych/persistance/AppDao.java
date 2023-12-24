package eu.andret.kalendarzswiatnietypowych.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

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

	@Query("SELECT * FROM holiday_day")
	LiveData<List<HolidayDay>> getAllHolidayDays();

	@Query("SELECT * FROM floating_holiday")
	LiveData<List<FloatingHoliday>> getAllFloatingHolidays();
}
