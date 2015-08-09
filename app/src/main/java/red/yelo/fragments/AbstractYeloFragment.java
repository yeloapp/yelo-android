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

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.activities.AbstractYeloActivity;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.data.DatabaseColumns;
import red.yelo.http.RetroCallback;
import red.yelo.http.api.ChatApi;
import red.yelo.http.api.FacebookApi;
import red.yelo.http.api.GoogleApi;
import red.yelo.http.api.GoogleUrlShortenerApi;
import red.yelo.http.api.GoogleUserApi;
import red.yelo.http.api.YeloApi;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.Keys;
import red.yelo.utils.AppConstants.UserInfo;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by anshul1235 on 14/07/14.
 */
public abstract class AbstractYeloFragment extends Fragment implements Callback {

    private static final String TAG = "AbstractYeloFragment";

    /**
     * Flag that indicates that this fragment is attached to an Activity
     */
    private boolean mIsAttached;

    /**
     * Stores the id for the container view
     */
    protected int mContainerViewId;

    protected YeloApi mYeloApi;

    protected ChatApi mChatApi;

    protected GoogleApi mGoogleApi;

    protected FacebookApi mFacebookApi;

    protected GoogleUrlShortenerApi mGoogleUrlShortenerApi;


    protected GoogleUserApi mGoogleUserApi;

    protected Bus mBus;

    private static final int FADE_CROSSOVER_TIME_MILLIS = 300;

    private ProgressDialog mProgressDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        YeloApplication application = (YeloApplication) getActivity().getApplication();
        mYeloApi = application.getYeloApi();
        mChatApi = application.getChatApi();
        mGoogleApi = application.getGoogleApi();
        mFacebookApi = application.getFacebookApi();
        mGoogleUserApi = application.getGoogleUserApi();
        mGoogleUrlShortenerApi = application.getGoogleUrlShortenerApi();
        mBus = application.getBus();
        mIsAttached = true;

    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * A Tag to add to all async tasks. This must be unique for all Fragments types
     *
     * @return An Object that's the tag for this fragment
     */
    protected abstract Object getTaskTag();

    /**
     * Whether this Fragment is currently attached to an Activity
     *
     * @return <code>true</code> if attached, <code>false</code> otherwise
     */
    public boolean isAttached() {
        return mIsAttached;
    }

    /**
     * Call this method in the onCreateView() of any subclasses
     *
     * @param container          The container passed into onCreateView()
     * @param savedInstanceState The Instance state bundle passed into the onCreateView() method
     */
    protected void init(final ViewGroup container,
                        final Bundle savedInstanceState) {
        if (container != null) {
            mContainerViewId = container.getId();
        }
        long lastScreenTime = 0l;

        if (savedInstanceState != null) {
            lastScreenTime = savedInstanceState.getLong(Keys.LAST_SCREEN_TIME);
        }

    }

    /**
     * Helper method to load fragments into layout
     *
     * @param containerResId The container resource Id in the content view into which to load the
     *                       fragment
     * @param fragment       The fragment to load
     * @param tag            The fragment tag
     * @param addToBackStack Whether the transaction should be addded to the backstack
     * @param backStackTag   The tag used for the backstack tag
     */
    public void loadFragment(final int containerResId,
                             final AbstractYeloFragment fragment, final String tag,
                             final boolean addToBackStack, final String backStackTag) {

        if (mIsAttached) {
            ((AbstractYeloActivity) getActivity())
                    .loadFragment(containerResId, fragment, tag, addToBackStack, backStackTag);
        }

    }

    public void setActionBarDisplayOptions(final int displayOptions) {
        if (mIsAttached) {

            ((AbstractYeloActivity) getActivity())
                    .setActionBarDisplayOptions(displayOptions);
        }
    }

