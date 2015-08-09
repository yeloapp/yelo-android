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
import red.yelo.fragments.AskFriendFragment;
import red.yelo.fragments.RewardsFragment;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.FragmentTags;


/**
 * @author Sharath Pandeshwar
 * @since 15/07/14
 * <p/>
 * Activity responsible for handling Rewards for users of Yelo
 */
public class RewardsActivity extends AbstractYeloActivity {

    public static final String TAG = "RewardsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        if (savedInstanceState == null) {
            loadRewardsScreen(getIntent().getExtras());
        }
    }


    public void loadRewardsScreen(Bundle args) {
        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment.instantiate(this, RewardsFragment.class.getName(), args), FragmentTags.REWARDS, false, null);
    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


}
