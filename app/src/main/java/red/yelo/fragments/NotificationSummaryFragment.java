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

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

import red.yelo.R;
import red.yelo.activities.WallPostActivity;
import red.yelo.adapters.NotificationSummaryAdapter;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableNotifications;
import red.yelo.data.TableTags;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;
import red.yelo.utils.mugen.Mugen;
import red.yelo.utils.mugen.MugenCallbacks;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class NotificationSummaryFragment extends AbstractYeloFragment implements
        DBInterface.AsyncDbQueryCallback, LoaderManager.LoaderCallbacks<Cursor>
        , NotificationSummaryAdapter.ViewClickListener {

    public static final String TAG = "NotificationSummaryFragment";


    /**
     * RecyclerView into which the all notifications will be placed
     */
    private RecyclerView mNotificationList;

    private NotificationSummaryAdapter mNotificationSummaryAdapter;

    /**
     * cursor to load the categories so as to get ids of each in onclick
     */
    private Cursor mCursor;

    private TextView mEmptyViewText;

    private boolean mIsLoading, mIsFetched, mAllFetched;

    private int mCountLoaded = 0;

    private Toolbar mToolbar;

    private HashMap<String, String> mGroupColorMap = new HashMap<String, String>();

    private View mEmptyView;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(false);
        final View contentView = inflater
                .inflate(R.layout.fragment_notification_summary, container, false);

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        setToolbar(mToolbar);
        mNotificationList = (RecyclerView) contentView.findViewById(R.id.list_notifications);

        mNotificationSummaryAdapter = new NotificationSummaryAdapter(getActivity(), NotificationSummaryFragment.this);
        mNotificationList.setAdapter(mNotificationSummaryAdapter);
        mNotificationList.setHasFixedSize(true);
        mNotificationList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        mEmptyView = contentView.findViewById(R.id.empty_view);


        mEmptyViewText = (TextView) mEmptyView.findViewById(R.id.empty_view_text);

        mEmptyViewText.setText(getString(R.string.notifications_not_available));


        loadGroupGocs();

        return contentView;

    }

    private void loadGroupGocs() {

        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS, null, this);

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Mugen.with(mNotificationList, new MugenCallbacks() {
            @Override
            public void onLoadMore() {

                if (!mIsLoading) {
                    loadNotifications();
                }
            }

            @Override
            public boolean isLoading() {
                return mIsLoading;
            }

            @Override
            public boolean hasLoadedAllItems() {
                return mAllFetched;

            }
        }).start();
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static NotificationSummaryFragment newInstance() {
        NotificationSummaryFragment f = new NotificationSummaryFragment();
        return f;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {


            case android.R.id.home: {

                getActivity().finish();
//                DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_NOTIICATIONS, null,
//                        null, TableNotifications.NAME, null, null, false, this);
//                return true;
            }
        }
        return true;

    }

