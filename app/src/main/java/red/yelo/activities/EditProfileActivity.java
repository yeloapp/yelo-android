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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.soundcloud.android.crop.Crop;

import red.yelo.R;
import red.yelo.fragments.AbstractYeloFragment;
import red.yelo.fragments.EditProfileFragment;
import red.yelo.utils.AppConstants;
import red.yelo.utils.GooglePlusManager;
import red.yelo.utils.Logger;

public class EditProfileActivity extends AbstractYeloActivity implements GooglePlusManager
        .GooglePlusAuthCallback {


    public static final String TAG = "EditProfileActivity";

    /**
     * Helper class for connecting to GooglePlus for login
     */
    private GooglePlusManager mGooglePlusManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);


        mGooglePlusManager = new GooglePlusManager(this, this);

        if (savedInstanceState == null) {
            loadEditProfileFragment();
        }
    }


    /**
     * Gets a reference to the Google Plus Manager
     */
    public GooglePlusManager getPlusManager() {

        return mGooglePlusManager;
    }


    /**
     * Loads the {@link red.yelo.fragments.ImageViewFragment} into the fragment container
     * and opens the image through the url supplied
     */
    public void loadEditProfileFragment() {


        Bundle args = new Bundle();
        args.putBoolean(AppConstants.Keys.AFTER_LOGIN, false);
        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, EditProfileFragment.class
                                .getName(), args), AppConstants.FragmentTags.EDIT_PROFILE, false,
                null
        );

    }


    @Override
    protected Object getTaskTag() {
        return null;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == GooglePlusManager.CONNECTION_UPDATE_ERROR)
                && (resultCode == RESULT_OK)) {
            mGooglePlusManager.onActivityResult();
        } else if (requestCode == Crop.REQUEST_CROP) {

            ((EditProfileFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.FragmentTags.EDIT_PROFILE)).onActivityResult
                    (requestCode, resultCode,
                            data);
            Logger.d(TAG, "REQUEST CROP");
        }

        else if (requestCode == Crop.REQUEST_PICK) {

            ((EditProfileFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.FragmentTags.EDIT_PROFILE)).onActivityResult
                    (requestCode, resultCode,
                            data);
            Logger.d(TAG, "REQUEST PICK");
        }
    }


    @Override
    protected void onStart() {
        mGooglePlusManager.onActivityStarted();

        super.onStart();

    }

    @Override
    protected void onStop() {
        mGooglePlusManager.onActivityStopped();
        super.onStop();
    }


    /**
     * this calls the google Login method of EditProfileFragment
     */
    @Override
    public void onLogin() {


        EditProfileFragment editProfileFragment = ((EditProfileFragment) getSupportFragmentManager()
                .findFragmentByTag(AppConstants.FragmentTags.EDIT_PROFILE));


        if (editProfileFragment != null && editProfileFragment.isResumed()) {
            editProfileFragment.onGoogleLogin();
        }
    }

    @Override
    public void onLoginError(final Exception error) {

        EditProfileFragment editProfileFragment = ((EditProfileFragment) getSupportFragmentManager()
                .findFragmentByTag(AppConstants.FragmentTags.EDIT_PROFILE));


        if (editProfileFragment != null && editProfileFragment.isResumed()) {
            editProfileFragment.onGoogleLoginError(error);
        }

    }

    @Override
    public void onLogout() {

        final EditProfileFragment fragment = ((EditProfileFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        AppConstants.FragmentTags.EDIT_PROFILE));

        if (fragment != null && fragment.isResumed()) {
            fragment.onGoogleLogout();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;

            }
        }
        return super.onOptionsItemSelected(item);

    }


}
