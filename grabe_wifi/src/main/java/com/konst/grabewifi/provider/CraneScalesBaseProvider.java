package com.konst.grabewifi.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/**
 */
public class CraneScalesBaseProvider extends ContentProvider {

    private static final String DATABASE_NAME = "craneScales.db";
    private static final int DATABASE_VERSION = 1;
    static final String AUTHORITY = "com.kostya.scalegrab.craneScales";
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    private static final int ALL_ROWS = 1;
    private static final int SINGLE_ROWS = 2;

    private enum TableList {
        INVOICE_LIST,
        INVOICE_ID,
        WEIGHING_LIST,
        WEIGHING_ID
    }

    private static final UriMatcher uriMatcher;
    private SQLiteDatabase db;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, InvoiceTable.TABLE, TableList.INVOICE_LIST.ordinal());
        uriMatcher.addURI(AUTHORITY, InvoiceTable.TABLE + "/#", TableList.INVOICE_ID.ordinal());
        uriMatcher.addURI(AUTHORITY, WeighingTable.TABLE, TableList.WEIGHING_LIST.ordinal());
        uriMatcher.addURI(AUTHORITY, WeighingTable.TABLE + "/#", TableList.WEIGHING_ID.ordinal());
    }

    /*public void vacuum(){
        db.execSQL("VACUUM");
    }*/

    private String getTable(Uri uri) {
        switch (TableList.values()[uriMatcher.match(uri)]) {
            case INVOICE_LIST:
            case INVOICE_ID:
                return InvoiceTable.TABLE; // return
            case WEIGHING_LIST:
            case WEIGHING_ID:
                return WeighingTable.TABLE; // return
            /* PROVIDE A DEFAULT CASE HERE **/
            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        DBHelper dbHelper = new DBHelper(getContext());
        //db = dbHelper.getWritableDatabase();
        db = dbHelper.getReadableDatabase();
        if (db != null) {
            db.setLockingEnabled(false);
        }
        return true;
    }

    public Cursor queryG(Uri uri, String[] projection, String selection, String[] selectionArgs, String group, String sort) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (TableList.values()[uriMatcher.match(uri)]) {
            case INVOICE_LIST: // общий Uri
                queryBuilder.setTables(InvoiceTable.TABLE);
                break;
            case INVOICE_ID: // Uri с ID
                queryBuilder.setTables(InvoiceTable.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
                break;
            case WEIGHING_LIST: // общий Uri
                queryBuilder.setTables(WeighingTable.TABLE);
                break;
            case WEIGHING_ID: // Uri с ID
                queryBuilder.setTables(WeighingTable.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, group, null, sort);
        if (cursor == null) {
            return null;
        }
        Context context = getContext();
        if (context != null) {
            ContentResolver contentResolver = context.getContentResolver();
            if (contentResolver != null) {
                cursor.setNotificationUri(contentResolver, uri);
            }
        }
        return cursor;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (TableList.values()[uriMatcher.match(uri)]) {
            case INVOICE_LIST: // общий Uri
                queryBuilder.setTables(InvoiceTable.TABLE);
                break;
            case INVOICE_ID: // Uri с ID
                queryBuilder.setTables(InvoiceTable.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
                break;
            case WEIGHING_LIST: // общий Uri
                queryBuilder.setTables(WeighingTable.TABLE);
                break;
            case WEIGHING_ID: // Uri с ID
                queryBuilder.setTables(WeighingTable.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sort);
        if (cursor == null) {
            return null;
        }
        Context context = getContext();
        if (context != null) {
            ContentResolver contentResolver = context.getContentResolver();
            if (contentResolver != null) {
                cursor.setNotificationUri(contentResolver, uri);
            }
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                return "vnd.android.cursor.dir/vnd.";
            case SINGLE_ROWS:
                return "vnd.android.cursor.item/vnd.";
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        long rowID = db.insert(getTable(uri), null, contentValues);
        if (rowID > 0L) {
            Uri resultUri = ContentUris.withAppendedId(uri, rowID);
            Context context = getContext();
            if (context != null) {
                context.getContentResolver().notifyChange(resultUri, null);
                return resultUri;
            }
        }
        throw new SQLiteException("Ошибка добавления записи " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArg) {
        int delCount;
        String id;
        switch (TableList.values()[uriMatcher.match(uri)]) {
            case INVOICE_LIST: // общий Uri
                delCount = db.delete(InvoiceTable.TABLE, where, whereArg);
                break;
            case INVOICE_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                delCount = db.delete(InvoiceTable.TABLE, where, whereArg);
                break;
            case WEIGHING_LIST: // общий Uri
                delCount = db.delete(WeighingTable.TABLE, where, whereArg);
                break;
            case WEIGHING_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                delCount = db.delete(WeighingTable.TABLE, where, whereArg);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db.execSQL("VACUUM");
        if (delCount > 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] whereArg) {
        int updateCount;
        String id;
        switch (TableList.values()[uriMatcher.match(uri)]) {
            case INVOICE_LIST: // общий Uri
                updateCount = db.update(InvoiceTable.TABLE, contentValues, where, whereArg);
                break;
            case INVOICE_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                updateCount = db.update(InvoiceTable.TABLE, contentValues, where, whereArg);
                break;
            case WEIGHING_LIST: // общий Uri
                updateCount = db.update(WeighingTable.TABLE, contentValues, where, whereArg);
                break;
            case WEIGHING_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                updateCount = db.update(WeighingTable.TABLE, contentValues, where, whereArg);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        if (updateCount > 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        return updateCount;
    }

    private static class DBHelper extends SQLiteOpenHelper {

        DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(InvoiceTable.TABLE_CREATE);
            db.execSQL(WeighingTable.TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL(DROP_TABLE_IF_EXISTS + InvoiceTable.TABLE);
            db.execSQL(DROP_TABLE_IF_EXISTS + WeighingTable.TABLE);
            onCreate(db);
        }
    }
}
