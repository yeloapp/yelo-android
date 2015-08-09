
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
 */package red.yelo.verification;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.activities.HomeActivity;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.analytics.VerificationMethod;
import red.yelo.bus.HomeOpen;
import red.yelo.bus.PageNextPrevious;
import red.yelo.bus.RestAdapterUpdate;
import red.yelo.bus.SmsVerification;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.request.VerifyUserRequestModel;
import red.yelo.retromodels.response.VerifySmsResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.UserInfo;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@Deprecated
public class VerificationService extends Service implements Callback {
    private static final String TAG = "VerificationService";

    private Bus mBus;
    private final IBinder mVerificationServiceBinder = new VerificationServiceBinder();
    private CallStateListener mCallStateListener;
    private TelephonyManager mTelephonyManager;
    private boolean mRing;
    private String mIncomingNumber;


    @Override
    public void onCreate() {
        super.onCreate();

        mBus = ((YeloApplication) getApplication().getApplicationContext()).getBus();
        mBus.register(this);
        mTelephonyManager = (TelephonyManager) (getApplication().getApplicationContext())
                .getSystemService(Context.TELEPHONY_SERVICE);
        mCallStateListener = new CallStateListener();
        mTelephonyManager.listen(mCallStateListener, PhoneStateListener.LISTEN_CALL_STATE);

    }


