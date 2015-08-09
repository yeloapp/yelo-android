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


import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.melnykov.fab.FloatingActionButton;
import com.vinaysshenoy.okulus.OkulusImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import red.yelo.R;
import red.yelo.activities.CreateCardActivity;
import red.yelo.activities.CreateServiceCardActivity;
import red.yelo.activities.EditProfileActivity;
import red.yelo.activities.WallsViewActivity;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableUsers;
import red.yelo.http.HttpConstants;
import red.yelo.http.HttpConstants.ApiResponseCodes;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.GetUserModel;
import red.yelo.retromodels.response.CreateListingResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.widgets.slidingtabs.SlidingTabLayout;


/**
 * @author Sharath Pandeshwar
 * @since 19/03/15
 * <p/>
 * Controller responsible for showing details of a user in the form of pager
 */
public class UserProfilePagerFragment extends AbstractYeloFragment implements View.OnClickListener, ViewPager.OnPageChangeListener, SlidingTabLayout.MyTabClickListener, LoaderManager.LoaderCallbacks<Cursor>, RetroCallback.RetroResponseListener, DBInterface.AsyncDbQueryCallback {

    public static final String TAG = "UserProfilePagerFragment";

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private String mUserId;

    /**
     * If this flag is true it means user is seeing his own profile. If it is false it means he is
     * seeing somebody else's profile.
     */
    private boolean mIsMyProfileScreen = false, mFromPagerScreen;

    private OkulusImageView mUserImageView;
    private TextView mUserSubText, mUserNameText;
    private LinearLayout mProfileLayout;

    private Toolbar mToolbar;
    private AboutMePagerAdapter mPagerdapter;
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mPager;
    private FloatingActionButton mAddFABButton;

    private View mContentView;

    private Bitmap mShareImage;

    /* This index tells which pf the child pages of the pager is currently being shown */
    private int mIndexOfPager = 0;

    /**
     * Reference to arguments passes to me.
     */
    Bundle mBundle;

