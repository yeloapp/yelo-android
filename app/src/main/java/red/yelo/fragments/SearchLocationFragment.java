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

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.adapters.LocationSuggestionAdapter;
import red.yelo.bus.GetLocation;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableDiscoverTags;
import red.yelo.data.TableLocationSuggesstions;
import red.yelo.data.TableServices;
import red.yelo.data.TableWallPosts;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.response.GetClickedPlaceModel;
import red.yelo.retromodels.response.GetPlacesModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class SearchLocationFragment extends AbstractYeloFragment implements
        DBInterface.AsyncDbQueryCallback, LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener, View.OnClickListener, TextWatcher,
        RetroCallback.RetroResponseListener {

    public static final String TAG = "SearchFragment";

    private EditText mLocationAutoComplete;
    /**
     * ListView into which the all location suggesstions will be placed
     */
    private ListView mLocationSearchList;

    private LocationSuggestionAdapter mLocationSuggestionAdapter;

    /**
     * cursor to load the categories so as to get ids of each in onclick
     */
    private Cursor mCursor;

    private String mPlaceName;

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private Toolbar mToolbar;

    private boolean mFromPostWall, mFromSelectInterests;

    private String[] mCityLatitudes, mCityLongitudes, mCityNames;

    private String mLatitude, mLongitide;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(false);
        final View contentView = inflater
                .inflate(R.layout.fragment_search, container, false);

        mLocationSearchList = (ListView) contentView.findViewById(R.id.list_suggestions);
        mLocationAutoComplete = (EditText) contentView.findViewById(R.id.search_auto_suggest);

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        setToolbar(mToolbar).setTitle("Select Location");

        mLocationSuggestionAdapter = new LocationSuggestionAdapter(getActivity());
        mLocationSearchList.setAdapter(mLocationSuggestionAdapter);
        mLocationSearchList.setOnItemClickListener(this);

        mCityNames = getResources().getStringArray(R.array.city_names);
        mCityLatitudes = getResources().getStringArray(R.array.city_latitudes);
        mCityLongitudes = getResources().getStringArray(R.array.city_longitudes);


        View emptyView = contentView.findViewById(R.id.empty_view);

        //LinearLayout emptyLayout = (LinearLayout) emptyView.findViewById(R.id.empty_layout);

        (emptyView.findViewById(R.id.bangalore_layout)).setOnClickListener(this);
        (emptyView.findViewById(R.id.delhi_layout)).setOnClickListener(this);
        (emptyView.findViewById(R.id.mumbai_layout)).setOnClickListener(this);
        (emptyView.findViewById(R.id.sf_layout)).setOnClickListener(this);


        emptyView.setOnClickListener(this);

        mLocationSearchList.setEmptyView(emptyView);

        mLocationAutoComplete.addTextChangedListener(this);

        loadSuggestions();

        Bundle extras = getArguments();
        if (extras.containsKey(AppConstants.Keys.PLACE)) {
            mLocationAutoComplete.setText(extras.getString(AppConstants.Keys.PLACE));
        }

        if (extras.containsKey(AppConstants.Keys.FROM_WALL)) {
            mFromPostWall = extras.getBoolean(AppConstants.Keys.FROM_WALL);
        }

        if (extras.containsKey(AppConstants.Keys.FROM_AVATAR)) {
            mFromSelectInterests = extras.getBoolean(AppConstants.Keys.FROM_AVATAR);
        }

        return contentView;
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static SearchLocationFragment newInstance() {
        SearchLocationFragment f = new SearchLocationFragment();
        return f;
    }


    private void loadSuggestions() {
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SUGGESTIONS, null, this);
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

        if (taskId == AppConstants.QueryTokens.DELETE_WALL_POSTS_SEARCH_RESULTS) {
            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_TAGS,getTaskTag(),null, TableDiscoverTags.NAME,null,
                    null,true,this);
        }

        if (taskId == AppConstants.QueryTokens.DELETE_TAGS) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(AppConstants.Keys.PLACE, mPlaceName);
            if(getActivity()!=null) {
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            }
        }


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
        if (loaderId == AppConstants.Loaders.LOAD_SUGGESTIONS) {

            String selection = DatabaseColumns.PLACE_NAME + SQLConstants.LIKE_ARG;

            if (mLocationAutoComplete.getText().toString().equals("")) {

                return new SQLiteLoader(getActivity(), false, TableLocationSuggesstions.NAME, null,
                        selection, new String[]{""}, null, null, null, null);

            } else {
                return new SQLiteLoader(getActivity(), false, TableLocationSuggesstions.NAME, null,
                        selection, new String[]{mLocationAutoComplete.getText().toString() + "%"}, null, null, null, null);

            }
        } else {
            return null;
        }
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                getActivity().finish();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            mLocationSuggestionAdapter.swapCursor(cursor);
            mCursor = cursor;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS) {
            mLocationSuggestionAdapter.swapCursor(null);
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.bangalore_layout) {


            if (!mFromPostWall && !mFromSelectInterests) {
                SharedPreferenceHelper.set(R.string.pref_latitude, mCityLatitudes[0]);
                SharedPreferenceHelper.set(R.string.pref_longitude, mCityLongitudes[0]);
                SharedPreferenceHelper.set(R.string.pref_location, mCityNames[0]);
                SharedPreferenceHelper.set(R.string.pref_city, mCityNames[0]);

                refresh("0");
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppConstants.Keys.PLACE, mCityNames[0]);
                resultIntent.putExtra(AppConstants.Keys.LATITUDE, mCityLatitudes[0]);
                resultIntent.putExtra(AppConstants.Keys.LONGITUDE, mCityLongitudes[0]);
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            }

        }

        if (v.getId() == R.id.delhi_layout) {


            if (!mFromPostWall && !mFromSelectInterests) {
                SharedPreferenceHelper.set(R.string.pref_latitude, mCityLatitudes[1]);
                SharedPreferenceHelper.set(R.string.pref_longitude, mCityLongitudes[1]);
                SharedPreferenceHelper.set(R.string.pref_location, mCityNames[1]);
                SharedPreferenceHelper.set(R.string.pref_city, mCityNames[1]);

                refresh("0");
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppConstants.Keys.PLACE, mCityNames[1]);
                resultIntent.putExtra(AppConstants.Keys.LATITUDE, mCityLatitudes[1]);
                resultIntent.putExtra(AppConstants.Keys.LONGITUDE, mCityLongitudes[1]);
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            }

        }

        if (v.getId() == R.id.mumbai_layout) {


            if (!mFromPostWall && !mFromSelectInterests) {
                SharedPreferenceHelper.set(R.string.pref_latitude, mCityLatitudes[2]);
                SharedPreferenceHelper.set(R.string.pref_longitude, mCityLongitudes[2]);
                SharedPreferenceHelper.set(R.string.pref_location, mCityNames[2]);
                SharedPreferenceHelper.set(R.string.pref_city, mCityNames[2]);

                refresh("0");
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppConstants.Keys.PLACE, mCityNames[2]);
                resultIntent.putExtra(AppConstants.Keys.LATITUDE, mCityLatitudes[2]);
                resultIntent.putExtra(AppConstants.Keys.LONGITUDE, mCityLongitudes[2]);
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            }
        }
        if (v.getId() == R.id.sf_layout) {


            if (!mFromPostWall && !mFromSelectInterests) {
                SharedPreferenceHelper.set(R.string.pref_latitude, mCityLatitudes[3]);
                SharedPreferenceHelper.set(R.string.pref_longitude, mCityLongitudes[3]);
                SharedPreferenceHelper.set(R.string.pref_location, mCityNames[3]);
                SharedPreferenceHelper.set(R.string.pref_city, mCityNames[3]);
                Logger.d(TAG,"LATI: "+ mCityLatitudes[3]+" LONGI: "+ mCityLongitudes[3]);

                refresh("0");
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppConstants.Keys.PLACE, mCityNames[3]);
                resultIntent.putExtra(AppConstants.Keys.LATITUDE, mCityLatitudes[3]);
                resultIntent.putExtra(AppConstants.Keys.LONGITUDE, mCityLongitudes[3]);
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final Cursor cursor = (Cursor) mLocationSuggestionAdapter
                .getItem(position);

        fetchSuggestionClicked(cursor.getString(cursor.getColumnIndex(DatabaseColumns.PLACE_ID)));
        mPlaceName = cursor.getString(cursor.getColumnIndex(DatabaseColumns.PLACE_NAME));
        if (!mFromPostWall) {
            SharedPreferenceHelper.set(R.string.pref_location, mPlaceName);
        }

    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        if (isAttached()) {
            fetchSuggestions(s.toString());
            loadSuggestions();
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
        retroCallback = new RetroCallback(SearchLocationFragment.this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_PLACES);

        retroCallbackList.add(retroCallback);
        mGoogleApi.getAddressList(params, retroCallback);


    }

    private void fetchServiceCategoriesFromServer() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_DISCOVER_GROUPS);
        retroCallbackList.add(retroCallback);
        HashMap<String,String> params = new HashMap<String,String>();
        params.put(AppConstants.Keys.LATITUDE, SharedPreferenceHelper.getString(R.string.pref_latitude));
        params.put(AppConstants.Keys.LONGITUDE, SharedPreferenceHelper.getString(R.string.pref_longitude));
        mYeloApi.getGocsForDiscover(params, retroCallback);
    }


    private void fetchSuggestionClicked(String placeId) {

        final Map<String, String> params = new HashMap<String, String>(6);

        params.put(HttpConstants.KEY, getResources().getString(R.string.google_api_key));
        params.put(HttpConstants.PLACE_ID,
                placeId);

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(SearchLocationFragment.this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE);
        retroCallbackList.add(retroCallback);
        mGoogleApi.getFullAddress(params, retroCallback);

    }


    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_PLACES: {
                GetPlacesModel mGetPlacesModel = ((GetPlacesModel) model);

                for (int i = 0; i < mGetPlacesModel.predictions.size(); i++) {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseColumns.PLACE_NAME, mGetPlacesModel.predictions.get(i).description);
                    values.put(DatabaseColumns.PLACE_ID, mGetPlacesModel.predictions.get(i).place_id);

                    String selection = DatabaseColumns.PLACE_ID + SQLConstants.EQUALS_ARG;


                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_LOCATION_SUGGESSTIONS, getTaskTag(), values,
                            TableLocationSuggesstions.NAME, values, selection, new String[]{mGetPlacesModel.predictions.get(i).place_id}, true, this);


                }
            }
            break;

            case HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE: {
                GetClickedPlaceModel mGetPlacesModel = ((GetClickedPlaceModel) model);

                Location location = new Location("");
                location.setLatitude(Double.parseDouble(mGetPlacesModel.result.geometry.location.lat));
                location.setLongitude(Double.parseDouble(mGetPlacesModel.result.geometry.location.lng));


                GetLocation getLocation = new GetLocation();

                getLocation.setLongitude(mGetPlacesModel.result.geometry.location.lng);
                getLocation.setLatitude(mGetPlacesModel.result.geometry.location.lat);
                getLocation.setName(mPlaceName);

                mLatitude = mGetPlacesModel.result.geometry.location.lat;
                mLongitide = mGetPlacesModel.result.geometry.location.lng;


                if (!mFromPostWall) {
                    SharedPreferenceHelper.set(R.string.pref_latitude, mGetPlacesModel.result.geometry.location.lat);
                    SharedPreferenceHelper.set(R.string.pref_longitude, mGetPlacesModel.result.geometry.location.lng);
                    String[] mAddress = mPlaceName.split(",");
                    if (mAddress.length > 3) {
                        String cityName = mAddress[mAddress.length - 3].trim();
                        SharedPreferenceHelper.set(R.string.pref_city, cityName);

                    }
                    else if(mAddress.length ==3) {
                        String cityName = mAddress[mAddress.length - 3].trim();

                        SharedPreferenceHelper.set(R.string.pref_city, cityName);
                    }
                    else if(mAddress.length ==2) {
                        String cityName = mAddress[mAddress.length - 2].trim();

                        SharedPreferenceHelper.set(R.string.pref_city, cityName);
                    }
                    else {
                        SharedPreferenceHelper.set(R.string.pref_city, "");

                    }

                    SharedPreferenceHelper.set(R.string.pref_location, mPlaceName);
                    refresh("0");
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(AppConstants.Keys.PLACE, mPlaceName);
                    resultIntent.putExtra(AppConstants.Keys.LATITUDE, mLatitude);
                    resultIntent.putExtra(AppConstants.Keys.LONGITUDE, mLongitide);

                    getActivity().setResult(Activity.RESULT_OK, resultIntent);
                    getActivity().finish();
                }

            }
            break;
        }

    }

    private void refresh(String limit) {
        //isFetched = true;

        String selection = DatabaseColumns.ID + " NOT IN ( SELECT " + DatabaseColumns.ID
                + " from " + TableWallPosts.NAME + " order by " + DatabaseColumns.TIMESTAMP_EPOCH + " desc limit " + limit + ")";

        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_WALL_POSTS_SEARCH_RESULTS, getTaskTag(),
                null, TableWallPosts.NAME, selection, null, true, this);

        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_SERVICE_CARD_CATEGORY,
                getTaskTag(), null, TableServices.NAME, null,null, true, this);


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
