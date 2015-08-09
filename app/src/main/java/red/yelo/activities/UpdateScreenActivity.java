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
import red.yelo.fragments.UpdateAppFragment;
import red.yelo.utils.AppConstants;

public class UpdateScreenActivity extends AbstractYeloActivity {


    public static final String TAG = "UpdateSkillsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);


        if (savedInstanceState == null) {
            loadUpdateScreen();
        }


    }

    /**
     * Loads the {@link red.yelo.fragments.TagUserFragment} into the fragment container
     */
    public void loadUpdateScreen() {

        Bundle args = new Bundle();

        args.putString(AppConstants.Keys.UPDATE_TEXT,getIntent().getStringExtra(AppConstants.Keys.UPDATE_TEXT));
        args.putBoolean(AppConstants.Keys.MAINTAINENCE,getIntent().getBooleanExtra(AppConstants.Keys.MAINTAINENCE,false));


        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, UpdateAppFragment.class
                                .getName(), args), AppConstants.FragmentTags.UPDATE_APP, false,
                null
        );

    }

    @Override
    protected Object getTaskTag() {
        return null;
    }


}
