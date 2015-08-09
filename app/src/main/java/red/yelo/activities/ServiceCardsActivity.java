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

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import red.yelo.R;
import red.yelo.fragments.ServiceCardsFragment;
import red.yelo.utils.AppConstants.FragmentTags;


/**
 * @author Sharath Pandeshwar
 * @since 21/03/15
 * <p/>
 * Activity responsible for showling list of service cards.
 */
public class ServiceCardsActivity extends AbstractYeloActivity {

    public static final String TAG = "ServiceCardsActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        if (savedInstanceState == null) {
            loadServiceCardsScreen(getIntent().getExtras());
        }
    }


    public void loadServiceCardsScreen(Bundle args) {
        loadFragment(R.id.frame_content, ServiceCardsFragment.newInstance(args), FragmentTags.SERVICE_CARDS, false, null);
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }
}
