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

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.bus.ServiceCardConfirmationDialog;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.TableMyWallComments;
import red.yelo.data.TableServices;
import red.yelo.data.TableWallComments;
import red.yelo.data.TableWallPosts;
import red.yelo.retromodels.TaggedUser;
import red.yelo.retromodels.request.PostWallCommentRequestModel;
import red.yelo.retromodels.response.GetCreateServiceCardResponse;
import red.yelo.retromodels.response.GetCreateWallResponseModel;
import red.yelo.retromodels.response.GetWallItemResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.Logger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class WallPostIntentService extends IntentService implements DBInterface.AsyncDbQueryCallback {
    private static final String ACTION_POST = "red.yelo.http.action.POST";
    private static final String ACTION_TAG = "red.yelo.http.action.TAG";
    private static final String ACTION_CREATE_SERVICE_CARD = "red.yelo.http.action.CREATE_SERVICE_CARD";
    private static final String ACTION_UPDATE_SERVICE_CARD = "red.yelo.http.action.UPDATE_SERVICE_CARD";



    public static final String TAG = "WallPostIntentService";

    private DateFormatter mMessageDateFormatter;


    public WallPostIntentService() {
        super("WallPostIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_POST.equals(action)) {
                final String message = intent.getStringExtra(HttpConstants.POST_MESSAGE);
                final String latitude = intent.getStringExtra(HttpConstants.POST_LATITUDE);
                final String longitude = intent.getStringExtra(HttpConstants.POST_LONGITUDE);
                final String tagIdSelected = intent.getStringExtra(HttpConstants.POST_TAG_ID);
                final String wallId = intent.getStringExtra(HttpConstants.WALL_ID);
                final String address = intent.getStringExtra(HttpConstants.POST_ADDRESS);
                final String groupId = intent.getStringExtra(HttpConstants.GROUP_ID);
                final String keywords = intent.getStringExtra(HttpConstants.KEYWORDS);


                boolean update = intent.getBooleanExtra(HttpConstants.UPDATE_POST, false);
                String city, country;
                city = intent.getStringExtra(HttpConstants.POST_CITY);
                country = intent.getStringExtra(HttpConstants.POST_COUNTRY);
                final String imagepath = intent.getStringExtra(AppConstants.WALL_IMAGE_NAME);

                TypedFile typedFile;
                File photo;
                photo = new File(imagepath);
                typedFile = new TypedFile("application/octet-stream", photo);

                ///////
                final Map<String, String> params = new HashMap<String, String>(8);
                params.put(HttpConstants.POST_MESSAGE, message);
                params.put(HttpConstants.POST_LATITUDE, latitude);
                params.put(HttpConstants.POST_LONGITUDE, longitude);
                params.put(HttpConstants.POST_TAG_ID, tagIdSelected);
                params.put(HttpConstants.POST_ADDRESS, address);
                params.put(HttpConstants.POST_GROUP_ID, groupId);

                params.put(HttpConstants.POST_CITY, city);
                params.put(HttpConstants.POST_COUNTRY, country);

                handleActionPost(params, typedFile, update, wallId);
            } else if (ACTION_TAG.equals(action)) {

                final Bundle args = intent.getBundleExtra(HttpConstants.ARGS);
                String comment = intent.getStringExtra(HttpConstants.COMMENT);
                String email = intent.getStringExtra(HttpConstants.EMAIL);
                String name = intent.getStringExtra(HttpConstants.NAME);
                String mobileNumber = intent.getStringExtra(HttpConstants.MOBILE_NUMBER);

                PostWallCommentRequestModel postWallCommentRequestModel = new PostWallCommentRequestModel();

                postWallCommentRequestModel.wall_item.setTmp_id(args.getString(AppConstants.Keys.TEMP_ID));
                postWallCommentRequestModel.wall_item.setComment(comment);

                TaggedUser user = new TaggedUser();
                user.setEmail(email);
                user.setName(name);
                user.setMobile_number(mobileNumber);

                List<TaggedUser> tagUsersList = new ArrayList<TaggedUser>(1);

                tagUsersList.add(user);

                postWallCommentRequestModel.setTag_users(tagUsersList);

                ((YeloApplication) getApplication()).getYeloApi().tagUser(args.getString(AppConstants.Keys.WALL_ID),
                        postWallCommentRequestModel, new Callback<GetWallItemResponseModel>() {
                            @Override
                            public void success(GetWallItemResponseModel getWallItemResponseModel, Response response) {
                                ContentValues values = new ContentValues();
                                values.put(DatabaseColumns.WALL_ID, args.getString(AppConstants.Keys.WALL_ID));
                                values.put(DatabaseColumns.ID, getWallItemResponseModel.wall_item.id);
                                values.put(DatabaseColumns.COMMENT, getWallItemResponseModel.wall_item.comment);
                                values.put(DatabaseColumns.STATE, AppConstants.SyncStates.SYNCED);
                                values.put(DatabaseColumns.WALL_USER_ID, args.getString(AppConstants.Keys.USER_ID));
                                values.put(DatabaseColumns.USER_ID, getWallItemResponseModel.wall_item.user_id);
                                values.put(DatabaseColumns.USER_NAME, getWallItemResponseModel.wall_item.name);
                                values.put(DatabaseColumns.IMAGE_URL, getWallItemResponseModel.wall_item.image_url);


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


                                DBInterface
                                        .updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, null,
                                                values, TableWallComments.NAME, values, selection,
                                                new String[]{getWallItemResponseModel.wall_item.tmp_id}, true, WallPostIntentService.this);

                                if (args.getString(AppConstants.Keys.USER_ID).equals(AppConstants.UserInfo.INSTANCE.getId())) {
                                    DBInterface
                                            .updateAsync(AppConstants.QueryTokens.UPDATE_MY_WALLCOMMENTS, null,
                                                    values, TableMyWallComments.NAME, values, selection,
                                                    new String[]{args.getString(AppConstants.Keys.TEMP_ID)}, true, WallPostIntentService.this);
                                }

                                ContentValues valueTagCount = new ContentValues();

                                valueTagCount.put(DatabaseColumns.TAG_USER_COUNT, (args.getInt(AppConstants.Keys.TAG_USER_COUNT) + 1) + "");

                                String selectionWallID = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                                DBInterface
                                        .updateAsync(AppConstants.QueryTokens.UPDATE_TAG_COUNT, null,
                                                valueTagCount, TableWallPosts.NAME, valueTagCount, selectionWallID,
                                                new String[]{args.getString(AppConstants.Keys.WALL_ID)}, true, WallPostIntentService.this);


                            }

                            @Override
                            public void failure(RetrofitError error) {

                                String selectTagUser = DatabaseColumns.TEMP_ID + SQLConstants.EQUALS_ARG;


                                String tempId = args.getString(AppConstants.Keys.TEMP_ID);

                                DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_TAG_USER, null, null, TableWallComments.NAME
                                        , selectTagUser,
                                        new String[]{tempId}, true, WallPostIntentService.this);


                                if(((GetWallItemResponseModel) error.getBody())!=null) {
                                    if (((GetWallItemResponseModel) error.getBody()).error_message != null) {
                                        if (((GetWallItemResponseModel) error.getBody()).error_message.mobile_number.get(0) != null) {
                                            Toast.makeText(getApplicationContext(), ((GetWallItemResponseModel) error.getBody()).error_message.mobile_number.get(0), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Connection error. Please try after some time", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }else {
                                    Toast.makeText(getApplicationContext(), "Connection error. Please try after some time", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }

                );
            }

            else  if (ACTION_CREATE_SERVICE_CARD.equals(action)) {
                final String title = intent.getStringExtra(HttpConstants.SERVICE_TITLE);
                final String description = intent.getStringExtra(HttpConstants.SERVICE_DESCRIPTION);
                final String price = intent.getStringExtra(HttpConstants.SERVICE_PRICE);
                final String listingId = intent.getStringExtra(HttpConstants.LISTING_ID);
                final String serviceCardId = intent.getStringExtra(AppConstants.Keys.ID);
                final String latitude = intent.getStringExtra(HttpConstants.SERVICE_LATITUDE);
                final String longitude = intent.getStringExtra(HttpConstants.SERVICE_LONGITUDE);
                final String duration = intent.getStringExtra(HttpConstants.SERVICE_DURATION);
                final String durationUnit = intent.getStringExtra(HttpConstants.SERVICE_DURATION_UNIT);
                final String note = intent.getStringExtra(HttpConstants.SERVICE_NOTE);
                final String tagId = intent.getStringExtra(HttpConstants.SERVICE_SUB_CATEGORY_ID);



                boolean update = intent.getBooleanExtra(HttpConstants.UPDATE_CARD, false);
                final String imagepath = intent.getStringExtra(HttpConstants.SERVICE_IMAGE_PATH);

                TypedFile typedFile;
                File photo;
                photo = new File(imagepath);
                typedFile = new TypedFile("application/octet-stream", photo);

                ///////
                final Map<String, String> params = new HashMap<String, String>(8);
                params.put(HttpConstants.SERVICE_TITLE, title);
                params.put(HttpConstants.SERVICE_DESCRIPTION, description);
                params.put(HttpConstants.SERVICE_PRICE, price);
                params.put(HttpConstants.LISTING_ID, listingId);
                params.put(HttpConstants.SERVICE_LATITUDE, latitude);
                params.put(HttpConstants.SERVICE_LONGITUDE, longitude);
                params.put(HttpConstants.SERVICE_DURATION, duration);
                params.put(HttpConstants.SERVICE_DURATION_UNIT, durationUnit);
                params.put(HttpConstants.SERVICE_NOTE, note);
                params.put(HttpConstants.SERVICE_SUB_CATEGORY_ID, tagId);



                handleActionServiceCard(params, typedFile, update, serviceCardId);
            }
            else  if (ACTION_UPDATE_SERVICE_CARD.equals(action)) {
                final String title = intent.getStringExtra(HttpConstants.SERVICE_TITLE);
                final String description = intent.getStringExtra(HttpConstants.SERVICE_DESCRIPTION);
                final String price = intent.getStringExtra(HttpConstants.SERVICE_PRICE);
                final String listingId = intent.getStringExtra(HttpConstants.LISTING_ID);
                final String serviceCardId = intent.getStringExtra(AppConstants.Keys.ID);
                final String latitude = intent.getStringExtra(HttpConstants.SERVICE_LATITUDE);
                final String longitude = intent.getStringExtra(HttpConstants.SERVICE_LONGITUDE);
                final String duration = intent.getStringExtra(HttpConstants.SERVICE_DURATION);
                final String durationUnit = intent.getStringExtra(HttpConstants.SERVICE_DURATION_UNIT);
                final String note = intent.getStringExtra(HttpConstants.SERVICE_NOTE);
                final String tagId = intent.getStringExtra(HttpConstants.SERVICE_SUB_CATEGORY_ID);
                final boolean imageUploaded = intent.getBooleanExtra(AppConstants.Keys.IMAGE_UPLOADED,false);



                boolean update = intent.getBooleanExtra(HttpConstants.UPDATE_CARD, false);
                final String imagepath = intent.getStringExtra(HttpConstants.SERVICE_IMAGE_PATH);

                if(imageUploaded) {
                    TypedFile typedFile;
                    File photo;
                    photo = new File(imagepath);
                    typedFile = new TypedFile("application/octet-stream", photo);

                    ///////
                    final Map<String, String> params = new HashMap<String, String>(8);
                    params.put(HttpConstants.SERVICE_TITLE, title);
                    params.put(HttpConstants.SERVICE_DESCRIPTION, description);
                    params.put(HttpConstants.SERVICE_PRICE, price);
                   // params.put(HttpConstants.LISTING_ID, listingId);
                    params.put(HttpConstants.SERVICE_LATITUDE, latitude);
                    params.put(HttpConstants.SERVICE_LONGITUDE, longitude);
                    params.put(HttpConstants.SERVICE_DURATION, duration);
                    params.put(HttpConstants.SERVICE_DURATION_UNIT, durationUnit);
                    params.put(HttpConstants.SERVICE_NOTE, note);
                    params.put(HttpConstants.SERVICE_SUB_CATEGORY_ID, tagId);

                    update = true;

                    handleActionServiceCard(params, typedFile, update, serviceCardId);
                }
                else {
                    ///////
                    final Map<String, String> params = new HashMap<String, String>(8);
                    params.put(HttpConstants.SERVICE_TITLE, title);
                    params.put(HttpConstants.SERVICE_DESCRIPTION, description);
                    params.put(HttpConstants.SERVICE_PRICE, price);
                    //params.put(HttpConstants.LISTING_ID, listingId);
                    params.put(HttpConstants.SERVICE_LATITUDE, latitude);
                    params.put(HttpConstants.SERVICE_LONGITUDE, longitude);
                    params.put(HttpConstants.SERVICE_DURATION, duration);
                    params.put(HttpConstants.SERVICE_DURATION_UNIT, durationUnit);
                    params.put(HttpConstants.SERVICE_NOTE, note);


                    handleActionUpdateServiceCardWithoutImage(params, serviceCardId);
                }


            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */

    private void handleActionPost(Map<String, String> params, TypedFile imagefile, boolean update, String wallId) {

        if (!update) {
            ((YeloApplication) getApplication()).getYeloApi().postWallMessageWithImage(imagefile, params, new Callback<GetCreateWallResponseModel>() {
                @Override
                public void success(GetCreateWallResponseModel getCreateWallResponseModel, Response response) {

                    mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                            AppConstants.MESSAGE_TIME_FORMAT);


                    ContentValues values = new ContentValues(6);
                    values.put(DatabaseColumns.ID, getCreateWallResponseModel.wall.id);
                    values.put(DatabaseColumns.MESSAGE, getCreateWallResponseModel.wall.message);
                    values.put(DatabaseColumns.TAG_NAME, getCreateWallResponseModel.wall.tag_name);
                    values.put(DatabaseColumns.TAG_USER_COUNT, getCreateWallResponseModel.wall.tagged_users_count);
                    values.put(DatabaseColumns.CHAT_USER_COUNT, getCreateWallResponseModel.wall.chat_users_count);
                    values.put(DatabaseColumns.USER_NAME, getCreateWallResponseModel.wall.wall_owner.name);
                    values.put(DatabaseColumns.USER_ID, getCreateWallResponseModel.wall.wall_owner.user_id);
                    values.put(DatabaseColumns.DATE_TIME, getCreateWallResponseModel.wall.created_at);
                    values.put(DatabaseColumns.CITY, getCreateWallResponseModel.wall.city);
                    values.put(DatabaseColumns.GROUP_ID, getCreateWallResponseModel.wall.group_id);
                    values.put(DatabaseColumns.GROUP_NAME, getCreateWallResponseModel.wall.group_name);
                    values.put(DatabaseColumns.COLOR, getCreateWallResponseModel.wall.group_color);
                    values.put(DatabaseColumns.COUNTRY, getCreateWallResponseModel.wall.country);
                    values.put(DatabaseColumns.ADDRESS, getCreateWallResponseModel.wall.address);
                    try {
                        values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(getCreateWallResponseModel.wall.created_at));
                        values.put(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, mMessageDateFormatter.getEpoch(getCreateWallResponseModel.wall.updated_at));
                        values.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(getCreateWallResponseModel.wall.created_at));

                    } catch (ParseException e) {
                        e.printStackTrace();
                        //should not happen
                    }
                    if (getCreateWallResponseModel.wall.wall_image != null) {
                        values.put(DatabaseColumns.WALL_IMAGES, getCreateWallResponseModel.wall.wall_image.image_url);
                    }
                    values.put(DatabaseColumns.USER_IMAGE, getCreateWallResponseModel.wall.wall_owner.image_url);


                    Log.d(TAG, "INSERTED IN WALL INTENT SERVICE");
                    DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, getTaskTag(), null
                            , TableWallPosts.NAME, null, values, true, WallPostIntentService.this);


                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        } else {
            ((YeloApplication) getApplication()).getYeloApi().updateWallMessageWithImage(wallId, imagefile, params, new Callback<GetCreateWallResponseModel>() {
                @Override
                public void success(GetCreateWallResponseModel getCreateWallResponseModel, Response response) {


                    mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                            AppConstants.MESSAGE_TIME_FORMAT);


                    ContentValues values = new ContentValues(6);
                    values.put(DatabaseColumns.ID, getCreateWallResponseModel.wall.id);
                    values.put(DatabaseColumns.MESSAGE, getCreateWallResponseModel.wall.message);
                    values.put(DatabaseColumns.TAG_NAME, getCreateWallResponseModel.wall.tag_name);
                    values.put(DatabaseColumns.TAG_USER_COUNT, getCreateWallResponseModel.wall.tagged_users_count);
                    values.put(DatabaseColumns.CHAT_USER_COUNT, getCreateWallResponseModel.wall.chat_users_count);
                    values.put(DatabaseColumns.USER_NAME, getCreateWallResponseModel.wall.wall_owner.name);
                    values.put(DatabaseColumns.USER_ID, getCreateWallResponseModel.wall.wall_owner.user_id);
                    values.put(DatabaseColumns.DATE_TIME, getCreateWallResponseModel.wall.created_at);
                    values.put(DatabaseColumns.CITY, getCreateWallResponseModel.wall.city);
                    values.put(DatabaseColumns.GROUP_ID, getCreateWallResponseModel.wall.group_id);
                    values.put(DatabaseColumns.GROUP_NAME, getCreateWallResponseModel.wall.group_name);
                    values.put(DatabaseColumns.COLOR, getCreateWallResponseModel.wall.group_color);
                    values.put(DatabaseColumns.COUNTRY, getCreateWallResponseModel.wall.country);
                    values.put(DatabaseColumns.ADDRESS, getCreateWallResponseModel.wall.address);
                    try {
                        values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(getCreateWallResponseModel.wall.created_at));
                        values.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(getCreateWallResponseModel.wall.created_at));
                        values.put(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, mMessageDateFormatter.getEpoch(getCreateWallResponseModel.wall.updated_at));


                    } catch (ParseException e) {
                        e.printStackTrace();
                        //should not happen
                    }
                    if (getCreateWallResponseModel.wall.wall_image != null) {
                        values.put(DatabaseColumns.WALL_IMAGES, getCreateWallResponseModel.wall.wall_image.image_url);
                    }
                    values.put(DatabaseColumns.USER_IMAGE, getCreateWallResponseModel.wall.wall_owner.image_url);

                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    Log.d(TAG, "INSERTED IN WALL INTENT SERVICE");

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, getTaskTag(), values
                            , TableWallPosts.NAME, values, selection,
                            new String[]{getCreateWallResponseModel.wall.id}, true, WallPostIntentService.this);


                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */

    private void handleActionServiceCard(Map<String, String> params, TypedFile imagefile, boolean update,String serviceCardId) {


        if (!update) {
                    ((YeloApplication) getApplication()).getYeloApi().createServiceCardWithImage(imagefile, params, new Callback<GetCreateServiceCardResponse>() {
                @Override
                public void success(GetCreateServiceCardResponse getCreateServiceCardResponse, Response response) {

                    mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                            AppConstants.MESSAGE_TIME_FORMAT);

                    ((YeloApplication) getApplicationContext()).getBus().post(new ServiceCardConfirmationDialog(
                            getResources().getString(R.string.service_confirmation_message)));

                    ContentValues values = new ContentValues(6);

                    values.put(DatabaseColumns.ID, getCreateServiceCardResponse.serviceCard.id);
                    values.put(DatabaseColumns.USER_ID, getCreateServiceCardResponse.serviceCard.owner.id);
                    values.put(DatabaseColumns.USER_NAME, getCreateServiceCardResponse.serviceCard.owner.name);
                    values.put(DatabaseColumns.USER_IMAGE, getCreateServiceCardResponse.serviceCard.owner.imageUrl);
                    values.put(DatabaseColumns.GROUP_NAME, getCreateServiceCardResponse.serviceCard.groupName);
                    values.put(DatabaseColumns.SUBGROUP_NAME, getCreateServiceCardResponse.serviceCard.subgroupName);
                    values.put(DatabaseColumns.TITLE, getCreateServiceCardResponse.serviceCard.title);
                    values.put(DatabaseColumns.COLOR, getCreateServiceCardResponse.serviceCard.color);
                    values.put(DatabaseColumns.DURATION, getCreateServiceCardResponse.serviceCard.durationTime);
                    values.put(DatabaseColumns.DELIVERABLE, getCreateServiceCardResponse.serviceCard.note);
                    values.put(DatabaseColumns.SERVICE_IMAGE, getCreateServiceCardResponse.serviceCard.imageUrl);
                    values.put(DatabaseColumns.SERVICE_DESCRIPTION, getCreateServiceCardResponse.serviceCard.description);
                    values.put(DatabaseColumns.SERVICE_PRICE, getCreateServiceCardResponse.serviceCard.price);
                    values.put(DatabaseColumns.VERIFIED, getCreateServiceCardResponse.serviceCard.owner.docVerified + "");
                    values.put(DatabaseColumns.USER_NUMBER, getCreateServiceCardResponse.serviceCard.owner.mobileNumber);


                    Log.d(TAG, "INSERTED IN WALL INTENT SERVICE");
                    DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_SERVICE_CARD, getTaskTag(), null
                            , TableServices.NAME, null, values, true, WallPostIntentService.this);


                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getApplicationContext(),getResources().getString(R.string.api_error),Toast.LENGTH_SHORT).show();
                }
            });
        } else {


            ((YeloApplication) getApplication()).getYeloApi().updateServiceCardWithImage(serviceCardId, imagefile, params, new Callback<GetCreateServiceCardResponse>() {
                @Override
                public void success(GetCreateServiceCardResponse getCreateServiceCardResponse, Response response) {

                    Toast.makeText(getApplicationContext(),"Service card updated",Toast.LENGTH_SHORT).show();

                    ContentValues values = new ContentValues(6);

                    values.put(DatabaseColumns.ID, getCreateServiceCardResponse.serviceCard.id);
                    values.put(DatabaseColumns.USER_ID, getCreateServiceCardResponse.serviceCard.owner.id);
                    values.put(DatabaseColumns.USER_NAME, getCreateServiceCardResponse.serviceCard.owner.name);
                    values.put(DatabaseColumns.USER_IMAGE, getCreateServiceCardResponse.serviceCard.owner.imageUrl);
                    values.put(DatabaseColumns.GROUP_NAME, getCreateServiceCardResponse.serviceCard.groupName);
                    values.put(DatabaseColumns.DURATION, getCreateServiceCardResponse.serviceCard.durationTime);
                    values.put(DatabaseColumns.DELIVERABLE, getCreateServiceCardResponse.serviceCard.note);
                    values.put(DatabaseColumns.SUBGROUP_NAME, getCreateServiceCardResponse.serviceCard.subgroupName);
                    values.put(DatabaseColumns.RATING, getCreateServiceCardResponse.serviceCard.avgRating);
                    values.put(DatabaseColumns.RATING_COUNT, getCreateServiceCardResponse.serviceCard.ratings.size() + "");
                    values.put(DatabaseColumns.TITLE, getCreateServiceCardResponse.serviceCard.title);
                    values.put(DatabaseColumns.COLOR, getCreateServiceCardResponse.serviceCard.color);
                    values.put(DatabaseColumns.SERVICE_IMAGE, getCreateServiceCardResponse.serviceCard.imageUrl);
                    values.put(DatabaseColumns.SERVICE_DESCRIPTION, getCreateServiceCardResponse.serviceCard.description);
                    values.put(DatabaseColumns.SERVICE_PRICE, getCreateServiceCardResponse.serviceCard.price);
                    values.put(DatabaseColumns.VERIFIED, getCreateServiceCardResponse.serviceCard.owner.docVerified);
                    values.put(DatabaseColumns.USER_NUMBER, getCreateServiceCardResponse.serviceCard.owner.mobileNumber);


                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    Log.d(TAG, "UPDATED IN WALL INTENT SERVICE");

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_SERVICE_CARD, getTaskTag(), values
                            , TableServices.NAME, values, selection,
                            new String[]{getCreateServiceCardResponse.serviceCard.id}, true, WallPostIntentService.this);


                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
        }

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */

    private void handleActionUpdateServiceCardWithoutImage
    (Map<String, String> params,String serviceCardId) {


            ((YeloApplication) getApplication()).getYeloApi().updateServiceCard(serviceCardId, params, new Callback<GetCreateServiceCardResponse>() {
                @Override
                public void success(GetCreateServiceCardResponse getCreateServiceCardResponse, Response response) {


                    ContentValues values = new ContentValues(6);

                    values.put(DatabaseColumns.ID, getCreateServiceCardResponse.serviceCard.id);
                    values.put(DatabaseColumns.USER_ID, getCreateServiceCardResponse.serviceCard.owner.id);
                    values.put(DatabaseColumns.USER_NAME, getCreateServiceCardResponse.serviceCard.owner.name);
                    values.put(DatabaseColumns.USER_IMAGE, getCreateServiceCardResponse.serviceCard.owner.imageUrl);
                    values.put(DatabaseColumns.GROUP_NAME, getCreateServiceCardResponse.serviceCard.groupName);
                    values.put(DatabaseColumns.DURATION, getCreateServiceCardResponse.serviceCard.durationTime);
                    values.put(DatabaseColumns.DELIVERABLE, getCreateServiceCardResponse.serviceCard.note);
                    values.put(DatabaseColumns.SUBGROUP_NAME, getCreateServiceCardResponse.serviceCard.subgroupName);
                    values.put(DatabaseColumns.RATING, getCreateServiceCardResponse.serviceCard.avgRating);
                    values.put(DatabaseColumns.RATING_COUNT, getCreateServiceCardResponse.serviceCard.ratings.size() + "");
                    values.put(DatabaseColumns.TITLE, getCreateServiceCardResponse.serviceCard.title);
                    values.put(DatabaseColumns.COLOR, getCreateServiceCardResponse.serviceCard.color);
                    values.put(DatabaseColumns.SERVICE_IMAGE, getCreateServiceCardResponse.serviceCard.imageUrl);
                    values.put(DatabaseColumns.SERVICE_DESCRIPTION, getCreateServiceCardResponse.serviceCard.description);
                    values.put(DatabaseColumns.SERVICE_PRICE, getCreateServiceCardResponse.serviceCard.price);
                    values.put(DatabaseColumns.VERIFIED, getCreateServiceCardResponse.serviceCard.owner.docVerified);
                    values.put(DatabaseColumns.USER_NUMBER, getCreateServiceCardResponse.serviceCard.owner.mobileNumber);


                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    Log.d(TAG, "UPDATED IN WALL INTENT SERVICE");

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_SERVICE_CARD, getTaskTag(), values
                            , TableServices.NAME, values, selection,
                            new String[]{getCreateServiceCardResponse.serviceCard.id}, true, WallPostIntentService.this);


                }

                @Override
                public void failure(RetrofitError error) {

                }
            });


    }

    public Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

        if (taskId == AppConstants.QueryTokens.UPDATE_WALLPOST) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, getTaskTag(), null
                        , TableWallPosts.NAME, null, values, true, WallPostIntentService.this);


            }
        }
        if (taskId == AppConstants.QueryTokens.UPDATE_WALLCOMMENTS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLCOMMENT, null, null
                        , TableWallComments.NAME, null, values, true, this);
            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_MY_WALLCOMMENTS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLCOMMENT, null, null
                        , TableMyWallComments.NAME, null, values, true, this);
            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_WALLPOST) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;

                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, null, null
                        , TableWallPosts.NAME, null, values, true, this);
            }
        }


        if (taskId == AppConstants.QueryTokens.UPDATE_SERVICE_CARD) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;

                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_SERVICE_CARD, null, null
                        , TableServices.NAME, null, values, true, this);
            }
        }

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }
}
