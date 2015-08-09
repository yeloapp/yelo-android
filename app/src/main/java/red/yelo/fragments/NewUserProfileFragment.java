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
import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.vinaysshenoy.okulus.OkulusImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableUsers;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.GetUserModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.widgets.slidingtabs.SlidingTabLayout;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class NewUserProfileFragment extends AbstractYeloFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        DBInterface.AsyncDbQueryCallback,
        SwipeRefreshLayout.OnRefreshListener,
        RetroCallback.RetroResponseListener, View.OnClickListener,
        SlidingTabLayout.MyTabClickListener, ViewPager.OnPageChangeListener {

    public static final String TAG = "NewUserProfileFragment";
    private String mUserId;

    private ImageView mUserBackgroundImageView;

    private TextView mUserName;

    private OkulusImageView mUserImageView;

    private PagerAdapter mPagerdapter;

    private SlidingTabLayout mSlidingTabLayout;

    private ViewPager mPager;

    private DateFormatter mMessageDateFormatter;


    //private SwipeRefreshLayout mSwipeRefreshLayout;


    private ProgressBar mProgressWheel;

    private String mListId;


    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private Toolbar mToolbar;

    private boolean mFromHomeScreen;

    private String mSelectedTagId;


    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_user_profile_new, container, false);


        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                AppConstants.WALL_DATE_FORMAT);


        Bundle extras = getArguments();

        if (extras != null) {
            mUserId = extras.getString(AppConstants.Keys.USER_ID);
            if (extras.containsKey(AppConstants.Keys.FROM_HOMESCREEN)) {
                mFromHomeScreen = extras.getBoolean(AppConstants.Keys.FROM_HOMESCREEN);
            }
        }

        mToolbar = (Toolbar) contentView.findViewById(R.id.my_awesome_toolbar);

        if (!mFromHomeScreen) {
            setToolbar(mToolbar).setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_action_navigation_arrow_back_white));
        } else {
            mToolbar.setVisibility(View.GONE);
        }
        mProgressWheel = (ProgressBar) contentView.findViewById(R.id.progress_wheel);

        mUserBackgroundImageView = (ImageView) contentView.findViewById(R.id.background_image);

        mUserBackgroundImageView.setBackgroundColor(R.color.primaryColor);


        mUserImageView = (OkulusImageView) contentView.findViewById(R.id.profile_image);
        mUserName = (TextView) contentView.findViewById(R.id.user_name);
//
//        mSwipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipe_container);
//        mSwipeRefreshLayout.setOnRefreshListener(this);
//        mSwipeRefreshLayout.setColorSchemeResources(R.color.white);
//        mSwipeRefreshLayout.setProgressBackgroundColor(R.color.primaryColor);
//        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);

        mPagerdapter = new PagerAdapter(getChildFragmentManager(), Arrays.asList(getResources().getStringArray(R.array.profile_pager_titles)));
        mPager = (ViewPager) contentView.findViewById(R.id.pager);
        mPager.setAdapter(mPagerdapter);

        mSlidingTabLayout = (SlidingTabLayout) contentView.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.profile_home, R.id.label_tab);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.yelo_red));
        //mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setOnPageChangeListener(this);
        mSlidingTabLayout.setOnMyTabClickListener(this);
        mSlidingTabLayout.setViewPager(mPager);


        loadUserDetails();

        return contentView;

    }


    private void fetchUserDetails(String userId) {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_USER_DETAILS);
        retroCallbackList.add(retroCallback);

        mYeloApi.getUserDetailAsync(userId, retroCallback);
        //mSwipeRefreshLayout.setRefreshing(true);


    }


    private void loadUserDetails() {
        fetchUserDetails(mUserId);
        getLoaderManager().restartLoader(AppConstants.Loaders.LOAD_USER, null, this);

    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static NewUserProfileFragment newInstance(Bundle args) {
        NewUserProfileFragment f = new NewUserProfileFragment();
        f.setArguments(args);
        return f;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == AppConstants.Loaders.LOAD_USER) {

            String selection = DatabaseColumns.ID
                    + SQLConstants.EQUALS_ARG;

            return new SQLiteLoader(getActivity(), false, TableUsers.NAME, null,
                    selection, new String[]{mUserId}, null, null, null, null);
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
        // mRecommendCountText.setText(recommendsLabel);

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
//        mReferralCountMadeText.setText(tagLabel);
//        mUserDescriptionText.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_DESCRIPTION)));
//
        mUserName.setText(cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_NAME)));


        ColorGenerator generator = ColorGenerator.DEFAULT;

        int color = generator.getColor((cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase());
        Resources r = getActivity().getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());

        TextDrawable drawable = TextDrawable.builder()
                .buildRect("", color);

        if (cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_IMAGE)).contains("assets/fallback/")) {
            mUserBackgroundImageView.setBackgroundColor(color);
        } else {
            mUserBackgroundImageView.setBackgroundColor(color);
            Utils.loadCircularImage(getActivity(), mUserImageView, cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_IMAGE)), AvatarBitmapTransformation.AvatarSize.BIG, drawable);
        }

//        String tagNames[] = cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_TAGS)).split(",");
//        String tagIds[] = cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_TAGS_IDS)).split(",");
//
//        MultiTagLayout.Tag[] mTags = new MultiTagLayout.Tag[tagNames.length];
//        for (int j = 0; j < tagNames.length; j++) {
//            mTags[j] = new MultiTagLayout.Tag(tagIds[j], tagNames[j]);
//
//        }
//
//        mLocationText.setText(SharedPreferenceHelper.getString(R.string.pref_location));
//
//        ArrayList<MultiTagLayout.Tag> tagList = new ArrayList<MultiTagLayout.Tag>();
//        tagList.addAll(Arrays.asList(mTags));
//        if (tagList.size() != 0) {
//            mMultiTagLayout.setVisibility(View.VISIBLE);
//            mLineBreak2.setVisibility(View.VISIBLE);
//        }
//        mMultiTagLayout.setTags(tagList);


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {


    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

        if (taskId == AppConstants.QueryTokens.UPDATE_USERS) {
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
    public void onRefresh() {
        //  mSwipeRefreshLayout.setEnabled(false);
        fetchUserDetails(mUserId);

    }

    private void hideRefreshing() {
//        mSwipeRefreshLayout.setEnabled(true);
//        mSwipeRefreshLayout.setRefreshing(false);
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


    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.GET_USER_DETAILS: {
                GetUserModel getUserModel = ((GetUserModel) model);
                String tagNamesValue = "";
                String tagIdsValue = "";

                mProgressWheel.setVisibility(View.INVISIBLE);
                if (getUserModel.user.listings.size() > 0) {

                    for(int i=0;i<getUserModel.user.listings.size();i++) {

                        tagNamesValue = tagNamesValue+","+getUserModel.user.listings.get(i).tag_name;
                        tagIdsValue = tagNamesValue+","+getUserModel.user.listings.get(i).tag_id;
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

                DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_USERS, getTaskTag(), values,
                        TableUsers.NAME, values, selection, new String[]{getUserModel.user.id}, true, this);

                hideRefreshing();
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

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onTabClicked(int position) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

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

        private int[] imageResId = {
                R.drawable.ic_launcher,
                R.drawable.ic_launcher,
                R.drawable.ic_launcher
        };


        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }


        @Override
        public Fragment getItem(int position) {

            switch (position) {

                case 0: {

                    Bundle argsProfile = new Bundle(1);
                    argsProfile.putString(AppConstants.Keys.USER_ID, mUserId);
                    return AboutFragment.newInstance(argsProfile);
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

                case 3: {
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


}
