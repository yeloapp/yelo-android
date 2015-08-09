/*
 *
 *  * Copyright (C) 2015 yelo.red
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 */package red.yelo.utils;

import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import java.util.Locale;

import red.yelo.http.HttpConstants;

/**
 * Created by anshul1235 on 14/07/14.
 */
public class AppConstants {


    public static final java.lang.String PLAY_STORE_MARKET_LINK = "https://play.google.com/store/apps/details?id=red.yelo";
    public static final String REFERRER_FORMAT = "referrer=%s";
    public static final String REFERRER_VALUE = "utm_source=%s&utm_campaign=%s&utm_medium=%s&utm_content=%s";

    public static final String POST_SHARE = "post_share";
    public static final String APP_VIRALITY = "app_virality";
    public static final String ANDROID_APP = "android_app";
    public static final String CHECK_THIS_OUT = "check_this_out";

    public static final String SMS_VERIFY_FORMAT = "yelo verification code: ";

    public static final String PLATFORM = "android";
    public static final String MY_LOCATION = "My Location";

    /**
     * Minimum time between syncing contacts to server
     */
    public static final long CONTACT_SYNC_INTERVAL = 7 * 24 * 60 * 60L; // 1 week in seconds

    /**
     * Minimum time between syncing leaders
     */
    public static final long ONE_DAY_TIME = 24 * 60 * 60L; // 1 day in seconds

    /**
     * Minimum time between uploading the batch to the server
     */
    public static final long CONTACT_PER_BATCH_UPLOAD_INTERVAL = 60 * 5L;

    public static final String FBPERMISSIONS[] = new String[]{
            "email", "user_about_me"
    };

    public static final String TYPE = "wall";

    public static final int NOTIFICATION_ID_WALL = 2;
    /**
     * Notification Id for notifications related to messages
     */
    public static final int MESSAGE_NOTIFICATION_ID = 1;

    public static final int CHAT_PAGE = 2;
    public static final int DEFAULT_SERVICE_PAGER_NUMBER = 0;
    public static final int DEFAULT_PAGER_NUMBER = 1;

    public static final String AVATOR_PROFILE_NAME = "yelo_userimage.jpg";
    public static final String WALL_IMAGE_NAME = "wall_image_name.jpg";

    public static final String SERVICE_IMAGE = "service_image.jpg";

    public static final String CATEGORY_SEPERATOR = ",";

    //page(default page number) and per(default count to be loaded per page)
    // as per listing for categories as well as services
    public static final String PER_VALUE = "20";

    public static final String DISTANCE = "50"; //in kms

    /*
    * heartbeat interval for rabbitmq chat
    */
    //public static final int HEART_BEAT_INTERVAL = 20;

    public static final int LOGIN_WAITING_TIME = 100000;


    public static final String ACTION_SHOW_ALL_CHATS = "com.lovocal.ACTION_SHOW_ALL_CHATS";
    public static final String ACTION_SHOW_HOME_SCREEN = "com.lovocal.ACTION_SHOW_HOME_SCREEN";
    public static final String ACTION_SHOW_CHAT_DETAIL = "com.lovocal.ACTION_SHOW_CHAT_DETAIL";
    public static final String ACTION_DISCONNECT_CHAT = "com.lovocal.ACTION_DISCONNECT_CHAT";
    public static final String ACTION_QUERY_BUTTON_CLICKED = "com.lovocal.ACTION_QUERYBUTTON_CLICKED";
    public static final String ACTION_RECONNECT_CHAT = "com.lovocal.ACTION_RECONNECT_CHAT";
    public static final String ACTION_USER_INFO_UPDATED = "com.lovocal.ACTION_USER_INFO_UPDATED";

    public static final String CHAT_ID_FORMAT = "%s#%s";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss Z";
    public static final String CHAT_TIME_FORMAT = "dd MMM, h:mm a";
    public static final String MESSAGE_TIME_FORMAT = "h:mm a";
    public static final String WALL_DATE_FORMAT = "dd MMM";

    public static final int HEART_BEAT = 20;
    public static final int HEART_BEAT_BACKGROUND = 3600;


    public static final String SERVICE = "service";
    public static final String USER = "user";

    public static final String MOST_RESPONSIVE = "most_responsive";
    public static final String NEARBY = "nearby";

    public static final String NO_ID = "no_id";
    public static final String NO_IMAGE = "no_image";

