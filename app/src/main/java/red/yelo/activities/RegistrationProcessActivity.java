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
 */package red.yelo.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.LocationListener;
import com.soundcloud.android.crop.Crop;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import red.yelo.R;
import red.yelo.bus.PageNextPrevious;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.TableTags;
import red.yelo.fragments.DemoIntroducingCards;
import red.yelo.fragments.EditProfileFragment;
import red.yelo.fragments.LoginFragment;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.response.TagsRecommendationResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.FragmentTags;
import red.yelo.utils.GooglePlayClientWrapper;
import red.yelo.utils.GooglePlusManager;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import red.yelo.utils.Utils;
import red.yelo.widgets.RegistrationViewPager;
import red.yelo.widgets.StepPagerStrip;

public class RegistrationProcessActivity extends AbstractYeloActivity implements
        GooglePlusManager
                .GooglePlusAuthCallback, ViewPager.OnPageChangeListener, LocationListener,
        RetroCallback.RetroResponseListener{

    public static final String TAG = "RegistrationProcessActivity";


    /**
     * Helper class for connecting to GooglePlus for login
     */
    private GooglePlusManager mGooglePlusManager;

    /**
     * Strip like indicator for steps
     */
    private StepPagerStrip mStepPagerStrip;

    /**
     * Pager for holding all the registration screens(fragments)
     */
    private RegistrationViewPager mPager;

    /**
     * Screens fragment adapter
     */
    private FragmentStatePagerAdapter mPagerdapter;

    /**
     * Helper for connecting to Google Play Services
     */
    private GooglePlayClientWrapper mGooglePlayClientWrapper;

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();



    private static final int ACTION_BAR_DISPLAY_MASK = ActionBar.DISPLAY_SHOW_TITLE |
            ActionBar.DISPLAY_USE_LOGO | ActionBar
            .DISPLAY_SHOW_HOME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_registration);

        mBus.register(this);

        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mPager = (RegistrationViewPager) findViewById(R.id.pager);

        mStepPagerStrip.setPageCount(getResources().getInteger(R.integer.registration_step_count));
        mStepPagerStrip.setCurrentPage(0);

        mGooglePlayClientWrapper = new GooglePlayClientWrapper(this, this);
        mGooglePlusManager = new GooglePlusManager(this, this);


        mPagerdapter = new RegistrationStepsAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerdapter);
        mPager.setPagingEnabled(false);

        mPager.setOnPageChangeListener(this);
        if(savedInstanceState == null) {

            Utils.registerReferralValuesInAnalytics();
        }
    }


    @Override
    public void onLocationChanged(Location location) {

        AppConstants.DeviceInfo.INSTANCE.setLatestLocation(location);

        //location is saved locally whenever there is a location change

        SharedPreferenceHelper.set(R.string.pref_latitude, location.getLatitude() + "");
        SharedPreferenceHelper.set(R.string.pref_longitude, location.getLongitude() + "");


        Logger.d(TAG, location.getLatitude() + "");
        mGooglePlayClientWrapper.onStop();

        //set app will fetch location (to prevent location to fetch again and
        // again when this activity is called)
        SharedPreferenceHelper
                .set(R.string.pref_update_location, false);

    }

    @Override
    protected void onStop() {
        mGooglePlusManager.onActivityStopped();
        mGooglePlayClientWrapper.onStop();

        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (SharedPreferenceHelper
                .getBoolean(R.string.pref_update_location)) {
            mGooglePlayClientWrapper.onStart();
        }
        mGooglePlusManager.onActivityStarted();


    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == GooglePlusManager.CONNECTION_UPDATE_ERROR)
                && (resultCode == RESULT_OK)) {
            mGooglePlusManager.onActivityResult();
        }
        else if (requestCode == Crop.REQUEST_CROP) {

            EditProfileFragment frag1 = (EditProfileFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
            frag1.onActivityResult
                    (requestCode, resultCode,
                            data);
            Logger.d(TAG, "REQUEST CROP");
        }

        else if (requestCode == Crop.REQUEST_PICK) {

            EditProfileFragment frag1 = (EditProfileFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
            frag1.onActivityResult
                    (requestCode, resultCode,
                            data);
            Logger.d(TAG, "REQUEST PICK");
        }
    }

    /**
     * Gets a reference to the Google Plus Manager
     */
    public GooglePlusManager getPlusManager() {

        return mGooglePlusManager;
    }


    /**
     * this calls the google Login method of EditProfileFragment
     */
    @Override
    public void onLogin() {

        if (mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem()).getClass().equals(EditProfileFragment.class)) {

            EditProfileFragment editProfileFragment = (EditProfileFragment)
                    mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());


            if (editProfileFragment != null && editProfileFragment.isResumed()) {
                editProfileFragment.onGoogleLogin();
            }
        }
    }

    @Override
    public void onLoginError(final Exception error) {

        if (mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem()).getClass().equals(EditProfileFragment.class)) {
            EditProfileFragment editProfileFragment = (EditProfileFragment)
                    mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());


            if (editProfileFragment != null && editProfileFragment.isResumed()) {
                editProfileFragment.onGoogleLoginError(error);
            }
        }

    }

    @Override
    public void onLogout() {

        if (mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem()).getClass().equals(EditProfileFragment.class)) {

            final EditProfileFragment fragment = ((EditProfileFragment) getSupportFragmentManager()
                    .findFragmentByTag(
                            FragmentTags.EDIT_PROFILE));

            if (fragment != null && fragment.isResumed()) {
                fragment.onGoogleLogout();
            }
        }
    }


    @Override
    protected Object getTaskTag() {
        return null;
    }


    @Override
    protected void onResume() {
        super.onResume();

        mBus.post(new PageNextPrevious(true, SharedPreferenceHelper.getInt(R.string.pref_registration_screens)));

    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        mStepPagerStrip.setCurrentPage(i);

        if(i!=0){
            mStepPagerStrip.setVisibility(View.GONE);
        }

    }



    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void success(Object model, int requestId) {
        switch (requestId){
        case HttpConstants.ApiResponseCodes.GET_TAG_SUGGESTIONS: {
            TagsRecommendationResponseModel tagsRecommendationResponseModel = ((TagsRecommendationResponseModel) model);


            for (int i = 0; i < tagsRecommendationResponseModel.tags.size(); i++) {

                ContentValues values = new ContentValues();
                values.put(DatabaseColumns.ID, tagsRecommendationResponseModel.tags.get(i).id);
                values.put(DatabaseColumns.NAME, tagsRecommendationResponseModel.tags.get(i).name);


                String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                Logger.d(TAG, "UPDATE");

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

    public class RegistrationStepsAdapter extends FragmentStatePagerAdapter {

        public RegistrationStepsAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public int getItemPosition(Object object) {

            return POSITION_NONE;
        }


        @Override
        public Fragment getItem(int position) {

            switch (position) {


                case 0:

                    mStepPagerStrip.setVisibility(View.GONE);
                    return LoginFragment.newInstance();


                case 1:

                    return EditProfileFragment.newInstance();


                case 2:

                    Bundle args = new Bundle(1);
                    args.putBoolean(AppConstants.Keys.FROM_LOGIN, true);
                    return DemoIntroducingCards.newInstance(args);

                //TODO depreciated
//                case 3:
//
//                    Bundle argsInterest = new Bundle();
//                    argsInterest.putBoolean(AppConstants.Keys.FROM_LOGIN, true);
//                    return SelectInterestsFragment.newInstance(argsInterest);


                default:
                    break;
            }
            return null;


        }
    }

    // It is invoked when bus posts the change in screen (during the registration process)
    @Subscribe
    public void onPageChange(PageNextPrevious event) {

        if (event.next) {

            mPager.setCurrentItem(event.screen,true);
            SharedPreferenceHelper.set(R.string.pref_registration_screens, event.screen);
        }



    }

    private void fetchTagSuggestions() {
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_TAG_SUGGESTIONS);
        retroCallbackList.add(retroCallback);
        mYeloApi.getTagRecommendations(retroCallback);

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {

        if (mPager.getCurrentItem() == 1) {

            EditProfileFragment frag1 = (EditProfileFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
            frag1.onDialogClick(dialog, which);
            Logger.d(TAG, "WILL CLICK");

        }

    }


}
