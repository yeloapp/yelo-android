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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import red.yelo.R;
import red.yelo.adapters.CollectionListAdapter;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableCollections;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.response.GetCollectionResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;

/**
 * Created by anshul1235 on 24/10/14.
 */
public class CollectionListFragment extends AbstractYeloFragment implements
        DBInterface.AsyncDbQueryCallback, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
        , RetroCallback.RetroResponseListener {
    public static final String TAG = "CollectionListFragment";

    private ListView mCollectionListView;
    private CollectionListAdapter mCollectionListAdapter;



    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_collections, container, false);



        mCollectionListView = (ListView) contentView.findViewById(R.id.collection_list);
        mCollectionListAdapter = new CollectionListAdapter(getActivity());
        mCollectionListView.setAdapter(mCollectionListAdapter);
        loadCollections();
        int[] materialColors = getActivity().getResources().getIntArray(R.array.collectionListColors);


        String[] heading = new String[]{"tech","design","handymen","health","hobbies","photography"};
        String[] subHeading = new String[]{"android, web developer, nodejs ...",
                "interior, graphic, UI/UX, fashion",
                "carpenter, plumber, electrician, cook ...",
                "personal trainer, yoga, swimming ...",
                "painting, sketching, sculpture, ...",
                "fashion, wedding portfolio event ..."
        };


        for (int i = 0; i < materialColors.length; i++) {
            ContentValues values = new ContentValues();

            values.put(DatabaseColumns.ID, i+"");
            values.put(DatabaseColumns.HEADING, heading[i]);
            values.put(DatabaseColumns.SUB_HEADING, subHeading[i]);
            values.put(DatabaseColumns.COLOR, i+"");

            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

            DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_COLLECTIONS, getTaskTag(), values
                    , TableCollections.NAME, values, selection, new String[]{i+""}, true, this);

        }



        return contentView;

    }

    private void loadCollections(){
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_COLLECTIONS, null, this);

    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static CollectionListFragment newInstance() {
        CollectionListFragment f = new CollectionListFragment();
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
        if (taskId == AppConstants.QueryTokens.UPDATE_COLLECTIONS) {
            if (updateCount == 0) {
                ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_COLLECTIONS,
                        getTaskTag(), null, TableCollections.NAME, null, values, true, this);
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
        if (loaderId == AppConstants.Loaders.LOAD_COLLECTIONS) {


            return new SQLiteLoader(getActivity(), true, TableCollections.NAME, null,
                    null, null, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_COLLECTIONS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {
                mCollectionListAdapter.swapCursor(cursor);

            }

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if (loader.getId() == AppConstants.Loaders.LOAD_COLLECTIONS) {
        }
    }

    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_COLLECTIONS: {

                GetCollectionResponseModel getCollectionResponseModel = ((GetCollectionResponseModel) model);

//                int[] materialColors = getActivity().getResources().getIntArray(R.array.collectionListColors);
//
//
//                for (int i = 0; i < getCollectionResponseModel.collections.size(); i++) {
//                    ContentValues values = new ContentValues();
//
//                    values.put(DatabaseColumns.HEADING, refferalsResponseModel.tags.get(i).name);
//                    values.put(DatabaseColumns.SUB_HEADING, refferalsResponseModel.tags.get(i).id);
//                    values.put(DatabaseColumns.COLOR, refferalsResponseModel.tags.get(i).count);
//
//                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
//
//                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_REFERRALS, getTaskTag(), values
//                            , TableReferralsTags.NAME, values, selection, new String[]{refferalsResponseModel.tags.get(i).id}, true, this);
//
//                }


                break;
            }
            default:
                break;
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
