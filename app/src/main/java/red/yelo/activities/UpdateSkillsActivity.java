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

import red.yelo.R;
import red.yelo.fragments.AbstractYeloFragment;
import red.yelo.fragments.DemoIntroducingCards;
import red.yelo.utils.AppConstants;

public class UpdateSkillsActivity extends AbstractYeloActivity {


    public static final String TAG = "UpdateSkillsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);


        if (savedInstanceState == null) {
            loadSkillFragment();
        }


    }

    /**
     * Loads the {@link red.yelo.fragments.TagUserFragment} into the fragment container
     */
    public void loadSkillFragment() {


        Intent intent = getIntent();



        Bundle args = new Bundle();

        args.putBoolean(AppConstants.Keys.UPDATE,true);
        args.putString(AppConstants.Keys.LIST_ID,intent.getStringExtra(AppConstants.Keys.LIST_ID));
        args.putString(AppConstants.Keys.USER_ID,intent.getStringExtra(AppConstants.Keys.USER_ID));


        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, DemoIntroducingCards.class
                                .getName(), args), AppConstants.FragmentTags.CREATE_SERVICE, false,
                null
        );

    }


    @Override
    protected Object getTaskTag() {
        return null;
    }


}
