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
 */package red.yelo.fragments;


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
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.melnykov.fab.FloatingActionButton;
import com.soundcloud.android.crop.Crop;

import org.w3c.dom.Text;

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

import red.yelo.R;
import red.yelo.activities.SearchLocationActivity;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableSubCategories;
import red.yelo.data.TableTags;
import red.yelo.data.TableWallPosts;
import red.yelo.http.HttpConstants;
import red.yelo.http.HttpConstants.ApiResponseCodes;
import red.yelo.http.RetroCallback;
import red.yelo.http.WallPostIntentService;
import red.yelo.retromodels.request.PostWallMessageRequestModel;
import red.yelo.retromodels.response.GoogleGeocodeResponse;
import red.yelo.retromodels.response.Tags;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.Keys;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.InternalFileContentProvider;
import red.yelo.utils.Logger;
import red.yelo.utils.PhotoUtils;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import retrofit.mime.TypedFile;


/**
 * @author Sharath Pandeshwar
 * @since 04/03/15.
 * Fragment responsible for letting user to create or edit a wall post.
 */
public class CreateEditWallPostFragment extends AbstractYeloFragment implements DBInterface.AsyncDbQueryCallback, LoaderManager.LoaderCallbacks<Cursor>, OnClickListener, RetroCallback.RetroResponseListener {

    public static final String TAG = "CreateEditWallPostFragment";


    /**
     * This flag tells if the fragment is editing or creating a new post.
     */
    private boolean mIsInEditMode = false;

    /**
     * If in 'Edit' mode this variable will hold the Id of the wallpost being edited.
     */
    private String mWallId;

    /**
     * EditText holding description of the post.
     */
    private EditText mMessageEdit;

    /**
     * Textview holding Category and Sub Category names.
     */
    private TextView mGocName, mCategoryName;

    private ImageView mGocPickerDropdown;

    /**
     * Holder for Wall Image
     */
    private ImageView mWallImage;

    private Uri mCameraImageCaptureUri;

    private TextView mPlaceName, mEditLocation, mAttachImage;


    private Toolbar mToolbar;

    private String mLatitude, mLongitude, mTagIdSelected, mTagNameSelected, mGroupIdSelected;

    private Tags[] mTags;

    private String[] mAllGocNames, mAllGocIds, mAllGocCategories, mAllSubCategoryId, mAllSubCategoryNames, mGocColors;

    private String mActionColor;

    private FrameLayout mGocFrame, mCategoryFrameLayout;

    private ProgressDialog mProgressDialog;

    private LinearLayout mGocLayout;

    private File mWallImageFile;

    private Bitmap mCompressedPhoto;

    private boolean mWasWallImageUploaded;

    private ImageView mAddImagePlaceholder;

    private FloatingActionButton mPostButton;

    private DateFormatter mMessageDateFormatter;

    private SimpleDateFormat mFormatter;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;
    private static final int PIC_CROP = 4;
    private String mWallImageFileName = AppConstants.WALL_IMAGE_NAME;

    private static final String ACTION_POST = "red.yelo.http.action.POST";
    private static final String ACTION_PUT = "red.yelo.http.action.PUT";

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private String mCityName, mStateName, mCountryName, mAddressName;



