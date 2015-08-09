
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
 */package red.yelo.data;

/**
 * Constant interface to hold table columns. DO NOT IMPLEMENT.
 *
 * @author Anshul Kamboj
 */
public interface DatabaseColumns {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String LIST_NAME = "list_name";
    public static final String TYPE = "type";
    public static final String IMAGE_URL = "image_url";
    public static final String MAIN_IMAGE = "main_image";
    public static final String CATEGORY_ID = "category_id";
    public static final String CATEGORY_NAME = "category_name";
    public static final String CATEGORY_IMAGE = "category_image";
    public static final String TAGS = "tags";
    public static final String MOBILE_NUMBER = "mobile_number";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String COUNTRY = "country";
    public static final String CITY = "city";
    public static final String STATE = "state";
    public static final String ZIP_CODE = "zip_code";
    public static final String CUSTOMERCARE_NUMBER = "customercare_number";
    public static final String LANDLINE_NUMBER = "landline_number";
    public static final String ADDRESS = "address";
    public static final String WEBSITE = "website";
    public static final String TWITTER_LINK = "twitter_link";
    public static final String FACEBOOK_LINK = "facebook_link";
    public static final String LINKEDIN_LINK = "linkedin_link";
    public static final String CATEGORIES = "categories";
    public static final String CHAT_ID = "chat_id";
    public static final String SERVER_CHAT_ID = "server_chat_id";
    public static final String TIMESTAMP = "timestamp";
    public static final String MESSAGE = "message";
    public static final String TIMESTAMP_HUMAN = "timestamp_human";
    public static final String TIMESTAMP_EPOCH = "timestamp_epoch";
    public static final String TIMESTAMP_EPOCH_UPDATED_AT = "timestamp_epoch_updated_at";
    public static final String SENT_AT = "sent_at";
    public static final String CHAT_TYPE = "chat_type";
    public static final String LAST_MESSAGE_ID = "last_message_id";
    public static final String SENDER_ID = "sender_id";
    public static final String RECEIVER_ID = "receiver_id";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String CHAT_QUERY_ID = "chat_query_id";
    public static final String SENDER_NAME = "sender_name";
    public static final String SENDER_IMAGE = "sender_image";
    public static final String RECEIVER_NAME = "receiver_name";
    public static final String RECEIVER_IMAGE = "receiver_image";
    public static final String IMAGE = "image";
    public static final String LIST_ID = "list_id";
    public static final String USER_ID = "user_id";
    public static final String TAG_NAME = "tag_name";
    public static final String QUERY_USER_ID = "query_user_id";
    public static final String LIST_USER_ID = "list_user_id";
    public static final String TAGSNAMES = "tag_names";
    public static final String LISTING_TYPE = "listing_type";
    public static final String RATING = "rating";
    public static final String COMMENT = "comment";
    public static final String REVIEW_OWNER_ID = "review_owner_id";
    public static final String TAG_ID = "tag_id";
    public static final String TAG_USER_COUNT = "tag_user_count";
    public static final String CHAT_USER_COUNT = "chat_user_count";
    public static final String USER_DESCRIPTION = "user_description";
    public static final String TAGGED_USER_IDS = "tagged_user_ids";
    public static final String TAGGED_NAMES = "tagged_names";
    public static final String TAGGED_IMAGE_URLS = "tagged_image_urls";
    public static final String DATE_TIME = "date_time";
    public static final String WALL_ID = "wall_id";
    public static final String USER_NAME = "user_name";
    public static final String USER_IMAGE = "user_image";
    public static final String USER_TAGS = "user_tags";
    public static final String PLACE_NAME = "place_name";
    public static final String PIN_CODE = "pin_code";
    public static final String PLACE_ID = "place_id";
    public static final String WALL_IMAGES = "wall_images";
    public static final String TAGGED_USER_NUMBERS = "tagged_user_numbers";
    public static final String TAGGED_USER_EMAILS = "tagged_user_emails";
    public static final String TAGGED_IDS = "tagged_ids";
    public static final String STARS = "stars";
    public static final String TAG_COUNT = "tag_count";
    public static final String REVIEW_COUNT = "review_count";
    public static final String AVERAGE_RATING = "average_rating";
    public static final String USER_TAGS_IDS = "user_tags_ids";
    public static final String WALL_USER_ID = "wall_user_id";
    public static final String KEY = "key";
    public static final String NOTIFICATION_STATUS = "notification_status";
    public static final String STATUS = "status";
    public static final String TEMP_ID = "temp_id";
    public static final String CONNECT_COUNT = "connect_count";
    public static final String NUMBER = "number";
    public static final String IS_PRESENT = "is_present";
    public static final String COUNT = "count";
    public static final String HEADING = "heading";
    public static final String SUB_HEADING = "sub_heading";
    public static final String COLOR = "color";
    public static final String TAG_IDS = "tag_ids";
    public static final String AVATAR = "avatar";
    public static final String AVATAR_DESCRIPTION = "avatar_description";
    public static final String COLLECTION_ID = "collection_id";
    public static final String EMAIL = "email";
    public static final String KEYWORDS = "keywords";
    public static final String REFERRAL_COUNT = "referral_count";
    public static final String GROUP_NAME = "group_name";
    public static final String SUBGROUP_NAME = "subgroup_name";
    public static final String GROUP_ID = "group_id";
    public static final String SUBGROUP_ID = "subgroup_id";
    public static final String URL = "url";
    public static final String URL_NAME = "url_name";
    public static final String AMOUNT = "amount";
    public static final String TIMESTAMP_HUMAN_UPDATED_AT = "timestamp_human_updated_at";
    public static final String TITLE = "title";
    public static final String SERVICE_IMAGE = "service_image";
    public static final String SERVICE_DESCRIPTION = "service_description";
    public static final String SERVICE_PRICE = "service_price";
    public static final String USER_NUMBER = "user_number";
    public static final String RATING_COUNT = "rating_count";
    public static final String CURRENCY = "currency";
    public static final String VERIFIED = "verified";
    public static final String DURATION = "duration";
    public static final String DELIVERABLE = "deliverable";
    public static final String BOOK_COUNT = "book_count";
    public static final String VIEW_COUNT = "view_count";
    public static final String COMMENT_USER_COUNT = "comment_user_count";


    /**
     * Indicates the status of a chat message
     * <ul>
     * <li>0 - Sending</li>
     * <li>1 - Sent</li>
     * <li>2 - Received</li>
     * <li>-1 - Failed</li>
     * </ul>
     */
    public static final String CHAT_STATUS = "chat_status";

    /**
     * @deprecated Not used as of DB Version 4
     */
    public static final String CHAT_ACK = "sending_ack";


}
