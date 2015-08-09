
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import red.yelo.R;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;


/**
 * Class that receives campaign tracking intents and broadcasts them
 */
public class CampaignTrackingReceiver extends BroadcastReceiver {

    private static final String TAG = "CampaignTrackingReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        final String referrer = intent.getStringExtra("referrer");
        Logger.v(TAG, "Campaign Referrer is %s", referrer);
        if(!TextUtils.isEmpty(referrer)) {
            //Store referrer in SharedPreferences for upload later
            SharedPreferenceHelper.set(R.string.pref_referrer, referrer);
        }
//        if (!TextUtils.isEmpty(referrer)) {
//
//            String decodedReferrer = null;
//            try {
//                decodedReferrer = URLDecoder.decode(referrer, HTTP.UTF_8);
//            } catch (UnsupportedEncodingException e) {
//                Logger.e(TAG, "Unable to decode URL", e);
//            }
//
//            if (!TextUtils.isEmpty(decodedReferrer)) {
//
//                parseAndStoreReferralParams(context, decodedReferrer);
//            }
//
//        }

        /*
        * Forward the referrer to Google Analytics so we get tracking info in Google Analytics
        * */
        final com.google.android.gms.analytics.CampaignTrackingReceiver googleAnalyticsCampaignTrackingReceiver = new com.google.android.gms.analytics.CampaignTrackingReceiver();
        googleAnalyticsCampaignTrackingReceiver.onReceive(context, intent);
    }

    /**
     * Takes in the decoded referral string, splits it into the individual parts,
     * and saves it in the Shared Preferences
     *
     * @param context         A reference to the app context
     * @param decodedReferrer The decoded referrer
     */
    private void parseAndStoreReferralParams(Context context, String decodedReferrer) {

        final String[] referralParts = decodedReferrer.split("&");

        if (referralParts.length > 0) {
            // Read out the individual components

            // The referral param key
            String key;

            // The referral param value
            String value;

            // The position of the '=' character to read out the campaign parameters
            int positionOfEquals;

            // The key for which to save the campaign parameter into the shared preference
            int sharedPreferenceKey;

            for (final String eachPart : referralParts) {

                positionOfEquals = eachPart.indexOf('=');

                //No sense in saving it if there are no key-value pairs
                if (positionOfEquals >= 1) {

                    key = eachPart.substring(0, positionOfEquals + 1);
                    value = eachPart.substring(positionOfEquals + 1, eachPart.length());

                    if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {

                        if("share_token".equals(key)) {
                            //Store referrer in SharedPreferences for upload later
                            SharedPreferenceHelper.set(R.string.pref_referrer, value);
                        } else {
                            //Save the value to the SharedPreferences
                            sharedPreferenceKey = getSharedPreferenceKeyForCampaignParameter(key);

                            if (sharedPreferenceKey != 0) {
                                SharedPreferenceHelper.set(sharedPreferenceKey, value);
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * Returns the preference key for a particular campaign parameter
     *
     * @param key The parameter key
     * @return The string resource id for the parameter
     */
    private int getSharedPreferenceKeyForCampaignParameter(final String key) {

        if ("utm_source".equals(key)) {
            return R.string.pref_utm_source;
        } else if ("utm_campaign".equals(key)) {
            return R.string.pref_utm_campaign;
        } else if ("utm_medium".equals(key)) {
            return R.string.pref_utm_medium;
        } else if ("utm_content".equals(key)) {
            return R.string.pref_utm_content;
        } else if ("utm_term".equals(key)) {
            return R.string.pref_utm_term;
        } else {
            return 0;
        }
    }

}
