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

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import java.text.ParseException;

import red.yelo.R;
import red.yelo.analytics.Attachment;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.TableDiscoverTags;
import red.yelo.data.TableMyWallComments;
import red.yelo.data.TableMyWallPosts;
import red.yelo.data.TableServices;
import red.yelo.data.TableSubCategories;
import red.yelo.data.TableUsers;
import red.yelo.data.TableWallComments;
import red.yelo.data.TableWallPosts;
import red.yelo.http.HttpConstants.ApiResponseCodes;
import red.yelo.retromodels.GetUserModel;
import red.yelo.retromodels.ReferralScoreModel;
import red.yelo.retromodels.ServiceCardsResponseModel;
import red.yelo.retromodels.ServiceCardsResponseModel.ServiceCard;
import red.yelo.retromodels.response.CommentResponseModel;
import red.yelo.retromodels.response.CreateUserResponseModel;
import red.yelo.retromodels.response.GetCollectionResponseModel;
import red.yelo.retromodels.response.GetCreateServiceCardResponse;
import red.yelo.retromodels.response.GetCreateWallResponseModel;
import red.yelo.retromodels.response.GetServiceCardResponseModel;
import red.yelo.retromodels.response.GetSubcategoryResponseModel;
import red.yelo.retromodels.response.GetWallItemResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.QueryTokens;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Created by anshul1235 on 27/10/14.
 */
public class RetroCallback<ConvertedData> implements RetroCallbackInterface<ConvertedData>, DBInterface.AsyncDbQueryCallback {

    public static final String TAG = "RetroCallback";

    /**
     * Listener to retrieve models and request codes
     */
    private RetroResponseListener mRetroResponseListener;


    private boolean isCancelled;
    private int mRequestId;

    private Bundle extras;

    private DateFormatter mMessageDateFormatter;


    /**
     * @return requestid, used to through success callbacks
     */
    @Override
    public int getRequestId() {
        return mRequestId;
    }


    /**
     * @param code set code which is used to assign per request for handling
     * @return requestid, used to through success callbacks
     */
    @Override
    public int setRequestId(int code) {
        this.mRequestId = code;
        return code;
    }


    /**
     * sets the cancel flag for the request to true, it will not give a callback after being cancelled
     */
    @Override
    public void cancel() {
        isCancelled = true;
    }


    @Override
    public void setExtras(Bundle extras) {
        this.extras = extras;

    }


