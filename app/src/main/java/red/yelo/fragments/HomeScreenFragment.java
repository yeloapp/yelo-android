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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import red.yelo.R;
import red.yelo.activities.AbstractYeloActivity;
import red.yelo.activities.CreateCardActivity;
import red.yelo.activities.CreateServiceCardActivity;
import red.yelo.activities.EditProfileActivity;
import red.yelo.activities.HomeActivity;
import red.yelo.activities.LeaderBoardActivity;
import red.yelo.activities.NotificationSummaryActivity;
import red.yelo.activities.PostOnWallActivity;
import red.yelo.activities.RewardsActivity;
import red.yelo.activities.SearchLocationActivity;
import red.yelo.activities.ServiceCardsActivity;
import red.yelo.activities.SettingsActivity;
import red.yelo.activities.UserProfileActivity;
import red.yelo.activities.WallsViewActivity;
import red.yelo.bus.ScrollToTop;
import red.yelo.bus.ShowPostShowcase;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableNotifications;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.response.GoogleGeocodeResponse;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.RequestCodes;
import red.yelo.utils.AppConstants.UserInfo;
import red.yelo.utils.Logger;
import red.yelo.utils.SearchViewNetworkQueryHelper;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.widgets.slidingtabs.SlidingTabLayout;


/**
 * Created by anshul1235 on 14/07/14.
 */
public class HomeScreenFragment extends AbstractYeloFragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>, ViewPager.OnPageChangeListener, ObservableScrollViewCallbacks, SlidingTabLayout.MyTabClickListener, RetroCallback.RetroResponseListener, SearchViewNetworkQueryHelper.NetworkCallbacks, MenuItemCompat.OnActionExpandListener {


    public static final String TAG = "HomeScreenFragment";
    //private TabPageIndicator mIndicator;
    //private List<String> mTitles;
    private PagerAdapter mPagerdapter;

    private SlidingTabLayout mSlidingTabLayout;

    private View mHeaderView, mView;

    private int mBaseTranslationY;


    private ViewPager mPager;
    private int mBadgeCount = 0;
    // Animation
    private Animation mAnimBounce;

    private TextView mNotificationCountTextView;

    private int mPreviousNotificationCount;

    private EditText mEditQuery;

    private TextView mLocationTitleText;

    private Toolbar mToolbar;

    private boolean mPageSelected, mFirstOpen;

    private FloatingActionButton mFabButton;

    private int lastIndex = 2;

    private SearchView mSearchView;

    private SearchViewNetworkQueryHelper mSearchNetworkQueryHelper;


    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();


