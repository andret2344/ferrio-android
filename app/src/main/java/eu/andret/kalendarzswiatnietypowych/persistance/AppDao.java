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
	@Query("SELECT * FROM holiday_day")
	LiveData<List<HolidayDay>> getAllHolidayDays();

	@Query("SELECT * FROM floating_holiday")
	LiveData<List<FloatingHoliday>> getAllFloatingHolidays();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertHolidayDay(HolidayDay day);

	@Query("DELETE FROM holiday_day")
	void deleteAllHolidayDays();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertHoliday(Holiday holiday);

	@Query("DELETE FROM holiday")
	void deleteAllHolidays();

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertFloatingHoliday(FloatingHoliday floatingHoliday);

	@Query("DELETE FROM floating_holiday")
	void deleteAllFloatingHolidays();
}
