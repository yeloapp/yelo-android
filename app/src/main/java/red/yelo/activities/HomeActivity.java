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

package red.yelo.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;
import com.squareup.otto.Subscribe;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.bus.GetLocation;
import red.yelo.bus.ServiceCardConfirmationDialog;
import red.yelo.chat.ChatService;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.TableTags;
import red.yelo.data.ViewGroupColorsWithCards;
import red.yelo.fragments.AbstractYeloFragment;
import red.yelo.fragments.ChatDetailsFragment;
import red.yelo.fragments.ChatsFragment;
import red.yelo.fragments.EditProfileFragment;
import red.yelo.fragments.HomeScreenFragment;
import red.yelo.fragments.LoginFragment;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.request.UserDetailsWithoutImageRequestModel;
import red.yelo.retromodels.response.CreateUserResponseModel;
import red.yelo.retromodels.response.GetCollectionResponseModel;
import red.yelo.retromodels.response.NetworkStatusResponse;
import red.yelo.retromodels.response.ServerStatus;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.FragmentTags;
import red.yelo.utils.AppConstants.Keys;
import red.yelo.utils.GooglePlayClientWrapper;
import red.yelo.utils.GooglePlusManager;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeActivity extends AbstractYeloActivity implements
        LocationListener, Callback, View.OnClickListener, GooglePlusManager
        .GooglePlusAuthCallback,
        RetroCallback.RetroResponseListener,ServiceConnection {

    public static final String TAG = "HomeActivity";

    /**
     * Helper for connecting to Google Play Services
     */
    private GooglePlayClientWrapper mGooglePlayClientWrapper;

    private LinearLayout mLayoutServerMessage;

    private Button mUpdateApp;

    /**
     * Helper class for connecting to GooglePlus for login
     */
    private GooglePlusManager mGooglePlusManager;

    private ProgressBar mProgressBar;

    private TextView mLocationText;

    private Button mSelectLocation;

    private ChatService mChatService;

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();


    private boolean mBoundToChatService;


    private static final int DEFAULT_FONT_SIZE = 22;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);



        mGooglePlayClientWrapper = new GooglePlayClientWrapper(this, this);

        mGooglePlusManager = new GooglePlusManager(this, this);


        mLayoutServerMessage = (LinearLayout) findViewById(R.id.layout_server_message);
        mUpdateApp = (Button) findViewById(R.id.update_button);

        mLocationText = (TextView) findViewById(R.id.getting_location_text);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_sync);
        mSelectLocation = (Button) findViewById(R.id.select_location);


        //mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mUpdateApp.setOnClickListener(this);

