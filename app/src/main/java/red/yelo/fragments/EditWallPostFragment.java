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
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableMyWallPosts;
import red.yelo.data.TableTags;
import red.yelo.data.TableWallPosts;
import red.yelo.fragments.dialogs.SingleChoiceDialogFragment;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.http.WallPostIntentService;
import red.yelo.retromodels.request.PostWallMessageRequestModel;
import red.yelo.retromodels.response.GetCreateWallResponseModel;
import red.yelo.retromodels.response.GoogleGeocodeResponse;
import red.yelo.retromodels.response.Tags;
import red.yelo.retromodels.response.TagsRecommendationResponseModel;
import red.yelo.retromodels.response.TagsSuggestionsResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.Logger;
import red.yelo.utils.PhotoUtils;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.widgets.MultiTagLayout;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.mime.TypedFile;

/**
 * Created by anshul1235 on 15/07/14.
 *
 * Old 'Edit Post'
 * @deprecated
 */
public class EditWallPostFragment extends AbstractYeloFragment implements View.OnClickListener
        , Callback,
        LoaderManager.LoaderCallbacks<Cursor>,
        DBInterface.AsyncDbQueryCallback, AdapterView.OnItemClickListener, TextWatcher, RetroCallback.RetroResponseListener,
        MultiTagLayout.OnTagClickListener {

    public static final String TAG = "EditWallPostFragment";

    private EditText mMessageEdit;

    private TextView mPlaceName, mAddUpdateImage;

    private boolean mEditWall;

    private Tags[] mTags;

    private MultiTagLayout mMultiTagLayout;

    /**
     * cursor to load the categories so as to get ids of each in onclick
     */
    private Cursor mCursor;

    private String mLatitude, mLongitude, mTagIdSelected;

    private String[] mAddress;

    private Button mPostButton;

    /**
     * Reference to the Dialog Fragment for selecting the picture type
     */
    private SingleChoiceDialogFragment mChoosePictureDialogFragment;


    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;


    private String mWallImageFileName = AppConstants.WALL_IMAGE_NAME;

    private Uri mCameraImageCaptureUri;


    private SimpleDateFormat mFormatter;

    private DateFormatter mMessageDateFormatter;

    private Bitmap mCompressedPhoto;

    private ImageView mWallImage;

    private boolean mWasWallImageUploaded;

    private File mWallImageFile;

    private static final String ACTION_POST = "red.yelo.http.action.POST";

    private String mWallId;

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private String mTextTruncate = "";

    private List<String> mTagsIds = new ArrayList<String>();

    private Toolbar mToolbar;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_edit_post_wall, container, false);

        Bundle extras = getArguments();

        if (extras != null) {
            if (extras.containsKey(AppConstants.Keys.EDIT_POST)) {
                mEditWall = extras.getBoolean(AppConstants.Keys.EDIT_POST);
            }
            mWallId = extras.getString(AppConstants.Keys.WALL_ID);
            mTagIdSelected = extras.getString(AppConstants.Keys.TAG_ID);
            mLatitude = SharedPreferenceHelper.getString(R.string.pref_latitude);
            mLongitude = SharedPreferenceHelper.getString(R.string.pref_longitude);

        }

        mBus.register(this);

        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                AppConstants.WALL_DATE_FORMAT);

        mMultiTagLayout = (MultiTagLayout) contentView.findViewById(R.id.tag_container);
        mMultiTagLayout.setOnTagClickListener(this);
        mMultiTagLayout.setShouldSelectOnClick(true);

        Logger.d(TAG, mTagIdSelected + "");
        mMultiTagLayout.setSelectedTags(mTagIdSelected);
        mAddUpdateImage = (TextView) contentView.findViewById(R.id.text_add_image);
        mAddUpdateImage.setOnClickListener(this);

        mPlaceName = (TextView) contentView.findViewById(R.id.text_location);
        mPostButton = (Button) contentView.findViewById(R.id.post_wall_message);
        mMessageEdit = (EditText) contentView.findViewById(R.id.message);
        mWallImage = (ImageView) contentView.findViewById(R.id.wall_image);
        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        setToolbar(mToolbar);


        mPostButton.setOnClickListener(this);
        mPlaceName.setOnClickListener(this);


        if (savedInstanceState != null) {
            mMessageEdit.setHint(savedInstanceState.getString(AppConstants.Keys.MESSAGE));
            mTagIdSelected = savedInstanceState.getString(AppConstants.Keys.TAG_NAME);
            mMultiTagLayout.setTagSelected(mTagIdSelected, true);
            mAddress = savedInstanceState.getStringArray(AppConstants.Keys.ADDRESS);
            mPlaceName.setText(TextUtils.join(",", mAddress));
        }

        setFileName();
        mCameraImageCaptureUri = Uri.fromFile(mWallImageFile);
        fillAddressDetails();
        fetchTagSuggestions();
        loadSuggesstions();

        String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
        DBInterface.queryAsync(AppConstants.QueryTokens.QUERY_WALL_DETAILS, getTaskTag(), null, true, TableMyWallPosts.NAME,
                null, selection, new String[]{mWallId}, null, null, null, null, this);

        return contentView;

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString(AppConstants.Keys.MESSAGE, mMessageEdit.getText().toString());
        outState.putString(AppConstants.Keys.TAG_ID, mTagIdSelected);
        outState.putStringArray(AppConstants.Keys.ADDRESS, mAddress);
        super.onSaveInstanceState(outState);

    }

    private void fetchTagSuggestions() {

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_TAG_SUGGESTIONS);
        retroCallbackList.add(retroCallback);

        mYeloApi.getTagRecommendations(retroCallback);

    }


    private void updateView(Cursor wallDetails) {

        wallDetails.moveToFirst();
//        mSelectTagsSuggesstions.addObject(new MultiTagLayout.Tag(wallDetails.getString(wallDetails.getColumnIndex(DatabaseColumns.TAG_ID)),
//                wallDetails.getString(wallDetails.getColumnIndex(DatabaseColumns.TAG_NAME))));
//        mSelectTagsSuggesstions.setText(wallDetails.getString(wallDetails.getColumnIndex(DatabaseColumns.TAG_NAME)));
        mMessageEdit.setText(wallDetails.getString(wallDetails.getColumnIndex(DatabaseColumns.MESSAGE)));
        mTagIdSelected = wallDetails.getString(wallDetails.getColumnIndex(DatabaseColumns.TAG_ID));

        if (!wallDetails.getString(wallDetails.getColumnIndex(DatabaseColumns.WALL_IMAGES)).equals("")) {
            mWallImage.setVisibility(View.VISIBLE);
            mAddUpdateImage.setText(getResources().getString(R.string.update_image));
            Glide.with(getActivity())
                    .load(wallDetails.getString(wallDetails.getColumnIndex(DatabaseColumns.WALL_IMAGES)))
                    .centerCrop()
                    .placeholder(R.color.snow_light)
                    .into(mWallImage);
        }

        wallDetails.close();
    }

    private void loadSuggesstions() {
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS, null, this);
    }



    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    private void postToWall(String tagIdSelected, String latitude, String longitude, String[] address,
                            String message) {
        PostWallMessageRequestModel postWallMessageRequestModel = new PostWallMessageRequestModel();

        postWallMessageRequestModel.wall.setMessage(message);
        postWallMessageRequestModel.wall.setTag_id(tagIdSelected);
        postWallMessageRequestModel.wall.setLatitude(latitude);
        postWallMessageRequestModel.wall.setLongitude(longitude);

        Logger.d(TAG, latitude, longitude);


        if (mAddress.length > 2) {
            postWallMessageRequestModel.wall.setCity(address[address.length - 3].trim());
            postWallMessageRequestModel.wall.setCountry(address[address.length - 1].trim());

            RetroCallback retroCallback;
            retroCallback = new RetroCallback(this);
            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.UPDATE_WALL);
            retroCallbackList.add(retroCallback);


            mYeloApi.updateWallMessage(mWallId, postWallMessageRequestModel, retroCallback);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_location), Toast.LENGTH_SHORT).show();
        }
    }

    private void postToWallWithImage(String tagIdSelected, String latitude, String longitude, String[] address,
                                     String message, String imagepath) {


//        final Map<String, String> params = new HashMap<String, String>(8);
//        params.put(HttpConstants.POST_MESSAGE, message);
//        params.put(HttpConstants.POST_LATITUDE,latitude);
//        params.put(HttpConstants.POST_LONGITUDE,longitude);
//        params.put(HttpConstants.POST_TAG_ID,tagIdSelected);

        if (mAddress.length > 2) {
            postOnWallWithImage(getActivity(), tagIdSelected, latitude, longitude, address, message, imagepath);

        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_location), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see android.app.IntentService
     */
    public void postOnWallWithImage(Context context, String tagIdSelected, String latitude,
                                    String longitude, String[] address,
                                    String message, String imagepath) {
        Intent intent = new Intent(context, WallPostIntentService.class);

        intent.setAction(ACTION_POST);
        intent.putExtra(HttpConstants.UPDATE_POST, true);
        intent.putExtra(HttpConstants.WALL_ID, mWallId);
        intent.putExtra(HttpConstants.POST_MESSAGE, message);
        intent.putExtra(HttpConstants.POST_LATITUDE, latitude);
        intent.putExtra(HttpConstants.POST_LONGITUDE, longitude);
        intent.putExtra(HttpConstants.POST_TAG_ID, tagIdSelected);
        intent.putExtra(HttpConstants.POST_CITY, address[address.length - 3].trim());
        intent.putExtra(HttpConstants.POST_COUNTRY, address[address.length - 1].trim());
        intent.putExtra(AppConstants.WALL_IMAGE_NAME, imagepath);

        context.startService(intent);

        getActivity().finish();


    }


    @Override
    public void failure(RetrofitError error) {
        getActivity().setProgressBarIndeterminateVisibility(false);


        Toast.makeText(getActivity(), "Broadcast sent failed", Toast.LENGTH_SHORT).show();

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
        } else if (taskId == AppConstants.QueryTokens.UPDATE_WALLPOST) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, getTaskTag(), null
                        , TableWallPosts.NAME, null, values, true, this);


            }
        }

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

        if (taskId == AppConstants.QueryTokens.QUERY_WALL_DETAILS) {
            if (isAttached()) {
                updateView(cursor);
            }
            cursor.close();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {

            return new SQLiteLoader(getActivity(), false, TableTags.NAME, null,
                    null, null, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);
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
            for (int i = 0; i < cursor.getCount(); i++) {

                mTags[i] = new Tags(cursor.getString(
                        cursor.getColumnIndex(DatabaseColumns.NAME)),
                        cursor.getString(
                                cursor.getColumnIndex(DatabaseColumns.ID)), "image_url");
                cursor.moveToNext();
            }

            ArrayList<MultiTagLayout.Tag> tagList = new ArrayList<MultiTagLayout.Tag>(mTags.length);
            //tagList.addAll(Arrays.asList(mTags));
                /*mTagAdapter = new TagsArrayAdapter(getActivity(), R.layout.layout_tag_grid, tagList, true);

                mTagMultiselectList.setAdapter(mTagAdapter);*/

            for (Tags eachTag : mTags) {
                tagList.add(new MultiTagLayout.Tag(eachTag.getId(), eachTag.getName()));
            }

            mMultiTagLayout.setTags(tagList);

            /*ArrayList<Tags> tagList = new ArrayList<Tags>();
            tagList.addAll(Arrays.asList(mTags));
            mTagAdapter = new TagsArrayAdapter(getActivity(), R.layout.layout_tag_grid, tagList, true);

            mTagMultiselectList.setAdapter(mTagAdapter);*/

            if (getActivity() != null) {
                getActivity().setProgressBarIndeterminateVisibility(false);
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
//
//        final Cursor cursor = (Cursor) mTagSuggesstionMultiselectAdapter
//                .getItem(position);
//
//        mTagIdSelected = cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID));
//
//        mSelectTagsSuggesstions.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.NAME)));
//        //mPostLayout.setVisibility(View.VISIBLE);
//        hideKeyboard(mSelectTagsSuggesstions);

    }

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

        }
//        else if (v.getId() == R.id.place_layout) {
//            final Intent selectLocationActivity = new Intent(getActivity(),
//                    SearchLocationActivity.class);
//
//            //selectLocationActivity.putExtra(AppConstants.Keys.PLACE,mPlaceName.getText().toString());
//            selectLocationActivity.putExtra(AppConstants.Keys.FROM_WALL, true);
//
//            startActivityForResult(selectLocationActivity, AppConstants.RequestCodes.GET_PLACE);
//
//        }
        else if (v.getId() == R.id.post_wall_message) {

            if (mWasWallImageUploaded) {
                TypedFile typedFile;
                File photo;
                photo = new File(mWallImageFile.getAbsolutePath());
                typedFile = new TypedFile("application/octet-stream", photo);

                //TODO post to intent service
                postToWallWithImage(mTagIdSelected, mLatitude, mLongitude, mAddress,
                        mMessageEdit.getText().toString(), mWallImageFile.getAbsolutePath());

            } else {
                postToWall(mTagIdSelected, mLatitude, mLongitude, mAddress, mMessageEdit.getText().toString());
            }
        } else if (v.getId() == R.id.text_add_image) {

            showChoosePictureSourceDialog();

        }
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
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageCaptureUri);

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


    private void fillAddressDetails() {

        final Map<String, String> params = new HashMap<String, String>(6);
        params.put(HttpConstants.LATLNG, SharedPreferenceHelper.getString(R.string.pref_latitude)
                + "," + SharedPreferenceHelper.getString(R.string.pref_longitude));
        params.put(HttpConstants.KEY, getResources().getString(R.string.google_api_key));
        params.put(HttpConstants.RESULT_TYPE, HttpConstants.STREET_ADDRESS);

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE);
        retroCallbackList.add(retroCallback);

        mGoogleApi.getMyAddress(params, retroCallback);

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
                        new Crop(mCameraImageCaptureUri).output(mCameraImageCaptureUri).withAspect(600, 300).start(getActivity());

                    break;

                case Crop.REQUEST_CROP:

                    setAndSaveImage(mCameraImageCaptureUri, PICK_FROM_CAMERA);
                    break;

                case CROP_FROM_CAMERA:
                    final Bundle extras = data.getExtras();
                    if (extras != null) {
                        mCompressedPhoto = extras.getParcelable("data");
                        mWallImage.setImageBitmap(mCompressedPhoto);
                        mWallImage.setVisibility(View.VISIBLE);

                    }
                    PhotoUtils.saveImage(PhotoUtils.compressManageAspect(100, 100, mCompressedPhoto), mWallImageFile);
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

                                            mCompressedPhoto = getBitmap(mWallImageFileName, uriurl);
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mWasWallImageUploaded = true;
                                                    mCompressedPhoto = PhotoUtils.compressManageAspect(500, 500, mCompressedPhoto);
                                                    mWallImage.setImageBitmap(mCompressedPhoto);
                                                    mWallImage.setVisibility(View.VISIBLE);
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
                                new Crop(mGalleryImageCaptureUri).output(mCameraImageCaptureUri).withAspect(600, 300).start(getActivity());
                                // doCrop(PICK_FROM_FILE);

                            }
                        }

                        break;
                    }
                case AppConstants.RequestCodes.GET_PLACE: {
                    if (resultCode == Activity.RESULT_OK) {
                        String place = data.getStringExtra(AppConstants.Keys.PLACE);
                        mAddress = place.split(",");
                        mLatitude = SharedPreferenceHelper.getString(R.string.pref_latitude);
                        mLongitude = SharedPreferenceHelper.getString(R.string.pref_longitude);
                        mPlaceName.setText(place);

                        Logger.d(TAG, "location manually saved");
                        if (place.equals(AppConstants.MY_LOCATION)) {
                            if (isAttached())
                                fillAddressDetails();
                            // TODO Update your TextView.
                        }
                        //
                        break;
                    }
                }
            }
        }
    }

    private Bitmap getBitmap(String tag, Uri url) {
        File cacheDir;
        // if the device has an SD card
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


        if (mPhotoUri.toString().contains("http")) {
//            String[] split = mPhotoUri.toString().split("https://");
//            Logger.d(TAG,mPhotoUri.toString());
        } else {
            mCompressedPhoto = PhotoUtils
                    .rotateBitmapIfNeededAndCompressIfTold(getActivity(), uri, source_string, true);


            if (mCompressedPhoto != null) {

                mWallImage.setImageBitmap(mCompressedPhoto);
                mWallImage.setVisibility(View.VISIBLE);
                PhotoUtils.saveImage(PhotoUtils.compressManageAspect(500, 500, mCompressedPhoto), mWallImageFile);

            }
            mWasWallImageUploaded = true;
        }

    }

    private void setFileName() {
        mWallImageFile = new File(Utils.getStorageDirectory(getActivity()),mWallImageFileName);
    }


    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.UPDATE_WALL: {
                getActivity().setProgressBarIndeterminateVisibility(false);

                Toast.makeText(getActivity(), getResources().getString(R.string.query_update_message), Toast.LENGTH_SHORT).show();

                GetCreateWallResponseModel createWallResponseModel = ((GetCreateWallResponseModel) model);

                ContentValues values = new ContentValues(6);
                values.put(DatabaseColumns.ID, createWallResponseModel.wall.id);
                values.put(DatabaseColumns.MESSAGE, createWallResponseModel.wall.message);
                values.put(DatabaseColumns.TAG_NAME, createWallResponseModel.wall.tag_name);
                values.put(DatabaseColumns.TAG_USER_COUNT, createWallResponseModel.wall.tagged_users_count);
                values.put(DatabaseColumns.CHAT_USER_COUNT, createWallResponseModel.wall.chat_users_count);
                values.put(DatabaseColumns.USER_NAME, createWallResponseModel.wall.wall_owner.name);
                values.put(DatabaseColumns.USER_ID, createWallResponseModel.wall.wall_owner.user_id);
                values.put(DatabaseColumns.DATE_TIME, createWallResponseModel.wall.created_at);
                try {
                    values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(createWallResponseModel.wall.created_at));
                    values.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(createWallResponseModel.wall.created_at));

                } catch (ParseException e) {
                    e.printStackTrace();
                    //should not happen 78176
                }
                if (createWallResponseModel.wall.wall_image != null) {
                    values.put(DatabaseColumns.WALL_IMAGES, createWallResponseModel.wall.wall_image.image_url);
                }
                values.put(DatabaseColumns.USER_IMAGE, createWallResponseModel.wall.wall_owner.image_url);

                String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, getTaskTag(), values
                        , TableWallPosts.NAME, values, selection,
                        new String[]{createWallResponseModel.wall.id}, true, this);


                getActivity().finish();
                break;
            }
            case HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE: {
                GoogleGeocodeResponse googleGeocodeResponse = ((GoogleGeocodeResponse) model);
                if (googleGeocodeResponse.results.size() != 0) {
                    mAddress = googleGeocodeResponse.results.get(0).getAddress();


                    mPlaceName.setText(TextUtils.join(",", mAddress));


                }
                break;
            }
            case HttpConstants.ApiResponseCodes.GET_TAG_SUGGESTIONS: {
                TagsRecommendationResponseModel tagsRecommendationResponseModel = ((TagsRecommendationResponseModel) model);


                for (int i = 0; i < tagsRecommendationResponseModel.tags.size(); i++) {

                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.ID, tagsRecommendationResponseModel.tags.get(i).id);
                    values.put(DatabaseColumns.NAME, tagsRecommendationResponseModel.tags.get(i).name);


                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    Logger.d(TAG, "UPDATE");

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
                            TableTags.NAME, values, selection, new String[]{tagsRecommendationResponseModel.tags.get(i).id}, true, this);


                }

                break;
            }

            case HttpConstants.ApiResponseCodes.GET_TAG_AUTO_SUGGESTIONS: {
                getActivity().setProgressBarIndeterminateVisibility(false);


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
                *//*mTagAdapter = new TagsArrayAdapter(getActivity(), R.layout.layout_tag_grid, tagList, true);

                mTagMultiselectList.setAdapter(mTagAdapter);*//*

                for(Tags eachTag : mTags) {
                    tagList.add(new MultiTagLayout.Tag(eachTag.getId(), eachTag.getName()));
                }*/

                break;
            }
            default:
                break;
        }
    }

    @Override
    public void failure(int requestId, int errorCode, String message) {

    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
    }

    @Override
    public void onTagClicked(View view, MultiTagLayout.Tag tag) {

    }


}