    /**
     * Pops the fragment from the backstack, checking to see if the bundle args have {@linkplain
     * Keys#UP_NAVIGATION_TAG} which gives the name of the backstack tag to pop to. This is mainly
     * for providing Up navigation
     */
    public void onUpNavigate() {
        final Bundle args = getArguments();

        if ((args != null) && args.containsKey(Keys.UP_NAVIGATION_TAG)) {
            getFragmentManager()
                    .popBackStack(args.getString(Keys.UP_NAVIGATION_TAG),
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            getFragmentManager().popBackStack();
        }
    }

    /**
     * Whether this fragment will handle the particular dialog click or not
     *
     * @param dialog The dialog that was interacted with
     * @return <code>true</code> If the fragment will handle it, <code>false</code> otherwise
     */
    public boolean willHandleDialog(final DialogInterface dialog) {


        return false;
    }

    /**
     * Handle the click for the dialog. The fragment will receive this call, only if {@link
     * #willHandleDialog(DialogInterface)} returns <code>true</code>
     *
     * @param dialog The dialog that was interacted with
     * @param which  The button that was clicked
     */
    public void onDialogClick(final DialogInterface dialog, final int which) {


    }

    /**
     * Is the user logged in
     */
    protected boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getMobileNumber());
    }

    protected boolean isVerified() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getAuthToken());
    }

    protected boolean isActivated() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getId());
    }


    /**
     * for refreshing the user fragment after user login and logout
     */
    protected void userRefresh(boolean flag) {
        SharedPreferenceHelper.set(R.string.pref_force_user_refetch, flag);

    }


    /**
     * Handles the behaviour for onBackPressed().
     *
     * @return <code>true</code> If the fragment will handle onBackPressed
     */
    public boolean onBackPressed() {

        return false;

    }

    protected String getCountryCode() {
        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryZipCode;
    }


    public void sendSmsMessage(String mobileNumber, String key) {

        SmsManager smsManager = SmsManager.getDefault();

        try {
            smsManager.sendTextMessage(mobileNumber, null, AppConstants.SMS_VERIFY_FORMAT + key, null, null);
            MixpanelAnalytics.getInstance().onVerificationSmsSent();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getResources().getString(R.string.sms_not_send), Toast.LENGTH_SHORT).show();
        }
    }

    // Copy EditCopy text to the ClipBoard
    public void copyToClipBoard(String message) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Chat", message);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public void onStop() {
        super.onStop();
        //TODO Cancel all requests
        getActivity().setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContainerViewId = 0;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsAttached = false;
    }

    public void hideKeyboard(EditText editText) {

        if (editText != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        } else {

            getActivity().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    public void showKeyboard(EditText editText) {

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Gets a reference to the Activity's action Bar, or {@code null} if none exists
     */
    public ActionBar getActionBar() {

        if (isAttached()) {
            final Activity activity = getActivity();

            if (activity instanceof AppCompatActivity) {
                return ((AppCompatActivity) activity).getSupportActionBar();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                getActivity().finish();

                return true;
            }


            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    //TODO not in all cases it retrieves contact number
    public String getMobileNumber() {
        String mPhoneNumber = "";
        TelephonyManager tMgr = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneNumber = tMgr.getLine1Number();

        if (TextUtils.isEmpty(mPhoneNumber)) {
            return "";
        } else {
            return mPhoneNumber.substring(mPhoneNumber.length() - 10, mPhoneNumber.length());
        }
    }

    public int getUtcOffset() {
        TimeZone tz = TimeZone.getDefault();
        Date now = new Date();
        int offsetFromUtc = tz.getOffset(now.getTime()) / 1000;
        return offsetFromUtc;
    }

    public void shareWall(final String message) {

        final String appDownloadLink = Utils.getShareLinkForTag();
        shareIntent(message, appDownloadLink);
//        if (!AppConstants.DeviceInfo.INSTANCE.isNetworkConnected()) {
//            shareIntent(message, appDownloadLink);
//        } else {
//
//            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setIndeterminate(true);
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progressDialog.setMessage(getString(R.string.generating_share_link));
//            progressDialog.show();
//
//            final Map<String, String> params = new HashMap<>(2);
//            params.put(HttpConstants.KEY, getString(R.string.google_api_key));
//
//            final GoogleUrl url = new GoogleUrl();
//            url.longUrl = appDownloadLink;
//
//            final RetroCallback<GoogleUrl> googleUrlRetroCallback = new RetroCallback<>(new RetroCallback.RetroResponseListener<GoogleUrl>() {
//                @Override
//                public void success(GoogleUrl model, int requestId) {
//
//                    if (isAttached()) {
//                            progressDialog.dismiss();
//                            shareIntent(message, model.id);
//                    }
//                }
//
//                @Override
//                public void failure(int requestId, int errorCode, String message) {
//
//                    if (isAttached()) {
//                        progressDialog.dismiss();
//                        shareIntent(message, appDownloadLink);
//                    }
//                }
//            });
//            googleUrlRetroCallback.setRequestId(HttpConstants.ApiResponseCodes.SHORTEN_URL);
//            mGoogleUrlShortenerApi.shortenUrl(params, url, googleUrlRetroCallback);
//        }
    }

    private void shareIntent(String message, String appDownloadLink) {
        final Intent shareIntent = Utils
                .createAppShareIntent(getActivity(), message, appDownloadLink);
        try {
            startActivity(Intent
                    .createChooser(shareIntent,
                            getString(R.string.share_via)));
        } catch (ActivityNotFoundException e) {
            //Shouldn't happen
        }
    }


    public void shareTag(final String userName, final String category) {

        final String appDownloadLink = Utils.getShareLinkForTag();
        shareIntentForTag(userName, category, appDownloadLink);
//        if (!AppConstants.DeviceInfo.INSTANCE.isNetworkConnected()) {
//            shareIntentForTag(userName,category,appDownloadLink);
//        } else {
//
//            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setIndeterminate(true);
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progressDialog.setMessage(getString(R.string.generating_share_link));
//            progressDialog.show();
//
//            final Map<String, String> params = new HashMap<>(2);
//            params.put(HttpConstants.KEY, getString(R.string.google_api_key));
//
//            final GoogleUrl url = new GoogleUrl();
//            url.longUrl = appDownloadLink;
//
//            final RetroCallback<GoogleUrl> googleUrlRetroCallback = new RetroCallback<>(new RetroCallback.RetroResponseListener<GoogleUrl>() {
//                @Override
//                public void success(GoogleUrl model, int requestId) {
//
//                    if (isAttached()) {
//                        progressDialog.dismiss();
//                        shareIntentForTag(userName,category, model.id);
//                    }
//                }
//
//                @Override
//                public void failure(int requestId, int errorCode, String message) {
//
//                    if (isAttached()) {
//                        progressDialog.dismiss();
//                        shareIntent(message, appDownloadLink);
//                    }
//                }
//            });
//            googleUrlRetroCallback.setRequestId(HttpConstants.ApiResponseCodes.SHORTEN_URL);
//            mGoogleUrlShortenerApi.shortenUrl(params, url, googleUrlRetroCallback);
//        }
    }

    private void shareIntentForTag(String userName, String category, String appStoreLink) {
        Intent shareIntent = Utils.createTagShareIntent(getActivity(), userName, category, appStoreLink);
        try {
            startActivity(Intent
                    .createChooser(shareIntent,
                            "Share with " + userName));
        } catch (ActivityNotFoundException e) {
            //Shouldn't happen
        }
    }

    public void cancelAllCallbacks(List<RetroCallback> retroCallbackList) {
        for (RetroCallback aRetroCallbackList : retroCallbackList) {
            aRetroCallbackList.cancel();
        }
    }

    @Override
    public void success(Object o, Response response) {

    }

    @Override
    public void failure(RetrofitError error) {

        if (isAttached()) {
            Toast.makeText(getActivity(), R.string.retro_error, Toast.LENGTH_SHORT).show();
        }
    }

    public ActionBar setToolbar(Toolbar toolbar) {

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().
                setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back);

        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    public ActionBar setToolbar(Toolbar toolbar, String title, boolean isNavigationWhite) {

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (isNavigationWhite) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().
                    setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back_white);

        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().
                    setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back);
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);

        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    public void colorizeActionBar(int color, final Toolbar toolbar) {

        if (getActivity() != null) {
            int oldColor = getResources().getColor(R.color.white);

            if (toolbar != null) {
                Drawable toolbarDrawable = toolbar.getBackground();
                if (toolbarDrawable != null && toolbarDrawable instanceof ColorDrawable) {
                    oldColor = ((ColorDrawable) toolbarDrawable).getColor();
                }
            }

            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, color);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int color = (Integer) valueAnimator.getAnimatedValue();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getActivity().getWindow().setStatusBarColor(darkenColor(color));
                        getActivity().getWindow().setNavigationBarColor(darkenColor(color));
                    }
                    toolbar.setBackgroundColor(color);
                }
            });
            colorAnimation.setDuration(FADE_CROSSOVER_TIME_MILLIS);
            colorAnimation.start();
        }
    }

    public void colorizeView(int color, final View view) {

        if (view != null) {

            int oldColor = getResources().getColor(R.color.transparent);

            if (view != null) {
                Drawable toolbarDrawable = view.getBackground();
                if (toolbarDrawable != null && toolbarDrawable instanceof ColorDrawable) {
                    oldColor = ((ColorDrawable) toolbarDrawable).getColor();
                }
            }

            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, color);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int color = (Integer) valueAnimator.getAnimatedValue();

                    view.setBackgroundColor(color);
                }
            });
            colorAnimation.setDuration(FADE_CROSSOVER_TIME_MILLIS);
            colorAnimation.start();
        }
    }

    private int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }


    public ProgressDialog showProgressDialog() {


        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgress(0);
        return mProgressDialog;
    }

    private String getElapsedTimeFormat(long timeEpoch, Cursor cursor) {

        long timeElapsed = Utils.getCurrentEpochTime() - timeEpoch;

        int[] timeValues = Utils.getHoursMinsSecs(timeElapsed);

        final int hours = timeValues[0];
        final int minutes = timeValues[1];
        final int seconds = timeValues[2];
        final int days = hours / 24;
        final int weeks = days / 7;


        if (hours < 1) {
            if (minutes < 1) {
                if (seconds < 10) {
                    return getActivity().getString(R.string.just_now);
                } else {
                    return getActivity().getString(R.string.seconds_ago, seconds);
                }

            } else {
                return getActivity().getString(R.string.minutes_ago, minutes);
            }
        } else if (hours < 23) {
            return getActivity().getString(R.string.hours_ago, hours);

        } else if (hours > 23 && hours < 167) {

            return getActivity().getString(R.string.days_ago, days);


        } else if (weeks > 0) {
            return getActivity().getString(R.string.weeks_ago, weeks);
        } else {
            return cursor.getString(cursor
                    .getColumnIndex(DatabaseColumns.TIMESTAMP_HUMAN));
        }

    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public void showNetworkNotAvailableMessage(String message) {
        if (TextUtils.isEmpty(message)) {

        } else {
            if (message.contains("Network is unreachable"))

                Toast.makeText(getActivity(), "Network is unreachable", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleShare() {
        String shareString = Utils.getShareLinkForTag();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Try out yelo! Download now\n"+shareString);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
}
