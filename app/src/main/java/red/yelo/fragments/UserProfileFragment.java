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
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.melnykov.fab.FloatingActionButton;
import com.vinaysshenoy.okulus.OkulusImageView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.activities.ChatScreenActivity;
import red.yelo.activities.CreateCardActivity;
import red.yelo.activities.CreateServiceCardActivity;
import red.yelo.activities.EditProfileActivity;
import red.yelo.activities.EditWallPostActivity;
import red.yelo.activities.SearchLocationActivity;
import red.yelo.activities.UpdateSkillsActivity;
import red.yelo.activities.WallsViewActivity;
import red.yelo.adapters.ProfileCardListAdapter;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableCollections;
import red.yelo.data.TableMyWallComments;
import red.yelo.data.TableMyWallPosts;
import red.yelo.data.TableProfileCards;
import red.yelo.data.TableUsers;
import red.yelo.data.TableWallComments;
import red.yelo.data.TableWallPosts;
import red.yelo.data.ViewGroupColorsWithCards;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.GetUserModel;
import red.yelo.retromodels.response.CreateListingResponseModel;
import red.yelo.retromodels.response.GetWallItemResponseModel;
import red.yelo.retromodels.response.GetWallResponseModel;
import red.yelo.retromodels.response.GoogleGeocodeResponse;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.LoadMoreHelper;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.widgets.MultiTagLayout;


/**
 * Created by anshul1235 on 15/07/14.
 */
public class UserProfileFragment extends AbstractYeloFragment implements LoaderManager.LoaderCallbacks<Cursor>, DBInterface.AsyncDbQueryCallback, AdapterView.OnItemClickListener, LoadMoreHelper.LoadMoreCallbacks, SwipeRefreshLayout.OnRefreshListener, RetroCallback.RetroResponseListener, View.OnClickListener, ProfileCardListAdapter.ProfileCardListener {

    public static final String TAG = "UserProfileFragment";
    private String mUserId;

    private OkulusImageView mUserImageView;

    private TextView mUserSubText, mUserNameText;

    /**
     * Date formatter for formatting timestamps for messages
     */
    private DateFormatter mMessageDateFormatter;


    /**
     * ListView which will hold all the wall posts
     */
    private ListView mCardListView;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private String mSelectedWallId;

    private String mListId;

    private ProgressBar mProgressWheel;


    private ProfileCardListAdapter mProfileCardListAdapter;

    private View mEmptyView;


    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private GetWallResponseModel mGetWallResponseModel;

    private Toolbar mToolbar;

    private boolean mFromHomeScreen;

    private String mSelectedTagId;

    private LinearLayout mProfileLayout;


    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        View headerView = inflater.inflate(R.layout.layout_header_profile, null, false);
        View emptyView = inflater.inflate(R.layout.layout_item_add_graphic, null, false);
        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT, AppConstants.WALL_DATE_FORMAT);


        Bundle extras = getArguments();

        if (extras != null) {
            mUserId = extras.getString(AppConstants.Keys.USER_ID);
            if (extras.containsKey(AppConstants.Keys.FROM_HOMESCREEN)) {
                mFromHomeScreen = extras.getBoolean(AppConstants.Keys.FROM_HOMESCREEN);
            }
        }

        mEmptyView = contentView.findViewById(R.id.empty_view);
        mEmptyView.setVisibility(View.GONE);

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);

        if (!mFromHomeScreen) {
            //setToolbar(mToolbar);
            /* Changed by Sharath */
            mToolbar.setVisibility(View.GONE);
        } else {
            mToolbar.setVisibility(View.GONE);
        }
        mProgressWheel = (ProgressBar) contentView.findViewById(R.id.progress_wheel);

        mUserImageView = (OkulusImageView) headerView.findViewById(R.id.profile_image);