    /**
     * Intent filter for chat button click events
     */
    private final IntentFilter mQueryButtonClicked = new IntentFilter(AppConstants.ACTION_QUERY_BUTTON_CLICKED);


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);

        setHasOptionsMenu(true);


        final View contentView = inflater.inflate(R.layout.fragment_home_screen, container, false);

        mAnimBounce = AnimationUtils.loadAnimation(getActivity(), R.anim.bouncing);

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);


        mLocationTitleText = (TextView) mToolbar.findViewById(R.id.text_location);
        mLocationTitleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPager.getCurrentItem() == 1) {
                    final Intent mSearchActivity = new Intent(getActivity(), SearchLocationActivity.class);
                    startActivityForResult(mSearchActivity, AppConstants.RequestCodes.GET_PLACE);
                }
            }
        });
        mHeaderView = contentView.findViewById(R.id.header);
        ViewCompat.setElevation(mHeaderView, 4);

        mFabButton = (FloatingActionButton) contentView.findViewById(R.id.fabbutton);

        mFabButton.setOnClickListener(this);


        if (TextUtils.isEmpty(SharedPreferenceHelper.getString(R.string.pref_city))) {
            ((AbstractYeloActivity) getActivity()).setLocationTitleWithRedDot("yelo", mToolbar, mLocationTitleText);

        } else {
            ((AbstractYeloActivity) getActivity()).setLocationTitleWithRedDot(SharedPreferenceHelper.getString(R.string.pref_city), mToolbar, mLocationTitleText);

        }

        ((HomeActivity) getActivity()).setSupportActionBar(mToolbar);

        ((HomeActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        final Bundle extras = getArguments();

        int pagerPosition;
        if (extras != null && extras.containsKey(AppConstants.Keys.PAGER_POSITION)) {
            pagerPosition = extras.getInt(AppConstants.Keys.PAGER_POSITION);
        } else {
            //Set it to feed page
            pagerPosition = 1;
        }

        mPagerdapter = new PagerAdapter(getChildFragmentManager(), Arrays.asList(getResources().getStringArray(R.array.home_screen_titles)));
        mPager = (ViewPager) contentView.findViewById(R.id.pager);
        mPager.setAdapter(mPagerdapter);

        mSlidingTabLayout = (SlidingTabLayout) contentView.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_home, R.id.label_tab);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.dark_yelo));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setOnPageChangeListener(this);
        mSlidingTabLayout.setOnMyTabClickListener(this);
        mSlidingTabLayout.setViewPager(mPager);
        mFirstOpen = true;

        mPager.setCurrentItem(pagerPosition);

        /*mIndicator = (TabPageIndicator) contentView.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setOnPageChangeListener(this);

        mIndicator.setCurrentItem(pagerPosition);*/
        loadNotifications();

        if (!TextUtils.isEmpty(SharedPreferenceHelper.getString(R.string.pref_latitude))) {
            getLatLongFromAddress(Double.parseDouble(SharedPreferenceHelper.getString(R.string.pref_latitude)), Double.parseDouble(SharedPreferenceHelper.getString(R.string.pref_longitude)));
        }

        return contentView;

    }


    public FloatingActionButton getFabButton() {
        return mFabButton;
    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.layout_home_notification) {
            //loadNotificationInfoIntoActionBar(0);
            mBadgeCount = 0;
            final Intent mNotificationScreen = new Intent(getActivity(), NotificationSummaryActivity.class);
            startActivity(mNotificationScreen);
        } else if (v.getId() == R.id.fabbutton) {
            if (lastIndex == 0) {
                if (Build.VERSION.SDK_INT > 15) {
                    final Intent createServiceCard = new Intent(getActivity(), CreateServiceCardActivity.class);
                    createServiceCard.putExtra(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE.getId());
                    ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), Pair.create((View) v, "fab"));
                    getActivity().startActivity(createServiceCard, transitionActivityOptions.toBundle());
                } else {
                    final Intent createServiceCard = new Intent(getActivity(), CreateServiceCardActivity.class);
                    createServiceCard.putExtra(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE.getId());

                    startActivity(createServiceCard);
                }
            } else if (lastIndex == 1) {
                if (Build.VERSION.SDK_INT > 15) {
                    final Intent postonWall = new Intent(getActivity(), PostOnWallActivity.class);

                    ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), Pair.create((View) v, "fab"));
                    getActivity().startActivity(postonWall, transitionActivityOptions.toBundle());
                } else {
                    final Intent postonWall = new Intent(getActivity(), PostOnWallActivity.class);
                    startActivity(postonWall);
                }
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_NOTIFICATIONS) {

            String selection = DatabaseColumns.NOTIFICATION_STATUS + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(getActivity(), false, TableNotifications.NAME, null, selection, new String[]{AppConstants.NotificationStatus.UNREAD_NOT_OPENED}, null, null, null, null);

        } else {
            return null;
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (loader.getId() == AppConstants.Loaders.LOAD_NOTIFICATIONS) {
            mBadgeCount = cursor.getCount();

            if (mNotificationCountTextView != null) {
              //  loadNotificationInfoIntoActionBar(mBadgeCount);
            }
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_NOTIFICATIONS) {
        }
    }


    @Override
    public void onPageScrolled(int i, float v, int i2) {
        if (i2 == 2) {
            //mFabButton.hide(true);
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
    }


    @Override
    public void onPageSelected(int index) {

        getActivity().invalidateOptionsMenu();

        int[] materialColors = getActivity().getResources().getIntArray(R.array.collectionListColors);


        if (!mFirstOpen) {
            mPageSelected = true;
        }
        if (index == 2) {
            clearChatNotifications();
            mFabButton.hide(true);
            lastIndex = index;
            ((AbstractYeloActivity) getActivity()).setLocationTitleWithRedDot("yelo", mToolbar, mLocationTitleText);

        }
        if (index == 1) {

            //  clearNotifications();

            mFabButton.show();

            if (lastIndex != 2) {
                Utils.hideShowViewByScale(mFabButton, R.drawable.ic_ask);
            }
            lastIndex = index;

            ((AbstractYeloActivity) getActivity()).setLocationTitleWithRedDot(SharedPreferenceHelper.getString(R.string.pref_city), mToolbar, mLocationTitleText);

        }
        if (index == 0) {
            mFabButton.show(true);
            /* Change made by Sharath Pandeshwar on 19/03/2015 to accommodate Discover page in the place of About Me Page */
            Utils.hideShowViewByScale(mFabButton, R.drawable.ic_add_text);
            //Utils.hideViewByScale(mFabButton);
           // mFabButton.hide(true);

            /* End of changes made by Sharath */
            lastIndex = index;
            ((AbstractYeloActivity) getActivity()).setLocationTitleWithRedDot(SharedPreferenceHelper.getString(R.string.pref_city), mToolbar, mLocationTitleText);


        }

        if (mView != null) {
            propagateToolbarState(toolbarIsShown(), mView);
        }

    }


    /**
     * Cancels any notifications being displayed. Call this if the relevant screen is opened within
     * the app
     */
    public void clearChatNotifications() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        notificationManager.cancel(AppConstants.MESSAGE_NOTIFICATION_ID);
    }


    @Override
    public void onResume() {
        super.onResume();

        if(mPager.getCurrentItem()!=0)
        setCityDotRed();
    }


    private void setCityDotRed() {
        if (TextUtils.isEmpty(SharedPreferenceHelper.getString(R.string.pref_city))) {
            ((AbstractYeloActivity) getActivity()).setLocationTitleWithRedDot("yelo", mToolbar, mLocationTitleText);

        } else {
            ((AbstractYeloActivity) getActivity()).setLocationTitleWithRedDot(SharedPreferenceHelper.getString(R.string.pref_city), mToolbar, mLocationTitleText);

        }
    }


    @Override
    public void onPageScrollStateChanged(int i) {

    }


    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        if (dragging) {
            int toolbarHeight = mToolbar.getHeight();
            float currentHeaderTranslationY = ViewHelper.getTranslationY(mHeaderView);
            if (firstScroll) {
                if (-toolbarHeight < currentHeaderTranslationY) {
                    mBaseTranslationY = scrollY;
                }
            }
            int headerTranslationY = Math.min(0, Math.max(-toolbarHeight, -(scrollY - mBaseTranslationY)));
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewHelper.setTranslationY(mHeaderView, headerTranslationY);
        }
    }


    @Override
    public void onDownMotionEvent() {

    }


    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        mBaseTranslationY = 0;

        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return;
        }
        View view = fragment.getView();
        if (view == null) {
            return;
        }

        // ObservableXxxViews have same API
        // but currently they don't have any common interfaces.
        adjustToolbar(scrollState, view);
    }


    public Fragment getCurrentFragment() {
        return mPagerdapter.getItem(mPager.getCurrentItem());
    }


    public Toolbar getToolbar() {
        return mToolbar;
    }


    public View getHeaderView() {
        return mHeaderView;
    }


    public void adjustToolbar(ScrollState scrollState, View view) {
        int toolbarHeight = mToolbar.getHeight();
        final Scrollable scrollView = (Scrollable) view.findViewById(R.id.yelo_board_list);
        if (scrollView == null) {
            return;
        }

        mView = view;

        if (scrollState == ScrollState.UP) {
            if (toolbarHeight < scrollView.getCurrentScrollY()) {
                hideToolbar();
            } else if (scrollView.getCurrentScrollY() < toolbarHeight) {
                showToolbar();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (toolbarHeight < scrollView.getCurrentScrollY()) {
                showToolbar();
            }
        }
    }


    private void showToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        if (headerTranslationY != 0) {
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewPropertyAnimator.animate(mHeaderView).translationY(0).setDuration(200).start();
        }
        propagateToolbarState(true, mView);
    }


    private void hideToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        int toolbarHeight = mToolbar.getHeight();
        if (headerTranslationY != -toolbarHeight) {
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewPropertyAnimator.animate(mHeaderView).translationY(-toolbarHeight).setDuration(200).start();
        }
        propagateToolbarState(false, mView);
    }


    public void propagateToolbarState(boolean isShown, View view) {
        int toolbarHeight = mToolbar.getHeight();

        mView = view;
        // Set scrollY for the fragments that are not created yet
        mPager.setScrollY(isShown ? 0 : toolbarHeight);

        // Set scrollY for the active fragments
        for (int i = 0; i < mPagerdapter.getCount(); i++) {
            // Skip current item
            if (i == mPager.getCurrentItem()) {
                continue;
            }

            // Skip destroyed or not created item
            Fragment f = mPagerdapter.getItem(i);
            if (f == null) {
                continue;
            }

            propagateToolbarState(isShown, view, toolbarHeight);
        }
    }


    public boolean toolbarIsShown() {
        return ViewHelper.getTranslationY(mHeaderView) == 0;
    }


    private void propagateToolbarState(boolean isShown, View view, int toolbarHeight) {
        Scrollable scrollView = (Scrollable) view.findViewById(R.id.yelo_board_list);
        if (scrollView == null) {
            return;
        }
        if (isShown) {
            // Scroll up
            if (0 < scrollView.getCurrentScrollY()) {
                scrollView.scrollVerticallyTo(0);
            }
        } else {
            // Scroll down (to hide padding)
            if (scrollView.getCurrentScrollY() < toolbarHeight) {
                scrollView.scrollVerticallyTo(toolbarHeight);
            }

        }
    }


    @Override
    public void onTabClicked(int position) {

        if (!mPageSelected) {

            mBus.post(new ScrollToTop(true));
        }
        mPageSelected = false;
        mFirstOpen = false;
    }


    @Override
    public void success(Object model, int requestId) {
        if (requestId == HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE) {
            String[] address;
            GoogleGeocodeResponse googleGeocodeResponse = ((GoogleGeocodeResponse) model);
            if (googleGeocodeResponse.results.size() != 0) {
                address = googleGeocodeResponse.results.get(0).getAddress();

                if (address.length >= 3) {
                    SharedPreferenceHelper.set(R.string.pref_city, address[address.length - 3].trim());
                    ((AbstractYeloActivity) getActivity()).setLocationTitleWithRedDot(address[address.length - 3].trim(), mToolbar, mLocationTitleText);
                }
            } else {
            }
        }


    }


    @Override
    public void failure(int requestId, int errorCode, String message) {

    }


    @Override
    public void performQuery(SearchView searchView, String query) {

    }


    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return false;
    }


    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return false;
    }


    public class PagerAdapter extends FragmentStatePagerAdapter {

        private final List<String> mTabs;
        private int mScrollY;


        public PagerAdapter(final FragmentManager fm, final List<String> tabs) {
            super(fm);
            mTabs = tabs;
        }


        public void setScrollY(int scrollY) {
            mScrollY = scrollY;
        }


        private int[] imageResId = {R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher};


        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case 0: {

                    Bundle argsProfile = new Bundle(1);
                    argsProfile.putString(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE.getId());
                    argsProfile.putBoolean(AppConstants.Keys.FROM_HOMESCREEN, true);
                    argsProfile.putBoolean(AppConstants.Keys.PAGER, true);
                    argsProfile.putInt(AppConstants.Keys.SERVICE_SCREEN_TYPE, AppConstants.ServiceScreenType.PROFILE);
                    /* Changed by Sharath on 19/03/2014 to bring 'Discover' fragment in place of 'About Me */
                    //return UserProfilePagerFragment.newInstance(argsProfile);
                    return DiscoverFragment.newInstance();
                    //return UserProfilePagerFragment.newInstance(argsProfile);
                    /* End of changes made by Sharath */

//                    if (0 < mScrollY) {
//                        Bundle args = new Bundle();
//                        args.putInt(YeloBoardFragment.ARG_INITIAL_POSITION, 1);
//                        f.setArguments(args);
//                    }

                }

                case 1: {

                    Fragment f = YeloBoardFragment.newInstance();
                    Bundle args = new Bundle();
                    args.putInt(YeloBoardFragment.ARG_INITIAL_POSITION, 1);
                    f.setArguments(args);
                    return f;

//                    Bundle argsProfile = new Bundle(1);
//                    argsProfile.putString(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE.getId());
//                    argsProfile.putBoolean(AppConstants.Keys.FROM_HOMESCREEN,true);
//                    return CollectionListFragment.newInstance();
                }


                case 2: {
                    Bundle args = new Bundle(1);
                    return ChatsFragment.newInstance(args);
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

//            Drawable image = getResources().getDrawable(imageResId[position]);
//            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
//            SpannableString sb = new SpannableString(" ");
//            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
//            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            return sb;
            return mTabs.get(position);
        }
    }


    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {


        int menuResId = R.menu.home_only_notifications;
        if (mPager != null) {


            menuResId = (mPager.getCurrentItem() == 0 ? R.menu.home_menu_profile : R.menu.home);
            if (mPager.getCurrentItem() == 2) {
                menuResId = R.menu.home;
            }
        }
        inflater.inflate(menuResId, menu);
//        if (mPager.getCurrentItem() != 0) {
//            final MenuItem menuItem = menu.findItem(R.id.action_notifications);
//            final View actionView = menuItem.getActionView();
//
//            if (actionView != null) {
//                mNotificationCountTextView = (TextView) actionView.findViewById(R.id.notification_count);
//
//                actionView.findViewById(R.id.layout_home_notification).setOnClickListener(this);
//
//                loadNotificationInfoIntoActionBar(mBadgeCount);
//            } else {
//                Logger.e(TAG, "ACTION VIEW IS NULL");
//            }
//        }

        if (mPager.getCurrentItem() == 0) {
            //final MenuItem menuItem = menu.findItem(R.id.action_search);

            //mSearchView = (SearchView) MenuItemCompat.getActionView(menuItem);
            //MenuItemCompat.setOnActionExpandListener(menuItem, this);

            if (isAttached()) {
               /* if (mSearchView.getChildAt(0) != null) {
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
                mSearchNetworkQueryHelper.setSuggestCountThreshold(0);
                mSearchNetworkQueryHelper.setSuggestWaitThreshold(400); */
            }

        }
    }