    public static final String TOP = "top";

    public static final String ALL_TAG_NAME = "#all";

    public static final int SERVER_LAG_TIME = 15000;

    public static final String TRUE = "true";
    public static final String ERROR = "error";

    //registration screens
    public static final int LOGIN_SCREEN = 0;
    public static final int EDIT_PROFILE_SCREEN = 1;
    public static final int CREATE_AVATAR = 2;
    public static final int SELECT_INTEREST = 3;
    public static final int REGISTRATION_DONE = 4;


    public static final String MIME_TYPE = "application/octet-stream";

    //VERIFICATION
    public static final String ALREADY_REGISTERED = "already_registered";
    public static final String NOT_REGISTERED = "not_registered";


    /**
     * Constant Interface, DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface Keys {
        public static final String UP_NAVIGATION_TAG = "up_navigation_tag";
        public static final String ONWARD_INTENT = "onward_intent";
        public static final String PAGER_POSITION = "pager_position";
        public static final String ID = "id";
        public static final String CATEGORY_ID = "category_id";
        public static final String CATEGORY_NAME = "category_name";
        public static final String CATEGORY_NAMES = "category_names";
        public static final String USER_INFO = "user_info";
        public static final String CHAT_ID = "chat_id";
        public static final String USER_ID = "user_id";
        public static final String LIST_USER_ID = "list_user_id";
        public static final String QUERY_USER_ID = "query_user_id";
        public static final String LIST_ID = "list_id";
        public static final String TAGS = "tags";
        public static final String SENDER_TYPE = "sender_type";
        public static final String CHAT_ID_ARRAY = "chat_id_array";
        public static final String USER_ID_ARRAY = "user_id_array";
        public static final String CHAT_TITLE = "chat_title";
        public static final String MY_ID = "my_id";
        public static final String LOAD_CHAT = "load_chat";
        public static final String FROM_NOTIFICATIONS = "from_notifications";
        public static final String FINISH_ON_BACK = "finish_on_back";
        public static final String IMAGEFEATURE_POSITION = "imagefeature_position";
        public static final String IMAGE_URLS = "image_urls";
        public static final String IMAGE_RES_ID = "image_res_id";
        public static final String HELP_TEXT = "help_text";
        public static final String BANNER_COUNT = "banner_count";
        public static final String CHAT_INDEX = "chat_index";
        public static final String LAST_FETCHED_LOCATION = "last_fetched_location";
        public static final String HAS_LOADED_ALL_ITEMS = "has_loaded_all_items";
        public static final String PANEL_OPEN = "panel_open";
        public static final String SERVICE_ID_ARRAY = "service_id_array";
        public static final String LIST_ID_ARRAY = "list_id_array";
        public static final String SERVICE_IMAGE = "service_image";
        public static final String TAG_LIST = "tag_list";
        public static final String FROM_LOGIN = "from_login";
        public static final String LISTING_TYPE = "listing_type";
        public static final String ADDRESS = "address";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String DESCRIPTION = "description";
        public static final String PROFILE_IMAGE = "profile_image";
        public static final String FROM_HOMESCREEN = "from_homescreen";
        public static final String USER_NAME = "user_name";
        public static final String RATING = "rating";
        public static final String REVIEW_COUNT = "review_count";
        public static final String API_SERVICE = "api_service";
        public static final String LAST_SCREEN_TIME = "last_screen_time";
        public static final String NAVIGATION_SELECTED = "navigation_selected";
        public static final String CONNECTION_COUNT = "connection_count";
        public static final String TAG_NAME = "tag_name";
        public static final String DATE_TIME = "date_time";
        public static final String MESSAGE = "message";
        public static final String TAGGED_USERS_IMAGE = "tagged_users_image";
        public static final String TAGGED_USERS_ID = "tagged_user_id";
        public static final String FROM_WALL = "from_wall";
        public static final String TAG_ID = "tag_id";
        public static final String PLACE = "place";
        public static final String FROM_AVATAR = "from_avatar";
        public static final String TAG_USER_COUNT = "tag_user_count";
        public static final String CHAT_USER_COUNT = "chat_user_count";
        public static final String WALL_IMAGES = "wall_images";
        public static final String USER_IMAGE = "user_image";
        public static final String AFTER_LOGIN = "after_login";
        public static final String ACTION_SHOW_ALL_CHATS = "action_show_all_chats";
        public static final String VERIFICATION_STATE = "verification_state";
        public static final String REGISTRATION_SCREEN_STATE = "registration_screen_state";
        public static final String WALL_ID = "wall_id";
        public static final String EDIT_POST = "edit_post";
        public static final String UPDATE = "update";
        public static final String FROM_WALL_POST = "from_wall_post";
        public static final String FROM_TAG = "from_tag";
        public static final String TEMP_ID = "temp_id";
        public static final String FROM_PROFILE = "from_profile";
        public static final String UPDATE_TEXT = "update_text";
        public static final String MAINTAINENCE = "maintainence";
        public static final String NOTIFICATION_ID = "notification_id";
        public static final String HEART_BEAT = "heart_beat";
        public static final String GROUP_NAME = "group_name";
        public static final String GROUP_ID = "group_id";
        public static final String COLOR = "color";
        public static final String KEYWORDS = "keywords";
        public static final String URL = "url";
        public static final String SUBCATEGORY_NAME = "subcategory_name";
        public static final String SUBCATEGORY_ID = "subcategory_id";
        public static final String FETCHED = "fetched";
        public static final String FROM_START = "from_start";
        public static final String CITY_NAME = "city_name";
        public static final String STATE_NAME = "state_name";
        public static final String COUNTRY_NAME = "country_name";
        public static final String SERVICE_SCREEN_TYPE = "service_screen_type";
        public static final String PROFILE_CARD_NAME = "profile_card_name";
        public static final String PROFILE_CARD_ID = "profile_card_id";
        public static final String TITLE = "title";
        public static final String PRICE = "price";
        public static final String CONTACT_NUMBER = "contact_number";
        public static final String SERVICE_PRICE = "service_price";
        public static final String PAGER = "pager";
        public static final String SERVICE_ID = "service_id";
        public static final String DURATION = "duration";
        public static final String DELIVERABLE = "deliverable";
        public static final String EDIT_SERVICE = " edit_service";
        public static final String IMAGE_UPLOADED = "image_uploaded";
        public static final String RATING_COUNT = "rating_count";
        public static final String COMMENT = "comment";


    }

    /**
     * Constant interface, DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface FragmentTags {
        public static final String HOME_SCREEN = "home_screen";
        public static final String NAV_DRAWER = "nav_drawer";
        public static final String LOGIN = "login";
        public static final String DIALOG_SEND_QUERY = "dialog_send_query";
        public static final String EDIT_PROFILE = "edit_profile";
        public static final String CREATE_SERVICE = "create_service";
        public static final String SEARCH_SERVICE = "search_service";
        public static final String CHATS = "chats";
        public static final String CHAT_DETAILS = "chat_details";
        public static final String CHAT_DETAILS_PAGER = "chat_details_pager";
        public static final String BROADCAST_QUERY = "broadcast_query";
        public static final String ABOUT_ME = "about_me";
        public static final String MY_SERVICES = "my_services";
        public static final String PROFILE = "profile";
        public static final String PICK_TAGS = "pick_tags";
        public static final String PROFILE_FROM_CHAT_DETAILS = "profile_from_chat_details";
        public static final String SEARCH = "search";
        public static final String WALL_POST = "wall_post";
        public static final String REVIEWS = "reviews";
        public static final String FROM_ABOUT_ME = "from_about_me";
        public static final String FEEDBACK = "feedback";
        public static final String POST_ON_WALL = "post_on_wall";
        public static final String SELECT_TAGS = "select_tags";
        public static final String TAG_USER = "tag_user";
        public static final String WALL_IMAGE = "wall_image";
        public static final String NOTIFICATION_SUMMARY = "notification_summary";
        public static final String RESOLVE_POST = "resolve_post";
        public static final String UPDATE_APP = "update_app";
        public static final String SETTINGS = "settings";
        public static final String IMAGE_CROP = "image_crop";
        public static final String REFERRAL_FRAGMENT = "referral_fragment";
        public static final String RECOMMENDATION_FRAGMENT = "recommendation_fragment";
        public static final String YELO_BOARD = "yelo_board";
        public static final String CREATE_CARD = "create_card";
        public static final String CREATE_SERVICE_CARD = "create_card";
        public static final String ASK_FRIEND = "ask_friend";
        public static final String WALLS = "walls";
        public static final String REWARDS = "rewards";
        public static final String CLAIMS_TABLE_FRAGMENT = "claims_table_fragment";
        public static final String LEADERBOARD = "leaderboard";
        public static final String SERVICE_CARDS = "service_cards";
        public static final String EXPANDED_SERVICE_CARD = "expanded_service_card";


        //dialogs
        public static final String DIALOG_TAKE_PICTURE = "dialog_take_picture";
        public static final String DIALOG_CHAT_LONGCLICK = "dialog_chat_longclick";
        public static final String DIALOG_LISTING_LONGCLICK = "dialog_listing_longclick";
        public static final String DIALOG_ADD_TAG = "dialog_add_tag";
        public static final String DIALOG_ADD_RATINGS = "dialog_add_ratings";
        public static final String DIALOG_TAG_USER_OPTIONS = "dialog_tag_user_options";
        public static final String DIALOG_TAG_USER = "dialog_tag_user";
        public static final String DIALOG_WALL_OPTIONS = "dialog_wall_options";


    }


    /**
     * Constant interface. DO NOT IMPLEMENT.
     *
     * @author Anshul Kamboj
     */
    public static interface ChatStatus {
        //Different types of chat status. Linked to the chat_sending_status of database
        public static final int SENDING = 0;
        public static final int SENT = 1;
        public static final int FAILED = -1;
        public static final int RECEIVED = 2;
    }