//        mReferralCountMadeText.setOnClickListener(this);
//        mRecommendCountText.setOnClickListener(this);
        mUserSubText = (TextView) headerView.findViewById(R.id.user_subText);
        mUserNameText = (TextView) headerView.findViewById(R.id.user_name);

        mProfileLayout = (LinearLayout) headerView.findViewById(R.id.profile_layout);

        if (mFromHomeScreen) {
            mProfileLayout.setMinimumHeight(200);
        }


        mCardListView = (ListView) contentView.findViewById(R.id.yelo_board_cardlist);

        /* Commented by Sharath on 20/03/2014 */
        //mCardListView.addHeaderView(headerView);
        //mCardListView.setEmptyView(emptyView);

        mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.white);
        mSwipeRefreshLayout.setProgressBackgroundColor(R.color.primaryColor);
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);

        if (mFromHomeScreen) {
            mProfileCardListAdapter = new ProfileCardListAdapter(getActivity(), true, this);
        } else {
            mProfileCardListAdapter = new ProfileCardListAdapter(getActivity(), false, this);

        }
        mCardListView.setAdapter(mProfileCardListAdapter);
        mCardListView.setOnItemClickListener(this);


        if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
            loadCollections();
            loadUserDetails();

        } else {
            mProgressWheel.setVisibility(View.VISIBLE);
            loadOtherUserDetails();
            loadOtherCollections();

        }


        return contentView;

    }


    private void fetchUserDetails(String userId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_USER_DETAILS);
        retroCallbackList.add(retroCallback);

        mYeloApi.getUserDetailAsync(userId, retroCallback);
        mSwipeRefreshLayout.setRefreshing(true);


    }


    private void loadUserDetails() {
        fetchUserDetails(mUserId);
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_USER, null, this);

    }


    private void loadOtherUserDetails() {
        fetchUserDetails(mUserId);
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_OTHER_USER, null, this);

    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    public static UserProfileFragment newInstance(Bundle args) {
        UserProfileFragment f = new UserProfileFragment();
        f.setArguments(args);
        return f;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_USER) {

            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

            return new SQLiteLoader(getActivity(), false, TableUsers.NAME, null, selection, new String[]{mUserId}, null, null, null, null);
        } else if (loaderId == AppConstants.Loaders.LOAD_OTHER_USER) {

            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

            return new SQLiteLoader(getActivity(), false, TableUsers.NAME, null, selection, new String[]{mUserId}, null, null, null, null);
        } else if (loaderId == AppConstants.Loaders.LOAD_PROFILE_CARDS) {


            String selectionUser = DatabaseColumns.USER_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), true, TableProfileCards.NAME, null, selectionUser, new String[]{mUserId}, null, null, DatabaseColumns.GROUP_NAME + SQLConstants.ASCENDING, null);
        } else if (loaderId == AppConstants.Loaders.LOAD_OTHER_PROFILE_CARDS) {


            String selectionUser = DatabaseColumns.USER_ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), true, TableProfileCards.NAME, null, selectionUser, new String[]{mUserId}, null, null, DatabaseColumns.GROUP_NAME + SQLConstants.ASCENDING, null);
        } else {
            return null;
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_USER) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {

                if (cursor.moveToFirst()) {
                    updateView(cursor);
                }
            }

        } else if (loader.getId() == AppConstants.Loaders.LOAD_OTHER_USER) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {

                if (cursor.moveToFirst()) {
                    updateView(cursor);
                }
            }

        } else if (loader.getId() == AppConstants.Loaders.LOAD_PROFILE_CARDS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {
                mProfileCardListAdapter.swapCursor(cursor);

            }

        } else if (loader.getId() == AppConstants.Loaders.LOAD_OTHER_PROFILE_CARDS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {
                mProfileCardListAdapter.swapCursor(cursor);


            }

        }
    }


    private void updateView(Cursor cursor) {

        final String tagCount = cursor.getString(cursor.getColumnIndex(DatabaseColumns.TAG_COUNT));
        final String connectCount = cursor.getString(cursor.getColumnIndex(DatabaseColumns.CONNECT_COUNT));

        String recommendsLabel;
        if (TextUtils.isEmpty(tagCount)) {
            recommendsLabel = "0";
        } else {
            recommendsLabel = tagCount;
        }

//        final String reviewCount = cursor.getString(cursor.getColumnIndex(DatabaseColumns.REVIEW_COUNT));
//        String reviewLabel;

        String tagLabel;

        if (TextUtils.isEmpty(tagCount)) {
            //reviewLabel = getString(R.string.testimonials, "0");
            tagLabel = "0";

        } else {
            //reviewLabel = getString(R.string.testimonials, reviewCount);
            tagLabel = connectCount;

        }
        mUserSubText.setText(recommendsLabel + " referrals  • " + tagLabel + " connections");


        mUserNameText.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_NAME)));


        ColorGenerator generator = ColorGenerator.DEFAULT;

        int color = generator.getColor((cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase());

        if (!mFromHomeScreen) {
            mProfileLayout.setBackgroundColor(getResources().getColor(R.color.primaryColor));
        } else {
            mProfileLayout.setBackgroundColor(getResources().getColor(R.color.white));

        }

        Resources r = getActivity().getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());

        TextDrawable drawable = TextDrawable.builder().buildRoundRect((cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase(), color, Math.round(px));

        if (cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_IMAGE)).contains("assets/fallback/")) {
            Utils.loadCircularImage(getActivity(), mUserImageView, "", AvatarBitmapTransformation.AvatarSize.BIG, drawable);

        } else {
            Utils.loadCircularImage(getActivity(), mUserImageView, cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_IMAGE)), AvatarBitmapTransformation.AvatarSize.BIG, drawable);
        }

        String tagNames[] = cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_TAGS)).split(",");
        String tagIds[] = cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_TAGS_IDS)).split(",");

        MultiTagLayout.Tag[] mTags = new MultiTagLayout.Tag[tagNames.length];
        for (int j = 0; j < tagNames.length; j++) {
            mTags[j] = new MultiTagLayout.Tag(tagIds[j], tagNames[j]);

        }
