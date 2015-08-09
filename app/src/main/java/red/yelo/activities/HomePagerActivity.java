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

package red.yelo.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.Arrays;
import java.util.List;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableNotifications;
import red.yelo.fragments.ChatsFragment;
import red.yelo.fragments.UserProfileFragment;
import red.yelo.fragments.YeloBoardFragment;
import red.yelo.utils.AppConstants;
import red.yelo.widgets.slidingtabs.SlidingTabLayout;

public class HomePagerActivity extends AbstractYeloActivity implements
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, ActionBar.TabListener, ViewPager.OnPageChangeListener,
        ObservableScrollViewCallbacks {
    public static final String TAG = "HomePagerActivity";
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

    private Toolbar mToolbar;

    private SparseArray<Fragment> mPages;


    /**
     * Intent filter for chat button click events
     */
    private final IntentFilter mQueryButtonClicked = new IntentFilter(
            AppConstants.ACTION_QUERY_BUTTON_CLICKED);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        mAnimBounce = AnimationUtils.loadAnimation(this,
                R.anim.bouncing);

        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        mHeaderView = findViewById(R.id.header);
        ViewCompat.setElevation(mHeaderView, 4);

        setTitle("yelo", mToolbar);

        setSupportActionBar(mToolbar);

        int pagerPosition = 1;

        //Set it to feed page
        pagerPosition = 1;

        mPagerdapter = new PagerAdapter(getSupportFragmentManager(), Arrays.asList(getResources().getStringArray(R.array.home_screen_titles)));
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_home, R.id.label_tab);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.white));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setOnPageChangeListener(this);
        mSlidingTabLayout.setViewPager(mPager);

        mPager.setCurrentItem(pagerPosition);

        /*mIndicator = (TabPageIndicator) contentView.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setOnPageChangeListener(this);

        mIndicator.setCurrentItem(pagerPosition);*/
        loadNotifications();

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.layout_home_notification) {
            loadNotificationInfoIntoActionBar(0);
            mBadgeCount = 0;
            final Intent mNotificationScreen = new Intent(this,
                    NotificationSummaryActivity.class);
            startActivity(mNotificationScreen);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_NOTIFICATIONS) {

            String selection = DatabaseColumns.NOTIFICATION_STATUS + SQLConstants.EQUALS_ARG;
            return new SQLiteLoader(this, false, TableNotifications.NAME, null,
                    selection, new String[]{AppConstants.NotificationStatus.UNREAD_NOT_OPENED}, null, null, null, null);

        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (loader.getId() == AppConstants.Loaders.LOAD_NOTIFICATIONS) {
            mBadgeCount = cursor.getCount();

            if (mNotificationCountTextView != null) {
                loadNotificationInfoIntoActionBar(mBadgeCount);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == AppConstants.Loaders.LOAD_NOTIFICATIONS) {
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        //Do nothing
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        //Do nothing
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int index) {

        this.invalidateOptionsMenu();

        if (index == 2) {
            clearChatNotifications();
        }
        if (index == 1) {

            //  clearNotifications();
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
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(AppConstants.MESSAGE_NOTIFICATION_ID);
    }


    @Override
    public void onResume() {
        super.onResume();
        // clearNotifications();
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
        return mPagerdapter.getItemAt(mPager.getCurrentItem());
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

    public class PagerAdapter extends FragmentStatePagerAdapter {

        private final List<String> mTabs;
        private int mScrollY;


        public PagerAdapter(final FragmentManager fm, final List<String> tabs) {
            super(fm);
            mTabs = tabs;
            mPages = new SparseArray<Fragment>();
        }

        public void setScrollY(int scrollY) {
            mScrollY = scrollY;
        }


        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (0 <= mPages.indexOfKey(position)) {
                mPages.remove(position);
            }
            super.destroyItem(container, position, object);
        }



        @Override
        public Fragment getItem(int position) {

            Fragment f;
            switch (position) {

                case 0: {

                    Bundle argsProfile = new Bundle(1);
                    f = UserProfileFragment.newInstance(argsProfile);

                    argsProfile.putString(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE.getId());
                    break;
                }

                case 1: {

                    f = YeloBoardFragment.newInstance();
                    Bundle args = new Bundle();
                    args.putInt(YeloBoardFragment.ARG_INITIAL_POSITION, 1);
                    f.setArguments(args);
//                    if (0 < mScrollY) {
//                        Bundle args = new Bundle();
//                        args.putInt(YeloBoardFragment.ARG_INITIAL_POSITION, 1);
//                        f.setArguments(args);
//                    }
                    break;
                }


                case 2: {
                    Bundle args = new Bundle(1);
                    f = ChatsFragment.newInstance(args);
                    break;
                }


                default: {
                    return null;
                }
            }

            mPages.put(position, f);
            return f;
        }


        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position);
        }

        public Fragment getItemAt(int position) {
            return mPages.get(position);
        }


    }


//    @Override
//    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
//
//        int menuResId = R.menu.home_only_notifications;
//        if (mPager != null) {
//
//
//            menuResId = (mPager.getCurrentItem() == 0 ? R.menu.home : R.menu.notifications_with_search);
//            if (mPager.getCurrentItem() == 2) {
//                menuResId = R.menu.home_only_notifications;
//            }
//        }
//        inflater.inflate(menuResId, menu);
//
//        final MenuItem menuItem = menu.findItem(R.id.action_notifications);
//        final View actionView = menuItem.getActionView();
//
//        if (actionView != null) {
//            mNotificationCountTextView = (TextView) actionView
//                    .findViewById(R.id.notification_count);
//
//            actionView.findViewById(R.id.layout_home_notification).setOnClickListener(this);
//
////            ((ImageView) actionView
////                    .findViewById(R.id.notification_bell)).setOnClickListener(HomeScreenFragment.this);
//            loadNotificationInfoIntoActionBar(mBadgeCount);
//        } else {
//            Logger.e(TAG, "ACTION VIEW IS NULL");
//        }
//
//    }

    private void loadNotificationInfoIntoActionBar(int notificationCount) {
        if (notificationCount == 0) {
            mNotificationCountTextView.setVisibility(View.GONE);


        } else {
            mNotificationCountTextView.setVisibility(View.VISIBLE);
            mNotificationCountTextView.setText(notificationCount + "");
            mNotificationCountTextView.startAnimation(mAnimBounce);

        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {


            case R.id.action_notifications: {

                mBadgeCount = 0;
                loadNotificationInfoIntoActionBar(0);

                return true;
            }

            case R.id.action_settings: {

                startActivity(new Intent(this, SettingsActivity.class));
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
//                    Helpshift.showFAQs(this);
//                }
//                return true;
//            }

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
//            }805703


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

//
//    @Override
//    public boolean willHandleDialog(final DialogInterface dialog) {
//
//        if (mPager.getCurrentItem() == 2) {
//            ChatsFragment frag1 = (ChatsFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
//            return frag1.willHandleDialog(dialog);
//        } else if (mPager.getCurrentItem() == 1) {
//            YeloBoardFragment frag1 = (YeloBoardFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
//            Logger.d(TAG, "WILL HANDLE");
//            return frag1.willHandleDialog(dialog);
//        }
//        return false;
//
//    }
//
//    @Override
//    public void onDialogClick(final DialogInterface dialog, final int which) {
//
//
//        if (mPager.getCurrentItem() == 2) {
//            ChatsFragment frag1 = (ChatsFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
//            frag1.onDialogClick(dialog, which);
//        } else if (mPager.getCurrentItem() == 1) {
//
//            YeloBoardFragment frag1 = (YeloBoardFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
//            frag1.onDialogClick(dialog, which);
//            Logger.d(TAG, "WILL CLICK");
//
//        }
//
//
//    }


    private void loadNotifications() {

        getSupportLoaderManager().restartLoader(AppConstants.Loaders.LOAD_NOTIFICATIONS, null, this);

    }


}