//
//    private void loadNotificationInfoIntoActionBar(int notificationCount) {
//        if (notificationCount == 0) {
//            mNotificationCountTextView.setVisibility(View.GONE);
//
//
//        } else {
//            mNotificationCountTextView.setVisibility(View.VISIBLE);
//            mNotificationCountTextView.setText(notificationCount + "");
//            mNotificationCountTextView.startAnimation(mAnimBounce);
//
//        }
//    }
//

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {


            case R.id.action_notifications: {

                final Intent mNotificationScreen = new Intent(getActivity(), NotificationSummaryActivity.class);
                startActivity(mNotificationScreen);

                return true;
            }

            case R.id.action_mywalls: {

                final Intent mWalls = new Intent(getActivity(), WallsViewActivity.class);

                mWalls.putExtra(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE.getId());
                startActivity(mWalls);
                return true;
            }

            case R.id.action_settings: {

                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }

            case R.id.action_profile: {

                loadProfile(UserInfo.INSTANCE.getId(), UserInfo.INSTANCE.getFirstName());
                return true;
            }


//            case R.id.action_search: {
//
//                startActivity(new Intent(getActivity(), SearchWallActivity.class));
//                return true;
//            }


//            case R.id.action_help: {
//
//                if (BuildConfig.ENABLE_HELPSHIFT) {
//                    Helpshift.showFAQs(getActivity());
//                }
//                return true;
//            }


            case R.id.action_editprofile: {
                final Intent mEditProfileIntent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(mEditProfileIntent);

                return true;
            }


            case R.id.action_location: {

                final Intent mSearchActivity = new Intent(getActivity(), SearchLocationActivity.class);
                startActivityForResult(mSearchActivity, AppConstants.RequestCodes.GET_PLACE);

                return true;
            }

            /*  Changed by Sharath Pandeshwar on 05/03/2014 */
            case R.id.action_rewards: {
                handleShare();
                return true;
            }

            case R.id.action_leaderboard: {
                final Intent leaderBoard = new Intent(getActivity(), LeaderBoardActivity.class);
                startActivity(leaderBoard);
                return true;
            }

            case R.id.action_me: {
                final Intent aboutMeIntent = new Intent(getActivity(), UserProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(AppConstants.Keys.USER_ID, UserInfo.INSTANCE.getId());
                bundle.putInt(AppConstants.Keys.SERVICE_SCREEN_TYPE, AppConstants.ServiceScreenType.PROFILE);
                bundle.putBoolean(AppConstants.Keys.FROM_HOMESCREEN, true);
                aboutMeIntent.putExtras(bundle);
                startActivity(aboutMeIntent);
                return true;
            }

            case R.id.action_search: {
                final Intent searchServiceIntent = new Intent(getActivity(), ServiceCardsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(AppConstants.Keys.SERVICE_SCREEN_TYPE, AppConstants.ServiceScreenType.SEARCH_WITH_KEYWORDS);
                searchServiceIntent.putExtras(bundle);
                startActivity(searchServiceIntent);
                return true;
            }


            /*  End of modifications done one 05/03/2014 */

//            case R.id.action_search: {
//
//                final Intent mSearchActivity = new Intent(getActivity(),
//                        SearchLocationActivity.class);
//                startActivity(mSearchActivity);
//
//                return true;
//            }


            //depreciated
//            case R.id.action_select_interest: {
//
//                loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
//                                .instantiate(getActivity(), SelectInterestsFragment.class
//                                        .getName(), null), AppConstants.FragmentTags.SELECT_TAGS, true,
//                        null
//                );
//
//                return true;
//            }


//
//            case R.id.action_feedback: {
//
//                final Intent feedBackActivity = new Intent(getActivity(),
//                        FeedbackActivity.class);
//                startActivity(feedBackActivity);
//
//
//                return true;
//            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }


    /**
     * This loads the profile of the user
     *
     * @param userId user id of the user u want to open profile of
     * @param name   name of the user
     */
    private void loadProfile(String userId, String name) {
        final Intent userProfileIntent = new Intent(getActivity(), UserProfileActivity.class);

        userProfileIntent.putExtra(AppConstants.Keys.USER_ID, userId);
        userProfileIntent.putExtra(AppConstants.Keys.USER_NAME, name);
        userProfileIntent.putExtra(AppConstants.Keys.SERVICE_SCREEN_TYPE, AppConstants.ServiceScreenType.PROFILE);
        startActivity(userProfileIntent);
    }


    @Override
    public boolean willHandleDialog(final DialogInterface dialog) {

        if (mPager.getCurrentItem() == 2) {
            ChatsFragment frag1 = (ChatsFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
            return frag1.willHandleDialog(dialog);
        } else if (mPager.getCurrentItem() == 1) {
            YeloBoardFragment frag1 = (YeloBoardFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
            return frag1.willHandleDialog(dialog);
        }
        return false;

    }


    @Override
    public void onDialogClick(final DialogInterface dialog, final int which) {


        if (mPager.getCurrentItem() == 2) {
            ChatsFragment frag1 = (ChatsFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
            frag1.onDialogClick(dialog, which);
        } else if (mPager.getCurrentItem() == 1) {

            YeloBoardFragment frag1 = (YeloBoardFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
            frag1.onDialogClick(dialog, which);

        }


    }


    private void loadNotifications() {

        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_NOTIFICATIONS, null, this);

    }


    @Subscribe
    public void showPostShowcase(ShowPostShowcase showPostShowcase) {
        if (showPostShowcase.show) {
            showPostShowcase();
        }
    }


    private void showPostShowcase() {

        if (isAttached()) {
            ShowcaseView showcaseView = new ShowcaseView.Builder(getActivity(), true).setTarget(new ViewTarget(mFabButton)).setContentText(getResources().getString(R.string.post_tutorial_message)).setContentTitle("Post query").hideOnTouchOutside().setInnerRadius(80).setOuterRadius(100).setStyle(R.style.CustomShowcaseTheme).build();

            SharedPreferenceHelper.set(R.string.pref_post_tutorial_played, true);

            showcaseView.hideButton();

        }

    }




    private void getLatLongFromAddress(double latitude, double longitude) {

        Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());

        if (Geocoder.isPresent()) {

            try {
                List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, 1);

                if (addresses.size() > 0) {

                    double lat = addresses.get(0).getLatitude();
                    double lng = addresses.get(0).getLongitude();
                    String locality = addresses.get(0).getSubLocality();
                    String city = addresses.get(0).getAddressLine(2);

                    if (TextUtils.isEmpty(city)) {
                        fillAddressDetails();
                    }
                    else {
                        SharedPreferenceHelper.set(R.string.pref_city, addresses.get(0).getAddressLine(2).split(",")[0].trim());
                        ((AbstractYeloActivity) getActivity()).setLocationTitleWithRedDot(addresses.get(0).getAddressLine(2).split(",")[0].trim(), mToolbar, mLocationTitleText);

                    }
                    Logger.d(TAG, lat + "  " + lng);

                } else {
                    fillAddressDetails();
                }

            } catch (Exception e) {
                fillAddressDetails();
            }
        }
        else {
            fillAddressDetails();
        }
    }

    private void fillAddressDetails() {

        final Map<String, String> params = new HashMap<String, String>(6);

        params.put(HttpConstants.LATLNG, SharedPreferenceHelper.getString(R.string.pref_latitude)
                + "," + SharedPreferenceHelper.getString(R.string.pref_longitude));
        params.put("language", Locale.getDefault().getCountry());
        params.put("sensor", "false");
        Logger.d(TAG, "LATI: " + SharedPreferenceHelper.getString(R.string.pref_latitude)
                + " LONGI: " +SharedPreferenceHelper.getString(R.string.pref_longitude));

        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_SELECTED_PLACE);
        retroCallbackList.add(retroCallback);

        mGoogleApi.getMyAddress(params, retroCallback);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {
                case AppConstants.RequestCodes.GET_PLACE: {
                    if (resultCode == Activity.RESULT_OK) {
                        if (mPager.getCurrentItem() == 1) {
                            ((YeloBoardFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem())).onActivityResult(requestCode, resultCode, data);
                        }
                        break;

                    }
                }
            }
        }
    }
}
