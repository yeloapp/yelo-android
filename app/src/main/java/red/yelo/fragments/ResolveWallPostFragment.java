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

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.adapters.UserSelectionListAdapter;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableSelectionUsers;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.request.CloseWallRequestModel;
import red.yelo.retromodels.response.GetWallConnectResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;

/**
 * Created by anshul1235 on 24/10/14.
 */
public class ResolveWallPostFragment extends AbstractYeloFragment implements View.OnClickListener,
        DBInterface.AsyncDbQueryCallback, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
        , RetroCallback.RetroResponseListener {
    public static final String TAG = "ResolveWallPostFragment";

    private ListView mUserList;

    private UserSelectionListAdapter mUserSelectionListAdapter;

    private Button mSelectButton;

    private String mWallId, mMobileSelected;

    private Toolbar mToolbar;

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_resove_wallpost, container, false);


        Bundle extras = getArguments();

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);

        setToolbar(mToolbar);

        if (extras != null) {
            mWallId = extras.getString(AppConstants.Keys.WALL_ID);
        }

        mUserList = (ListView) contentView.findViewById(R.id.list_users);
        mSelectButton = (Button) contentView.findViewById(R.id.select_button);
        mSelectButton.setOnClickListener(this);

        mUserSelectionListAdapter = new UserSelectionListAdapter(getActivity());
        mUserList.setAdapter(mUserSelectionListAdapter);
        mUserList.setVerticalScrollBarEnabled(false);
        mUserList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mUserList.setOnItemClickListener(this);

        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_SELECTION_USERS, getTaskTag(), null, TableSelectionUsers.NAME,
                null, null, true, this);


        return contentView;

    }

    private void loadSelectionUsers() {
        fetchSelectionUsers();

        if(isAttached()) {
            getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_CLOSE_USERS, null, this);
        }
    }

    private void fetchSelectionUsers() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_WALL_CONNECTS);
        retroCallbackList.add(retroCallback);

        mYeloApi.getWallConnects(mWallId, retroCallback);

    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static ResolveWallPostFragment newInstance() {
        ResolveWallPostFragment f = new ResolveWallPostFragment();
        return f;
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {
        if (taskId == AppConstants.QueryTokens.DELETE_SELECTION_USERS) {
            loadSelectionUsers();
        }
    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {
        if (taskId == AppConstants.QueryTokens.UPDATE_WALL_SELECTION_USERS) {
            if (updateCount == 0) {
                ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALL_SELECTION_USERS,
                        getTaskTag(), null, TableSelectionUsers.NAME, null, values, true, this);
            }

        }
    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.select_button) {
            final int checkedItemCount = mUserList.getCheckedItemCount();

            if (checkedItemCount == 1) {

                final Cursor selectedContact = (Cursor) mUserList.getItemAtPosition(mUserList.getCheckedItemPosition());
                String userName = selectedContact.getString(selectedContact.getColumnIndex(DatabaseColumns.USER_NAME));
                String userId = selectedContact.getString(selectedContact.getColumnIndex(DatabaseColumns.ID));
                String mobileNumber = selectedContact.getString(selectedContact.getColumnIndex(DatabaseColumns.MOBILE_NUMBER));
                mMobileSelected = mobileNumber;
                closeWall(userName, userId, mobileNumber, mWallId);
            }
        }
    }

    private void closeWall(String userName, String userId, String mobileNumber, String wallId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.CLOSE_WALL);
        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.WALL_ID, mWallId);
        retroCallback.setExtras(args);
        retroCallbackList.add(retroCallback);

        CloseWallRequestModel closeWallRequestModel = new CloseWallRequestModel();
        closeWallRequestModel.setIs_solved("1");
        closeWallRequestModel.setMobile_number(mobileNumber);
        closeWallRequestModel.setName(userName);
        closeWallRequestModel.setUser_id(userId);

        mYeloApi.closeWall(wallId, closeWallRequestModel, retroCallback);


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_CLOSE_USERS) {


            return new SQLiteLoader(getActivity(), true, TableSelectionUsers.NAME, null,
                    null, null, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_CLOSE_USERS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {
                mUserSelectionListAdapter.swapCursor(cursor);


            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if (loader.getId() == AppConstants.Loaders.LOAD_CLOSE_USERS) {
            mUserSelectionListAdapter.swapCursor(null);
        }
    }

    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_WALL_CONNECTS: {


                GetWallConnectResponseModel wallConnectResponseModel = ((GetWallConnectResponseModel) model);

                final int totalCount = wallConnectResponseModel.chat_users.size() + wallConnectResponseModel.tag_users.size();

                if (totalCount > 0) {

                    final Map<String, ContentValues> userMap = new LinkedHashMap<String, ContentValues>((int) (totalCount * 1.33f));

                    ContentValues values;
                    for (GetWallConnectResponseModel.User tagUser : wallConnectResponseModel.tag_users) {

                        values = new ContentValues(4);

                        values.put(DatabaseColumns.ID, tagUser.id);
                        values.put(DatabaseColumns.USER_NAME, tagUser.name);
                        values.put(DatabaseColumns.MOBILE_NUMBER, tagUser.name);
                        values.put(DatabaseColumns.USER_IMAGE, tagUser.image_url);

                        userMap.put(tagUser.id, values);

                    }

                    for (GetWallConnectResponseModel.User chatUser : wallConnectResponseModel.chat_users) {

                        if (!userMap.containsKey(chatUser.id)) {
                            values = new ContentValues(4);

                            values.put(DatabaseColumns.ID, chatUser.id);
                            values.put(DatabaseColumns.USER_NAME, chatUser.name);
                            values.put(DatabaseColumns.MOBILE_NUMBER, chatUser.name);
                            values.put(DatabaseColumns.USER_IMAGE, chatUser.image_url);

                            userMap.put(chatUser.id, values);
                        }
                    }

                    final String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    for (String userId : userMap.keySet()) {
                        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALL_SELECTION_USERS,
                                getTaskTag(), userMap.get(userId), TableSelectionUsers.NAME, userMap.get(userId), selection,
                                new String[]{userId}, true, this);
                    }

                }
                break;
            }
            case HttpConstants.ApiResponseCodes.CLOSE_WALL:
//
//                ContentValues values = new ContentValues();
//                values.put(DatabaseColumns.STATE, AppConstants.SyncStates.CLOSED);
//
//                String selection = DatabaseColumns.TAGGED_USER_NUMBERS + SQLConstants.EQUALS_ARG;
//
//                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, getTaskTag(), null, TableMyWallComments.NAME,
//                        values, selection, new String[]{mMobileSelected}, true, this);

                getActivity().finish();

                break;
            default:
                break;
            // case HttpConstants.ApiResponseCodes.CLOSE:
        }
    }

    @Override
    public void failure(int requestId, int errorCode, String message) {

    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
    }
}
