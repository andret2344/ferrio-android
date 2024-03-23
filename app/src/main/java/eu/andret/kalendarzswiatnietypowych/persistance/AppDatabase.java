package eu.andret.kalendarzswiatnietypowych.persistance;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import eu.andret.kalendarzswiatnietypowych.entity.FloatingHoliday;
import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;

@Database(entities = {Holiday.class, FloatingHoliday.class, HolidayDay.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
	public abstract AppDao appDao();
}
