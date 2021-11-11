package eu.andret.kalendarzswiatnietypowych.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import eu.andret.kalendarzswiatnietypowych.entity.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.Language;

public class HolidaysDBHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 3;

	public HolidaysDBHelper(final Context context) {
		super(context, "localHolidays", null, DATABASE_VERSION);
		context.openOrCreateDatabase(getDatabaseName(), Context.MODE_PRIVATE, null);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS holiday");
		db.execSQL("DROP TABLE IF EXISTS language");
		db.execSQL("DROP TABLE IF EXISTS metadata");
		db.execSQL("CREATE TABLE holiday (text TEXT, language VARCHAR(31), metadata INT, url TEXT, CONSTRAINT h_pk PRIMARY KEY (language, metadata))");
		db.execSQL("CREATE TABLE language (code VARCHAR(31) PRIMARY KEY, name VARCHAR(31))");
		db.execSQL("CREATE TABLE metadata (id INTEGER PRIMARY KEY, month INT, day INT, usual BOOLEAN)");
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		switch (oldVersion) {
			case 1:
			case 2:
				onCreate(db);
				break;
			default:
				throw new IllegalArgumentException();
		}
	}

	public boolean languageExists(final String code) {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor cursor = db.rawQuery("SELECT * FROM language WHERE code = ?", new String[]{code});
		if (cursor == null) {
			return false;
		}
		final boolean result = cursor.moveToFirst();
		cursor.close();
		return result;
	}

	@NonNull
	public Set<Language> getLanguages() {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor cursor = db.rawQuery("SELECT code, name FROM language", null);
		if (cursor == null) {
			return Collections.emptySet();
		}
		final Set<Language> languages = new TreeSet<>();
		while (cursor.moveToNext()) {
			final String code = cursor.getString(0);
			final String name = cursor.getString(1);
			languages.add(new Language(name, code));
		}
		cursor.close();
		return languages;
	}

	public void insertLanguage(@NonNull final Language language) {
		final SQLiteDatabase db = getWritableDatabase();
		final ContentValues values = new ContentValues();
		values.put("code", language.getCode());
		values.put("name", language.getName());
		db.insert("language", null, values);
		db.close();
	}

	public void update(final List<HolidayDay> list, final Language language) {
		final SQLiteDatabase dbWritable = getWritableDatabase();
		final SQLiteDatabase dbReadable = getReadableDatabase();
		dbWritable.beginTransaction();
		list.stream().forEach(holidayDay ->
				holidayDay.getHolidaysList(true).stream().forEach(holiday -> {
					updateMetadata(dbWritable, dbReadable, holiday.getMetadataId(), holidayDay.getDay(), holidayDay.getMonth(), holiday.isUsual());
					final String[] whereArgs = {language.getCode(), String.valueOf(holiday.getMetadataId())};
					final Cursor cursor = dbReadable.rawQuery("SELECT text FROM holiday WHERE language = ? AND metadata = ?", whereArgs);
					if (cursor.moveToFirst()) {
						final ContentValues values = new ContentValues();
						values.put("metadata", holiday.getMetadataId());
						values.put("language", language.getCode());
						values.put("text", holiday.getText());
						values.put("url", holiday.getUrl());
						dbWritable.update("holiday", values, "language = ? AND metadata = ?", whereArgs);
					} else {
						final String hSQL = "INSERT INTO holiday(text, metadata, language, url) VALUES (?, ?, ?, ?)";
						final SQLiteStatement hStatement = dbWritable.compileStatement(hSQL);
						hStatement.bindString(1, holiday.getText());
						hStatement.bindLong(2, holiday.getMetadataId());
						hStatement.bindString(3, language.getCode());
						hStatement.bindString(4, holiday.getUrl());
						hStatement.executeInsert();
					}
					cursor.close();
				}));
		dbWritable.setTransactionSuccessful();
		dbWritable.endTransaction();
		dbWritable.close();
		dbReadable.close();
	}

	private void updateMetadata(final SQLiteDatabase dbWritable, final SQLiteDatabase dbReadable, final int metadataId,
								final int day, final int month, final boolean usual) {
		final Cursor cursor = dbReadable.rawQuery("SELECT id FROM metadata WHERE id = ?", new String[]{String.valueOf(metadataId)});
		if (!cursor.moveToFirst()) {
			final String mSQL = "INSERT INTO metadata(id, day, month, usual) VALUES (?, ?, ?, ?)";
			final SQLiteStatement mStatement = dbWritable.compileStatement(mSQL);
			mStatement.bindLong(1, metadataId);
			mStatement.bindLong(2, day);
			mStatement.bindLong(3, month);
			mStatement.bindLong(4, usual ? 1 : 0);
			mStatement.executeInsert();
		} else {
			final ContentValues values = new ContentValues();
			values.put("day", day);
			values.put("month", month);
			values.put("usual", usual);
			dbWritable.update("metadata", values, "id = ?", new String[]{String.valueOf(metadataId)});
		}
		cursor.close();
	}

	public HolidayCalendar getAll(@NonNull final String languageCode) {
		final HolidayCalendar holidayCalendar = new HolidayCalendar();
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor cursor = db.rawQuery("SELECT H.text, H.url, M.id, M.day, M.month, M.usual FROM holiday H" +
						" INNER JOIN metadata M ON H.metadata = M.id WHERE H.language = ? ORDER BY M.month ASC, M.day ASC, M.usual DESC, H.text ASC",
				new String[]{languageCode});
		if (cursor != null && cursor.moveToFirst()) {
			do {
				final int day = cursor.getInt(3);
				final int month = cursor.getInt(4);
				final HolidayDay holidayDay = holidayCalendar.getOrCreateDay(month, day);
				holidayDay.addHoliday(new Holiday(
						cursor.getInt(2),
						cursor.getString(0),
						cursor.getInt(5) == 1,
						cursor.getString(1)));
				cursor.moveToNext();
			} while (!cursor.isAfterLast());
			cursor.close();
		}
		db.close();
		return holidayCalendar;
	}
}
