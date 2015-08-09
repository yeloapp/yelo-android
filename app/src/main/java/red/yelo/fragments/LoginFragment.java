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
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dd.CircularProgressButton;
import com.squareup.otto.Subscribe;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.activities.HomeActivity;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.analytics.VerificationMethod;
import red.yelo.bus.HomeOpen;
import red.yelo.bus.PageNextPrevious;
import red.yelo.bus.RestAdapterUpdate;
import red.yelo.bus.SmsVerification;
import red.yelo.data.DBInterface;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.request.UserDetailsRequestModel;
import red.yelo.retromodels.request.VerifyUserRequestModel;
import red.yelo.retromodels.response.SerialKeyResponseModel;
import red.yelo.retromodels.response.VerifySmsResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.UserInfo;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.widgets.CirclePageIndicator;
import red.yelo.widgets.layout.LinearLayoutKeyboardHandler;

/**
 * Created by anshul1235 on 15/07/14.
 */


public class LoginFragment extends AbstractYeloFragment implements View.OnClickListener,
        DBInterface.AsyncDbQueryCallback, RetroCallback.RetroResponseListener, ViewPager.OnPageChangeListener {

    public static final String TAG = "LoginFragment";

    private static final long TEXT_SWITCH_INTERVAL = 2000L;
    private EditText mMobileNumber, mCountryCodeEdit, mEnterCodeEdit;

    private CircularProgressButton mActivateButton;
    private Button mVerifyManually;
    private TextView mReEnterButton,mCopyrightText;
    private boolean mVerificationSent = false,mVerifying,mFragmentPaused = false,mReverse;
    private TextSwitcher mTextSwitcher;
    private int indexSwitch = 0,indexSwitchImage = 0;
    private String[] mSwitcherTextArray;
    private CountDownTimer mSmsWaitTimer,mTextSwitchTimer, mImageSlideTimer;
    private LinearLayout mNumberHolderLayout;
    private CirclePageIndicator mCirclePageIndicator;
    private ViewPager mImageTutorialPager;
    private PagerAdapter mPagerdapter;
    private LinearLayoutKeyboardHandler mLinearLayoutKeyboardHandler;
    private View mCustomeDialogView;


    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);

        setHasOptionsMenu(true);


        final View contentView = inflater
                .inflate(R.layout.fragment_login, container, false);


        mSwitcherTextArray = getResources().getStringArray(R.array.login_switcher_array);
        mTextSwitcher = (TextSwitcher) contentView.findViewById(R.id.main_textswitcher);
        mMobileNumber = (EditText) contentView.findViewById(R.id.edit_mobilenumber);
        mCountryCodeEdit = (EditText) contentView.findViewById(R.id.edit_countrycode);
        mActivateButton = (CircularProgressButton) contentView.findViewById(R.id.button_activate);
        mEnterCodeEdit = (EditText) contentView.findViewById(R.id.enter_code_text);
        mNumberHolderLayout = (LinearLayout) contentView.findViewById(R.id.number_holder_layout);
        mReEnterButton = (TextView) contentView.findViewById(R.id.re_enter_button);
        mCopyrightText = (TextView) contentView.findViewById(R.id.copyright_text);
        mCopyrightText.setText(Html.fromHtml(getString(R.string.copyright_string)));
        mCopyrightText.setMovementMethod(LinkMovementMethod.getInstance());



        mCirclePageIndicator = (CirclePageIndicator) contentView.findViewById(R.id.onBoardImageIndicator);
        mImageTutorialPager = (ViewPager) contentView.findViewById(R.id.imagePager);
        mLinearLayoutKeyboardHandler = (LinearLayoutKeyboardHandler) contentView.findViewById(R.id.linearlayout);
        mVerifyManually = (Button) contentView.findViewById(R.id.button_verify);
        mPagerdapter = new PagerAdapter(getChildFragmentManager());

        mVerifyManually.setOnClickListener(this);
        //mActivateButton.setIndeterminateProgressMode(true);
        mCirclePageIndicator.setOnPageChangeListener(this);

        mImageTutorialPager.setAdapter(mPagerdapter);

        mImageTutorialPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mImageSlideTimer.cancel();
                return false;
            }
        });
        mCirclePageIndicator.setViewPager(mImageTutorialPager);


        mActivateButton.setOnClickListener(this);
        mReEnterButton.setOnClickListener(this);

        mCountryCodeEdit.setText(getCountryCode());

        mMobileNumber.setText(getMobileNumber());

        mTextSwitcher.setFactory(new SwitcherOptionsViewFactory(getActivity()));
        Animation inAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.yelo_fade_in);
        mTextSwitcher.setInAnimation(inAnimation);
        Animation outAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.yelo_fade_out);
        mTextSwitcher.setOutAnimation(outAnimation);
        startSwitchingTexts();

