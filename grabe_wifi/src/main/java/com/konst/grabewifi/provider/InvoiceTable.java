package com.konst.grabewifi.provider;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class InvoiceTable {
    private final Context mContext;
    final ContentResolver contentResolver;
    public static final int READY = 1;
    public static final int UNREADY = 0;

    public static final String TABLE = "invoiceTable";

    public static final String KEY_ID               = BaseColumns._ID;
    public static final String KEY_DATE_CREATE      = "date";
    public static final String KEY_TIME_CREATE      = "time";
    public static final String KEY_NAME_AUTO        = "nameAuto";
    public static final String KEY_TOTAL_WEIGHT     = "totalWeight";
    public static final String KEY_IS_READY         = "checkIsReady";
    public static final String KEY_IS_CLOUD         = "isCloud";
    public static final String KEY_DATA0            = "data0";
    public static final String KEY_DATA1            = "data1";

    /** Стадии весового чека. */
    public enum State{
        /** Первое взвешивание. */
        CHECK_FIRST("ПЕРВОЕ"),
        /** Второе взвешивание. */
        CHECK_SECOND("ВТОРОЕ"),
        /** Предварительный. */
        CHECK_PRELIMINARY("ПРЕДВАРИТЕЛЬНЫЙ"),
        /** Готовый. */
        CHECK_READY("ГОТОВЫЙ"),
        /** Сохранен на сервере. */
        CHECK_ON_SERVER("НА СЕРВЕРЕ");

        public String getText() {
            return text;
        }

        private final String text;

        State(String t) {
            text = t;
        }
    }

    private static final String[] All_COLUMN_TABLE = {
            KEY_ID,
            KEY_DATE_CREATE,
            KEY_TIME_CREATE,
            KEY_NAME_AUTO,
            KEY_TOTAL_WEIGHT,
            KEY_IS_READY,
            KEY_IS_CLOUD,
            KEY_DATA0,
            KEY_DATA1};

    public static final String TABLE_CREATE = "create table "
            + TABLE + " ("
            + KEY_ID + " integer primary key autoincrement, "
            + KEY_DATE_CREATE + " text,"
            + KEY_TIME_CREATE + " text,"
            + KEY_NAME_AUTO + " text,"
            + KEY_TOTAL_WEIGHT + " integer,"
            + KEY_IS_READY + " integer,"
            + KEY_IS_CLOUD + " integer,"
            + KEY_DATA0 + " text,"
            + KEY_DATA1 + " text );";


    private static final Uri CONTENT_URI = Uri.parse("content://" + CraneScalesBaseProvider.AUTHORITY + '/' + TABLE);

    public InvoiceTable(Context context) {
        mContext = context;
        contentResolver = mContext.getContentResolver();
    }

    public Uri insertNewEntry(ContentValues value) {
        value.put(KEY_NAME_AUTO, "");
        value.put(KEY_TOTAL_WEIGHT, 0);
        value.put(KEY_IS_READY, UNREADY);
        value.put(KEY_IS_CLOUD, UNREADY);
        value.put(KEY_DATA0, "");
        value.put(KEY_DATA1, "");
        return contentResolver.insert(CONTENT_URI, value);
    }

    public Uri insertNewEntry(String key, String value) {
        ContentValues newTaskValues = new ContentValues();
        newTaskValues.put(key, value);
        return contentResolver.insert(CONTENT_URI, newTaskValues);
    }

    public Uri insertNewEntry() {
        ContentValues newTaskValues = new ContentValues();
        Date date = new Date();
        newTaskValues.put(KEY_DATE_CREATE, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date));
        newTaskValues.put(KEY_TIME_CREATE, new SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(date));
        newTaskValues.put(KEY_NAME_AUTO, "");
        newTaskValues.put(KEY_TOTAL_WEIGHT, 0);
        newTaskValues.put(KEY_IS_READY, 0);
        newTaskValues.put(KEY_DATA0, "");
        newTaskValues.put(KEY_DATA1, "");
        return contentResolver.insert(CONTENT_URI, newTaskValues);
    }

    public Uri insertNewEntry(String nameAuto) {
        ContentValues newTaskValues = new ContentValues();
        Date date = new Date();
        newTaskValues.put(KEY_DATE_CREATE, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date));
        newTaskValues.put(KEY_TIME_CREATE, new SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(date));
        newTaskValues.put(KEY_NAME_AUTO, nameAuto);
        newTaskValues.put(KEY_TOTAL_WEIGHT, 0);
        newTaskValues.put(KEY_IS_READY, 0);
        newTaskValues.put(KEY_DATA0, "");
        newTaskValues.put(KEY_DATA1, "");
        return contentResolver.insert(CONTENT_URI, newTaskValues);
    }

    public void removeEntry(int _rowIndex) {
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        contentResolver.delete(uri, null, null);
    }

    public Cursor getIsCloud(){
        return contentResolver.query(CONTENT_URI, null, KEY_IS_CLOUD + "= " + READY, null, null);
    }

    public static long dayDiff(Date d1, Date d2) {
        final long DAY_MILLIS = 1000 * 60 * 60 * 24;
        long day1 = d1.getTime() / DAY_MILLIS;
        long day2 = d2.getTime() / DAY_MILLIS;
        return day1 - day2;
    }

    public Cursor getAllItem() {
        return contentResolver.query(CONTENT_URI, All_COLUMN_TABLE, null, null, null);
    }

    public Cursor getAllGroupDate() {
        //c = db.query("invoiceTable", new String[] { "date", "sum(totalWeight) as totalWeight" }, null, null, "date", null, null);
        String selection = "date IS NOT NULL) GROUP BY (" + KEY_DATE_CREATE;
        return contentResolver.query(CONTENT_URI, new String[] {KEY_ID,KEY_DATE_CREATE,"sum(totalWeight) AS "+KEY_TOTAL_WEIGHT}, selection, null,null);
    }

    public Cursor getEntryItem(int _rowIndex) {
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        try {
            Cursor result = contentResolver.query(uri, All_COLUMN_TABLE, null, null, null);
            result.moveToFirst();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor getEntryItem(int _rowIndex, String... columns) {
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        try {
            Cursor result = contentResolver.query(uri, columns, null, null, null);
            result.moveToFirst();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public ContentValues getValuesItem(int _rowIndex) throws Exception {
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        try {
            Cursor result = contentResolver.query(uri, All_COLUMN_TABLE, null, null, null);
            result.moveToFirst();
            ContentQueryMap mQueryMap = new ContentQueryMap(result, BaseColumns._ID, true, null);
            Map<String, ContentValues> map = mQueryMap.getRows();
            result.close();
            return map.get(String.valueOf(_rowIndex));
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public boolean updateEntry(int _rowIndex, String key, int in) {
        //boolean b;
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        try {
            ContentValues newValues = new ContentValues();
            newValues.put(key, in);
            return contentResolver.update(uri, newValues, null, null) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateEntry(int _rowIndex, String key, String value) {
        //boolean b;
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        try {
            ContentValues newValues = new ContentValues();
            newValues.put(key, value);
            return contentResolver.update(uri, newValues, null, null) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void updateEntry(int _rowIndex, ContentValues values) {
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        try {
            contentResolver.update(uri, values, null, null);
        } catch (Exception e) {
            return;
        }
    }

    public Cursor getPreliminary() {
        return contentResolver.query(CONTENT_URI, null, KEY_IS_CLOUD + "= " + UNREADY, null, null);
    }

    /** Получить записи все записи которые не отправленые на сервер
     * и те которые проведенные или не равны дате.
     * Для того чтобы не закрытые сегодняшнии записи не отправлять на сервер.
     * @param date Дата рабочачя для не закрытых записей.
     * @return Курсор записей.
     */
    public Cursor getPreliminary(Date date) {
        String d = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date);
        return contentResolver.query(CONTENT_URI, null, KEY_IS_CLOUD + " = " + UNREADY + " and (" + KEY_IS_READY + " = " + READY + " or " + KEY_DATE_CREATE + " != " + '"' +d+ '"' + " )", null, null);
    }

    public Cursor getToday(String date) {
        return contentResolver.query(CONTENT_URI, null, KEY_DATE_CREATE + "= " + '"' +date+ '"', null, null);
    }
}
