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
 */package red.yelo.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.soundcloud.android.crop.Crop;

import org.apache.http.client.params.AllClientPNames;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import red.yelo.R;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableSubCategories;
import red.yelo.data.TableTags;
import red.yelo.data.TableWallPosts;
import red.yelo.fragments.dialogs.SingleChoiceDialogFragment;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.http.WallPostIntentService;
import red.yelo.retromodels.request.PostWallMessageRequestModel;
import red.yelo.retromodels.response.GetCollectionResponseModel;
import red.yelo.retromodels.response.GoogleGeocodeResponse;
import red.yelo.retromodels.response.KeywordSuggestionsResponseModel;
import red.yelo.retromodels.response.Tags;
import red.yelo.retromodels.response.TagsRecommendationResponseModel;
import red.yelo.retromodels.response.TagsSuggestionsResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.InternalFileContentProvider;
import red.yelo.utils.Logger;
import red.yelo.utils.MyGeoCoder;
import red.yelo.utils.PhotoUtils;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.widgets.MultiTagLayout;
import red.yelo.widgets.ProgressWheel;
import red.yelo.widgets.autocomplete.INetworkSuggestCallbacks;
import red.yelo.widgets.autocomplete.NetworkedAutoCompleteTextView;
import red.yelo.widgets.autocomplete.TokenCompleteTextView;
import retrofit.Callback;
import retrofit.mime.TypedFile;


/**
 * Class responsible for 'Create Post'
 */
