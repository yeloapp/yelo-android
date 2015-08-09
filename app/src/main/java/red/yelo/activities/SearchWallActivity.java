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
import red.yelo.fragments.SearchWallFragment;
import red.yelo.utils.AppConstants;

public class SearchWallActivity extends AbstractYeloActivity {


    public static final String TAG = "SearchWallActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);


                getSupportActionBar().setTitle(R.string.select_your_posts);

        if (savedInstanceState == null) {
            loadSearchWallScreen();
        }
    }


    /**
     * Loads the {@link red.yelo.fragments.SearchWallFragment} into the fragment container
     * this selects the location and throws location object to the bus(which is handled by @Subscribe
     * methods
     */
    public void loadSearchWallScreen() {



        Bundle args=new Bundle();
        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, SearchWallFragment.class
                                .getName(), args), AppConstants.FragmentTags.SEARCH, false,
                null
        );

    }




    @Override
    protected Object getTaskTag() {
        return null;
    }




}
