package eu.andret.kalendarzswiatnietypowych.persistence;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

@Dao
public interface AppDao {
	@Query("SELECT * FROM holiday ORDER BY month, day")
	LiveData<List<Holiday>> getAllHolidays();

	@Query("SELECT * FROM holiday WHERE id = :id LIMIT 1")
	LiveData<Holiday> getHolidayById(String id);

	@Query("SELECT * FROM holiday WHERE month = :month AND day = :day")
	List<Holiday> getHolidaysByDay(int month, int day);

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	void insertAll(List<Holiday> holidays);

	@Query("DELETE FROM holiday")
	void deleteAll();

	@Transaction
	default void replaceAll(final List<Holiday> holidays) {
		deleteAll();
		insertAll(holidays);
	}
}
