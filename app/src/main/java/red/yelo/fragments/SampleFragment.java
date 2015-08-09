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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import red.yelo.R;
import red.yelo.data.DBInterface;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;

/**
 * Created by anshul1235 on 24/10/14.
 */
public class SampleFragment extends AbstractYeloFragment implements
        DBInterface.AsyncDbQueryCallback, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
        , RetroCallback.RetroResponseListener {
    public static final String TAG = "CreateCardFragment";


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




        return contentView;

    }



    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static SampleFragment newInstance() {
        SampleFragment f = new SampleFragment();
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
            return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_COLLECTIONS: {



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
