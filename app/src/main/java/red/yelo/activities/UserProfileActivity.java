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

import android.os.Bundle;
import android.support.v4.app.Fragment;

import red.yelo.R;
import red.yelo.fragments.AbstractYeloFragment;
import red.yelo.fragments.UserProfileFragment;
import red.yelo.fragments.UserProfilePagerFragment;
import red.yelo.utils.AppConstants;

public class UserProfileActivity extends AbstractYeloActivity {


    public static final String TAG = "UserProfileActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);


        if(getIntent().getStringExtra(AppConstants.Keys.USER_NAME)!=null) {
            setTitle("");
        }

        if (savedInstanceState == null) {
            loadUserProfileFragment(getIntent().getExtras());
        }
    }


    /**
     * Loads the {@link red.yelo.fragments.ImageViewFragment} into the fragment container
     * and opens the image through the url supplied
     */
    public void loadUserProfileFragment(Bundle bundle) {


        //Bundle args = new Bundle();
        //args.putString(AppConstants.Keys.USER_ID, userid);


        /* Changed to point to UserProfilePagerFragment by Sharath Pandeshwar on 21/03/2015 to pass on all extras to Pager Fragment */
        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, UserProfilePagerFragment.class
                                .getName(), bundle), AppConstants.FragmentTags.PROFILE, false,
                null
        );

    }


    @Override
    protected Object getTaskTag() {
        return null;
    }


}
