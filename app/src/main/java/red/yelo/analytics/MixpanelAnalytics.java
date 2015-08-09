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
 */package red.yelo.analytics;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONObject;

import red.yelo.BuildConfig;
import red.yelo.R;
import red.yelo.YeloApplication;

/**
 * Class that contains helper methods for Mixpanel
 * <p/>
 * Created by vinaysshenoy on 02/10/14.
 */
public class MixpanelAnalytics {

    private static final String TAG = "MixpanelAnalytics";

    private static final boolean ENABLED = BuildConfig.ENABLE_MIXPANEL;

    private static MixpanelAnalytics sInstance;

    private MixpanelAPI mMixpanelAPI;

    private PropsGenerator mPropsGenerator;

    private MixpanelAnalytics() {

        if (ENABLED) {
            final String projectToken = YeloApplication.getStaticContext().getString(R.string.mixpanel_api_key);
            mMixpanelAPI = MixpanelAPI.getInstance(YeloApplication.getStaticContext(), projectToken);
            mPropsGenerator = new PropsGenerator();
        }

    }

    /**
     * Retrieve an instance of the Mixpanel analytics for this application
     */
    public static MixpanelAnalytics getInstance() {

        if (sInstance == null) {
            synchronized (MixpanelAnalytics.class) {
                if (sInstance == null) {
                    sInstance = new MixpanelAnalytics();
                }
            }
        }

        return sInstance;
    }

    /**
     * Method to be called to set the user id on login/signup
     *
     * @param userId      The account id of the user
     * @param isNewSignUp {@code true} if the logged in user is a completely new user, {@code false} if the user has logged in previously
     */
    public void setLoginId(final String userId, final boolean isNewSignUp) {

        if (ENABLED) {

            if (isNewSignUp) {
                mMixpanelAPI.alias(userId, null);
            } else {
                mMixpanelAPI.identify(userId);
            }
        }
    }

    /**
     * Method to be called to create a User profile. Call it after login AFTER calling {@link #setLoginId(String, boolean)}
     *
     * @param userId     The account id of the user
     * @param signupDate The date on which the user signed up. The format should be <b>YYYY-MM-DDTHH:MM:SS</b>
     */
    public void identifyUser(final String userId, final String signupDate) {

        if (ENABLED) {
            mMixpanelAPI.getPeople().identify(userId);
            mMixpanelAPI.getPeople().setOnce("Signup Date", signupDate);
        }
    }

    /**
     * Updates the name for this user
     *
     * @param name The name of the user
     */
    public void nameUser(final String name) {

        if(ENABLED) {
            mMixpanelAPI.getPeople().set("$first_name", name);
            mMixpanelAPI.getPeople().set("Name", name);
        }
    }

    /**
     * Sets the referral info for the user. Be <b>VERY</b> careful when you call this.
     * <p/>
     * Once this is set, it cannot be changed unless the app data is cleared
     *
     * @param utmSource   The referral source
     * @param utmCampaign The referral campaign
     * @param utmMedium   The medium of the referral
     * @param utmContent  The referral content
     * @param utmTerm     The referral term
     */
    public void setReferralInfo(final String utmSource, final String utmCampaign, final String utmMedium, final String utmContent, final String utmTerm) {

        if (ENABLED) {

            final JSONObject props = mPropsGenerator.makeReferralInfoProps(utmSource, utmCampaign, utmMedium, utmContent, utmTerm);
            if (props != null) {
                mMixpanelAPI.registerSuperPropertiesOnce(props);
            }
        }
    }

    /**
     * Attempt to send all stored events. Call this in the onDestroy()
     * of the Main activity
     */
    public void flush() {

        if (ENABLED) {
            mMixpanelAPI.flush();
        }
    }

    /**
     * Send the event for the very first launch. Do not call this on subsequent launches.
     * <p/>
     * It is <b>IMPERATIVE</b> to call {@link #setReferralInfo(String, String, String, String, String)} first before calling this to ensure
     * that referral info is associated with the first launch of the app
     */
    public void onFirstLaunch() {

        if (ENABLED) {
            trackOpt("First Launch", null);
        }
    }

