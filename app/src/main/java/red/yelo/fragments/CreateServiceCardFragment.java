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
 */

package red.yelo.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.melnykov.fab.FloatingActionButton;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import red.yelo.R;
import red.yelo.activities.UserProfileActivity;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableProfileCards;
import red.yelo.data.TableSubCategories;
import red.yelo.data.TableTags;
import red.yelo.data.TableUsers;
import red.yelo.data.ViewGroupColorsWithCards;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.http.WallPostIntentService;
import red.yelo.retromodels.GetUserModel;
import red.yelo.retromodels.response.CreateListingResponseModel;
import red.yelo.retromodels.response.GetCollectionResponseModel;
import red.yelo.retromodels.response.Tags;
import red.yelo.utils.AppConstants;
import red.yelo.utils.InternalFileContentProvider;
import red.yelo.utils.Logger;
import red.yelo.utils.PhotoUtils;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;

/**
 * Created by anshul1235 on 24/10/14.
 */
public class CreateServiceCardFragment extends AbstractYeloFragment implements
        DBInterface.AsyncDbQueryCallback, LoaderManager.LoaderCallbacks<Cursor>
        , RetroCallback.RetroResponseListener, View.OnClickListener {
    public static final String TAG = "CreateCardFragment";
    private static final String ACTION_CREATE_SERVICE_CARD = "red.yelo.http.action.CREATE_SERVICE_CARD";
    private static final String ACTION_UPDATE_SERVICE_CARD = "red.yelo.http.action.UPDATE_SERVICE_CARD";


    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private Toolbar mToolbar;

    private LinearLayout mGocLayout;
    private FrameLayout mCategoryFrameLayout, mGocFrame;

    private TextView mTitleEdit, mDescriptionEdit, mPriceEdit,
            mTitleNonEdit, mDescriptionNonEdit, mPriceNonEdit, mGocName, mCategoryName;


    private ImageView mSpinnerIcon, mServiceImage;

    private String[] mAllProfileCardNames, mProfileCardColors, mAllProfileCardIds;

    private FloatingActionButton mFabButton;

    private boolean mIsUpdate, mOtherUser, mFromLogin, mIsUpdated, mServiceImageUploaded;

    private String mServiceCardId, mUserId, mGocNameSelected, mSelectedTagId, mSelectedCategoryName, mActionColor, mKeyText,
            mGocIdSelected;

    private ProgressDialog mProgressDialog;


    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private static final int PIC_CROP = 4;

    private Uri mCameraImageCaptureUri;
    private File mServiceImageFile;
    private String mServiceImageFileName = AppConstants.SERVICE_IMAGE;
    private Bitmap mCompressedPhoto;

    private FrameLayout mAttachFileFrame;

    private EditText mDuration, mDeliverables;

    private Spinner mTimeSpinner;

    private String mContactNumber, mCardId, mTitle, mTagName, mUserName, mPrice, mUserImageUrl,
            mDurationInfo, mDeliverableInfo, mServiceImageUrl, mDescriptionString, mGroupNameString;

    private boolean mEditService;

    private View mImageBackground;

    private Tags[] mTags;

    private String[] mAllGocNames, mAllGocCategories, mGocColors, mAllSubCategoryId, mAllGocIds;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);

        final View contentView = inflater
                .inflate(R.layout.fragment_create_service_card, container, false);

        mProgressDialog = showProgressDialog();

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        mGocLayout = (LinearLayout) contentView.findViewById(R.id.goc_layout);
        mGocFrame = (FrameLayout) contentView.findViewById(R.id.frame_goc);
        mGocName = (TextView) contentView.findViewById(R.id.goc_name);
        mCategoryFrameLayout = (FrameLayout) contentView.findViewById(R.id.category_frame_layout);
        mCategoryName = (TextView) contentView.findViewById(R.id.goc_category_name);

        mFabButton = (FloatingActionButton) contentView.findViewById(R.id.fabbutton);
        mSpinnerIcon = (ImageView) contentView.findViewById(R.id.spinner_icon);
        mAttachFileFrame = (FrameLayout) contentView.findViewById(R.id.attach_image_frame);
        mImageBackground = contentView.findViewById(R.id.image_background);

        mTitleEdit = (TextView) contentView.findViewById(R.id.title);
        mDescriptionEdit = (TextView) contentView.findViewById(R.id.description);
        mPriceEdit = (TextView) contentView.findViewById(R.id.price);
        mDuration = (EditText) contentView.findViewById(R.id.duration_value_edit);
        mDeliverables = (EditText) contentView.findViewById(R.id.deliverable_values);
        mTimeSpinner = (Spinner) contentView.findViewById(R.id.duration_spinner);

        mServiceImage = (ImageView) contentView.findViewById(R.id.service_image);


        mFabButton.setOnClickListener(this);
        mAttachFileFrame.setOnClickListener(this);

        setToolbar(mToolbar, getResources().getString(R.string.title_activity_create_card), true);


        mGocLayout.setOnClickListener(this);
        mCategoryFrameLayout.setOnClickListener(this);


        Bundle extras = getArguments();

        if (extras != null) {

            if (extras.containsKey(AppConstants.Keys.EDIT_SERVICE)) {
                mServiceImageUrl = extras.getString(AppConstants.Keys.SERVICE_IMAGE);
                mUserImageUrl = extras.getString(AppConstants.Keys.USER_IMAGE);

                mServiceCardId = extras.getString(AppConstants.Keys.SERVICE_ID);
                mTitle = extras.getString(AppConstants.Keys.TITLE);
                mDescriptionString = extras.getString(AppConstants.Keys.DESCRIPTION);
                mPrice = extras.getString(AppConstants.Keys.PRICE);
                mGroupNameString = extras.getString(AppConstants.Keys.GROUP_NAME);
                mTagName = extras.getString(AppConstants.Keys.SUBCATEGORY_NAME);
                mUserId = extras.getString(AppConstants.Keys.USER_ID);
                mUserName = extras.getString(AppConstants.Keys.USER_NAME);
                mContactNumber = extras.getString(AppConstants.Keys.CONTACT_NUMBER);
                mDeliverableInfo = extras.getString(AppConstants.Keys.DELIVERABLE);
                mDurationInfo = extras.getString(AppConstants.Keys.DURATION);
                mEditService = extras.getBoolean(AppConstants.Keys.EDIT_SERVICE);
                mSelectedCategoryName = mGroupNameString;
                mGocIdSelected = extras.getString(AppConstants.Keys.GROUP_ID);
                mSelectedTagId = extras.getString(AppConstants.Keys.SUBCATEGORY_ID);

                mServiceImage.setVisibility(View.VISIBLE);
                Glide.with(getActivity())
                        .load(mServiceImageUrl)
                        .asBitmap()
                        .centerCrop()
                        .animate(R.anim.fade_in)
                        .placeholder(R.color.snow_light)
                        .into(mServiceImage);

                //set values
                mTitleEdit.setText(mTitle);
                mDescriptionEdit.setText(mDescriptionString);
                mPriceEdit.setText(mPrice);
                if(mDurationInfo.contains("hour")){
                    mTimeSpinner.setSelection(AppConstants.DurationUnit.HOUR);
                }
                else if(mDurationInfo.contains("day")){
                    mTimeSpinner.setSelection(AppConstants.DurationUnit.DAY);

                }
                else if(mDurationInfo.contains("week")){
                    mTimeSpinner.setSelection(AppConstants.DurationUnit.WEEK);

                }
                else if(mDurationInfo.contains("month")){
                    mTimeSpinner.setSelection(AppConstants.DurationUnit.MONTH);

                }


                mDurationInfo = mDurationInfo.replaceAll("\\D+","");
                mDuration.setText(mDurationInfo);
                mDeliverables.setText(mDeliverableInfo);
                mGocName.setText(mGroupNameString);
                mCategoryName.setText(mTagName);


            } else {
                mUserId = extras.getString(AppConstants.Keys.USER_ID);

                if (extras.containsKey(AppConstants.Keys.FROM_LOGIN)) {
                    mFromLogin = extras.getBoolean(AppConstants.Keys.FROM_LOGIN);
                }

                if (extras.containsKey(AppConstants.Keys.ID)) {
                    mGocName.setText(extras.getString(AppConstants.Keys.GROUP_NAME));
                    mServiceCardId = extras.getString(AppConstants.Keys.ID);
                    if (extras.getString(AppConstants.Keys.USER_ID).equals(AppConstants.UserInfo.INSTANCE.getId())) {
                        mIsUpdate = true;
                    } else {
                        mOtherUser = true;
                    }

                }
            }
        }



        setFileName();
        mCameraImageCaptureUri = Uri.fromFile(mServiceImageFile);


        if (savedInstanceState != null) {

            mSelectedTagId = savedInstanceState.getString(AppConstants.Keys.CATEGORY_ID);
            //TODO not sure about address
            //mAddress = savedInstanceState.getStringArray(AppConstants.Keys.ADDRESS);

            mGocName.setText(savedInstanceState.getString(AppConstants.Keys.GROUP_NAME));
            mTitleEdit.setText(savedInstanceState.getString(AppConstants.Keys.TITLE));
            mPriceEdit.setText(savedInstanceState.getString(AppConstants.Keys.PRICE));
            mDescriptionEdit.setText(savedInstanceState.getString(AppConstants.Keys.DESCRIPTION));


            mCategoryName.setText(savedInstanceState.getString(AppConstants.Keys.CATEGORY_NAME));

            mGocIdSelected = savedInstanceState.getString(AppConstants.Keys.GROUP_ID);
            loadSubCategories(mGocIdSelected);


            mActionColor = savedInstanceState.getString(AppConstants.Keys.COLOR);

            if (!TextUtils.isEmpty(mActionColor)) {
                colorizeActionBar(Color.parseColor(mActionColor), mToolbar);
                colorizeView(Color.parseColor(mActionColor), mGocFrame);
            }


        }

        fetchGocs();
        loadGroupGocs();
        return contentView;

    }

    private void fetchGocs() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_GROUPS);
        retroCallbackList.add(retroCallback);
        if (!mOtherUser)
            mProgressDialog.show();
        mYeloApi.getGocs(retroCallback);
    }

    private void setFileName() {

        mServiceImageFile = new File(Utils.getStorageDirectory(getActivity()), mServiceImageFileName);
    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static CreateOpenCardFragment newInstance() {
        CreateOpenCardFragment f = new CreateOpenCardFragment();
        return f;
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

        if (taskId == AppConstants.QueryTokens.INSERT_PROFILE_CARDS) {
            DBInterface.notifyChange(ViewGroupColorsWithCards.NAME);

        }
    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {


    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

        if (taskId == AppConstants.QueryTokens.UPDATE_PROFILE_CARDS) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_PROFILE_CARDS, getTaskTag(), null, TableProfileCards.NAME, null, values, true, this);
            } else {
                DBInterface.notifyChange(ViewGroupColorsWithCards.NAME);
            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface
                        .insertAsync(AppConstants.QueryTokens.INSERT_TAG_SUGGESSTIONS,
                                getTaskTag(), null, TableTags.NAME, null, values, true, this);

            }
        }

    }

    private void loadProfileCards() {

        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_PROFILE_CARD_IN_SERVICES, null, this);

    }

    private void loadGroupGocs() {

        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS_IN_SERVICES, null, this);

    }


    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_PROFILE_CARD_IN_SERVICES) {

            String selection = DatabaseColumns.USER_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableProfileCards.NAME, null,
                    selection, new String[]{mUserId}, null, null, null, null);

        } else if (loaderId == AppConstants.Loaders.LOAD_CATEGORIES_IN_SERVICES) {

            String categoryId = bundle.getString(AppConstants.Keys.TAG_ID);

            String selection = DatabaseColumns.CATEGORY_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableSubCategories.NAME, null,
                    selection, new String[]{categoryId}, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);

        } else if (loaderId == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS_IN_SERVICES) {

            return new SQLiteLoader(getActivity(), false, TableTags.NAME, null,
                    null, null, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {


//        if (loader.getId() == AppConstants.Loaders.LOAD_PROFILE_CARD_IN_SERVICES) {
//
//            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
//
//            cursor.moveToFirst();
//            mAllProfileCardIds = new String[cursor.getCount()];
//            mAllProfileCardNames = new String[cursor.getCount() + 1];
//            mProfileCardColors = new String[cursor.getCount()];
//
//
//            for (int i = 0; i < cursor.getCount(); i++) {
//
//
//                String cardName = cursor.getString(
//                        cursor.getColumnIndex(DatabaseColumns.GROUP_NAME));
//                String cardId = cursor.getString(
//                        cursor.getColumnIndex(DatabaseColumns.ID));
//
//                String color = cursor.getString(
//                        cursor.getColumnIndex(DatabaseColumns.COLOR));
//
//                if (!TextUtils.isEmpty(mGroupNameString)) {
//                    if (mGroupNameString.equals(cardName)) {
//                        mProfileCardIdSelected = cardId;
//                        mProfileCardNameSelected = cardName;
//
//                        colorizeActionBar(Color.parseColor(color), mToolbar);
//                        colorizeView(Color.parseColor(color), mProfileCardFrame);
//
//                    }
//                }
//
//
//                mAllProfileCardIds[i] = cardId;
//                mAllProfileCardNames[i] = cardName;
//                mProfileCardColors[i] = color;
//
//                if (i == cursor.getCount() - 1) {
//                    mProgressDialog.dismiss();
//                }
//                cursor.moveToNext();
//
//            }
//
//            mAllProfileCardNames[mAllProfileCardIds.length] = "Create a new profile card";
//        }


        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS_IN_SERVICES) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());

            mTags = new Tags[cursor.getCount()];
            cursor.moveToFirst();
            mAllGocNames = new String[cursor.getCount()];
            mGocColors = new String[cursor.getCount()];
            mAllGocIds = new String[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {

                mTags[i] = new Tags(cursor.getString(
                        cursor.getColumnIndex(DatabaseColumns.NAME)),
                        cursor.getString(
                                cursor.getColumnIndex(DatabaseColumns.ID)), "image_url");
                mAllGocNames[i] = mTags[i].getName().toUpperCase();
                mGocColors[i] = cursor.getString(
                        cursor.getColumnIndex(DatabaseColumns.COLOR));
                mAllGocIds[i] = cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID));

                if (i == cursor.getCount() - 1) {
                    mProgressDialog.dismiss();
                }
                cursor.moveToNext();


            }

        }

        if (loader.getId() == AppConstants.Loaders.LOAD_CATEGORIES_IN_SERVICES) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());

            cursor.moveToFirst();
            mAllGocCategories = new String[cursor.getCount()];
            mAllSubCategoryId = new String[cursor.getCount()];


            for (int i = 0; i < cursor.getCount(); i++) {


                String subcategory = cursor.getString(
                        cursor.getColumnIndex(DatabaseColumns.NAME));

                mAllGocCategories[i] = subcategory;
                mAllSubCategoryId[i] = cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID));

                if (i == cursor.getCount() - 1) {
                    mProgressDialog.dismiss();
                }
                cursor.moveToNext();

            }

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_USER_DETAILS: {
                GetUserModel getUserModel = ((GetUserModel) model);
                String tagNamesValue = "";
                String tagIdsValue = "";
                userRefresh(false);
                if (getUserModel.user.listings.size() > 0) {
                    if (getUserModel.user.listings.size() > 0) {

                        for (int i = 0; i < getUserModel.user.listings.size(); i++) {

                            tagNamesValue = tagNamesValue + "," + getUserModel.user.listings.get(i).tag_name;
                            tagIdsValue = tagNamesValue + "," + getUserModel.user.listings.get(i).tag_id;
                        }
                    }
                }

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


                ContentValues values = new ContentValues();
                values.put(DatabaseColumns.ID, getUserModel.user.id);
                values.put(DatabaseColumns.USER_IMAGE, getUserModel.user.image_url);
                values.put(DatabaseColumns.USER_NAME, getUserModel.user.name);
                values.put(DatabaseColumns.USER_DESCRIPTION, getUserModel.user.description);
                values.put(DatabaseColumns.TAG_COUNT, getUserModel.user.total_tagged);
                values.put(DatabaseColumns.REVIEW_COUNT, getUserModel.user.total_ratings);
                values.put(DatabaseColumns.AVERAGE_RATING, getUserModel.user.rating_avg);
                values.put(DatabaseColumns.REVIEW_COUNT, getUserModel.user.total_ratings);
                values.put(DatabaseColumns.USER_TAGS, tagNamesValue);
                values.put(DatabaseColumns.USER_TAGS_IDS, tagIdsValue);
                values.put(DatabaseColumns.CONNECT_COUNT, getUserModel.user.connects_count);

                String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_USERS, getTaskTag(), values, TableUsers.NAME, values, selection, new String[]{getUserModel.user.id}, true, this);

                String selectionProfileCards = DatabaseColumns.USER_ID + SQLConstants.EQUALS_ARG;
                int i = 0;

                //add values to the profile cards
                for (CreateListingResponseModel.Listing listing : getUserModel.user.listings) {

                    ContentValues valuesProfileCard = new ContentValues();
                    valuesProfileCard.put(DatabaseColumns.ID, listing.id);
                    valuesProfileCard.put(DatabaseColumns.USER_ID, mUserId);
                    valuesProfileCard.put(DatabaseColumns.REFERRAL_COUNT, listing.referral_count);

                    List<String> keywords = new ArrayList<String>();

                    if (listing.listing_keywords != null) {
                        for (CreateListingResponseModel.Listing.ListingKeywords keywordsList : listing.listing_keywords) {
                            keywords.add(" " + keywordsList.name.substring(0, 1).toUpperCase() + keywordsList.name.substring(1));
                        }
                    }

                    valuesProfileCard.put(DatabaseColumns.SUB_HEADING, TextUtils.join(",", keywords));

                    if (listing.listing_links != null) {
                        if (listing.listing_links.size() != 0) {
                            valuesProfileCard.put(DatabaseColumns.URL, listing.listing_links.get(0).url);
                        }
                    }
                    valuesProfileCard.put(DatabaseColumns.GROUP_ID, listing.group_id);
                    valuesProfileCard.put(DatabaseColumns.GROUP_NAME, listing.group_name);
                    valuesProfileCard.put(DatabaseColumns.SUBGROUP_ID, listing.tag_id);
                    valuesProfileCard.put(DatabaseColumns.SUBGROUP_NAME, listing.tag_name);
                    valuesProfileCard.put(DatabaseColumns.COLOR, listing.group_color);

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_PROFILE_CARDS, getTaskTag(), valuesProfileCard, TableProfileCards.NAME, valuesProfileCard, selection, new String[]{listing.id}, true, this);


                }

                break;
            }

            case HttpConstants.ApiResponseCodes.GET_GROUPS: {

                GetCollectionResponseModel getCollectionResponseModel = ((GetCollectionResponseModel) model);

                for (GetCollectionResponseModel.Collection eachEntry : getCollectionResponseModel.groups) {

                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.ID, eachEntry.id);
                    values.put(DatabaseColumns.NAME, eachEntry.name);
                    values.put(DatabaseColumns.COLOR, eachEntry.color);

                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
                            TableTags.NAME, values, selection, new String[]{eachEntry.id}, true, this);

                }

                break;


            }

            default:
                break;
        }
    }

    @Override
    public void failure(int requestId, int errorCode, String message) {

        mProgressDialog.dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
    }


