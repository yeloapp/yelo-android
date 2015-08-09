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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.soundcloud.android.crop.Crop;
import com.vinaysshenoy.okulus.OkulusImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.activities.EditProfileActivity;
import red.yelo.activities.RegistrationProcessActivity;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.analytics.ProfileMethod;
import red.yelo.bus.PageNextPrevious;
import red.yelo.data.DBInterface;
import red.yelo.fragments.dialogs.SingleChoiceDialogFragment;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.request.UserDetailsWithoutImageRequestModel;
import red.yelo.retromodels.response.CreateUserResponseModel;
import red.yelo.retromodels.response.FacebookProfileResponseModel;
import red.yelo.retromodels.response.GoogleProfileResponse;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.InternalFileContentProvider;
import red.yelo.utils.Logger;
import red.yelo.utils.PhotoUtils;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import retrofit.mime.TypedFile;

/**
 * Created by anshul1235 on 18/07/14.
 */

public class EditProfileFragment extends AbstractYeloFragment implements View.OnClickListener,
        Session.StatusCallback, RetroCallback.RetroResponseListener, DBInterface.AsyncDbQueryCallback {

    public static final String TAG = "EditProfileFragment";


    private OkulusImageView mProfilePic;

    private EditText mFirstName, mDesciption;

    private Bitmap mCompressedPhoto;

    private File mAvatarfile;

    private Uri mCameraImageCaptureUri;

    private boolean mWasProfileImageUploaded = false, mUpdate;

    private String mProfileImageUrl;

    private String mAvatarFileName = AppConstants.AVATOR_PROFILE_NAME;

    private String mId;

    private ProgressBar mProgressBar;

    private ImageView mGalleryIcon;


    private static final int PICK_FROM_CAMERA = 6;
    private static final int CROP_FROM_CAMERA = 7;
    private static final int PICK_FROM_FILE = 9;
    private static final int REQUEST_CROP_PICTURE = 8;

    private boolean isFromLogin;

    private static final int PIC_CROP = 10;

    private TextView mAddImageText;


    /**
     * Reference to the Dialog Fragment for selecting the picture type
     */
    private SingleChoiceDialogFragment mChoosePictureDialogFragment;

    private Button mNextButton;

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private Toolbar mToolbar;

    private View mFragmentView;

    @SuppressLint({"NewApi"})
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        final View contentView = inflater
                .inflate(R.layout.fragment_profile_edit, container, false);


        mFragmentView = contentView;
        if (getArguments() != null) {
            if (getArguments().containsKey(AppConstants.Keys.AFTER_LOGIN)) {
                mUpdate = true;
            }
        }

        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);


        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session
                        .restoreSession(getActivity(), null, this, savedInstanceState);
            }
            if (session == null) {
                session = new Session(getActivity());
            }

            Session.setActiveSession(session);

        }

        // I don't think this is needed as this function is called below as well
