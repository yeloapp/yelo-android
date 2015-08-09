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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.activities.ChatScreenActivity;
import red.yelo.activities.CreateServiceCardActivity;
import red.yelo.activities.ServiceCardExpandedActivity;
import red.yelo.adapters.ServiceCardsAdapter;
import red.yelo.adapters.ServiceCardsAdapter.CardActionsClickListener;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableServices;
import red.yelo.http.HttpConstants;
import red.yelo.http.HttpConstants.ApiResponseCodes;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.response.GetServiceCardResponseModel;
import red.yelo.retromodels.response.KeywordSuggestionsResponseModel.Keywords;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;
import red.yelo.utils.SearchViewNetworkQueryHelper;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.utils.mugen.Mugen;
import red.yelo.utils.mugen.MugenCallbacks;
import red.yelo.widgets.autocomplete.INetworkSuggestCallbacks;
import red.yelo.widgets.autocomplete.NetworkedAutoCompleteTextView;


public class ServiceCardsFragment extends AbstractYeloFragment implements DBInterface.AsyncDbQueryCallback,
        AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>,
        RetroCallback.RetroResponseListener, CardActionsClickListener, SwipeRefreshLayout.OnRefreshListener,
        INetworkSuggestCallbacks, SearchViewNetworkQueryHelper.NetworkCallbacks,
        MenuItemCompat.OnActionExpandListener, View.OnClickListener {
    public static final String TAG = "ServiceCardsFragment";

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    /**
     * List of all Service Cards
     */
    public RecyclerView mServiceCardListView;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    /* Adapter responsible for blowing list of service cards */
    private ServiceCardsAdapter mServiceCardsAdapter;

    private int mScreenType;
    private String mUserId, mCategoryId;
    private boolean mUserOwner, mSearchScreen;

    private Toolbar mToolbar;

    private String mSearchKeyword = "";

    private View mEmptyView;

    private SearchView mSearchView;
    private SearchViewNetworkQueryHelper mSearchNetworkQueryHelper;

    private int mCurrentpage = 1;

    private boolean mIsLoading = false, allFetched, allLoaded;


    //*******************************************************************
    // Life Cycle Related
    //*******************************************************************


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater.inflate(R.layout.fragment_service_list, container, false);

        initialiseViews(contentView, savedInstanceState);
        initialiseFragmentView(getArguments());

        //this function will update the view according to the owner (editable /non editable)
        updateView(mUserOwner);

        return contentView;
    }


    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
    }

    //*******************************************************************
    // View Related
    //*******************************************************************


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Recycler view Load more implementation using open source library called Mugen

        if (mScreenType != AppConstants.ServiceScreenType.PROFILE) {
            Mugen.with(mServiceCardListView, new MugenCallbacks() {
                @Override
                public void onLoadMore() {

                    if (!mIsLoading) {
                        allLoaded = false;
                        //increments the pagerNumber on loadmore
                        mCurrentpage = mCurrentpage + 1;
                        fetchCategoryCards(mCategoryId, mCurrentpage);

                    }
                }

                @Override
                public boolean isLoading() {
                    return mIsLoading;
                }

                @Override
                public boolean hasLoadedAllItems() {
                    return allFetched;

                }
            }).start();
        }

    }


    private void updateView(boolean isUserOwner) {
        if (isUserOwner) {
            inflateEditableView();
        } else {
            inflateNormalView();
        }
    }


    private void inflateEditableView() {

    }


    private void inflateNormalView() {

    }


    private void showRefreshing() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);
            }
        }, 500);
    }

    private void initialiseFragmentView(Bundle extras) {

        if (extras != null) {
            mScreenType = extras.getInt(AppConstants.Keys.SERVICE_SCREEN_TYPE, 1);

            if (mScreenType == AppConstants.ServiceScreenType.PROFILE) {
                mUserId = extras.getString(AppConstants.Keys.USER_ID);
                if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
                    mUserOwner = true;
                } else {
                    mUserOwner = false;
                }
                fetchUserCards(mUserId);
                loadUserServiceCards();
                showRefreshing();

            } else if (mScreenType == AppConstants.ServiceScreenType.SEARCH_WITH_CATEGORY) {
                mToolbar.setVisibility(View.VISIBLE);
                setToolbar(mToolbar, extras.getString(AppConstants.Keys.CATEGORY_NAME).toUpperCase(), false);
                mCategoryId = extras.getString(AppConstants.Keys.CATEGORY_ID);
                loadCategoryServiceCards();
                mCurrentpage = 1;
                fetchCategoryCards(mCategoryId, mCurrentpage);
                mSearchScreen = true;
                setHasOptionsMenu(true);

                showRefreshing();

            } else if (mScreenType == AppConstants.ServiceScreenType.SEARCH_WITH_KEYWORDS) {
                mSearchScreen = true;
                mToolbar.setVisibility(View.VISIBLE);
                setToolbar(mToolbar);
                setHasOptionsMenu(true);
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        }
    }


    private void initialiseViews(View contentView, Bundle savedInstance) {

        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.padding, null);
        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);
        mServiceCardsAdapter = new ServiceCardsAdapter(getActivity(), this, headerView, mUserOwner);

        mServiceCardListView = (RecyclerView) contentView.findViewById(R.id.service_card_list);
        mServiceCardListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mServiceCardListView.setHasFixedSize(false);

        mServiceCardListView.setAdapter(mServiceCardsAdapter);

        mServiceCardListView.setVerticalScrollBarEnabled(false);

        mEmptyView = contentView.findViewById(R.id.empty_view);
        mEmptyView.setVisibility(View.GONE);

        TextView addCard = (TextView) mEmptyView.findViewById(R.id.add_card);
        addCard.setOnClickListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.white);
        mSwipeRefreshLayout.setProgressBackgroundColor(R.color.primaryColor);
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mSwipeRefreshLayout.setEnabled(true);


    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
    }


    private void loadKeywordSearchCards() {

    }

    //*******************************************************************
    // Data Related functions
    //*******************************************************************


    private void loadUserServiceCards() {
        if (isAttached())
            getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_USER_SERVICE_CARDS, null, this);
    }


    private void loadCategoryServiceCards() {
        if (isAttached())
            getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_CATEGORY_SERVICE_CARDS, null, this);
    }


    private void loadSearchKeyWords(String keyword) {
        Bundle bundle = new Bundle();
        bundle.putString(AppConstants.Keys.KEYWORDS, keyword);
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SERVICE_BASED_ON_SEARCH, bundle, this);
    }

    private void loadSearchKeyWords(String keyword, String categoryId) {
        if (isAttached()) {
            Bundle bundle = new Bundle();
            bundle.putString(AppConstants.Keys.KEYWORDS, keyword);
            bundle.putString(AppConstants.Keys.CATEGORY_ID, categoryId);

            getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_SERVICE_BASED_ON_CATEGORY_SEARCH, bundle, this);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_USER_SERVICE_CARDS) {
            String selection = DatabaseColumns.USER_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableServices.NAME, null, selection, new String[]{mUserId}, null, null, null, null);
        } else if (loaderId == AppConstants.Loaders.LOAD_CATEGORY_SERVICE_CARDS) {
            String selection = DatabaseColumns.GROUP_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableServices.NAME, null, selection, new String[]{mCategoryId}, null, null, null, null);
        } else if (loaderId == AppConstants.Loaders.LOAD_SERVICE_BASED_ON_SEARCH) {
            String keyword = "";
            if (bundle != null && bundle.containsKey(AppConstants.Keys.KEYWORDS)) {
                keyword = bundle.getString(AppConstants.Keys.KEYWORDS);
            }
            String selection = DatabaseColumns.TITLE + SQLConstants.LIKE_ARG + SQLConstants.OR + DatabaseColumns.SERVICE_DESCRIPTION + SQLConstants.LIKE_ARG;
            return new SQLiteLoader(getActivity(), true, TableServices.NAME, null, selection, new String[]{"%" + keyword + "%", "%" + keyword + "%"}, null, null, null, null);
        } else if (loaderId == AppConstants.Loaders.LOAD_SERVICE_BASED_ON_CATEGORY_SEARCH) {
            String keyword = "";
            String categoryId = "";
            if (bundle != null && bundle.containsKey(AppConstants.Keys.KEYWORDS)) {
                keyword = bundle.getString(AppConstants.Keys.KEYWORDS);
            }
            categoryId = bundle.getString(AppConstants.Keys.CATEGORY_ID);


            String selection = SQLConstants.PARANTHESIS_OPEN + DatabaseColumns.TITLE + SQLConstants.LIKE_ARG + SQLConstants.OR + DatabaseColumns.SERVICE_DESCRIPTION +
                    SQLConstants.LIKE_ARG + SQLConstants.PARANTHESES_CLOSE + SQLConstants.AND +
                    DatabaseColumns.GROUP_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), true, TableServices.NAME, null, selection, new String[]{"%" + keyword + "%", "%" + keyword + "%", categoryId}, null, null, null, null);
        }

        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_USER_SERVICE_CARDS) {
            Logger.d(TAG, "User service Cursor Loaded with count: %d", cursor.getCount());
            mServiceCardsAdapter.swapCursor(cursor);

            if (cursor.getCount() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);

            }

        } else if (loader.getId() == AppConstants.Loaders.LOAD_CATEGORY_SERVICE_CARDS) {
            Logger.d(TAG, "Category service Cursor Loaded with count: %d", cursor.getCount());
            mServiceCardsAdapter.swapCursor(cursor);
        } else if (loader.getId() == AppConstants.Loaders.LOAD_SERVICE_BASED_ON_SEARCH) {
            Logger.d(TAG, "Search service Cursor Loaded with count: %d", cursor.getCount());
            mServiceCardsAdapter.swapCursor(cursor);
            if (cursor.getCount() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);

            }
        } else if (loader.getId() == AppConstants.Loaders.LOAD_SERVICE_BASED_ON_CATEGORY_SEARCH) {
            Logger.d(TAG, "Search service Cursor Loaded with count: %d", cursor.getCount());
            mServiceCardsAdapter.swapCursor(cursor);
            if (cursor.getCount() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.GONE);

            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }


    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {
        if (taskId == AppConstants.QueryTokens.DELETE_SERVICE_CARD_CATEGORY) {
            mCurrentpage = 1;
            fetchCategoryServiceCardsFromServer(mCategoryId, mCurrentpage + "");
        }
    }


    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {
    }


    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }

    //*******************************************************************
    // HTTP Related functions
    //*******************************************************************


    /**
     * Helper http function to get list of my service cards.
     */
    private void fetchMyServiceCardsFromServer() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_MY_SERVICES);
        retroCallbackList.add(retroCallback);
        mYeloApi.getMyServiceCards(retroCallback);
    }


    /**
     * Helper http function to get list of my service cards.
     */
    private void fetchUsersServiceCardsFromServer(String userId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(ApiResponseCodes.GET_USERS_SERVICES);
        retroCallbackList.add(retroCallback);
        mYeloApi.getUsersServiceCards(userId, retroCallback);
    }


    private void fetchCategoryServiceCardsFromServer(String categoryId, String pageNumber) {
        final Map<String, String> params = new HashMap<String, String>(5);
        params.put(HttpConstants.LATITUDE, SharedPreferenceHelper.getString(R.string.pref_latitude));
        params.put(HttpConstants.LONGITUDE, SharedPreferenceHelper.getString(R.string.pref_longitude));
        params.put(HttpConstants.TYPE, HttpConstants.SearchType.SERVICE_CARD);
        params.put(HttpConstants.PER, "10");
        params.put(HttpConstants.RADIUS, "50");
        params.put(HttpConstants.GROUP_ID, categoryId);
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_CATEGORY_SERVICES);
        retroCallbackList.add(retroCallback);
        params.put(HttpConstants.PAGE, pageNumber);
        mYeloApi.getServiceCards(params, retroCallback);
        mSwipeRefreshLayout.setRefreshing(true);
        mIsLoading = true;
    }


    /**
     * Helper http function to get list of my service cards based on keyword
     */
    private void fetchSearchServiceCardsFromServer(String keyword) {
        final Map<String, String> params = new HashMap<String, String>(5);
        params.put(HttpConstants.LATITUDE, SharedPreferenceHelper.getString(R.string.pref_latitude));
        params.put(HttpConstants.LONGITUDE, SharedPreferenceHelper.getString(R.string.pref_longitude));
        params.put(HttpConstants.TYPE, HttpConstants.SearchType.SERVICE_CARD);
        params.put(HttpConstants.PER, "20");
        params.put(HttpConstants.RADIUS, "50");
        params.put(HttpConstants.TITLE, keyword);
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.SEARCH_SERVICES);
        retroCallbackList.add(retroCallback);
        params.put(HttpConstants.PAGE, "1");
        mYeloApi.getServiceCards(params, retroCallback);
        mSwipeRefreshLayout.setRefreshing(true);

    }

    /**
     * Helper http function to get list of my service cards based on keyword and category
     */
    private void fetchSearchServiceCardsFromServer(String keyword, String categoryId) {
        final Map<String, String> params = new HashMap<String, String>(5);
        params.put(HttpConstants.LATITUDE, SharedPreferenceHelper.getString(R.string.pref_latitude));
        params.put(HttpConstants.LONGITUDE, SharedPreferenceHelper.getString(R.string.pref_longitude));
        params.put(HttpConstants.TYPE, HttpConstants.SearchType.SERVICE_CARD);
        params.put(HttpConstants.GROUP_ID, categoryId);
        params.put(HttpConstants.PER, "20");
        params.put(HttpConstants.RADIUS, "50");
        params.put(HttpConstants.TITLE, keyword);
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.SEARCH_SERVICES);
        retroCallbackList.add(retroCallback);
        params.put(HttpConstants.PAGE, "1");
        mYeloApi.getServiceCards(params, retroCallback);
        mSwipeRefreshLayout.setRefreshing(true);

    }


    @Override
    public void success(Object model, int requestId) {

        if (isAttached()) {
            switch (requestId) {
                case HttpConstants.ApiResponseCodes.GET_MY_SERVICES: {

                    mSwipeRefreshLayout.setRefreshing(false);

                    break;
                }
                case HttpConstants.ApiResponseCodes.GET_USERS_SERVICES: {

                    GetServiceCardResponseModel getUsersServiceCardResponseModel = ((GetServiceCardResponseModel) model);

                    if ((getUsersServiceCardResponseModel.serviceCards.size() == 0) &&
                            AppConstants.UserInfo.INSTANCE.getId().equals(mUserId)) {
                        mEmptyView.setVisibility(View.VISIBLE);
                    } else {
                        mEmptyView.setVisibility(View.GONE);

                    }

                    mSwipeRefreshLayout.setRefreshing(false);

                    break;
                }
                case HttpConstants.ApiResponseCodes.GET_CATEGORY_SERVICES: {
                    GetServiceCardResponseModel getServiceCardResponseModel = ((GetServiceCardResponseModel) model);

                    mIsLoading = false;

                    if ((getServiceCardResponseModel.search.size() + mServiceCardsAdapter.getItemCount()) == 0) {
                        mEmptyView.setVisibility(View.VISIBLE);
                    } else {
                        mEmptyView.setVisibility(View.GONE);

                    }
                    loadCategoryServiceCards();
                    mSwipeRefreshLayout.setRefreshing(false);

                    break;
                }
                case ApiResponseCodes.SEARCH_SERVICES: {

                    GetServiceCardResponseModel getSearchServiceCardResponseModel = ((GetServiceCardResponseModel) model);
//                    if (getSearchServiceCardResponseModel.search.size() == 0) {
//                        mEmptyView.setVisibility(View.VISIBLE);
//                    } else {
//                        mEmptyView.setVisibility(View.GONE);
//
//                    }

                    if (TextUtils.isEmpty(mCategoryId)) {
                        loadSearchKeyWords(mSearchKeyword);
                    } else {
                        loadSearchKeyWords(mSearchKeyword, mCategoryId);
                    }
                    mSwipeRefreshLayout.setRefreshing(false);

                    break;
                }
                default:
                    break;
            }
        }
    }


    @Override
    public void failure(int requestId, int errorCode, String message) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (AppConstants.UserInfo.INSTANCE.getId().equals(mUserId)) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);

        }
    }

    //*******************************************************************
    // Interface Implementations
    //*******************************************************************


    @Override
    public void onRefresh() {
        if (mScreenType == AppConstants.ServiceScreenType.PROFILE && mUserOwner) {
            fetchUsersServiceCardsFromServer(mUserId);
        } else if (mScreenType == AppConstants.ServiceScreenType.PROFILE && !mUserOwner) {
            fetchUsersServiceCardsFromServer(mUserId);
        } else if (mScreenType == AppConstants.ServiceScreenType.SEARCH_WITH_CATEGORY) {

            String selection = DatabaseColumns.GROUP_ID + SQLConstants.EQUALS_ARG;
            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_SERVICE_CARD_CATEGORY,
                    getTaskTag(), null, TableServices.NAME, selection, new String[]{mCategoryId}, true, this);

        } else if (mScreenType == AppConstants.ServiceScreenType.SEARCH_WITH_KEYWORDS) {
            /**
             * No pull to refresh in search by keywords model.
             */
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }


    /**
     * Method callback when the chat action is clicked
     *
     * @param view          The View that was clicked
     * @param userId        The user id of the post author
     * @param userName      The name of the user
     * @param userImage     The image url of the user
     * @param cardId
     * @param tagName
     * @param contactNumber The contact number of the user who own the service card
     */
    @Override
    public void onBookClicked(View view, String userId, String userName, String userImage, String cardId, String tagName, String contactNumber, String price, String title) {
        /**
         * TODO: Complete
         */

        loadChat(userId, userName, userImage, tagName, contactNumber, price, title);
    }

    @Override
    public void onPostClicked(View view, String userId, String userName, String userImage,
                              String cardId, String tagName, String contactNumber,
                              String price, String title, String serviceImage, String description,
                              String groupName, String duration, String deliverable, String groupId,
                              String subcategoryId,String rating,String ratingCount) {

        Intent intent = new Intent(getActivity(), ServiceCardExpandedActivity.class);


        intent.putExtra(AppConstants.Keys.SERVICE_IMAGE, serviceImage);
        intent.putExtra(AppConstants.Keys.USER_IMAGE, userImage);
        intent.putExtra(AppConstants.Keys.TITLE, title);
        intent.putExtra(AppConstants.Keys.DESCRIPTION, description);
        intent.putExtra(AppConstants.Keys.PRICE, price);
        intent.putExtra(AppConstants.Keys.GROUP_NAME, groupName);
        intent.putExtra(AppConstants.Keys.SUBCATEGORY_NAME, tagName);
        intent.putExtra(AppConstants.Keys.USER_ID, userId);
        intent.putExtra(AppConstants.Keys.USER_NAME, userName);
        intent.putExtra(AppConstants.Keys.SERVICE_ID, cardId);
        intent.putExtra(AppConstants.Keys.CONTACT_NUMBER, contactNumber);
        intent.putExtra(AppConstants.Keys.DELIVERABLE, deliverable);
        intent.putExtra(AppConstants.Keys.DURATION, duration);
        intent.putExtra(AppConstants.Keys.GROUP_ID, groupId);
        intent.putExtra(AppConstants.Keys.SUBCATEGORY_ID, subcategoryId);
        intent.putExtra(AppConstants.Keys.RATING, rating);
        intent.putExtra(AppConstants.Keys.RATING_COUNT, ratingCount);



        startActivity(intent);

    }


    /**
     * Loads the actual chat screen. This is used in the case where the user taps on an item in the
     * list of chats
     */
    private void loadChat(String userId, String chatName, String image, String tagName, String contactNumber, String price, String title) {

        final String chatId = Utils.generateChatId(userId, AppConstants.UserInfo.INSTANCE.getId());

        if (getActivity() != null) {

            final Intent chatScreenActivity = new Intent(getActivity(), ChatScreenActivity.class);
            chatScreenActivity.putExtra(AppConstants.Keys.USER_ID, userId);
            chatScreenActivity.putExtra(AppConstants.Keys.CHAT_ID, chatId);
            chatScreenActivity.putExtra(AppConstants.Keys.CHAT_TITLE, chatName);
            chatScreenActivity.putExtra(AppConstants.Keys.PROFILE_IMAGE, image);
            chatScreenActivity.putExtra(AppConstants.Keys.FROM_WALL, true);
            chatScreenActivity.putExtra(AppConstants.Keys.TAG_NAME, tagName);
            chatScreenActivity.putExtra(AppConstants.Keys.CONTACT_NUMBER, contactNumber);
            chatScreenActivity.putExtra(AppConstants.Keys.SERVICE_PRICE, price);
            chatScreenActivity.putExtra(AppConstants.Keys.TITLE, title);
            chatScreenActivity.putExtra(AppConstants.Keys.MY_ID, AppConstants.UserInfo.INSTANCE.getId());

            startActivity(chatScreenActivity);
        }
    }


    @Override
    public void performNetworkQuery(NetworkedAutoCompleteTextView textView, String query) {
        mSearchKeyword = textView.getText().toString();
        if (TextUtils.isEmpty(mCategoryId)) {
            loadSearchKeyWords(mSearchKeyword);
            fetchSearchServiceCardsFromServer(mSearchKeyword);
        } else {
            loadSearchKeyWords(mSearchKeyword, mCategoryId);
            fetchSearchServiceCardsFromServer(mSearchKeyword, mCategoryId);
        }
    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.refer_user_searchmenu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_search);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        MenuItemCompat.setOnActionExpandListener(menuItem, this);
        if (TextUtils.isEmpty(mCategoryId)) {
            MenuItemCompat.expandActionView(menuItem);
        }

        if (isAttached()) {
            if (mSearchView.getChildAt(0) != null) {
                LinearLayout ll = (LinearLayout) mSearchView.getChildAt(0);
                LinearLayout ll2 = (LinearLayout) ll.getChildAt(2);
                LinearLayout ll3 = (LinearLayout) ll2.getChildAt(1);
                SearchView.SearchAutoComplete autoComplete = (SearchView.SearchAutoComplete) ll3.getChildAt(0);
// set the hint text color
                autoComplete.setTextColor(getResources().getColor(R.color.dark_yelo));
                // textView.setHintTextColor(Color.WHITE);
                //MenuItemCompat.setActionView(menuItem, mSearchView);
            }
            mSearchNetworkQueryHelper = new SearchViewNetworkQueryHelper(mSearchView, this);
            mSearchNetworkQueryHelper.setSuggestCountThreshold(1);
            mSearchNetworkQueryHelper.setSuggestWaitThreshold(400);
        }

    }

    @Override
    public void onSuggestionClicked(NetworkedAutoCompleteTextView textView, Keywords suggestion) {

    }

    //*******************************************************************
    // Utility functions
    //*******************************************************************


    private void fetchUserCards(String userId) {

        fetchUsersServiceCardsFromServer(userId);
        loadUserServiceCards();
    }


    private void fetchCategoryCards(String categoryId, int pageNumber) {
        if (!SharedPreferenceHelper.getString(R.string.pref_latitude).equals("")) {
            fetchCategoryServiceCardsFromServer(categoryId, pageNumber + "");

        }
    }


    public static ServiceCardsFragment newInstance(Bundle bundle) {
        ServiceCardsFragment f = new ServiceCardsFragment();
        f.setArguments(bundle);
        return f;
    }


    public static ServiceCardsFragment newInstance() {
        ServiceCardsFragment f = new ServiceCardsFragment();
        return f;
    }

    //*******************************************************************
    // Enforced by base class
    //*******************************************************************


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void performQuery(SearchView searchView, String query) {
        mSearchKeyword = query;
        if (TextUtils.isEmpty(mCategoryId)) {
            loadSearchKeyWords(mSearchKeyword);
            fetchSearchServiceCardsFromServer(mSearchKeyword);
        } else {
            loadSearchKeyWords(mSearchKeyword, mCategoryId);
            fetchSearchServiceCardsFromServer(mSearchKeyword, mCategoryId);
        }
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        if (!TextUtils.isEmpty(mCategoryId)) {
            mCurrentpage = 1;
            fetchCategoryCards(mCategoryId, mCurrentpage);
            loadSearchKeyWords("", mCategoryId);

        } else {
            getActivity().finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_card) {
            final Intent createServiceCard = new Intent(getActivity(), CreateServiceCardActivity.class);
            createServiceCard.putExtra(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE.getId());

            startActivity(createServiceCard);
        }
    }

    //*******************************************************************
    // End of class
    //*******************************************************************
}
