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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import red.yelo.utils.Logger;

/**
 * Props generator for Mixpanel
 * <p/>
 * Created by vinaysshenoy on 03/10/14.
 */
public class PropsGenerator {

    private static final String TAG = "PropsGenerator";

    /**
     * Create a JsonObject props for the user profile method
     *
     * @param profileMethod The user's profileMethod
     */
    public JSONObject makeProfileMethodProps(final ProfileMethod profileMethod) {

        JSONObject props;

        try {
            props = new JSONObject();
            props.put("Via", profileMethod.code);

        } catch (JSONException e) {
            Logger.e(TAG, "Error building Mixpanel Props", e);
            props = null;
        }

        return props;
    }

    /**
     * Create a JsonObject props for the referral info
     *
     * @param utmSource   The referral source
     * @param utmCampaign The referral campaign
     * @param utmMedium   The medium of the referral
     * @param utmContent  The referral content
     * @param utmTerm     The referral term
     */
    public JSONObject makeReferralInfoProps(final String utmSource, final String utmCampaign, final String utmMedium, final String utmContent, final String utmTerm) {

        JSONObject props;

        try {
            props = new JSONObject();
            props.put("Referral Source", utmSource);
            props.put("Referral Campaign", utmCampaign);
            props.put("Referral Medium", utmMedium);
            props.put("Referral Campaign Content", utmContent);
            props.put("Referral Term", utmTerm);

        } catch (JSONException e) {
            Logger.e(TAG, "Error building Mixpanel Props", e);
            props = null;
        }

        return props;
    }


    /**
     * Create a JsonObject props for the session duration
     *
     * @param sessionDurationSeconds The duration of the session, in seconds
     */
    public JSONObject makeDurationProps(final long sessionDurationSeconds) {

        JSONObject props;

        try {
            props = new JSONObject();
            props.put("Duration", sessionDurationSeconds);

        } catch (JSONException e) {
            Logger.e(TAG, "Error building Mixpanel Props", e);
            props = null;
        }

        return props;
    }

    /**
     * Create a JsonObject props for the verify SMS event
     *
     * @param smsReceiveInterval The time(in seconds) it took to receive the verification SMS after sending the first SMS
     */
    public JSONObject makeVerificationSmsReceivedProps(final int smsReceiveInterval) {

        JSONObject props;

        try {
            props = new JSONObject();
            props.put("Delay", smsReceiveInterval);

        } catch (JSONException e) {
            Logger.e(TAG, "Error building Mixpanel Props", e);
            props = null;
        }

        return props;
    }

    /**
     * Create a Props object for a problem posted event
     *
     * @param category   The category of the post
     * @param attachment Whatever attachement is added to the post
     * @param postLength The total length of the post
     */
    public JSONObject makeProblemPostedProps(final String category, final Attachment attachment, final int postLength) {

        JSONObject props;

        try {
            props = new JSONObject();
            props.put("Category", category);
            props.put("Attachment", attachment.code);
            props.put("Length", postLength);

        } catch (JSONException e) {
            Logger.e(TAG, "Error building Mixpanel Props", e);
            props = null;
        }

        return props;
    }

    /**
     * Create a Props object for a connection happening through a post
     *
     * @param postCity   The City associated with the post
     * @param category   The category of the post
     * @param attachment Whatever attachment is added to the post
     * @param postLength The total length of the post
     */
    public JSONObject makeConnectedViaPostProps(final String postCity, final String category, final Attachment attachment, final int postLength) {

        JSONObject props;

        try {
            props = new JSONObject();
            props.put("Post City", postCity);
            props.put("Category", category);
            props.put("Attachment", attachment);
            props.put("Length", postLength);

        } catch (JSONException e) {
            Logger.e(TAG, "Error building Mixpanel Props", e);
            props = null;
        }

        return props;
    }

    /**
     * Create a Props object for a connection happening through a post
     *
     * @param connectionDirection The direction of the connection
     * @param postCity            The City associated with the post
     * @param category            The category of the post
     * @param attachment          Whatever attachment is added to the post
     * @param postLength          The total length of the post
     */
    public JSONObject makeConnectedViaPostProps(final ConnectionDirection connectionDirection, final String postCity, final String category, final Attachment attachment, final int postLength) {

        JSONObject props;

        try {
            props = new JSONObject();
            props.put("Direction", connectionDirection.code);
            props.put("Post City", postCity);
            props.put("Category", category);
            props.put("Attachment", attachment);
            props.put("Length", postLength);

        } catch (JSONException e) {
            Logger.e(TAG, "Error building Mixpanel Props", e);
            props = null;
        }

        return props;
    }

    /**
     * Create a Props object for a problem getting solved
     *
     * @param problemSolved       Whether the problem was solved or not
     * @param numberOfConnections The total number of connections on the post
     * @param numberOfTags        The total number of tags on the post
     * @param postCity            The City associated with the post
     * @param category            The category of the post
     * @param attachment          Whatever attachment is added to the post
     * @param postLength          The total length of the post
     */
    public JSONObject makeConnectedViaPostProps(final boolean problemSolved, final int numberOfConnections, final int numberOfTags, final String postCity, final String category, final Attachment attachment, final int postLength) {

        JSONObject props;

        try {
            props = new JSONObject();
            props.put("Solved", problemSolved ? "Yes" : "No");
            props.put("Number Of Connections", numberOfConnections);
            props.put("Number Of Tags", numberOfTags);
            props.put("Post City", postCity);
            props.put("Category", category);
            props.put("Attachment", attachment);
            props.put("Length", postLength);

        } catch (JSONException e) {
            Logger.e(TAG, "Error building Mixpanel Props", e);
            props = null;
        }

        return props;
    }

    /**
     * Converts an array of strings to a JSONArray
     *
     * @param strings The list of strings to convert
     * @return A JSONArray with the strings
     */
    private JSONArray arrayFromStrings(List<String> strings) {

        final JSONArray array = new JSONArray();

        if (strings != null) {
            for (final String eachString : strings) {
                array.put(eachString);
            }
        }

        return array;
    }

    /**
     * Create a Props object for number verified
     *
     * @param verificationMethod The method how the number was verified
     */
    public JSONObject makeNumberVerifiedProps(VerificationMethod verificationMethod) {

        JSONObject props;

        try {
            props = new JSONObject();
            props.put("Verification Method", verificationMethod.code);

        } catch (JSONException e) {
            Logger.e(TAG, "Error building Mixpanel Props", e);
            props = null;
        }

        return props;
    }
}
