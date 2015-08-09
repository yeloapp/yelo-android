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

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import red.yelo.R;
import red.yelo.activities.UserProfileActivity;
import red.yelo.activities.WallPostActivity;
import red.yelo.adapters.ClaimsAdapter;
import red.yelo.adapters.LeadersAdapter;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableClaims;
import red.yelo.data.TableLeaders;
import red.yelo.data.TableSelectionUsers;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.response.WeeklyLeaders;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;

/**
 * Created by anshul1235 on 24/10/14.
 */
public class LeaderBoardFragment extends AbstractYeloFragment implements
        DBInterface.AsyncDbQueryCallback, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
        , RetroCallback.RetroResponseListener, LeadersAdapter.LeadersActionClickListener {
    public static final String TAG = "LeaderBoardFragment";


    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private ListView mLeaderList;

    private LeadersAdapter mLeadersAdapter;

    private Toolbar mToolbar;

    private View mEmptyView;

    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_leaderboard, container, false);

        initialiseViews(contentView, savedInstanceState, inflater);


        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgress(0);

        fetchAndLoadLeaders();


        return contentView;

    }


    private void loadLeaders() {

        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_LEADERS, null, this);

    }


    /**
     * Initialising views
     *
     * @return
     */
    private void initialiseViews(View contentView, Bundle savedInstanceState, LayoutInflater inflater) {
        mLeaderList = (ListView) contentView.findViewById(R.id.list_leaders);
        mLeadersAdapter = new LeadersAdapter(getActivity(), this);


        View footerView = inflater.inflate(R.layout.layout_leader_header, null, false);

        mLeaderList.setAdapter(mLeadersAdapter);
        mLeaderList.addFooterView(footerView);

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        setToolbar(mToolbar);

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static LeaderBoardFragment newInstance() {
        LeaderBoardFragment f = new LeaderBoardFragment();
        return f;
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {


    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

        if (taskId == AppConstants.QueryTokens.UPDATE_LEADERS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_LEADERS, getTaskTag(), null
                        , TableLeaders.NAME, null, values, true, this);
            }
        }

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_LEADERS) {

            return new SQLiteLoader(getActivity(), false, TableLeaders.NAME, null,
                    null, null, null, null, DatabaseColumns.REFERRAL_COUNT
                    + SQLConstants.DESCENDING, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_LEADERS) {
            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());

            mLeadersAdapter.swapCursor(cursor);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void success(Object model, int requestId) {
        mProgressDialog.dismiss();
        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_LEADERS: {

                WeeklyLeaders weeklyLeaders = ((WeeklyLeaders) model);

                int i = 0;
                if (weeklyLeaders.users.size() > 0) {
                    for (WeeklyLeaders.Users user : weeklyLeaders.users) {
                        ContentValues values = new ContentValues();
                        values.put(DatabaseColumns.ID, i);
                        values.put(DatabaseColumns.USER_ID, user.id);
                        values.put(DatabaseColumns.USER_IMAGE, user.imageUrl);
                        values.put(DatabaseColumns.REFERRAL_COUNT, user.referralCount);
                        values.put(DatabaseColumns.USER_NAME, user.name);


                        String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                        DBInterface
                                .updateAsync(AppConstants.QueryTokens.UPDATE_LEADERS, getTaskTag(),
                                        values, TableLeaders.NAME, values, selection,
                                        new String[]{i + ""}, true, this);
                        i++;
                    }
                }
                else {
                    DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_LEADERS,getTaskTag(),null,TableLeaders.NAME,
                            null,null,true,this);
                }


                break;
            }
            default:
                break;
        }
    }

    @Override
    public void failure(int requestId, int errorCode, String message) {
        mProgressDialog.dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
    }


    private void fetchAndLoadLeaders() {

        SharedPreferenceHelper.set(R.string.pref_last_fetched_weeklyleaders, Utils.getCurrentEpochTime());
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_LEADERS);
        retroCallbackList.add(retroCallback);
        mYeloApi.getWeeklyLeaders(retroCallback);
        loadLeaders();
        mProgressDialog.show();

    }

    @Override
    public void onLeaderClicked(View view, String userId, String userName) {

        loadProfile(userId, userName);

    }

    /**
     * This loads the profile of the user
     *
     * @param userId user id of the user u want to open profile of
     * @param name   name of the user
     */
    private void loadProfile(String userId, String name) {
        final Intent userProfileIntent = new Intent(getActivity(), UserProfileActivity.class);

        userProfileIntent.putExtra(AppConstants.Keys.USER_ID, userId);
        userProfileIntent.putExtra(AppConstants.Keys.USER_NAME, name);
        userProfileIntent.putExtra(AppConstants.Keys.SERVICE_SCREEN_TYPE, AppConstants.ServiceScreenType.PROFILE);
        startActivity(userProfileIntent);
    }
}
