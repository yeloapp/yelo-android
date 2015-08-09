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

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import red.yelo.R;
import red.yelo.fragments.SettingsFragment;
import red.yelo.utils.AppConstants;

/**
 * Created by vinaysshenoy on 10/11/14.
 */
public class SettingsActivity extends AbstractYeloActivity {

    private static final String TAG = "SettingsActivity";
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);

        setToolbar(mToolbar);
        if(savedInstanceState == null) {
            loadSettingsFragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Loads the Settings fragment into the View */
    private void loadSettingsFragment() {

        final FragmentManager fragmentManager = getFragmentManager();
        final SettingsFragment settingsFragment = (SettingsFragment) Fragment.instantiate(this, SettingsFragment.class.getName());
        final android.app.FragmentTransaction transaction = fragmentManager
                .beginTransaction();

        transaction.replace(R.id.frame_content, settingsFragment, AppConstants.FragmentTags.SETTINGS);
        transaction.commit();
    }

    @Override
    protected Object getTaskTag() {
        return TAG;
    }
}
