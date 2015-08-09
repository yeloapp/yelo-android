
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
 */package red.yelo.http;

/**
 * @author Anshul Kamboj Interface that holds all constants related to Http
 *         Requests
 */
public class HttpConstants {


    /**
     * The API version in use by the app
     */
    private static final int API_VERSION = 1;
    private static Server SERVER = Server.PRODUCTION;

    public static String getApiBaseUrl() {
        return SERVER.mUrl;
    }

    public static String getChatUrl() {
        return SERVER.mChatUrl;
    }

    public static String getApiChatUrl() {
        return SERVER.mChatApiUrl;
    }

    public static String getGoogleGeocodeApiBaseUrl() {
        return GOOGLE_GEOCODE_API_BASEURL;
    }

    public static String getGoogleUserInfo() {
        return GOOGLE_USERINFO_API_URL;
    }

    public static String getGoogleUrlShortenerApiUrl() {
        return GOOGLE_API_SHORTENER_URL;
    }

    public static String getFacebookBaseUrl() {
        return FACEBOOK_API_URL;
    }


    public static String getChangedChatUrl() {
        return "http://" + SERVER.mChatUrl + SERVER.mChatLink;
    }

    public static int getChatPort() {
        return SERVER.mChatPort;
    }

    /**
     * Enum to switch between servers
     */
    private enum Server {

        LOCAL(
                "http://YOUR_LOCAL_SERVER/api/v",
                "http://YOUR_LOCAL_SERVER/api/v",
                API_VERSION,
                "YOUR_LOCAL_SERVER",
                8080),

        DEV(
                "http://YOUR_DEV_SERVER/api/v",
                "http://YOUR_DEV_SERVER/api/v",
                API_VERSION,
                "YOUR_DEV_SERVER",
                8080),

        PRODUCTION(
                "http://YOUR_PRODUCTION_SERVER/api/v",
                "http://YOUR_PRODUCTION_SERVER/api/v",
                API_VERSION,
                "YOUR_PRODUCTION_SERVER",
                8080);

        public final String mUrl;
        public final String mChatApiUrl;
        public final String mChatUrl;
        public final int mChatPort;
        public final String mChatLink = ":3000/api/v1";

        Server(final String url, final String chatApiUrl,
               final int apiVersion, final String chatUrl, final int chatPort) {
            mUrl = url + apiVersion;
            mChatUrl = chatUrl;
            mChatPort = chatPort;
            mChatApiUrl = chatApiUrl + apiVersion;
        }
    }


    //constants
    public static final String HEADER_AUTHORIZATION_FORMAT = "Token token=\"%s\", device_id=\"%s\"";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String GOOGLE_GEOCODE_API_BASEURL = "https://maps.googleapis.com/maps/api/";
    public static final String GOOGLE_USERINFO_API_URL = "https://www.googleapis.com/oauth2/v1";
    public static final String GOOGLE_API_SHORTENER_URL = "https://www.googleapis.com/urlshortener/v1";
    public static final String FACEBOOK_API_URL = "https://graph.facebook.com";
    public static final String FIELDS = "fields";


    //search params
    public static final String CAT_ID = "cat_id";
    public static final String SUB_CAT_IDS = "sub_cat_ids[]";
    public static final String SUB_CAT_ID = "sub_cat_ids";
    public static final String PAGE = "page";
    public static final String PER = "per";
    public static final String DISTANCE = "distance";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String LOCATION = "location";
    public static final String INPUT = "input";
    public static final String Q = "q";
    public static final String SUB_CAT_NAME = "sub_cat_name";

    //edit profile params
    public static final String MOBILE_NUMBER = "user[mobile_number]";
    public static final String FIRST_NAME = "user[name]";
    public static final String LAST_NAME = "user[last_name]";
    public static final String EMAIL = "user[email]";
    public static final String DESCRIPTION = "user[description]";
    public static final String USER_LATITUDE = "user[latitude]";
    public static final String USER_LONGITUDE = "user[longitude]";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String ALT = "alt";
    public static final String UTC_OFFSET = "utc_offset";


