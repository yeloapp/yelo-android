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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import red.yelo.R;
import red.yelo.activities.CreateServiceCardActivity;
import red.yelo.activities.ServiceCardsActivity;
import red.yelo.adapters.ServiceListAdapter;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableDiscoverTags;
import red.yelo.data.TableTags;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.response.GetCollectionResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;


/**
 * @author Sharath Pandeshwar
 * @since 19/03/2014
 * Fragment responsible to show list of Service Cards available.
 */
public class DiscoverFragment extends AbstractYeloFragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, RetroCallback.RetroResponseListener,
        DBInterface.AsyncDbQueryCallback,SwipeRefreshLayout.OnRefreshListener,View.OnClickListener {
    public static final String TAG = "DiscoverFragment";

    private GridView mServiceListView;
    private ServiceListAdapter mServiceListAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private View mEmptyView;

    private boolean mIsFetched;



    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();


    //*******************************************************************
    // Life Cycle Related Functions
    //*******************************************************************


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater.inflate(R.layout.fragment_discover, container, false);
        initializeViews(contentView, savedInstanceState);
        loadServiceList();

        if(TextUtils.isEmpty(AppConstants.UserInfo.INSTANCE.getFirstName())){
            fetchUserDetails(AppConstants.UserInfo.INSTANCE.getId());
        }
        return contentView;
    }


    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
        mEmptyView.setVisibility(View.GONE);
        mIsFetched = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadServiceList();
    }

    //*******************************************************************
    // View Related Functions
    //*******************************************************************


    private void initializeViews(View contentView, Bundle savedInstanceState) {
        mServiceListView = (GridView) contentView.findViewById(R.id.service_list);
        mServiceListAdapter = new ServiceListAdapter(getActivity());
        mServiceListView.setAdapter(mServiceListAdapter);
        mServiceListView.setOnItemClickListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.white);
        mSwipeRefreshLayout.setProgressBackgroundColor(R.color.primaryColor);
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mSwipeRefreshLayout.setEnabled(true);

        mEmptyView = contentView.findViewById(R.id.empty_view);
        mEmptyView.setVisibility(View.GONE);

        TextView addCard = (TextView) mEmptyView.findViewById(R.id.add_card);
        TextView textView = (TextView) mEmptyView.findViewById(R.id.empty_view_text);
        textView.setText("No cards found in your city");
        addCard.setOnClickListener(this);

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        String category_id = cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID));
        String categoryName = cursor.getString(cursor.getColumnIndex(DatabaseColumns.NAME));


        /* Call ServiceCards List Activity to show the list of service cards under this category */
        Bundle bundle = new Bundle();
        bundle.putInt(AppConstants.Keys.SERVICE_SCREEN_TYPE, AppConstants.ServiceScreenType.SEARCH_WITH_CATEGORY);
        bundle.putString(AppConstants.Keys.CATEGORY_ID, category_id);
        bundle.putString(AppConstants.Keys.CATEGORY_NAME, categoryName);

        Intent startServiceCardsIntent = new Intent(getActivity(), ServiceCardsActivity.class);
        //startServiceCardsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startServiceCardsIntent.putExtras(bundle);
        startActivity(startServiceCardsIntent);
    }


    //*******************************************************************
    // Data Related Functions
    //*******************************************************************


    private void loadServiceList() {
        /* Get the list from the table */
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SERVICE_CATEGORIES, null, this);
        /* Also get the list from server in case something changed */
        fetchServiceCategoriesFromServer();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_SERVICE_CATEGORIES) {
            String selection = DatabaseColumns.TYPE + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableDiscoverTags.NAME, null, selection,
                    new String[]{AppConstants.GroupType.DISCOVER},
                    null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);
        } else {
            return null;
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_SERVICE_CATEGORIES) {
            Logger.i(TAG, "Service Category Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {
                mServiceListAdapter.swapCursor(cursor);

                if(cursor.getCount() == 0&&!mIsFetched){
                    fetchServiceCategoriesFromServer();
                    mIsFetched = true;
                }
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_SERVICE_CATEGORIES) {
        }
    }


    /**
     * Method called when an asynchronous insert operation is done
     *
     * @param taskId      The token passed into the async mthod
     * @param cookie      Any extra object passed into the query.
     * @param insertRowId The inserted row id, or -1 if it failed
     */
    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }


    /**
     * Method called when an asynchronous delete operation is done
     *
     * @param taskId      The token passed into the async method
     * @param cookie      Any extra object passed into the query.
     * @param deleteCount The number of rows deleted
     */
    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

        if(taskId == AppConstants.QueryTokens.DELETE_TAGS){
            fetchServiceCategoriesFromServer();
            fetchGocs();
        }
    }


    /**
     * Method called when an asynchronous update operation is done
     *
     * @param taskId      The token passed into the async method
     * @param cookie      Any extra object passed into the query.
     * @param updateCount The number of rows updated
     */
    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {
        if (taskId == AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS) {
            if (updateCount == 0) {
                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_TAG_SUGGESSTIONS, getTaskTag(),
                        null, TableTags.NAME, null, values, true, this);
            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_DISCOVER_TAGS_SUGGESSTIONS) {
            if (updateCount == 0) {
                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_DISCOVER_TAG_SUGGESSTIONS, getTaskTag(),
                        null, TableDiscoverTags.NAME, null, values, true, this);
            }
        }
    }


    /**
     * Method called when an asyncronous query operation is done
     *
     * @param taskId The token passed into the async method
     * @param cookie Any extra object passed into the query.
     * @param cursor The {@link Cursor} read from the database
     */
    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }


    //*******************************************************************
    // Network Related Functions
    //*******************************************************************


    private void fetchServiceCategoriesFromServer() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_DISCOVER_GROUPS);
        retroCallbackList.add(retroCallback);
        HashMap<String,String> params = new HashMap<String,String>();
        params.put(AppConstants.Keys.LATITUDE, SharedPreferenceHelper.getString(R.string.pref_latitude));
        params.put(AppConstants.Keys.LONGITUDE,SharedPreferenceHelper.getString(R.string.pref_longitude));
        mYeloApi.getGocsForDiscover(params,retroCallback);
    }

    private void fetchUserDetails(String userId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_USER_DETAILS);
        retroCallbackList.add(retroCallback);

        mYeloApi.getUserDetailAsync(userId, retroCallback);
        mSwipeRefreshLayout.setRefreshing(true);
    }

    private void fetchGocs() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_GROUPS);
        retroCallbackList.add(retroCallback);
        mYeloApi.getGocs(retroCallback);
    }

    @Override
    public void success(Object model, int requestId) {
        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_DISCOVER_GROUPS: {

                mSwipeRefreshLayout.setRefreshing(false);
                GetCollectionResponseModel getCollectionResponseModel = ((GetCollectionResponseModel) model);

                if(getCollectionResponseModel.groups.size()==0){
                    mEmptyView.setVisibility(View.VISIBLE);
                }
                else {
                    mEmptyView.setVisibility(View.GONE);
                }


                break;
            }

            case HttpConstants.ApiResponseCodes.GET_GROUPS: {

                GetCollectionResponseModel getCollectionResponseModel = ((GetCollectionResponseModel) model);

                for (GetCollectionResponseModel.Collection eachEntry : getCollectionResponseModel.groups) {

                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.ID, eachEntry.id);
                    values.put(DatabaseColumns.NAME, eachEntry.name);
                    values.put(DatabaseColumns.COLOR, eachEntry.color);

                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
                            TableTags.NAME, values, selection, new String[]{eachEntry.id}, true, this);

                }

                break;
            }
            default:
                break;
        }
    }


    @Override
    public void failure(int requestId, int errorCode, String message) {

    }

    //*******************************************************************
    // Utility Functions
    //*******************************************************************


    public static DiscoverFragment newInstance() {
        DiscoverFragment f = new DiscoverFragment();
        return f;
    }


    //*******************************************************************
    // Functions enforced by parent classes.
    //*******************************************************************


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onRefresh() {

        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_TAGS,getTaskTag(),null,TableDiscoverTags.NAME,null,
                null,true,this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.add_card){
            final Intent createServiceCard = new Intent(getActivity(), CreateServiceCardActivity.class);
            createServiceCard.putExtra(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE.getId());

            startActivity(createServiceCard);
        }
    }


    //*******************************************************************
    // End of class
    //*******************************************************************
}
