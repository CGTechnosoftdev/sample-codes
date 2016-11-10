package com.cgt.socialnetwork.common;

/**
 * Created by CGTechnosoft
 */
public class Constants {
    public static final String URL = "http://52.77.231.254:8080/mobileapi/";
    public static final String SEARCH_HASH_TAG = URL + "searchHashtag";
    public static final String LOGOUT = URL + "logout";
    public static final String EDIT_PROFILE = URL + "edit_profile";
    public static final String GET_COUNTRIES = URL + "get_countries";
    public static final String DELETE_COMMENT = URL + "deleteComment";
    public static final String DELETE_POST = URL + "deletePost";
    public static final String LOGIN = URL + "login";
    public static final String FACEBOOK_LOGIN = URL + "facebookLogin";
    public static final String FORGOT_PASSWORD = URL + "forgotpassword";
    public static final String GET_NOTIFICATIONS = URL + "getNotifications";
    public static final String CREATE_USER = URL + "create_user";
    public static final String GET_PROFILE = URL + "get_profile";
    public static final String ADD_VOTE = URL + "addVote";
    public static final String GET_POST_COMMENTS = URL + "getPostComments";
    public static final String GET_FEEDS = URL + "getFeeds";
    public static final String GET_POST_BY_HASHTAG = URL + "getPostByHashtag";
    public static final String GET_USER_PUBLIC_FEED = URL + "getUserPublicFeed";
    public static final String SEARCH_FEEDS = URL + "searchFeeds";
    public static final String READ_NOTIFICATION = URL + "readNotification";
    public static final String EDIT_POST = URL + "editPost";
    public static final String ADD_COMMENT = URL + "addComment";
    public static final String ADD_POST = URL + "add_post";
    public static final String CHANGE_PASSWORD = URL + "changePassword";
    public static final String GET_POST_DETAIL = URL + "getpostDetail";

    public static final String KEY_USER_DATA = "user_data";

    public static final String KEY_SCREEN = "screen";
    public static final String KEY_SCREEN_FEED = "FragmentFeed";
    public static final String KEY_SCREEN_PUBLIC_FEED = "FragmentPublicFeed";
    public static final String KEY_SCREEN_SEARCH_FEED = "ActivityFeedSearch";
    public static final String KEY_SCREEN_POST_DETAIL = "PostDetail";
    public static final String KEY_SCREEN_COMMENT = "Comment";

    public static final int SUCCESS = 1;
    public static final int INVALID_REQUEST = 2;
    public static final int DATA_NOT_FOUND = 9;
    public static final int TOKEN_EXPIRED = 12;
    public static final int BLANK_TOKEN = 13;
    public static final int USER_BLOCK = 19;

    public static final int FAILURE_RESULT = 3;
    public static final int SUCCESS_RESULT = 4;
    public static final String KEY_CURRENT_LAT = "current_lat";
    public static final String KEY_CURRENT_LONG = "current_long";

    public static final String KEY_DEVICE_TYPE_VALUE = "1";
    public static final String KEY_LOGIN_AS_APP = "2";
    public static final String KEY_LOGIN_AS_FB = "3";
    public static final String KEY_PHOTO = "photo";
    public static final String KEY_LOCATION = "location";
    public static final String LOGIN_FRAGMENT = "login_fragment";
    public static final String SIGN_UP_FRAGMENT = "sign_up_fragment";
    public static final String FORGET_PSW_FRAGMENT = "forget_psw_fragment";
    public static final int REQUEST_CODE_PROFILE = 1;


    // User API
    public static final String KEY_NAME = "name";
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_DEVICE_ID = "device_id";
    public static final String KEY_DEVICE_TYPE = "device_type";
    public static final String KEY_PUSH_TOKEN = "push_token";
    public static final String KEY_FACEBOOK_ID = "facebook_id";
    public static final String KEY_USER_STATUS = "user_status";


    // Country API
    public static final String KEY_COUNTRY_NAME = "name";
    public static final String KEY_ISO = "iso";
    public static final String COUNTRY_ALL = "All";

    // Circle API
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_PHOTO = "user_photo";
    public static final String KEY_CITY = "city";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_IS_ANONYMOUS = "is_anonymous";

    public static final int TAG_LIKED = 2;
    public static final int TAG_ADDED = 4;
    public static final int TAG_REPLY = 3;
    public static final int TAG_CREATED = 1;