//        setFileName();
        mProfilePic = (OkulusImageView) contentView.findViewById(R.id.image_profile_pic);

        mProfilePic.setOnClickListener(this);

        mNextButton = (Button) contentView.findViewById(R.id.next_button);
        mFirstName = (EditText) contentView.findViewById(R.id.text_first_name);
        mDesciption = (EditText) contentView.findViewById(R.id.text_about_me);
        mProgressBar = (ProgressBar) contentView.findViewById(R.id.progressBar);
        mGalleryIcon = (ImageView) contentView.findViewById(R.id.gallery_ic);
        mAddImageText = (TextView) contentView.findViewById(R.id.add_image_text);

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);

        setToolbar(mToolbar);

        if (!mUpdate) {
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    nextScreen();
                    return true;
                }
            });
        }


        mNextButton.setOnClickListener(this);

        contentView.findViewById(R.id.btn_google_login).setOnClickListener(this);
        contentView.findViewById(R.id.btn_facebook_login).setOnClickListener(this);

        mFirstName.setText(AppConstants.UserInfo.INSTANCE.getFirstName());
        if (AppConstants.UserInfo.INSTANCE.getDescription().equals("")) {
            // mDesciption.setText(getResources().getString(R.string.default_description));

        } else {
            mDesciption.setText(AppConstants.UserInfo.INSTANCE.getDescription());
        }
        if (savedInstanceState == null) {
            if (!TextUtils.isEmpty(AppConstants.UserInfo.INSTANCE.getProfilePicture())) {
                mGalleryIcon.setVisibility(View.GONE);
                mAddImageText.setVisibility(View.GONE);
                Utils.loadCircularImageForEditProfile(getActivity(), mProfilePic, AppConstants.UserInfo.INSTANCE.getProfilePicture(), AvatarBitmapTransformation.AvatarSize.EDIT_PROFILE, contentView);
            } else {
                // Utils.loadCircularImage(getActivity(), mProfilePic, AppConstants.UserInfo.INSTANCE.getProfilePicture(), AvatarBitmapTransformation.AvatarSize.EDIT_PROFILE);
            }
        }
        setFileName();
        mCameraImageCaptureUri = Uri.fromFile(mAvatarfile);

        if (!mUpdate) {
            SharedPreferenceHelper.set(R.string.pref_registration_screen, AppConstants.EDIT_PROFILE_SCREEN);
        }

        return contentView;

    }

    private void setFileName() {
        mAvatarfile = new File(Utils.getStorageDirectory(getActivity()), AppConstants.AVATOR_PROFILE_NAME);
    }


    public static EditProfileFragment newInstance() {
        EditProfileFragment f = new EditProfileFragment();
        return f;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(AppConstants.Keys.MY_ID, mId);
        final Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile_edit, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.action_profile_save: {

                if (mUpdate) {
                    if (!TextUtils.isEmpty(mFirstName.getText().toString())) {

                        //TODO Check for the location
                        UserDetailsWithoutImageRequestModel userDetailsWithoutImageModel = new UserDetailsWithoutImageRequestModel();
                        userDetailsWithoutImageModel.user.setName(mFirstName.getText().toString());

                        if (mDesciption.getText().toString().equals("")) {

                            userDetailsWithoutImageModel.user.setDescription(getActivity().getResources().getString(R.string.default_description));

                        } else {
                            userDetailsWithoutImageModel.user.setDescription(mDesciption.getText().toString());
                        }
                        userDetailsWithoutImageModel.user.setExt_img_url(mProfileImageUrl);
                        userDetailsWithoutImageModel.user.setUtc_offset(getUtcOffset() + "");
                        userDetailsWithoutImageModel.user.setPlatform_version(SharedPreferenceHelper
                                .getInt(R.string.pref_last_version_code) + "");

//                    userDetailsWithoutImageModel.user.setCity();
//                    userDetailsWithoutImageModel.user.setCountry();


                        if (mWasProfileImageUploaded) {
                            TypedFile typedFile;
                            File photo;
                            photo = new File(mAvatarfile.getAbsolutePath());
                            typedFile = new TypedFile(AppConstants.MIME_TYPE, photo);

                            final Map<String, String> params = new HashMap<String, String>(2);
                            params.put(HttpConstants.FIRST_NAME, mFirstName.getText().toString());
                            params.put(HttpConstants.DESCRIPTION, mDesciption.getText().toString());
                            params.put(HttpConstants.UTC_OFFSET, getUtcOffset() + "");

                            mProgressBar.setVisibility(View.VISIBLE);

                            RetroCallback retroCallback;
                            retroCallback = new RetroCallback(this);
                            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.UPDATE_PROFILE);
                            Bundle args = new Bundle();
                            args.putBoolean(AppConstants.Keys.UPDATE, mUpdate);
                            retroCallback.setExtras(args);
                            retroCallbackList.add(retroCallback);

                            mYeloApi.updateUserMultipart(AppConstants.UserInfo.INSTANCE.getId(), typedFile, params, retroCallback);


                        } else {
                            mProgressBar.setVisibility(View.VISIBLE);

                            RetroCallback retroCallback;
                            retroCallback = new RetroCallback(this);
                            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.UPDATE_PROFILE);
                            retroCallbackList.add(retroCallback);
                            Bundle args = new Bundle();
                            args.putBoolean(AppConstants.Keys.UPDATE, mUpdate);
                            retroCallback.setExtras(args);

                            mYeloApi.updateUserNoImage(AppConstants.UserInfo.INSTANCE.getId(), userDetailsWithoutImageModel, retroCallback);

                        }

                        if (mUpdate) {
                            getActivity().finish();
                        }

                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.profile_validation_message),
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    nextScreen();
                }
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }

        }
    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {


            case R.id.image_profile_pic: {
                showChoosePictureSourceDialog();
                break;
            }

            case R.id.btn_facebook_login: {

                fetchProfileInfoFromFacebook();
                break;
            }


            case R.id.btn_google_login: {

                fetchProfileInfoFromGoogle();
                break;
            }

            case R.id.next_button: {
                if (!TextUtils.isEmpty(mFirstName.getText().toString())) {

                    //TODO Check for the location
                    UserDetailsWithoutImageRequestModel userDetailsWithoutImageModel = new UserDetailsWithoutImageRequestModel();
                    userDetailsWithoutImageModel.user.setName(mFirstName.getText().toString());

                    if (mDesciption.getText().toString().equals("")) {

                        userDetailsWithoutImageModel.user.setDescription(getActivity().getResources().getString(R.string.default_description));

                    } else {
                        userDetailsWithoutImageModel.user.setDescription(mDesciption.getText().toString());
                    }
                    userDetailsWithoutImageModel.user.setExt_img_url(mProfileImageUrl);
                    userDetailsWithoutImageModel.user.setUtc_offset(getUtcOffset() + "");
                    userDetailsWithoutImageModel.user.setPlatform_version(SharedPreferenceHelper
                            .getInt(R.string.pref_last_version_code) + "");

//                    userDetailsWithoutImageModel.user.setCity();
//                    userDetailsWithoutImageModel.user.setCountry();


                    if (mWasProfileImageUploaded) {
                        TypedFile typedFile;
                        File photo;
                        photo = new File(mAvatarfile.getAbsolutePath());
                        typedFile = new TypedFile(AppConstants.MIME_TYPE, photo);

                        final Map<String, String> params = new HashMap<String, String>(2);
                        params.put(HttpConstants.FIRST_NAME, mFirstName.getText().toString());
                        params.put(HttpConstants.DESCRIPTION, mDesciption.getText().toString());
                        params.put(HttpConstants.UTC_OFFSET, getUtcOffset() + "");

                        mProgressBar.setVisibility(View.VISIBLE);

                        RetroCallback retroCallback;
                        retroCallback = new RetroCallback(this);
                        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.UPDATE_PROFILE);
                        Bundle args = new Bundle();
                        args.putBoolean(AppConstants.Keys.UPDATE, mUpdate);
                        retroCallback.setExtras(args);
                        retroCallbackList.add(retroCallback);

                        mYeloApi.updateUserMultipart(AppConstants.UserInfo.INSTANCE.getId(), typedFile, params, retroCallback);


                    } else {
                        mProgressBar.setVisibility(View.VISIBLE);

                        RetroCallback retroCallback;
                        retroCallback = new RetroCallback(this);
                        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.UPDATE_PROFILE);
                        retroCallbackList.add(retroCallback);
                        Bundle args = new Bundle();
                        args.putBoolean(AppConstants.Keys.UPDATE, mUpdate);
                        retroCallback.setExtras(args);

                        mYeloApi.updateUserNoImage(AppConstants.UserInfo.INSTANCE.getId(), userDetailsWithoutImageModel, retroCallback);

                    }

                    if (mUpdate) {
                        getActivity().finish();
                    }

                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.profile_validation_message),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            }


        }
    }

    /**
     * Fetch profile information from Facebook
     */
    private void fetchProfileInfoFromFacebook() {

        mWasProfileImageUploaded = false;
        final Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this)
                    .setPermissions(Arrays
                            .asList(AppConstants
                                    .FBPERMISSIONS))
                    .setCallback(this));
        } else {
            Session.openActiveSession(getActivity(), this, true, this);

        }
    }

    /**
     * Fetch profile info from Google
     */
    private void fetchProfileInfoFromGoogle() {

        mWasProfileImageUploaded = false;

        if (mUpdate) {
            ((EditProfileActivity) getActivity()).getPlusManager().login();

        } else {
            ((RegistrationProcessActivity) getActivity()).getPlusManager().login();
        }
    }

    /**
     * Method to handle click on profile image
     */
    private void showChoosePictureSourceDialog() {


        new MaterialDialog.Builder(getActivity())
                .items(getResources().getStringArray(R.array.take_photo_choices))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0) { // Pick from camera
                            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, InternalFileContentProvider.PROFILE_PIC_URI);

                            try {

                                startActivityForResult(
                                        Intent.createChooser(intent, getString(R.string.complete_action_using)),
                                        PICK_FROM_CAMERA);
                            } catch (final ActivityNotFoundException e) {
                                e.printStackTrace();
                            }

                        } else if (which == 1) { // pick from file
                            // final Intent intent = new Intent();
                            // final Intent intent = new Intent();
                            Crop.pickImage(getActivity());
//                Intent intent =Utils.getPickImageIntent(getActivity());
//                startActivityForResult(
//                        Intent.createChooser(intent, getString(R.string.complete_action_using)),
//                        PICK_FROM_FILE);
                        } else if (which == 2) { // pick from facebook
                            fetchProfileInfoFromFacebook();
                        } else if (which == 3) { // pick from google
                            fetchProfileInfoFromGoogle();
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

            if (resultCode != Activity.RESULT_OK) {
                return;
            }

            switch (requestCode) {
                case PICK_FROM_CAMERA:
                    // doCrop(PICK_FROM_CAMERA);

                    getActivity().getContentResolver().notifyChange(mCameraImageCaptureUri, null);

                    new Crop(mCameraImageCaptureUri).output(mCameraImageCaptureUri).asSquare().start(getActivity());
                    //performCrop(mCameraImageCaptureUri);
                    //setAndSaveImage(mCameraImageCaptureUri, PICK_FROM_CAMERA);


//                CropImageIntentBuilder cropImage = new CropImageIntentBuilder(200, 200, mCameraImageCaptureUri);
//                cropImage.setSourceImage(data.getData());
//
//                startActivityForResult(cropImage.getIntent(getActivity()), REQUEST_CROP_PICTURE);

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
                case Crop.REQUEST_PICK:
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

                                        mCompressedPhoto = PhotoUtils.getBitmap(getActivity(), mAvatarFileName, uriurl);
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mWasProfileImageUploaded = true;
                                                mCompressedPhoto = PhotoUtils.compressManageAspect(100, 100, mCompressedPhoto);
                                                mGalleryIcon.setVisibility(View.GONE);
                                                mAddImageText.setVisibility(View.GONE);
                                                PhotoUtils.saveImage(PhotoUtils.compressManageAspect(100, 100, mCompressedPhoto), mAvatarfile);
                                                mProfilePic.setImageBitmap(mCompressedPhoto);

                                            }
                                        });
                                    }
                                }).start();


                            }
                        } else { // it is a regular local image file
                            Uri mGalleryImageCaptureUri = data.getData();
//                        CropImageIntentBuilder cropImageGallery = new CropImageIntentBuilder(200, 200, mGalleryImageCaptureUri);
//                        cropImageGallery.setSourceImage(data.getData());
//                        startActivityForResult(cropImageGallery.getIntent(getActivity()), REQUEST_CROP_PICTURE);
                            new Crop(mGalleryImageCaptureUri).output(mCameraImageCaptureUri).asSquare().start(getActivity());
                            //setAndSaveImage(mGalleryImageCaptureUri, PICK_FROM_FILE);
                            // doCrop(PICK_FROM_FILE);

                        }
                    }


                    break;

            }
        }
        Session.getActiveSession()
                .onActivityResult(getActivity(), requestCode, resultCode, data);

    }


    @Override
    public void onResume() {
        super.onResume();
        Session session = Session.getActiveSession();
        session.addCallback(this);
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

        mCompressedPhoto = PhotoUtils
                .rotateBitmapIfNeededAndCompressIfTold(getActivity(), uri, source_string, true);


        if (mCompressedPhoto != null) {
            mProfilePic.setImageBitmap(null);
            mProfilePic.setImageBitmap(mCompressedPhoto);
            mGalleryIcon.setVisibility(View.GONE);
            mAddImageText.setVisibility(View.GONE);
            PhotoUtils.saveImage(PhotoUtils.compressManageAspect(200, 200, mCompressedPhoto), mAvatarfile);


        }
        mWasProfileImageUploaded = true;
    }


    @Override
    public void call(final Session session, SessionState state, Exception exception) {


        if (state.isOpened()) {
            final Map<String, String> params = new HashMap<String, String>(3);
            params.put(HttpConstants.ACCESS_TOKEN, session.getAccessToken());
            //TODO enable field when the permission is accepted
            params.put(HttpConstants.FIELDS, HttpConstants.FaceBookConstants.ID + "," +
                    HttpConstants.FaceBookConstants.NAME + "," +
                    HttpConstants.FaceBookConstants.BIO + "," +
                    HttpConstants.FaceBookConstants.ABOUT);

            RetroCallback retroCallback;
            retroCallback = new RetroCallback(this);
            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_FACEBOOK_PROFILE);
            retroCallbackList.add(retroCallback);
            mProgressBar.setVisibility(View.VISIBLE);
            mFacebookApi.getFacebookProfile(params, retroCallback);
        }

    }

    /**
     * Method called when google login completes
     */
    public void onGoogleLogin() {
        String googleAccessToken;
        if (!mUpdate) {
            googleAccessToken = ((RegistrationProcessActivity) getActivity())
                    .getPlusManager().getAccessToken();
        } else {
            googleAccessToken = ((EditProfileActivity) getActivity())
                    .getPlusManager().getAccessToken();
        }


        fetchProfileDetails(googleAccessToken);


        if (!TextUtils.isEmpty(googleAccessToken)) {
        }
    }

    /**
     * Method called when there is an error while google login
     *
     * @param error The {@link Exception} that occured
     */
    public void onGoogleLoginError(final Exception error) {

        Logger.e(TAG, "GOOGLE LOGIN ERROR");
    }

    /**
     * Method called when google logout happens
     */
    public void onGoogleLogout() {

    }


    private void fetchProfileDetails(String access_token) {

        final Map<String, String> params = new HashMap<String, String>(6);

        params.put(HttpConstants.ALT, "json");
        params.put(HttpConstants.ACCESS_TOKEN, access_token);

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_GOOGLE_PROFILE);
        retroCallbackList.add(retroCallback);

        mGoogleUserApi.getGoogleProfile(params, retroCallback);

        mProgressBar.setVisibility(View.VISIBLE);

    }

    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.UPDATE_PROFILE: {
                CreateUserResponseModel userResponseModel = ((CreateUserResponseModel) model);
                AppConstants.UserInfo.INSTANCE.setFirstName(userResponseModel.user.name);
                MixpanelAnalytics.getInstance().nameUser(AppConstants.UserInfo.INSTANCE.getFirstName());
                AppConstants.UserInfo.INSTANCE.setProfilePicture(userResponseModel.user.image_url);
                AppConstants.UserInfo.INSTANCE.setDescription(userResponseModel.user.description);


                SharedPreferenceHelper.set(R.string.pref_first_name, userResponseModel.user.name);
                SharedPreferenceHelper.set(R.string.pref_profile_image, userResponseModel.user.image_url);
                SharedPreferenceHelper.set(R.string.pref_user_id, userResponseModel.user.id);
                SharedPreferenceHelper.set(R.string.pref_description, userResponseModel.user.description);


                userRefresh(true);

                if (mFirstName.isActivated()) {
                    hideKeyboard(mFirstName);
                } else {
                    hideKeyboard(mDesciption);
                }

                mProgressBar.setVisibility(View.INVISIBLE);

                if (mUpdate) {
                    getActivity().finish();
                } else {
                    mBus.post(new PageNextPrevious(true, AppConstants.CREATE_AVATAR));

                }

                break;
            }
            case HttpConstants.ApiResponseCodes.GET_FACEBOOK_PROFILE: {
                MixpanelAnalytics.getInstance().onProfileInfoAdded(ProfileMethod.FACEBOOK);
                FacebookProfileResponseModel facebookProfileResponseModel = ((FacebookProfileResponseModel) model);
                mProgressBar.setVisibility(View.INVISIBLE);

                String name = facebookProfileResponseModel.name;
                String capNames = name.substring(0, 1).toUpperCase() + name.substring(1);
                mFirstName.setText(capNames);

                mProfileImageUrl = "https://graph.facebook.com/" + facebookProfileResponseModel.id + "/picture?type=large";

                if (mDesciption.getText().toString().equals("")) {
                    mDesciption.setText(facebookProfileResponseModel.about);
                }

                if (getActivity() != null) {
                    Utils.loadCircularImageForEditProfile(getActivity(), mProfilePic, mProfileImageUrl, AvatarBitmapTransformation.AvatarSize.EDIT_PROFILE, mFragmentView);
                }
                break;
            }
            case HttpConstants.ApiResponseCodes.GET_GOOGLE_PROFILE: {
                MixpanelAnalytics.getInstance().onProfileInfoAdded(ProfileMethod.GOOGLE);
                GoogleProfileResponse googleProfileResponse = ((GoogleProfileResponse) model);
                mProgressBar.setVisibility(View.INVISIBLE);

                String name = googleProfileResponse.name;
                String capNames = name.substring(0, 1).toUpperCase() + name.substring(1);
                mFirstName.setText(capNames);

                Utils.loadCircularImageForEditProfile(getActivity(), mProfilePic, googleProfileResponse.picture, AvatarBitmapTransformation.AvatarSize.EDIT_PROFILE, mFragmentView);
                mGalleryIcon.setVisibility(View.GONE);
                mAddImageText.setVisibility(View.GONE);
                mProfileImageUrl = googleProfileResponse.picture;
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void failure(int requestId, int errorCode, String message) {

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }

//    private void performCrop(Uri picUri) {
//        try {
//            CropImageIntentBuilder cropImage = new CropImageIntentBuilder(200, 200, Uri.fromFile(mAvatarfile));
//            cropImage.setOutputQuality(100);
//            cropImage.setSourceImage(picUri);
//            startActivityForResult(cropImage.getIntent(getActivity()), PIC_CROP);
//        }
//        // respond to users whose devices do not support the crop action
//        catch (ActivityNotFoundException anfe) {
//            // display an error message
//            String errorMessage = "Whoops - your device doesn't support the crop action!";
//            Toast toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT);
//            toast.show();
//        }
//    }

    private void nextScreen() {
        if (!TextUtils.isEmpty(mFirstName.getText().toString())) {

            //TODO Check for the location
            UserDetailsWithoutImageRequestModel userDetailsWithoutImageModel = new UserDetailsWithoutImageRequestModel();
            userDetailsWithoutImageModel.user.setName(mFirstName.getText().toString());

            if (mDesciption.getText().toString().equals("")) {

                userDetailsWithoutImageModel.user.setDescription(getActivity().getResources().getString(R.string.default_description));

            } else {
                userDetailsWithoutImageModel.user.setDescription(mDesciption.getText().toString());
            }
            userDetailsWithoutImageModel.user.setExt_img_url(mProfileImageUrl);
            userDetailsWithoutImageModel.user.setUtc_offset(getUtcOffset() + "");
            userDetailsWithoutImageModel.user.setPlatform_version(SharedPreferenceHelper
                    .getInt(R.string.pref_last_version_code) + "");

//                    userDetailsWithoutImageModel.user.setCity();
//                    userDetailsWithoutImageModel.user.setCountry();


            if (mWasProfileImageUploaded) {
                TypedFile typedFile;
                File photo;
                photo = new File(mAvatarfile.getAbsolutePath());
                typedFile = new TypedFile(AppConstants.MIME_TYPE, photo);

                final Map<String, String> params = new HashMap<String, String>(2);
                params.put(HttpConstants.FIRST_NAME, mFirstName.getText().toString());
                params.put(HttpConstants.DESCRIPTION, mDesciption.getText().toString());
                params.put(HttpConstants.UTC_OFFSET, getUtcOffset() + "");

                mProgressBar.setVisibility(View.VISIBLE);

                RetroCallback retroCallback;
                retroCallback = new RetroCallback(this);
                retroCallback.setRequestId(HttpConstants.ApiResponseCodes.UPDATE_PROFILE);
                Bundle args = new Bundle();
                args.putBoolean(AppConstants.Keys.UPDATE, mUpdate);
                retroCallback.setExtras(args);
                retroCallbackList.add(retroCallback);

                mYeloApi.updateUserMultipart(AppConstants.UserInfo.INSTANCE.getId(), typedFile, params, retroCallback);


            } else {
                mProgressBar.setVisibility(View.VISIBLE);

                RetroCallback retroCallback;
                retroCallback = new RetroCallback(this);
                retroCallback.setRequestId(HttpConstants.ApiResponseCodes.UPDATE_PROFILE);
                retroCallbackList.add(retroCallback);
                Bundle args = new Bundle();
                args.putBoolean(AppConstants.Keys.UPDATE, mUpdate);
                retroCallback.setExtras(args);

                mYeloApi.updateUserNoImage(AppConstants.UserInfo.INSTANCE.getId(), userDetailsWithoutImageModel, retroCallback);

            }

            if (mUpdate) {
                getActivity().finish();
            }

        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.profile_validation_message),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
