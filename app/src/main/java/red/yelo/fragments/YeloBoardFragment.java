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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.otto.Subscribe;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.activities.ChatScreenActivity;
import red.yelo.activities.CloseWallActivity;
import red.yelo.activities.EditWallPostActivity;
import red.yelo.activities.SearchLocationActivity;
import red.yelo.activities.TagUserActivity;
import red.yelo.activities.UserProfileActivity;
import red.yelo.activities.WallPostActivity;
import red.yelo.adapters.WallPostAdapter;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.bus.ScrollToTop;
import red.yelo.bus.ShowPostShowcase;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableUsers;
import red.yelo.data.TableWallComments;
import red.yelo.data.TableWallPosts;
import red.yelo.fragments.dialogs.SingleChoiceDialogFragment;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.request.CloseWallRequestModel;
import red.yelo.retromodels.request.ReportAbuseRequestModel;
import red.yelo.retromodels.request.UserDetailsRequestModel;
import red.yelo.retromodels.response.GetCreateWallResponseModel;
import red.yelo.retromodels.response.GetWallItemResponseModel;
import red.yelo.retromodels.response.GetWallResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.utils.mugen.Mugen;
import red.yelo.utils.mugen.MugenCallbacks;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class YeloBoardFragment extends AbstractYeloFragment implements DBInterface.AsyncDbQueryCallback,
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener,
        PopupMenu.OnMenuItemClickListener,
        PopupMenu.OnDismissListener, WallPostAdapter.PostActionsClickListener,
        RetroCallback.RetroResponseListener, ObservableScrollViewCallbacks,
        View.OnClickListener {


    public static final String TAG = "YeloBoardFragment";


    public static final String ARG_INITIAL_POSITION = "ARG_INITIAL_POSITION";


    /**
     * List of Wall posts
     */
    public ObservableRecyclerView mCardListView;

    /**
     * Cursor adapter to hold all the feeds
     */
    private WallPostAdapter mWallListAdapter;


    private String mWallIdForTag;

    /**
     * Date formatter for formatting timestamps for messages
     */
    private DateFormatter mMessageDateFormatter;

    /**
     * Reference to the Dialog Fragment for selecting the wall options
     */
    private SingleChoiceDialogFragment mWallUserOptionsDialog, mWallOtherUserOptionsDialog;

    private String mSelectedWallId, mSelectedTagId;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Empty view for the empty state screen for feed
     */
    private View mEmptyView;

    /**
     * retroCallbackList keeps a record of the api calls made on this fragment, so that when the view
     * is the not present the api calls doesn't give responses here and prevent fragment not attached
     * exception. in OnPause we cancel all the requests to do so
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private GetWallResponseModel mGetWallResponseModel;

    private int mCurrentpage = 1;

    private boolean mIsLoading = false, mUserWalls, allFetched, allLoaded, mFetchedOnOpen, isFetched,
            mDialogFlag = true;

    private int mWallItemSize = 1, mDBNotifyCounter = 0;

    private String mTagId = "", mUserId = "";

    private boolean mReferShowcase, mFetchingWalls;

    private FloatingActionButton mFloatActionButton;

    private Toolbar mToolbar;

    private TextView mChangeLocationText;


    @SuppressLint("ResourceAsColor")//Added for SwipeRefreshLayout
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);

        final View contentView = inflater
                .inflate(R.layout.fragment_yelo_board, container, false);


        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                AppConstants.WALL_DATE_FORMAT);

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);

        Bundle extras = getArguments();

        if (extras != null) {

            if (extras.containsKey(AppConstants.Keys.USER_ID)) {
                mUserId = extras.getString(AppConstants.Keys.USER_ID);
                mUserWalls = true;
            }
        }


        mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.white);
        mSwipeRefreshLayout.setProgressBackgroundColor(R.color.primaryColor);
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mSwipeRefreshLayout.setEnabled(true);

        mEmptyView = contentView.findViewById(R.id.empty_view);

        mChangeLocationText = (TextView) mEmptyView.findViewById(R.id.change_location);
        mChangeLocationText.setOnClickListener(this);


        mCardListView = (ObservableRecyclerView) contentView.findViewById(R.id.yelo_board_list);

        if (!mUserWalls)
            mFloatActionButton = ((HomeScreenFragment) getActivity().getSupportFragmentManager().findFragmentByTag(AppConstants.FragmentTags.HOME_SCREEN)).getFabButton();


        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.padding, null);
        mWallListAdapter = new WallPostAdapter(getActivity(), YeloBoardFragment.this, headerView);
        mCardListView.setAdapter(mWallListAdapter);
        mCardListView.setVerticalScrollBarEnabled(false);

        mCardListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCardListView.setHasFixedSize(false);
        /*this is added to track the scrolling down or up to show the tutorial screen overlay
        accordingly. its a recycler view so we need some implementation in it.*/
        addScrollerListenerForTutorial(mCardListView);


        //current page is the page previously loaded for fetching the feeds from the server.
        //this keeps on incrementing when the user scrolls down to load more
        mCurrentpage = Integer.parseInt(SharedPreferenceHelper.getString(R.string.pref_pages_loaded, 1 + ""));


        if (mUserWalls) {
            //this view is for the user only feed, its a seperate activity so we show toolbar here
            mToolbar.setTitleTextColor(getResources().getColor(R.color.dark_yelo));
            setToolbar(mToolbar);
            loadUserWallMessages();
        } else {

            //this view is for the main feed
            loadWallMessages();
            mToolbar.setVisibility(View.GONE);

        }
        fetchWallMessages(mCurrentpage);

        return contentView;

    }


    /**
     * This api call fetches only the user walls give the userid
     *
     * @param userId user id of the user whos walls we want to fetch
     */
    public void fetchMyWalls(String userId) {

        /*
        retroCallbackList keeps a record of the api calls made on this fragment, so that when the view
        is the not present the api calls doesn't give responses here and prevent fragment not attached
        exception. in OnPause we cancel all the requests to do so
        */

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        //keeping the response code different so as to prevent code duplicacy
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_USER_WALLS);
        retroCallbackList.add(retroCallback);
        mYeloApi.getUserWalls(userId, retroCallback);
        mSwipeRefreshLayout.setRefreshing(true);

    }


    /**
     * This api call fetches the feed wall
     *
     * @param pageNumber the current page to be fetched, increments on loadMore
     */
    private void fetchWallMessages(int pageNumber) {


        final Map<String, String> params = new HashMap<String, String>(5);

        if (!SharedPreferenceHelper.getString(R.string.pref_latitude).equals("")) {
            params.put(HttpConstants.LATITUDE, SharedPreferenceHelper.getString(R.string.pref_latitude));
            params.put(HttpConstants.LONGITUDE, SharedPreferenceHelper.getString(R.string.pref_longitude));
            params.put(HttpConstants.TYPE, HttpConstants.SearchType.WALL);
            params.put(HttpConstants.PER, "10");
            params.put(HttpConstants.RADIUS, "50");


            RetroCallback retroCallback;
            retroCallback = new RetroCallback(this);
            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_ALL_WALLS);
            retroCallbackList.add(retroCallback);

            params.put(HttpConstants.PAGE, pageNumber + "");

            if (!mFetchingWalls) {
                mYeloApi.getWallMessages(params, retroCallback);
                mFetchingWalls = true;
            }

            if (pageNumber != 1) {
                mCurrentpage = pageNumber;
            }

            mIsLoading = true;
            mSwipeRefreshLayout.setRefreshing(true);


        } else {

            //selects the location if location is not present in the app

            Utils.setDefaultLocation(getActivity());
//            final Intent selectLocationActivity = new Intent(getActivity(),
//                    SearchLocationActivity.class);
//            startActivityForResult(selectLocationActivity, AppConstants.RequestCodes.GET_PLACE);

        }
    }


    /**
     * This is added to track the scrolling down or up to show the tutorial screen overlay
     * accordingly. its a recycler view so we need some implementation in it.
     */
    private void addScrollerListenerForTutorial(final ObservableRecyclerView cardListView) {


        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_INITIAL_POSITION)) {
            ViewTreeObserver vto = cardListView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        cardListView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        cardListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
        mCardListView.setScrollViewCallbacks(this);


    }


    private void loadUserWallMessages() {
        mFetchedOnOpen = true;
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_USER_WALL_MESSAGES, null, this);

    }

    private void loadWallMessages() {

        mFetchedOnOpen = true;
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_WALL_MESSAGES, null, this);

    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    /**
     * gets the instance for the fragment to call in a pager
     *
     * @return
     */
    public static YeloBoardFragment newInstance() {
        YeloBoardFragment f = new YeloBoardFragment();
        return f;
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

        if (taskId == AppConstants.QueryTokens.DELETE_WALL_POSTS_SEARCH_RESULTS) {
            mCurrentpage = 1;
            SharedPreferenceHelper.set(R.string.pref_pages_loaded, (mCurrentpage) + "");
            allFetched = false;
            if (mUserWalls) {
                fetchMyWalls(mUserId);
            } else {
                fetchWallMessages(mCurrentpage);
            }
        }

        if (taskId == AppConstants.QueryTokens.DELETE_MY_WALL_POSTS_SEARCH_RESULTS) {
            mCurrentpage = 1;
            SharedPreferenceHelper.set(R.string.pref_pages_loaded, (mCurrentpage) + "");
            allFetched = false;
            fetchMyWalls(mUserId);
        }
    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {


        if (taskId == AppConstants.QueryTokens.UPDATE_WALLPOST) {

            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;

                mDBNotifyCounter++;

                if (mWallItemSize <= mDBNotifyCounter) {
                    hideRefreshing();
                    DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, getTaskTag(), null
                            , TableWallPosts.NAME, null, values, true, this);
                } else if (mCurrentpage == 1) {
                    DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, getTaskTag(), null
                            , TableWallPosts.NAME, null, values, true, this);
                } else {
                    DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, getTaskTag(), null
                            , TableWallPosts.NAME, null, values, false, this);
                }
            } else {
                hideRefreshing();
            }


        } else if (taskId == AppConstants.QueryTokens.UPDATE_WALLCOMMENTS) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLCOMMENT, getTaskTag(), null
                        , TableWallComments.NAME, null, values, true, this);
            }
        } else if (taskId == AppConstants.QueryTokens.UPDATE_USERS) {
            if (updateCount == 0) {

                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_USERS, getTaskTag(), null
                        , TableUsers.NAME, null, values, true, this);
            }
        }

    }


    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

        if (taskId == AppConstants.QueryTokens.QUERY_WALL_DETAILS) {

            if (cursor.moveToFirst()) {
                shareWall(cursor.getString(cursor.getColumnIndex(DatabaseColumns.MESSAGE)));
            }
            cursor.close();

        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_WALL_MESSAGES) {

            return new SQLiteLoader(getActivity(), false, TableWallPosts.NAME, null,
                    null, null, null, null, DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT
                    + SQLConstants.DESCENDING, null);

        }

        if (loaderId == AppConstants.Loaders.LOAD_USER_WALL_MESSAGES) {

            String selection = DatabaseColumns.USER_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableWallPosts.NAME, null,
                    selection, new String[]{mUserId}, null, null, DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT
                    + SQLConstants.DESCENDING, null);

        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_WALL_MESSAGES) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            mWallListAdapter.swapCursor(cursor);

            if (cursor.getCount() == 0 && !isFetched) {
                fetchWallMessages(mCurrentpage);
                isFetched = true;
            }
            if (cursor.getCount() > 0 && !isFetched && mTagId.equals("")) {
                mDBNotifyCounter = 0;
                fetchWallMessages(1);
                isFetched = true;
            }


        }

        if (loader.getId() == AppConstants.Loaders.LOAD_USER_WALL_MESSAGES) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {
                mWallListAdapter.swapCursor(cursor);
            }

            if (cursor.getCount() == 0 && !isFetched) {
                fetchMyWalls(mUserId);
                isFetched = true;
            }
            if (cursor.getCount() > 0 && !isFetched && mTagId.equals("")) {
                mDBNotifyCounter = 0;
                fetchMyWalls(mUserId);
                isFetched = true;
            }

        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        if (loader.getId() == AppConstants.Loaders.LOAD_WALL_MESSAGES) {
            mWallListAdapter.swapCursor(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();


        isFetched = false;
        mDBNotifyCounter = 0;
        mFetchedOnOpen = false;


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {


            //get the results of the location selected here from SearchLocationFragment
            if (requestCode == AppConstants.RequestCodes.GET_PLACE) {

                mSwipeRefreshLayout.setEnabled(true);
                isFetched = false;
                mDBNotifyCounter = 0;
                mFetchedOnOpen = false;
                mCurrentpage = 1;
                mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
                allFetched = false;
                mIsLoading = false;
                SharedPreferenceHelper.set(R.string.pref_pages_loaded, mCurrentpage + "");
                loadWallMessages();

            }


        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Recycler view Load more implementation using open source library called Mugen
        Mugen.with(mCardListView, new MugenCallbacks() {
            @Override
            public void onLoadMore() {

                if (!mIsLoading) {
                    allLoaded = false;
                    mDBNotifyCounter = 0;
                    mFetchedOnOpen = false;
                    mCurrentpage = Integer.parseInt(SharedPreferenceHelper.getString(R.string.pref_pages_loaded, 1 + ""));
                    //increments the pagerNumber on loadmore
                    SharedPreferenceHelper.set(R.string.pref_pages_loaded, (mCurrentpage + 1) + "");

                    fetchWallMessages(Integer.parseInt(SharedPreferenceHelper.getString(R.string.pref_pages_loaded)));

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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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


    /**
     * Show fragment for tagging users
     */
    private void showTagUserFragment(String wallId, String wallUserId, String tagCount) {


        if (isAttached()) {
            final Intent tagUserActivityIntent = new Intent(getActivity(),
                    TagUserActivity.class);
            tagUserActivityIntent.putExtra(AppConstants.Keys.WALL_ID, wallId);
            tagUserActivityIntent.putExtra(AppConstants.Keys.USER_ID, wallUserId);
            tagUserActivityIntent.putExtra(AppConstants.Keys.TAG_USER_COUNT, Integer.parseInt(tagCount));
            startActivity(tagUserActivityIntent);
        }

    }


    @Override
    public boolean willHandleDialog(DialogInterface dialog) {
        if ((mWallUserOptionsDialog != null)) {
            if (mWallUserOptionsDialog.getDialog() != null) {
                if (mWallUserOptionsDialog.getDialog().equals(dialog)) {
                    return true;
                }
            }
        }

        if ((mWallOtherUserOptionsDialog != null)) {
            if (mWallOtherUserOptionsDialog.getDialog() != null) {
                if (mWallOtherUserOptionsDialog.getDialog().equals(dialog)) {
                    return true;
                }
            }
        }

        return super.willHandleDialog(dialog);
    }

    @Override
    public void onDialogClick(final DialogInterface dialog, final int which) {


        if ((mWallUserOptionsDialog != null)) {
            if (mWallUserOptionsDialog.getDialog() != null) {
                if (mWallUserOptionsDialog.getDialog().equals(dialog)) {

                    if (which == 0) {

                        final Intent editWallPostIntent = new Intent(getActivity(),
                                EditWallPostActivity.class);
                        editWallPostIntent.putExtra(AppConstants.Keys.EDIT_POST, true);
                        editWallPostIntent.putExtra(AppConstants.Keys.WALL_ID, mSelectedWallId);
                        startActivity(editWallPostIntent);

                    } else if (which == 1) {

                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                getActivity());

                        // set title
                        alertDialogBuilder.setTitle("Confirm");

                        // set dialog message
                        alertDialogBuilder
                                .setMessage(getResources().getString(R.string.delete_wall_alert_message))
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            final DialogInterface dialog,
                                            final int id) {

                                        callDeleteApi(mSelectedWallId);
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            final DialogInterface dialog,
                                            final int id) {
                                        // if this button is clicked, just close
                                        // the dialog box and do nothing
                                        dialog.cancel();
                                    }
                                });

                        // create alert dialog
                        final AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();

                    }
                }
            }
        }

        if ((mWallOtherUserOptionsDialog != null)) {
            if (mWallOtherUserOptionsDialog.getDialog() != null) {
                if (mWallOtherUserOptionsDialog.getDialog().equals(dialog)) {

                    if (which == 0) {


                        Toast.makeText(getActivity(), "clicked other 0", Toast.LENGTH_SHORT).show();

                    } else if (which == 1) {
                        Toast.makeText(getActivity(), "clicked other 1", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    super.onDialogClick(dialog, which);
                }
            }
        }
    }


    /**
     * Api call for deleting the owner wall post
     *
     * @param wallId id of the wall which we want to delete
     */
    private void callDeleteApi(String wallId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.DELETE_WALL);
        retroCallbackList.add(retroCallback);

        mYeloApi.deleteWall(wallId, retroCallback);
        String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

        //deleting from local cache
        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_WALL_POST, getTaskTag(), null, TableWallPosts.NAME,
                selection, new String[]{wallId}, true, this);


    }


    private void openOwnerWallOptionsPopup(View view) {

        final PopupMenu menu = new PopupMenu(getActivity(), view);
        menu.inflate(R.menu.wall_owner_options);
        menu.setOnMenuItemClickListener(YeloBoardFragment.this);
        menu.setOnDismissListener(YeloBoardFragment.this);
        menu.show();
    }

    private void openOtherWallOptionsPopup(View view) {

        final PopupMenu menu = new PopupMenu(getActivity(), view);
        menu.inflate(R.menu.wall_other_options);
        menu.setOnMenuItemClickListener(YeloBoardFragment.this);
        menu.setOnDismissListener(YeloBoardFragment.this);
        menu.show();
    }


    /**
     * Loads the actual chat screen. This is used in the case where the user taps on an item in the
     * list of chats
     */
    private void loadChat(String wallId, String userId, String chatName, String image, String tagName) {

        final String chatId = Utils
                .generateChatId(userId, AppConstants.UserInfo.INSTANCE.getId());

        if (getActivity() != null) {

            final Intent chatScreenActivity = new Intent(getActivity(),
                    ChatScreenActivity.class);
            chatScreenActivity.putExtra(AppConstants.Keys.USER_ID, userId);
            chatScreenActivity.putExtra(AppConstants.Keys.CHAT_ID, chatId);
            chatScreenActivity.putExtra(AppConstants.Keys.CHAT_TITLE, chatName);
            chatScreenActivity.putExtra(AppConstants.Keys.PROFILE_IMAGE, image);
            chatScreenActivity.putExtra(AppConstants.Keys.WALL_ID, wallId);
            chatScreenActivity.putExtra(AppConstants.Keys.FROM_WALL, true);
            chatScreenActivity.putExtra(AppConstants.Keys.TAG_NAME, tagName);
            chatScreenActivity.putExtra(AppConstants.Keys.MY_ID, AppConstants.UserInfo.INSTANCE.getId());

            startActivity(chatScreenActivity);
        }
    }

    @Override
    public void onRefresh() {

        if (mUserWalls) {
            refresh("0");
        } else {
            refresh("3");
        }
    }

    /**
     * refreshes the current feed except the first int limit posts so as to show user refresh empty
     * effect and wait for the new posts to fetch again
     *
     * @param limit number of posts u want to retail while refreshing the feed
     */
    private void refreshMyWalls(String limit) {
        mSwipeRefreshLayout.setEnabled(false);
        mDBNotifyCounter = 0;
        mFetchedOnOpen = false;

        String selection = DatabaseColumns.ID + " NOT IN ( SELECT " + DatabaseColumns.ID
                + " from " + TableWallPosts.NAME + " order by " + DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT + " desc limit " + limit + ")"
                + SQLConstants.AND + DatabaseColumns.USER_ID + SQLConstants.EQUALS_ARG;

        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_MY_WALL_POSTS_SEARCH_RESULTS, getTaskTag(),
                null, TableWallPosts.NAME, selection, new String[]{mUserId}, true, this);


    }


    /**
     * refreshes the current feed except the first int limit posts so as to show user refresh empty
     * effect and wait for the new posts to fetch again
     *
     * @param limit number of posts u want to retail while refreshing the feed
     */
    private void refresh(String limit) {
        mSwipeRefreshLayout.setEnabled(false);
        mDBNotifyCounter = 0;
        mFetchedOnOpen = false;

        if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
            String selection = DatabaseColumns.ID + " NOT IN ( SELECT " + DatabaseColumns.ID
                    + " from " + TableWallPosts.NAME + " order by " + DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT + " desc limit " + limit + ")"
                    + SQLConstants.AND + DatabaseColumns.USER_ID + SQLConstants.EQUALS_ARG;

            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_WALL_POSTS_SEARCH_RESULTS, getTaskTag(),
                    null, TableWallPosts.NAME, selection, new String[]{mUserId}, true, this);

        } else {
            String selection = DatabaseColumns.ID + " NOT IN ( SELECT " + DatabaseColumns.ID
                    + " from " + TableWallPosts.NAME + " order by " + DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT + " desc limit " + limit + ")";

            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_WALL_POSTS_SEARCH_RESULTS, getTaskTag(),
                    null, TableWallPosts.NAME, selection, null, true, this);

            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_WALL_COMMENTS, getTaskTag(),
                    null, TableWallComments.NAME, null, null, true, this);
        }

    }


    private void hideRefreshing() {
        mSwipeRefreshLayout.setEnabled(true);
        mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onWallOptionsClicked(View view, String wallId, String userId, String tagId) {

        mSelectedWallId = wallId;
        mSelectedTagId = tagId;
        if (userId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
            openOwnerWallOptionsPopup(view);
        } else {
            openOtherWallOptionsPopup(view);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        boolean handled = false;
        switch (menuItem.getItemId()) {


            case R.id.action_delete: {

                if (!TextUtils.isEmpty(mSelectedWallId)) {
                    deletePost(mSelectedWallId);
                }
                handled = true;
                break;
            }

            case R.id.action_share: {

                String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
                DBInterface.queryAsync(AppConstants.QueryTokens.QUERY_WALL_DETAILS, getTaskTag(),
                        null, true, TableWallPosts.NAME, null, selection, new String[]{mSelectedWallId}, null, null,
                        null, null, this);
                handled = true;
                break;
            }

            case R.id.action_report_abuse: {
                if (!TextUtils.isEmpty(mSelectedWallId)) {
                    reportAbuse(mSelectedWallId);
                }
                handled = true;
                break;
            }
        }
        return handled;
    }

    /**
     * Open a post for editing
     *
     * @param selectedWallId The id of the post to edit
     */
    private void editPost(final String selectedWallId, final String selectedTagId) {

        final Intent editWallPostIntent = new Intent(getActivity(),
                EditWallPostActivity.class);
        editWallPostIntent.putExtra(AppConstants.Keys.EDIT_POST, true);
        editWallPostIntent.putExtra(AppConstants.Keys.WALL_ID, selectedWallId);
        editWallPostIntent.putExtra(AppConstants.Keys.TAG_ID, selectedTagId);


        startActivity(editWallPostIntent);
    }

    /**
     * Delete a post, along with a confirmation
     *
     * @param selectedWallId The id of the post to delete
     */
    private void deletePost(final String selectedWallId) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        // set title
        alertDialogBuilder.setTitle("Confirm");

        // set dialog message
        alertDialogBuilder
                .setMessage(getResources().getString(R.string.delete_wall_alert_message))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            final DialogInterface dialog,
                            final int id) {

                        callDeleteApi(selectedWallId);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            final DialogInterface dialog,
                            final int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    /**
     * Follow a post
     *
     * @param selectedWallId The id of the post to follow
     */
    private void followPost(final String selectedWallId) {

        Toast.makeText(getActivity(), "Follow Post", Toast.LENGTH_SHORT).show();
    }

    /**
     * Report abuse
     *
     * @param selectedWallId The id of the post to report
     */
    private void reportAbuse(final String selectedWallId) {

        ReportAbuseRequestModel reportAbuseRequestModel = new ReportAbuseRequestModel();
        reportAbuseRequestModel.setType(AppConstants.TYPE);
        reportAbuseRequestModel.setId(selectedWallId);

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.REPORT_ABUSE);
        retroCallbackList.add(retroCallback);

        mYeloApi.reportAbuse(reportAbuseRequestModel, retroCallback);
    }

    @Override
    public void onDismiss(PopupMenu popupMenu) {
        mSelectedWallId = null;
    }

    @Override
    public void onChatClicked(View view, String userId, String userName, String userImage, String wallId, String tagName) {
        MixpanelAnalytics.getInstance().onChatClicked();

        loadChat(wallId, userId, userName, userImage, tagName);
    }

    @Override
    public void onCommentClicked(View view, String wallId, String tagCount) {
        MixpanelAnalytics.getInstance().onWallOpened();

        /*  Changed by Sharath Pandeshwar on 04/03/2014 */

        /**
         * TODO: To undo the changes ASAP
         */
        final Intent wallPostIntent = new Intent(getActivity(), WallPostActivity.class);
        //final Intent wallPostIntent = new Intent(getActivity(), EditWallPostActivity.class);
        wallPostIntent.putExtra(AppConstants.Keys.COMMENT, true);

        /* End of modification done by Sharath Pandeshwar on 04/03/2014 */

        wallPostIntent.putExtra(AppConstants.Keys.ID, wallId);
        wallPostIntent.putExtra(AppConstants.Keys.FROM_WALL, true);
        wallPostIntent.putExtra(AppConstants.Keys.TAG_USER_COUNT, tagCount);
        startActivity(wallPostIntent);
    }


    @Override
    public void onTagClicked(View view, String wallId, String userId, String tagCount) {

        MixpanelAnalytics.getInstance().onReferClicked();

        mWallIdForTag = wallId;
        showTagUserFragment(mWallIdForTag, userId, tagCount);
    }

    @Override
    public void onCloseClicked(View view, final String wallId, boolean usersPresent) {
        if (isAttached()) {

            if (usersPresent) {
                final Intent closeWallActivity = new Intent(getActivity(),
                        CloseWallActivity.class);
                closeWallActivity.putExtra(AppConstants.Keys.WALL_ID, wallId);
                startActivity(closeWallActivity);
            } else {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());

                // set title
                alertDialogBuilder.setTitle(getString(R.string.confirm));

                // set dialog message
                alertDialogBuilder
                        .setMessage(getResources().getString(R.string.close_wall_alert_message))
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    final DialogInterface dialog,
                                    final int id) {

                                closeWall(wallId);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    final DialogInterface dialog,
                                    final int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                final AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        }
    }

    /**
     * api call for closing the wall
     *
     * @param wallId id of the wall u want to close
     */
    private void closeWall(String wallId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.CLOSE_WALL);
        Bundle args = new Bundle();
        args.putString(AppConstants.Keys.WALL_ID, wallId);
        retroCallback.setExtras(args);
        retroCallbackList.add(retroCallback);
        CloseWallRequestModel closeWallRequestModel = new CloseWallRequestModel();
        closeWallRequestModel.setIs_solved("1");

        mYeloApi.closeWall(wallId, closeWallRequestModel, retroCallback);


    }


    @Override
    public void onPostClicked(View view, String wallId, String tagCount) {


        MixpanelAnalytics.getInstance().onWallOpened();

        /*  Changed by Sharath Pandeshwar on 04/03/2014 */

        /**
         * TODO: To undo the changes ASAP
         */
        final Intent wallPostIntent = new Intent(getActivity(), WallPostActivity.class);
        //final Intent wallPostIntent = new Intent(getActivity(), EditWallPostActivity.class);
        wallPostIntent.putExtra(AppConstants.Keys.EDIT_POST, true);

        /* End of modification done by Sharath Pandeshwar on 04/03/2014 */

        wallPostIntent.putExtra(AppConstants.Keys.ID, wallId);
        wallPostIntent.putExtra(AppConstants.Keys.FROM_WALL, true);
        wallPostIntent.putExtra(AppConstants.Keys.TAG_USER_COUNT, tagCount);
        startActivity(wallPostIntent);
    }

    @Override
    public void onWallProfileImageClicked(View view, String userId, String userName) {
        loadProfile(userId, userName);
    }

    @Override
    public void onWallViewCreated(WallPostAdapter.WallPostViewHolder viewHolder) {
        if (mCardListView.getChildAt(0) != null) {

            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (!SharedPreferenceHelper.getBoolean(R.string.pref_refer_tutorial_played)) {
                        SharedPreferenceHelper.set(R.string.pref_refer_tutorial_played, true);
                        showReferShowcase();
                    }

                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    /**
     * This loads the profile of the user
     *
     * @param userId user id of the user u want to open profile of
     * @param name   name of the user
     */
    private void loadProfile(String userId, String name) {
        final Intent userProfileIntent = new Intent(getActivity(),
                UserProfileActivity.class);

        userProfileIntent.putExtra(AppConstants.Keys.USER_ID, userId);
        userProfileIntent.putExtra(AppConstants.Keys.USER_NAME, name);
        userProfileIntent.putExtra(AppConstants.Keys.SERVICE_SCREEN_TYPE, AppConstants.ServiceScreenType.PROFILE);
        startActivity(userProfileIntent);
    }


    @Override
    public void success(Object model, int requestId) {
        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_ALL_WALLS: {


                GetWallResponseModel wallResponseModel = ((GetWallResponseModel) model);

                mGetWallResponseModel = wallResponseModel;
                mWallItemSize = mGetWallResponseModel.search.size();
                if (mWallItemSize != 0) {
                    mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                    parseAndAddWallDetailsLocally(mGetWallResponseModel);
                    mIsLoading = false;

                } else {
                    mIsLoading = false;
                    allFetched = true;
                    hideRefreshing();
                    if (mCardListView.getAdapter().getItemCount() == 0) {
                        mEmptyView.setVisibility(View.VISIBLE);
                    }

                }

                mFetchingWalls = false;

                //Doing it here because we don't want to block the wall api for uploading the contacts
                if (SharedPreferenceHelper.getBoolean(R.string.pref_contact_sync_flag, true) && mDialogFlag) {
                    confirmContactsUploadDialog();
                    mDialogFlag = false;
                }


                break;
            }


            case HttpConstants.ApiResponseCodes.GET_USER_WALLS: {


                GetWallResponseModel wallResponseModel = ((GetWallResponseModel) model);

                mGetWallResponseModel = wallResponseModel;
                mWallItemSize = mGetWallResponseModel.walls.size();
                if (mWallItemSize != 0) {
                    parseAndAddMyWallDetailsLocally(mGetWallResponseModel);
                    mIsLoading = false;

                } else {
                    mIsLoading = false;
                    allFetched = true;
                    hideRefreshing();

                }


                break;
            }

            case HttpConstants.ApiResponseCodes.REPORT_ABUSE: {
                Toast.makeText(getActivity(), getResources().getString(R.string.report), Toast.LENGTH_SHORT).show();
                break;
            }
            default:
                break;
        }

    }

    @Override
    public void failure(int requestId, int errorCode, String message) {
        mIsLoading = false;
        hideRefreshing();
        mFetchingWalls = false;
        Toast.makeText(getActivity(), "Something went wrong...", Toast.LENGTH_SHORT).show();
        mEmptyView.setVisibility(View.VISIBLE);


    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
        mSwipeRefreshLayout.setRefreshing(false);


    }


    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

        if (!SharedPreferenceHelper.getBoolean(R.string.pref_refer_tutorial_played) && !mReferShowcase) {

            if (mCardListView.getChildAt(1) != null) {
                mCardListView.smoothScrollToPosition(2);

                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!SharedPreferenceHelper.getBoolean(R.string.pref_refer_tutorial_played)) {
                            SharedPreferenceHelper.set(R.string.pref_refer_tutorial_played, true);
                            showReferShowcase();
                        }

                    }
                };
                handler.postDelayed(runnable, 500);
            }
        } else if (!SharedPreferenceHelper.getBoolean(R.string.pref_post_tutorial_played) && mReferShowcase) {
            mBus.post(new ShowPostShowcase(true));
        }


    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    private void showReferShowcase() {
        if (isAttached()) {
            if (mCardListView.getChildAt(0) != null) {

                View view = mCardListView.getChildAt(0);


                ShowcaseView showcaseView = new ShowcaseView.Builder(getActivity(), true)
                        .setTarget(new ViewTarget((TextView) view.findViewById(R.id.tag_button)))
                        .setContentTitle("Refer")
                        .setContentText(getResources().getString(R.string.refer_tutorial_message))
                        .hideOnTouchOutside()
                        .setInnerRadius(80)
                        .setOuterRadius(100)
                        .setStyle(R.style.CustomShowcaseTheme)
                        .build();


                showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {
                        mReferShowcase = true;
                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                    }
                });
                SharedPreferenceHelper.set(R.string.pref_refer_tutorial_played, true);

                RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);


                int margin = ((Number) (getResources().getDisplayMetrics().density * 32)).intValue();
                lps.setMargins(margin, margin, margin, margin);

                showcaseView.hideButton();
            }
        }
    }


    @Subscribe
    public void scrollToTop(ScrollToTop scrollToTop) {

        if (scrollToTop.mScroll) {
            mCardListView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.change_location) {
            final Intent selectLocationActivity = new Intent(getActivity(),
                    SearchLocationActivity.class);
            startActivityForResult(selectLocationActivity, AppConstants.RequestCodes.GET_PLACE);
        }
    }


    /**
     * Parses the wall information locally
     * TODO a little messy need refactoring
     *
     * @param mGetWallResponseModel model which is received in the api call response
     */
    private void parseAndAddWallDetailsLocally(GetWallResponseModel mGetWallResponseModel) {
        for (int i = 0; i < mGetWallResponseModel.search.size(); i++) {

            if (i == mWallItemSize - 1) {
                allLoaded = true;
            } else {
                allLoaded = false;
            }
            ContentValues values = new ContentValues(25);
            values.put(DatabaseColumns.ID, mGetWallResponseModel.search.get(i).id);
            values.put(DatabaseColumns.MESSAGE, mGetWallResponseModel.search.get(i).message);
            values.put(DatabaseColumns.TAG_NAME, mGetWallResponseModel.search.get(i).tag_name);
            values.put(DatabaseColumns.TAG_ID, mGetWallResponseModel.search.get(i).tag_id);
            values.put(DatabaseColumns.TAG_USER_COUNT, mGetWallResponseModel.search.get(i).tagged_users_count);
            values.put(DatabaseColumns.CHAT_USER_COUNT, mGetWallResponseModel.search.get(i).chat_users_count);
            values.put(DatabaseColumns.COMMENT_USER_COUNT, mGetWallResponseModel.search.get(i).comments_count);
            values.put(DatabaseColumns.USER_NAME, mGetWallResponseModel.search.get(i).wall_owner.name);
            values.put(DatabaseColumns.USER_ID, mGetWallResponseModel.search.get(i).wall_owner.user_id);

            if (mGetWallResponseModel.search.get(i).wall_owner.user_id.equals(AppConstants.UserInfo.INSTANCE.getId())) {
                mWallItemSize--;
            }
            values.put(DatabaseColumns.DATE_TIME, mGetWallResponseModel.search.get(i).created_at);
            values.put(DatabaseColumns.CITY, mGetWallResponseModel.search.get(i).city);
            values.put(DatabaseColumns.COLOR, mGetWallResponseModel.search.get(i).group_color);
            values.put(DatabaseColumns.COUNTRY, mGetWallResponseModel.search.get(i).country);
            values.put(DatabaseColumns.GROUP_ID, mGetWallResponseModel.search.get(i).group_id);
            values.put(DatabaseColumns.GROUP_NAME, mGetWallResponseModel.search.get(i).group_name);


            if (TextUtils.isEmpty(mGetWallResponseModel.search.get(i).address)) {
                values.put(DatabaseColumns.ADDRESS, Character.toUpperCase(mGetWallResponseModel.search.get(i).city.charAt(0)) + mGetWallResponseModel.search.get(i).city.substring(1));

            } else {
                values.put(DatabaseColumns.ADDRESS, mGetWallResponseModel.search.get(i).address);
            }

            try {
                values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(mGetWallResponseModel.search.get(i).created_at));
                values.put(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, mMessageDateFormatter.getEpoch(mGetWallResponseModel.search.get(i).updated_at));
                values.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(mGetWallResponseModel.search.get(i).created_at));

            } catch (ParseException e) {
                e.printStackTrace();
                //should not happen
            }
            if (mGetWallResponseModel.search.get(i).wall_image != null) {
                values.put(DatabaseColumns.WALL_IMAGES, mGetWallResponseModel.search.get(i).wall_image.image_url);
            }
            values.put(DatabaseColumns.USER_IMAGE, mGetWallResponseModel.search.get(i).wall_owner.image_url);


            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


            if (mFetchedOnOpen) {
                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, getTaskTag(), values
                        , TableWallPosts.NAME, values, selection, new String[]{mGetWallResponseModel.search.get(i).id}, true, this);

            } else {
                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, getTaskTag(), values
                        , TableWallPosts.NAME, values, selection, new String[]{mGetWallResponseModel.search.get(i).id}, false, this);

            }

            if (mGetWallResponseModel.search.get(i).wall_items.size() != 0) {
                for (int j = 0; j < mGetWallResponseModel.search.get(i).wall_items.size(); j++) {


                    GetWallItemResponseModel.WallItem wallItem = mGetWallResponseModel.search.get(i).wall_items.get(j);
                    ContentValues valuesComments = new ContentValues();
                    valuesComments.put(DatabaseColumns.WALL_ID, mGetWallResponseModel.search.get(i).id);
                    valuesComments.put(DatabaseColumns.ID, wallItem.id);
                    valuesComments.put(DatabaseColumns.COMMENT, wallItem.comment);
                    valuesComments.put(DatabaseColumns.WALL_USER_ID, mGetWallResponseModel.search.get(i).wall_owner.user_id);
                    valuesComments.put(DatabaseColumns.USER_ID, wallItem.user_id);
                    valuesComments.put(DatabaseColumns.TYPE, AppConstants.CommentType.REFER);
                    valuesComments.put(DatabaseColumns.USER_NAME, wallItem.name);
                    valuesComments.put(DatabaseColumns.IMAGE_URL, wallItem.image_url);
                    valuesComments.put(DatabaseColumns.DATE_TIME, wallItem.created_at);


                    try {
                        valuesComments.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(wallItem.created_at));
                        valuesComments.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(wallItem.created_at));

                    } catch (ParseException e) {
                        e.printStackTrace();
                        //should not happen
                    }

                    if (wallItem.tagged_users.size() > 0) {
                        valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.tagged_users.get(0).id);
                        valuesComments.put(DatabaseColumns.TAGGED_NAMES, wallItem.tagged_users.get(0).name);
                        valuesComments.put(DatabaseColumns.IS_PRESENT, wallItem.tagged_users.get(0).is_present + "");


                        if (wallItem.tagged_users.get(0).details != null) {
                            valuesComments.put(DatabaseColumns.TAGGED_USER_NUMBERS, wallItem.tagged_users.get(0).details.mobile_number);
                            valuesComments.put(DatabaseColumns.TAGGED_USER_EMAILS, wallItem.tagged_users.get(0).details.email);
                        }

                        valuesComments.put(DatabaseColumns.TAGGED_IMAGE_URLS, wallItem.tagged_users.get(0).image_url);
                        valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.tagged_users.get(0).user_id);
                        valuesComments.put(DatabaseColumns.TAGGED_IDS, wallItem.tagged_users.get(0).id);
                    }

                    String selectionWallId = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    DBInterface
                            .updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, getTaskTag(),
                                    valuesComments, TableWallComments.NAME, valuesComments, selectionWallId,
                                    new String[]{wallItem.id}, false, this);
                }
            }

            if (mGetWallResponseModel.search.get(i).wall_chats.size() != 0) {
                for (int j = 0; j < mGetWallResponseModel.search.get(i).wall_chats.size(); j++) {


                    GetCreateWallResponseModel.WallChats wallItem = mGetWallResponseModel.search.get(i).wall_chats.get(j);
                    ContentValues valuesComments = new ContentValues();
                    valuesComments.put(DatabaseColumns.WALL_ID, mGetWallResponseModel.search.get(i).id);
                    valuesComments.put(DatabaseColumns.ID, mGetWallResponseModel.search.get(i).id + wallItem.user_id);
                    valuesComments.put(DatabaseColumns.WALL_USER_ID, mGetWallResponseModel.search.get(i).wall_owner.user_id);
                    valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.user_id);
                    valuesComments.put(DatabaseColumns.TAGGED_NAMES, wallItem.name);
                    valuesComments.put(DatabaseColumns.TYPE, AppConstants.CommentType.CHAT);
                    valuesComments.put(DatabaseColumns.USER_ID, mGetWallResponseModel.search.get(i).wall_owner.user_id);
                    valuesComments.put(DatabaseColumns.USER_NAME, mGetWallResponseModel.search.get(i).wall_owner.name);
                    valuesComments.put(DatabaseColumns.TAGGED_IMAGE_URLS, wallItem.image_url);
                    valuesComments.put(DatabaseColumns.TAGGED_IDS, wallItem.user_id);
                    valuesComments.put(DatabaseColumns.IS_PRESENT, "true");
                    valuesComments.put(DatabaseColumns.DATE_TIME, wallItem.last_chat);

                    try {
                        valuesComments.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(wallItem.last_chat));
                        valuesComments.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(wallItem.last_chat));

                    } catch (ParseException e) {
                        e.printStackTrace();
                        //should not happen
                    }


                    String selectionWallId = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


                    DBInterface
                            .updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, getTaskTag(),
                                    valuesComments, TableWallComments.NAME, valuesComments, selectionWallId,
                                    new String[]{mGetWallResponseModel.search.get(i).id + wallItem.user_id}, false, this);

                }
            }
        }
    }


    /**
     * Parses the wall information locally
     * TODO a little messy need refactoring
     *
     * @param mGetWallResponseModel model which is received in the api call response
     */
    private void parseAndAddMyWallDetailsLocally(GetWallResponseModel mGetWallResponseModel) {
        for (int i = 0; i < mGetWallResponseModel.walls.size(); i++) {

            if (i == mWallItemSize - 1) {
                allLoaded = true;
            } else {
                allLoaded = false;
            }
            ContentValues values = new ContentValues(6);
            values.put(DatabaseColumns.ID, mGetWallResponseModel.walls.get(i).id);
            values.put(DatabaseColumns.MESSAGE, mGetWallResponseModel.walls.get(i).message);
            values.put(DatabaseColumns.TAG_NAME, mGetWallResponseModel.walls.get(i).tag_name);
            values.put(DatabaseColumns.TAG_ID, mGetWallResponseModel.walls.get(i).tag_id);
            values.put(DatabaseColumns.TAG_USER_COUNT, mGetWallResponseModel.walls.get(i).tagged_users_count);
            values.put(DatabaseColumns.CHAT_USER_COUNT, mGetWallResponseModel.walls.get(i).chat_users_count);
            values.put(DatabaseColumns.COMMENT_USER_COUNT, mGetWallResponseModel.walls.get(i).comments_count);
            values.put(DatabaseColumns.USER_NAME, mGetWallResponseModel.walls.get(i).wall_owner.name);
            values.put(DatabaseColumns.USER_ID, mGetWallResponseModel.walls.get(i).wall_owner.user_id);
            values.put(DatabaseColumns.DATE_TIME, mGetWallResponseModel.walls.get(i).created_at);
            values.put(DatabaseColumns.CITY, mGetWallResponseModel.walls.get(i).city);
            values.put(DatabaseColumns.COLOR, mGetWallResponseModel.walls.get(i).group_color);
            values.put(DatabaseColumns.COUNTRY, mGetWallResponseModel.walls.get(i).country);
            values.put(DatabaseColumns.GROUP_ID, mGetWallResponseModel.walls.get(i).group_id);
            values.put(DatabaseColumns.GROUP_NAME, mGetWallResponseModel.walls.get(i).group_name);
            if (TextUtils.isEmpty(mGetWallResponseModel.walls.get(i).address)) {
                values.put(DatabaseColumns.ADDRESS, Character.toUpperCase(mGetWallResponseModel.walls.get(i).city.charAt(0)) + mGetWallResponseModel.walls.get(i).city.substring(1));

            } else {
                values.put(DatabaseColumns.ADDRESS, mGetWallResponseModel.walls.get(i).address);
            }

            try {
                values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(mGetWallResponseModel.walls.get(i).created_at));
                values.put(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, mMessageDateFormatter.getEpoch(mGetWallResponseModel.walls.get(i).updated_at));
                values.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(mGetWallResponseModel.walls.get(i).created_at));

            } catch (ParseException e) {
                e.printStackTrace();
                //should not happen
            }
            if (mGetWallResponseModel.walls.get(i).wall_image != null) {
                values.put(DatabaseColumns.WALL_IMAGES, mGetWallResponseModel.walls.get(i).wall_image.image_url);
            }
            values.put(DatabaseColumns.USER_IMAGE, mGetWallResponseModel.walls.get(i).wall_owner.image_url);


            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


            if (mFetchedOnOpen) {
                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, getTaskTag(), values
                        , TableWallPosts.NAME, values, selection, new String[]{mGetWallResponseModel.walls.get(i).id}, true, this);

            } else {
                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, getTaskTag(), values
                        , TableWallPosts.NAME, values, selection, new String[]{mGetWallResponseModel.walls.get(i).id}, false, this);

            }

            if (mGetWallResponseModel.walls.get(i).wall_items.size() != 0) {
                for (int j = 0; j < mGetWallResponseModel.walls.get(i).wall_items.size(); j++) {


                    GetWallItemResponseModel.WallItem wallItem = mGetWallResponseModel.walls.get(i).wall_items.get(j);
                    ContentValues valuesComments = new ContentValues();
                    valuesComments.put(DatabaseColumns.WALL_ID, mGetWallResponseModel.walls.get(i).id);
                    valuesComments.put(DatabaseColumns.ID, wallItem.id);
                    valuesComments.put(DatabaseColumns.COMMENT, wallItem.comment);
                    valuesComments.put(DatabaseColumns.WALL_USER_ID, mGetWallResponseModel.walls.get(i).wall_owner.user_id);
                    valuesComments.put(DatabaseColumns.USER_ID, wallItem.user_id);
                    valuesComments.put(DatabaseColumns.TYPE, AppConstants.CommentType.REFER);
                    valuesComments.put(DatabaseColumns.USER_NAME, wallItem.name);
                    valuesComments.put(DatabaseColumns.IMAGE_URL, wallItem.image_url);
                    valuesComments.put(DatabaseColumns.DATE_TIME, wallItem.created_at);


                    try {
                        valuesComments.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(wallItem.created_at));
                        valuesComments.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(wallItem.created_at));

                    } catch (ParseException e) {
                        e.printStackTrace();
                        //should not happen
                    }

                    if (wallItem.tagged_users.size() > 0) {
                        valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.tagged_users.get(0).id);
                        valuesComments.put(DatabaseColumns.TAGGED_NAMES, wallItem.tagged_users.get(0).name);
                        valuesComments.put(DatabaseColumns.IS_PRESENT, wallItem.tagged_users.get(0).is_present + "");


                        if (wallItem.tagged_users.get(0).details != null) {
                            valuesComments.put(DatabaseColumns.TAGGED_USER_NUMBERS, wallItem.tagged_users.get(0).details.mobile_number);
                            valuesComments.put(DatabaseColumns.TAGGED_USER_EMAILS, wallItem.tagged_users.get(0).details.email);
                        }

                        valuesComments.put(DatabaseColumns.TAGGED_IMAGE_URLS, wallItem.tagged_users.get(0).image_url);
                        valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.tagged_users.get(0).user_id);
                        valuesComments.put(DatabaseColumns.TAGGED_IDS, wallItem.tagged_users.get(0).id);
                    }

                    String selectionWallId = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                    DBInterface
                            .updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, getTaskTag(),
                                    valuesComments, TableWallComments.NAME, valuesComments, selectionWallId,
                                    new String[]{wallItem.id}, false, this);
                }
            }

            if (mGetWallResponseModel.walls.get(i).wall_chats.size() != 0) {
                for (int j = 0; j < mGetWallResponseModel.walls.get(i).wall_chats.size(); j++) {


                    GetCreateWallResponseModel.WallChats wallItem = mGetWallResponseModel.walls.get(i).wall_chats.get(j);
                    ContentValues valuesComments = new ContentValues();
                    valuesComments.put(DatabaseColumns.WALL_ID, mGetWallResponseModel.walls.get(i).id);
                    valuesComments.put(DatabaseColumns.ID, mGetWallResponseModel.walls.get(i).id + wallItem.user_id);
                    valuesComments.put(DatabaseColumns.WALL_USER_ID, mGetWallResponseModel.walls.get(i).wall_owner.user_id);
                    valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.user_id);
                    valuesComments.put(DatabaseColumns.TAGGED_NAMES, wallItem.name);
                    valuesComments.put(DatabaseColumns.TYPE, AppConstants.CommentType.CHAT);
                    valuesComments.put(DatabaseColumns.USER_ID, mGetWallResponseModel.walls.get(i).wall_owner.user_id);
                    valuesComments.put(DatabaseColumns.USER_NAME, mGetWallResponseModel.walls.get(i).wall_owner.name);
                    valuesComments.put(DatabaseColumns.TAGGED_IMAGE_URLS, wallItem.image_url);
                    valuesComments.put(DatabaseColumns.TAGGED_IDS, wallItem.user_id);
                    valuesComments.put(DatabaseColumns.IS_PRESENT, "true");
                    valuesComments.put(DatabaseColumns.DATE_TIME, wallItem.last_chat);

                    try {
                        valuesComments.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(wallItem.last_chat));
                        valuesComments.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(wallItem.last_chat));

                    } catch (ParseException e) {
                        e.printStackTrace();
                        //should not happen
                    }


                    String selectionWallId = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


                    DBInterface
                            .updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, getTaskTag(),
                                    valuesComments, TableWallComments.NAME, valuesComments, selectionWallId,
                                    new String[]{mGetWallResponseModel.walls.get(i).id + wallItem.user_id}, false, this);

                }
            }
        }
    }

    private void confirmContactsUploadDialog() {


        boolean wrapInScrollView = true;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View mCustomConfirmDialog = inflater
                .inflate(R.layout.layout_dialog_confirm_contacts, null);

        new MaterialDialog.Builder(getActivity())
                .title("Confirm")
                .positiveText("Yes")
                .negativeText("No")
                .customView(mCustomConfirmDialog, wrapInScrollView)
                .positiveColor(getResources().getColor(R.color.blue_link))
                .negativeColor(getResources().getColor(R.color.blue_link))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        SharedPreferenceHelper.set(R.string.pref_contact_sync_flag, true);
                        YeloApplication.syncContactsIfNecessary(true);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        SharedPreferenceHelper.set(R.string.pref_contact_sync_flag, false);
                        dialog.dismiss();
                    }
                })
                .build()
                .show();
    }
}