    /**
     * Constant interface. DO NOT IMPLEMENT.
     *
     * @author Anshul Kamboj
     */
    public static interface WallStatus {
        public static final String CLOSE = "false";
        public static final String OPEN = "true";

    }


    /**
     * Constant interface. DO NOT IMPLEMENT.
     *
     * @author Anshul Kamboj
     */
    public static interface DurationUnit {
        public static final int HOUR = 0;
        public static final int DAY = 1;
        public static final int WEEK = 2;
        public static final int MONTH = 3;

    }

    /**
     * Constant interface. DO NOT IMPLEMENT.
     *
     * @author Anshul Kamboj
     */
    public static interface MessageType {
        public static final String PHONE_PRESENT = "phone_present";
        public static final String EMAIL_PRESENT = "email_present";
        public static final String LINK_PRESENT = "link_present";
        public static final String MESSAGE_OK = "message_ok";


    }


    /**
     * Constant interface. DO NOT IMPLEMENT.
     *
     * @author Anshul Kamboj
     */
    public static interface ConnectionType {
        public static final String WIFI = "1";
        public static final String EDGE = "0";


    }

    /**
     * Constant interface. DO NOT IMPLEMENT.
     *
     * @author Anshul Kamboj
     */
    public static interface GroupType {
        public static final String DISCOVER = "1";
        public static final String OTHER = "0";


    }