    // Notification API
    public static final String KEY_ID = "id";
    public static final String KEY_POST_ID = "post_id";
    public static final String KEY_IS_READ = "isread";
    public static final String KEY_ACTION = "action";
    public static final String KEY_POST_USER_ID = "post_user_id";
    public static final String KEY_NOTIFICATION_ID = "notification_id";

    // Feed API
    public static final String KEY_POSTS = "posts";
    public static final String KEY_P_ID = "id";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_CATEGORY_ID = "cate_id";
    public static final String KEY_CATEGORY_TITLE = "cate_title";
    public static final String KEY_POST_IMAGE = "image";
    public static final String KEY_POST_AUDIO = "audio";
    public static final String KEY_CREATED_DATE = "created";
    public static final String KEY_MODIFIED_DATE = "modified";
    public static final String KEY_VOTES = "p_votes";
    public static final String KEY_COMMENTS = "p_comments";
    public static final String KEY_U_ID = "u_id";
    public static final String KEY_U_NAME = "u_name";
    public static final String KEY_U_PHOTO = "u_photo";
    public static final String KEY_USER_CITY = "u_city";
    public static final String KEY_IS_PRIVATE = "is_private";
    public static final String KEY_TYPE = "type";
    public static final String KEY_USER_LIKE = "is_like";
    public static final String KEY_USER_COUNTRY = "u_country";

    // Comment API
    public static final String KEY_COMMENT_ID = "comment_id";

    // Preference Key
    public static final String DIR_IMAGE = "image";

    // File formats
    public static final String EXT_PDF = ".pdf";
    public static final String EXT_JPG = ".jpg";
    public static final String PREFIX_IMG = "Img_";

    // Loaders
    public static final int LOADER_NOTIFICATION = 1;

    public static final int PERMISSION_REQUEST_CODE_CAMERA = 1;
    public static final int PERMISSION_REQUEST_CODE_STORAGE = 2;
    public static final int PERMISSION_REQUEST_CODE_LOCATION = 3;

    public static final int ITEM_PER_REQUEST = 25;

    // Request Builder params
    public static final String KEY_PARAM_PAGE_SIZE = "page_size";
    public static final String KEY_PARAM_UNTIL_DATE = "until_date";
    public static final String KEY_PARAM_SINCE_DATE = "since_date";
    public static final String PREF_KEY_POST_UNTIL_DATE = "feed_until_date";
    public static final String PREF_KEY_POST_SINCE_DATE = "feed_since_date";
    public static final String PREF_KEY_FEED_UNTIL_DATE = "post_until_date";
    public static final String PREF_KEY_FEED_SINCE_DATE = "post_since_date";
    public static final String PREF_KEY_PUBLIC_FEED_UNTIL_DATE = "public_feed_until_date";
    public static final String PREF_KEY_PUBLIC_FEED_SINCE_DATE = "public_feed_since_date";
    public static final String PREF_KEY_COMMENT_UNTIL_DATE = "comment_until_date";
    public static final String PREF_KEY_COMMENT_SINCE_DATE = "comment_since_date";
    public static final String PREF_KEY_SEARCH_FEED_UNTIL_DATE = "search_feed_until_date";
    public static final String PREF_KEY_HASHTAG_FEED_UNTIL_DATE = "hashtag_feed_until_date";

    public static final String KEY_HEADER_TOKEN = "token";

    public static final String FEEDS_PAGE_SIZE = "25";


    public static final String KEY_PARAM_CONTENT = "content";
    public static final String KEY_PARAM_LATITUDE = "latitude";
    public static final String KEY_PARAM_LONGITUDE = "longitude";
    public static final String KEY_PARAM_VOTE = "vote";
    public static final String KEY_PARAM_USER_ID = "user_id";
    public static final String KEY_PARAM_POST_ID = "post_id";
    public static final String KEY_PARAM_OLD_PASSWORD = "oldpassword";
    public static final String KEY_PARAM_NEW_PASSWORD = "newpassword";
    public static final String KEY_HASH_TAG = "hashtag";

    public static final String KEY_PARAM_SEARCH_DATA = "search_data";

    // Json response keys
    public static final String KEY_CODE = "code";
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_DATA = "data";
    public static final String KEY_DELETED = "deleted";
    public static final String KEY_RESET = "reset";

    public static final String PREF_KEY_RELOAD_NOTIFICATIONS = "reload_notifications";

    public static final int CODE_FACEBOOK_LOGIN = 19;

    public static final String KEY_CALLING_COMPONENT = "calling_component";

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
}
