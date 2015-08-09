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

import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import red.yelo.R;
import red.yelo.adapters.ClaimsAdapter;
import red.yelo.adapters.NotificationSummaryAdapter;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableClaims;
import red.yelo.data.TableNotifications;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;

/**
 * Created by anshul1235 on 24/10/14.
 */
public class ClaimsTableFragment extends AbstractYeloFragment implements
        AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = "ClaimsTableFragment";


    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private RecyclerView mClaimList;

    private ClaimsAdapter mClaimsAdapter;

    private Toolbar mToolbar;

    private View mEmptyView;




    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_claims_list, container, false);


        initialiseViews(contentView,savedInstanceState);

        loadClaims();



        return contentView;

    }


    private void loadClaims() {

        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_CLAIMS, null, this);

    }


    /**
     * Initialising views
     * @return
     */
    private void initialiseViews(View contentView,Bundle savedInstanceState){
        mClaimList = (RecyclerView) contentView.findViewById(R.id.list_claims);
        mClaimsAdapter = new ClaimsAdapter(getActivity());
        mClaimList.setAdapter(mClaimsAdapter);
        mClaimList.setHasFixedSize(true);
        mClaimList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        setToolbar(mToolbar);

        mEmptyView = contentView.findViewById(R.id.empty_view);


        TextView emptyViewText;

        emptyViewText = (TextView) mEmptyView.findViewById(R.id.empty_view_text);
        emptyViewText.setText(getString(R.string.no_new_claims));


    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static ClaimsTableFragment newInstance() {
        ClaimsTableFragment f = new ClaimsTableFragment();
        return f;
    }




    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        if(loaderId == AppConstants.Loaders.LOAD_CLAIMS) {

            return new SQLiteLoader(getActivity(), false, TableClaims.NAME, null,
                    null, null, null, null, DatabaseColumns.TIMESTAMP_EPOCH
                    + SQLConstants.DESCENDING,null);
        }
        else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if(loader.getId() == AppConstants.Loaders.LOAD_CLAIMS){
            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());

            mClaimsAdapter.swapCursor(cursor);
            if(cursor.getCount()==0){
                mEmptyView.setVisibility(View.VISIBLE);
                mClaimList.setVisibility(View.GONE);
            }
            else {
                mEmptyView.setVisibility(View.GONE);
                mClaimList.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
    }

}
