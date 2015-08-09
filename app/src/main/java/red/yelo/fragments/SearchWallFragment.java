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
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.adapters.TagsSuggestionAdapter;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableLocationSuggesstions;
import red.yelo.data.TableTags;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.response.Tags;
import red.yelo.retromodels.response.TagsRecommendationResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;
import red.yelo.widgets.MultiTagLayout;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class SearchWallFragment extends AbstractYeloFragment implements
        DBInterface.AsyncDbQueryCallback, LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener, TextWatcher,
        RetroCallback.RetroResponseListener, MultiTagLayout.OnTagClickListener,AdapterView.OnItemClickListener {

    public static final String TAG = "SearchWallFragment";

    private TextView mSelectCategories;

    /**
     * MultiTagLayout into which the Tags will be placed
     */
    //private MultiTagLayout mMultiTagLayout;

    /**
     * ListView into which the all tag suggesstions will be placed
     */
    private ListView mTagSuggestions;

    /**
     * cursor to load the categories so as to get ids of each in onclick
     */
    private Cursor mCursor;

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private Tags[] mTags;

    private boolean mTagClicked;

    private String mTagId;

    private TagsSuggestionAdapter mTagSuggestionAdapter;

    private FrameLayout mFragmeLayout;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(false);
        final View contentView = inflater
                .inflate(R.layout.fragment_search_walls, container, false);

        mSelectCategories = (TextView) contentView.findViewById(R.id.search_auto_suggest);

//        mMultiTagLayout = (MultiTagLayout) contentView.findViewById(R.id.tag_container);
//        mMultiTagLayout.setOnTagClickListener(this);
//        mMultiTagLayout.setShouldSelectOnClick(true);
//        mMultiTagLayout.setSelectionMode(MultiTagLayout.SelectionMode.SINGLE);

        mTagSuggestions = (ListView) contentView.findViewById(R.id.list_suggestions);

        mFragmeLayout = (FrameLayout) contentView.findViewById(R.id.frame_content_posts);

        mTagSuggestionAdapter = new TagsSuggestionAdapter(getActivity());

        mTagSuggestions.setAdapter(mTagSuggestionAdapter);
        mTagSuggestions.setOnItemClickListener(this);




        mSelectCategories.setOnClickListener(this);

        fetchTagSuggestions();


        return contentView;
    }


    private void fetchTagSuggestions() {

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_TAG_SUGGESTIONS);
        retroCallbackList.add(retroCallback);

        mYeloApi.getTagRecommendations(retroCallback);

    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static SearchWallFragment newInstance() {
        SearchWallFragment f = new SearchWallFragment();
        return f;
    }

    private void loadSuggesstions() {

        if (isAttached()) {
            getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS, null, this);
        }

    }

    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

        if (taskId == AppConstants.QueryTokens.UPDATE_LOCATION_SUGGESSTIONS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface
                        .insertAsync(AppConstants.QueryTokens.INSERT_LOCATION_SUGGESSTIONS,
                                getTaskTag(), null, TableLocationSuggesstions.NAME, null, values, true, this);

            }
        }

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {

            if(mTagClicked){

                String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
                return new SQLiteLoader(getActivity(), false, TableTags.NAME, null,
                        selection, new String[]{mTagId}, null, null, null, null);

            }
            else {

                return new SQLiteLoader(getActivity(), false, TableTags.NAME, null,
                        null, null, null, null, null, null);
            }

        } else {
            return null;
        }

    }

    @Override
    public boolean onBackPressed() {

        if(mTagClicked){
            mTagClicked = false;
            mFragmeLayout.setVisibility(View.GONE);
            loadSuggesstions();
            return true;
        }
        else {
            return super.onBackPressed();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());

            mTagSuggestionAdapter.swapCursor(cursor);
            mCursor = cursor;

//
//            mTags = new Tags[cursor.getCount()];
//            cursor.moveToFirst();
//            for (int i = 0; i < cursor.getCount(); i++) {
//
//                mTags[i] = new Tags(cursor.getString(
//                        cursor.getColumnIndex(DatabaseColumns.NAME)),
//                        cursor.getString(
//                                cursor.getColumnIndex(DatabaseColumns.ID)), "image_url");
//                cursor.moveToNext();
//            }
//
//            ArrayList<MultiTagLayout.Tag> tagList = new ArrayList<MultiTagLayout.Tag>(mTags.length);
//            //tagList.addAll(Arrays.asList(mTags));
//                /*mTagAdapter = new TagsArrayAdapter(getActivity(), R.layout.layout_tag_grid, tagList, true);
//
//                mTagMultiselectList.setAdapter(mTagAdapter);*/
//
//            for (Tags eachTag : mTags) {
//                tagList.add(new MultiTagLayout.Tag(eachTag.getId(), eachTag.getName()));
//            }
//
//            mMultiTagLayout.setTags(tagList);

            /*ArrayList<Tags> tagList = new ArrayList<Tags>();
            tagList.addAll(Arrays.asList(mTags));
            mTagAdapter = new TagsArrayAdapter(getActivity(), R.layout.layout_tag_grid, tagList, true);

            mTagMultiselectList.setAdapter(mTagAdapter);*/

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS) {
            mTagSuggestionAdapter.swapCursor(null);
        }
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                if(mTagClicked){
                    mTagClicked = false;
                    mFragmeLayout.setVisibility(View.GONE);
                    loadSuggesstions();
                }
                else {
                    getActivity().finish();
                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }


    @Override
    public void onClick(View v) {
        if(!mTagClicked) {
            loadSuggesstions();
        }
        else {
            mTagClicked = false;
            mFragmeLayout.setVisibility(View.GONE);
            loadSuggesstions();
        }

    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (isAttached()) {
            fetchSuggestions(s.toString());
            //loadSuggestions();
        }
        //mLocationSuggesstionAdapter.getFilter().filter(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void fetchSuggestions(String q) {

        final Map<String, String> params = new HashMap<String, String>(6);

        params.put(HttpConstants.KEY, getResources().getString(R.string.google_api_key));
        params.put(HttpConstants.INPUT, q);
        if (AppConstants.DeviceInfo.INSTANCE.getLatestLocation() != null) {
            params.put(HttpConstants.LOCATION,
                    AppConstants.DeviceInfo.INSTANCE.getLatestLocation().getLatitude()
                            + "," +
                            AppConstants.DeviceInfo.INSTANCE.getLatestLocation().getLongitude());
        }

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(SearchWallFragment.this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_PLACES);

        retroCallbackList.add(retroCallback);
        mGoogleApi.getAddressList(params, retroCallback);


    }

    private void fetchSuggestionClicked(String placeId) {

        final Map<String, String> params = new HashMap<String, String>(6);

        params.put(HttpConstants.KEY, getResources().getString(R.string.google_api_key));
        params.put(HttpConstants.PLACE_ID,
                placeId);

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(SearchWallFragment.this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE);
        retroCallbackList.add(retroCallback);
        mGoogleApi.getFullAddress(params, retroCallback);

    }


    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_TAG_SUGGESTIONS: {
                TagsRecommendationResponseModel tagsRecommendationResponseModel = ((TagsRecommendationResponseModel) model);


                for (int i = 0; i < tagsRecommendationResponseModel.tags.size(); i++) {

                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.ID, tagsRecommendationResponseModel.tags.get(i).id);
                    values.put(DatabaseColumns.NAME, tagsRecommendationResponseModel.tags.get(i).name);


                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
                            TableTags.NAME, values, selection, new String[]{tagsRecommendationResponseModel.tags.get(i).id}, true, this);


                }
                for (int i = 0; i < tagsRecommendationResponseModel.user_tags.size(); i++) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.ID, tagsRecommendationResponseModel.user_tags.get(i).id);
                    values.put(DatabaseColumns.NAME, tagsRecommendationResponseModel.user_tags.get(i).name);


                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS, getTaskTag(), values,
                            TableTags.NAME, values, selection, new String[]{tagsRecommendationResponseModel.user_tags.get(i).id}, true, this);


                }

                break;
            }
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

    @Override
    public void onTagClicked(View view, MultiTagLayout.Tag tag) {

        mTagClicked = true;
        mTagId = tag.id;
        loadSuggesstions();

        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.TAG_ID,tag.id);
        loadFragment(R.id.frame_content_posts, (AbstractYeloFragment) Fragment
                        .instantiate(getActivity(), YeloBoardFragment.class
                                .getName(), args), AppConstants.FragmentTags.YELO_BOARD, false,
                null
        );

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);

        mTagClicked = true;
        mTagId = cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID));
        mSelectCategories.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.NAME)));

        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.TAG_ID,mTagId);
        mFragmeLayout.setVisibility(View.VISIBLE);
        loadFragment(R.id.frame_content_posts, (AbstractYeloFragment) Fragment
                        .instantiate(getActivity(), YeloBoardFragment.class
                                .getName(), args), AppConstants.FragmentTags.YELO_BOARD, false,
                null
        );
    }
}