    //*******************************************************************
    // Life Cycle Related Functions
    //*******************************************************************


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);


        mBundle = getArguments();
        if (mBundle != null) {
            mUserId = mBundle.getString(AppConstants.Keys.USER_ID);
            if (mBundle.containsKey(AppConstants.Keys.FROM_HOMESCREEN)) {
                mIsMyProfileScreen = mBundle.getBoolean(AppConstants.Keys.FROM_HOMESCREEN);
            }

            if (mBundle.containsKey(AppConstants.Keys.PAGER)) {
                mFromPagerScreen = mBundle.getBoolean(AppConstants.Keys.PAGER);
            }
        }

        final View contentView = inflater.inflate(R.layout.fragment_user_profile_pager, container, false);
        mContentView = contentView;
        mContentView.setDrawingCacheEnabled(true);
        initializeViews(contentView, savedInstanceState);

        if (mIsMyProfileScreen) {
            loadMyUserDetails();
        } else {
            loadOtherUserDetails();
        }

        return contentView;
    }


    @Override
    public void onPause() {
        super.onPause();
        for (RetroCallback aRetroCallbackList : retroCallbackList) {
            if (aRetroCallbackList.getRequestId() != ApiResponseCodes.CREATE_WALL)
                aRetroCallbackList.cancel();
        }
    }

    //*******************************************************************
    // View Related Functions
    //*******************************************************************


    private void initializeViews(View contentView, Bundle savedInstanceState) {

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);


        mAddFABButton = (FloatingActionButton) contentView.findViewById(R.id.fabbutton);
        mAddFABButton.setOnClickListener(this);

        mUserImageView = (OkulusImageView) contentView.findViewById(R.id.profile_image);
        mUserSubText = (TextView) contentView.findViewById(R.id.user_subText);
        mUserNameText = (TextView) contentView.findViewById(R.id.user_name);
        mProfileLayout = (LinearLayout) contentView.findViewById(R.id.profile_layout);

        mPagerdapter = new AboutMePagerAdapter(getChildFragmentManager(), Arrays.asList(getResources().getStringArray(R.array.about_me_pager_screen_titles)));
        mPager = (ViewPager) contentView.findViewById(R.id.pager);
        mPager.setAdapter(mPagerdapter);

        mSlidingTabLayout = (SlidingTabLayout) contentView.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_home, R.id.label_tab);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.dark_yelo));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setOnPageChangeListener(this);
        mSlidingTabLayout.setOnMyTabClickListener(this);
        mSlidingTabLayout.setViewPager(mPager);
        mPager.setCurrentItem(0);

        if (!mIsMyProfileScreen) {
            mAddFABButton.setVisibility(View.GONE);
        } else {
            mAddFABButton.setVisibility(View.VISIBLE);
        }

        if (mFromPagerScreen) {
            mToolbar.setVisibility(View.GONE);
            mAddFABButton.setVisibility(View.GONE);
            mSlidingTabLayout.setBackgroundColor(getResources().getColor(R.color.white));
            mProfileLayout.setBackgroundColor(getResources().getColor(R.color.white));
        }

        if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
            setHasOptionsMenu(true);

        } else {
            loadOtherUserDetails();
            setHasOptionsMenu(true);

        }

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fabbutton) {
            if (mIndexOfPager == 0) {
                if (Build.VERSION.SDK_INT > 15) {
                    final Intent createCard = new Intent(getActivity(), CreateCardActivity.class);
                    ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), Pair.create((View) v, "fab"));
                    getActivity().startActivity(createCard, transitionActivityOptions.toBundle());
                } else {
                    final Intent createCard = new Intent(getActivity(), CreateCardActivity.class);
                    startActivity(createCard);
                }
            } else {
                final Intent createServiceCard = new Intent(getActivity(), CreateServiceCardActivity.class);
                createServiceCard.putExtra(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE.getId());

                startActivity(createServiceCard);
            }
        }
    }


    private void setUserDetails(Cursor cursor) {
        final String tagCount = cursor.getString(cursor.getColumnIndex(DatabaseColumns.TAG_COUNT));
        final String connectCount = cursor.getString(cursor.getColumnIndex(DatabaseColumns.CONNECT_COUNT));

        String recommendsLabel;
        if (TextUtils.isEmpty(tagCount)) {
            recommendsLabel = "0";
        } else {
            recommendsLabel = tagCount;
        }

        String tagLabel;

        if (TextUtils.isEmpty(tagCount)) {
            tagLabel = "0";
        } else {
            tagLabel = connectCount;
        }

        mUserSubText.setText(recommendsLabel + " referrals  • " + tagLabel + " connections");
        mUserNameText.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_DESCRIPTION)));
        setToolbar(mToolbar, cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_NAME)), false);


        ColorGenerator generator = ColorGenerator.DEFAULT;
        int color = generator.getColor((cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase());
        Resources r = getActivity().getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());

        TextDrawable drawable = TextDrawable.builder().buildRoundRect((cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase(), color, Math.round(px));
        if (cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_IMAGE)).contains("assets/fallback/")) {
            Utils.loadCircularImage(getActivity(), mUserImageView, "", AvatarBitmapTransformation.AvatarSize.BIG, drawable);
        } else {
            Utils.loadCircularImage(getActivity(), mUserImageView, cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_IMAGE)), AvatarBitmapTransformation.AvatarSize.BIG, drawable);
        }
    }

    //*******************************************************************
    // Menu Related Functions
    //*******************************************************************


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        int menuResId = 0;
        if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
            menuResId = R.menu.profile_options;
        }
        else {
            menuResId = R.menu.share_option_dark_yelo;
        }

        inflater.inflate(menuResId, menu);

    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                getActivity().finish();

                return true;
            }


            case R.id.action_editprofile: {
                final Intent mEditProfileIntent = new Intent(getActivity(),
                        EditProfileActivity.class);
                startActivity(mEditProfileIntent);

                return true;
            }

            case R.id.action_mywalls: {

                final Intent mWalls = new Intent(getActivity(),
                        WallsViewActivity.class);

                mWalls.putExtra(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE.getId());
                startActivity(mWalls);
                return true;
            }


            case R.id.action_share: {
                mShareImage = mContentView.getDrawingCache();


//                Bitmap iconTop = BitmapFactory.decodeResource(getActivity().getResources(),
//                        R.drawable.ic_launcher);
//
//                mShareImage = putOverlayRight(getActivity(), mShareImage, iconTop);
                Utils.shareImageAsBitmap(mShareImage, getActivity(), getResources().getString(R.string.share_profile));


                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }


    //*******************************************************************
    // Data Related Functions
    //*******************************************************************


    private void loadMyUserDetails() {
        fetchUserDetailsFromServer(mUserId);
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_USER, null, this);
    }


    private void loadOtherUserDetails() {
        fetchUserDetailsFromServer(mUserId);
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_OTHER_USER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        if (loaderId == AppConstants.Loaders.LOAD_USER) {
            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableUsers.NAME, null, selection, new String[]{mUserId}, null, null, null, null);
        } else if (loaderId == AppConstants.Loaders.LOAD_OTHER_USER) {
            String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableUsers.NAME, null, selection, new String[]{mUserId}, null, null, null, null);
        }
        return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == AppConstants.Loaders.LOAD_USER) {
            Logger.d(TAG, "User Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {
                if (cursor.moveToFirst()) {
                    setUserDetails(cursor);
                }
            }
        } else if (loader.getId() == AppConstants.Loaders.LOAD_OTHER_USER) {
            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (isAttached()) {
                if (cursor.moveToFirst()) {
                    setUserDetails(cursor);
                }
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    /**
     * Method called when an asynchronous insert operation is done
     *
     * @param taskId      The token passed into the async mthod
     * @param cookie      Any extra object passed into the query.
     * @param insertRowId The inserted row id, or -1 if it failed
     */
    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }


    /**
     * Method called when an asynchronous delete operation is done
     *
     * @param taskId      The token passed into the async method
     * @param cookie      Any extra object passed into the query.
     * @param deleteCount The number of rows deleted
     */
    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }


    /**
     * Method called when an asynchronous update operation is done
     *
     * @param taskId      The token passed into the async method
     * @param cookie      Any extra object passed into the query.
     * @param updateCount The number of rows updated
     */
    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

    }


    /**
     * Method called when an asyncronous query operation is done
     *
     * @param taskId The token passed into the async method
     * @param cookie Any extra object passed into the query.
     * @param cursor The {@link Cursor} read from the database
     */
    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }

    //*******************************************************************
    // Http Related Functions
    //*******************************************************************


    private void fetchUserDetailsFromServer(String userId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_USER_DETAILS);
        retroCallbackList.add(retroCallback);

        mYeloApi.getUserDetailAsync(userId, retroCallback);
    }


    /**
     * Method callback when the success response is received
     *
     * @param model     model response received from the server
     * @param requestId The id of the response
     */
    @Override
    public void success(Object model, int requestId) {
        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_USER_DETAILS: {
                GetUserModel getUserModel = ((GetUserModel) model);
                String tagNamesValue = "";
                String tagIdsValue = "";
                userRefresh(false);
                //mProgressWheel.setVisibility(View.INVISIBLE);
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
                break;
            }
        }

    }


    /**
     * Method callback when the request is failed
     *
     * @param requestId The id of the response
     * @param errorCode The errorcode of the response
     * @param message
     */
    @Override
    public void failure(int requestId, int errorCode, String message) {

    }

    //*******************************************************************
    // Interface Implementation
    //*******************************************************************


    /**
     * Method callback when the chat action is clicked
     *
     * @param position position of the tab clicked
     */
    @Override
    public void onTabClicked(int position) {

    }


    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.
     *
     * @param position             Position index of the first page currently being displayed.
     *                             Page position+1 will be visible if positionOffset is nonzero.
     * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
     * @param positionOffsetPixels Value in pixels indicating the offset from position.
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }


    /**
     * This method will be invoked when a new page becomes selected. Animation is not
     * necessarily complete.
     *
     * @param position Position index of the new selected page.
     */
    @Override
    public void onPageSelected(int position) {
        mIndexOfPager = position;
    }


    /**
     * Called when the scroll state changes. Useful for discovering when the user
     * begins dragging, when the pager is automatically settling to the current page,
     * or when it is fully stopped/idle.
     *
     * @param state The new scroll state.
     * @see ViewPager#SCROLL_STATE_IDLE
     * @see ViewPager#SCROLL_STATE_DRAGGING
     * @see ViewPager#SCROLL_STATE_SETTLING
     */
    @Override
    public void onPageScrollStateChanged(int state) {

    }


    //*******************************************************************
    // Private classes
    //*******************************************************************

    private class AboutMePagerAdapter extends FragmentStatePagerAdapter {

        private final List<String> mTabs;
        private int mScrollY;


        public AboutMePagerAdapter(final FragmentManager fm, final List<String> tabs) {
            super(fm);
            mTabs = tabs;
        }


        public void setScrollY(int scrollY) {
            mScrollY = scrollY;
        }


        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case 0: {
                    /* Passing on my knowledge to my children */
                    if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
                        mBundle.putBoolean(AppConstants.Keys.FROM_HOMESCREEN, true);
                    }
                    return UserProfileFragment.newInstance(mBundle);
                }

                case 1: {
                    /* Passing on my knowledge to my children */
                    if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
                        mBundle.putBoolean(AppConstants.Keys.FROM_HOMESCREEN, true);
                    }
                    Fragment f = ServiceCardsFragment.newInstance(mBundle);
                    return f;
                }

                default: {
                    return null;
                }
            }
        }


        @Override
        public int getCount() {
            return mTabs.size();
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position);
        }
    }


    //*******************************************************************
    // Enforced by base class
    //*******************************************************************


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    //*******************************************************************
    // Utility Functions
    //*******************************************************************


    public static UserProfilePagerFragment newInstance(Bundle args) {
        UserProfilePagerFragment f = new UserProfilePagerFragment();
        f.setArguments(args);
        return f;
    }



    //*******************************************************************
    // End of class
    //*******************************************************************

}
