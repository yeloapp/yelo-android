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

import android.os.Bundle;
import android.support.v4.app.Fragment;

import red.yelo.R;
import red.yelo.fragments.AbstractYeloFragment;
import red.yelo.fragments.ChatsFragment;
import red.yelo.utils.AppConstants;

/**
 * This activity is alpha release , and has been depreciated (needs to rebuild)
 */
public class ChatsActivity extends AbstractYeloActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        //initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);


        if (savedInstanceState == null) {
            loadChatsFragment();
        }
    }

    /** Load the fragment for login */
    private void loadChatsFragment() {

        Bundle args = new Bundle();
        args.putBoolean(AppConstants.Keys.FROM_NOTIFICATIONS, true);
        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, ChatsFragment.class.getName(), getIntent().getExtras()),
                AppConstants.FragmentTags.CHATS, false, null
        );
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


//    @Override
//    protected boolean isDrawerActionBarToggleEnabled() {
//        return false;
//    }


}
