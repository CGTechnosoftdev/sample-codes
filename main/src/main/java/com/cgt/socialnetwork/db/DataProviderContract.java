package com.cgt.socialnetwork.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by CGTechnosoft
 */
public class DataProviderContract {

    private DataProviderContract() {
    }

    // The URI scheme used for content URIs
    public static final String SCHEME = "content";

    // The provider's authority
    public static final String AUTHORITY = "com.cgt.socialnetwork";

    // The DataProvider content URI
    public static final Uri DATABASE_URI = Uri.parse(SCHEME + "://" + AUTHORITY);

    // The content provider database name
    public static final String DATABASE_NAME = "cgtsocialnetwork";

    // The starting version of the database
    public static final int DATABASE_VERSION = 2;

    /**
     * The MIME type for a content URI that would return multiple rows
     * <P>Type: TEXT</P>
     */
    public static final String MIME_TYPE_ROWS = "vnd.android.cursor.dir/vnd.com.cgt.socialnetwork";

    public static final class Notification implements BaseColumns {
        // Notification table name
        public static final String TABLE_NAME = "Notification";

        // Notification table content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(DATABASE_URI, TABLE_NAME);

        // Notification table column names
        public static final String _ID = "_id";
        public static final String NOTIFICATION_ID = "notif_id";
        public static final String CONTENT = "content";
        public static final String POST_ID = "post_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String USER_PHOTO = "user_photo";
        public static final String ACTION = "action"; // 1:Create Post, 2:Like, 3:Reply(comment), 4:Add Circle
        public static final String COMMENT_ID = "comment_id";
        public static final String IS_READ = "isRead"; // 0 - unread, 1 - read
        public static final String POST_USER_ID = "post_user_id";

        // create Notification table
        public static final String CREATE_TABLE =
                "CREATE TABLE Notification (_id INTEGER PRIMARY KEY AUTOINCREMENT, notif_id INTEGER, content TEXT NOT NULL, post_id INTEGER, timestamp INTEGER, user_id INTEGER, " +
                        "user_name TEXT, user_photo TEXT, action INTEGER, comment_id INTEGER, isRead INTEGER DEFAULT 0, post_user_id INTEGER DEFAULT -1);";


        public static final String[] PROJECTION =
                {
                        _ID,
                        NOTIFICATION_ID,
                        CONTENT,
                        POST_ID,
                        TIMESTAMP,
                        USER_ID,
                        USER_NAME,
                        USER_PHOTO,
                        ACTION,
                        COMMENT_ID,
                        IS_READ,
                        POST_USER_ID
                };
    }

    public static final class Country implements BaseColumns {
        // COUNTRY table name
        public static final String TABLE_NAME = "country";

        // COUNTRY table content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(DATABASE_URI, TABLE_NAME);

        // COUNTRY table column names
        public static final String COUNTRY_NAME = "countryName";
        public static final String ISO = "iso";

        public static final String[] PROJECTION =
                {
                        _ID,
                        COUNTRY_NAME,
                        ISO
                };

        // create COUNTRY table
        public static final String CREATE_TABLE =
                "CREATE TABLE Country (_id INTEGER PRIMARY KEY, countryName TEXT NOT NULL, iso TEXT NOT NULL);";
    }

    public static final class Feed implements BaseColumns {

        // Feed table name
        public static final String TABLE_NAME = "feed";

        // Feed table content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(DATABASE_URI, TABLE_NAME);

        // Feed table column names
        public static final String CONTENT = "content";
        public static final String TYPE = "type";
        public static final String IMAGE = "image";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String VOTES = "p_votes";
        public static final String COMMENTS = "p_comments";
        public static final String CREATED_DATE = "created";
        public static final String MODIFIED_DATE = "modified";
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String USER_CITY = "user_city";
        public static final String USER_COUNTRY = "user_country";
        public static final String USER_PHOTO = "photoUrl";
        public static final String STATUS = "pending";
        public static final String USER_LIKED = "user_liked";
        public static final String CLIENT_ID = "client_id";

        public static final String[] LIST_PROJECTION =
                {
                        _ID, CONTENT, IMAGE, LATITUDE, LONGITUDE, VOTES, COMMENTS,
                        CREATED_DATE, MODIFIED_DATE, USER_ID, USER_NAME, USER_PHOTO, USER_CITY, USER_COUNTRY, STATUS, USER_LIKED, CLIENT_ID
                };

        // create Feed table
        public static final String CREATE_TABLE =
                "CREATE TABLE feed (_id INTEGER, content TEXT, type TEXT, image TEXT, latitude REAL, longitude REAL, p_votes INTEGER, p_comments INTEGER," +
                        "created INTEGER, modified INTEGER, user_id INTEGER, user_name TEXT, photoUrl TEXT, user_city TEXT, user_country TEXT, " +
                        "pending INTEGER DEFAULT 0, user_liked INTEGER DEFAULT 0, client_id TEXT);";
    }

    public static final class PublicFeed implements BaseColumns {

        // Feed table name
        public static final String TABLE_NAME = "public_feed";

        // Feed table content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(DATABASE_URI, TABLE_NAME);

        // Feed table column names
        public static final String CONTENT = "content";
        public static final String TYPE = "type";
        public static final String IMAGE = "image";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String VOTES = "p_votes";
        public static final String COMMENTS = "p_comments";
        public static final String CREATED_DATE = "created";
        public static final String MODIFIED_DATE = "modified";
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String USER_CITY = "user_city";
        public static final String USER_COUNTRY = "user_country";
        public static final String USER_PHOTO = "photoUrl";
        public static final String STATUS = "pending";
        public static final String USER_LIKED = "user_liked";

