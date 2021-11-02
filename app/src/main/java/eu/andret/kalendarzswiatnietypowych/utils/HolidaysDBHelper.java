package eu.andret.kalendarzswiatnietypowych.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.entity.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;
import eu.andret.kalendarzswiatnietypowych.entity.Language;

public class HolidaysDBHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 3;
	private static HolidaysDBHelper instance;
	private final Context context;

	private HolidaysDBHelper(final Context context) {
		super(context, "localHolidays", null, DATABASE_VERSION);
		this.context = context;
		context.openOrCreateDatabase(getDatabaseName(), Context.MODE_PRIVATE, null);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS uhc_holidays");
		db.execSQL("DROP TABLE IF EXISTS uhc_language");
		db.execSQL("DROP TABLE IF EXISTS uhc_metadata");
		db.execSQL("CREATE TABLE uhc_holidays (text TEXT, language VARCHAR(31), metadata INT, date_updated INT, url TEXT, CONSTRAINT h_pk PRIMARY KEY (language, metadata))");
		db.execSQL("CREATE TABLE uhc_language (code VARCHAR(31) PRIMARY KEY, name VARCHAR(31))");
		db.execSQL("CREATE TABLE uhc_metadata (id INTEGER PRIMARY KEY, month INT, day INT, type INT default NULL, usual BOOLEAN)");
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		switch (oldVersion) {
			case 1:
				db.execSQL("ALTER TABLE uhc_metadata ADD COLUMN favourite BOOLEAN");
				db.execSQL("UPDATE uhc_metadata SET favourite = 0");
				break;
			case 2:
				onCreate(db);
				break;
			default:
				throw new IllegalArgumentException();
		}
	}

	public boolean languageExists(final String code) {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor cursor = db.rawQuery("SELECT * FROM uhc_language WHERE code = ?", new String[]{code});
		if (cursor == null) {
			return false;
		}
		final boolean result = cursor.moveToFirst();
		cursor.close();
		return result;
	}

	public List<Language> getLanguages() {
		final SQLiteDatabase db = getReadableDatabase();
		final Cursor cursor = db.rawQuery("SELECT code, name FROM uhc_language", null);
		if (cursor == null) {
			return Collections.emptyList();
		}
		final List<Language> languages = new ArrayList<>();
		while (cursor.moveToNext()) {
			final String code = cursor.getString(0);
			final String name = cursor.getString(1);
			languages.add(new Language(name, code));
		}
		cursor.close();
		return languages;
	}

	public void insertLanguage(final Language language) {
		final SQLiteDatabase db = getWritableDatabase();
		final ContentValues values = new ContentValues();
		values.put("code", language.getCode());
		values.put("name", language.getName());
		db.insert("uhc_language", null, values);
		db.close();
	}

	public void update(final List<HolidayDay> list, final Language language) {
		final SQLiteDatabase dbWritable = getWritableDatabase();
		final SQLiteDatabase dbReadable = getReadableDatabase();
		final String mSQL = "INSERT INTO uhc_metadata('id', 'day', 'month', 'usual') VALUES (?, ?, ?, ?)";
		final String hSQL = "INSERT INTO uhc_holidays('text', 'metadata', 'language', 'date_updated', 'url') VALUES (?, ?, ?, ?, ?)";
		dbWritable.beginTransaction();
		final SQLiteStatement mStatement = dbWritable.compileStatement(mSQL);
		final SQLiteStatement hStatement = dbWritable.compileStatement(hSQL);
		for (final HolidayDay ho : list) {
			final int day = ho.getDay();
			final int month = ho.getMonth().getMonth().getValue();
			for (final Holiday h : ho.getHolidays()) {
				mStatement.bindLong(1, h.getMetadataId());
				mStatement.bindLong(2, day);
				mStatement.bindLong(3, month);
				mStatement.bindLong(4, h.isUsual() ? 1 : 0);
				Cursor cursor = dbReadable.rawQuery("SELECT id FROM uhc_metadata WHERE `id`=?", new String[]{String.valueOf(h.getMetadataId())});
				if (cursor == null || !cursor.moveToFirst()) {
					mStatement.executeInsert();
				}
				if (cursor != null) {
					cursor.close();
				}
				mStatement.clearBindings();
				final ContentValues values = new ContentValues();
				hStatement.bindString(1, h.getText());
				hStatement.bindLong(2, h.getMetadataId());
				hStatement.bindString(3, language.getCode());
				hStatement.bindLong(4, new Date().getTime() / 1000);
				hStatement.bindString(5, h.getUrl());
				values.put("metadata", h.getMetadataId());
				values.put("language", language.getCode());
				values.put("text", h.getText());
				values.put("date_updated", new Date().getTime() / 1000);
				values.put("url", h.getUrl());
				cursor = dbReadable.rawQuery("SELECT text FROM uhc_holidays WHERE language = ? AND metadata = ?", new String[]{language.getCode(), "" + h.getMetadataId()});
				if (cursor.moveToFirst()) {
					dbWritable.update("uhc_holidays", values, "language = ? AND metadata = ?", new String[]{language.getCode(), "" + h.getMetadataId()});
				} else {
					hStatement.executeInsert();
				}
				cursor.close();
			}
		}
		dbWritable.setTransactionSuccessful();
		dbWritable.endTransaction();
		final Cursor cursor = dbReadable.rawQuery("SELECT COUNT(*) FROM uhc_metadata", null);// + " WHERE `language`=?", new
		cursor.moveToFirst();
		dbWritable.close();
		dbReadable.close();
		cursor.close();
	}

	public synchronized void reload(final String language) {
		if (language == null) {
			return;
		}
		final SQLiteDatabase db = getReadableDatabase();
		db.beginTransaction();
		final Cursor cursor = db.rawQuery("SELECT H.text AS txt, H.url AS url, M.id AS mid, M.day AS day, M.month AS month, M.usual AS usual FROM uhc_holidays H" +
				" INNER JOIN uhc_metadata M ON H.metadata = M.id WHERE H.language = ? ORDER BY M.month ASC, M.day ASC, M.usual DESC, H.text ASC", new String[]{language});
		int day = 1;
		int month = 1;
		if (cursor != null && cursor.moveToFirst()) {
			HolidayDay hd = null;
			do {
				final int currDay = cursor.getInt(cursor.getColumnIndex("day"));
				final int currMonth = cursor.getInt(cursor.getColumnIndex("month"));
				if (month != currMonth || day != currDay || hd == null) {
					hd = HolidayCalendar.getInstance(context).getMonth(currMonth).new HolidayDay(currDay, null);
				}
				hd.new Holiday(cursor.getInt(cursor.getColumnIndex("mid")), cursor.getString(cursor.getColumnIndex("txt")), cursor.getInt(cursor.getColumnIndex("usual")) == 1, cursor.getString(cursor.getColumnIndex("link")));
				cursor.moveToNext();
				day = currDay;
				month = currMonth;
			} while (!cursor.isAfterLast());
			cursor.close();
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}

	public static HolidaysDBHelper getInstance(final Context context) {
		if (instance == null) {
			instance = new HolidaysDBHelper(context);
		}
		return instance;
	}
}