//    /**
//     * Method to handle click on goc
//     */
//    private void showChooseProfileCardDialog() {
//
//            new MaterialDialog.Builder(getActivity())
//                    .items(mAllProfileCardNames)
//                    .itemsCallback(new MaterialDialog.ListCallback() {
//                                       @Override
//                                       public void onSelection(MaterialDialog dialog, View view,
//                                                               int which, CharSequence text) {
//
//
//                                           if (which == mAllProfileCardNames.length-1) {
//
//                                               final Intent createCard = new Intent(getActivity(),
//                                                       CreateCardActivity.class);
//
//                                               startActivity(createCard);
//                                           } else {
//                                               mActionColor = mProfileCardColors[which];
//                                               mProfileCardName.setText(mAllProfileCardNames[which]);
//                                               mProfileCardNameSelected = mAllProfileCardNames[which];
//                                               mProfileCardIdSelected = mAllProfileCardIds[which];
//                                               colorizeActionBar(Color.parseColor(mProfileCardColors[which]), mToolbar);
//                                               colorizeView(Color.parseColor(mProfileCardColors[which]), mProfileCardFrame);
//                                           }
//                                       }
//
//                                   }
//
//                    ).show();
//
//
//    }

    /**
     * Method to handle click on goc
     */
    private void showChooseGocDialog() {
        final int[] materialColors = getActivity().getResources().getIntArray(R.array.collectionListColors);


        new MaterialDialog.Builder(getActivity())
                .items(mAllGocNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                                   @Override
                                   public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {


                                       mActionColor = mGocColors[which];
                                       mGocName.setText(mAllGocNames[which]);
                                       mGocNameSelected = mAllGocNames[which];
                                       mCategoryName.setText("Subcategory");

                                       mGocIdSelected = mAllGocIds[which];
                                       colorizeActionBar(Color.parseColor(mGocColors[which]), mToolbar);
                                       colorizeView(Color.parseColor(mGocColors[which]), mGocFrame);

                                       loadSubCategories(mTags[which].getId());
                                   }

                               }

                ).show();

    }

    private void loadSubCategories(String groupId) {

        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.TAG_ID, groupId);
        fetchCategories(groupId);
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_CATEGORIES_IN_SERVICES, args, this);

    }

    private void fetchCategories(String groupId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SUB_CATEGORIES);

        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.TAG_ID, groupId);
        retroCallback.setExtras(args);
        retroCallbackList.add(retroCallback);


        if (!mOtherUser)
            mProgressDialog.show();
        mYeloApi.getSubCategories(groupId, retroCallback);
    }


    /**
     * Method to handle click on category
     */
    private void showChooseCategoryDialog() {

        new MaterialDialog.Builder(getActivity())
                .items(mAllGocCategories)
                .itemsCallback(new MaterialDialog.ListCallback() {
                                   @Override
                                   public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                       mSelectedCategoryName = mAllGocCategories[which];
                                       mCategoryName.setText(mSelectedCategoryName);
                                       mSelectedTagId = mAllSubCategoryId[which];
                                   }

                               }

                ).show();

    }


    /**
     * This loads the profile of the user
     *
     * @param userId user id of the user u want to open profile of
     * @param name   name of the user
     */
    private void loadProfile(String userId, String name) {
        final Intent userProfileIntent = new Intent(getActivity(), UserProfileActivity.class);

        userProfileIntent.putExtra(AppConstants.Keys.USER_ID, userId);
        userProfileIntent.putExtra(AppConstants.Keys.USER_NAME, name);
        userProfileIntent.putExtra(AppConstants.Keys.SERVICE_SCREEN_TYPE, AppConstants.ServiceScreenType.PROFILE);
        startActivity(userProfileIntent);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.goc_layout) {
            if (!mOtherUser) {
                showChooseGocDialog();
            }
        }else if (v.getId() == R.id.category_frame_layout) {
            if (TextUtils.isEmpty(mGocIdSelected)) {
                Toast.makeText(getActivity(), getResources().getString(R.string.select_category_first), Toast.LENGTH_SHORT).show();
            } else {
                showChooseCategoryDialog();
            }
        }

        else if (v.getId() == R.id.fabbutton) {


            if (mEditService) {
                if (TextUtils.isEmpty(mTitleEdit.getText().toString().trim()) || TextUtils.isEmpty(mDescriptionEdit.
                        getText().toString().trim()) || TextUtils.isEmpty(mPriceEdit.getText().toString().trim()) ||
                        TextUtils.isEmpty(mDuration.getText().toString().trim()) || TextUtils.isEmpty(
                        mDeliverables.getText().toString().trim())) {

                    Toast.makeText(getActivity(), "Please enter full details", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(mGocIdSelected)) {
                    Toast.makeText(getActivity(), "Please select category", Toast.LENGTH_SHORT).show();

                }else if (TextUtils.isEmpty(mSelectedTagId)) {
                    Toast.makeText(getActivity(), "Please select sub category", Toast.LENGTH_SHORT).show();

                }

                else {
                    updateCard(mTitleEdit.getText().toString(), mDescriptionEdit.getText().toString(),
                            mPriceEdit.getText().toString(), mGocIdSelected,mSelectedTagId, mServiceImageFile.getAbsolutePath(),
                            mDuration.getText().toString(), mTimeSpinner.getSelectedItemPosition(), mDeliverables.getText().toString(), mServiceImageUploaded);
                }
            } else {
                createCard();
            }
        } else if (v.getId() == R.id.attach_image_frame) {
            showChoosePictureSourceDialog();

        }
    }

    private void createCard() {

        if (TextUtils.isEmpty(mTitleEdit.getText().toString().trim()) || TextUtils.isEmpty(mDescriptionEdit.
                getText().toString().trim()) || TextUtils.isEmpty(mPriceEdit.getText().toString().trim()) ||
                TextUtils.isEmpty(mDuration.getText().toString().trim()) || TextUtils.isEmpty(
                mDeliverables.getText().toString().trim())) {

            Toast.makeText(getActivity(), "Please enter full details", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mGocIdSelected)) {
            Toast.makeText(getActivity(), "Please select category", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(mSelectedTagId)) {
            Toast.makeText(getActivity(), "Please select sub category", Toast.LENGTH_SHORT).show();

        }  else if (!mServiceImageUploaded) {
            Toast.makeText(getActivity(), "You need to attach an image with this card", Toast.LENGTH_LONG).show();

        } else {
            createServiceCard(mTitleEdit.getText().toString(), mDescriptionEdit.getText().toString(),
                    mPriceEdit.getText().toString(), mGocIdSelected,mSelectedTagId, mServiceImageFile.getAbsolutePath(),
                    mDuration.getText().toString(), mTimeSpinner.getSelectedItemPosition(), mDeliverables.getText().toString());
        }


    }

    private void createServiceCard(String title, String description, String price, String categoryId,
                                   String subCategoryId
            , String imagePath, String duration, int selectedTimer, String deliverables) {

        Intent intent = new Intent(getActivity(), WallPostIntentService.class);

        intent.setAction(ACTION_CREATE_SERVICE_CARD);
        intent.putExtra(HttpConstants.SERVICE_TITLE, title);
        intent.putExtra(HttpConstants.SERVICE_CATEGORY_ID, categoryId);
        intent.putExtra(HttpConstants.SERVICE_SUB_CATEGORY_ID, subCategoryId);
        intent.putExtra(HttpConstants.SERVICE_DESCRIPTION, description);
        intent.putExtra(HttpConstants.SERVICE_PRICE, price);
        intent.putExtra(HttpConstants.SERVICE_IMAGE_PATH, imagePath);
        intent.putExtra(HttpConstants.SERVICE_LATITUDE, SharedPreferenceHelper.getString(R.string.pref_latitude));
        intent.putExtra(HttpConstants.SERVICE_LONGITUDE, SharedPreferenceHelper.getString(R.string.pref_longitude));
        intent.putExtra(HttpConstants.SERVICE_DURATION, duration);
        intent.putExtra(HttpConstants.SERVICE_NOTE, deliverables);

        String durationUnit = "hour";
        if (selectedTimer == AppConstants.DurationUnit.HOUR) {
            durationUnit = "hour";
        }
        else if (selectedTimer == AppConstants.DurationUnit.DAY) {
            durationUnit = "day";
        }
        else if (selectedTimer == AppConstants.DurationUnit.WEEK) {
            durationUnit = "week";
        } else if (selectedTimer == AppConstants.DurationUnit.MONTH) {
            durationUnit = "month";
        }
        intent.putExtra(HttpConstants.SERVICE_DURATION_UNIT, durationUnit);


        getActivity().startService(intent);

        getActivity().finish();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {


        outState.putString(AppConstants.Keys.GROUP_NAME, mGocName.getText().toString());
        outState.putString(AppConstants.Keys.CATEGORY_ID, mSelectedTagId);
        //TODO not sure about address
        // outState.putStringArray(AppConstants.Keys.ADDRESS, mAddress);
        outState.putString(AppConstants.Keys.COLOR, mActionColor);
        outState.putString(AppConstants.Keys.GROUP_ID, mGocIdSelected);
        outState.putString(AppConstants.Keys.TITLE, mTitleEdit.getText().toString().trim());
        outState.putString(AppConstants.Keys.COLOR, mActionColor);
        outState.putString(AppConstants.Keys.DESCRIPTION, mDescriptionEdit.getText().toString().trim());
        outState.putString(AppConstants.Keys.PRICE, mPriceEdit.getText().toString().trim());


        super.onSaveInstanceState(outState);

    }

    private void updateCard(String title, String description, String price, String categoryId,String subCategoryId
            , String imagePath, String duration, int selectedTimer, String deliverables, boolean imageUploaded) {
        Intent intent = new Intent(getActivity(), WallPostIntentService.class);

        intent.setAction(ACTION_UPDATE_SERVICE_CARD);
        intent.putExtra(HttpConstants.SERVICE_TITLE, title);
        intent.putExtra(HttpConstants.SERVICE_CATEGORY_ID, categoryId);
        intent.putExtra(HttpConstants.SERVICE_SUB_CATEGORY_ID, subCategoryId);
        intent.putExtra(HttpConstants.SERVICE_DESCRIPTION, description);
        intent.putExtra(HttpConstants.SERVICE_PRICE, price);
        intent.putExtra(HttpConstants.SERVICE_IMAGE_PATH, imagePath);
        intent.putExtra(HttpConstants.SERVICE_LATITUDE, SharedPreferenceHelper.getString(R.string.pref_latitude));
        intent.putExtra(HttpConstants.SERVICE_LONGITUDE, SharedPreferenceHelper.getString(R.string.pref_longitude));
        intent.putExtra(HttpConstants.SERVICE_DURATION, duration);
        intent.putExtra(AppConstants.Keys.ID, mServiceCardId);
        intent.putExtra(HttpConstants.SERVICE_NOTE, deliverables);
        intent.putExtra(AppConstants.Keys.EDIT_SERVICE, mEditService);
        intent.putExtra(AppConstants.Keys.IMAGE_UPLOADED, imageUploaded);


        String durationUnit = "day";
        if (selectedTimer == AppConstants.DurationUnit.HOUR) {
            durationUnit = "hour";
        }
        else if (selectedTimer == AppConstants.DurationUnit.DAY) {
            durationUnit = "day";
        } else if (selectedTimer == AppConstants.DurationUnit.WEEK) {
            durationUnit = "week";
        } else if (selectedTimer == AppConstants.DurationUnit.MONTH) {
            durationUnit = "month";
        }
        intent.putExtra(HttpConstants.SERVICE_DURATION_UNIT, durationUnit);


        getActivity().startService(intent);
        getActivity().setResult(Activity.RESULT_OK, null);

        getActivity().finish();
    }


    private void updateView(Cursor cursor) {


    }


    /**
     * Method to handle click on profile image
     */
    private void showChoosePictureSourceDialog() {

        new MaterialDialog.Builder(getActivity())
                .items(getResources().getStringArray(R.array.take_photo_choices_wall))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0) { // Pick from camera
                            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, InternalFileContentProvider.SERVICE_PIC_URI);

                            try {
                                startActivityForResult(
                                        Intent.createChooser(intent, getString(R.string.complete_action_using)),
                                        PICK_FROM_CAMERA);
                            } catch (final ActivityNotFoundException e) {
                                e.printStackTrace();
                            }

                        } else if (which == 1) { // pick from file
                            Intent intent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(
                                    Intent.createChooser(intent, getString(R.string.complete_action_using)),
                                    PICK_FROM_FILE);
                        }

                    }
                })
                .show();

    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {
                case PICK_FROM_CAMERA:
                    // doCrop(PICK_FROM_CAMERA);
                    if (resultCode == Activity.RESULT_OK)
                        new Crop(mCameraImageCaptureUri).output(mCameraImageCaptureUri).withAspect(600, 250).start(getActivity());

                    // performCrop(mCameraImageCaptureUri);
                    break;

                case Crop.REQUEST_CROP:

                    setAndSaveImage(mCameraImageCaptureUri, PICK_FROM_CAMERA);
                    break;

                case PIC_CROP: {
                    if (data != null) {
                        // get the returned data
                        Bundle extras = data.getExtras();
                        // get the cropped bitmap
                        setAndSaveImage(mCameraImageCaptureUri, PICK_FROM_CAMERA);
                        //mProfilePic.setImageBitmap(selectedBitmap);
                    }
                }
                break;


                case PICK_FROM_FILE:
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        final String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
                        Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                        // some devices (OS versions return an URI of com.android instead of com.google.android
                        if (selectedImage.toString().startsWith("content://com.android.gallery3d.provider")) {
                            // use the com.google provider, not the com.android provider.
                            selectedImage = Uri.parse(selectedImage.toString().replace("com.android.gallery3d", "com.google.android.gallery3d"));
                        }
                        if (cursor != null) {
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                            // if it is a picasa image on newer devices with OS 3.0 and up
                            //Logger.d(TAG,selectedImage.toString()+"SOMETHING");
                            if (selectedImage.toString().startsWith("content://com.google.android.apps") ||
                                    selectedImage.toString().startsWith("content://com.google.android.gallery3d")) {
//                            Logger.d(TAG,selectedImage.toString()+"SOMETHING");
                                columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                                if (columnIndex != -1) {
                                    final Uri uriurl = selectedImage;
//                                Logger.d(TAG,selectedImage.toString());
                                    // Do this in a background thread, since we are fetching a large image from the web


                                    new Thread(new Runnable() {
                                        public void run() {

                                            mCompressedPhoto = getBitmap(mServiceImageFileName, uriurl);
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mServiceImageUploaded = true;
                                                    mCompressedPhoto = PhotoUtils.compressManageAspect(400, 400, mCompressedPhoto);
                                                    mServiceImage.setImageBitmap(mCompressedPhoto);
                                                    mServiceImage.setVisibility(View.VISIBLE);
                                                    PhotoUtils.saveImage(PhotoUtils.compressManageAspect(400, 400, mCompressedPhoto), mServiceImageFile);
                                                }
                                            });
                                        }
                                    }).start();


                                }
                            } else { // it is a regular local image file
                                Uri mGalleryImageCaptureUri = data.getData();
                                //setAndSaveImage(mGalleryImageCaptureUri, PICK_FROM_FILE);
                                //performCrop(mGalleryImageCaptureUri);
                                new Crop(mGalleryImageCaptureUri).output(mCameraImageCaptureUri).withAspect(600, 250).start(getActivity());
                                // doCrop(PICK_FROM_FILE);


                            }
                        }

                        break;
                    }
            }
        }
    }

    /**
     * Set the Profile Image and Save it locally
     *
     * @param uri             URI of the image to be saved.
     * @param source_of_image If the image was from Gallery or Camera
     */

    private void setAndSaveImage(final Uri uri, final int source_of_image) {
        String source_string;
        if (source_of_image == PICK_FROM_FILE) {
            source_string = "Gallery";
        } else {
            source_string = "Camera";
        }

        Uri mPhotoUri = uri;

        mCompressedPhoto = PhotoUtils
                .rotateBitmapIfNeededAndCompressIfTold(getActivity(), uri, source_string, false);


        if (mCompressedPhoto != null) {

            mServiceImage.setImageBitmap(mCompressedPhoto);
            PhotoUtils.saveImage(PhotoUtils.compressManageAspect(400, 400, mCompressedPhoto), mServiceImageFile);

        }
        mServiceImageUploaded = true;
        mServiceImage.setVisibility(View.VISIBLE);

    }

    private Bitmap getBitmap(String tag, Uri url) {
        //Cache Directory will be in app directory from now on.
        File cacheDir;
        cacheDir = new File(Utils.getStorageDirectory(getActivity()), ".OCFL311");

        if (!cacheDir.exists())
            cacheDir.mkdirs();

        File f = new File(cacheDir, tag);

        try {
            Bitmap bitmap = null;
            InputStream is = null;
            if (url.toString().startsWith("content://com.google.android.apps") ||
                    url.toString().startsWith("content://com.google.android.gallery3d")) {
                is = getActivity().getContentResolver().openInputStream(url);
            } else {
                is = new URL(url.toString()).openStream();
            }
            OutputStream os = new FileOutputStream(f);
            byte[] buffer = new byte[512];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.close();
            return BitmapFactory.decodeFile(f.getAbsolutePath());
        } catch (Exception ex) {
            // something went wrong
            ex.printStackTrace();
            return null;
        }


    }


    @Override
    public void onResume() {
        super.onResume();

    }

}
