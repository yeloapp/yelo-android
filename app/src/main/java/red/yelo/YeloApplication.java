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
 */package red.yelo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.view.ViewConfiguration;

import com.bugsense.trace.BugSenseHandler;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.lang.reflect.Field;

import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.analytics.SessionTracker;
import red.yelo.bus.RestAdapterUpdate;
import red.yelo.chat.ChatService;
import red.yelo.http.HttpConstants;
import red.yelo.http.api.ChatApi;
import red.yelo.http.api.FacebookApi;
import red.yelo.http.api.GoogleApi;
import red.yelo.http.api.GoogleUrlShortenerApi;
import red.yelo.http.api.GoogleUserApi;
import red.yelo.http.api.YeloApi;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.UserInfo;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.utils.md5.MD5;
import red.yelo.verification.VerificationService;
import retrofit.ErrorHandler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Custom Application class which holds some common functionality for the
 * Application
 *
 * @author Anshul Kamboj
 */
public class YeloApplication extends Application {

    private static final String TAG = "YeloApplication";

    /**
     * Reference to the bus (OTTO By Square)
     */
    private Bus mBus;


    /**
     * Maintains a reference to the application context so that it can be
     * referred anywhere wihout fear of leaking. It's a hack, but it works.
     */
    private static Context sStaticContext;

    private YeloApi mYeloApi;

    private ChatApi mChatApi;

    private GoogleApi mGoogleApi;

    private FacebookApi mFacebookApi;

    private GoogleUserApi mGoogleUserApi;

    private GoogleUrlShortenerApi mGoogleUrlShortenerApi;

    public static volatile Handler applicationHandler = null;


    /**
     * Gets a reference to the application context
     */
    public static Context getStaticContext() {
        if (sStaticContext != null) {
            return sStaticContext;
        }

        //Should NEVER hapen
        throw new RuntimeException("No static context instance");
    }

    @Override
    public void onCreate() {

        sStaticContext = getApplicationContext();
        /* Disable native library loading for MD5 hashes */
        MD5.initNativeLibrary(true);

        /* Initialize this right here because initialization is a heavy process */
        //PhoneNumberUtil.getInstance();

        mBus = new Bus();
        mBus.register(this);


        if (BuildConfig.USE_BUGSENSE) {
            BugSenseHandler.initAndStartSession(sStaticContext, getResources().getString(R.string.bug_sense_api_key));
        }

        readUserInfoFromSharedPref();
        overrideHardwareMenuButton();
        clearNotifications();


//        if (SharedPreferenceHelper
//                .getInt(R.string.pref_last_version_code,0) ==0) {
//            doMigrationFromAlpha();
//        }
//

        /*
        * In app version 7, a buggy version of contact sync was shipped
         *
         * So on update, we rest the flag so that contact sync is attempted again
         * */
        if (SharedPreferenceHelper.getInt(R.string.pref_last_version_code, 0) == 7) {
            SharedPreferenceHelper.removeKeys(this, R.string.pref_last_contact_sync_time);
        }


        saveCurrentAppVersionIntoPreferences();
        initApiServices();
        Utils.setupNetworkInfo(this);

        Logger.d(TAG, "Started Application");

        if (AppConstants.DeviceInfo.INSTANCE.isNetworkConnected()) {
            //startChatService();

        }
        initSessionTracking();
        setUserNameInMixpanel();

        applicationHandler = new Handler(sStaticContext.getMainLooper());


    }

    /**
     * Sets the user name in Mixpanel
     */
    private void setUserNameInMixpanel() {

        if (isLoggedIn()) {

            if (!TextUtils.isEmpty(UserInfo.INSTANCE.getFirstName())) {
                MixpanelAnalytics.getInstance().nameUser(UserInfo.INSTANCE.getFirstName());
            }
        }

    }

