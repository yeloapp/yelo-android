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

import com.soundcloud.android.crop.Crop;

import red.yelo.R;
import red.yelo.fragments.AbstractYeloFragment;
import red.yelo.fragments.CreateEditWallPostFragment;
import red.yelo.fragments.EditWallPostFragment;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;


public class EditWallPostActivity extends AbstractYeloActivity {

    public static final String TAG = "EditWallPostActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        // initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);


        if (savedInstanceState == null) {
            loadEditWallPostFragment();
        }
    }


    /**
     * Loads the {@link red.yelo.fragments.WallPostFragment} into the fragment container
     */
    public void loadEditWallPostFragment() {


        Intent intent = getIntent();

        Bundle args = new Bundle();

        if (intent.hasExtra(AppConstants.Keys.EDIT_POST)) {
            args.putBoolean(AppConstants.Keys.EDIT_POST, intent.getBooleanExtra(AppConstants.Keys.EDIT_POST, false));
        }
        args.putString(AppConstants.Keys.ID, intent.getStringExtra(AppConstants.Keys.ID));
        args.putString(AppConstants.Keys.TAG_ID, intent.getStringExtra(AppConstants.Keys.TAG_ID));


        /*  Changed by Sharath Pandeshwar on 04/03/2014 */

        /* loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, EditWallPostFragment.class
                                .getName(), args), AppConstants.FragmentTags.POST_ON_WALL, false,
                null
        ); */

        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment.instantiate(this, CreateEditWallPostFragment.class.getName(), args), AppConstants.FragmentTags.POST_ON_WALL, false, null);

        /* End of modification done by Sharath Pandeshwar on 04/03/2014 */
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*  Changed by Sharath Pandeshwar on 05/03/2014 */

        //Changed to CreateEditWallPostFragment
        if (requestCode == Crop.REQUEST_PICK) {
            ((CreateEditWallPostFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.FragmentTags.POST_ON_WALL)).onActivityResult(requestCode, resultCode, data);
            Logger.d(TAG, "REQUEST PICK");
        } else if (requestCode == Crop.REQUEST_CROP) {
            ((CreateEditWallPostFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.FragmentTags.POST_ON_WALL)).onActivityResult(requestCode, resultCode, data);
            Logger.d(TAG, "REQUEST CROP");
        }

        /*  End of modification by Sharath Pandeshwar on 05/03/2014 */
    }


    @Override
    protected Object getTaskTag() {
        return null;
    }


}