    //geo code params (for Google API)
    public static final String LATLNG = "latlng";
    public static final String RESULT_TYPE = "result_type";
    public static final String KEY = "key";
    public static final String PLACE_ID = "placeid";
    public static final String STREET_ADDRESS = "street_address";
    public static final String TYPE = "type";


    //chats
    public static final String SENDER_ID = "sender_id";
    public static final String RECEIVER_ID = "receiver_id";
    public static final String RECEIVED = "received";
    public static final String LIST_CAT_ID = "list_cat_id";
    public static final String LIST_USER_ID = "list_user_id";
    public static final String QUERY_USER_ID = "query_user_id";
    public static final String LIST_CAT_IDS = "user[list_cat_ids]";
    public static final String CHAT_ID = "chat_id";
    public static final String REPLY_ID = "reply_id";
    public static final String SENT_AT = "sent_at";
    public static final String MESSAGE = "message";
    public static final String TIME = "time";
    public static final String SENDER = "sender";
    public static final String RECEIVER = "receiver";
    public static final String ID_USER = "id_user";
    public static final String LIST_ID = "list_id";
    public static final String SENT_TIME = "sent_time";
    public static final String SERVER_SENT_AT = "server_sent_at";
    public static final String NAME = "name";
    public static final String SENDER_TYPE = "sender_type";
    public static final String CHAT_QUERY_ID = "chat_query_id";
    public static final String CHAT_QUERY_MESSAGE = "chat_query_message";
    public static final String SENDER_NAME = "sender_name";
    public static final String SENDER_IMAGE = "sender_image";
    public static final String RECEIVER_IMAGE = "receiver_image";
    public static final String RECEIVER_NAME = "receiver_name";
    public static final String CHATTER_ID = "chatter_id";
    public static final String STATUS = "status";

    //analytics
    public static final String REFERRAL_ID = "referral_id";
    public static final String DEVICE_ID = "device_id";

    //wall posts
    public static final String POST_MESSAGE = "wall[message]";
    public static final String POST_LATITUDE = "wall[latitude]";
    public static final String POST_LONGITUDE = "wall[longitude]";
    public static final String POST_CITY = "wall[city]";
    public static final String POST_ADDRESS = "wall[address]";
    public static final String POST_COUNTRY = "wall[country]";
    public static final String POST_TAG_ID = "wall[tag_id]";
    public static final String POST_GROUP_ID = "wall[group_id]";
    public static final String RADIUS = "radius";
    public static final String TAG_IDS = "tag_ids";
    public static final String UPDATE_POST = "update_post";
    public static final String GROUP_ID = "group_id";
    public static final String KEYWORDS = "keywords";

    //service cards
    public static final String SERVICE_TITLE = "service_card[title]";
    public static final String SERVICE_DURATION = "service_card[duration]";
    public static final String SERVICE_CATEGORY_ID = "service_card[category_id]";
    public static final String SERVICE_SUB_CATEGORY_ID = "tag_id";
    public static final String SERVICE_NOTE = "service_card[note]";
    public static final String SERVICE_DURATION_UNIT = "service_card[duration_unit]";
    public static final String SERVICE_DESCRIPTION = "service_card[description]";
    public static final String SERVICE_PRICE = "service_card[price]";
    public static final String SERVICE_LATITUDE = "service_card[latitude]";
    public static final String SERVICE_LONGITUDE = "service_card[longitude]";
    public static final String LISTING_ID = "listing_id";
    public static final String SERVICE_IMAGE_PATH = "image_path";
    public static final String UPDATE_CARD = "update_card";
    public static final String TITLE = "title";


    //notifications
    public static final String COLLAPSE_KEY = "collapse_key";
    public static final String RESOURCE = "resource";
    public static final String DEST = "dest";
    public static final String TAG = "tag";
    public static final String WALL_ID = "wall_id";
    public static final String PAYLOAD = "payload";


    //oath constants
    public static final String OAUTH_VERIFIER = "oauth_verifier";
    public static final String DENIED = "denied";

    //close loop
    public static final String USER_ID = "user_id";
    public static final String MOBILE_NUMBER_CLOSE = "mobile_number";
    public static final String IS_SOLVED = "is_solved";


