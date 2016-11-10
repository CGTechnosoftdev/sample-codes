package com.cgt.socialnetwork.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CGTechnosoft
 */
public class MyContentProvider extends ContentProvider {

    // Database specific constant declarations
    private SQLiteDatabase db;
    public static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, DataProviderContract.Notification.TABLE_NAME, Constants.NOTIFICATION);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, DataProviderContract.Country.TABLE_NAME, Constants.COUNTRY);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, DataProviderContract.Feed.TABLE_NAME, Constants.FEED);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, DataProviderContract.PublicFeed.TABLE_NAME, Constants.PUBLIC_FEED);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, DataProviderContract.Comments.TABLE_NAME, Constants.COMMENTS);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, DataProviderContract.SearchFeeds.TABLE_NAME, Constants.SEARCH_FEEDS);
        uriMatcher.addURI(DataProviderContract.AUTHORITY, DataProviderContract.HashTagFeeds.TABLE_NAME, Constants.HASHTAG_FEED);
    }

    public MyContentProvider() {
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        SQLiteDatabase.loadLibs(context);
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getReadableDatabase("123456789");
        return false;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case Constants.NOTIFICATION:
                count = db.delete(DataProviderContract.Notification.TABLE_NAME, selection, selectionArgs);
                break;
            case Constants.COUNTRY:
                count = db.delete(DataProviderContract.Country.TABLE_NAME, selection, selectionArgs);
                break;
            case Constants.FEED:
                count = db.delete(DataProviderContract.Feed.TABLE_NAME, selection, selectionArgs);
                break;
            case Constants.PUBLIC_FEED:
                count = db.delete(DataProviderContract.PublicFeed.TABLE_NAME, selection, selectionArgs);
                break;
            case Constants.COMMENTS:
                count = db.delete(DataProviderContract.Comments.TABLE_NAME, selection, selectionArgs);
                break;
            case Constants.SEARCH_FEEDS:
                count = db.delete(DataProviderContract.SearchFeeds.TABLE_NAME, selection, selectionArgs);
                break;
            case Constants.HASHTAG_FEED:
                count = db.delete(DataProviderContract.HashTagFeeds.TABLE_NAME, selection, selectionArgs);
                break;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case Constants.NOTIFICATION:
                return DataProviderContract.MIME_TYPE_ROWS;

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = -1;
        switch (uriMatcher.match(uri)) {
            case Constants.NOTIFICATION:
                // Inserts the row into the table and returns the new row's _id value
                id = db.insert(DataProviderContract.Notification.TABLE_NAME, "", values);

                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != id) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }
            case Constants.COUNTRY:
                // Inserts the row into the table and returns the new row's _id value
                id = db.insert(DataProviderContract.Country.TABLE_NAME, "", values);

                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != id) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }

            case Constants.FEED:
                // Inserts the row into the table and returns the new row's _id value
                id = db.insert(DataProviderContract.Feed.TABLE_NAME, "", values);

                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != id) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }

            case Constants.PUBLIC_FEED:
                // Inserts the row into the table and returns the new row's _id value
                id = db.insert(DataProviderContract.PublicFeed.TABLE_NAME, "", values);

                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != id) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }
            case Constants.COMMENTS:
                // Inserts the row into the table and returns the new row's _id value
                id = db.insert(DataProviderContract.Comments.TABLE_NAME, "", values);

                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != id) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }

            case Constants.SEARCH_FEEDS:
                // Inserts the row into the table and returns the new row's _id value
                id = db.insert(DataProviderContract.SearchFeeds.TABLE_NAME, "", values);

                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != id) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }
            case Constants.HASHTAG_FEED:
                // Inserts the row into the table and returns the new row's _id value
                id = db.insert(DataProviderContract.HashTagFeeds.TABLE_NAME, "", values);

                // If the insert succeeded, notify a change and return the new row's content URI.
                if (-1 != id) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return Uri.withAppendedPath(uri, Long.toString(id));
                } else {
                    throw new SQLiteException("Insert error:" + uri);
                }

            default:
                throw new IllegalArgumentException("Insert: Invalid URI" + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor returnCursor = null;
        switch (uriMatcher.match(uri)) {
            case Constants.NOTIFICATION:
                // Does the query against a read-only version of the database
                returnCursor = db.query(
                        DataProviderContract.Notification.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                // Sets the ContentResolver to watch this content URI for data changes
                returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return returnCursor;
            case Constants.COUNTRY:
                // Does the query against a read-only version of the database
                returnCursor = db.query(
                        DataProviderContract.Country.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                // Sets the ContentResolver to watch this content URI for data changes
                returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return returnCursor;

            case Constants.FEED:
                // Does the query against a read-only version of the database
                returnCursor = db.query(
                        DataProviderContract.Feed.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                // Sets the ContentResolver to watch this content URI for data changes
                returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return returnCursor;

            case Constants.PUBLIC_FEED:
                // Does the query against a read-only version of the database
                returnCursor = db.query(
                        DataProviderContract.PublicFeed.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                // Sets the ContentResolver to watch this content URI for data changes
                returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return returnCursor;
            case Constants.COMMENTS:
                // Does the query against a read-only version of the database
                returnCursor = db.query(
                        DataProviderContract.Comments.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                // Sets the ContentResolver to watch this content URI for data changes
                returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return returnCursor;

            case Constants.SEARCH_FEEDS:
                // Does the query against a read-only version of the database
                returnCursor = db.query(
                        DataProviderContract.SearchFeeds.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                // Sets the ContentResolver to watch this content URI for data changes
                returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return returnCursor;
            case Constants.HASHTAG_FEED:
                // Does the query against a read-only version of the database
                returnCursor = db.query(
                        DataProviderContract.HashTagFeeds.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                // Sets the ContentResolver to watch this content URI for data changes
                returnCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return returnCursor;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count = 0;

        switch (uriMatcher.match(uri)) {
            case Constants.NOTIFICATION:
                count = db.update(DataProviderContract.Notification.TABLE_NAME, values, selection, selectionArgs);
                break;

            case Constants.COUNTRY:
                count = db.update(DataProviderContract.Country.TABLE_NAME, values, selection, selectionArgs);
                break;

            case Constants.FEED:
                count = db.update(DataProviderContract.Feed.TABLE_NAME, values, selection, selectionArgs);
                break;

            case Constants.PUBLIC_FEED:
                count = db.update(DataProviderContract.PublicFeed.TABLE_NAME, values, selection, selectionArgs);
                break;

            case Constants.COMMENTS:
                count = db.update(DataProviderContract.Comments.TABLE_NAME, values, selection, selectionArgs);
                break;

            case Constants.SEARCH_FEEDS:
                count = db.update(DataProviderContract.SearchFeeds.TABLE_NAME, values, selection, selectionArgs);
                break;

            case Constants.HASHTAG_FEED:
                count = db.update(DataProviderContract.HashTagFeeds.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If the update succeeded, notify a change and return the number of updated rows.
        if (0 != count) {
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        } else {
            throw new SQLiteException("Update error:" + uri);
        }
    }

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DataProviderContract.DATABASE_NAME, null, DataProviderContract.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DataProviderContract.Notification.CREATE_TABLE);
            db.execSQL(DataProviderContract.Country.CREATE_TABLE);
            db.execSQL(DataProviderContract.Feed.CREATE_TABLE);
            db.execSQL(DataProviderContract.PublicFeed.CREATE_TABLE);
            db.execSQL(DataProviderContract.Comments.CREATE_TABLE);
            db.execSQL(DataProviderContract.SearchFeeds.CREATE_TABLE);
            db.execSQL(DataProviderContract.HashTagFeeds.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            List<String> columns = getColumnNames(db, DataProviderContract.Notification.TABLE_NAME);
            addColumnIfNotExists(db, DataProviderContract.Notification.TABLE_NAME, columns,
                    DataProviderContract.Notification.POST_USER_ID, "INTEGER");
        }

        private static List<String> getColumnNames(SQLiteDatabase db, String tableName) {
            List<String> ret = new ArrayList<String>();
            Cursor cur = db.rawQuery("PRAGMA table_info( " + tableName + " )",
                    null);
            try {
                if (cur.moveToFirst()) {

                    int nameColumn = cur.getColumnIndex("name");

                    do {
                        ret.add(cur.getString(nameColumn));

                    } while (cur.moveToNext());
                }
            } finally {
                cur.close();
            }

            return ret;
        }

        private static void addColumnIfNotExists(SQLiteDatabase db, String table,
                                                 List<String> tableColumns, String col, String type) {
            if (!tableColumns.contains(col)) {
                db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + col + " " + type);
            }
        }

        private static void dropColumn(SQLiteDatabase db, String table, String col) {
            db.execSQL("ALTER TABLE " + table + " DROP COLUMN " + col);
        }

    }
}