    /**
     * Send the event that a Session has ended
     *
     * @param sessionDurationSeconds The duration of the session, in seconds
     */
    public void onSessionEnded(final long sessionDurationSeconds) {

        if (ENABLED) {
            track("Session Ended", mPropsGenerator.makeDurationProps(sessionDurationSeconds));
        }
    }

    /**
     * Send the event that the missed call verification was started
     */
    public void onMissedCallVerificationStarted() {

        if (ENABLED) {
            trackOpt("Started Missed Call Verification", null);
        }

    }

    /**
     * Send the event that a SMS was sent to verify the number
     */
    public void onVerificationSmsSent() {

        if (ENABLED) {
            trackOpt("Verification SMS Sent", null);
        }
    }

    /**
     * Send the event that the number was verified
     */
    public void onNumberVerified(final VerificationMethod verificationMethod) {

        if (ENABLED) {
            track("Number Verified", mPropsGenerator.makeNumberVerifiedProps(verificationMethod));
        }
    }
    /**
     * Send the event when chat button is clicked
     */
    public void onChatClicked() {

        if (ENABLED) {
            trackOpt("Chat Clicked On Post", null);
        }
    }

    /**
     * Send the event when refer button is clicked
     */
    public void onReferClicked() {

        if (ENABLED) {
            trackOpt("Refer Clicked On Post", null);
        }
    }
    /**
     * Send the event when wall post is opened
     */
    public void onWallOpened() {

        if (ENABLED) {
            trackOpt("When the post is opened", null);
        }
    }
    /**
     * Send the event when chat message is sent
     */
    public void onChatMessageSent() {

        if (ENABLED) {
            trackOpt("Message Sent On Chat Screen", null);
        }
    }

    /**
     * Send the event when a contact is referred
     */
    public void onContactReferred() {

        if (ENABLED) {
            trackOpt("Contact Referred", null);
        }
    }

    /**
     * Send the event that the user chose to verify manually
     */
    public void onManualVerificationAttempt() {

        if (ENABLED) {
            trackOpt("Manual Verification Attempt", null);
        }
    }

    /**
     * Send the event that a verification SMS was received
     *
     * @param smsReceiveInterval The duration(in seconds) it took to receive the verification SMS after sending the first one
     */
    public void onVerificationSmsReceived(final int smsReceiveInterval) {

        if (ENABLED) {
            track("Verification SMS Received", mPropsGenerator.makeVerificationSmsReceivedProps(smsReceiveInterval));
        }
    }

    /**
     * Send the event that a user created a post
     *
     * @param category   The category of the post
     * @param attachment Whatever attachement is added to the post
     * @param postLength The total length of the post
     */
    public void onPostCreated(final String category, final Attachment attachment, final int postLength) {

        if (ENABLED) {
            track("Problem Posted", mPropsGenerator.makeProblemPostedProps(category, attachment, postLength));
        }
    }

    /**
     * Send the event that a user added profile info
     *
     * @param profileMethod How the user chose to complete his profile
     */
    public void onProfileInfoAdded(final ProfileMethod profileMethod) {

        if (ENABLED) {
            track("Profile Info Added", mPropsGenerator.makeProfileMethodProps(profileMethod));
        }
    }

    /**
     * Track an event with a set of properties
     *
     * @param event The event to send
     * @param props The properties to send. If this is {@code null}, the event is not tracked
     */
    private void track(final String event, final JSONObject props) {

        if (props != null) {
            trackOpt(event, props);
        }
    }

    /**
     * Track an event, with an optional set of properties
     *
     * @param event The event to send
     * @param props The properties to send. If this is {@code null}, the event is still tracked
     */
    private void trackOpt(final String event, final JSONObject props) {

        if (ENABLED) {
            mMixpanelAPI.track(event, props);
        }
    }

}