//    @Override
//    public boolean onBackPressed() {
////        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_NOTIICATIONS, null,
////                null, TableNotifications.NAME, null, null, false, this);
////        return true;
//
//    }

    private void loadNotifications() {

        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_NOTIFICATIONS, null, this);

    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {
        if (taskId == AppConstants.QueryTokens.DELETE_NOTIICATIONS) {
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_NOTIFICATIONS) {

            mCountLoaded = mCountLoaded + 20;

            return new SQLiteLoader(getActivity(), false, TableNotifications.NAME, null,
                    null, null, null, null, BaseColumns._ID
                    + SQLConstants.DESCENDING, mCountLoaded + "");

        } else if (loaderId == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {

            return new SQLiteLoader(getActivity(), false, TableTags.NAME, null,
                    null, null, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (loader.getId() == AppConstants.Loaders.LOAD_NOTIFICATIONS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());

            mNotificationSummaryAdapter.swapCursor(cursor);
            mCursor = cursor;
            if(mCursor.getCount()==0){
                mEmptyView.setVisibility(View.VISIBLE);
                mNotificationList.setVisibility(View.GONE);
            }
            else {
                mEmptyView.setVisibility(View.GONE);
                mNotificationList.setVisibility(View.VISIBLE);
            }

            markNotificationAsOpened(cursor);

        }

        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {

            cursor.moveToFirst();

            while (cursor.moveToNext()) {
                mGroupColorMap.put(cursor.getString(cursor.getColumnIndex(DatabaseColumns.NAME)).toLowerCase(),
                        cursor.getString(cursor.getColumnIndex(DatabaseColumns.COLOR)));
            }



            loadNotifications();
        }
    }

    private void markNotificationAsOpened(Cursor cursor) {
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            ContentValues values = new ContentValues();
            String selection = BaseColumns._ID + SQLConstants.EQUALS_ARG;
            values.put(DatabaseColumns.NOTIFICATION_STATUS, AppConstants.NotificationStatus.UNREAD_OPENED);
            if (cursor.getString(cursor.getColumnIndex(DatabaseColumns.NOTIFICATION_STATUS)).equals(AppConstants.NotificationStatus.UNREAD_NOT_OPENED)) {
                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_NOTIFICATION_STATUS, getTaskTag(), null, TableNotifications.NAME, values,
                        selection, new String[]{cursor.getString(cursor.getColumnIndex(BaseColumns._ID))}, true, this);
            }
            cursor.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_NOTIFICATIONS) {
            mNotificationSummaryAdapter.swapCursor(null);
        }
    }


    @Override
    public void onPostClicked(View view, String notificationId, String wallId, String messageType) {

//        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_NOTIICATIONS, null,
//                null, TableNotifications.NAME, null, null, false, this);
//

        ContentValues values = new ContentValues();

        values.put(DatabaseColumns.NOTIFICATION_STATUS, AppConstants.NotificationStatus.READ);

        String selection = BaseColumns._ID + SQLConstants.EQUALS_ARG;

        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_NOTIFICATION_STATUS, getTaskTag(), null, TableNotifications.NAME,
                values, selection, new String[]{notificationId}, true, this);

        if (messageType.equals(AppConstants.CollapseKey.WALL)) {

            final Intent wallPostIntent = new Intent(getActivity(),
                    WallPostActivity.class);
            wallPostIntent.putExtra(AppConstants.Keys.ID, wallId);
            wallPostIntent.putExtra(AppConstants.Keys.NOTIFICATION_ID, notificationId);
            wallPostIntent.putExtra(AppConstants.Keys.FROM_NOTIFICATIONS, true);

            startActivity(wallPostIntent);
        } else if (messageType.equals(AppConstants.CollapseKey.SUMMARY)) {
            getActivity().finish();
        } else if (messageType.equals(AppConstants.CollapseKey.TAG)) {
            final Intent wallPostIntent = new Intent(getActivity(),
                    WallPostActivity.class);
            wallPostIntent.putExtra(AppConstants.Keys.ID, wallId);
            wallPostIntent.putExtra(AppConstants.Keys.NOTIFICATION_ID, notificationId);
            wallPostIntent.putExtra(AppConstants.Keys.FROM_NOTIFICATIONS, true);

            startActivity(wallPostIntent);
        } else if (messageType.equals(AppConstants.CollapseKey.PIN)) {
            final Intent wallPostIntent = new Intent(getActivity(),
                    WallPostActivity.class);
            wallPostIntent.putExtra(AppConstants.Keys.ID, wallId);
            wallPostIntent.putExtra(AppConstants.Keys.NOTIFICATION_ID, notificationId);
            wallPostIntent.putExtra(AppConstants.Keys.FROM_NOTIFICATIONS, true);

            startActivity(wallPostIntent);
        } else if (messageType.equals(AppConstants.CollapseKey.CONTACT_WALL)) {
            final Intent wallPostIntent = new Intent(getActivity(),
                    WallPostActivity.class);
            wallPostIntent.putExtra(AppConstants.Keys.ID, wallId);
            wallPostIntent.putExtra(AppConstants.Keys.NOTIFICATION_ID, notificationId);
            wallPostIntent.putExtra(AppConstants.Keys.FROM_NOTIFICATIONS, true);

            startActivity(wallPostIntent);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        clearNotifications();
    }

    private void clearNotifications() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        notificationManager.cancel(AppConstants.NOTIFICATION_ID_WALL);
    }

    public HashMap<String,String> getGroupColors(){
        return mGroupColorMap;
    }
}
