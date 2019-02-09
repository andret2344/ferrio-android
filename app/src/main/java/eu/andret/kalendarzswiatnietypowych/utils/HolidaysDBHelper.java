package eu.andret.kalendarzswiatnietypowych.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay;
import eu.andret.kalendarzswiatnietypowych.utils.HolidayCalendar.HolidayMonth.HolidayDay.Holiday;

public class HolidaysDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "localHolidays";
    private static final String TABLE_HOLIDAYS = "uhc_holidays";
    private static final String TABLE_LANGUAGE = "uhc_language";
    private static final String TABLE_METADATA = "uhc_metadata";
    private static HolidaysDBHelper instance;
    private final Context context;
    private static int usersCount = 0;

    private HolidaysDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOLIDAYS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LANGUAGE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_METADATA);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HOLIDAYS + " (text TEXT, language INT, metadata INT, date_updated INT, external_link TEXT, CONSTRAINT h_pk PRIMARY KEY (language, metadata))");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_LANGUAGE + " (id INTEGER PRIMARY KEY, name VARCHAR(32))");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_METADATA + " (id INTEGER PRIMARY KEY, month INT, day INT, type INT default NULL, usual BOOLEAN)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE " + TABLE_METADATA + " ADD COLUMN favourite BOOLEAN");
                db.execSQL("UPDATE " + TABLE_METADATA + " SET favourite=0");
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public long getLastUpdate() {
        usersCount++;
        SQLiteDatabase db = getReadableDatabase();
        usersCount++;
        Cursor cursor = db.rawQuery("SELECT date_updated FROM " + TABLE_HOLIDAYS + " ORDER BY date_updated DESC", new String[]{});
        long result = -1;
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getLong(0);
        }
        usersCount--;
        if (usersCount == 0) {
            db.close();
        }
        return result;
    }

    public void insertLanguage(LanguagePacket lp) {
        usersCount++;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", lp.getId());
        values.put("name", lp.getLocale().getLanguage());
        db.insert(TABLE_LANGUAGE, null, values);
        usersCount--;
        if (usersCount == 0) {
            db.close();
        }
    }

    public void update(List<HolidayDay> list, LanguagePacket language) {
        usersCount++;
        SQLiteDatabase db_w = getWritableDatabase();
        SQLiteDatabase db_r = getReadableDatabase();
        String mSQL = "INSERT INTO " + TABLE_METADATA + "('id', 'day', 'month', 'usual') VALUES (?, ?, ?, ?)";
        String hSQL = "INSERT INTO " + TABLE_HOLIDAYS + "('text', 'metadata', 'language', 'date_updated', 'external_link') VALUES (?, ?, ?, ?, ?)";
        db_w.beginTransaction();
        SQLiteStatement mStatement = db_w.compileStatement(mSQL);
        SQLiteStatement hStatement = db_w.compileStatement(hSQL);
        for (HolidayDay ho : list) {
            int day = ho.getDay();
            int month = ho.getMonth().getMonth();
            for (Holiday h : ho.getHolidays()) {
                mStatement.bindLong(1, h.getMetadataId());
                mStatement.bindLong(2, day);
                mStatement.bindLong(3, month);
                mStatement.bindLong(4, h.isUsual() ? 1 : 0);

                Cursor cursor = db_r.rawQuery("SELECT id FROM " + TABLE_METADATA + " WHERE `id`=?", new String[]{String.valueOf(h.getMetadataId())});
                if (cursor == null || !cursor.moveToFirst()) {
                    mStatement.executeInsert();
                }
                mStatement.clearBindings();
                cursor.close();
                ContentValues values = new ContentValues();
                hStatement.bindString(1, h.getText());
                hStatement.bindLong(2, h.getMetadataId());
                hStatement.bindLong(3, language.getId());
                hStatement.bindLong(4, new Date().getTime() / 1000);
                hStatement.bindString(5, h.getExternalLink());
                values.put("metadata", h.getMetadataId());
                values.put("language", language.getId());
                values.put("text", h.getText());
                values.put("date_updated", new Date().getTime() / 1000);
                values.put("external_link", h.getExternalLink());
                cursor = db_r.rawQuery("SELECT text FROM " + TABLE_HOLIDAYS + " WHERE `language`=? AND `metadata`=?", new String[]{"" + language.getId(), "" + h.getMetadataId()});
                if (cursor.moveToFirst()) {
                    db_w.update(TABLE_HOLIDAYS, values, "language=? AND metadata=?", new String[]{"" + language.getId(), "" + h.getMetadataId()});
                } else {
                    hStatement.executeInsert();
                }
                cursor.close();
            }
        }
        db_w.setTransactionSuccessful();
        db_w.endTransaction();
        Cursor cursor = db_r.rawQuery("SELECT COUNT(*) FROM " + TABLE_METADATA, null);// + " WHERE `language`=?", new
        cursor.moveToFirst();
        usersCount--;
        if (usersCount == 0) {
            db_w.close();
            db_r.close();
        }
    }

    public void test1() {
        List<Long> list = new ArrayList<>();
        long time = 0;
        SQLiteDatabase db = getReadableDatabase();
        for (int i = 0; i < 100; i++) {
            long start = System.nanoTime();
		    db.rawQuery("SELECT * FROM holidays WHERE id=?", new String[]{String.valueOf(i)});
		    time += System.nanoTime() - start;
		    list.add(System.nanoTime() - start);
        }
        Log.d("AC", "max: " + Collections.max(list));
        Log.d("AC", "min: " + Collections.min(list));
        Log.d("AC", "avg: " + time/100);
    }

    public synchronized void reload(int language) {
        if (language == -1) {
            return;
        }
        usersCount++;
        SQLiteDatabase db = getReadableDatabase();
        db.beginTransaction();
        String TEXT = "H" + ".text";
        String LINK = "H" + ".external_link";
        String DAY = "M" + ".day";
        String MID = "M" + ".id";
        String MONTH = "M" + ".month";
        String USUAL = "M" + ".usual";
        String SELECT = "SELECT " + TEXT + " AS txt, " + LINK + " AS link, " + MID + " AS mid, " + DAY + " AS day, " + MONTH + " AS month, " + USUAL + " AS usual ";
        String FROM = "FROM " + TABLE_HOLIDAYS + " H";
        String WHERE = "WHERE H.language=" + language + " ";
        String ORDER = "ORDER BY M.month ASC, M.day ASC, M.usual DESC, H.text ASC";
        String sql = SELECT + FROM + " INNER JOIN " + TABLE_METADATA + " M ON H.metadata=M.id " + WHERE + ORDER;
        Cursor cursor = db.rawQuery(sql, null);
        int day = 1;
        int month = 1;
        if (cursor != null && cursor.moveToFirst()) {
            HolidayDay hd = null;
            do {
                int currDay = cursor.getInt(cursor.getColumnIndex("day"));
                int currMonth = cursor.getInt(cursor.getColumnIndex("month"));
                if (month != currMonth || day != currDay || hd == null) {
                    hd = HolidayCalendar.getInstance(context).getMonth(currMonth).new HolidayDay(currDay, null);
                }
                hd.new Holiday(cursor.getInt(cursor.getColumnIndex("mid")), cursor.getString(cursor.getColumnIndex("txt")), cursor.getInt(cursor.getColumnIndex("usual")) == 1, cursor.getString(cursor.getColumnIndex("link")));
                cursor.moveToNext();
                day = currDay;
                month = currMonth;
            } while (!cursor.isAfterLast());
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        usersCount--;
        if (usersCount == 0) {
            db.close();
        }
    }

    public List<LanguagePacket> getExistingLanguages() {
        usersCount++;
        SQLiteDatabase db = getReadableDatabase();
        List<LanguagePacket> result = new ArrayList<LanguagePacket>();
        Cursor c = db.rawQuery("SELECT L.id AS id, L.name AS name, COUNT(H.text) AS c FROM " + TABLE_LANGUAGE + " L INNER JOIN " + TABLE_HOLIDAYS + " H ON L.id=H.language GROUP BY H.language", null);
        if (c != null && c.moveToFirst()) {
            while (!c.isAfterLast()) {
                result.add(new LanguagePacket(c.getInt(0), new Locale(c.getString(1)), c.getInt(2), true, false));
                c.moveToNext();
            }
        }
        c.close();
        usersCount--;
        if (usersCount == 0) {
            db.close();
        }
        return result;
    }

    public List<Integer> getExistingLanguagesIds() {
        usersCount++;
        SQLiteDatabase db = getReadableDatabase();
        List<Integer> result = new ArrayList<Integer>();
        Cursor c = db.rawQuery("SELECT id FROM " + TABLE_LANGUAGE, null);
        if (c != null && c.moveToFirst()) {
            while (!c.isAfterLast()) {
                result.add(c.getInt(0));
                c.moveToNext();
            }
        }
        c.close();
        usersCount--;
        if (usersCount == 0) {
            db.close();
        }
        return result;
    }

    public static HolidaysDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new HolidaysDBHelper(context);
        }
        return instance;
    }

    public Date getLastUpdateDate(LanguagePacket lp) {
        usersCount++;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(date_updated) FROM " + TABLE_HOLIDAYS + " WHERE language=?", new String[]{"" + lp.getId()});
        long result = -1;
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getLong(0);
        }
        usersCount--;
        if (usersCount == 0) {
            db.close();
        }
        return new Date(result);
    }

    public void remove(LanguagePacket languagePacket) {
        usersCount++;
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("DELETE FROM " + TABLE_HOLIDAYS + " WHERE language=" + languagePacket.getId(), null);
        c.moveToFirst();
        c.close();
        c = db.rawQuery("DELETE FROM " + TABLE_LANGUAGE + " WHERE id=" + languagePacket.getId(), null);
        c.moveToFirst();
        c.close();
        usersCount--;
        if (usersCount == 0) {
            db.close();
        }
    }
}
