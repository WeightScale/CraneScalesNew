package com.konst.grabewifi.provider;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class WeighingTable {
    private final Context mContext;
    final ContentResolver contentResolver;

    public static final String TABLE = "weighingTable";

    public static final String KEY_ID               = BaseColumns._ID;
    public static final String KEY_ID_INVOICE       = "idInvoice";
    public static final String KEY_DATE_TIME_CREATE = "dateTime";
    public static final String KEY_WEIGHT           = "totalWeight";
    public static final String KEY_DATA0            = "data0";
    public static final String KEY_DATA1            = "data1";

    private static final String[] All_COLUMN_TABLE = {
            KEY_ID,
            KEY_ID_INVOICE,
            KEY_DATE_TIME_CREATE,
            KEY_WEIGHT,
            KEY_DATA0,
            KEY_DATA1};

    public static final String TABLE_CREATE = "create table "
            + TABLE + " ("
            + KEY_ID + " integer primary key autoincrement, "
            + KEY_ID_INVOICE + " integer,"
            + KEY_DATE_TIME_CREATE + " text,"
            + KEY_WEIGHT + " integer,"
            + KEY_DATA0 + " text,"
            + KEY_DATA1 + " text );";

    public static final String[] COLUMN_FOR_INVOICE = {
            KEY_DATE_TIME_CREATE,
            KEY_WEIGHT};

    private static final Uri CONTENT_URI = Uri.parse("content://" + CraneScalesBaseProvider.AUTHORITY + '/' + TABLE);

    public WeighingTable(Context context) {
        mContext = context;
        contentResolver = mContext.getContentResolver();
    }

    public Uri insertNewEntry(int _indexInvoice, int weight) {
        ContentValues newTaskValues = new ContentValues();
        newTaskValues.put(KEY_DATE_TIME_CREATE, new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
        newTaskValues.put(KEY_ID_INVOICE, _indexInvoice);
        newTaskValues.put(KEY_WEIGHT, weight);
        newTaskValues.put(KEY_DATA0, "");
        newTaskValues.put(KEY_DATA1, "");
        return contentResolver.insert(CONTENT_URI, newTaskValues);
    }

    public void removeEntryInvoice(int invoice){
        contentResolver.delete(CONTENT_URI, KEY_ID_INVOICE + " = " + invoice, null);
    }

    public void removeEntry(int _rowIndex) {
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        contentResolver.delete(uri, null, null);
    }

    public Cursor getEntryInvoice(int invoiceId) {
        return mContext.getContentResolver().query(CONTENT_URI, null, KEY_ID_INVOICE + " = " + invoiceId, null, null);
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

    public boolean updateEntry(int _rowIndex, ContentValues values) {
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        try {
            return contentResolver.update(uri, values, null, null) > 0;
        } catch (Exception e) {
            return false;
        }
    }

}