//        if(SharedPreferenceHelper.getString(R.string.pref_latitude).equals("")){
//            mLocationText.setVisibility(View.VISIBLE);
//            mProgressBar.setVisibility(View.VISIBLE);
//            mSelectLocation.setVisibility(View.VISIBLE);
//        }
//        else {
//            loadHomeScreen();
//        }

        mSelectLocation.setOnClickListener(this);

        //check if registration is not done than open the registration process activity
        if (SharedPreferenceHelper.getInt(R.string.pref_registration_screen) !=
                AppConstants.REGISTRATION_DONE) {
            finish();
            final Intent registrationProcessActivity = new Intent(this,
                    RegistrationProcessActivity.class);
            startActivity(registrationProcessActivity);
            overridePendingTransition(0, 0);
        } else {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            if (savedInstanceState == null) {
                loadHomeScreen();
            }


            RetroCallback retroCallback;
            retroCallback = new RetroCallback(this);
            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SERVER_STATUS);

            mYeloApi.getServerStatus(retroCallback);


            //this will update the gcm id if not pushed to the server
            updateGCMPushIdIfNotPushed();


            updateVersionCode();
            if (!AppConstants.UserInfo.INSTANCE.getAuthToken().equals("")) {
                YeloApplication.startChatService();
            }
        }
        Utils.setNetworkAvailableWithPing();
        fetchGocs();


        if(TextUtils.isEmpty(AppConstants.UserInfo.INSTANCE.getEmail())){
            updateEmailAddress(Utils.getEmail(this));
        }

    }


    private void updateEmailAddress(String email){

        UserDetailsWithoutImageRequestModel userDetailsWithoutImageRequestModel = new UserDetailsWithoutImageRequestModel();
        userDetailsWithoutImageRequestModel.user.setEmail(email);

        mYeloApi.updateUserNoImage(AppConstants.UserInfo.INSTANCE.getId(), userDetailsWithoutImageRequestModel, new Callback<CreateUserResponseModel>() {
            @Override
            public void success(CreateUserResponseModel createUserResponseModel, Response response) {
                SharedPreferenceHelper.set(R.string.pref_email,createUserResponseModel.user.email);
                AppConstants.UserInfo.INSTANCE.setEmail(createUserResponseModel.user.email);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

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

                DBInterface.notifyChange(ViewGroupColorsWithCards.NAME);

            }
        }
    }

    private void fetchGocs() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_GROUPS);
        retroCallbackList.add(retroCallback);

        mYeloApi.getGocs(retroCallback);
    }


    private void updateGCMPushIdIfNotPushed() {

        Logger.d(TAG, SharedPreferenceHelper.getString(R.string.pref_registration_id));
        if (!SharedPreferenceHelper.getBoolean(R.string.pref_push_id_uploaded, false) && !SharedPreferenceHelper.getString(R.string.pref_registration_id).isEmpty()) {
            UserDetailsWithoutImageRequestModel userDetailsWithoutImageRequestModel = new UserDetailsWithoutImageRequestModel();
            userDetailsWithoutImageRequestModel.user.setPush_id(SharedPreferenceHelper.getString(R.string.pref_registration_id));

            RetroCallback retroCallbackGCM;
            retroCallbackGCM = new RetroCallback(this);
            retroCallbackGCM.setRequestId(HttpConstants.ApiResponseCodes.UPDATE_GCM_ID);

            mYeloApi.updateUserNoImage
                    (AppConstants.UserInfo.INSTANCE.getId(), userDetailsWithoutImageRequestModel, retroCallbackGCM);

        }

    }

    private void updateVersionCode() {

        UserDetailsWithoutImageRequestModel userDetailsWithoutImageRequestModel = new UserDetailsWithoutImageRequestModel();

        userDetailsWithoutImageRequestModel.user.setPlatform_version(SharedPreferenceHelper
                .getInt(R.string.pref_last_version_code) + "");

        mYeloApi.updateUserNoImage(AppConstants.UserInfo.INSTANCE.getId(), userDetailsWithoutImageRequestModel, this);
    }


    /**
     * Sends info about the first launch to Analytics
     */
    private void onFirstLaunch() {

        Utils.registerReferralValuesInAnalytics();
        MixpanelAnalytics.getInstance().onFirstLaunch();
        SharedPreferenceHelper.set(R.string.pref_first_launch, false);

    }


    private void loadAppropriateScreen() {

        if (!isVerified()) {
            //jump to activate screen
            loadLoginFragment();
        } else if (isVerified() && !isLoggedIn()) {
            //jump to Edit Profile Screen
            loadEditProfileFragment();

        } else if (isLoggedIn()) {
            loadHomeScreen();
        } else {
            //jump to activate screen
            loadLoginFragment();
        }

    }

    /**
     * Loads the {@link ChatsFragment} into the fragment container
     */
    private void loadChatsFragment() {

        final Intent chatsActivity = new Intent(this,
                ChatsActivity.class);

        startActivity(chatsActivity);


    }

    /**
     * Load the fragment for editing the profile
     */
    private void loadEditProfileFragment() {

        Bundle args = new Bundle(1);

        args.putString(AppConstants.Keys.ID, AppConstants.UserInfo.INSTANCE.getId());
        args.putBoolean(AppConstants.Keys.FROM_LOGIN, true);

        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, EditProfileFragment.class.getName(), args),
                AppConstants.FragmentTags.EDIT_PROFILE, false, null
        );
    }

    /**
     * Load the fragment for login
     */
    private void loadLoginFragment() {

        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, LoginFragment.class.getName(), getIntent().getExtras()),
                AppConstants.FragmentTags.LOGIN, false, null
        );
    }

    /**
     * Loads the {@link ChatDetailsFragment} into the fragment container
     *
     * @param chatId The chat detail to load
     * @param userId The user Id of the user with which the current user is chatting
     */
    private void loadChatDetailFragment(final String chatId, final String userId, final String myId,
                                        final String listId, final String tagId, final String listUserId,
                                        final String queryUserId, final String profileImage) {

        if (TextUtils.isEmpty(chatId) || TextUtils.isEmpty(userId)) {
            finish();
        }


        final Bundle args = new Bundle(2);
        args.putString(Keys.CHAT_ID, chatId);
        args.putString(Keys.USER_ID, userId);
        args.putString(Keys.MY_ID, myId);
        args.putString(Keys.LIST_ID, listId);
        args.putString(Keys.LIST_USER_ID, listUserId);
        args.putString(Keys.QUERY_USER_ID, queryUserId);
        args.putString(Keys.TAGS, tagId);
        args.putBoolean(Keys.FROM_NOTIFICATIONS, true);
        args.putString(Keys.SENDER_TYPE, AppConstants.USER);
        args.putString(Keys.PROFILE_IMAGE, profileImage);
        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, ChatDetailsFragment.class.getName(), args),
                FragmentTags.CHAT_DETAILS, false, null
        );

    }


    @Override
    public void onResume() {
        super.onResume();

        final Intent chatServiceBindIntent = new Intent(this, ChatService.class);
        bindService(chatServiceBindIntent, this, Context.BIND_AUTO_CREATE);

    }


    @Override
    protected void onStop() {
        mGooglePlayClientWrapper.onStop();
        mGooglePlusManager.onActivityStopped();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Needed to ensure analytics are persisted and sent

//        YeloApplication.stopChatService();
//        YeloApplication.startChatServiceInBackground();
        setMainActivityIsOpen(false);
        MixpanelAnalytics.getInstance().flush();

        //it will be null when user is not logged in
        if(mChatService!=null) {
            mChatService.stopChatServiceAfterFiveMinutes();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (SharedPreferenceHelper
                .getBoolean(R.string.pref_update_location)) {
            mGooglePlayClientWrapper.onStart();
        }
        mGooglePlusManager.onActivityStarted();
//        YeloApplication.stopChatService();
//        YeloApplication.startChatService();

        //Send analytics if is the very first launch
        if (isFirstLaunch()) {
            onFirstLaunch();
        }

        setMainActivityIsOpen(true);

        //YeloApplication.startChatServiceMain();

        //Send referral to server if it exists
        if (AppConstants.DeviceInfo.INSTANCE.isNetworkConnected()) {
            informReferralToServer();
        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == GooglePlusManager.CONNECTION_UPDATE_ERROR)
                && (resultCode == RESULT_OK)) {
            mGooglePlusManager.onActivityResult();
        }
    }

    /**
     * Gets a reference to the Google Plus Manager
     */
    public GooglePlusManager getPlusManager() {

        return mGooglePlusManager;
    }

    @Override
    public void onLogin() {

        final EditProfileFragment fragment = ((EditProfileFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        AppConstants.FragmentTags.EDIT_PROFILE));

        if (fragment != null && fragment.isResumed()) {
            fragment.onGoogleLogin();
        }
    }

    @Override
    public void onLoginError(final Exception error) {

        final EditProfileFragment fragment = ((EditProfileFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        AppConstants.FragmentTags.EDIT_PROFILE));

        if (fragment != null && fragment.isResumed()) {
            fragment.onGoogleLoginError(error);
        }

    }

    @Override
    public void onLogout() {

        final EditProfileFragment fragment = ((EditProfileFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        AppConstants.FragmentTags.EDIT_PROFILE));

        if (fragment != null && fragment.isResumed()) {
            fragment.onGoogleLogout();
        }
    }


    /**
     * Loads the {@link red.yelo.fragments.HomeScreenFragment} into the fragment container
     */
    public void loadHomeScreen() {

        Bundle args = new Bundle();

        String action = getIntent().getAction();

        if (action != null) {
            if (action.equals(AppConstants.ACTION_SHOW_ALL_CHATS)) {
                args.putInt(Keys.PAGER_POSITION, 2);
            }
        } else {
            args.putInt(Keys.PAGER_POSITION, 1);

        }

        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, HomeScreenFragment.class
                                .getName(), args), FragmentTags.HOME_SCREEN, false,
                null
        );

