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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.activities.HomeActivity;
import red.yelo.activities.WallPostActivity;
import red.yelo.adapters.RecommendationListAdapter;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableProfileCards;
import red.yelo.data.TableRecommendations;
import red.yelo.data.TableSubCategories;
import red.yelo.data.TableTags;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.Link;
import red.yelo.retromodels.request.CreateListingRequestModel;
import red.yelo.retromodels.response.CreateListingResponseModel;
import red.yelo.retromodels.response.GetCollectionResponseModel;
import red.yelo.retromodels.response.GetRecommendationResponseModel;
import red.yelo.retromodels.response.GoogleGeocodeResponse;
import red.yelo.retromodels.response.KeywordSuggestionsResponseModel;
import red.yelo.retromodels.response.Tags;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.widgets.autocomplete.INetworkSuggestCallbacks;
import red.yelo.widgets.autocomplete.NetworkedAutoCompleteTextView;

/**
 * Created by anshul1235 on 24/10/14.
 */
public class CreateOpenCardFragment extends AbstractYeloFragment implements
        DBInterface.AsyncDbQueryCallback, AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
        , RetroCallback.RetroResponseListener, View.OnClickListener, INetworkSuggestCallbacks,
        RecommendationListAdapter.RecommendationActionClickListener {
    public static final String TAG = "CreateCardFragment";


    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private RecommendationListAdapter mRecommendationListAdapter;


    private ListView mReferralList;

    private Toolbar mToolbar;

    private LinearLayout mGocLayout;
    private FrameLayout mCategoryFrameLayout, mGocFrame;

    private TextView mGocName, mCategoryName, mUrlText, mKeywordNonEdit, mUrlTextNonEdit, mCategoryNameNonEdit,
            mReferralText, mReferralTextNonEdit, mKeywordTitleNonEdit, mUrlTitleNonEdit;

    private ImageView mSpinnerIcon;

    private NetworkedAutoCompleteTextView mKeywords;

    private String[] mAllGocNames, mAllGocCategories, mGocColors, mAllSubCategoryId, mAllGocIds;

    private Tags[] mTags;

    private String mQ;

    private FloatingActionButton mFabButton;

    private String mLatitude = "", mLongitude = "", mTagNameAdded;

    private String[] mAddress;

    private boolean mIsUpdate, mOtherUser, mFromLogin, mIsUpdated;

    private String mListingId, mUserId, mGocNameSelected, mSelectedTagId, mSelectedCategoryName, mActionColor, mKeyText,
            mGocIdSelected;

    private ProgressDialog mProgressDialog;

    private ProgressBar mProgressBar;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);

        final View contentView = inflater
                .inflate(R.layout.fragment_create_open_card, container, false);

        mProgressDialog = showProgressDialog();

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        mReferralList = (ListView) contentView.findViewById(R.id.list_referrals);
        mGocLayout = (LinearLayout) contentView.findViewById(R.id.goc_layout);
        mGocFrame = (FrameLayout) contentView.findViewById(R.id.frame_goc);
        mGocName = (TextView) contentView.findViewById(R.id.goc_name);
        mFabButton = (FloatingActionButton) contentView.findViewById(R.id.fabbutton);
        mSpinnerIcon = (ImageView) contentView.findViewById(R.id.spinner_icon);
        mProgressBar = (ProgressBar) contentView.findViewById(R.id.progress_wheel);

        mFabButton.setOnClickListener(this);

        mLatitude = SharedPreferenceHelper.getString(R.string.pref_latitude);
        mLongitude = SharedPreferenceHelper.getString(R.string.pref_longitude);


        setToolbar(mToolbar, getResources().getString(R.string.title_create_card), true);

        View headerView = inflater
                .inflate(R.layout.layout_create_card_header, null, false);

        View headerViewNonEdit = inflater
                .inflate(R.layout.layout_card_header_non_edit, null, false);


        mCategoryFrameLayout = (FrameLayout) headerView.findViewById(R.id.category_frame_layout);
        mCategoryName = (TextView) headerView.findViewById(R.id.goc_category_name);
        mCategoryNameNonEdit = (TextView) headerViewNonEdit.findViewById(R.id.goc_category_name);
        mUrlText = (TextView) headerView.findViewById(R.id.url);
        mUrlTextNonEdit = (TextView) headerViewNonEdit.findViewById(R.id.url);
        mReferralText = (TextView) headerView.findViewById(R.id.referrals);
        mReferralTextNonEdit = (TextView) headerViewNonEdit.findViewById(R.id.referrals);
        mKeywordTitleNonEdit = (TextView) headerViewNonEdit.findViewById(R.id.keywordTitle);
        mUrlTitleNonEdit = (TextView) headerViewNonEdit.findViewById(R.id.urlTitle);


        mKeywords = (NetworkedAutoCompleteTextView) headerView.findViewById(R.id.keywords);

        mKeywordNonEdit = (TextView) headerViewNonEdit.findViewById(R.id.keywords);

        mKeywords.setNetworkSuggestCallbacks(this);
        mKeywords.setSuggestCountThreshold(2);
        mKeywords.setSuggestWaitThreshold(400);


        mGocLayout.setOnClickListener(this);


        mRecommendationListAdapter = new RecommendationListAdapter(getActivity(), this);

        Bundle extras = getArguments();

        if (extras != null) {
            if (extras.containsKey(AppConstants.Keys.FROM_LOGIN)) {
                mFromLogin = extras.getBoolean(AppConstants.Keys.FROM_LOGIN);
            }

            if (extras.containsKey(AppConstants.Keys.ID)) {
                mGocName.setText(extras.getString(AppConstants.Keys.GROUP_NAME));
                mListingId = extras.getString(AppConstants.Keys.ID);
//                mSelectedCategoryName = extras.getString(AppConstants.Keys.SUBCATEGORY_NAME);
                mSelectedTagId = extras.getString(AppConstants.Keys.SUBCATEGORY_ID);
                if (extras.getString(AppConstants.Keys.USER_ID).equals(AppConstants.UserInfo.INSTANCE.getId())) {
                    mIsUpdate = true;
                } else {
                    mOtherUser = true;
                }
                mUserId = extras.getString(AppConstants.Keys.USER_ID);
                loadSubCategories(extras.getString(AppConstants.Keys.GROUP_ID));

            }
        }

        mCategoryFrameLayout.setOnClickListener(this);

        if (mOtherUser) {
            mReferralList.addHeaderView(headerViewNonEdit);
            mSpinnerIcon.setVisibility(View.GONE);
            mFabButton.setVisibility(View.GONE);

        } else {
            mReferralList.addHeaderView(headerView);
        }
        //Need to be called after addHeaderView due to limitation in android version < 4.4
        mReferralList.setAdapter(mRecommendationListAdapter);
        mReferralList.setOnItemClickListener(this);
        fetchGocs();
        loadGroupGocs();
        if (savedInstanceState != null) {

            mSelectedTagId = savedInstanceState.getString(AppConstants.Keys.CATEGORY_ID);
            mAddress = savedInstanceState.getStringArray(AppConstants.Keys.ADDRESS);

            mGocName.setText(savedInstanceState.getString(AppConstants.Keys.GROUP_NAME));

            if (!mOtherUser) {
                mKeywords.setText(savedInstanceState.getString(AppConstants.Keys.KEYWORDS));
                mUrlText.setText(savedInstanceState.getString(AppConstants.Keys.URL));
                mCategoryName.setText(savedInstanceState.getString(AppConstants.Keys.CATEGORY_NAME));
            } else {
                mKeywordNonEdit.setText(savedInstanceState.getString(AppConstants.Keys.KEYWORDS));
                mUrlTextNonEdit.setText(savedInstanceState.getString(AppConstants.Keys.URL));
                mCategoryNameNonEdit.setText(savedInstanceState.getString(AppConstants.Keys.CATEGORY_NAME));
            }

            mGocIdSelected = savedInstanceState.getString(AppConstants.Keys.GROUP_ID);
            loadSubCategories(mGocIdSelected);


            mActionColor = savedInstanceState.getString(AppConstants.Keys.COLOR);

            if (!TextUtils.isEmpty(mActionColor)) {
                colorizeActionBar(Color.parseColor(mActionColor), mToolbar);
                colorizeView(Color.parseColor(mActionColor), mGocFrame);
            }


        }


        if (mOtherUser) {
            mFabButton.setVisibility(View.GONE);
        }
        fillAddressDetails();

        if (savedInstanceState == null) {

            if (mIsUpdate) {
                loadCard(extras.getString(AppConstants.Keys.ID));
            }
            if (mOtherUser) {
                loadCard(extras.getString(AppConstants.Keys.ID));

            }


        }


        return contentView;

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {


        outState.putString(AppConstants.Keys.GROUP_NAME, mGocName.getText().toString());
        outState.putString(AppConstants.Keys.CATEGORY_ID, mSelectedTagId);
        outState.putStringArray(AppConstants.Keys.ADDRESS, mAddress);
        outState.putString(AppConstants.Keys.COLOR, mActionColor);
        outState.putString(AppConstants.Keys.GROUP_ID, mGocIdSelected);


        if (!mOtherUser) {
            outState.putString(AppConstants.Keys.CATEGORY_NAME, mCategoryName.getText().toString());
            outState.putString(AppConstants.Keys.KEYWORDS, mKeywords.getText().toString());
            outState.putString(AppConstants.Keys.URL, mUrlText.getText().toString());
        } else {
            outState.putString(AppConstants.Keys.CATEGORY_NAME, mCategoryNameNonEdit.getText().toString());
            outState.putString(AppConstants.Keys.KEYWORDS, mKeywordNonEdit.getText().toString());
            outState.putString(AppConstants.Keys.URL, mUrlTextNonEdit.getText().toString());
        }


        super.onSaveInstanceState(outState);

    }


    private void fillAddressDetails() {

        final Map<String, String> params = new HashMap<String, String>(6);
        params.put(HttpConstants.LATLNG, SharedPreferenceHelper.getString(R.string.pref_latitude)
                + "," + SharedPreferenceHelper.getString(R.string.pref_longitude));
        params.put(HttpConstants.KEY, getResources().getString(R.string.google_api_key));
        params.put(HttpConstants.RESULT_TYPE, HttpConstants.STREET_ADDRESS);

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE);
        retroCallbackList.add(retroCallback);
        mGoogleApi.getMyAddress(params, retroCallback);

    }


    private void fetchGocs() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_GROUPS);
        retroCallbackList.add(retroCallback);
        if (!mOtherUser)
            mProgressDialog.show();
        mYeloApi.getGocs(retroCallback);
    }

    private void fetchCategories(String groupId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SUB_CATEGORIES);

        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.TAG_ID, groupId);
        retroCallback.setExtras(args);
        retroCallbackList.add(retroCallback);


        if (!mOtherUser)
            mProgressDialog.show();
        mYeloApi.getSubCategories(groupId, retroCallback);
    }

    private void loadReferralsTagsList(String subcategoryId) {
        fetchReferralsMade(subcategoryId);

        if (isAttached()) {
            getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_RECOMMENDATIONS, null, this);
        }
    }

    private void fetchReferralsMade(String subcategoryId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);

        Map<String, String> params = new HashMap<String, String>(1);
        params.put(HttpConstants.Tag_ID, subcategoryId);

        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_RECOMMENDATIONS);
        retroCallbackList.add(retroCallback);
        mProgressBar.setVisibility(View.VISIBLE);
        mYeloApi.getRecommendationsRec(mUserId, params, retroCallback);
    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static CreateOpenCardFragment newInstance() {
        CreateOpenCardFragment f = new CreateOpenCardFragment();
        return f;
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

        if (taskId == AppConstants.QueryTokens.INSERT_PROFILE_CARDS) {
            userRefresh(true);

            if (mFromLogin) {

                final Intent homeActivityIntent = new Intent(getActivity(),
                        HomeActivity.class);
                startActivityForResult(homeActivityIntent, AppConstants.RequestCodes.HOME);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            } else {
                if (getActivity() != null) {

                    getActivity().finish();
                }
            }
        }

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

        if (taskId == AppConstants.QueryTokens.DELETE_RECOMMENDATIONS) {
            loadReferralsTagsList(mSelectedTagId);
        }

    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

        if (taskId == AppConstants.QueryTokens.UPDATE_TAGS_SUGGESSTIONS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface
                        .insertAsync(AppConstants.QueryTokens.INSERT_TAG_SUGGESSTIONS,
                                getTaskTag(), null, TableTags.NAME, null, values, true, this);

            }
        }
        if (taskId == AppConstants.QueryTokens.UPDATE_RECOMMENDATIONS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_RECOMMENDATIONS,
                        getTaskTag(), null, TableRecommendations.NAME, null, values, true, this);

            }
        }


        if (taskId == AppConstants.QueryTokens.UPDATE_PROFILE_CARDS) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_PROFILE_CARDS, getTaskTag(), null
                        , TableProfileCards.NAME, null, values, true, this);
            } else {
                userRefresh(true);
                getActivity().finish();

                if (mFromLogin) {

                    final Intent homeActivityIntent = new Intent(getActivity(),
                            HomeActivity.class);
                    startActivityForResult(homeActivityIntent, AppConstants.RequestCodes.HOME);
                }

            }


        }


    }

    private void loadGroupGocs() {

        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS, null, this);

    }

    private void loadCard(String listId) {

        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.ID, listId);
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_PROFILE_CARD, args, this);

    }


    private void loadSubCategories(String groupId) {

        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.TAG_ID, groupId);
        fetchCategories(groupId);
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_CATEGORIES, args, this);

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (position > 0) {
            Cursor cursor = (Cursor) parent.getItemAtPosition(position - 1);

            String wallId = cursor.getString(cursor.getColumnIndex(DatabaseColumns.WALL_ID));

            final Intent wallPostIntent = new Intent(getActivity(),
                    WallPostActivity.class);
            wallPostIntent.putExtra(AppConstants.Keys.ID, wallId);
            wallPostIntent.putExtra(AppConstants.Keys.FROM_WALL, true);
            startActivity(wallPostIntent);
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {

            return new SQLiteLoader(getActivity(), false, TableTags.NAME, null,
                    null, null, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);
        } else if (loaderId == AppConstants.Loaders.LOAD_CATEGORIES) {

            String categoryId = bundle.getString(AppConstants.Keys.TAG_ID);

            String selection = DatabaseColumns.CATEGORY_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableSubCategories.NAME, null,
                    selection, new String[]{categoryId}, null, null, DatabaseColumns.NAME + SQLConstants.ASCENDING, null);

        } else if (loaderId == AppConstants.Loaders.LOAD_PROFILE_CARD) {

            String listId = bundle.getString(AppConstants.Keys.ID);

            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableProfileCards.NAME, null,
                    selection, new String[]{listId}, null, null, null, null);

        } else if (loaderId == AppConstants.Loaders.LOAD_RECOMMENDATIONS) {


            String selection = DatabaseColumns.TAG_NAME + SQLConstants.EQUALS_ARG + SQLConstants.AND +
                    DatabaseColumns.USER_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableRecommendations.NAME, null,
                    selection, new String[]{mSelectedCategoryName, mUserId}, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_SUGGESTIONS_TAGS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());

            mTags = new Tags[cursor.getCount()];
            cursor.moveToFirst();
            mAllGocNames = new String[cursor.getCount()];
            mGocColors = new String[cursor.getCount()];
            mAllGocIds = new String[cursor.getCount()];
            for (int i = 0; i < cursor.getCount(); i++) {

                mTags[i] = new Tags(cursor.getString(
                        cursor.getColumnIndex(DatabaseColumns.NAME)),
                        cursor.getString(
                                cursor.getColumnIndex(DatabaseColumns.ID)), "image_url");
                mAllGocNames[i] = mTags[i].getName().toUpperCase();
                mGocColors[i] = cursor.getString(
                        cursor.getColumnIndex(DatabaseColumns.COLOR));
                mAllGocIds[i] = cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID));

                if (i == cursor.getCount() - 1) {
                    mProgressDialog.dismiss();
                }
                cursor.moveToNext();


            }

        }

        if (loader.getId() == AppConstants.Loaders.LOAD_RECOMMENDATIONS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {
                mRecommendationListAdapter.swapCursor(cursor);

                if (cursor.getCount() > 0) {
                    mReferralTextNonEdit.setVisibility(View.VISIBLE);
                    mReferralText.setVisibility(View.VISIBLE);
                }


            }
        }


        if (loader.getId() == AppConstants.Loaders.LOAD_CATEGORIES) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());

            cursor.moveToFirst();
            mAllGocCategories = new String[cursor.getCount()];
            mAllSubCategoryId = new String[cursor.getCount()];


            for (int i = 0; i < cursor.getCount(); i++) {


                String subcategory = cursor.getString(
                        cursor.getColumnIndex(DatabaseColumns.NAME));

                mAllGocCategories[i] = subcategory;
                mAllSubCategoryId[i] = cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID));

                if (i == cursor.getCount() - 1) {
                    mProgressDialog.dismiss();
                }
                cursor.moveToNext();

            }

        }

        if (loader.getId() == AppConstants.Loaders.LOAD_PROFILE_CARD) {
            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());

            cursor.moveToFirst();

            if (!mIsUpdated) {
                updateView(cursor);
                mIsUpdated = true;
            }


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_RECOMMENDATIONS) {
            mRecommendationListAdapter.swapCursor(null);
        }
    }

    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
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

            case HttpConstants.ApiResponseCodes.GET_RECOMMENDATIONS: {
                GetRecommendationResponseModel recommendationResponseModel = ((GetRecommendationResponseModel) model);

                mProgressBar.setVisibility(View.GONE);

                for (int i = 0; i < recommendationResponseModel.recommendations.size(); i++) {


                    ContentValues values = new ContentValues();

                    values.put(DatabaseColumns.ID, recommendationResponseModel.recommendations.get(i).id);
                    values.put(DatabaseColumns.WALL_ID, recommendationResponseModel.recommendations.get(i).wall_id);
                    values.put(DatabaseColumns.USER_NAME, recommendationResponseModel.recommendations.get(i).name);
                    values.put(DatabaseColumns.TAG_NAME, recommendationResponseModel.recommendations.get(i).tag_name);
                    values.put(DatabaseColumns.COMMENT, recommendationResponseModel.recommendations.get(i).comment);
                    values.put(DatabaseColumns.USER_IMAGE, recommendationResponseModel.recommendations.get(i).image_url);
                    values.put(DatabaseColumns.USER_ID, mUserId);
                    values.put(DatabaseColumns.TAG_ID, mSelectedTagId);


                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_RECOMMENDATIONS, getTaskTag(), values,
                            TableRecommendations.NAME, values, selection, new String[]{recommendationResponseModel.recommendations.get(i).id,
                            }, false, this);


                }


                break;
            }

            case HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE: {
                GoogleGeocodeResponse googleGeocodeResponse = ((GoogleGeocodeResponse) model);
                if (googleGeocodeResponse.results.size() != 0) {
                    mAddress = googleGeocodeResponse.results.get(0).getAddress();
                }
                break;
            }

            case HttpConstants.ApiResponseCodes.GET_SUB_CATEGORIES: {
                //TODO hide the swipe refreshing
            }
            break;

            case HttpConstants.ApiResponseCodes.GET_SUGGESTIONS: {

                KeywordSuggestionsResponseModel keywordSuggestionsResponseModel = ((KeywordSuggestionsResponseModel) model);


                if ((keywordSuggestionsResponseModel.suggestions != null)
                        && (keywordSuggestionsResponseModel.suggestions.size() > 0)) {
                    mKeywords.onSuggestionsFetched(mQ, keywordSuggestionsResponseModel.suggestions, true);
                }


            }
            break;

            case HttpConstants.ApiResponseCodes.CREATE_LISTING: {

                int i = 0;

                CreateListingResponseModel createListingResponseModel = ((CreateListingResponseModel) model);

                //add values to the profile cards
                String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


                ContentValues valuesProfileCard = new ContentValues();
                valuesProfileCard.put(DatabaseColumns.ID, createListingResponseModel.listing.id);
                valuesProfileCard.put(DatabaseColumns.USER_ID, mUserId);
                valuesProfileCard.put(DatabaseColumns.REFERRAL_COUNT, createListingResponseModel.listing.referral_count);

                List<String> keywords = new ArrayList<String>();

                for (CreateListingResponseModel.Listing.ListingKeywords keywordsList : createListingResponseModel.listing.listing_keywords) {
                    keywords.add(" " + keywordsList.name.substring(0, 1).toUpperCase() + keywordsList.name.substring(1));
                }

                valuesProfileCard.put(DatabaseColumns.SUB_HEADING, TextUtils.join(",", keywords));

                if (createListingResponseModel.listing.listing_links.size() != 0) {
                    valuesProfileCard.put(DatabaseColumns.URL, createListingResponseModel.listing.listing_links.get(0).url);
                }
                valuesProfileCard.put(DatabaseColumns.GROUP_ID, createListingResponseModel.listing.group_id);
                valuesProfileCard.put(DatabaseColumns.GROUP_NAME, createListingResponseModel.listing.group_name);
                valuesProfileCard.put(DatabaseColumns.COLOR, createListingResponseModel.listing.group_color);
                valuesProfileCard.put(DatabaseColumns.SUBGROUP_ID, createListingResponseModel.listing.tag_id);
                valuesProfileCard.put(DatabaseColumns.SUBGROUP_NAME, createListingResponseModel.listing.tag_name);


                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_PROFILE_CARDS, getTaskTag(), valuesProfileCard,
                        TableProfileCards.NAME, valuesProfileCard, selection, new String[]{createListingResponseModel.listing.id}, true, this);

                mProgressDialog.dismiss();

                break;
            }

            case HttpConstants.ApiResponseCodes.UPDATE_LISTING: {
                int i = 0;

                CreateListingResponseModel createListingResponseModel = ((CreateListingResponseModel) model);

                //add values to the profile cards
                String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


                ContentValues valuesProfileCard = new ContentValues();
                valuesProfileCard.put(DatabaseColumns.ID, createListingResponseModel.listing.id);
                valuesProfileCard.put(DatabaseColumns.USER_ID, mUserId);
                valuesProfileCard.put(DatabaseColumns.REFERRAL_COUNT, createListingResponseModel.listing.referral_count);

                List<String> keywords = new ArrayList<String>();

                for (CreateListingResponseModel.Listing.ListingKeywords keywordsList : createListingResponseModel.listing.listing_keywords) {
                    keywords.add(keywordsList.name);
                }

                valuesProfileCard.put(DatabaseColumns.SUB_HEADING, TextUtils.join(",", keywords));

                if (createListingResponseModel.listing.listing_links.size() != 0) {
                    valuesProfileCard.put(DatabaseColumns.URL, createListingResponseModel.listing.listing_links.get(0).url);
                }
                valuesProfileCard.put(DatabaseColumns.GROUP_ID, createListingResponseModel.listing.group_id);
                valuesProfileCard.put(DatabaseColumns.GROUP_NAME, createListingResponseModel.listing.group_name);
                valuesProfileCard.put(DatabaseColumns.SUBGROUP_ID, createListingResponseModel.listing.tag_id);
                valuesProfileCard.put(DatabaseColumns.SUBGROUP_NAME, createListingResponseModel.listing.tag_name);
                valuesProfileCard.put(DatabaseColumns.COLOR, createListingResponseModel.listing.group_color);


                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_PROFILE_CARDS, getTaskTag(), valuesProfileCard,
                        TableProfileCards.NAME, valuesProfileCard, selection, new String[]{createListingResponseModel.listing.id}, true, this);


                userRefresh(true);

                break;
            }

            default:
                break;
        }
    }

    @Override
    public void failure(int requestId, int errorCode, String message) {

        mProgressDialog.dismiss();
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
    }


    /**
     * Method to handle click on goc
     */
    private void showChooseGocDialog() {
        final int[] materialColors = getActivity().getResources().getIntArray(R.array.collectionListColors);


        new MaterialDialog.Builder(getActivity())
                .items(mAllGocNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                                   @Override
                                   public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {


                                       mActionColor = mGocColors[which];
                                       mGocName.setText(mAllGocNames[which]);
                                       mGocNameSelected = mAllGocNames[which];
                                       mCategoryName.setText("Subcategory");

                                       mGocIdSelected = mAllGocIds[which];
                                       colorizeActionBar(Color.parseColor(mGocColors[which]), mToolbar);
                                       colorizeView(Color.parseColor(mGocColors[which]), mGocFrame);

                                       loadSubCategories(mTags[which].getId());
                                   }

                               }

                ).show();

    }

    /**
     * Method to handle click on goc
     */
    private void showChooseCategoryDialog() {

        new MaterialDialog.Builder(getActivity())
                .items(mAllGocCategories)
                .itemsCallback(new MaterialDialog.ListCallback() {
                                   @Override
                                   public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                       mSelectedCategoryName = mAllGocCategories[which];
                                       mCategoryName.setText(mSelectedCategoryName);
                                       mSelectedTagId = mAllSubCategoryId[which];
                                   }

                               }

                ).show();

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.goc_layout) {
            if (!mOtherUser) {
                showChooseGocDialog();
            }
        } else if (v.getId() == R.id.category_frame_layout) {
            if (TextUtils.isEmpty(mGocIdSelected)) {
                Toast.makeText(getActivity(), getResources().getString(R.string.select_category_first), Toast.LENGTH_SHORT).show();
            } else {
                showChooseCategoryDialog();
            }
        } else if (v.getId() == R.id.fabbutton) {
            if (mIsUpdate) {
                updateCard();
            } else {
                createCard();
            }
        } else if (v.getId() == R.id.url) {

            if (((TextView) v).getText().toString().startsWith("http://") ||
                    ((TextView) v).getText().toString().startsWith("https://")) {
                final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(((TextView) v).getText().toString()));
                getActivity().startActivity(intent);
            } else if (((TextView) v).getText().toString().startsWith("www.")) {
                final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://" + ((TextView) v).getText().toString()));
                getActivity().startActivity(intent);
            }
        }
    }

    private void createCard() {

        createListing();


    }

    private void createListing() {
        //            //creating the listing
        CreateListingRequestModel createListingRequestModel = new CreateListingRequestModel();


        //createListingRequestModel.setTag_ids(mTagsIds);
        if (mAddress != null) {
            if (mAddress.length > 2) {
                createListingRequestModel.listing.setState(mAddress[mAddress.length - 2]);
                createListingRequestModel.listing.setCountry(mAddress[mAddress.length - 1]);
                createListingRequestModel.listing.setCity(mAddress[mAddress.length - 3]);
            }

        }
        if (!mLatitude.equals("")) {
            createListingRequestModel.listing.setLatitude
                    (mLatitude);
            createListingRequestModel.listing.setLongitude
                    (mLongitude);
        } else {
            createListingRequestModel.listing.setLatitude
                    ("0.0");
            createListingRequestModel.listing.setLongitude
                    ("0.0");
        }


        createListingRequestModel.listing.setTag_id(mSelectedTagId);

        List<String> keyList = new ArrayList<>();

        String keyListArray[] = TextUtils.split(mKeywords.getText().toString(), ",");

        for (String keyword : keyListArray) {
            keyList.add(keyword.trim());

        }

        createListingRequestModel.setKeywords(keyList);

        Link link = new Link("", mUrlText.getText().toString());
        List<Link> linksList = new ArrayList<Link>();
        linksList.add(link);
        createListingRequestModel.setLinks(linksList);
        if (TextUtils.isEmpty(mSelectedTagId)) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_tags_message),
                    Toast.LENGTH_SHORT).show();

        } else {
            RetroCallback retroCallback;
            retroCallback = new RetroCallback(this);
            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.CREATE_LISTING);
            retroCallbackList.add(retroCallback);
            mProgressDialog.show();
            mYeloApi.createListing(createListingRequestModel, retroCallback);
        }

    }


    private void updateCard() {
        //            //creating the listing
        CreateListingRequestModel createListingRequestModel = new CreateListingRequestModel();


        //createListingRequestModel.setTag_ids(mTagsIds);
        if (mAddress != null) {
            if (mAddress.length > 2) {
                createListingRequestModel.listing.setState(mAddress[mAddress.length - 2]);
                createListingRequestModel.listing.setCountry(mAddress[mAddress.length - 1]);
                createListingRequestModel.listing.setCity(mAddress[mAddress.length - 3]);
            }

        }
        if (!mLatitude.equals("")) {
            createListingRequestModel.listing.setLatitude
                    (mLatitude);
            createListingRequestModel.listing.setLongitude
                    (mLongitude);
        } else {
            createListingRequestModel.listing.setLatitude
                    ("0.0");
            createListingRequestModel.listing.setLongitude
                    ("0.0");
        }


        createListingRequestModel.listing.setTag_id(mSelectedTagId);

        List<String> keyList = new ArrayList<>();
        String keyListArray[] = TextUtils.split(mKeywords.getText().toString(), ",");

        for (String keyword : keyListArray) {
            keyList.add(keyword.trim());

        }
        createListingRequestModel.setKeywords(keyList);

        Link link = new Link("", mUrlText.getText().toString());
        List<Link> linksList = new ArrayList<Link>();
        linksList.add(link);
        createListingRequestModel.setLinks(linksList);
        if (TextUtils.isEmpty(mSelectedTagId)) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_tags_message),
                    Toast.LENGTH_SHORT).show();

        } else {
            RetroCallback retroCallback;
            retroCallback = new RetroCallback(this);
            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.UPDATE_LISTING);
            retroCallbackList.add(retroCallback);

            mProgressDialog.show();
            mYeloApi.updateCard(mListingId, createListingRequestModel, retroCallback);
        }

    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.create_card_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {


            case R.id.action_create: {

                //TODO create the card

                return true;
            }


            case android.R.id.home: {

                if (mFromLogin) {
                    if (mFromLogin) {
                        getActivity().finish();
                        final Intent homeActivityIntent = new Intent(getActivity(),
                                HomeActivity.class);
                        startActivityForResult(homeActivityIntent, AppConstants.RequestCodes.HOME);
                    }
                } else {
                    getActivity().onBackPressed();
                }
                return true;
            }


            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }


    @Override
    public void performNetworkQuery(NetworkedAutoCompleteTextView textView, String query) {

        mQ = query;
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SUGGESTIONS);

        retroCallbackList.add(retroCallback);

        final Map<String, String> params = new HashMap<String, String>(3);
        params.put(HttpConstants.Q, query);
        params.put(HttpConstants.Tag_ID, mSelectedTagId);
        params.put(HttpConstants.TYPE, AppConstants.SuggestionType.KEYWORDS);
        mYeloApi.getSuggestions(params, retroCallback);
    }

    @Override
    public void onSuggestionClicked(NetworkedAutoCompleteTextView textView, KeywordSuggestionsResponseModel.Keywords suggestion) {

        if (TextUtils.isEmpty(mKeyText)) {
            mKeyText = suggestion.name;
        } else {
            mKeyText = mKeyText + "," + suggestion.name;
        }
        textView.setText(mKeyText + ",");
        textView.setSelection(textView.getText().length());

    }

    private void updateView(Cursor cursor) {


        if (mOtherUser) {
            mCategoryNameNonEdit.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.SUBGROUP_NAME)));
            mKeywordNonEdit.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.SUB_HEADING)).trim());
            mSelectedTagId = cursor.getString(cursor.getColumnIndex(DatabaseColumns.SUBGROUP_ID));
            if (TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(DatabaseColumns.URL)))) {

                mUrlTextNonEdit.setVisibility(View.GONE);
                mUrlTitleNonEdit.setVisibility(View.GONE);
            } else {
                mUrlTextNonEdit.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.URL)));
                mUrlTextNonEdit.setOnClickListener(this);
            }

            if (TextUtils.isEmpty(mKeywordNonEdit.getText().toString())) {
                mKeywordTitleNonEdit.setVisibility(View.GONE);

            }
            mSelectedCategoryName = mCategoryNameNonEdit.getText().toString();


            setToolbar(mToolbar, "Profile Card", true);
        } else {
            mCategoryName.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.SUBGROUP_NAME)));
            mSelectedCategoryName = mCategoryName.getText().toString();
            mSelectedTagId = cursor.getString(cursor.getColumnIndex(DatabaseColumns.SUBGROUP_ID));
            mKeywords.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.SUB_HEADING)));
            mUrlText.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.URL)));
        }

        mActionColor = cursor.getString(cursor.getColumnIndex(DatabaseColumns.COLOR));
        mGocIdSelected = cursor.getString(cursor.getColumnIndex(DatabaseColumns.GROUP_ID));

        if (isAttached()) {
            colorizeActionBar(Color.parseColor(mActionColor), mToolbar);
            colorizeView(Color.parseColor(mActionColor), mGocFrame);
        }


        loadReferralsTagsList(mSelectedTagId);


    }

    @Override
    public boolean onBackPressed() {
        if (mFromLogin) {
            getActivity().finish();
            final Intent homeActivityIntent = new Intent(getActivity(),
                    HomeActivity.class);
            startActivityForResult(homeActivityIntent, AppConstants.RequestCodes.HOME);
            return true;
        } else {
            return super.onBackPressed();
        }
    }

    @Override
    public void onRecommendationClicked(View view, String wallId) {

        final Intent wallPostIntent = new Intent(getActivity(),
                WallPostActivity.class);
        wallPostIntent.putExtra(AppConstants.Keys.ID, wallId);
        wallPostIntent.putExtra(AppConstants.Keys.FROM_WALL, true);
        startActivity(wallPostIntent);
    }
}