    /**
     * Constant interface. DO NOT IMPLEMENT.
     *
     * @author Anshul Kamboj
     */

    public static interface AppSettings {
        public static boolean PROFILE_PIC_MANDATORY = true;
    }

    public static interface ServerStatus {

        public static final String SERVER_STATUS_OK = "1";
        public static final String SERVER_STATUS_UPDATE = "2";
        public static final String SERVER_STATUS_MAINTAINENCE = "3";


    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface ChatType {

        public static final String PERSONAL = "personal";
        public static final String SERVICE = "service";
        public static final String GROUP = "group";
        public static final String BLOCK = "blocked";
    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface CommentType {

        public static final String CHAT = "chat";
        public static final String REFER = "refer";
        public static final String COMMENTS = "comments";

    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface NotificationStatus {

        public static final String READ = "read";
        public static final String UNREAD_OPENED = "unread_opened";
        public static final String UNREAD_NOT_OPENED = "unread_not_opened";
    }


    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface CollapseKey {

        public static final String WALL = "wall";
        public static final String TAG = "tag";
        public static final String SUMMARY = "summary";
        public static final String PIN = "pin";
        public static final String CONTACT_WALL = "contact_wall";
        public static final String ALERT = "alert";
        public static final String COMMENT = "comment";
    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface ChatAction {

        public static final String BLOCK = "block";
        public static final String ALLOW = "allow";
        public static final String REJECT = "reject";
    }


    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface SyncStates {

        public static String SYNCING = "0";
        public static String SYNCED = "1";
        public static String CLOSED = "2";

    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface ServiceScreenType {
        public static final int SEARCH_WITH_CATEGORY = 1;
        public static final int PROFILE = 2;
        public static final int SEARCH_WITH_KEYWORDS = 3;

    }


    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface SuggestionType {
        public static String KEYWORDS = "keywords";
    }


    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface QueryTokens {

        // 1-100 for load queries
        public static final int GET_SERVICE_IDS = 1;
        public static final int QUERY_SENDER_USER_DETAIL = 2;
        public static final int QUERY_RECEIVER_USER_DETAIL = 3;
        public static final int QUERY_WALL_DETAILS = 4;


        // 101-200 for insert queries
        public static final int INSERT_CATEGORIES = 101;
        public static final int INSERT_SERVICES = 102;
        public static final int INSERT_MY_SERVICES = 103;
        public static final int INSERT_TAGS = 104;
        public static final int INSERT_SERVICES_IN_LISTING = 105;
        public static final int INSERT_REVIEWS = 106;
        public static final int INSERT_WALLPOST = 107;
        public static final int INSERT_WALLCOMMENT = 108;
        public static final int INSERT_USERS = 109;
        public static final int INSERT_LOCATION_SUGGESSTIONS = 110;
        public static final int INSERT_TAG_SUGGESSTIONS = 111;
        public static final int INSERT_NOTIFICATIONS = 112;
        public static final int INSERT_WALL_SELECTION_USERS = 113;
        public static final int INSERT_MY_WALLCOMMENT = 114;
        public static final int INSERT_MY_WALLPOST = 115;
        public static final int INSERT_WALLCOMMENT_IN_TAG_FRAGMENT = 116;
        public static final int INSERT_REFERRALS = 117;
        public static final int INSERT_RECOMMENDATIONS = 118;
        public static final int INSERT_COLLECTIONS = 119;
        public static final int INSERT_SUBCATEGORIES = 120;
        public static final int INSERT_PROFILE_CARDS = 121;
        public static final int INSERT_CLAIMS = 122;
        public static final int INSERT_LEADERS = 123;
        public static final int INSERT_SERVICE_CARD = 124;
        public static final int INSERT_SERVICE_LIST = 125;
        public static final int INSERT_DISCOVER_TAG_SUGGESSTIONS = 126;


        // 201-300 for update queries
        public static final int UPDATE_TAGS = 201;
        public static final int UPDATE_MY_SERVICES = 202;
        public static final int UPDATE_SERVICES = 203;
        public static final int UPDATE_REVIEWS = 204;
        public static final int UPDATE_CATEGORIES = 205;
        public static final int UPDATE_LISTING_SERVICES = 206;
        public static final int UPDATE_WALLPOST = 207;
        public static final int UPDATE_WALLCOMMENTS = 208;
        public static final int UPDATE_USERS = 209;
        public static final int UPDATE_MESSAGE_STATUS = 210;
        public static final int UPDATE_LOCATION_SUGGESSTIONS = 211;
        public static final int UPDATE_TAGS_SUGGESSTIONS = 212;
        public static final int UPDATE_WALL_SELECTION_USERS = 213;
        public static final int UPDATE_MY_WALLPOST = 214;
        public static final int UPDATE_MY_WALLCOMMENTS = 215;
        public static final int UPDATE_WALL_SELECTION_USERS_CHATS = 216;
        public static final int UPDATE_TAG_COUNT = 217;
        public static final int UPDATE_NOTIFICATION_STATUS = 218;
        public static final int UPDATE_CHAT_READ = 219;
        public static final int UPDATE_REFERRALS = 220;
        public static final int UPDATE_CHATS = 221;
        public static final int UPDATE_COLLECTIONS = 222;
        public static final int UPDATE_SUBCATEGORIES = 223;
        public static final int UPDATE_PROFILE_CARDS = 224;
        public static final int UPDATE_RECOMMENDATIONS = 225;
        public static final int UPDATE_CLAIMS = 226;
        public static final int UPDATE_LEADERS = 227;
        public static final int UPDATE_SERVICE_CARD = 228;
        public static final int UPDATE_SERVICE_LIST = 229;
        public static final int UPDATE_DISCOVER_TAGS_SUGGESSTIONS = 230;


        //301-400 for delete queries
        public static final int DELETE_CATEGORIES = 301;
        public static final int DELETE_SERVICES = 302;
        public static final int DELETE_CHAT_MESSAGE = 303;
        public static final int DELETE_MY_SERVICES = 304;
        public static final int DELETE_CHATS = 305;
        public static final int DELETE_CHAT_MESSAGES = 306;
        public static final int DELETE_TAGS = 307;
        public static final int DELETE_LISTING = 308;
        public static final int DELETE_WALL_POSTS_SEARCH_RESULTS = 309;
        public static final int DELETE_NOTIICATIONS = 310;
        public static final int DELETE_WALL_POST = 311;
        public static final int DELETE_MY_WALL_POST = 312;
        public static final int DELETE_MY_WALL_POSTS_SEARCH_RESULTS = 313;
        public static final int DELETE_SELECTION_USERS = 314;
        public static final int DELETE_TAG_USER = 315;
        public static final int DELETE_WALL_COMMENTS = 316;
        public static final int DELETE_MY_WALL_COMMENTS = 317;
        public static final int DELETE_PARTICULAR_NOTIFICATION = 318;
        public static final int DELETE_REFERRALS_RECEIVED = 319;
        public static final int DELETE_RECOMMENDATIONS = 320;
        public static final int DELETE_LEADERS = 321;
        public static final int DELETE_SERVICE_CARD = 322;
        public static final int DELETE_SERVICE_CARD_CATEGORY = 323;


    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     *
     * @author Anshul Kamboj
     */
    public static interface Loaders {

        public static final int LOAD_CATEGORIES = 201;
        public static final int LOAD_CATEGORIES_IN_SERVICES = 202;
        public static final int LOAD_SERVICES = 203;
        public static final int ALL_CHATS = 204;
        public static final int CHAT_DETAILS = 205;
        public static final int USER_DETAILS_CHAT_DETAILS = 206;
        public static final int LOAD_MY_SERVICES = 207;
        public static final int LOAD_TAGS = 208;
        public static final int LOAD_NEARBY_SERVICES = 209;
        public static final int LOAD_RESPONSIVE_SERVICES = 210;
        public static final int LOAD_SEARCH_SERVICES = 211;
        public static final int LOAD_REVIEWS = 212;
        public static final int LOAD_TOP_SERVICES = 213;
        public static final int LOAD_MY_RATING = 214;
        public static final int LOAD_WALL_MESSAGES = 215;
        public static final int LOAD_CATEGORIES_ON_YELOBOARD = 216;
        public static final int LOAD_WALL_COMMENTS = 217;
        public static final int LOAD_SUGGESTIONS = 218;
        public static final int LOAD_SUGGESTIONS_TAGS = 219;
        public static final int LOAD_WALL = 220;
        public static final int LOAD_USER = 221;
        public static final int LOAD_USER_WALL_MESSAGES = 222;
        public static final int LOAD_NOTIFICATIONS = 223;
        public static final int LOAD_CLOSE_USERS = 224;
        public static final int LOAD_REFERRALS_RECEIVED = 225;
        public static final int LOAD_RECOMMENDATIONS = 226;
        public static final int LOAD_COLLECTIONS = 227;
        public static final int LOAD_CONTACTS = 228;
        public static final int LOAD_PROFILE_CARDS = 229;
        public static final int LOAD_PROFILE_CARD = 230;
        public static final int LOAD_OTHER_USER = 231;
        public static final int LOAD_OTHER_PROFILE_CARDS = 232;
        public static final int LOAD_CLAIMS = 233;
        public static final int LOAD_LEADERS = 234;
        public static final int LOAD_USER_SERVICE_CARDS = 235;
        public static final int LOAD_CATEGORY_SERVICE_CARDS = 236;
        public static final int LOAD_PROFILE_CARD_IN_SERVICES = 237;
        public static final int LOAD_SERVICE_CARD = 238;
        public static final int LOAD_SERVICE_CATEGORIES = 239;
        public static final int LOAD_SERVICE_BASED_ON_SEARCH = 240;
        public static final int LOAD_SERVICE_BASED_ON_CATEGORY_SEARCH = 241;
        public static final int LOAD_SUGGESTIONS_TAGS_IN_SERVICES = 242;


    }

    /**
     * All the request codes used in the application will be placed here
     *
     * @author Anshul Kamboj
     */
    public static interface RequestCodes {


        public static final int LOGIN = 100;
        public static final int EDIT_PROFILE = 101;
        public static final int CREATE_SERVICE = 102;
        public static final int GALLERY_INTENT_CALLED = 103;
        public static final int GALLERY_KITKAT_INTENT_CALLED = 104;
        public static final int HOME = 105;
        public static final int GET_PLACE = 106;
        public static final int REGISTRATION_ACTIVITY = 107;
        public static final int ONWARD = 108;
        public static final int REWARDS = 109;


    }


    /**
     * Singleton to hold frequently accessed info in memory
     *
     * @author Anshul Kamboj
     */
    public enum UserInfo {

        INSTANCE;

        private String mAuthToken;
        private String mEmail;
        private String mMobileNumber;
        private String mId;
        private String mProfilePicture;
        private String mAuthHeader;
        private String mDeviceId;
        private String mFirstName;
        private String mLastName;
        private String mDescription;

        private UserInfo() {
            reset();
        }

        public void reset() {
            mAuthToken = "";
            mAuthHeader = "";
            mEmail = "";
            mId = "";
            mProfilePicture = "";
            mFirstName = "";
            mLastName = "";
            mMobileNumber = "";
            mDescription = "";
        }

        public String getMobileNumber() {
            return mMobileNumber;
        }

        public void setMobileNumber(final String mobileNumber) {
            if (mobileNumber == null) {
                mMobileNumber = "";
            } else {
                mMobileNumber = mobileNumber;
            }
        }

        public void setDescription(final String description) {
            if (description == null) {
                mDescription = "";
            } else {
                mDescription = description;
            }
        }

        public String getAuthToken() {
            return mAuthToken;
        }


        public void setAuthToken(final String authToken) {

            if (authToken == null) {
                mAuthToken = "";
            } else {
                mAuthToken = authToken;
            }
        }

        public String getEmail() {
            return mEmail;
        }

        public void setEmail(final String email) {

            if (email == null) {
                mEmail = "";
            } else {
                mEmail = email;
            }
        }

        public String getId() {
            return mId;
        }

        public void setId(final String id) {

            if (id == null) {
                mId = "";
            } else {
                mId = id;
            }
        }

        public String getProfilePicture() {
            return mProfilePicture;
        }

        public void setProfilePicture(final String profilePicture) {

            if (profilePicture == null) {
                mProfilePicture = "";
            } else {
                mProfilePicture = profilePicture;
            }

        }

        public String getDeviceId() {
            return mDeviceId;
        }

        public void setDeviceId(final String deviceId) {
            mDeviceId = deviceId;
        }

        public String getDescription() {
            return mDescription;
        }

        public String getAuthHeader() {

            if (TextUtils.isEmpty(mAuthHeader)
                    && !TextUtils.isEmpty(mAuthToken)
                    && !TextUtils.isEmpty(mDeviceId)) {
                mAuthHeader = String
                        .format(Locale.US, HttpConstants.HEADER_AUTHORIZATION_FORMAT, mAuthToken,
                                mDeviceId);
            }
            return mAuthHeader;
        }

        public String getFirstName() {
            return mFirstName;
        }

        public void setFirstName(final String firstName) {

            mFirstName = firstName;
        }

        public String getLastName() {
            return mLastName;
        }

        public void setLastName(final String lastName) {

            mLastName = lastName;
        }

    }

    /**
     * Singleton to hold the current network state. Broadcast receiver for network state will be
     * used to keep this updated
     *
     * @author Anshul Kamboj
     */
    public enum DeviceInfo {

        INSTANCE;

        private final Location defaultLocation = new Location(LocationManager.PASSIVE_PROVIDER);

        private boolean mIsNetworkConnected;
        private int mCurrentNetworkType;
        private Location mLatestLocation;

        private DeviceInfo() {
            reset();
        }

        public void reset() {

            mIsNetworkConnected = false;
            mCurrentNetworkType = ConnectivityManager.TYPE_DUMMY;
            mLatestLocation = defaultLocation;
        }

        public boolean isNetworkConnected() {
            return mIsNetworkConnected;
        }

        public void setNetworkConnected(final boolean isNetworkConnected) {
            mIsNetworkConnected = isNetworkConnected;
        }

        public int getCurrentNetworkType() {
            return mCurrentNetworkType;
        }

        public void setCurrentNetworkType(final int currentNetworkType) {
            mCurrentNetworkType = currentNetworkType;
        }

        public Location getLatestLocation() {
            return mLatestLocation;
        }

        public void setLatestLocation(final Location latestLocation) {
            if (latestLocation == null) {
                mLatestLocation = defaultLocation;
            }
            mLatestLocation = latestLocation;
        }

    }

}
