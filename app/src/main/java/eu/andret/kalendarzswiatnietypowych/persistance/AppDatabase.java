package eu.andret.kalendarzswiatnietypowych.persistance;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import eu.andret.kalendarzswiatnietypowych.entity.Holiday;

@Database(entities = {Holiday.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
	public abstract AppDao appDao();
}