public class PostOnWallActivity extends AbstractYeloActivity implements View.OnClickListener
        , Callback,
        LoaderManager.LoaderCallbacks<Cursor>,
        DBInterface.AsyncDbQueryCallback, AdapterView.OnItemClickListener, TextWatcher,
        TokenCompleteTextView.TokenListener, MultiTagLayout.OnTagClickListener,
        RetroCallback.RetroResponseListener, INetworkSuggestCallbacks {

    public static final String TAG = "PostOnWallFragment";

    private EditText mMessageEdit;

    private TextView mPlaceName, mAddMoreText;

    private LinearLayout mAddMoreLayout;

    private ProgressWheel mProgressWheel;

    private CardView mCardView;

    /**
     * GridView into which the all tag suggesstions will be placed
     */
   /* private GridView mTagMultiselectList;

    private TagsArrayAdapter mTagAdapter;*/


    private MultiTagLayout mMultiTagLayout;

    /**
     * cursor to load the categories so as to get ids of each in onclick
     */
    private Cursor mCursor;

    private String mLatitude, mLongitude, mTagIdSelected, mTagNameSelected, mGroupIdSelected, mQ,
            mKeyText;

    /**
     * Reference to the Dialog Fragment for selecting the picture type
     */
    private SingleChoiceDialogFragment mChoosePictureDialogFragment;


    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private static final int PIC_CROP = 4;


    private String mWallImageFileName = AppConstants.WALL_IMAGE_NAME;

    private Uri mCameraImageCaptureUri;


    private SimpleDateFormat mFormatter;

    private DateFormatter mMessageDateFormatter;

    private Bitmap mCompressedPhoto;

    private ImageView mWallImage;

    private ImageView mAddImagePlaceholder;

    private boolean mWasWallImageUploaded;

    private File mWallImageFile;

    private static final String ACTION_POST = "red.yelo.http.action.POST";

    private Tags[] mTags;

    private String mTextTruncate = "";

    private String mPlace, mActionColor;

    private List<String> mTagsIds = new ArrayList<String>();

    private FloatingActionButton mPostButton;


    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private String[] mHintText;

    private Toolbar mToolbar;

    private String[] mAllGocNames, mAllGocIds, mAllGocCategories, mAllSubCategoryId, mAllSubCategoryNames,
            mGocColors;

    private LinearLayout mGocLayout;

    private TextView mGocName, mCategoryName, mAttachFile, mEditLocation;

    private NetworkedAutoCompleteTextView mKeywordText;

    private FrameLayout mGocFrame, mCategoryFrameLayout;

    private ProgressDialog mProgressDialog;

    private String mCityName, mStateName, mCountryName, mAddressName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_query);

        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setToolbar(mToolbar, getResources().getString(R.string.title_post_wall), true);
        mProgressDialog = showProgressDialog();


        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                AppConstants.WALL_DATE_FORMAT);

        mMultiTagLayout = (MultiTagLayout) findViewById(R.id.tag_container);
        mMultiTagLayout.setOnTagClickListener(this);
        mMultiTagLayout.setShouldSelectOnClick(true);
        mMultiTagLayout.setSelectionMode(MultiTagLayout.SelectionMode.SINGLE);

        mPlaceName = (TextView) findViewById(R.id.text_location);
        mMessageEdit = (EditText) findViewById(R.id.message);
        mWallImage = (ImageView) findViewById(R.id.wall_image);
        mAddImagePlaceholder = (ImageView) findViewById(R.id.gallery_ic);
        mProgressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        mProgressWheel.setBarColor(getResources().getColor(R.color.primaryColorDark));
        mPostButton = (FloatingActionButton) findViewById(R.id.fabbutton);
        mCategoryFrameLayout = (FrameLayout) findViewById(R.id.category_frame_layout);
        mCategoryFrameLayout.setOnClickListener(this);
        mCategoryName = (TextView) findViewById(R.id.goc_category_name);
        mEditLocation = (TextView) findViewById(R.id.text_edit);
        mKeywordText = (NetworkedAutoCompleteTextView) findViewById(R.id.keywords);
        mAddMoreText = (TextView) findViewById(R.id.add_more_details);
        mAddMoreLayout = (LinearLayout) findViewById(R.id.layout_more_details);

        mAddMoreLayout.setOnClickListener(this);
        mAddMoreText.setOnClickListener(this);

        mKeywordText.setNetworkSuggestCallbacks(this);

        mEditLocation.setOnClickListener(this);

        mAttachFile = (TextView) findViewById(R.id.attach_image);
        mAttachFile.setOnClickListener(this);

        mGocLayout = (LinearLayout) findViewById(R.id.goc_layout);
        mGocFrame = (FrameLayout) findViewById(R.id.frame_goc);
        mGocName = (TextView) findViewById(R.id.goc_name);

        mGocLayout.setOnClickListener(this);

        mPostButton.setOnClickListener(this);

        mPlaceName.setOnClickListener(this);


        mHintText = getResources().getStringArray(R.array.post_start_sample);

        int idx = new Random().nextInt(mHintText.length);
        String random = (mHintText[idx]);
        //mMessageEdit.setHint(random);
        // mMessageEdit.setSelection(mMessageEdit.getText().length());


        mLatitude = SharedPreferenceHelper.getString(R.string.pref_latitude);
        mLongitude = SharedPreferenceHelper.getString(R.string.pref_longitude);

        if (savedInstanceState != null) {
            mMessageEdit.setHint(savedInstanceState.getString(AppConstants.Keys.MESSAGE));
            mGocName.setText(savedInstanceState.getString(AppConstants.Keys.GROUP_NAME));
            mGroupIdSelected = savedInstanceState.getString(AppConstants.Keys.GROUP_ID);
            mTagNameSelected = savedInstanceState.getString(AppConstants.Keys.TAG_ID);
            mTagIdSelected = savedInstanceState.getString(AppConstants.Keys.TAG_NAME);
            mAddressName = savedInstanceState.getString(AppConstants.Keys.ADDRESS);
            mCityName = savedInstanceState.getString(AppConstants.Keys.CITY_NAME);
            mCountryName = savedInstanceState.getString(AppConstants.Keys.COUNTRY_NAME);
            mStateName = savedInstanceState.getString(AppConstants.Keys.STATE_NAME);
            mPlaceName.setText(mAddressName);
            mKeywordText.setText(savedInstanceState.getString(AppConstants.Keys.KEYWORDS));
            mCategoryName.setText(savedInstanceState.getString(AppConstants.Keys.CATEGORY_NAME));

            mActionColor = savedInstanceState.getString(AppConstants.Keys.COLOR);
            if (!TextUtils.isEmpty(mActionColor)) {
                colorizeActionBar(Color.parseColor(mActionColor), mToolbar);
                colorizeView(Color.parseColor(mActionColor), mGocFrame);
                loadSubCategories(mGroupIdSelected);


            }


        }
        setFileName();
        mCameraImageCaptureUri = Uri.fromFile(mWallImageFile);
        fetchGocs();
        loadGroupGocs();
        if (!TextUtils.isEmpty(SharedPreferenceHelper.getString(R.string.pref_latitude))) {
            getLatLongFromAddress(Double.parseDouble(SharedPreferenceHelper.getString(R.string.pref_latitude)),
                    Double.parseDouble(SharedPreferenceHelper.getString(R.string.pref_longitude)));

            //fillAddressDetails();
        }


    }

    private void loadGroupGocs() {

        getSupportLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS, null, this);

    }

    private void loadSubCategories(String groupId) {

        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.TAG_ID, groupId);
        fetchCategories(groupId);
        getSupportLoaderManager().restartLoader(AppConstants.Loaders.LOAD_CATEGORIES, args, this);

    }

    private void fetchCategories(String groupId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SUB_CATEGORIES);

        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.TAG_ID, groupId);
        retroCallback.setExtras(args);
        retroCallbackList.add(retroCallback);
        mProgressDialog.show();
        mYeloApi.getSubCategories(groupId, retroCallback);
    }


    private void fetchGocs() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_GROUPS);
        retroCallbackList.add(retroCallback);
        mProgressDialog.show();
        mYeloApi.getGocs(retroCallback);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString(AppConstants.Keys.MESSAGE, mMessageEdit.getText().toString());
        outState.putString(AppConstants.Keys.TAG_ID, mTagIdSelected);
        outState.putString(AppConstants.Keys.GROUP_ID, mGroupIdSelected);
        outState.putString(AppConstants.Keys.TAG_NAME, mTagNameSelected);
        outState.putString(AppConstants.Keys.ADDRESS, mAddressName);
        outState.putString(AppConstants.Keys.CITY_NAME, mCityName);
        outState.putString(AppConstants.Keys.COUNTRY_NAME, mCountryName);
        outState.putString(AppConstants.Keys.STATE_NAME, mStateName);


        outState.putString(AppConstants.Keys.GROUP_NAME, mGocName.getText().toString());
        outState.putString(AppConstants.Keys.CATEGORY_ID, mTagIdSelected);
