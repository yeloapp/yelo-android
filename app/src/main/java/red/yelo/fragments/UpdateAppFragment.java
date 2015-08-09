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
 */package red.yelo.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import red.yelo.R;
import red.yelo.utils.AppConstants;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class UpdateAppFragment extends AbstractYeloFragment implements View.OnClickListener {

    public static final String TAG = "UpdateAppFragment";
    private Button mAppUpdate;
    private String mUpdateText;
    private TextView mUpdateTextView;
    private boolean mIsMaintainence;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_update_app, container, false);

        Bundle extras;
        extras = getArguments();

        if (extras != null) {
            mUpdateText = extras.getString(AppConstants.Keys.UPDATE_TEXT);
            mIsMaintainence = extras.getBoolean(AppConstants.Keys.MAINTAINENCE);

        }

        mAppUpdate = (Button) contentView.findViewById(R.id.update_button);
        mUpdateTextView = (TextView) contentView.findViewById(R.id.update_app_text);
        mUpdateTextView.setText(mUpdateText);


        if (mIsMaintainence) {
            mAppUpdate.setVisibility(View.GONE);
        }
        mAppUpdate.setOnClickListener(this);

        return contentView;

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static UpdateAppFragment newInstance() {
        UpdateAppFragment f = new UpdateAppFragment();
        return f;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.update_button) {
            final String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {

                return true;
            }


            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

}