    //TAGGING
    public static final String ARGS = "args";
    public static final String TAG_USERS = "tag_users";
    public static final String COMMENT = "comment";
    public static final String Tag_ID = "tag_id";

    //GCM CHAT STOP NOTIFICATION
    public static final String CHAT_ALERT = "chat alert";

    //COMMENTS
    public static final String COMMENT_MESSAGE = "comment[message]";


    /**
     * Constant interface. DO NOT IMPLEMENT.
     *
     * @author Anshul Kamboj
     */
    public static interface SearchType {
        //Different types of chat status. Linked to the chat_sending_status of database
        public static final String WALL = "wall";
        public static final String SERVICE_CARD = "service_card";
    }

    /**
     * Constant interface. DO NOT IMPLEMENT.
     *
     * @author Anshul Kamboj
     */
    public static interface FaceBookConstants {
        public static final String EMAIL = "email";
        public static final String BIO = "bio";
        public static final String WORK = "work";
        public static final String ABOUT = "about";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String PICTURE = "picture";
    }


    public static interface ApiResponseCodes {

        //GET REQUESTS 100-199
        public static final int GET_PLACES = 100;
        public static final int GET_SELECTED_PLACE = 101;
        public static final int GET_TAG_SUGGESTIONS = 102;
        public static final int GET_TAG_AUTO_SUGGESTIONS = 103;
        public static final int GET_GOOGLE_PROFILE = 104;
        public static final int GET_FACEBOOK_PROFILE = 105;
        public static final int GET_USER_WALLS = 106;
        public static final int GET_USER_DETAILS = 107;
        public static final int GET_PARTICULAR_WALL = 108;
        public static final int GET_ALL_WALLS = 109;
        public static final int GET_WALL_CONNECTS = 110;
        public static final int GET_SERVER_STATUS = 111;
        public static final int GET_RECOMMENDATIONS_MADE = 112;
        public static final int GET_RECOMMENDATIONS = 113;
        public static final int GET_RECOMMENDS = 114;
        public static final int GET_COLLECTIONS = 115;
        public static final int GET_GROUPS = 116;
        public static final int GET_SUB_CATEGORIES = 117;
        public static final int GET_SUGGESTIONS = 118;
        public static final int GET_REFERRAL_SCORE = 119;
        public static final int CLAIM_REWARD = 120;
        public static final int GET_LEADERS = 121;
        public static final int GET_MY_SERVICES = 122;
        public static final int GET_CATEGORY_SERVICES = 123;
        public static final int GET_USERS_SERVICES = 124;
        public static final int SEARCH_SERVICES = 125;
        public static final int GET_MISS_CALL = 126;
        public static final int GET_DISCOVER_GROUPS = 127;
        public static final int GET_WALL_COMMENTS = 128;


        //POST REQUESTS 200-299
        public static final int CREATE_LISTING = 200;
        public static final int ACTIVATE_LOGIN = 201;
        public static final int VERIFY_SMS = 202;
        public static final int CREATE_WALL = 203;
        public static final int TAG_USER = 204;
        public static final int CLOSE_WALL = 205;
        public static final int REPORT_ABUSE = 206;
        public static final int GET_SERIAL_CODE = 207;
        public static final int VERIFY_SMS_MANUAL = 208;
        public static final int UPLOAD_CONTACTS = 209;
        public static final int SHORTEN_URL = 210;
        public static final int CREATE_CARD = 211;
        public static final int SEND_INVITES = 212;
        public static final int CREATE_SERVICE_CARD = 213;
        public static final int RATE_SERVICE = 214;
        public static final int COMMENT = 215;


        //PUT REQUESTS 300-399
        public static final int UPDATE_PROFILE = 300;
        public static final int UPDATE_WALL = 301;
        public static final int UPDATE_LISTING = 302;
        public static final int UPDATE_GCM_ID = 303;
        public static final int UPDATE_SERVICE_CARD = 304;

        //DELETE REQUESTS 400-499
        public static final int DELETE_CHAT = 400;
        public static final int DELETE_WALL = 401;
        public static final int DELETE_SERVICE = 402;


    }

    public static interface ErrorCodes {

        public static final int NO_WALLS = 1;
    }


}