//        outState.putStringArray(AppConstants.Keys.ADDRESS, mAddress);
        outState.putString(AppConstants.Keys.COLOR, mActionColor);


        outState.putString(AppConstants.Keys.CATEGORY_NAME, mCategoryName.getText().toString());
        outState.putString(AppConstants.Keys.KEYWORDS, mKeywordText.getText().toString());

        super.onSaveInstanceState(outState);

    }

    private void loadSuggesstions() {

        getSupportLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS, null, this);

    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    private void postToWall(String tagName, String tagIdSelected, String latitude, String longitude,
                            String message, String groupId, String keywords) {
        PostWallMessageRequestModel postWallMessageRequestModel = new PostWallMessageRequestModel();

        postWallMessageRequestModel.wall.setMessage(message);
        if (tagIdSelected == null) {

        } else {
            postWallMessageRequestModel.wall.setTag_id(tagIdSelected);
        }
        postWallMessageRequestModel.wall.setLatitude(latitude);
        postWallMessageRequestModel.wall.setGroup_id(groupId);


        postWallMessageRequestModel.wall.setLongitude(longitude);

        String keywordArray[] = TextUtils.split(keywords, ",");

        List<String> keywordList = new ArrayList<String>();

        for (String keyword : keywordArray) {
            keywordList.add(keyword);
        }

        postWallMessageRequestModel.wall.setKeywords(keywordList);
        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                AppConstants.WALL_DATE_FORMAT);

        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());

        final String sentAt = mFormatter.format(new Date());
        String tempId = "";
        try {
            tempId = mMessageDateFormatter.getEpoch(sentAt) + "";
        } catch (ParseException e) {
            //should not happen
        }
        postWallMessageRequestModel.wall.setTmp_id(tempId);

        Logger.d(TAG, latitude, longitude);

        if (!TextUtils.isEmpty(mAddressName)) {


            postWallMessageRequestModel.wall.setCity(mStateName.split(",")[0]);
            postWallMessageRequestModel.wall.setCountry(mCountryName);
            postWallMessageRequestModel.wall.setAddress(mAddressName);

            RetroCallback retroCallback;
            retroCallback = new RetroCallback(this);
            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.CREATE_WALL);
            Bundle args = new Bundle();
            args.putString(AppConstants.Keys.TEMP_ID, tempId);
            retroCallback.setExtras(args);
            retroCallbackList.add(retroCallback);

            addWallLocally(tempId, tagName, tagIdSelected, groupId, latitude, longitude, message);
            mYeloApi.postWallMessage(postWallMessageRequestModel, retroCallback);

            finish();

        } else {
            Toast.makeText(this, getResources().getString(R.string.toast_select_location), Toast.LENGTH_SHORT).show();
        }
    }

    private void addWallLocally(String tempId, String tagName, String tagIdSelected, String groupId, String latitude, String longitude,
                                String message) {

        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                AppConstants.WALL_DATE_FORMAT);

        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());

        final String sentAt = mFormatter.format(new Date());


        ContentValues valuesWall = new ContentValues(6);

        valuesWall.put(DatabaseColumns.TEMP_ID, tempId);
        valuesWall.put(DatabaseColumns.MESSAGE, message);
        valuesWall.put(DatabaseColumns.TAG_NAME, tagName);
        valuesWall.put(DatabaseColumns.STATE, AppConstants.SyncStates.SYNCING + "");
        valuesWall.put(DatabaseColumns.TAG_ID, tagIdSelected);
        valuesWall.put(DatabaseColumns.USER_NAME, AppConstants.UserInfo.INSTANCE.getFirstName());
        valuesWall.put(DatabaseColumns.ADDRESS, mAddressName);
        valuesWall.put(DatabaseColumns.USER_ID, AppConstants.UserInfo.INSTANCE.getId());
        try {
            valuesWall.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(sentAt));
            valuesWall.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(sentAt));
            valuesWall.put(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, mMessageDateFormatter.getEpoch(sentAt));


        } catch (ParseException e) {
            e.printStackTrace();
            //should not happen
        }
        valuesWall.put(DatabaseColumns.USER_IMAGE, AppConstants.UserInfo.INSTANCE.getProfilePicture());


        DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, null, null
                , TableWallPosts.NAME, null, valuesWall, true, this);

    }

    private void postToWallWithImage(String groupId, String tagName, String tagIdSelected, String latitude, String longitude,
                                     String message, String imagepath, String keywords) {


//        final Map<String, String> params = new HashMap<String, String>(8);
//        params.put(HttpConstants.POST_MESSAGE, message);
//        params.put(HttpConstants.POST_LATITUDE,latitude);
//        params.put(HttpConstants.POST_LONGITUDE,longitude);
//        params.put(HttpConstants.POST_TAG_ID,tagIdSelected);


        if (!TextUtils.isEmpty(mAddressName)) {
            postOnWallWithImage(this, tagIdSelected, latitude, longitude, message, imagepath, mGroupIdSelected, mKeywordText.getText().toString());
        } else {
            Toast.makeText(this, getResources().getString(R.string.toast_select_location), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
    public void postOnWallWithImage(Context context, String tagIdSelected, String latitude,
                                    String longitude,
                                    String message, String imagepath, String groupId, String keywords) {
        Intent intent = new Intent(context, WallPostIntentService.class);

        intent.setAction(ACTION_POST);
        intent.putExtra(HttpConstants.POST_MESSAGE, message);
        intent.putExtra(HttpConstants.POST_LATITUDE, latitude);
        intent.putExtra(HttpConstants.POST_LONGITUDE, longitude);
        intent.putExtra(HttpConstants.POST_TAG_ID, tagIdSelected);
        intent.putExtra(HttpConstants.GROUP_ID, groupId);
        intent.putExtra(HttpConstants.KEYWORDS, keywords);
        intent.putExtra(HttpConstants.POST_CITY, mCityName.split(",")[0]);
        intent.putExtra(HttpConstants.POST_ADDRESS, mAddressName);
        intent.putExtra(HttpConstants.POST_COUNTRY, mCountryName);
        intent.putExtra(AppConstants.WALL_IMAGE_NAME, imagepath);

        context.startService(intent);

        this.finish();
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

        if (taskId == AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS) {
            if (updateCount == 0) {
                Logger.d(TAG, "insert");

                final ContentValues values = (ContentValues) cookie;
                DBInterface
                        .insertAsync(AppConstants.QueryTokens.INSERT_TAG_SUGGESSTIONS,
                                getTaskTag(), null, TableTags.NAME, null, values, true, this);

            }
        }

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {

            return new SQLiteLoader(this, false, TableTags.NAME, null,
                    null, null, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);
        } else if (loaderId == AppConstants.Loaders.LOAD_CATEGORIES) {

            String categoryId = bundle.getString(AppConstants.Keys.TAG_ID);

            String selection = DatabaseColumns.CATEGORY_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(this, false, TableSubCategories.NAME, null,
                    selection, new String[]{categoryId}, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);

        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());

            mTags = new Tags[cursor.getCount()];
            cursor.moveToFirst();
            mAllGocNames = new String[cursor.getCount()];
            mAllGocIds = new String[cursor.getCount()];
            mGocColors = new String[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {

                mTags[i] = new Tags(cursor.getString(
                        cursor.getColumnIndex(DatabaseColumns.NAME)),
                        cursor.getString(
                                cursor.getColumnIndex(DatabaseColumns.ID)), "image_url");

                mAllGocIds[i] = mTags[i].getId();
                mAllGocNames[i] = mTags[i].getName().toUpperCase();
                mGocColors[i] = cursor.getString(
                        cursor.getColumnIndex(DatabaseColumns.COLOR));
                Logger.v(TAG, "color = " + mGocColors[i]);

                if (i == cursor.getCount() - 1) {
                    mProgressDialog.dismiss();
                }

                cursor.moveToNext();


            }

        }

        if (loader.getId() == AppConstants.Loaders.LOAD_CATEGORIES) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());

            cursor.moveToFirst();
            mAllGocCategories = new String[cursor.getCount()];
            mAllSubCategoryId = new String[cursor.getCount()];


            for (int i = 0; i < cursor.getCount(); i++) {
                mAllGocCategories[i] = cursor.getString(
                        cursor.getColumnIndex(DatabaseColumns.NAME));

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
        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        //performTagClickOperation(position, view);

    }

    /*private void performTagClickOperation(int position, View view) {

        Tags tag = mTagAdapter.getItem(position);
        tag.toggleChecked();
        TagsArrayAdapter.TagsViewHolder viewHolder = (TagsArrayAdapter.TagsViewHolder)
                view.getTag();

        if (tag.isChecked()) {
            viewHolder.getTextView().setTextColor(getResources().getColor(R.color.grass_primary));
            viewHolder.getTextView().setBackgroundResource(R.drawable.tag_background_select);
        } else {
            viewHolder.getTextView().setTextColor(getResources().getColor(R.color.tag_text));
            viewHolder.getTextView().setBackgroundResource(R.drawable.tag_background);
        }

        if (tag.isChecked()) {

            if (!mTextTruncate.equals("")) {
                mSelectTagsSuggesstions.getText().delete(mSelectTagsSuggesstions.getText().length() - mTextTruncate.length(),
                        mSelectTagsSuggesstions.getText().length());
                mTextTruncate = "";
            }
            mSelectTagsSuggesstions.addObject(tag);
            if (!tag.getId().equals(AppConstants.NO_ID)) {
                mTagsIds.add(getTagIdFromTagName(tag.toString()));
            }
        } else {
            for (int i = 0; i < mTagsIds.size(); i++) {
                if (tag.toString().equals(getTagNameFromTagId(mTagsIds.get(i)))) {
                    mTagsIds.remove(i);
                    mSelectTagsSuggesstions.removeObject(tag);

                }
            }
        }
    }*/

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        String[] split = s.toString().split(",");

        if (split.length > 0) {
            mTextTruncate = split[split.length - 1].trim();
            if (mTextTruncate.length() > 0) {
                fetchSuggestions(split[split.length - 1].trim());
                loadSuggesstions();
            }

        } else {
            mTextTruncate = s.toString().trim();
            fetchSuggestions(s.toString().trim());

        }
        //mLocationSuggesstionAdapter.getFilter().filter(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

        if (s == null || s.length() == 0) {
            loadSuggesstions();
        }
    }

    private void fetchSuggestions(String q) {

        final Map<String, String> params = new HashMap<String, String>(1);
        params.put(HttpConstants.Q, q);

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_TAG_AUTO_SUGGESTIONS);
        retroCallbackList.add(retroCallback);

        mYeloApi.getTagSuggestions(params, retroCallback);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.select_tags_edits) {
            //mPostLayout.setVisibility(View.GONE);
            //mTagMultiselectList.setVisibility(View.VISIBLE);

        } else if (v.getId() == R.id.text_location) {
            final Intent selectLocationActivity = new Intent(this,
                    SearchLocationActivity.class);

            //selectLocationActivity.putExtra(AppConstants.Keys.PLACE,mPlaceName.getText().toString());
            selectLocationActivity.putExtra(AppConstants.Keys.FROM_WALL, true);

            startActivityForResult(selectLocationActivity, AppConstants.RequestCodes.GET_PLACE);

        } else if (v.getId() == R.id.text_edit) {
            final Intent selectLocationActivity = new Intent(this,
                    SearchLocationActivity.class);

            //selectLocationActivity.putExtra(AppConstants.Keys.PLACE,mPlaceName.getText().toString());
            selectLocationActivity.putExtra(AppConstants.Keys.FROM_WALL, true);

            startActivityForResult(selectLocationActivity, AppConstants.RequestCodes.GET_PLACE);
        } else if (v.getId() == R.id.goc_layout) {

            showChooseGocDialog();

        } else if (v.getId() == R.id.category_frame_layout) {
            if (mGroupIdSelected == null)
                Toast.makeText(this, getResources().getString(R.string.select_category_first), Toast.LENGTH_SHORT).show();
            else
                showChooseCategoryDialog();
        } else if (v.getId() == R.id.add_more_details) {
            if (mAddMoreLayout.getVisibility() == View.GONE) {
                mAddMoreLayout.setVisibility(View.VISIBLE);
            } else {
                mAddMoreLayout.setVisibility(View.GONE);
            }
        } else if (v.getId() == R.id.attach_image) {

            showChoosePictureSourceDialog();

        } else if (v.getId() == R.id.fabbutton) {
            if (TextUtils.isEmpty(mMessageEdit.getText().toString().trim())) {
                Toast.makeText(this, getResources().getString(R.string.please_write_your_query), Toast.LENGTH_SHORT).show();

            }
//                else if(validatePostMessage(mMessageEdit.getText().toString()).equals(AppConstants.MessageType.PHONE_PRESENT)){
//                    Toast.makeText(getActivity(), getResources().getString(R.string.no_phone_number_message), Toast.LENGTH_SHORT).show();
//
//                }
            else {
                if (mGroupIdSelected != null) {
                    if (mTagIdSelected != null) {
                        if (!TextUtils.isEmpty(mAddressName)) {
                            if (mWasWallImageUploaded) {
                                TypedFile typedFile;
                                File photo;
                                photo = new File(mWallImageFile.getAbsolutePath());
                                typedFile = new TypedFile("application/octet-stream", photo);

                                postToWallWithImage(mGroupIdSelected, mTagNameSelected, mTagIdSelected, mLatitude, mLongitude,
                                        mMessageEdit.getText().toString(), mWallImageFile.getAbsolutePath(), mKeywordText.getText().toString());

                            } else {
                                postToWall(mTagNameSelected, mTagIdSelected, mLatitude, mLongitude, mMessageEdit.getText().toString(), mGroupIdSelected, mKeywordText.getText().toString());
                            }
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.please_select_location), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.please_select_subcategory_message), Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.please_select_tags_message), Toast.LENGTH_SHORT).show();
                }


            }
        }
    }

    /**
     * Method to handle click on profile image
     */
    private void showChoosePictureSourceDialog() {

        new MaterialDialog.Builder(this)
                .items(getResources().getStringArray(R.array.take_photo_choices_wall))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0) { // Pick from camera
                            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, InternalFileContentProvider.POST_PIC_URI);

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


    private void getLatLongFromAddress(double latitude, double longitude) {

        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());

        if (Geocoder.isPresent()) {

            try {
                List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, 1);

                if (addresses.size() > 0) {

                    double lat = addresses.get(0).getLatitude();
                    double lng = addresses.get(0).getLongitude();
                    String locality = addresses.get(0).getSubLocality();
                    mCityName = addresses.get(0).getAddressLine(2);
                    mStateName = addresses.get(0).getAdminArea();
                    mCountryName = addresses.get(0).getCountryName();
                    mAddressName = addresses.get(0).getSubLocality();
                    mLatitude = addresses.get(0).getLatitude() + "";
                    mLongitude = addresses.get(0).getLongitude() + "";
                    if(TextUtils.isEmpty(locality)) {
                        fillAddressDetails();
                    }
                    else {
                        mPlaceName.setText(locality);

                    }
                    Logger.d(TAG, lat + "  " + lng);

                } else {
                    fillAddressDetails();
                }

            } catch (Exception e) {
                e.printStackTrace();
                fillAddressDetails();
            }
        }
        else {
            fillAddressDetails();
        }
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
                        new Crop(mCameraImageCaptureUri).output(mCameraImageCaptureUri).withAspect(600, 300).start(this);

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

                case AppConstants.RequestCodes.GET_PLACE: {
                    if (resultCode == Activity.RESULT_OK) {
                        String place = data.getStringExtra(AppConstants.Keys.PLACE);
                        mLatitude = data.getStringExtra(AppConstants.Keys.LATITUDE);
                        mLongitude = data.getStringExtra(AppConstants.Keys.LONGITUDE);
                        mPlaceName.setText(place);
                        mAddressName = place;

                        getLatLongFromAddress(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude));
                        break;
                    }
                }

                case PICK_FROM_FILE:
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        final String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
                        Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
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

                                            mCompressedPhoto = getBitmap(mWallImageFileName, uriurl);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mWasWallImageUploaded = true;
                                                    mCompressedPhoto = PhotoUtils.compressManageAspect(500, 500, mCompressedPhoto);
                                                    mWallImage.setImageBitmap(mCompressedPhoto);
                                                    mWallImage.setVisibility(View.VISIBLE);
                                                    mAddImagePlaceholder.setVisibility(View.GONE);
                                                    PhotoUtils.saveImage(PhotoUtils.compressManageAspect(500, 500, mCompressedPhoto), mWallImageFile);
                                                }
                                            });
                                        }
                                    }).start();


                                }
                            } else { // it is a regular local image file
                                Uri mGalleryImageCaptureUri = data.getData();
                                //setAndSaveImage(mGalleryImageCaptureUri, PICK_FROM_FILE);
                                //performCrop(mGalleryImageCaptureUri);
                                new Crop(mGalleryImageCaptureUri).output(mCameraImageCaptureUri).withAspect(600, 300).start(this);
                                // doCrop(PICK_FROM_FILE);


                            }
                        }

                        break;
                    }
            }
        }
    }

    private Bitmap getBitmap(String tag, Uri url) {
        //Cache Directory will be in app directory from now on.
        File cacheDir;
        cacheDir = new File(Utils.getStorageDirectory(this), ".OCFL311");

        if (!cacheDir.exists())
            cacheDir.mkdirs();

        File f = new File(cacheDir, tag);

        try {
            Bitmap bitmap = null;
            InputStream is = null;
            if (url.toString().startsWith("content://com.google.android.apps") ||
                    url.toString().startsWith("content://com.google.android.gallery3d")) {
                is = this.getContentResolver().openInputStream(url);
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
                .rotateBitmapIfNeededAndCompressIfTold(this, uri, source_string, false);


        if (mCompressedPhoto != null) {

            mWallImage.setImageBitmap(mCompressedPhoto);
            mAddImagePlaceholder.setVisibility(View.GONE);
            PhotoUtils.saveImage(PhotoUtils.compressManageAspect(500, 500, mCompressedPhoto), mWallImageFile);

        }
        mWasWallImageUploaded = true;
        mWallImage.setVisibility(View.VISIBLE);

    }


    private void setFileName() {

        mWallImageFile = new File(Utils.getStorageDirectory(this), mWallImageFileName);
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {


            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            case R.id.action_post: {


                if (mMessageEdit.getText().toString().trim().equals("Put me in touch with")) {
                    Toast.makeText(this, getResources().getString(R.string.please_write_your_query), Toast.LENGTH_SHORT).show();

                }
//                else if(validatePostMessage(mMessageEdit.getText().toString()).equals(AppConstants.MessageType.PHONE_PRESENT)){
//                    Toast.makeText(this, getResources().getString(R.string.no_phone_number_message), Toast.LENGTH_SHORT).show();
//
//                }
                else {
                    if (mTagNameSelected != null) {
                        if (!TextUtils.isEmpty(mAddressName)) {
                            if (mWasWallImageUploaded) {
                                TypedFile typedFile;
                                File photo;
                                photo = new File(mWallImageFile.getAbsolutePath());
                                typedFile = new TypedFile("application/octet-stream", photo);

                                postToWallWithImage(mGroupIdSelected, mTagNameSelected, mTagIdSelected, mLatitude, mLongitude,
                                        mMessageEdit.getText().toString(), mWallImageFile.getAbsolutePath(), mKeywordText.getText().toString());

                            } else {
                                postToWall(mTagNameSelected, mTagIdSelected, mLatitude, mLongitude, mMessageEdit.getText().toString(), mGroupIdSelected, mKeywordText.getText().toString());
                            }
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.please_select_location), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.please_select_tags_message), Toast.LENGTH_SHORT).show();
                    }


                    return true;
                }
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onTokenAdded(Object token) {
        MultiTagLayout.Tag tag = (MultiTagLayout.Tag) token;
        mMultiTagLayout.setTagSelected(tag.id, true);
        mTagsIds.add(tag.id);
    }

    @Override
    public void onTokenRemoved(Object token) {

        MultiTagLayout.Tag tag = (MultiTagLayout.Tag) token;
        mMultiTagLayout.setTagSelected(tag.id, false);

    }

    private void fetchTagSuggestions() {

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_TAG_SUGGESTIONS);
        retroCallbackList.add(retroCallback);

        mYeloApi.getTagRecommendations(retroCallback);

        mProgressWheel.setVisibility(View.VISIBLE);
        mProgressWheel.spin();

    }

    @Override
    public void onTagClicked(View view, MultiTagLayout.Tag tag) {

        mTagIdSelected = tag.id;
        mTagNameSelected = tag.name;
    }

    @Override
    public void success(Object model, int requestId) {
        switch (requestId) {

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


            case HttpConstants.ApiResponseCodes.GET_TAG_AUTO_SUGGESTIONS: {
                this.setProgressBarIndeterminateVisibility(false);


                TagsSuggestionsResponseModel tagsSuggestionsResponseModel = ((TagsSuggestionsResponseModel) model);
                mTags = new Tags[tagsSuggestionsResponseModel.tags.size()];

                for (int i = 0; i < tagsSuggestionsResponseModel.tags.size(); i++) {

                    mTags[i] = new Tags(tagsSuggestionsResponseModel.tags.get(i).name,
                            tagsSuggestionsResponseModel.tags.get(i).id, "image_url");


                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.ID, tagsSuggestionsResponseModel.tags.get(i).id);
                    values.put(DatabaseColumns.NAME, tagsSuggestionsResponseModel.tags.get(i).name);


                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    Logger.d(TAG, "UPDATE");

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
                            TableTags.NAME, values, selection, new String[]{tagsSuggestionsResponseModel.tags.get(i).id}, true, this);


                }
                /*ArrayList<MultiTagLayout.Tag> tagList = new ArrayList<MultiTagLayout.Tag>(mTags.length);
                //tagList.addAll(Arrays.asList(mTags));
                *//*mTagAdapter = new TagsArrayAdapter(this, R.layout.layout_tag_grid, tagList, true);

                mTagMultiselectList.setAdapter(mTagAdapter);*//*

                for(Tags eachTag : mTags) {
                    tagList.add(new MultiTagLayout.Tag(eachTag.getId(), eachTag.getName()));
                }*/

                break;
            }
            case HttpConstants.ApiResponseCodes.GET_TAG_SUGGESTIONS: {
                TagsRecommendationResponseModel tagsRecommendationResponseModel = ((TagsRecommendationResponseModel) model);


                mProgressWheel.setVisibility(View.INVISIBLE);
                for (int i = 0; i < tagsRecommendationResponseModel.tags.size(); i++) {

                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.ID, tagsRecommendationResponseModel.tags.get(i).id);
                    values.put(DatabaseColumns.NAME, tagsRecommendationResponseModel.tags.get(i).name);


                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    Logger.d(TAG, "UPDATE");

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
                            TableTags.NAME, values, selection, new String[]{tagsRecommendationResponseModel.tags.get(i).id}, true, this);


                }
                for (int i = 0; i < tagsRecommendationResponseModel.user_tags.size(); i++) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.ID, tagsRecommendationResponseModel.user_tags.get(i).id);
                    values.put(DatabaseColumns.NAME, tagsRecommendationResponseModel.user_tags.get(i).name);


                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
                            TableTags.NAME, values, selection, new String[]{tagsRecommendationResponseModel.user_tags.get(i).id}, true, this);


                }

                break;
            }
            case HttpConstants.ApiResponseCodes.CREATE_WALL: {

                Toast.makeText(this, "Wall Posted", Toast.LENGTH_SHORT).show();

                // all the db changes has been moved to RetroCallback
                // as I am instantly closing this fragment as a response

                break;
            }

            case HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE: {

                GoogleGeocodeResponse googleGeocodeResponse = ((GoogleGeocodeResponse) model);
                if (googleGeocodeResponse.results.size() != 0) {

                    mPlaceName.setText(googleGeocodeResponse.results.get(0).getAddress()[0]);

                    String[] address = googleGeocodeResponse.results.get(0).getAddress();
                    if (address.length > 3) {
                        mCityName = address[address.length - 3].trim();
                        mStateName =address[address.length - 2].trim();
                        mCountryName = address[address.length - 1].trim();
                        mAddressName = address[address.length - 4].trim();
                    } else if (address.length >= 1) {
                        mAddressName = address[0].trim();
                        mStateName = mAddressName;
                        mCountryName = "India";

                    }
                    mPlaceName.setText(mAddressName);
                }

                break;
            }

            default:
                break;
        }
    }


    private void fillAddressDetails() {

        final Map<String, String> params = new HashMap<String, String>(6);

        params.put(HttpConstants.LATLNG, mLatitude
                + "," + mLongitude);
        params.put("language", Locale.getDefault().getCountry());
        params.put("sensor", "false");

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE);
        retroCallbackList.add(retroCallback);

        mGoogleApi.getMyAddress(params, retroCallback);

    }


    @Override
    public void failure(int requestId, int errorCode, String message) {
    }

    @Override
    public void onPause() {
        super.onPause();


    }

    private void performCrop(Uri picUri) {
//        try {
//            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(600,300,600, 300, Uri.fromFile(mWallImageFile));
//            cropImage.setOutputQuality(100);
//            cropImage.setSourceImage(picUri);
//            startActivityForResult(cropImage.getIntent(this), PIC_CROP);
//        }
//        // respond to users whose devices do not support the crop action
//        catch (ActivityNotFoundException anfe) {
//            // display an error message
//            String errorMessage = "Whoops - your device doesn't support the crop action!";
//            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
//            toast.show();
//        }
    }

    private String validatePostMessage(String message) {


        if (message.matches("[0-9]+") && message.length() == 10) {
            return AppConstants.MessageType.PHONE_PRESENT;
        }
        return AppConstants.MessageType.MESSAGE_OK;
    }

    /**
     * Method to handle click on goc
     */
    private void showChooseGocDialog() {
        final int[] materialColors = getResources().getIntArray(R.array.collectionListColors);


        new MaterialDialog.Builder(this)
                .items(mAllGocNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                                   @Override
                                   public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                       mActionColor = mGocColors[which];
                                       mGocName.setText(mAllGocNames[which]);
                                       mCategoryName.setText("Subcategory");
                                       mTagIdSelected = null;
                                       colorizeActionBar(Color.parseColor(mActionColor), mToolbar);
                                       colorizeView(Color.parseColor(mActionColor), mGocFrame);

                                       mGroupIdSelected = mAllGocIds[which];

                                       loadSubCategories(mGroupIdSelected);
                                   }

                               }

                ).show();

    }


    /**
     * Method to handle click on goc
     */
    private void showChooseCategoryDialog() {

        new MaterialDialog.Builder(this)
                .items(mAllGocCategories)
                .itemsCallback(new MaterialDialog.ListCallback() {
                                   @Override
                                   public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                       mCategoryName.setText(mAllGocCategories[which]);
                                       mTagIdSelected = mAllSubCategoryId[which];
                                       mTagNameSelected = mAllGocCategories[which];
                                   }

                               }

                ).show();

    }

    private void showBackPressDialog() {
        new MaterialDialog.Builder(this)
                .title("Discard changes?")
                .positiveText("YES")
                .negativeText("NO")
                .positiveColor(getResources().getColor(R.color.blue_link))
                .negativeColor(getResources().getColor(R.color.blue_link))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        finish();
                    }
                })
                .show();
    }


    @Override
    public void performNetworkQuery(NetworkedAutoCompleteTextView textView, String query) {

        mQ = query;
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SUGGESTIONS);

        retroCallbackList.add(retroCallback);

        final Map<String, String> params = new HashMap<String, String>(3);
        params.put(HttpConstants.Q, query);
        params.put(HttpConstants.Tag_ID, mTagIdSelected);
        params.put(HttpConstants.TYPE, AppConstants.SuggestionType.KEYWORDS);
        mYeloApi.getSuggestions(params, retroCallback);
    }

    @Override
    public void onSuggestionClicked(NetworkedAutoCompleteTextView textView, KeywordSuggestionsResponseModel.Keywords suggestion) {

        if (TextUtils.isEmpty(mKeyText)) {
            mKeyText = suggestion.name;
        } else {
            mKeyText = mKeyText + "," + suggestion.name;
        }
        textView.setText(mKeyText + ",");
        textView.setSelection(textView.getText().length());

    }

    public boolean hasActivityChanged() {
        return mGroupIdSelected != null || !mMessageEdit.getText().toString().trim().isEmpty() || mWasWallImageUploaded || !mKeywordText.getText().toString().trim().isEmpty();
    }

    @Override
    public void onBackPressed() {
        if (hasActivityChanged()) {
            showBackPressDialog();
        } else
            super.onBackPressed();
    }


}