        public static final String[] LIST_PROJECTION =
                {
                        _ID, CONTENT, IMAGE, LATITUDE, LONGITUDE, VOTES, COMMENTS,
                        CREATED_DATE, MODIFIED_DATE, USER_ID, USER_NAME, USER_PHOTO, USER_CITY, USER_COUNTRY, STATUS, USER_LIKED
                };

        // create Feed table
        public static final String CREATE_TABLE =
                "CREATE TABLE public_feed (_id INTEGER, content TEXT, type TEXT, image TEXT, latitude REAL, longitude REAL, p_votes INTEGER, p_comments INTEGER," +
                        "created INTEGER, modified INTEGER, user_id INTEGER, user_name TEXT, photoUrl TEXT, user_city TEXT, user_country TEXT, " +
                        "pending INTEGER DEFAULT 0, user_liked INTEGER DEFAULT 0);";
    }

    public static final class SearchFeeds implements BaseColumns {

        // Feed table name
        public static final String TABLE_NAME = "search_feeds";

        // Feed table content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(DATABASE_URI, TABLE_NAME);

        // Feed table column names
        public static final String CONTENT = "content";
        public static final String IMAGE = "image";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String VOTES = "p_votes";
        public static final String COMMENTS = "p_comments";
        public static final String CREATED_DATE = "created";
        public static final String MODIFIED_DATE = "modified";
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String USER_CITY = "user_city";
        public static final String USER_COUNTRY = "user_country";
        public static final String USER_PHOTO = "photoUrl";
        public static final String STATUS = "pending";
        public static final String USER_LIKED = "user_liked";

        public static final String[] LIST_PROJECTION =
                {
                        _ID, CONTENT, IMAGE, LATITUDE, LONGITUDE, VOTES, COMMENTS,
                        CREATED_DATE, MODIFIED_DATE, USER_ID, USER_NAME, USER_PHOTO, USER_CITY, USER_COUNTRY, STATUS, USER_LIKED
                };

        // create Feed table
        public static final String CREATE_TABLE =
                "CREATE TABLE search_feeds (_id INTEGER, content TEXT, image TEXT, latitude REAL, longitude REAL, p_votes INTEGER, p_comments INTEGER," +
                        "created INTEGER, modified INTEGER, user_id INTEGER, user_name TEXT, photoUrl TEXT, user_city TEXT, user_country TEXT, " +
                        "pending INTEGER DEFAULT 0, user_liked INTEGER DEFAULT 0);";
    }

    public static final class HashTagFeeds implements BaseColumns {

        // Feed table name
        public static final String TABLE_NAME = "hashtag_feeds";

        // Feed table content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(DATABASE_URI, TABLE_NAME);

        // Feed table column names
        public static final String CONTENT = "content";
        public static final String IMAGE = "image";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String VOTES = "p_votes";
        public static final String COMMENTS = "p_comments";
        public static final String CREATED_DATE = "created";
        public static final String MODIFIED_DATE = "modified";
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String USER_CITY = "user_city";
        public static final String USER_COUNTRY = "user_country";
        public static final String USER_PHOTO = "photoUrl";
        public static final String STATUS = "pending";
        public static final String USER_LIKED = "user_liked";

        public static final String[] LIST_PROJECTION =
                {
                        _ID, CONTENT, IMAGE, LATITUDE, LONGITUDE, VOTES, COMMENTS,
                        CREATED_DATE, MODIFIED_DATE, USER_ID, USER_NAME, USER_PHOTO, USER_CITY, USER_COUNTRY, STATUS, USER_LIKED
                };

        // create Feed table
        public static final String CREATE_TABLE =
                "CREATE TABLE hashtag_feeds (_id INTEGER, content TEXT, image TEXT, latitude REAL, longitude REAL, p_votes INTEGER, p_comments INTEGER," +
                        "created INTEGER, modified INTEGER, user_id INTEGER, user_name TEXT, photoUrl TEXT, user_city TEXT, user_country TEXT, " +
                        "pending INTEGER DEFAULT 0, user_liked INTEGER DEFAULT 0);";
    }

    public static final class Comments implements BaseColumns {

        // Feed table name
        public static final String TABLE_NAME = "comments";

        // Feed table content URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(DATABASE_URI, TABLE_NAME);

        // Feed table column names
        public static final String COMMENT = "comment";
        public static final String CREATED_DATE = "created";
        public static final String POST_ID = "post_id";
        public static final String USER_ID = "user_id";
        public static final String USER_NAME = "user_name";
        public static final String USER_PHOTO = "photoUrl";
        public static final String CLIENT_ID = "client_id";
        public static final String STATUS = "pending";

        public static final String[] LIST_PROJECTION =
                {
                        _ID, COMMENT, CREATED_DATE, POST_ID, USER_ID, USER_NAME, USER_PHOTO, CLIENT_ID, STATUS
                };

        // create Comments table
        public static final String CREATE_TABLE =
                "CREATE TABLE comments (_id INTEGER, comment TEXT, created INTEGER, post_id INTEGER, user_id INTEGER, user_name TEXT, photoUrl TEXT," +
                        "client_id TEXT, pending INTEGER DEFAULT 0);";
    }
}