    /**
     * @param retroResponseListener An implementation if
     *                              {@link red.yelo.http.RetroCallback.RetroResponseListener)} to receive events
     *                              when we get the responses
     */
    public RetroCallback(final RetroResponseListener retroResponseListener) {
        this.mRetroResponseListener = retroResponseListener;
        this.mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, AppConstants.MESSAGE_TIME_FORMAT);
    }


    /**
     * @return true if cancelled and vice-versa
     */
    @Override
    public boolean isCanceled() {
        return isCancelled;
    }


    /**
     * method to handle the response according to the request codes assigned
     *
     * @param model    contains the response model from the server
     * @param response https://commondatastorage.googleapis.com/yelostore/uploads/user/image/545df4f46261721772000000/545df4f462617217720000001415611121.jpg
     */
    @Override
    public void success(ConvertedData model, Response response) {

        if (isCanceled()) return;
        switch (mRequestId) {
            /**
             * perform background processes here
             */
            case HttpConstants.ApiResponseCodes.TAG_USER:

                GetWallItemResponseModel getWallItemResponseModel = ((GetWallItemResponseModel) model);
                parseStoreTagUserResponse(getWallItemResponseModel);

                break;

            case HttpConstants.ApiResponseCodes.CREATE_WALL:


                GetCreateWallResponseModel createWallResponseModel = ((GetCreateWallResponseModel) model);
                parseStoreCreateWallResponse(createWallResponseModel);


                break;

            case ApiResponseCodes.COMMENT: {
                String wallid = extras.getString(AppConstants.Keys.WALL_ID);
                String userId = extras.getString(AppConstants.Keys.USER_ID);
                String tempId = extras.getString(AppConstants.Keys.TEMP_ID);

                CommentResponseModel comment = ((CommentResponseModel) model);
                parseAndStoreSingleComment(comment, wallid, userId,tempId);
            }

                break;


            case ApiResponseCodes.UPDATE_WALL:

                GetCreateWallResponseModel updateWallResponseModel = ((GetCreateWallResponseModel) model);
                parseStoreCreateWallResponse(updateWallResponseModel);
                break;


            case HttpConstants.ApiResponseCodes.CLOSE_WALL:

                String selectionClose = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                String wallid = extras.getString(AppConstants.Keys.WALL_ID);

                DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_WALL_POST, null, null, TableWallPosts.NAME, selectionClose, new String[]{wallid}, true, this);
                DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_MY_WALL_POST, null, null, TableMyWallPosts.NAME, selectionClose, new String[]{wallid}, true, this);

                mRetroResponseListener.success(model, mRequestId);

                break;

            case HttpConstants.ApiResponseCodes.GET_SUB_CATEGORIES:

                if (extras != null) {

                    String categoryId = extras.getString(AppConstants.Keys.TAG_ID);
                    GetSubcategoryResponseModel getSubcategoryResponseModel = ((GetSubcategoryResponseModel) model);

                    parseStoreSubCategories(categoryId, getSubcategoryResponseModel);


                }
                mRetroResponseListener.success(model, mRequestId);
                break;


            case HttpConstants.ApiResponseCodes.UPDATE_PROFILE:

                CreateUserResponseModel userResponseModel = ((CreateUserResponseModel) model);

                parseStoreUserDetails(userResponseModel);

                if (extras != null) {
                    if (!extras.getBoolean(AppConstants.Keys.UPDATE)) {
                        mRetroResponseListener.success(model, mRequestId);
                    }
                }
                break;


            case HttpConstants.ApiResponseCodes.GET_PARTICULAR_WALL:

                GetCreateWallResponseModel wallResponseModel = ((GetCreateWallResponseModel) model);
                parseStoreWallResponse(wallResponseModel);

                //this gives call back to the fragment from where it is called
                mRetroResponseListener.success(model, mRequestId);
                break;


            case HttpConstants.ApiResponseCodes.GET_WALL_COMMENTS:

                GetCreateWallResponseModel wallCommentsModel = ((GetCreateWallResponseModel) model);
                parseStoreWallComments(wallCommentsModel);

                //this gives call back to the fragment from where it is called
                mRetroResponseListener.success(model, mRequestId);
                break;


            case HttpConstants.ApiResponseCodes.GET_REFERRAL_SCORE:
                mRetroResponseListener.success(model, mRequestId);
                break;


            case ApiResponseCodes.CLAIM_REWARD:
                mRetroResponseListener.success(model, mRequestId);
                break;

            case ApiResponseCodes.GET_MY_SERVICES:
                //ServiceCardsResponseModel serviceCardsResponseModel = ((ServiceCardsResponseModel) model);
                //parseStoreServiceCardsList(serviceCardsResponseModel);
                GetServiceCardResponseModel getMyServiceCardResponseModel = ((GetServiceCardResponseModel) model);
                parseStoreUserServiceCardsList(getMyServiceCardResponseModel);
                mRetroResponseListener.success(model, mRequestId);
                break;

            case ApiResponseCodes.GET_USERS_SERVICES:
                GetServiceCardResponseModel getUsersServiceCardResponseModel = ((GetServiceCardResponseModel) model);
                parseStoreUserServiceCardsList(getUsersServiceCardResponseModel);
                mRetroResponseListener.success(model, mRequestId);
                break;


            case ApiResponseCodes.GET_CATEGORY_SERVICES:
                GetServiceCardResponseModel getServiceCardResponseModel = ((GetServiceCardResponseModel) model);
                parseStoreSearchServiceCardsList(getServiceCardResponseModel);
                mRetroResponseListener.success(model, mRequestId);
                break;


            case ApiResponseCodes.SEARCH_SERVICES:
                GetServiceCardResponseModel getSearchServiceCardResponseModel = ((GetServiceCardResponseModel) model);
                parseStoreSearchServiceCardsList(getSearchServiceCardResponseModel);
                mRetroResponseListener.success(model, mRequestId);
                break;

            case ApiResponseCodes.GET_DISCOVER_GROUPS:
                GetCollectionResponseModel getCollectionResponseModel = ((GetCollectionResponseModel) model);
                for (GetCollectionResponseModel.Collection eachEntry : getCollectionResponseModel.groups) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.ID, eachEntry.id);
                    values.put(DatabaseColumns.NAME, eachEntry.name);
                    values.put(DatabaseColumns.COLOR, eachEntry.color);
                    values.put(DatabaseColumns.TYPE,AppConstants.GroupType.DISCOVER);
                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_DISCOVER_TAGS_SUGGESSTIONS,
                            null, values, TableDiscoverTags.NAME, values, selection,
                            new String[]{eachEntry.id}, true, this);
                }
                mRetroResponseListener.success(model, mRequestId);
                break;


            case ApiResponseCodes.GET_USER_DETAILS:
                GetUserModel getUserModel = ((GetUserModel) model);
                if (getUserModel.user.id.equals(AppConstants.UserInfo.INSTANCE.getId())) {
                    SharedPreferenceHelper.set(R.string.pref_first_name, getUserModel.user.name);
                    SharedPreferenceHelper.set(R.string.pref_profile_image, getUserModel.user.image_url);
                    SharedPreferenceHelper.set(R.string.pref_description, getUserModel.user.description);
                    SharedPreferenceHelper.set(R.string.pref_share_token, getUserModel.user.share_token);


                    AppConstants.UserInfo.INSTANCE.setDescription(getUserModel.user.description);
                    AppConstants.UserInfo.INSTANCE.setFirstName(getUserModel.user.name);
                    AppConstants.UserInfo.INSTANCE.setProfilePicture(getUserModel.user.image_url);

                    MixpanelAnalytics.getInstance().nameUser(AppConstants.UserInfo.INSTANCE.getFirstName());
                }

                mRetroResponseListener.success(model, mRequestId);
                break;



            default:
                mRetroResponseListener.success(model, mRequestId);

                break;
        }
    }


    /**
     * @param error
     */
    @Override
    public void failure(RetrofitError error) {
        if (isCanceled()) return;
        String temp_id;

        switch (mRequestId) {

            case HttpConstants.ApiResponseCodes.TAG_USER:

                String selectTagUser = DatabaseColumns.TEMP_ID + SQLConstants.EQUALS_ARG;

                temp_id = extras.getString(AppConstants.Keys.TEMP_ID);

                DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_TAG_USER, null, null, TableWallComments.NAME, selectTagUser, new String[]{temp_id}, true, this);

                //mRetroResponseListener.failure(mRequestId, 400, "");


                break;

            case HttpConstants.ApiResponseCodes.CREATE_WALL:

                String selectWall = DatabaseColumns.TEMP_ID + SQLConstants.EQUALS_ARG;

                temp_id = extras.getString(AppConstants.Keys.TEMP_ID);

                DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_WALL_POST, null, null, TableWallPosts.NAME, selectWall, new String[]{temp_id}, true, this);
                break;

            case ApiResponseCodes.GET_MY_SERVICES:
                Logger.e(TAG, "Get My Services Failed");
                break;


            default:
                mRetroResponseListener.failure(mRequestId, 400, error.getMessage());
        }

        //mRetroResponseListener.failure(mRequestId,error.getResponse().getStatus());
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }


    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }


    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

        if (taskId == AppConstants.QueryTokens.UPDATE_WALLCOMMENTS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLCOMMENT, null, null, TableWallComments.NAME, null, values, true, this);
            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_MY_WALLCOMMENTS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLCOMMENT, null, null, TableMyWallComments.NAME, null, values, true, this);
            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_WALLPOST) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;

                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, null, null, TableWallPosts.NAME, null, values, true, this);
            }
        }
        if (taskId == AppConstants.QueryTokens.UPDATE_MY_WALLPOST) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;

                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, null, null, TableMyWallPosts.NAME, null, values, true, this);
            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_SUBCATEGORIES) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_SUBCATEGORIES, null, null, TableSubCategories.NAME, null, values, true, this);

            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_WALLCOMMENTS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLCOMMENT, null, null, TableWallComments.NAME, null, values, true, this);
            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_WALLPOST) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, null, null, TableWallPosts.NAME, null, values, true, this);
            }
        }

        if (taskId == QueryTokens.UPDATE_SERVICE_LIST) {
            if (updateCount == 0) {
                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_SERVICE_LIST, null, null, TableServices.NAME, null, values, true, this);
            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_DISCOVER_TAGS_SUGGESSTIONS) {
            if (updateCount == 0) {
                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_DISCOVER_TAG_SUGGESSTIONS, null,
                        null, TableDiscoverTags.NAME, null, values, true, this);
            }
        }
    }


    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }


    /**
     * interface which gives a callback on the UI for success of the responses
     *
     * @param <T>
     */
    public static interface RetroResponseListener<T> {

        /**
         * Method callback when the success response is received
         *
         * @param model     model response received from the server
         * @param requestId The id of the response
         */
        public void success(T model, int requestId);

        /**
         * Method callback when the request is failed
         *
         * @param requestId The id of the response
         * @param errorCode The errorcode of the response
         */
        public void failure(int requestId, int errorCode, String message);


    }


    // parsing methods




    /**
     * This parses the wall response into local cache to show on the fragments
     *
     * @param wallResponseModel its the model which holds the values
     */
    private void parseStoreWallComments(GetCreateWallResponseModel wallResponseModel) {

        if (wallResponseModel != null) {
            ContentValues values = new ContentValues(6);

            try {
                values.put(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, mMessageDateFormatter.getEpoch(wallResponseModel.wall.updated_at));


            } catch (ParseException e) {
                e.printStackTrace();
                //should not happen
            }
            if (wallResponseModel.wall.wall_image != null) {
                values.put(DatabaseColumns.WALL_IMAGES, wallResponseModel.wall.wall_image.image_url);
            }
            values.put(DatabaseColumns.USER_IMAGE, wallResponseModel.wall.wall_owner.image_url);


            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


            DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, null, values, TableWallPosts.NAME, values, selection, new String[]{wallResponseModel.wall.id}, false, this);


            if (wallResponseModel.wall.comments.size() != 0) {
                for (int j = 0; j < wallResponseModel.wall.comments.size(); j++) {


                    GetCreateWallResponseModel.Comments comment = wallResponseModel.wall.comments.get(j);
                    ContentValues valuesComments = new ContentValues();
                    valuesComments.put(DatabaseColumns.WALL_ID, wallResponseModel.wall.id);
                    valuesComments.put(DatabaseColumns.ID, comment.id);
                    valuesComments.put(DatabaseColumns.WALL_USER_ID, wallResponseModel.wall.wall_owner.user_id);
                    valuesComments.put(DatabaseColumns.COMMENT, comment.message);
                    valuesComments.put(DatabaseColumns.USER_ID, comment.userDetails.userId);
                    valuesComments.put(DatabaseColumns.DATE_TIME, comment.createdAt);
                    valuesComments.put(DatabaseColumns.TAGGED_NAMES, comment.userDetails.name);
                    valuesComments.put(DatabaseColumns.TYPE, AppConstants.CommentType.COMMENTS);
                    valuesComments.put(DatabaseColumns.TAGGED_IMAGE_URLS, comment.userDetails.image_url);
                    valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, comment.userDetails.userId);

                    try {
                        valuesComments.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(comment.createdAt));
                        valuesComments.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(comment.createdAt));


                    } catch (ParseException e) {
                        e.printStackTrace();
                        //should not happen
                    }
                    valuesComments.put(DatabaseColumns.IS_PRESENT, "true");


                    String selectionWallId = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, null,
                            valuesComments, TableWallComments.NAME, valuesComments, selectionWallId,
                            new String[]{comment.id}, false, this);
                }
            }

        }

    }


    /**
     * This parses the wall response into local cache to show on the fragments
     *
     * @param wallResponseModel its the model which holds the values
     */
    private void parseStoreWallResponse(GetCreateWallResponseModel wallResponseModel) {

        if (wallResponseModel != null) {
            ContentValues values = new ContentValues(6);
            values.put(DatabaseColumns.ID, wallResponseModel.wall.id);
            values.put(DatabaseColumns.MESSAGE, wallResponseModel.wall.message);
            values.put(DatabaseColumns.TAG_NAME, wallResponseModel.wall.tag_name);
            values.put(DatabaseColumns.TAG_ID, wallResponseModel.wall.tag_id);
            values.put(DatabaseColumns.TAG_USER_COUNT, wallResponseModel.wall.tagged_users_count);
            values.put(DatabaseColumns.CHAT_USER_COUNT, wallResponseModel.wall.chat_users_count);
            values.put(DatabaseColumns.COMMENT_USER_COUNT, wallResponseModel.wall.comments_count);
            values.put(DatabaseColumns.USER_NAME, wallResponseModel.wall.wall_owner.name);
            values.put(DatabaseColumns.USER_ID, wallResponseModel.wall.wall_owner.user_id);
            values.put(DatabaseColumns.DATE_TIME, wallResponseModel.wall.created_at);
            values.put(DatabaseColumns.CITY, wallResponseModel.wall.city);
            values.put(DatabaseColumns.COUNTRY, wallResponseModel.wall.country);
            values.put(DatabaseColumns.GROUP_ID, wallResponseModel.wall.group_id);
            values.put(DatabaseColumns.GROUP_NAME, wallResponseModel.wall.group_name);
            values.put(DatabaseColumns.COLOR, wallResponseModel.wall.group_color);


            if (TextUtils.isEmpty(wallResponseModel.wall.address)) {
                values.put(DatabaseColumns.ADDRESS, Character.toUpperCase(wallResponseModel.wall.city.charAt(0)) + wallResponseModel.wall.city.substring(1));

            } else {
                values.put(DatabaseColumns.ADDRESS, wallResponseModel.wall.address);
            }


            try {
                values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(wallResponseModel.wall.created_at));
                values.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(wallResponseModel.wall.created_at));
                values.put(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, mMessageDateFormatter.getEpoch(wallResponseModel.wall.updated_at));


            } catch (ParseException e) {
                e.printStackTrace();
                //should not happen
            }
            if (wallResponseModel.wall.wall_image != null) {
                values.put(DatabaseColumns.WALL_IMAGES, wallResponseModel.wall.wall_image.image_url);
            }
            values.put(DatabaseColumns.USER_IMAGE, wallResponseModel.wall.wall_owner.image_url);


            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


            DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, null, values, TableWallPosts.NAME, values, selection, new String[]{wallResponseModel.wall.id}, false, this);


            if (wallResponseModel.wall.wall_items.size() != 0) {
                for (int j = 0; j < wallResponseModel.wall.wall_items.size(); j++) {


                    GetWallItemResponseModel.WallItem wallItem = wallResponseModel.wall.wall_items.get(j);
                    ContentValues valuesComments = new ContentValues();
                    valuesComments.put(DatabaseColumns.WALL_ID, wallResponseModel.wall.id);
                    valuesComments.put(DatabaseColumns.ID, wallItem.id);
                    valuesComments.put(DatabaseColumns.WALL_USER_ID, wallResponseModel.wall.wall_owner.user_id);
                    valuesComments.put(DatabaseColumns.COMMENT, wallItem.comment);
                    valuesComments.put(DatabaseColumns.USER_ID, wallItem.user_id);
                    valuesComments.put(DatabaseColumns.DATE_TIME, wallItem.created_at);
                    valuesComments.put(DatabaseColumns.USER_NAME, wallItem.name);
                    valuesComments.put(DatabaseColumns.TYPE, AppConstants.CommentType.REFER);
                    valuesComments.put(DatabaseColumns.IMAGE_URL, wallItem.image_url);
                    try {
                        valuesComments.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(wallItem.created_at));
                        valuesComments.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(wallItem.created_at));


                    } catch (ParseException e) {
                        e.printStackTrace();
                        //should not happen
                    }
                    if (wallItem.tagged_users.size() > 0) {
                        valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.tagged_users.get(0).id);
                        valuesComments.put(DatabaseColumns.TAGGED_NAMES, wallItem.tagged_users.get(0).name);
                        if (wallItem.tagged_users.get(0).details != null) {
                            valuesComments.put(DatabaseColumns.TAGGED_USER_NUMBERS, wallItem.tagged_users.get(0).details.mobile_number);
                            valuesComments.put(DatabaseColumns.TAGGED_USER_EMAILS, wallItem.tagged_users.get(0).details.email);

                        }
                        valuesComments.put(DatabaseColumns.TAGGED_IMAGE_URLS, wallItem.tagged_users.get(0).image_url);
                        valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.tagged_users.get(0).user_id);
                        valuesComments.put(DatabaseColumns.TAGGED_IDS, wallItem.tagged_users.get(0).id);
                        valuesComments.put(DatabaseColumns.IS_PRESENT, wallItem.tagged_users.get(0).is_present + "");


                    }
                    String selectionWallId = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, null, valuesComments, TableWallComments.NAME, valuesComments, selectionWallId, new String[]{wallItem.id}, false, this);
                }
            }


            if (wallResponseModel.wall.wall_chats.size() != 0) {
                for (int j = 0; j < wallResponseModel.wall.wall_chats.size(); j++) {


                    GetCreateWallResponseModel.WallChats wallItem = wallResponseModel.wall.wall_chats.get(j);
                    ContentValues valuesComments = new ContentValues();
                    valuesComments.put(DatabaseColumns.WALL_ID, wallResponseModel.wall.id);
                    valuesComments.put(DatabaseColumns.ID, wallResponseModel.wall.id + wallItem.user_id);
                    valuesComments.put(DatabaseColumns.WALL_USER_ID, wallResponseModel.wall.wall_owner.user_id);
                    valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.user_id);
                    valuesComments.put(DatabaseColumns.TAGGED_NAMES, wallItem.name);
                    valuesComments.put(DatabaseColumns.USER_ID, wallResponseModel.wall.wall_owner.user_id);
                    valuesComments.put(DatabaseColumns.USER_NAME, wallResponseModel.wall.wall_owner.name);
                    valuesComments.put(DatabaseColumns.TYPE, AppConstants.CommentType.CHAT);
                    valuesComments.put(DatabaseColumns.TAGGED_IMAGE_URLS, wallItem.image_url);
                    valuesComments.put(DatabaseColumns.TAGGED_IDS, wallItem.user_id);
                    valuesComments.put(DatabaseColumns.DATE_TIME, wallItem.last_chat);


                    try {
                        valuesComments.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(wallResponseModel.wall.created_at));
                        valuesComments.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(wallResponseModel.wall.created_at));

                    } catch (ParseException e) {
                        e.printStackTrace();
                        //should not happen
                    }
                    valuesComments.put(DatabaseColumns.IS_PRESENT, "true");


                    String selectionWallId = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, null, valuesComments, TableWallComments.NAME, valuesComments, selectionWallId, new String[]{wallResponseModel.wall.id + wallItem.user_id}, false, this);
                }
            }

        }

    }


    private void parseStoreUserDetails(CreateUserResponseModel userResponseModel) {
        if (userResponseModel.user.id.equals(AppConstants.UserInfo.INSTANCE.getId())) {

            AppConstants.UserInfo.INSTANCE.setFirstName(userResponseModel.user.name);
            AppConstants.UserInfo.INSTANCE.setProfilePicture(userResponseModel.user.image_url);
            AppConstants.UserInfo.INSTANCE.setDescription(userResponseModel.user.description);

            SharedPreferenceHelper.set(R.string.pref_first_name, userResponseModel.user.name);
            SharedPreferenceHelper.set(R.string.pref_profile_image, userResponseModel.user.image_url);
            SharedPreferenceHelper.set(R.string.pref_user_id, userResponseModel.user.id);
            SharedPreferenceHelper.set(R.string.pref_description, userResponseModel.user.description);

            ContentValues valuesUser = new ContentValues();
            valuesUser.put(DatabaseColumns.USER_IMAGE, userResponseModel.user.image_url);
            valuesUser.put(DatabaseColumns.USER_NAME, userResponseModel.user.name);
            valuesUser.put(DatabaseColumns.USER_DESCRIPTION, userResponseModel.user.description);

            String selectionUser = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

            DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_USERS, null, valuesUser, TableUsers.NAME, valuesUser, selectionUser, new String[]{AppConstants.UserInfo.INSTANCE.getId()}, true, this);


        }
    }


    private void parseStoreSubCategories(String categoryId, GetSubcategoryResponseModel getSubcategoryResponseModel) {
        for (int i = 0; i < getSubcategoryResponseModel.tags.size(); i++) {

            ContentValues valuesSubCategories = new ContentValues();
            valuesSubCategories.put(DatabaseColumns.ID, getSubcategoryResponseModel.tags.get(i).id);

            String name = getSubcategoryResponseModel.tags.get(i).name;
            valuesSubCategories.put(DatabaseColumns.NAME, name);
            valuesSubCategories.put(DatabaseColumns.CATEGORY_ID, categoryId);
            if (getSubcategoryResponseModel.tags.get(i).keywords.size() != 0) {
                // valuesSubCategories.put(DatabaseColumns.KEYWORDS, eachEntry.keywords.get(0));
            }
            String selectionId = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

            DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_SUBCATEGORIES, null, valuesSubCategories, TableSubCategories.NAME, valuesSubCategories, selectionId, new String[]{getSubcategoryResponseModel.tags.get(i).id}, true, this);
        }

    }


    private void parseStoreCreateWallResponse(GetCreateWallResponseModel createWallResponseModel) {
        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, AppConstants.WALL_DATE_FORMAT);

        final String postCategory = createWallResponseModel.wall.tag_name;
        final GetCreateWallResponseModel.WallImage wallImage = createWallResponseModel.wall.wall_image;
        final String message = createWallResponseModel.wall.message;
        MixpanelAnalytics.getInstance().onPostCreated(postCategory, wallImage != null ? Attachment.PHOTO : Attachment.NONE, message != null ? message.length() : 0);

        ContentValues valuesWall = new ContentValues(6);
        valuesWall.put(DatabaseColumns.ID, createWallResponseModel.wall.id);
        valuesWall.put(DatabaseColumns.MESSAGE, createWallResponseModel.wall.message);
        valuesWall.put(DatabaseColumns.TAG_NAME, createWallResponseModel.wall.tag_name);
        valuesWall.put(DatabaseColumns.STATE, AppConstants.SyncStates.SYNCED);
        valuesWall.put(DatabaseColumns.TAG_ID, createWallResponseModel.wall.tag_id);
        valuesWall.put(DatabaseColumns.TAG_USER_COUNT, createWallResponseModel.wall.tagged_users_count);
        valuesWall.put(DatabaseColumns.CHAT_USER_COUNT, createWallResponseModel.wall.chat_users_count);
        valuesWall.put(DatabaseColumns.COMMENT_USER_COUNT, createWallResponseModel.wall.comments_count+"");
        valuesWall.put(DatabaseColumns.USER_NAME, createWallResponseModel.wall.wall_owner.name);
        valuesWall.put(DatabaseColumns.USER_ID, createWallResponseModel.wall.wall_owner.user_id);
        valuesWall.put(DatabaseColumns.DATE_TIME, createWallResponseModel.wall.created_at);
        valuesWall.put(DatabaseColumns.CITY, createWallResponseModel.wall.city);
        valuesWall.put(DatabaseColumns.COUNTRY, createWallResponseModel.wall.country);
        valuesWall.put(DatabaseColumns.GROUP_ID, createWallResponseModel.wall.group_id);
        valuesWall.put(DatabaseColumns.GROUP_NAME, createWallResponseModel.wall.group_name);
        valuesWall.put(DatabaseColumns.COLOR, createWallResponseModel.wall.group_color);
        if (TextUtils.isEmpty(createWallResponseModel.wall.address)) {
            valuesWall.put(DatabaseColumns.ADDRESS, Character.toUpperCase(createWallResponseModel.wall.city.charAt(0)) + createWallResponseModel.wall.city.substring(1));

        } else {
            valuesWall.put(DatabaseColumns.ADDRESS, createWallResponseModel.wall.address);
        }
        try {
            valuesWall.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(createWallResponseModel.wall.created_at));
            valuesWall.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(createWallResponseModel.wall.created_at));
            valuesWall.put(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, mMessageDateFormatter.getEpoch(createWallResponseModel.wall.updated_at));


        } catch (ParseException e) {
            e.printStackTrace();
            //should not happen
        }
        if (createWallResponseModel.wall.wall_image != null) {
            valuesWall.put(DatabaseColumns.WALL_IMAGES, createWallResponseModel.wall.wall_image.image_url);
        }
        valuesWall.put(DatabaseColumns.USER_IMAGE, createWallResponseModel.wall.wall_owner.image_url);


        String selectionWall = DatabaseColumns.TEMP_ID + SQLConstants.EQUALS_ARG;
        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, null, valuesWall, TableWallPosts.NAME, valuesWall, selectionWall, new String[]{createWallResponseModel.wall.tmp_id}, true, this);
    }


    private void parseStoreTagUserResponse(GetWallItemResponseModel getWallItemResponseModel) {
        ContentValues values = new ContentValues();
        values.put(DatabaseColumns.WALL_ID, extras.getString(AppConstants.Keys.WALL_ID));
        values.put(DatabaseColumns.ID, getWallItemResponseModel.wall_item.id);
        values.put(DatabaseColumns.COMMENT, getWallItemResponseModel.wall_item.comment);
        values.put(DatabaseColumns.STATE, AppConstants.SyncStates.SYNCED);
        values.put(DatabaseColumns.DATE_TIME, getWallItemResponseModel.wall_item.created_at);
        values.put(DatabaseColumns.WALL_USER_ID, extras.getString(AppConstants.Keys.USER_ID));
        values.put(DatabaseColumns.USER_ID, getWallItemResponseModel.wall_item.user_id);
        values.put(DatabaseColumns.USER_NAME, getWallItemResponseModel.wall_item.name);
        values.put(DatabaseColumns.IMAGE_URL, getWallItemResponseModel.wall_item.image_url);
        try {
            values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(getWallItemResponseModel.wall_item.created_at));
            values.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(getWallItemResponseModel.wall_item.created_at));


        } catch (ParseException e) {
            e.printStackTrace();
            //should not happen
        }


        if (getWallItemResponseModel.wall_item.tagged_users.size() > 0) {
            values.put(DatabaseColumns.TAGGED_USER_IDS, getWallItemResponseModel.wall_item.tagged_users.get(0).id);
            values.put(DatabaseColumns.IS_PRESENT, getWallItemResponseModel.
                    wall_item.tagged_users.get(0).is_present + "");
            values.put(DatabaseColumns.TAGGED_NAMES, getWallItemResponseModel.wall_item.tagged_users.get(0).name);
            if (getWallItemResponseModel.
                    wall_item.tagged_users.get(0).details != null) {

                values.put(DatabaseColumns.TAGGED_USER_NUMBERS, getWallItemResponseModel.
                        wall_item.tagged_users.get(0).details.mobile_number);
                values.put(DatabaseColumns.TAGGED_USER_EMAILS, getWallItemResponseModel.
                        wall_item.tagged_users.get(0).details.email);
            }
            values.put(DatabaseColumns.TAGGED_IMAGE_URLS, getWallItemResponseModel.
                    wall_item.tagged_users.get(0).image_url);
            values.put(DatabaseColumns.TAGGED_USER_IDS, getWallItemResponseModel.
                    wall_item.tagged_users.get(0).user_id);
            values.put(DatabaseColumns.TAGGED_IDS, getWallItemResponseModel.
                    wall_item.tagged_users.get(0).id);
        }


        String selection = DatabaseColumns.TEMP_ID + SQLConstants.EQUALS_ARG;


        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, null, values, TableWallComments.NAME, values, selection, new String[]{getWallItemResponseModel.wall_item.tmp_id}, true, this);

        if (extras.getString(AppConstants.Keys.USER_ID).equals(AppConstants.UserInfo.INSTANCE.getId())) {
            DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_MY_WALLCOMMENTS, null, values, TableMyWallComments.NAME, values, selection, new String[]{extras.getString(AppConstants.Keys.TEMP_ID)}, true, this);
        }

        ContentValues valueTagCount = new ContentValues();

        valueTagCount.put(DatabaseColumns.TAG_USER_COUNT, (extras.getInt(AppConstants.Keys.TAG_USER_COUNT) + 1) + "");

        String selectionWallID = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAG_COUNT, null, valueTagCount, TableWallPosts.NAME, valueTagCount, selectionWallID, new String[]{extras.getString(AppConstants.Keys.WALL_ID)}, true, this);

    }


    /**
     * Utility function to save list of service cards into {@link TableServices}
     */
    private void parseStoreServiceCardsList(ServiceCardsResponseModel serviceCardsResponseModel) {
        for (int i = 0; i < serviceCardsResponseModel.ServiceCards.size(); i++) {
            //Logger.v(TAG, serviceCardsResponseModel.ServiceCards.get(i).toString());

            ServiceCard card = serviceCardsResponseModel.ServiceCards.get(i);

            ContentValues values = new ContentValues();
            values.put(DatabaseColumns.ID, card.getId());
            values.put(DatabaseColumns.USER_ID, card.getServiceCardOwner().id);
            values.put(DatabaseColumns.USER_NAME, card.getServiceCardOwner().name);
            values.put(DatabaseColumns.USER_IMAGE, card.getServiceCardOwner().image_url);
            values.put(DatabaseColumns.GROUP_NAME, card.getGroupName());
            values.put(DatabaseColumns.GROUP_ID, card.getGroupId());
            values.put(DatabaseColumns.SERVICE_IMAGE, card.getImageUrl());
            values.put(DatabaseColumns.TITLE, card.getTitle());
            values.put(DatabaseColumns.SERVICE_DESCRIPTION, card.getDescription());
            values.put(DatabaseColumns.SERVICE_PRICE, card.getPrice());
            values.put(DatabaseColumns.USER_NUMBER, card.getServiceCardOwner().mobileNumber);

            /**
             * TODO: Put rest of the columns into table
             */

            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
            DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_SERVICE_LIST, null, values, TableServices.NAME, values, selection, new String[]{card.getId()}, true, this);
        }
    }


    /**
     * Utility function to save list of service cards into {@link TableServices}
     */
    private void parseStoreSearchServiceCardsList(GetServiceCardResponseModel getServiceCardResponseModel) {


        for (GetCreateServiceCardResponse.ServiceCard serviceCard : getServiceCardResponseModel.search) {
            Logger.v(TAG, serviceCard.toString());


            ContentValues values = new ContentValues();
            values.put(DatabaseColumns.ID, serviceCard.id);
            values.put(DatabaseColumns.USER_ID, serviceCard.owner.id);
            values.put(DatabaseColumns.USER_NAME, serviceCard.owner.name);
            values.put(DatabaseColumns.USER_IMAGE, serviceCard.owner.imageUrl);
            values.put(DatabaseColumns.GROUP_NAME, serviceCard.groupName);
            values.put(DatabaseColumns.USER_NUMBER, serviceCard.owner.mobileNumber);
            values.put(DatabaseColumns.GROUP_ID, serviceCard.groupId);
            values.put(DatabaseColumns.VIEW_COUNT, serviceCard.views);
            values.put(DatabaseColumns.BOOK_COUNT, serviceCard.books);
            values.put(DatabaseColumns.SUBGROUP_ID, serviceCard.subgroupId);
            values.put(DatabaseColumns.SUBGROUP_NAME, serviceCard.subgroupName);
            values.put(DatabaseColumns.RATING, serviceCard.avgRating);
            values.put(DatabaseColumns.RATING_COUNT, serviceCard.ratings.size()+"");
            values.put(DatabaseColumns.DURATION, serviceCard.durationTime);
            values.put(DatabaseColumns.DELIVERABLE, serviceCard.note);
            values.put(DatabaseColumns.COLOR, serviceCard.color);
            values.put(DatabaseColumns.SERVICE_IMAGE, serviceCard.imageUrl);
            values.put(DatabaseColumns.TITLE, serviceCard.title);
            values.put(DatabaseColumns.SERVICE_DESCRIPTION, serviceCard.description);
            values.put(DatabaseColumns.SERVICE_PRICE, serviceCard.price);
            /**
             * TODO: Put rest of the columns into table
             */

            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
            DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_SERVICE_LIST, null, values, TableServices.NAME, values, selection, new String[]{serviceCard.id}, true, this);
        }
    }

    /**
     * Utility function to save list of service cards into {@link TableServices}
     */
    private void parseStoreUserServiceCardsList(GetServiceCardResponseModel getServiceCardResponseModel) {

        if(getServiceCardResponseModel.serviceCards == null || getServiceCardResponseModel.serviceCards.size() ==0){
            return;
        }

        for (GetCreateServiceCardResponse.ServiceCard serviceCard : getServiceCardResponseModel.serviceCards) {
            //Logger.v(TAG, serviceCardsResponseModel.ServiceCards.get(i).toString());


            ContentValues values = new ContentValues();
            values.put(DatabaseColumns.ID, serviceCard.id);
            values.put(DatabaseColumns.USER_ID, serviceCard.owner.id);
            values.put(DatabaseColumns.USER_NAME, serviceCard.owner.name);
            values.put(DatabaseColumns.USER_IMAGE, serviceCard.owner.imageUrl);
            values.put(DatabaseColumns.GROUP_NAME, serviceCard.groupName);
            values.put(DatabaseColumns.USER_NUMBER, serviceCard.owner.mobileNumber);
            values.put(DatabaseColumns.GROUP_ID, serviceCard.groupId);
            values.put(DatabaseColumns.RATING, serviceCard.avgRating);
            values.put(DatabaseColumns.VIEW_COUNT, serviceCard.views);
            values.put(DatabaseColumns.BOOK_COUNT, serviceCard.books);
            values.put(DatabaseColumns.RATING_COUNT, serviceCard.ratings.size()+"");
            values.put(DatabaseColumns.SUBGROUP_ID, serviceCard.subgroupId);
            values.put(DatabaseColumns.SUBGROUP_NAME, serviceCard.subgroupName);
            values.put(DatabaseColumns.COLOR, serviceCard.color);
            values.put(DatabaseColumns.DURATION, serviceCard.durationTime);
            values.put(DatabaseColumns.DELIVERABLE, serviceCard.note);
            values.put(DatabaseColumns.SERVICE_IMAGE, serviceCard.imageUrl);
            values.put(DatabaseColumns.TITLE, serviceCard.title);
            values.put(DatabaseColumns.SERVICE_DESCRIPTION, serviceCard.description);
            values.put(DatabaseColumns.SERVICE_PRICE, serviceCard.price);
            /**
             * TODO: Put rest of the columns into table
             */

            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
            DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_SERVICE_LIST, null, values, TableServices.NAME, values, selection, new String[]{serviceCard.id}, true, this);
        }
    }

    private void parseAndStoreSingleComment(CommentResponseModel comment,String
                                             wallId,String wallOwnerUserId,String tempId){
        ContentValues valuesComments = new ContentValues();
        valuesComments.put(DatabaseColumns.WALL_ID, wallId);
        valuesComments.put(DatabaseColumns.ID, comment.comment.id);
        valuesComments.put(DatabaseColumns.WALL_USER_ID, wallOwnerUserId);
        valuesComments.put(DatabaseColumns.COMMENT, comment.comment.message);
        valuesComments.put(DatabaseColumns.USER_ID, comment.comment.userDetails.userId);
        valuesComments.put(DatabaseColumns.DATE_TIME, comment.comment.createdAt);
        valuesComments.put(DatabaseColumns.TAGGED_NAMES, comment.comment.userDetails.name);
        valuesComments.put(DatabaseColumns.TYPE, AppConstants.CommentType.COMMENTS);
        valuesComments.put(DatabaseColumns.TAGGED_IMAGE_URLS, comment.comment.userDetails.image_url);
        valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, comment.comment.userDetails.userId);

        try {
            valuesComments.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(comment.comment.createdAt));
            valuesComments.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(comment.comment.createdAt));


        } catch (ParseException e) {
            e.printStackTrace();
            //should not happen
        }
        valuesComments.put(DatabaseColumns.IS_PRESENT, "true");


        String selectionWallId = DatabaseColumns.TEMP_ID + SQLConstants.EQUALS_ARG;

        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, null,
                valuesComments, TableWallComments.NAME, valuesComments, selectionWallId,
                new String[]{tempId}, false, this);

    }

}