    //*******************************************************************
    // Life Cycle Related Functions
    //*******************************************************************


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater.inflate(R.layout.fragment_create_edit_post_wall, container, false);
        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, AppConstants.WALL_DATE_FORMAT);

        Bundle extras = getArguments();

        if (extras != null) {
            if (extras.containsKey(AppConstants.Keys.EDIT_POST)) {
                mIsInEditMode = extras.getBoolean(AppConstants.Keys.EDIT_POST);
            }
            mWallId = extras.getString(AppConstants.Keys.ID);
            mLatitude = SharedPreferenceHelper.getString(R.string.pref_latitude);
            mLongitude = SharedPreferenceHelper.getString(R.string.pref_longitude);
        }

        initializeViews(contentView, savedInstanceState, mIsInEditMode);

        setFileName();
        mCameraImageCaptureUri = Uri.fromFile(mWallImageFile);

        if (mIsInEditMode) {
            setToolbar(mToolbar, getResources().getString(R.string.title_edit_post), true);
            /* Load Details about the Wall Post to be edited. */
            loadWallPostDetails();
        } else {
            setToolbar(mToolbar, getResources().getString(R.string.title_post_wall), true);
            /* Load Categories and Sub Categories */
            loadGroupGocs();
        }

        if (!TextUtils.isEmpty(SharedPreferenceHelper.getString(R.string.pref_latitude))) {
            getLatLongFromAddress(Double.parseDouble(SharedPreferenceHelper.getString(R.string.pref_latitude)),
                    Double.parseDouble(SharedPreferenceHelper.getString(R.string.pref_longitude)));
        }

        return contentView;
    }


    @Override
    public void onPause() {
        super.onPause();

        // allow the request to run in the background :)

//        for (RetroCallback aRetroCallbackList : retroCallbackList) {
//            if (aRetroCallbackList.getRequestId() != HttpConstants.ApiResponseCodes.CREATE_WALL)
//                aRetroCallbackList.cancel();
//        }
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
        outState.putString(AppConstants.Keys.COLOR, mActionColor);
        outState.putString(AppConstants.Keys.CATEGORY_NAME, mCategoryName.getText().toString());
        /**
         * TODO: Ask Anshul
         */
        //outState.putString(AppConstants.Keys.KEYWORDS, mKeywordText.getText().toString());
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case AppConstants.RequestCodes.GET_PLACE: {
                    String place = data.getStringExtra(AppConstants.Keys.PLACE);
                    mLatitude = data.getStringExtra(AppConstants.Keys.LATITUDE);
                    mLongitude = data.getStringExtra(AppConstants.Keys.LONGITUDE);
                    mPlaceName.setText(place);
                    getLatLongFromAddress(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude));
                    break;
                }

                case PICK_FROM_CAMERA:
                    new Crop(mCameraImageCaptureUri).output(mCameraImageCaptureUri).withAspect(600, 300).start(getActivity());
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
                            if (selectedImage.toString().startsWith("content://com.google.android.apps") || selectedImage.toString().startsWith("content://com.google.android.gallery3d")) {
                                columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                                if (columnIndex != -1) {
                                    final Uri uriurl = selectedImage;
//                                Logger.d(TAG,selectedImage.toString());
                                    // Do this in a background thread, since we are fetching a large image from the web

                                    new Thread(new Runnable() {
                                        public void run() {
                                            mCompressedPhoto = getBitmap(mWallImageFileName, uriurl);
                                            getActivity().runOnUiThread(new Runnable() {
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
                                new Crop(mGalleryImageCaptureUri).output(mCameraImageCaptureUri).withAspect(600, 300).start(getActivity());
                            }
                        }

                        break;
                    }
            }
        }
    }


    @Override
    public boolean onBackPressed() {
        if (hasFragmentChanged()) {
            showBackPressDialog();
            return true;
        } else {
            return false;
        }
    }

    //*******************************************************************
    // View Related Functions
    //*******************************************************************


    private void initializeViews(View v, Bundle savedInstanceState, boolean isEditMode) {
        mMessageEdit = (EditText) v.findViewById(R.id.message);
        mGocName = (TextView) v.findViewById(R.id.goc_name);
        mCategoryName = (TextView) v.findViewById(R.id.goc_category_name);
        mGocPickerDropdown = (ImageView) v.findViewById(R.id.id_goc_picker_dropdown);
        mAddImagePlaceholder = (ImageView) v.findViewById(R.id.gallery_ic);
        mPlaceName = (TextView) v.findViewById(R.id.text_location);
        mPlaceName.setOnClickListener(this);
        mEditLocation = (TextView) v.findViewById(R.id.text_edit);
        mEditLocation.setOnClickListener(this);
        mAttachImage = (TextView) v.findViewById(R.id.attach_image);
        mAttachImage.setOnClickListener(this);
        mWallImage = (ImageView) v.findViewById(R.id.wall_image);
        mGocFrame = (FrameLayout) v.findViewById(R.id.frame_goc);
        mGocLayout = (LinearLayout) v.findViewById(R.id.goc_layout);
        mCategoryFrameLayout = (FrameLayout) v.findViewById(R.id.category_frame_layout);
        mProgressDialog = showProgressDialog();
        mPostButton = (FloatingActionButton) v.findViewById(R.id.fabbutton);
        mPostButton.setOnClickListener(this);

        /* Change the UI to suit Edit Mode. */
        if (mIsInEditMode) {
            mGocPickerDropdown.setVisibility(View.GONE);
            mGocLayout.setBackgroundColor(Color.TRANSPARENT);
        }

        if (savedInstanceState != null) {

            mMessageEdit.setHint(savedInstanceState.getString(AppConstants.Keys.MESSAGE));
            mGocName.setText(savedInstanceState.getString(AppConstants.Keys.GROUP_NAME));
            mGroupIdSelected = savedInstanceState.getString(AppConstants.Keys.GROUP_ID);
            mTagNameSelected = savedInstanceState.getString(AppConstants.Keys.TAG_NAME);
            mTagIdSelected = savedInstanceState.getString(AppConstants.Keys.TAG_ID);
            mAddressName = savedInstanceState.getString(AppConstants.Keys.ADDRESS);
            mCityName = savedInstanceState.getString(AppConstants.Keys.CITY_NAME);
            mCountryName = savedInstanceState.getString(AppConstants.Keys.COUNTRY_NAME);
            mStateName = savedInstanceState.getString(AppConstants.Keys.STATE_NAME);
            mPlaceName.setText(mAddressName);

            /**
             * TODO: Discuss with Anshul
             */
            //mKeywordText.setText(savedInstanceState.getString(AppConstants.Keys.KEYWORDS));
            mCategoryName.setText(savedInstanceState.getString(AppConstants.Keys.CATEGORY_NAME));

            mActionColor = savedInstanceState.getString(AppConstants.Keys.COLOR);
            if (!TextUtils.isEmpty(mActionColor)) {
                colorizeActionBar(Color.parseColor(mActionColor), mToolbar);
                colorizeView(Color.parseColor(mActionColor), mGocFrame);
                loadSubCategories(mGroupIdSelected);
            }
        }
    }


    /**
     * This should be called only when in Edit Mode.
     *
     * @param cursor
     */
    private void loadWallDataToViews(final Cursor cursor) {
        /**
         * TODO: Confirm with Anshul this is right.
         */
        mGroupIdSelected = cursor.getString(cursor.getColumnIndex(DatabaseColumns.GROUP_ID));
        mTagNameSelected = cursor.getString(cursor.getColumnIndex(DatabaseColumns.TAG_NAME));
        mTagIdSelected = cursor.getString(cursor.getColumnIndex(DatabaseColumns.TAG_ID));
        Logger.i(TAG, "%s", "The loaded Group has id=" + mGroupIdSelected + " and tag name= " + mTagNameSelected);

        String name = cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_NAME));
        String message = cursor.getString(cursor.getColumnIndex(DatabaseColumns.MESSAGE));
        String groupName = cursor.getString(cursor.getColumnIndex(DatabaseColumns.GROUP_NAME));

        String mWallImageUrl = cursor.getString(cursor.getColumnIndex(DatabaseColumns.WALL_IMAGES));
        String color = cursor.getString(cursor.getColumnIndex(DatabaseColumns.COLOR));

        String city = cursor.getString(cursor.getColumnIndex(DatabaseColumns.CITY));
        String country = cursor.getString(cursor.getColumnIndex(DatabaseColumns.COUNTRY));
        String address = cursor.getString(cursor.getColumnIndex(DatabaseColumns.ADDRESS));


        mMessageEdit.setText(message);
        mGocName.setText(groupName.toUpperCase());
        if (address != null && !TextUtils.isEmpty(address)) {
            mPlaceName.setText(address);
            mAddressName = address;
            mCityName = city;
            mCountryName = country;
        }

        mCategoryName.setText(mTagNameSelected);

        if (mWallImageUrl.equals("")) {
            mWallImage.setVisibility(View.GONE);
        } else {
            mWallImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(mWallImageUrl).asBitmap().centerCrop().override(500, 300).animate(R.anim.fade_in).placeholder(R.color.snow_light).into(mWallImage);
        }

        if (color != null && !TextUtils.isEmpty(color)) {
            /* Load Appropriate color for the chosen category */
            mActionColor = color;
            colorizeActionBar(Color.parseColor(mActionColor), mToolbar);
            colorizeView(Color.parseColor(mActionColor), mGocFrame);
        }

        loadSubCategories(mGroupIdSelected);
    }


    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.goc_layout) {
            showChooseGocDialog();
        } else if (v.getId() == R.id.category_frame_layout) {
            if (mGroupIdSelected == null)
                Toast.makeText(getActivity(), getResources().getString(R.string.select_category_first), Toast.LENGTH_SHORT).show();
            else showChooseCategoryDialog();
        } else if ((v.getId() == R.id.text_location) || (v.getId() == R.id.text_edit)) {
            final Intent selectLocationActivity = new Intent(getActivity(), SearchLocationActivity.class);
            selectLocationActivity.putExtra(AppConstants.Keys.FROM_WALL, true);
            startActivityForResult(selectLocationActivity, AppConstants.RequestCodes.GET_PLACE);
        } else if (v.getId() == R.id.attach_image) {
            showChoosePictureSourceDialog();
        } else if (v.getId() == R.id.fabbutton) {
            if (TextUtils.isEmpty(mMessageEdit.getText().toString().trim())) {
                Toast.makeText(getActivity(), getResources().getString(R.string.please_write_your_query), Toast.LENGTH_SHORT).show();
            } else {
                if (mGroupIdSelected != null) {
                    if (mTagIdSelected != null) {
                        if (!TextUtils.isEmpty(mAddressName)) {
                            if (mWasWallImageUploaded) {
                                TypedFile typedFile;
                                File photo;
                                photo = new File(mWallImageFile.getAbsolutePath());
                                typedFile = new TypedFile("application/octet-stream", photo);
                                //postToWallWithImage(mGroupIdSelected, mTagNameSelected, mTagIdSelected, mLatitude, mLongitude, mAddress, mMessageEdit.getText().toString(), mWallImageFile.getAbsolutePath(), mKeywordText.getText().toString());
                                postToWallWithImage(mGroupIdSelected, mTagNameSelected, mTagIdSelected, mLatitude, mLongitude, mMessageEdit.getText().toString(), mWallImageFile.getAbsolutePath(), null);
                            } else {
                                //postToWall(mTagNameSelected, mTagIdSelected, mLatitude, mLongitude, mAddress, mMessageEdit.getText().toString(), mGroupIdSelected, mKeywordText.getText().toString());
                                postToWall(mTagNameSelected, mTagIdSelected, mLatitude, mLongitude, mMessageEdit.getText().toString(), mGroupIdSelected, null);
                            }
                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_location), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.please_select_subcategory_message), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.please_select_tags_message), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //*******************************************************************
    // Data Related Functions
    //*******************************************************************


    /* Utility function to asynchronously load details about the wall post */
    private void loadWallPostDetails() {
        String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
        DBInterface.queryAsync(AppConstants.QueryTokens.QUERY_WALL_DETAILS, getTaskTag(), null, true, TableWallPosts.NAME, null, selection, new String[]{mWallId}, null, null, null, null, this);
    }


    /**
     * Loader function to load list of main category items
     */
    private void loadGroupGocs() {
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS, null, this);
    }


    private void loadSubCategories(String groupId) {
        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.TAG_ID, groupId);
        fetchCategories(groupId);
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_CATEGORIES, args, this);
    }


    private void addEditWallLocally(String tempId, String tagName, String tagIdSelected, String groupId, String latitude, String longitude, String message, final boolean isUpdate) {

        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, AppConstants.WALL_DATE_FORMAT);
        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());
        final String sentAt = mFormatter.format(new Date());

        ContentValues valuesWall = new ContentValues(6);
        valuesWall.put(DatabaseColumns.TEMP_ID, tempId);
        valuesWall.put(DatabaseColumns.MESSAGE, message);
        valuesWall.put(DatabaseColumns.TAG_NAME, tagName);
        valuesWall.put(DatabaseColumns.STATE, AppConstants.SyncStates.SYNCING + "");
        valuesWall.put(DatabaseColumns.TAG_ID, tagIdSelected);
        valuesWall.put(DatabaseColumns.USER_NAME, AppConstants.UserInfo.INSTANCE.getFirstName());
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

        if (isUpdate) {
            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
            DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, null, null, TableWallPosts.NAME, valuesWall, selection, new String[]{mWallId}, true, this);
        } else {
            DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, null, null, TableWallPosts.NAME, null, valuesWall, true, this);
        }
    }


    /**
     * Method called when an asynchronous insert operation is done
     *
     * @param taskId      The token passed into the async mthod
     * @param cookie      Any extra object passed into the query.
     * @param insertRowId The inserted row id, or -1 if it failed
     */
    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {
        if (taskId == AppConstants.QueryTokens.INSERT_WALLPOST && insertRowId != -1) {
            Logger.i(TAG, "Successfully inserted at row no. " + insertRowId);
        }
    }


    /**
     * Method called when an asynchronous delete operation is done
     *
     * @param taskId      The token passed into the async method
     * @param cookie      Any extra object passed into the query.
     * @param deleteCount The number of rows deleted
     */
    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }


    /**
     * Method called when an asynchronous update operation is done
     *
     * @param taskId      The token passed into the async method
     * @param cookie      Any extra object passed into the query.
     * @param updateCount The number of rows updated
     */
    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {
        if (taskId == AppConstants.QueryTokens.UPDATE_WALLPOST) {
            Logger.i(TAG, "%s", "Wall Post update count =" + updateCount);
        }
        /**
         * Note: Not taking care of update failed cases.
         */
    }


    /**
     * Method called when an asyncronous query operation is done
     *
     * @param taskId The token passed into the async method
     * @param cookie Any extra object passed into the query.
     * @param cursor The {@link Cursor} read from the database
     */
    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {
        if (taskId == AppConstants.QueryTokens.QUERY_WALL_DETAILS) {
            Logger.i(TAG, "%s", "Wall Post detail loaded with count =" + cursor.getCount());
            if (isAttached() && cursor.getCount() > 0 && cursor.moveToFirst()) {
                loadWallDataToViews(cursor);
            }
            cursor.close();
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {
            return new SQLiteLoader(getActivity(), false, TableTags.NAME, null, null, null, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);
        } else if (loaderId == AppConstants.Loaders.LOAD_CATEGORIES) {
            String categoryId = bundle.getString(AppConstants.Keys.TAG_ID);
            String selection = DatabaseColumns.CATEGORY_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableSubCategories.NAME, null, selection, new String[]{categoryId}, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);
        } else {
            return null;
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {
            Logger.d(TAG, "Suggestion Tags Cursor Loaded with count: %d", cursor.getCount());
            mTags = new Tags[cursor.getCount()];
            cursor.moveToFirst();
            mAllGocNames = new String[cursor.getCount()];
            mAllGocIds = new String[cursor.getCount()];
            mGocColors = new String[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {
                mTags[i] = new Tags(cursor.getString(cursor.getColumnIndex(DatabaseColumns.NAME)), cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID)), "image_url");
                mAllGocIds[i] = mTags[i].getId();
                mAllGocNames[i] = mTags[i].getName().toUpperCase();
                mGocColors[i] = cursor.getString(cursor.getColumnIndex(DatabaseColumns.COLOR));

                if (i == cursor.getCount() - 1) {
                    mProgressDialog.dismiss();
                }
                cursor.moveToNext();
            }

            // Now that all GOCs are loaded mGocLayout is ready to show the popup dialog.
            if (!mIsInEditMode) {
                mGocLayout.setOnClickListener(this);
            }
        }

        if (loader.getId() == AppConstants.Loaders.LOAD_CATEGORIES) {
            Logger.d(TAG, "Categories Cursor Loaded with count: %d", cursor.getCount());
            cursor.moveToFirst();
            mAllGocCategories = new String[cursor.getCount()];
            mAllSubCategoryId = new String[cursor.getCount()];

            for (int i = 0; i < cursor.getCount(); i++) {
                mAllGocCategories[i] = cursor.getString(cursor.getColumnIndex(DatabaseColumns.NAME));
                mAllSubCategoryId[i] = cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID));
                cursor.moveToNext();
            }

            mProgressDialog.dismiss();
            /* Now that I have loaded all categories, mCategoryFrameLayout is ready to show category dialog */
            mCategoryFrameLayout.setOnClickListener(this);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    //*******************************************************************
    // Network Related Functions
    //*******************************************************************


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


    private void fillAddressDetails() {

        final Map<String, String> params = new HashMap<String, String>(6);
        params.put(HttpConstants.LATLNG, SharedPreferenceHelper.getString(R.string.pref_latitude) + "," + SharedPreferenceHelper.getString(R.string.pref_longitude));
        params.put(HttpConstants.KEY, getResources().getString(R.string.google_api_key));
        params.put(HttpConstants.RESULT_TYPE, HttpConstants.STREET_ADDRESS);

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE);
        retroCallbackList.add(retroCallback);

        mGoogleApi.getMyAddress(params, retroCallback);

    }


    private void postToWall(String tagName, String tagIdSelected, String latitude, String longitude, String message, String groupId, String keywords) {

        PostWallMessageRequestModel postWallMessageRequestModel = new PostWallMessageRequestModel();

        postWallMessageRequestModel.wall.setMessage(message);
        if (tagIdSelected != null) {
            postWallMessageRequestModel.wall.setTag_id(tagIdSelected);
        }

        postWallMessageRequestModel.wall.setGroup_id(groupId);
        postWallMessageRequestModel.wall.setLatitude(latitude);
        postWallMessageRequestModel.wall.setLongitude(longitude);

        if (keywords != null && !TextUtils.isEmpty(keywords)) {
            String keywordArray[] = TextUtils.split(keywords, ",");
            List<String> keywordList = new ArrayList<String>();

            for (String keyword : keywordArray) {
                keywordList.add(keyword);
            }
            postWallMessageRequestModel.wall.setKeywords(keywordList);
        }

        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, AppConstants.WALL_DATE_FORMAT);
        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());
        final String sentAt = mFormatter.format(new Date());
        String tempId = "";

        try {
            tempId = mMessageDateFormatter.getEpoch(sentAt) + "";
        } catch (ParseException e) {
            //should not happen
        }
        postWallMessageRequestModel.wall.setTmp_id(tempId);

        if (!TextUtils.isEmpty(mAddressName)) {


            postWallMessageRequestModel.wall.setCity(mStateName.split(",")[0]);
            postWallMessageRequestModel.wall.setCountry(mCountryName);
            postWallMessageRequestModel.wall.setAddress(mAddressName);


            RetroCallback retroCallback;
            retroCallback = new RetroCallback(this);
            if (mIsInEditMode) {
                retroCallback.setRequestId(ApiResponseCodes.UPDATE_WALL);
            } else {
                retroCallback.setRequestId(HttpConstants.ApiResponseCodes.CREATE_WALL);
            }

            Bundle args = new Bundle();
            args.putString(AppConstants.Keys.TEMP_ID, tempId);
            retroCallback.setExtras(args);
            retroCallbackList.add(retroCallback);
            addEditWallLocally(tempId, tagName, tagIdSelected, groupId, latitude, longitude, message, mIsInEditMode);

            if (mIsInEditMode) {
                mYeloApi.updateWallMessage(mWallId, postWallMessageRequestModel, retroCallback);
            } else {
                mYeloApi.postWallMessage(postWallMessageRequestModel, retroCallback);
            }

            getActivity().finish();
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.toast_select_location), Toast.LENGTH_SHORT).show();
        }
    }


    private void postToWallWithImage(String groupId, String tagName, String tagIdSelected, String latitude, String longitude, String message, String imagepath, String keywords) {
        if (!TextUtils.isEmpty(mAddressName)) {
            postOnWallWithImage(getActivity(), tagIdSelected, latitude, longitude, message, imagepath, mGroupIdSelected, keywords, mWallId, mIsInEditMode);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.toast_select_location), Toast.LENGTH_SHORT).show();
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
                                    String message, String imagepath, String groupId, String keywords, String wallId, boolean isEdit) {
        Intent intent = new Intent(context, WallPostIntentService.class);
        intent.setAction(ACTION_POST);

        if (isEdit) {
            intent.putExtra(HttpConstants.UPDATE_POST, true);
        } else {
            intent.putExtra(HttpConstants.UPDATE_POST, false);
        }

        intent.putExtra(HttpConstants.POST_MESSAGE, message);
        intent.putExtra(HttpConstants.POST_LATITUDE, latitude);
        intent.putExtra(HttpConstants.POST_LONGITUDE, longitude);
        intent.putExtra(HttpConstants.POST_TAG_ID, tagIdSelected);
        intent.putExtra(HttpConstants.GROUP_ID, groupId);
        intent.putExtra(HttpConstants.KEYWORDS, keywords);
        intent.putExtra(HttpConstants.WALL_ID, wallId);
        intent.putExtra(HttpConstants.POST_CITY, mCityName.split(",")[0]);
        intent.putExtra(HttpConstants.POST_ADDRESS, mAddressName);
        intent.putExtra(HttpConstants.POST_COUNTRY, mCountryName);
        intent.putExtra(AppConstants.WALL_IMAGE_NAME, imagepath);

        context.startService(intent);
        getActivity().finish();
    }


    /**
     * Method callback when the success response is received
     *
     * @param model     model response received from the server
     * @param requestId The id of the response
     */
    @Override
    public void success(Object model, int requestId) {
        switch (requestId) {

            case HttpConstants.ApiResponseCodes.CREATE_WALL: {
                Toast.makeText(getActivity(), "Wall Posted", Toast.LENGTH_SHORT).show();
                break;
            }

            case ApiResponseCodes.UPDATE_WALL: {
                Toast.makeText(getActivity(), "Wall Updated", Toast.LENGTH_SHORT).show();
                break;
            }

            default:
                break;
        }
    }


    /**
     * Method callback when the request is failed
     *
     * @param requestId The id of the response
     * @param errorCode The errorcode of the response
     * @param message
     */
    @Override
    public void failure(int requestId, int errorCode, String message) {

    }

    //*******************************************************************
    // Dialog related Utility Functions
    //*******************************************************************


    /**
     * Method to handle click on goc
     */


    private void showChooseGocDialog() {
        final int[] materialColors = getResources().getIntArray(R.array.collectionListColors);


        new MaterialDialog.Builder(getActivity()).items(mAllGocNames).itemsCallback(new MaterialDialog.ListCallback() {
                                                                                        @Override
                                                                                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                                                            mActionColor = mGocColors[which];
                                                                                            mGocName.setText(mAllGocNames[which]);
                                                                                            mCategoryName.setText("Subcategory");
                                                                                            mTagIdSelected = null;
                                                                                            colorizeActionBar(Color.parseColor(mActionColor), mToolbar);
                                                                                            colorizeView(Color.parseColor(mActionColor), mGocFrame);
                                                                                            mGroupIdSelected = mAllGocIds[which];
                                                                                            Logger.v(TAG, "%s", "Sub Category Selected =" + mGroupIdSelected);
                                                                                            loadSubCategories(mGroupIdSelected);
                                                                                        }

                                                                                    }

        ).show();

    }


    /**
     * Method to handle click on goc
     */
    private void showChooseCategoryDialog() {

        new MaterialDialog.Builder(getActivity()).items(mAllGocCategories).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                mCategoryName.setText(mAllGocCategories[which]);
                mTagIdSelected = mAllSubCategoryId[which];
                mTagNameSelected = mAllGocCategories[which];
            }

        }).show();
    }


    /**
     * Method to handle click on profile image
     */
    private void showChoosePictureSourceDialog() {

        new MaterialDialog.Builder(getActivity()).items(getResources().getStringArray(R.array.take_photo_choices_wall)).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                if (which == 0) { // Pick from camera
                    final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, InternalFileContentProvider.POST_PIC_URI);

                    try {
                        startActivityForResult(Intent.createChooser(intent, getString(R.string.complete_action_using)), PICK_FROM_CAMERA);
                    } catch (final ActivityNotFoundException e) {
                        e.printStackTrace();
                    }

                } else if (which == 1) { // pick from file
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.complete_action_using)), PICK_FROM_FILE);
                }
            }
        }).show();
    }


    private void showBackPressDialog() {
        new MaterialDialog.Builder(getActivity()).title("Discard changes?").positiveText("YES").negativeText("NO").positiveColor(getResources().getColor(R.color.blue_link)).negativeColor(getResources().getColor(R.color.blue_link)).callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                getActivity().finish();
            }
        }).show();
    }

    //*******************************************************************
    // Image Related Utility Functions
    //*******************************************************************


    private void setFileName() {
        mWallImageFile = new File(Utils.getStorageDirectory(getActivity()), mWallImageFileName);
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
        mCompressedPhoto = PhotoUtils.rotateBitmapIfNeededAndCompressIfTold(getActivity(), uri, source_string, false);
        if (mCompressedPhoto != null) {
            mWallImage.setImageBitmap(mCompressedPhoto);
            mAddImagePlaceholder.setVisibility(View.GONE);
            PhotoUtils.saveImage(PhotoUtils.compressManageAspect(500, 500, mCompressedPhoto), mWallImageFile);
        }
        mWasWallImageUploaded = true;
        mWallImage.setVisibility(View.VISIBLE);
    }


    private Bitmap getBitmap(String tag, Uri url) {
        //Cache Directory will be in app directory from now on.
        File cacheDir;
        cacheDir = new File(Utils.getStorageDirectory(getActivity()), ".OCFL311");

        if (!cacheDir.exists()) cacheDir.mkdirs();

        File f = new File(cacheDir, tag);

        try {
            Bitmap bitmap = null;
            InputStream is = null;
            if (url.toString().startsWith("content://com.google.android.apps") || url.toString().startsWith("content://com.google.android.gallery3d")) {
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


    //*******************************************************************
    // Misc Utility Functions
    //*******************************************************************


    public boolean hasFragmentChanged() {
        return mGroupIdSelected != null || !mMessageEdit.getText().toString().trim().isEmpty() || mWasWallImageUploaded; //|| !mKeywordText.getText().toString().trim().isEmpty();
    }


    private void getLatLongFromAddress(double latitude, double longitude) {

        Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());

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
                mPlaceName.setText(locality);
                Logger.d(TAG, lat + "  " + lng);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //*******************************************************************
    // Functions enforced by parent classes.
    //*******************************************************************


    /**
     * A Tag to add to all async tasks. This must be unique for all Fragments types
     *
     * @return An Object that's the tag for this fragment
     */
    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    //*******************************************************************
    // End of class
    //*******************************************************************

}