//        mTelephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
//        mCallStateListener = new CallStateListener();
//        mTelephonyManager.listen(mCallStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(AppConstants.Keys.VERIFICATION_STATE)) {
//                mActivateButton.setText(getResources().getString(R.string.verifying_tag));
//                mActivateButton.setEnabled(false);
                mVerifying = true;
            }
        } else {
            if (mVerifying) {
//                mActivateButton.setText(getResources().getString(R.string.verifying_tag));
//                mActivateButton.setEnabled(false);
                mVerifying = true;
            }

        }
        SharedPreferenceHelper.set(R.string.pref_registration_screen, AppConstants.LOGIN_SCREEN);

        if (SharedPreferenceHelper.getBoolean(R.string.pref_is_verifying)) {

            mEnterCodeEdit.setVisibility(View.VISIBLE);
            mActivateButton.setVisibility(View.GONE);
            mVerifyManually.setVisibility(View.VISIBLE);
            mMobileNumber.setVisibility(View.GONE);
            mCountryCodeEdit.setVisibility(View.GONE);
            mReEnterButton.setVisibility(View.VISIBLE);
            mCopyrightText.setVisibility(View.GONE);
        }

        tutorialSlideShow();


        return contentView;

    }


    /**
     * Begins switching texts for the options for get in touch
     */
    private void startSwitchingTexts() {

        final int numberOfItems = mSwitcherTextArray.length;
        mTextSwitchTimer = new CountDownTimer((numberOfItems + 1) * TEXT_SWITCH_INTERVAL, TEXT_SWITCH_INTERVAL) {

            @Override
            public void onTick(long millisUntilFinished) {
                mTextSwitcher.setText(mSwitcherTextArray[indexSwitch]);
                indexSwitch = (indexSwitch + 1) % numberOfItems;
            }

            @Override
            public void onFinish() {
                start();
            }
        };
        Logger.d(TAG, "Start");
        mTextSwitchTimer.start();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(AppConstants.Keys.VERIFICATION_STATE, mVerifying);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static LoginFragment newInstance() {
        LoginFragment f = new LoginFragment();
        return f;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.auth, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return true;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_activate) {

            if (mActivateButton.getText().equals(getResources().getString(R.string.activate_tag))) {

                boolean canProceed = true;
                final String countryCode = mCountryCodeEdit.getText().toString();
                final String mobileNumber = mMobileNumber.getText().toString();

                if (TextUtils.isEmpty(countryCode)) {
                    mCountryCodeEdit.setError(getString(R.string.enter_country_code));
                    canProceed = false;
                }

                if (TextUtils.isEmpty(mobileNumber)) {
                    mMobileNumber.setError(getString(R.string.enter_mobile_number));
                    canProceed = false;
                }

                if (canProceed) {

                    final String formattedMobileNumber = String.format(Locale.US, "+%s-%s", countryCode, mobileNumber);
                    // if(Utils.isValidPhoneNumber(formattedMobileNumber)) {
                    if (mobileNumber.length() == 10) {
                        try {
                            //mActivateButton.setText(getResources().getString(R.string.verifying_tag));
                            UserInfo.INSTANCE.setDeviceId(Settings.Secure.getString(getActivity()
                                    .getContentResolver(), Settings.Secure.ANDROID_ID) + Utils.sha1(mMobileNumber.getText().toString()));

                            SharedPreferenceHelper.set(R.string.pref_device_id, UserInfo.INSTANCE.getDeviceId());
                            SharedPreferenceHelper.set(R.string.pref_mobile_number,mCountryCodeEdit.getText().toString()+ mMobileNumber.getText().toString());

                        } catch (NoSuchAlgorithmException e) {
                            //should not happen
                        }

                        UserDetailsRequestModel userDetails = new UserDetailsRequestModel();
                        userDetails.user.setMobile_number(mCountryCodeEdit.getText().toString() + mMobileNumber.getText().toString());

                        Logger.d(TAG, SharedPreferenceHelper.getString(R.string.pref_registration_id));

                        RetroCallback retroCallback;
                        retroCallback = new RetroCallback(this);
                        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.ACTIVATE_LOGIN);
                        retroCallbackList.add(retroCallback);

                        saveDetailsInSharedPref();

                        confirmPhoneNumberDialog(formattedMobileNumber);

                        //  mActivateButton.setEnabled(false);

                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.enter_valid_phone_number), Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (mActivateButton.getText().equals(getResources().getString(R.string.verify_manually))) {

                MixpanelAnalytics.getInstance().onManualVerificationAttempt();
//                mActivateButton.setEnabled(false);
//
//                mActivateButton.setText(R.string.verifying_tag);

                callVerifyApi(mEnterCodeEdit.getText().toString(),SharedPreferenceHelper.getString(R.string.pref_mobile_number), UserInfo.INSTANCE.getDeviceId(),
                        AppConstants.PLATFORM, SharedPreferenceHelper.getString(R.string.pref_registration_id), true);

            } else if (mActivateButton.getText().equals(getResources().getString(R.string.send_sms))) {

                final String secondsMessage = getString(R.string.seconds_message);


                mSmsWaitTimer = new CountDownTimer(AppConstants.LOGIN_WAITING_TIME, 1000) {

                    public void onTick(long millisUntilFinished) {
                        mEnterCodeEdit.setVisibility(View.GONE);
                        //mActivateButton.setEnabled(false);
                        mReEnterButton.setVisibility(View.GONE);
                        //here you can have your logic to set text to edittext
                    }

                    public void onFinish() {
                        if (isAttached()) {
                            mEnterCodeEdit.setVisibility(View.VISIBLE);
                            mCopyrightText.setVisibility(View.GONE);
                            //mActivateButton.setEnabled(true);
                            mReEnterButton.setVisibility(View.VISIBLE);
                        }
                    }
                }.start();

                mVerifying = true;

                // mActivateButton.setEnabled(false);

                //mActivateButton.setText(R.string.verifying_tag);

                RetroCallback retroCallback;
                retroCallback = new RetroCallback(this);
                retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SERIAL_CODE);
                retroCallbackList.add(retroCallback);

                UserDetailsRequestModel userDetails = new UserDetailsRequestModel();
                userDetails.user.setMobile_number(mCountryCodeEdit.getText().toString() + mMobileNumber.getText().toString());


                mYeloApi.getSerialCode(userDetails, retroCallback);


            }

        } else if (v.getId() == R.id.button_verify) {
            MixpanelAnalytics.getInstance().onManualVerificationAttempt();
//                mActivateButton.setEnabled(false);
//
//                mActivateButton.setText(R.string.verifying_tag);

            callVerifyApi(mEnterCodeEdit.getText().toString(),SharedPreferenceHelper.getString(R.string.pref_mobile_number), UserInfo.INSTANCE.getDeviceId(),
                    AppConstants.PLATFORM, SharedPreferenceHelper.getString(R.string.pref_registration_id), true);
        } else if (v.getId() == R.id.re_enter_button) {
            mEnterCodeEdit.setVisibility(View.GONE);
            mActivateButton.setVisibility(View.VISIBLE);
            mVerifyManually.setVisibility(View.GONE);
            mActivateButton.setText(getResources().getString(R.string.activate_tag));
            //mActivateButton.setEnabled(true);
            mNumberHolderLayout.setVisibility(View.VISIBLE);
            mReEnterButton.setVisibility(View.GONE);
            mCopyrightText.setVisibility(View.GONE);
            mCountryCodeEdit.setVisibility(View.VISIBLE);
            mMobileNumber.setVisibility(View.VISIBLE);
            mCopyrightText.setVisibility(View.VISIBLE);
            mCountryCodeEdit.setEnabled(true);
            mMobileNumber.setEnabled(true);

            SharedPreferenceHelper.set(R.string.pref_is_verifying, false);

        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mFragmentPaused = true;
        cancelTimers();
        cancelAllCallbacks(retroCallbackList);
//        mTelephonyManager.listen(mCallStateListener, PhoneStateListener.LISTEN_NONE);

    }

    private void cancelTimers() {

        if (mSmsWaitTimer != null) {
            //mSmsWaitTimer.cancel();
        }
        if (mTextSwitchTimer != null) {
            mTextSwitchTimer.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mFragmentPaused = false;
        SharedPreferenceHelper.set(R.string.pref_received_sms, false);
        if (SharedPreferenceHelper.getString(R.string.pref_verify_status).equals(AppConstants.ALREADY_REGISTERED)) {

            if (mSmsWaitTimer != null) {
                mSmsWaitTimer.cancel();
            }

            SharedPreferenceHelper.set(R.string.pref_registration_screen, AppConstants.REGISTRATION_DONE);
            SharedPreferenceHelper.set(R.string.pref_is_verifying, false);

            SharedPreferenceHelper.set(R.string.pref_verify_status, "");
            getActivity().finish();
            final Intent homeActivityIntent = new Intent(getActivity(),
                    HomeActivity.class);
            startActivity(homeActivityIntent);
        } else if (SharedPreferenceHelper.getString(R.string.pref_verify_status).equals(AppConstants.NOT_REGISTERED)) {
            if (mSmsWaitTimer != null) {
                mSmsWaitTimer.cancel();
            }
            SharedPreferenceHelper.set(R.string.pref_verify_status, "");
            mBus.post(new PageNextPrevious(true, AppConstants.EDIT_PROFILE_SCREEN));
        } else {
            //do nothing
        }

    }


    /**
     * Loads the {@link red.yelo.fragments.HomeScreenFragment} into the fragment container
     */
    public void loadHomeScreen() {

        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(getActivity(), HomeScreenFragment.class
                                .getName(), null), AppConstants.FragmentTags.HOME_SCREEN, false,
                null
        );

    }


    //receives the message from the SmsReceiver through bus method
    @Subscribe
    public void onSmsReceivedEvent(SmsVerification event) {

        /**
         * removed auto verification because of privacy issues (google)
         */
//        if(!SharedPreferenceHelper.getBoolean(R.string.pref_received_sms,true)) {
//            SharedPreferenceHelper.set(R.string.pref_received_sms, true);
//            MixpanelAnalytics.getInstance().onVerificationSmsReceived(0);
//            callVerifyApi(event.sms, mCountryCodeEdit.getText().toString()
//                            + mMobileNumber.getText().toString(), UserInfo.INSTANCE.getDeviceId(),
//                    AppConstants.PLATFORM, SharedPreferenceHelper.getString(R.string.pref_registration_id), false);
//        }
        mSmsWaitTimer.cancel();
        mEnterCodeEdit.setVisibility(View.VISIBLE);
        mEnterCodeEdit.setText(event.sms);
        mActivateButton.setProgress(0);
        mActivateButton.setVisibility(View.GONE);
        //mActivateButton.setEnabled(true);
        mReEnterButton.setVisibility(View.VISIBLE);
        mCountryCodeEdit.setVisibility(View.GONE);
        mMobileNumber.setVisibility(View.GONE);
        mCountryCodeEdit.setEnabled(true);
        mMobileNumber.setEnabled(true);
        mCopyrightText.setVisibility(View.GONE);
        mActivateButton.setVisibility(View.GONE);
        mVerifyManually.setVisibility(View.VISIBLE);

    }

    @Subscribe
    public void openHomeActivity(HomeOpen event){
        getActivity().finish();
        final Intent homeActivityIntent = new Intent(getActivity(),
                HomeActivity.class);
        startActivity(homeActivityIntent);
    }

    private void callVerifyApi(String code, String number, String deviceId, String platform, String registrationId, boolean manual) {
        VerifyUserRequestModel verifyUserRequest = new VerifyUserRequestModel();

        verifyUserRequest.user.setSerial_code(code);
        verifyUserRequest.user.setMobile_number(number);
        verifyUserRequest.user.setEncrypt_device_id(deviceId);
        verifyUserRequest.user.setPlatform(platform);
        verifyUserRequest.user.setPush_id(registrationId);


        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(manual ? HttpConstants.ApiResponseCodes.VERIFY_SMS_MANUAL : HttpConstants.ApiResponseCodes.VERIFY_SMS);
        retroCallbackList.add(retroCallback);
        mYeloApi.verifyUserSerialCode(verifyUserRequest, retroCallback);

        mVerificationSent = true;
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

    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.ACTIVATE_LOGIN: {

                SerialKeyResponseModel serialKeyResponseModel = ((SerialKeyResponseModel) model);


                SharedPreferenceHelper.set(R.string.pref_is_verifying, true);



                mVerifying = true;
                //purposes
                //sendSmsMessage(mMobileNumber.getText().toString(), serialKeyResponseModel.serial_code);

                //mBus.post(new SmsVerification(serialKeyResponseModel.serial_code));
                break;
            }
            case HttpConstants.ApiResponseCodes.VERIFY_SMS_MANUAL:
            case HttpConstants.ApiResponseCodes.VERIFY_SMS: {


                VerifySmsResponseModel userResponseModel = ((VerifySmsResponseModel) model);
                UserInfo.INSTANCE.setAuthToken(userResponseModel.auth_token);
                UserInfo.INSTANCE.setMobileNumber(mMobileNumber.getText().toString());
                UserInfo.INSTANCE.setId(userResponseModel.id);

                SharedPreferenceHelper.set(R.string.pref_auth_token, userResponseModel.auth_token);
                SharedPreferenceHelper.set(R.string.pref_user_id, userResponseModel.id);
                SharedPreferenceHelper.set(R.string.pref_mobile_number, mCountryCodeEdit.getText().toString()
                        +mMobileNumber.getText().toString());
                SharedPreferenceHelper.set(R.string.pref_share_token, userResponseModel.share_token);
                if (mSmsWaitTimer != null) {
                    mSmsWaitTimer.cancel();
                }

                mBus.post(new RestAdapterUpdate(UserInfo.INSTANCE.getAuthToken()));


                Bundle args = new Bundle(1);

                args.putBoolean(AppConstants.Keys.FROM_LOGIN, true);
                if (mVerificationSent) {

                    if (mFragmentPaused) {

                    } else {

                        if (requestId == HttpConstants.ApiResponseCodes.VERIFY_SMS) {
                            MixpanelAnalytics.getInstance().onNumberVerified(VerificationMethod.SMS_AUTO);
                        } else if (requestId == HttpConstants.ApiResponseCodes.VERIFY_SMS_MANUAL) {
                            MixpanelAnalytics.getInstance().onNumberVerified(VerificationMethod.SMS_MANUAL);
                        }
                        if (!userResponseModel.is_present) {

                            MixpanelAnalytics.getInstance().setLoginId(userResponseModel.id, true);
                            mBus.post(new PageNextPrevious(true, AppConstants.EDIT_PROFILE_SCREEN));
                        } else {

                            MixpanelAnalytics.getInstance().setLoginId(userResponseModel.id, false);
                            SharedPreferenceHelper.set(R.string.pref_registration_screen, AppConstants.REGISTRATION_DONE);
                            SharedPreferenceHelper.set(R.string.pref_is_verifying, false);
                            //SharedPreferenceHelper.set(R.string.pref_first_name, userResponseModel.);

                            getActivity().finish();
                            final Intent homeActivityIntent = new Intent(getActivity(),
                                    HomeActivity.class);
                            startActivity(homeActivityIntent);

                        }
                        //TODO: Vinay - Add signup date here
                        MixpanelAnalytics.getInstance().identifyUser(userResponseModel.id, null);
                        getActivity().setProgressBarIndeterminateVisibility(false);
                    }
                }


                break;


            }
            case HttpConstants.ApiResponseCodes.GET_SERIAL_CODE: {
                hideKeyboard(mMobileNumber);
//
//                SerialKeyResponseModel serialKeyResponseModel = ((SerialKeyResponseModel) model);
//
//
//                mSerialCode = serialKeyResponseModel.serial_code;
//                String decrypted = "";
//
//                try {
//                    decrypted = AESEncrypter.decrypt("CA5DD1C5435131", "96DDFA2EAD8C06D3AB445E118DC191D7", mSerialCode.trim(), "yegd3281%12jhh2313@!");
//                    Logger.d(TAG, "decrypted " + decrypted);
//                    sendSmsMessage(mMobileNumber.getText().toString(), decrypted);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


                //purposes
                //sendSmsMessage(mMobileNumber.getText().toString(), serialKeyResponseModel.serial_code);

                //mBus.post(new SmsVerification(serialKeyResponseModel.serial_code));
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void failure(int requestId, int errorCode, String message) {
        SharedPreferenceHelper.set(R.string.pref_received_sms, false);
        mActivateButton.setText(getResources().getString(R.string.activate_tag));
        if(AppConstants.DeviceInfo.INSTANCE.isNetworkConnected()) {
            Toast.makeText(getActivity(), "Login unsuccessful. Please try again later", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_network_message), Toast.LENGTH_SHORT).show();
        }
        //mActivateButton.setProgress(-1);
        mActivateButton.setProgress(0);
        mCountryCodeEdit.setEnabled(true);
        mMobileNumber.setEnabled(true);
        mVerifying = false;
        //mActivateButton.setEnabled(true);
        if (mSmsWaitTimer != null) {
            mSmsWaitTimer.cancel();
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }


    /**
     * Class that provides the Views for the View Switcher
     */
    private static class SwitcherOptionsViewFactory implements ViewSwitcher.ViewFactory {

        private final Context mContext;

        public SwitcherOptionsViewFactory(final Context context) {
            mContext = context;
        }

        @Override
        public View makeView() {
            return LayoutInflater.from(mContext).inflate(R.layout.layout_home_text_switcher, null, false);
        }
    }

    /**
     * Listener to detect incoming calls.
     */
    private class CallStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    // called when someone is ringing to this phone

                    Toast.makeText(getActivity(),
                            "Incoming: " + incomingNumber,
                            Toast.LENGTH_LONG).show();
                    break;

            }
        }
    }

    private void saveDetailsInSharedPref() {

        SharedPreferenceHelper.set(R.string.pref_mobile_number, mCountryCodeEdit.getText().toString()+mMobileNumber.getText().toString());
        SharedPreferenceHelper.set(R.string.pref_country_code, mCountryCodeEdit.getText().toString());
    }


    @Subscribe
    public void verifyFinalCall(VerifySmsResponseModel userResponseModel) {

        mVerificationSent = true;
        UserInfo.INSTANCE.setAuthToken(userResponseModel.auth_token);
        UserInfo.INSTANCE.setMobileNumber(mCountryCodeEdit.getText().toString() + mMobileNumber.getText().toString());
        UserInfo.INSTANCE.setId(userResponseModel.id);
        if (mSmsWaitTimer != null) {
            mSmsWaitTimer.cancel();
        }

        SharedPreferenceHelper.set(R.string.pref_auth_token, userResponseModel.auth_token);
        SharedPreferenceHelper.set(R.string.pref_user_id, userResponseModel.id);

        mBus.post(new RestAdapterUpdate(UserInfo.INSTANCE.getAuthToken()));


        Bundle args = new Bundle(1);

        args.putBoolean(AppConstants.Keys.FROM_LOGIN, true);
        if (mVerificationSent) {

            if (mFragmentPaused) {

                int a = 0;
            } else {
                {
                    if (!userResponseModel.is_present) {

                        mBus.post(new PageNextPrevious(true, AppConstants.EDIT_PROFILE_SCREEN));
                    } else {

                        SharedPreferenceHelper.set(R.string.pref_registration_screen, AppConstants.REGISTRATION_DONE);
                        SharedPreferenceHelper.set(R.string.pref_is_verifying, false);

                        getActivity().finish();
                        final Intent homeActivityIntent = new Intent(getActivity(),
                                HomeActivity.class);
                        startActivity(homeActivityIntent);

                    }
                }
            }
        }
    }

    private void startClock() {

        hideKeyboard(mMobileNumber);

        final String secondsMessage = getString(R.string.seconds_message);

        mImageSlideTimer.cancel();
        tutorialSlideShow();
        mCountryCodeEdit.setEnabled(false);
        mMobileNumber.setEnabled(false);

        mSmsWaitTimer = new CountDownTimer(AppConstants.LOGIN_WAITING_TIME, 1000) {



            public void onTick(long millisUntilFinished) {
                if (isAttached()) {
                    mEnterCodeEdit.setVisibility(View.GONE);
                    mReEnterButton.setVisibility(View.GONE);
                    mActivateButton.setProgress(Math.round(Float.parseFloat((millisUntilFinished / 1000) + "")));

                }//here you can have your logic to set text to edittext
            }

            public void onFinish() {
//                if (isAttached()) {
//                    mTimeText.setVisibility(View.GONE);
//                    mActivateButton.setEnabled(true);
//                    mActivateButton.setText(getResources().getString(R.string.send_sms));
//                    mHelpTextView.setVisibility(View.VISIBLE);
//                    mMissCallText.setVisibility(View.GONE);
////                        mEnterCodeEdit.setVisibility(View.VISIBLE);
////                        mTimeText.setVisibility(View.GONE);
////                        mActivateButton.setText(getResources().getString(R.string.verify_manually));
////                        mActivateButton.setEnabled(true);
////                        mReEnterButton.setVisibility(View.VISIBLE);
//                }
                if (isAttached()) {
                    mEnterCodeEdit.setVisibility(View.VISIBLE);
                    mActivateButton.setProgress(0);
                    mActivateButton.setVisibility(View.GONE);
                    //mActivateButton.setEnabled(true);
                    mReEnterButton.setVisibility(View.VISIBLE);
                    mCountryCodeEdit.setVisibility(View.GONE);
                    mMobileNumber.setVisibility(View.GONE);
                    mCountryCodeEdit.setEnabled(true);
                    mMobileNumber.setEnabled(true);
                    mCopyrightText.setVisibility(View.GONE);
                    mActivateButton.setVisibility(View.GONE);
                    mVerifyManually.setVisibility(View.VISIBLE);
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        YeloApplication.stopVerificationService();
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {

        private int[] Images = new int[]{R.drawable.tutorial1, R.drawable.tutorial2,
                R.drawable.tutorial3
        };

        private String[] helpMessagees;


        public PagerAdapter(final FragmentManager fm) {
            super(fm);
            helpMessagees = getResources().getStringArray(R.array.helpmessage_array);

        }


        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case 0: {
                    return ImageViewFragment.newInstance(Images[position], helpMessagees[position]);
                }

                case 1: {

                    return ImageViewFragment.newInstance(Images[position], helpMessagees[position]);

                }


                case 2: {
                    return ImageViewFragment.newInstance(Images[position], helpMessagees[position]);

                }


                default: {
                    return null;
                }
            }
        }


        @Override
        public int getCount() {
            return Images.length;
        }

    }


    private void confirmPhoneNumberDialog(String mobileNumber) {


        boolean wrapInScrollView = true;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mCustomeDialogView = inflater
                .inflate(R.layout.layout_dialog_confirm_number, null);

        TextView mobileNumberText = (TextView) mCustomeDialogView.findViewById(R.id.mobilenumber);
        mobileNumberText.setText(mobileNumber);
        new MaterialDialog.Builder(getActivity())
                .title("Confirm number")
                .customView(mCustomeDialogView, wrapInScrollView)
                .positiveText("OK")
                .negativeText("EDIT")
                .positiveColor(getResources().getColor(R.color.blue_link))
                .negativeColor(getResources().getColor(R.color.blue_link))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);


//                            mVerifying = true;
//
//                            RetroCallback retroCallback;
//                            retroCallback = new RetroCallback(LoginFragment.this);
//                            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SERIAL_CODE);
//                            retroCallbackList.add(retroCallback);
//
//                            UserDetailsRequestModel userDetails = new UserDetailsRequestModel();
//                            userDetails.user.setMobile_number(mCountryCodeEdit.getText().toString() + mMobileNumber.getText().toString());
//
//
//                            mYeloApi.getSerialCode(userDetails, retroCallback);
                        UserDetailsRequestModel userDetails = new UserDetailsRequestModel();
                        userDetails.user.setMobile_number(mCountryCodeEdit.getText().toString() + mMobileNumber.getText().toString());

                        Logger.d(TAG, SharedPreferenceHelper.getString(R.string.pref_registration_id));

                        RetroCallback retroCallback;
                        retroCallback = new RetroCallback(LoginFragment.this);
                        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.ACTIVATE_LOGIN);
                        retroCallbackList.add(retroCallback);

                        mYeloApi.createUser(userDetails, retroCallback);

                        mVerifying = true;


                        startClock();


                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                })
                .build()
                .show();
    }

    private void tutorialSlideShow() {


        mImageSlideTimer = new CountDownTimer((3 + 1) * 3000, 3000) {

            @Override
            public void onTick(long millisUntilFinished) {
                mImageTutorialPager.setCurrentItem(indexSwitchImage, true);

                if (indexSwitchImage == 0) {
                    mImageTutorialPager.setCurrentItem(0, true);
                    indexSwitchImage = 1;
                    mReverse = false;
                } else if (indexSwitchImage == 1) {
                    if (mReverse) {
                        indexSwitchImage = 0;
                    } else {
                        indexSwitchImage = 2;
                    }
                    mImageTutorialPager.setCurrentItem(1, true);

                } else if (indexSwitchImage == 2) {
                    mImageTutorialPager.setCurrentItem(2, true);
                    indexSwitchImage = 1;
                    mReverse = true;
                }

//                indexSwitchImage = (indexSwitchImage + 1) % 3;
//                if(indexSwitchImage ==2){
//
//                }
            }

            @Override
            public void onFinish() {
                start();
            }
        };
        Logger.d(TAG, "Start");
        mImageSlideTimer.start();
//
//        mImageSlideRunnable= new Runnable() {
//            @Override
//            public void run() {
//                mImageHandler.postDelayed(this, 3000);
//                if(mCurrentTutorialImage==0) {
//                    mImageTutorialPager.setCurrentItem(0,true);
//                    mCurrentTutorialImage=1;
//                }
//                else if(mCurrentTutorialImage==1){
//                    mImageTutorialPager.setCurrentItem(1,true);
//                    mCurrentTutorialImage = 2;
//                }
//                else if(mCurrentTutorialImage==2){
//                    mImageTutorialPager.setCurrentItem(2, true);
//                    mCurrentTutorialImage=0;
//                }
//            }
//        };
//        mImageHandler.postDelayed(mImageSlideRunnable , 3000);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mImageTutorialPager.setAdapter(mPagerdapter);

    }
}