    @Override
    public void success(Object o, Response response) {


        MixpanelAnalytics.getInstance().onNumberVerified(VerificationMethod.SMS_AUTO);
        VerifySmsResponseModel userResponseModel = ((VerifySmsResponseModel) o);


        mBus.post(userResponseModel);
        UserInfo.INSTANCE.setAuthToken(userResponseModel.auth_token);
        UserInfo.INSTANCE.setMobileNumber(SharedPreferenceHelper.getString(R.string.pref_mobile_number));
        UserInfo.INSTANCE.setId(userResponseModel.id);
        Logger.d(TAG, userResponseModel.auth_token + "");

        SharedPreferenceHelper.set(R.string.pref_auth_token, userResponseModel.auth_token);
        SharedPreferenceHelper.set(R.string.pref_user_id, userResponseModel.id);
        SharedPreferenceHelper.set(R.string.pref_share_token, userResponseModel.share_token);


        mBus.post(new RestAdapterUpdate(UserInfo.INSTANCE.getAuthToken()));


        Bundle args = new Bundle(1);

        args.putBoolean(AppConstants.Keys.FROM_LOGIN, true);

        if (!userResponseModel.is_present) {

            MixpanelAnalytics.getInstance().setLoginId(userResponseModel.id, true);
            SharedPreferenceHelper.set(R.string.pref_verify_status, AppConstants.NOT_REGISTERED);
            mBus.post(new PageNextPrevious(true, AppConstants.EDIT_PROFILE_SCREEN));
        } else {
            MixpanelAnalytics.getInstance().setLoginId(userResponseModel.id, false);
            SharedPreferenceHelper.set(R.string.pref_verify_status, AppConstants.ALREADY_REGISTERED);
            SharedPreferenceHelper.set(R.string.pref_registration_screen, AppConstants.REGISTRATION_DONE);
            SharedPreferenceHelper.set(R.string.pref_is_verifying, false);

        }
        //TODO: Vinay - Add signup date here
        MixpanelAnalytics.getInstance().identifyUser(userResponseModel.id, null);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mCallStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    @Override
    public void failure(RetrofitError error) {

    }

    /**
     * Binder to connect to the Verification Service
     */
    public class VerificationServiceBinder extends Binder {

        public VerificationService getService() {
            return VerificationService.this;
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mVerificationServiceBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {


        return START_NOT_STICKY;
    }


    /**
     * Check if user is logged in or not
     */
    private boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getAuthToken());
    }


    private void callVerifyApi(String callnumber, String number, String deviceId, String platform, String registrationId) {
        VerifyUserRequestModel verifyUserRequest = new VerifyUserRequestModel();

        verifyUserRequest.user.setMissed_call_number(callnumber);
        verifyUserRequest.user.setMobile_number(number);
        verifyUserRequest.user.setEncrypt_device_id(deviceId);
        verifyUserRequest.user.setPlatform(platform);
        verifyUserRequest.user.setPush_id(registrationId);

        ((YeloApplication) getApplicationContext()).getYeloApi().verifyUserPhoneNumber(verifyUserRequest, this);

    }

    //receives the message from the SmsReceiver through bus method
    @Subscribe
    public void onSmsReceivedEvent(SmsVerification event) {


        if (!SharedPreferenceHelper.getBoolean(R.string.pref_received_sms, true)) {
            SharedPreferenceHelper.set(R.string.pref_received_sms, true);
            MixpanelAnalytics.getInstance().onVerificationSmsReceived(0);
            callVerifyApi(event.sms, SharedPreferenceHelper.getString(R.string.pref_mobile_number),
                    UserInfo.INSTANCE.getDeviceId(),
                    AppConstants.PLATFORM, SharedPreferenceHelper.getString(R.string.pref_registration_id), false);
        }

    }

    private void callVerifyApi(String code, String number, String deviceId, String platform, String registrationId, boolean manual) {
        VerifyUserRequestModel verifyUserRequest = new VerifyUserRequestModel();

        verifyUserRequest.user.setSerial_code(code);
        verifyUserRequest.user.setMobile_number(number);
        verifyUserRequest.user.setEncrypt_device_id(deviceId);
        verifyUserRequest.user.setPlatform(platform);
        verifyUserRequest.user.setPush_id(registrationId);//480429


        ((YeloApplication) getApplicationContext()).getYeloApi().verifyUserSerialCode(verifyUserRequest, new Callback<VerifySmsResponseModel>() {
            @Override
            public void success(VerifySmsResponseModel verifySmsResponseModel, Response response) {

                VerifySmsResponseModel userResponseModel = verifySmsResponseModel;
                UserInfo.INSTANCE.setAuthToken(userResponseModel.auth_token);
                UserInfo.INSTANCE.setMobileNumber(SharedPreferenceHelper.getString(R.string.pref_mobile_number));
                UserInfo.INSTANCE.setId(userResponseModel.id);

                SharedPreferenceHelper.set(R.string.pref_auth_token, userResponseModel.auth_token);
                SharedPreferenceHelper.set(R.string.pref_user_id, userResponseModel.id);
                SharedPreferenceHelper.set(R.string.pref_mobile_number, SharedPreferenceHelper.getString(R.string.pref_mobile_number));
                SharedPreferenceHelper.set(R.string.pref_share_token, userResponseModel.share_token);

                mBus.post(new RestAdapterUpdate(UserInfo.INSTANCE.getAuthToken()));

                if (!userResponseModel.is_present) {

                    MixpanelAnalytics.getInstance().setLoginId(userResponseModel.id, true);
                    SharedPreferenceHelper.set(R.string.pref_verify_status, AppConstants.NOT_REGISTERED);
                    mBus.post(new PageNextPrevious(true, AppConstants.EDIT_PROFILE_SCREEN));
                } else {
                    MixpanelAnalytics.getInstance().setLoginId(userResponseModel.id, false);
                    SharedPreferenceHelper.set(R.string.pref_verify_status, AppConstants.ALREADY_REGISTERED);
                    SharedPreferenceHelper.set(R.string.pref_registration_screen, AppConstants.REGISTRATION_DONE);
                    SharedPreferenceHelper.set(R.string.pref_is_verifying, false);
                    mBus.post(new HomeOpen(true));

                }


                //TODO: Vinay - Add signup date here
                MixpanelAnalytics.getInstance().identifyUser(userResponseModel.id, null);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

    }

    public Object getTaskTag() {
        return hashCode();
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
                    mIncomingNumber = incomingNumber;
                    Logger.d(TAG, mIncomingNumber);
                    mRing = true;

                    break;

                case TelephonyManager.CALL_STATE_IDLE: {

                    if (mRing) {
                        Logger.d(TAG, mIncomingNumber);
                        callVerifyApi(mIncomingNumber, SharedPreferenceHelper.getString(R.string.pref_mobile_number),
                                SharedPreferenceHelper.getString(R.string.pref_device_id), AppConstants.PLATFORM,
                                SharedPreferenceHelper.getString(R.string.pref_registration_id));
                    }
                    break;
                }

            }
        }
    }


}