//        for (int i = 0; i < mTags.length; i++) {
//            ContentValues values = new ContentValues();
//
//            values.put(DatabaseColumns.ID, tagIds[i]);
//
//            values.put(DatabaseColumns.HEADING, tagNames[i]);
//            values.put(DatabaseColumns.COLOR, i+"");
//            values.put(DatabaseColumns.USER_ID,mUserId);
//            values.put(DatabaseColumns.REFERRAL_COUNT,);
//
//            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
//
//            if(!TextUtils.isEmpty(tagNames[i])) {
//                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_COLLECTIONS, getTaskTag(), values
//                        , TableProfileCards.NAME, values, selection, new String[]{i + ""}, true, this);
//            }
//
//        }

        ArrayList<MultiTagLayout.Tag> tagList = new ArrayList<MultiTagLayout.Tag>();
        tagList.addAll(Arrays.asList(mTags));
        if (tagList.size() != 0) {
        }


    }


    private void loadCollections() {
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_PROFILE_CARDS, null, this);

    }


    private void loadOtherCollections() {
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_OTHER_PROFILE_CARDS, null, this);

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_PROFILE_CARDS) {
            mProfileCardListAdapter.swapCursor(null);
        }
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

        if (taskId == AppConstants.QueryTokens.INSERT_PROFILE_CARDS) {
            DBInterface.notifyChange(ViewGroupColorsWithCards.NAME);

        }
    }


    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }


    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

        if (taskId == AppConstants.QueryTokens.UPDATE_USERS) {
            if (updateCount == 0) {
                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_USERS, getTaskTag(), null, TableUsers.NAME, null, values, true, this);
            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_PROFILE_CARDS) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_PROFILE_CARDS, getTaskTag(), null, TableProfileCards.NAME, null, values, true, this);
            } else {
                DBInterface.notifyChange(ViewGroupColorsWithCards.NAME);
            }
        }

        if (taskId == AppConstants.QueryTokens.UPDATE_MY_WALLPOST) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_MY_WALLPOST, getTaskTag(), null, TableMyWallPosts.NAME, null, values, true, this);
            }
        }
        if (taskId == AppConstants.QueryTokens.UPDATE_WALLPOST) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, getTaskTag(), null, TableWallPosts.NAME, null, values, true, this);
            }
        }
        if (taskId == AppConstants.QueryTokens.UPDATE_MY_WALLCOMMENTS) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_MY_WALLCOMMENT, getTaskTag(), null, TableMyWallComments.NAME, null, values, true, this);
            }
        }
        if (taskId == AppConstants.QueryTokens.UPDATE_COLLECTIONS) {
            if (updateCount == 0) {
                ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_COLLECTIONS, getTaskTag(), null, TableCollections.NAME, null, values, true, this);
            }

        }
        if (taskId == AppConstants.QueryTokens.UPDATE_WALLCOMMENTS) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLCOMMENT, getTaskTag(), null, TableWallComments.NAME, null, values, true, this);
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


    public void fetchMyWalls(String userId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_USER_WALLS);
        retroCallbackList.add(retroCallback);

        mYeloApi.getUserWalls(userId, retroCallback);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


    }


    @Override
    public void onLoadMore() {

    }


    @Override
    public boolean isLoading() {
        return false;
    }


    @Override
    public boolean hasLoadedAllItems() {
        return false;
    }


    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setEnabled(false);

        fetchUserDetails(mUserId);
    }


    private void hideRefreshing() {
        mSwipeRefreshLayout.setEnabled(true);
        mSwipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onResume() {
        super.onResume();

        if (SharedPreferenceHelper.getBoolean(R.string.pref_force_user_refetch)) {
            onRefresh();
        }
    }


    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_USER_DETAILS: {
                GetUserModel getUserModel = ((GetUserModel) model);
                String tagNamesValue = "";
                String tagIdsValue = "";
                userRefresh(false);
                mProgressWheel.setVisibility(View.INVISIBLE);
                if (getUserModel.user.listings.size() > 0) {
                    if (getUserModel.user.listings.size() > 0) {

                        for (int i = 0; i < getUserModel.user.listings.size(); i++) {

                            tagNamesValue = tagNamesValue + "," + getUserModel.user.listings.get(i).tag_name;
                            tagIdsValue = tagNamesValue + "," + getUserModel.user.listings.get(i).tag_id;
                        }
                    }
                }

                if (getUserModel.user.id.equals(AppConstants.UserInfo.INSTANCE.getId())) {
                    SharedPreferenceHelper.set(R.string.pref_first_name, getUserModel.user.name);
                    SharedPreferenceHelper.set(R.string.pref_profile_image, getUserModel.user.image_url);
                    SharedPreferenceHelper.set(R.string.pref_description, getUserModel.user.description);
                    SharedPreferenceHelper.set(R.string.pref_share_token, getUserModel.user.share_token);


                    AppConstants.UserInfo.INSTANCE.setDescription(getUserModel.user.description);
                    AppConstants.UserInfo.INSTANCE.setFirstName(getUserModel.user.name);
                    AppConstants.UserInfo.INSTANCE.setProfilePicture(getUserModel.user.image_url);

                    MixpanelAnalytics.getInstance().nameUser(AppConstants.UserInfo.INSTANCE.getFirstName());
                }


                ContentValues values = new ContentValues();
                values.put(DatabaseColumns.ID, getUserModel.user.id);
                values.put(DatabaseColumns.USER_IMAGE, getUserModel.user.image_url);
                values.put(DatabaseColumns.USER_NAME, getUserModel.user.name);
                values.put(DatabaseColumns.USER_DESCRIPTION, getUserModel.user.description);
                values.put(DatabaseColumns.TAG_COUNT, getUserModel.user.total_tagged);
                values.put(DatabaseColumns.REVIEW_COUNT, getUserModel.user.total_ratings);
                values.put(DatabaseColumns.AVERAGE_RATING, getUserModel.user.rating_avg);
                values.put(DatabaseColumns.REVIEW_COUNT, getUserModel.user.total_ratings);
                values.put(DatabaseColumns.USER_TAGS, tagNamesValue);
                values.put(DatabaseColumns.USER_TAGS_IDS, tagIdsValue);
                values.put(DatabaseColumns.CONNECT_COUNT, getUserModel.user.connects_count);

                String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_USERS, getTaskTag(), values, TableUsers.NAME, values, selection, new String[]{getUserModel.user.id}, true, this);

                String selectionProfileCards = DatabaseColumns.USER_ID + SQLConstants.EQUALS_ARG;
                int i = 0;

                //add values to the profile cards

                if(!mUserId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
                    if (getUserModel.user.listings.size() == 0) {
                        mEmptyView.setVisibility(View.VISIBLE);
                    } else {
                        mEmptyView.setVisibility(View.GONE);

                    }
                }

                for (CreateListingResponseModel.Listing listing : getUserModel.user.listings) {

                    ContentValues valuesProfileCard = new ContentValues();
                    valuesProfileCard.put(DatabaseColumns.ID, listing.id);
                    valuesProfileCard.put(DatabaseColumns.USER_ID, mUserId);
                    valuesProfileCard.put(DatabaseColumns.REFERRAL_COUNT, listing.referral_count);

                    List<String> keywords = new ArrayList<String>();

                    if (listing.listing_keywords != null) {
                        for (CreateListingResponseModel.Listing.ListingKeywords keywordsList : listing.listing_keywords) {
                            keywords.add(" " + keywordsList.name.substring(0, 1).toUpperCase() + keywordsList.name.substring(1));
                        }
                    }

                    valuesProfileCard.put(DatabaseColumns.SUB_HEADING, TextUtils.join(",", keywords));

                    if (listing.listing_links != null) {
                        if (listing.listing_links.size() != 0) {
                            valuesProfileCard.put(DatabaseColumns.URL, listing.listing_links.get(0).url);
                        }
                    }
                    valuesProfileCard.put(DatabaseColumns.GROUP_ID, listing.group_id);
                    valuesProfileCard.put(DatabaseColumns.GROUP_NAME, listing.group_name);
                    valuesProfileCard.put(DatabaseColumns.SUBGROUP_ID, listing.tag_id);
                    valuesProfileCard.put(DatabaseColumns.SUBGROUP_NAME, listing.tag_name);
                    valuesProfileCard.put(DatabaseColumns.COLOR, listing.group_color);

                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_PROFILE_CARDS, getTaskTag(), valuesProfileCard, TableProfileCards.NAME, valuesProfileCard, selection, new String[]{listing.id}, true, this);


                }

                hideRefreshing();
                break;
            }

            case HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE:

                String[] address;
                GoogleGeocodeResponse googleGeocodeResponse = ((GoogleGeocodeResponse) model);
                if (googleGeocodeResponse.results.size() != 0) {
                    address = googleGeocodeResponse.results.get(0).getAddress();

                    SharedPreferenceHelper.set(R.string.pref_location, TextUtils.join(",", address));
                } else {
                }
                break;

            case HttpConstants.ApiResponseCodes.GET_ALL_WALLS: {


                GetWallResponseModel wallResponseModel = ((GetWallResponseModel) model);

                mGetWallResponseModel = wallResponseModel;
                if (mGetWallResponseModel.search.size() != 0) {
                    for (int i = 0; i < mGetWallResponseModel.search.size(); i++) {


                        ContentValues values = new ContentValues(6);
                        values.put(DatabaseColumns.ID, mGetWallResponseModel.search.get(i).id);
                        values.put(DatabaseColumns.MESSAGE, mGetWallResponseModel.search.get(i).message);
                        values.put(DatabaseColumns.TAG_NAME, mGetWallResponseModel.search.get(i).tag_name);
                        values.put(DatabaseColumns.TAG_ID, mGetWallResponseModel.search.get(i).tag_id);
                        values.put(DatabaseColumns.TAG_USER_COUNT, mGetWallResponseModel.search.get(i).tagged_users_count);
                        values.put(DatabaseColumns.CHAT_USER_COUNT, mGetWallResponseModel.search.get(i).chat_users_count);
                        values.put(DatabaseColumns.USER_NAME, mGetWallResponseModel.search.get(i).wall_owner.name);
                        values.put(DatabaseColumns.USER_ID, mGetWallResponseModel.search.get(i).wall_owner.user_id);
                        values.put(DatabaseColumns.DATE_TIME, mGetWallResponseModel.search.get(i).created_at);

                        if (TextUtils.isEmpty(mGetWallResponseModel.search.get(i).address)) {
                            values.put(DatabaseColumns.ADDRESS, Character.toUpperCase(mGetWallResponseModel.search.get(i).city.charAt(0)) + mGetWallResponseModel.search.get(i).city.substring(1));

                        } else {
                            values.put(DatabaseColumns.ADDRESS, mGetWallResponseModel.search.get(i).address);
                        }

                        try {
                            values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(mGetWallResponseModel.search.get(i).created_at));
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


                        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, getTaskTag(), values, TableWallPosts.NAME, values, selection, new String[]{mGetWallResponseModel.search.get(i).id}, false, this);


                        if (mGetWallResponseModel.search.get(i).wall_items.size() != 0) {
                            for (int j = 0; j < mGetWallResponseModel.search.get(i).wall_items.size(); j++) {


                                GetWallItemResponseModel.WallItem wallItem = mGetWallResponseModel.search.get(i).wall_items.get(j);
                                ContentValues valuesComments = new ContentValues();
                                valuesComments.put(DatabaseColumns.WALL_ID, mGetWallResponseModel.search.get(i).id);
                                valuesComments.put(DatabaseColumns.ID, wallItem.id);
                                valuesComments.put(DatabaseColumns.COMMENT, wallItem.comment);
                                valuesComments.put(DatabaseColumns.WALL_USER_ID, mGetWallResponseModel.search.get(i).wall_owner.user_id);
                                valuesComments.put(DatabaseColumns.USER_ID, wallItem.user_id);
                                valuesComments.put(DatabaseColumns.USER_NAME, wallItem.name);
                                valuesComments.put(DatabaseColumns.IMAGE_URL, wallItem.image_url);

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

                                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, getTaskTag(), valuesComments, TableWallComments.NAME, valuesComments, selectionWallId, new String[]{wallItem.id}, false, this);
                            }
                        }

                    }

                }


                break;
            }


            default:
                break;
        }
    }


    @Override
    public void failure(int requestId, int errorCode, String message) {
        hideRefreshing();
        showNetworkNotAvailableMessage(message);


    }


    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
    }


    @Override
    public void onClick(View v) {
    }


    @Override
    public void onCardClicked(View view, String cardId, String userId, String groupName, String groupId, String subCategoryName, String subCategoryId, String color) {


        final Intent createCard = new Intent(getActivity(),
                CreateCardActivity.class);

        createCard.putExtra(AppConstants.Keys.ID, cardId);
        createCard.putExtra(AppConstants.Keys.USER_ID, userId);
        createCard.putExtra(AppConstants.Keys.GROUP_NAME, groupName);
        createCard.putExtra(AppConstants.Keys.GROUP_ID, groupId);
        createCard.putExtra(AppConstants.Keys.SUBCATEGORY_NAME, subCategoryName);
        createCard.putExtra(AppConstants.Keys.SUBCATEGORY_ID, subCategoryName);
        createCard.putExtra(AppConstants.Keys.GROUP_ID, groupId);
        createCard.putExtra(AppConstants.Keys.COLOR, color);


//        if (mFromHomeScreen) {
//
//            FloatingActionButton floatingActionButton = ((HomeScreenFragment)
//                    getActivity().getSupportFragmentManager().findFragmentByTag(AppConstants.FragmentTags.HOME_SCREEN))
//                    .getFabButton();
//            if (Build.VERSION.SDK_INT > 15) {
//
//                ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
//                        Pair.create((View) floatingActionButton, "fab"));
//
//                getActivity().startActivity(createCard, transitionActivityOptions.toBundle());
//            } else {
//                startActivity(createCard);
//            }
//
//        } else {

        startActivity(createCard);
        //       }

    }


    @Override
    public void onCreateCard(View view) {

        final Intent createCard = new Intent(getActivity(), CreateCardActivity.class);

        startActivity(createCard);
    }


}