//        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
//                        .instantiate(this, MyProfileActivity.class
//                                .getName(), args), FragmentTags.HOME_SCREEN, false,
//                null
//        );

//        final Intent homePager = new Intent(this,
//                HomePagerActivity.class);
//        startActivity(homePager);

    }



    @Override
    protected Object getTaskTag() {
        return null;
    }


    @Override
    public void onLocationChanged(Location location) {

        AppConstants.DeviceInfo.INSTANCE.setLatestLocation(location);
        Logger.d(TAG, location.getLatitude() + "");

        SharedPreferenceHelper.set(R.string.pref_latitude, location.getLatitude() + "");
        SharedPreferenceHelper.set(R.string.pref_longitude, location.getLongitude() + "");

        mLocationText.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);

        if (isFirstLaunch()) {
            loadHomeScreen();
        }

        mGooglePlayClientWrapper.onStop();
        //set app will fetch location
        SharedPreferenceHelper
                .set(R.string.pref_update_location, false);

    }

    /**
     * Informs the referral to server if it exists
     */
    private void informReferralToServer() {

        final String referrer = SharedPreferenceHelper
                .getString(R.string.pref_referrer);

        if (!TextUtils.isEmpty(referrer)) {

            final Map<String, String> params = new HashMap<String, String>();
            params.put(HttpConstants.REFERRAL_ID, referrer);
            params.put(HttpConstants.DEVICE_ID, AppConstants.UserInfo.INSTANCE.getDeviceId());

            mYeloApi.postReferrer(params, this);

        }
    }

    @Override
    public void success(Object o, Response response) {

        if (o != null) {
            if (o.getClass().equals(NetworkStatusResponse.class)) {
                //mProgressBar.setVisibility(View.GONE);
                NetworkStatusResponse networkStatusResponse = ((NetworkStatusResponse) o);

                if (networkStatusResponse.code.equals(AppConstants.ServerStatus.SERVER_STATUS_OK)) {
                    final String action = getIntent().getAction();

                    if (action == null) {

                        if (!isFinishing())
                            //loadEditProfileFragment();
                            loadAppropriateScreen();


                    } else if (action.equals(AppConstants.ACTION_SHOW_ALL_CHATS)) {
                        loadChatsFragment();
                    } else if (action.equals(AppConstants.ACTION_SHOW_HOME_SCREEN)) {
                        loadChatsFragment();
                    } else if (action.equals(AppConstants.ACTION_SHOW_CHAT_DETAIL)) {
                        loadChatDetailFragment(getIntent().getStringExtra(Keys.CHAT_ID), getIntent()
                                        .getStringExtra(Keys.USER_ID),
                                getIntent().getStringExtra(Keys.MY_ID),
                                getIntent().getStringExtra(Keys.LIST_ID),
                                getIntent().getStringExtra(Keys.TAGS),
                                getIntent().getStringExtra(Keys.LIST_USER_ID),
                                getIntent().getStringExtra(Keys.QUERY_USER_ID),
                                getIntent().getStringExtra(Keys.PROFILE_IMAGE));


                    } else {
                        //loadEditProfileFragment();
                        loadAppropriateScreen();
                    }
                } else if (networkStatusResponse.code.equals(AppConstants.ServerStatus.SERVER_STATUS_MAINTAINENCE)) {
                    String message = networkStatusResponse.message;
                    TextView serverMessageText = (TextView) findViewById(R.id.network_message);
                    mLayoutServerMessage.setVisibility(View.VISIBLE);
                    serverMessageText.setText(message);

                } else if (networkStatusResponse.code.equals(AppConstants.ServerStatus.SERVER_STATUS_UPDATE)) {
                    String message = networkStatusResponse.message;
                    TextView serverMessageText = (TextView) findViewById(R.id.network_message);
                    mLayoutServerMessage.setVisibility(View.VISIBLE);
                    mUpdateApp.setVisibility(View.VISIBLE);
                    serverMessageText.setText(message);

                }


            } else {
                SharedPreferenceHelper.removeKeys(this,
                        R.string.pref_referrer);
            }
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.update_button) {
            Uri marketUri = Uri
                    .parse(AppConstants.PLAY_STORE_MARKET_LINK);
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);
        } else if (v.getId() == R.id.select_location) {
            final Intent selectLocationActivity = new Intent(this,
                    SearchLocationActivity.class);
            selectLocationActivity.putExtra(AppConstants.Keys.FROM_AVATAR, true);
            startActivity(selectLocationActivity);
        }
    }


    //this called when user select location manually from SearchLocationFragment if GPS is off
    //or the location is null
    @Subscribe
    public void getLocation(GetLocation location) {


        SharedPreferenceHelper.set(R.string.pref_latitude, location.latitude);
        SharedPreferenceHelper.set(R.string.pref_longitude, location.longitude);

        Logger.d(TAG, "location manually saved");
    }

    /**
     * Checks whether this is the first launch of the app or not
     *
     * @return {@code true} if is the first launch, {@code false} otherwise
     */
    public boolean isFirstLaunch() {
        return SharedPreferenceHelper.getBoolean(R.string.pref_first_launch, true);
    }


    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_SERVER_STATUS:
                ServerStatus serverStatus = ((ServerStatus) model);

                if (serverStatus.code.equals(AppConstants.ServerStatus.SERVER_STATUS_OK)) {
                    //do nothing continue the app

                } else if (serverStatus.code.equals(AppConstants.ServerStatus.SERVER_STATUS_UPDATE)) {
                    try {
                        PackageInfo info = getPackageManager()
                                .getPackageInfo(getPackageName(), 0);


                        if (info.versionCode <= Integer.parseInt(serverStatus.version_android)) {

                            final Intent updateIntent = new Intent(this,
                                    UpdateScreenActivity.class);

                            updateIntent.putExtra(Keys.UPDATE_TEXT, serverStatus.message);
                            startActivity(updateIntent);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        //Shouldn't happen
                    }

                } else if (serverStatus.code.equals(AppConstants.ServerStatus.SERVER_STATUS_MAINTAINENCE)) {
                    final Intent updateIntent = new Intent(this,
                            UpdateScreenActivity.class);

                    updateIntent.putExtra(Keys.UPDATE_TEXT, serverStatus.message);
                    updateIntent.putExtra(Keys.MAINTAINENCE, true);

                     startActivity(updateIntent);
                }

                break;

            case HttpConstants.ApiResponseCodes.UPDATE_GCM_ID: {

                SharedPreferenceHelper.set(R.string.pref_push_id_uploaded, true);

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

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mBoundToChatService = true;
        mChatService = ((ChatService.ChatServiceBinder) service).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBoundToChatService = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBoundToChatService) {
            unbindService(this);
        }
    }

    @Subscribe
    public void openServiceDialog(ServiceCardConfirmationDialog serviceCardConfirmationDialog){

        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.service_confirm_dialog_title))
                .setMessage(serviceCardConfirmationDialog.message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }



}