    /**
     * Is the user logged in
     */
    protected boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getFirstName());
    }

    /**
     * Initialize the Api Services
     */
    private void initApiServices() {

        final RestAdapter.Builder builder = new RestAdapter.Builder()
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        if (!UserInfo.INSTANCE.getAuthToken().equals("")) {
                            request.addHeader(HttpConstants.HEADER_AUTHORIZATION, UserInfo.INSTANCE.getAuthHeader());
                        }
                    }
                })
                .setLogLevel(BuildConfig.DEBUG_MODE ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE);

        mYeloApi = builder.setEndpoint(HttpConstants.getApiBaseUrl()).build().create(YeloApi.class);
        mChatApi = builder.setEndpoint(HttpConstants.getApiChatUrl()).build().create(ChatApi.class);
        mGoogleApi = builder.setRequestInterceptor(RequestInterceptor.NONE).setEndpoint(HttpConstants.getGoogleGeocodeApiBaseUrl()).build().create(GoogleApi.class);
        mGoogleUserApi = builder.setRequestInterceptor(RequestInterceptor.NONE).setEndpoint(HttpConstants.getGoogleUserInfo()).build().create(GoogleUserApi.class);
        mGoogleUrlShortenerApi = builder.setRequestInterceptor(RequestInterceptor.NONE).setEndpoint(HttpConstants.getGoogleUrlShortenerApiUrl()).build().create(GoogleUrlShortenerApi.class);
        mFacebookApi = builder.setRequestInterceptor(RequestInterceptor.NONE).setEndpoint(HttpConstants.getFacebookBaseUrl()).build().create(FacebookApi.class);
    }

    /**
     * Initialize the session tracking which we will use to track app sessions
     */
    private void initSessionTracking() {

        /*
      * Tracking for tracking app sessions
     */
        new SessionTracker(this, new SessionTracker.SessionCallbacks() {
            @Override
            public void onSessionStarted(long startedAt) {
                Logger.d(TAG, "App Session Start");
            }

            @Override
            public void onSessionEnded(long endedAt, long sessionDurationMillis) {

                Logger.d(TAG, "App session end %d", sessionDurationMillis);
                MixpanelAnalytics.getInstance().onSessionEnded(sessionDurationMillis / 1000L);
            }
        });

    }


    /**
     * This migrates the locally cached data from alpha. The only thing this is
     * doing currently is clearing the Shared preferences
     */
    private void doMigrationFromAlpha() {
        SharedPreferenceHelper.clearPreferences(this);
    }


    /**
     * Start the chat service. The connection doesn't happen if the user isn't
     * logged in.
     */
    public static void startChatService() {


        final Intent intent = new Intent(sStaticContext, ChatService.class);
        intent.putExtra(AppConstants.Keys.HEART_BEAT, AppConstants.HEART_BEAT);

        sStaticContext.startService(intent);
    }

    /**
     * Start the chat service. The connection doesn't happen if the user isn't
     * logged in.
     */
    public static void startChatServiceMain() {


        final Intent intent = new Intent(sStaticContext, ChatService.class);
        intent.setAction(AppConstants.ACTION_DISCONNECT_CHAT);
        sStaticContext.startService(intent);
    }


    /**
     * Start the chat service. The connection doesn't happen if the user isn't
     * logged in.
     */
    public static void startChatServiceInBackground() {


        final Intent intent = new Intent(sStaticContext, ChatService.class);
        intent.putExtra(AppConstants.Keys.HEART_BEAT, AppConstants.HEART_BEAT_BACKGROUND);
        sStaticContext.startService(intent);
    }


    /**
     * Syncs the hashed user phone book to server if needed
     */
    public static void syncContactsIfNecessary(boolean uploadFlag) {

        if(uploadFlag) {
            final long lastContactSyncTime = SharedPreferenceHelper.getLong(R.string.pref_last_contact_sync_time);

            if ((Utils.getCurrentEpochTime() - lastContactSyncTime) > AppConstants.CONTACT_SYNC_INTERVAL) {

                final Intent syncContactsIntent = new Intent(sStaticContext, FriendFinderService.class);
                syncContactsIntent.setAction(FriendFinderService.ACTION_FIND_FRIENDS);
                sStaticContext.startService(syncContactsIntent);
            }
        }
    }

    /** @deprecated because og privacy issues
     * Start the Verification Service
     */
    public static void startVerificationService() {


        final Intent intent = new Intent(sStaticContext, VerificationService.class);
        sStaticContext.startService(intent);
    }

    public static void stopVerificationService() {

        final Intent intent = new Intent(sStaticContext, VerificationService.class);
        sStaticContext.stopService(intent);
    }

    /**
     * Stop the chat service.
     */
    public static void stopChatService() {

        final Intent intent = new Intent(sStaticContext, ChatService.class);
        sStaticContext.stopService(intent);

    }


    /**
     * Reads the previously fetched auth token from Shared Preferencesand stores
     * it in the Singleton for in memory access
     */
    private void readUserInfoFromSharedPref() {

        UserInfo.INSTANCE.setAuthToken(SharedPreferenceHelper
                .getString(R.string.pref_auth_token));
        UserInfo.INSTANCE.setId(SharedPreferenceHelper
                .getString(R.string.pref_user_id));
        UserInfo.INSTANCE.setEmail(SharedPreferenceHelper
                .getString(R.string.pref_email));
        UserInfo.INSTANCE.setProfilePicture(SharedPreferenceHelper
                .getString(R.string.pref_profile_image));
        UserInfo.INSTANCE.setFirstName(SharedPreferenceHelper
                .getString(R.string.pref_first_name));
        UserInfo.INSTANCE.setMobileNumber(SharedPreferenceHelper.getString(R.string.pref_mobile_number));
        UserInfo.INSTANCE.setDeviceId(SharedPreferenceHelper.getString(R.string.pref_device_id));
        UserInfo.INSTANCE.setDescription(SharedPreferenceHelper.getString(R.string.pref_description));


        //set app will fetch location
        SharedPreferenceHelper
                .set(R.string.pref_update_location, true);


    }


    public Bus getBus() {
        return mBus;
    }

    public ChatApi getChatApi() {
        return mChatApi;
    }

    public YeloApi getYeloApi() {
        return mYeloApi;
    }

    public GoogleApi getGoogleApi() {
        return mGoogleApi;
    }

    public FacebookApi getFacebookApi() {
        return mFacebookApi;
    }

    public GoogleUserApi getGoogleUserApi() {
        return mGoogleUserApi;
    }

    public GoogleUrlShortenerApi getGoogleUrlShortenerApi() {
        return mGoogleUrlShortenerApi;
    }


    /**
     * Some device manufacturers are stuck in the past and stubbornly use H/W
     * menu buttons, which is deprecated since Android 3.0. This breaks the UX
     * on newer devices since the Action Bar overflow just doesn't show. This
     * little hack tricks the Android OS into thinking that the device doesn't
     * have a permanant menu button, and hence the Overflow button gets shown.
     * This doesn't disable the Menu button, however. It will continue to
     * function as normal, so the users who are already used to it will be able
     * to use it as before
     */
    private void overrideHardwareMenuButton() {
        try {
            final ViewConfiguration config = ViewConfiguration.get(this);
            final Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (final Exception ex) {
            // Ignore since we can't do anything
        }

    }

    //This method is for the response of the bus.post method which is called when we get the auth
    //token after verification of phone number
    @Subscribe
    public void updateRestAdapterEvent(RestAdapterUpdate restAdapterUpdate) {
        UserInfo.INSTANCE.setAuthToken(restAdapterUpdate.authToken);
        SharedPreferenceHelper.set(R.string.pref_auth_token, restAdapterUpdate.authToken);
        /*mRestAdapter = new RestAdapter.Builder()
                .setEndpoint(HttpConstants.getApiBaseUrl())
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError cause) {
                        Response r = cause.getResponse();
                        if (r != null && r.getStatus() == 401) {
                            return new Exception(cause);
                        }
                        return cause;
                    }
                })
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        if (!UserInfo.INSTANCE.getAuthToken().equals("")) {
                            request.addHeader(HttpConstants.HEADER_AUTHORIZATION, UserInfo.INSTANCE.getAuthHeader());
                            Logger.d(TAG, "Updated restAdapter with authtoken");
                        }
                    }
                })
                .build();

        mChatRestAdapter = new RestAdapter.Builder()
                .setEndpoint(HttpConstants.getApiChatUrl())
                .setErrorHandler(new ErrorHandler() {
                    @Override
                    public Throwable handleError(RetrofitError cause) {
                        Response r = cause.getResponse();
                        if (r != null && r.getStatus() == 401) {
                            return new Exception(cause);
                        }
                        return cause;
                    }
                })
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        if (!UserInfo.INSTANCE.getAuthToken().equals("")) {
                            request.addHeader(HttpConstants.HEADER_AUTHORIZATION, UserInfo.INSTANCE.getAuthHeader());
                            Logger.d(TAG, "Updated restAdapter with authtoken");
                        }
                    }
                })
                .build();*/

        YeloApplication.startChatService();

        /*if (BuildConfig.DEBUG_MODE) {
            mRestAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
            mChatRestAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        }

        mChatApiService = mChatRestAdapter.create(Api.class);
        mApiService = mRestAdapter.create(Api.class);*/

    }

    /**
     * Save the current app version info into preferences. This is purely for
     * future use where we might need to use these values on an app update
     */
    private void saveCurrentAppVersionIntoPreferences() {
        try {
            PackageInfo info = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            SharedPreferenceHelper
                    .set(R.string.pref_last_version_code, info.versionCode);
            SharedPreferenceHelper
                    .set(R.string.pref_last_version_name, info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            //Shouldn't happen
        }
    }

    private void clearNotifications() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(AppConstants.NOTIFICATION_ID_WALL);
    }
}
