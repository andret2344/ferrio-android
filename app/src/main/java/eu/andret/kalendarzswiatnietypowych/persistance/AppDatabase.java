package eu.andret.kalendarzswiatnietypowych.persistance;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.andret.kalendarzswiatnietypowych.entity.FloatingHoliday;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;

@Database(entities = {Holiday.class, FloatingHoliday.class, HolidayDay.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
	public static final ExecutorService DATABASE_WRITE_EXECUTOR = Executors.newFixedThreadPool(4);

	public abstract AppDao appDao();
}
