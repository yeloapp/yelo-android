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
import android.view.View;

import red.yelo.R;
import red.yelo.bus.TagUserEvent;
import red.yelo.fragments.AbstractYeloFragment;
import red.yelo.fragments.WallPostFragment;
import red.yelo.utils.AppConstants;

public class WallPostActivity extends AbstractYeloActivity {

    public static final String TAG = "WallPostActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        // initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);


        if (savedInstanceState == null) {
            loadWallPostScreen();
        }

    }

    /**
     * Loads the {@link red.yelo.fragments.WallPostFragment} into the fragment container
     */
    public void loadWallPostScreen() {


        Intent intent = getIntent();

        Bundle args = new Bundle();

        args.putString(AppConstants.Keys.ID, intent.getStringExtra(AppConstants.Keys.ID));
        args.putString(AppConstants.Keys.NOTIFICATION_ID, intent.getStringExtra(AppConstants.Keys.NOTIFICATION_ID));
        args.putBoolean(AppConstants.Keys.FROM_PROFILE, intent.getBooleanExtra(AppConstants.Keys.FROM_PROFILE, false));
        if (intent.hasExtra(AppConstants.Keys.FROM_TAG)) {
            args.putBoolean(AppConstants.Keys.FROM_TAG, intent.getBooleanExtra(AppConstants.Keys.FROM_TAG, false));
        }
        if (intent.hasExtra(AppConstants.Keys.FROM_NOTIFICATIONS)) {
            args.putBoolean(AppConstants.Keys.FROM_NOTIFICATIONS, intent.getBooleanExtra(AppConstants.Keys.FROM_NOTIFICATIONS, false));
        }

        if(intent.hasExtra(AppConstants.Keys.COMMENT)){
            args.putBoolean(AppConstants.Keys.COMMENT, true);

        }


        /**
         * TODO: Uncomment the below
         */

        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, WallPostFragment.class
                                .getName(), args), AppConstants.FragmentTags.WALL_POST, false,
                null
        );



    }

    @Override
    protected Object getTaskTag() {
        return null;
    }


    public void fabClicked(View view) {
        mBus.post(new TagUserEvent(true));
    }


}
