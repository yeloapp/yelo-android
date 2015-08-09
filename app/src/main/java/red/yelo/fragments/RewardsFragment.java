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

package red.yelo.fragments;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.internal.ck;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import red.yelo.R;
import red.yelo.activities.ClaimTableActivity;
import red.yelo.activities.SearchLocationActivity;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.TableClaims;
import red.yelo.data.TableWallPosts;
import red.yelo.http.HttpConstants;
import red.yelo.http.HttpConstants.ApiResponseCodes;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.ClaimRewardsModel;
import red.yelo.retromodels.ReferralScoreModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;


/**
 * @author Sharath Pandeshwar
 * @since 15/07/14
 * <p/>
 * Controller responsible for handling Rewards for users of Yelo
 */
public class RewardsFragment extends AbstractYeloFragment implements View.OnClickListener,
        RetroCallback.RetroResponseListener, DBInterface.AsyncDbQueryCallback {

    public static final String TAG = "RewardsFragment";

    private TextView mCreditTextView, mClaimStatusTextView,mDescriptionText;
    private Button mInviteButton;
    private Button mClaimButton;

    private Toolbar mToolbar;

    private ProgressBar mProgressWheel;

    private int mMinimumClaim;

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    /**
     * Date formatter for formatting timestamps for messages
     */
    private DateFormatter mMessageDateFormatter;

    //*******************************************************************
    // Life Cycle Related Functions
    //*******************************************************************


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View contentView = inflater.inflate(R.layout.fragment_rewards, container, false);
        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        setToolbar(mToolbar);

        Bundle extras;
        extras = getArguments();

        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                AppConstants.WALL_DATE_FORMAT);

        initializeViews(contentView, savedInstanceState);
        fetchReferralPointsfromServer();

        return contentView;
    }


    @Override
    public void onPause() {
        super.onPause();
        mProgressWheel.setVisibility(View.GONE);
        for (RetroCallback aRetroCallbackList : retroCallbackList) {
            if (aRetroCallbackList.getRequestId() != HttpConstants.ApiResponseCodes.CREATE_WALL)
                aRetroCallbackList.cancel();
        }
    }

    //*******************************************************************
    // View Related Functions
    //*******************************************************************


    private void initializeViews(View v, Bundle savedInstanceState) {
        mCreditTextView = (TextView) v.findViewById(R.id.id_text_credit_balance);
        mInviteButton = (Button) v.findViewById(R.id.id_button_invite);
        mClaimButton = (Button) v.findViewById(R.id.id_button_claim);
        mClaimStatusTextView = (TextView) v.findViewById(R.id.id_text_status);
        mDescriptionText = (TextView) v.findViewById(R.id.id_text_description);

        mInviteButton.setOnClickListener(this);
        mProgressWheel = (ProgressBar) v.findViewById(R.id.progress_wheel);

        Long lastFetchedReferralPoint = SharedPreferenceHelper.getLong(R.string.pref_last_fetched_rewards_post, 0L);
        int processedCount = SharedPreferenceHelper.getInt(R.string.pref_last_fetch_processed_count, 0);
        int processingCount = SharedPreferenceHelper.getInt(R.string.pref_last_fetch_processing_count, 0);
        mMinimumClaim = SharedPreferenceHelper.getInt(R.string.pref_minimum_claim,50);


        mCreditTextView.setText("\u20B9" + Long.toString(lastFetchedReferralPoint));

        mClaimStatusTextView.setText("Money in process: ₹" + processingCount * mMinimumClaim);

        mClaimStatusTextView.setOnClickListener(this);

        if (lastFetchedReferralPoint >= mMinimumClaim) {
            enableClaimButton();
        } else {
            disableClaimButton();
        }
    }


    private void disableClaimButton() {
        Logger.i(TAG, "%s", "Disabling Claim Button");
        mClaimButton.setEnabled(false);
        mClaimButton.setOnClickListener(null);
        mClaimButton.setBackgroundColor(getResources().getColor(R.color.disabled_green));
    }


    private void enableClaimButton() {
        Logger.i(TAG, "%s", "Enabling Claim Button");
        mClaimButton.setEnabled(true);
        mClaimButton.setOnClickListener(this);
        mClaimButton.setBackgroundResource(R.drawable.selector_button_done);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.id_button_invite) {
            handleShare();
        } else if (v.getId() == R.id.id_button_claim) {
            claimRewardsFromServer();
        } else if (v.getId() == R.id.id_text_status) {
            final Intent ClaimsTableActivity = new Intent(getActivity(),
                    ClaimTableActivity.class);
            startActivity(ClaimsTableActivity);
        }
    }

    //*******************************************************************
    // Menu Related Functions
    //*******************************************************************


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.rewards_options, menu);
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                getActivity().finish();
                return true;
            }

            case R.id.action_refresh: {
                fetchReferralPointsfromServer();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    //*******************************************************************
    // Data Related Functions
    //*******************************************************************

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {
        if (taskId == AppConstants.QueryTokens.UPDATE_CLAIMS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_CLAIMS, getTaskTag(), null
                        , TableClaims.NAME, null, values, true, this);
            }
        }
    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }



    //*******************************************************************
    // Http Related Functions
    //*******************************************************************


    private void fetchReferralPointsfromServer() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_REFERRAL_SCORE);
        retroCallbackList.add(retroCallback);
        mProgressWheel.setVisibility(View.VISIBLE);
        mCreditTextView.setVisibility(View.INVISIBLE);
        mYeloApi.getReferralScore(retroCallback);
    }


    private void claimRewardsFromServer() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(ApiResponseCodes.CLAIM_REWARD);
        retroCallbackList.add(retroCallback);
        mProgressWheel.setVisibility(View.VISIBLE);
        mCreditTextView.setVisibility(View.INVISIBLE);
        mYeloApi.claimReward(retroCallback);
    }


    /**
     * Method callback when the success response is received
     *
     * @param model     model response received from the server
     * @param requestId The id of the response
     */
    @Override
    public void success(Object model, int requestId) {
        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_REFERRAL_SCORE: {
                mProgressWheel.setVisibility(View.GONE);
                mCreditTextView.setVisibility(View.VISIBLE);
                ReferralScoreModel referralScoreModel = ((ReferralScoreModel) model);

                SharedPreferenceHelper.set(R.string.pref_minimum_claim,referralScoreModel.minumumClaim);

                mMinimumClaim = referralScoreModel.minumumClaim;

                String descriptionText = String.format(Locale.US,
                        getResources().getString(R.string.text_rewards_description), referralScoreModel.minumumClaim/5, referralScoreModel.minumumClaim);
                mDescriptionText.setText(descriptionText);


                /**
                 * TODO: Remove dependency on hard coded indian currency string
                 */


                mCreditTextView.setText("\u20B9" + Long.toString(referralScoreModel.getScore()));

                int processingCount = 0, processedCount = 0;
                if (referralScoreModel.claims.size() > 0) {
                    for (ClaimRewardsModel.Claim claim : referralScoreModel.claims) {

                        ContentValues values = new ContentValues();

                        values.put(DatabaseColumns.ID, claim.id);
                        values.put(DatabaseColumns.AMOUNT, claim.amount);
                        values.put(DatabaseColumns.STATUS, claim.status);
                        values.put(DatabaseColumns.DATE_TIME, claim.createdAt);

                        try {
                            values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(claim.createdAt));
                            values.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(claim.createdAt));
                            values.put(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, mMessageDateFormatter.getEpoch(claim.updatedAt));
                            values.put(DatabaseColumns.TIMESTAMP_HUMAN_UPDATED_AT, mMessageDateFormatter.getOutputTimestamp(claim.updatedAt));

                        } catch (ParseException e) {
                            e.printStackTrace();
                            //should not happen
                        }


                        if (claim.status.equals(ClaimRewardsModel.PROCESSED)) {
                            processedCount++;
                        } else if (claim.status.equals(ClaimRewardsModel.PROCESSING)) {
                            processingCount++;
                        }

                        String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                        DBInterface
                                .updateAsync(AppConstants.QueryTokens.UPDATE_CLAIMS, getTaskTag(),
                                        values, TableClaims.NAME, values, selection,
                                        new String[]{claim.id}, false, this);
                    }
                }
                SharedPreferenceHelper.set(R.string.pref_last_fetched_rewards_post, referralScoreModel.getScore());
                SharedPreferenceHelper.set(R.string.pref_last_fetch_processing_count, processingCount);
                SharedPreferenceHelper.set(R.string.pref_last_fetch_processed_count, processedCount);

                mClaimStatusTextView.setText("Money in process: ₹" + processingCount * mMinimumClaim);


                if (referralScoreModel.getScore() >= mMinimumClaim) {
                    enableClaimButton();
                } else {
                    disableClaimButton();
                }
            }
            break;

            case ApiResponseCodes.CLAIM_REWARD: {
                mProgressWheel.setVisibility(View.GONE);
                mCreditTextView.setVisibility(View.VISIBLE);

                ClaimRewardsModel claimResult = ((ClaimRewardsModel) model);

                if (claimResult.getStatus().equals(ClaimRewardsModel.SUCCESS)) {
                    Toast.makeText(getActivity(), "Congratulations!!! Your claim is being processed.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Something went wrong. Please ensure you have at least " + mMinimumClaim + " points and try again.", Toast.LENGTH_SHORT).show();
                }

                /* Update the balance points and adjust the views accordingly */

                int processingCount = 0, processedCount = 0;
                if (claimResult.claims.size() > 0) {
                    for (ClaimRewardsModel.Claim claim : claimResult.claims) {

                        ContentValues values = new ContentValues();

                        values.put(DatabaseColumns.ID, claim.id);
                        values.put(DatabaseColumns.AMOUNT, claim.amount);
                        values.put(DatabaseColumns.STATUS, claim.status);
                        values.put(DatabaseColumns.DATE_TIME, claim.createdAt);

                        try {
                            values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(claim.createdAt));
                            values.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(claim.createdAt));
                            values.put(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, mMessageDateFormatter.getEpoch(claim.updatedAt));
                            values.put(DatabaseColumns.TIMESTAMP_HUMAN_UPDATED_AT, mMessageDateFormatter.getOutputTimestamp(claim.updatedAt));

                        } catch (ParseException e) {
                            e.printStackTrace();
                            //should not happen
                        }


                        if (claim.status.equals(ClaimRewardsModel.PROCESSED)) {
                            processedCount++;
                        } else if (claim.status.equals(ClaimRewardsModel.PROCESSING)) {
                            processingCount++;
                        }

                        String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                        DBInterface
                                .updateAsync(AppConstants.QueryTokens.UPDATE_CLAIMS, getTaskTag(),
                                        values, TableClaims.NAME, values, selection,
                                        new String[]{claim.id}, false, this);
                    }
                }

                SharedPreferenceHelper.set(R.string.pref_last_fetched_rewards_post, claimResult.getBalancePoints());
                SharedPreferenceHelper.set(R.string.pref_last_fetch_processing_count, processingCount);
                SharedPreferenceHelper.set(R.string.pref_last_fetch_processed_count, processedCount);

                mClaimStatusTextView.setText("Money in process: ₹" + processingCount * mMinimumClaim);

                mCreditTextView.setText("\u20B9" + Long.toString(claimResult.getBalancePoints()));
                if (claimResult.getBalancePoints() >= mMinimumClaim) {
                    enableClaimButton();
                } else {
                    disableClaimButton();
                }

            }
            break;

            default:
                break;
        }
    }


    /**
     * Method callback when the request is failed
     *
     * @param requestId The id of the response
     * @param errorCode The errorcode of the response
     * @param message
     */
    @Override
    public void failure(int requestId, int errorCode, String message) {
        mProgressWheel.setVisibility(View.GONE);
        mCreditTextView.setVisibility(View.VISIBLE);

        if (requestId == ApiResponseCodes.CLAIM_REWARD) {
            Toast.makeText(getActivity(), "Something went wrong. Please ensure you have at least " + mMinimumClaim + " points and try again.", Toast.LENGTH_SHORT).show();
        }

    }

    //*******************************************************************
    // Utility Functions
    //*******************************************************************


    public static RewardsFragment newInstance(Bundle args) {
        RewardsFragment f = new RewardsFragment();
        f.setArguments(args);
        return f;
    }




    //*******************************************************************
    // Functions enforced by parent classes.
    //*******************************************************************



    //*******************************************************************
    // End of class
    //*******************************************************************

}
