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

import red.yelo.R;
import red.yelo.fragments.ExpandedServiceCardFragment;
import red.yelo.utils.AppConstants.FragmentTags;


/**
 * @author Anshul Kamboj
 * @since 03/04/15
 * <p/>
 * Activity responsible for showing the expanded view of the card
 */
public class ServiceCardExpandedActivity extends AbstractYeloActivity {

    public static final String TAG = "ServiceCardExpandedActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        if (savedInstanceState == null) {
            loadServiceCardScreen(getIntent().getExtras());
        }
    }


    public void loadServiceCardScreen(Bundle args) {
        loadFragment(R.id.frame_content, ExpandedServiceCardFragment.newInstance(args),
                FragmentTags.EXPANDED_SERVICE_CARD, false, null);
    }


    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ExpandedServiceCardFragment.EDIT_SERVICE_CARD) {
            finish();
        }
    }
}
