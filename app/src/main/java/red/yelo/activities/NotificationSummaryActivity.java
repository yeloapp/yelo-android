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
import red.yelo.fragments.NotificationSummaryFragment;
import red.yelo.utils.AppConstants;

//@ActivityTransition(createEnterAnimation = R.anim.main_activity_launch,
//        createExitAnimation = R.anim.launch_zoom_out,
//        destroyEnterAnimation = R.anim.exit_zoom_in,
//        destroyExitAnimation = R.anim.main_activity_exit)
public class NotificationSummaryActivity extends AbstractYeloActivity {

    public static final String TAG = "NotificationSummaryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);


        if (savedInstanceState == null) {
            loadNotificationSummaryScreen();
        }
    }

    /**
     * Loads the {@link red.yelo.fragments.WallPostFragment} into the fragment container
     */
    public void loadNotificationSummaryScreen() {



        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, NotificationSummaryFragment.class
                                .getName(), null), AppConstants.FragmentTags.NOTIFICATION_SUMMARY, false,
                null
        );

    }

    @Override
    protected Object getTaskTag() {
        return null;
    }


}
