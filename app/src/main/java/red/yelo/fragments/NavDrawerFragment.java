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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.vinaysshenoy.okulus.OkulusImageView;

import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.activities.UserProfileActivity;
import red.yelo.adapters.NavDrawerAdapter;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.Utils;

/**
 * Fragment to load in the Navigation Drawer
 */
public class NavDrawerFragment extends AbstractYeloFragment implements AdapterView
        .OnItemClickListener {

    private static final String TAG = "NavDrawerFragment";

    /**
     * Intent filter for receiving updates whenever the User Info changes
     */
    private static final IntentFilter INTENT_FILTER                     = new IntentFilter(
            AppConstants.ACTION_USER_INFO_UPDATED);
    /**
     * BroadcastReceiver implementation that receives broadcasts when the user info is updated from
     * server
     */
    private final BroadcastReceiver mUserInfoUpdatedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction()
                                                    .equals(AppConstants
                                                                    .ACTION_USER_INFO_UPDATED)) {
                updateLoggedInStatus();
            }
        }
    };
    /**
     * ListView to provide Nav drawer content
     */
    private ListView mListView;
    /**
     * Drawer Adapter to provide the list view options
     */
    private NavDrawerAdapter         mDrawerAdapter;
    /**
     * Callback will be triggered whenever the Nav drawer takes an action.  Use to close the drawer
     * layout
     */
    private INavDrawerActionCallback mNavDrawerActionCallback;
    /**
     * Callback for delaying the running of nav drawer actions. This is so that the drawer can be
     * closed without jank
     */
    private Handler mHandler;
    /**
     * Header which shows either the user profile or the Sign In message
     */
    private ViewGroup mProfileHeader;

    /**
     * Bitmap transformation for loading images from picasso
     */
    private AvatarBitmapTransformation mAvatarBitmapTransformation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mHandler = new Handler();

        mAvatarBitmapTransformation = new AvatarBitmapTransformation(getActivity());
        mListView = (ListView) inflater.inflate(R.layout.fragment_nav_drawer, container, false);
        mDrawerAdapter = new NavDrawerAdapter(getActivity(), R.array.nav_drawer_primary,
                                              R.array.nav_drawer_secondary);

        mProfileHeader = (ViewGroup) inflater
                .inflate(R.layout.layout_nav_drawer_header, mListView, false);
        initProfileHeaderViews();

        mListView.addHeaderView(mProfileHeader, null, true);
        mListView.setAdapter(mDrawerAdapter);

        mListView.setOnItemClickListener(this);
        return mListView;
    }

    /**
     * Initialize the profile header views. Reads the references to the child views and stores them
     * as tags
     */
    private void initProfileHeaderViews() {

        //Get references to the two primary containers
        final ViewGroup profileContainer = (ViewGroup) mProfileHeader
                .findViewById(R.id.container_profile_info);
        mProfileHeader.setTag(R.id.container_profile_info, profileContainer);

        final ViewGroup signInContainer = (ViewGroup) mProfileHeader
                .findViewById(R.id.container_sign_in_message);
        mProfileHeader.setTag(R.id.container_sign_in_message, signInContainer);

        //Get references to the individual container children and set tags
        profileContainer
                .setTag(R.id.text_user_name, profileContainer.findViewById(R.id.text_user_name));
        profileContainer.setTag(R.id.image_user, profileContainer.findViewById(R.id.image_user));

        TextView textView = (TextView) signInContainer.findViewById(R.id.text_nav_item_title);
        textView.setText(R.string.text_sign_in);
        signInContainer.setTag(R.id.text_nav_item_title, textView);

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(YeloApplication.getStaticContext())
                             .unregisterReceiver(mUserInfoUpdatedBroadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(YeloApplication.getStaticContext())
                             .registerReceiver(mUserInfoUpdatedBroadcastReceiver, INTENT_FILTER);
        updateLoggedInStatus();
    }

    /**
     * Checks whether the user is logged in or not and updates the status accordingly
     */
    private void updateLoggedInStatus() {


        final View profileInfoContainer = (View) mProfileHeader.getTag(R.id.container_profile_info);
        final View signInMessageContainer = (View) mProfileHeader
                .getTag(R.id.container_sign_in_message);


        final TextView userNameTextView = ((TextView) profileInfoContainer
                .getTag(R.id.text_user_name));
        final OkulusImageView profileImageView = (OkulusImageView) profileInfoContainer
                .getTag(R.id.image_user);

        if (isLoggedIn()) {

            profileInfoContainer.setVisibility(View.VISIBLE);
            signInMessageContainer.setVisibility(View.GONE);

            userNameTextView.setText(AppConstants.UserInfo.INSTANCE.getFirstName());

            final String userImageUrl = AppConstants.UserInfo.INSTANCE.getProfilePicture();

            ColorGenerator generator = ColorGenerator.DEFAULT;

            int color = generator.getColor(("A").toUpperCase());
            Resources r = getActivity().getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());

            TextDrawable drawable = TextDrawable.builder()
                    .buildRoundRect(("A").toUpperCase(), color, Math.round(px));

            if (!TextUtils.isEmpty(userImageUrl)) {


                Utils.loadCircularImage(getActivity(),profileImageView,userImageUrl, AvatarBitmapTransformation.AvatarSize.NORMAL,drawable);

            } else {
                Utils.loadCircularImage(getActivity(),profileImageView,"", AvatarBitmapTransformation.AvatarSize.NORMAL,drawable);

            }

        } else {

            profileInfoContainer.setVisibility(View.GONE);
            signInMessageContainer.setVisibility(View.VISIBLE);
            userNameTextView.setText(null);
            profileImageView.setImageResource(0);
        }
    }



    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (parent == mListView) {
            final Runnable launchRunnable = makeRunnableForNavDrawerClick(position);
            if (launchRunnable != null) {
                //Give time for drawer to close before performing the action
                mHandler.postDelayed(launchRunnable, 250);
            }
            if (mNavDrawerActionCallback != null) {
                mNavDrawerActionCallback.onActionTaken();
            }
        }
    }

    /**
     * Creates a {@link Runnable} for positing to the Handler for launching the Navigation Drawer
     * click
     *
     * @param position The nav drawer item that was clicked
     * @return a {@link Runnable} to be posted to the Handler thread
     */
    private Runnable makeRunnableForNavDrawerClick(final int position) {

        Runnable runnable = null;
        final Activity activity = getActivity();

        switch (position) {

            //My Profile
            case 0: {
  /*
                 * If the current Activity is the User profile Activity and the user id loaded is
                  * the current user, don't load it again
                 */
//                if (activity instanceof UserProfileActivity && ((UserProfileActivity) activity)
//                        .getUserId().equals(
//                                AppConstants.UserInfo.INSTANCE.getId())) {
//                    return null;
//                }
//
//                runnable = new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (isLoggedIn()) {
//
//                            launchCurrentUserProfile();
//
//                        } else {
//
//                            final Intent loginIntent = new Intent(getActivity(),
//                                                                  AuthActivity.class);
//                            startActivityForResult(loginIntent, AppConstants.RequestCodes.LOGIN);
//                        }
//
//                    }
//                };
                break;

            }

            //Find Books
            case 1: {

//                if (getActivity() instanceof HomeActivity) {
//                    return null;
//                }
//                runnable = new Runnable() {
//                    @Override
//                    public void run() {
//
//                        final Intent homeActivity = new Intent(getActivity(), HomeActivity.class);
//                        homeActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(homeActivity);
//                    }
//                };

                break;
            }

            //My Chats
            case 2: {

//                GoogleAnalyticsManager
//                        .getInstance()
//                        .sendEvent(new HitBuilders.EventBuilder(AnalyticsConstants.Categories.USAGE,
//                                                                AnalyticsConstants.Actions
//                                                                        .NAVIGATION_OPTION
//                        )
//                                           .set(AnalyticsConstants.ParamKeys.TYPE,
//                                                AnalyticsConstants.ParamValues.CHATS));
//                if (activity instanceof ChatsActivity) {
//                    return null;
//                }
//
//                //TODO Check for login
//                runnable = new Runnable() {
//
//                    @Override
//                    public void run() {
//                        final Intent chatsListIntent = new Intent(getActivity(),
//                                                                  ChatsActivity.class);
//                        chatsListIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(chatsListIntent);
//                    }
//                };
                break;
            }

            //Settings
            case 3: {
//                runnable = new Runnable() {
//                    @Override
//                    public void run() {
//                        final Intent intent = new Intent(getActivity(), SettingsActivity.class);
//                        startActivity(intent);
//                    }
//                };
                break;
            }

            //Share
            case 4: {
//                GoogleAnalyticsManager
//                        .getInstance()
//                        .sendEvent(new HitBuilders.EventBuilder(AnalyticsConstants.Categories.USAGE,
//                                                                AnalyticsConstants.Actions
//                                                                        .NAVIGATION_OPTION
//                        )
//                                           .set(AnalyticsConstants.ParamKeys.TYPE,
//                                                AnalyticsConstants.ParamValues.SHARE));
//                runnable = new Runnable() {
//
//                    @Override
//                    public void run() {
//
//                        Intent shareIntent = Utils
//                                .createAppShareIntent(getActivity());
//                        try {
//                            startActivity(Intent
//                                                  .createChooser(shareIntent,
//                                                          getString(R.string.share_via)));
//                        } catch (ActivityNotFoundException e) {
//                            //Shouldn't happen
//                        }
//
//                    }
//                };

                break;
            }

            //Rate Us
            case 5: {
//                GoogleAnalyticsManager
//                        .getInstance()
//                        .sendEvent(new HitBuilders.EventBuilder(AnalyticsConstants.Categories.USAGE,
//                                                                AnalyticsConstants.Actions
//                                                                        .NAVIGATION_OPTION
//                        )
//                                           .set(AnalyticsConstants.ParamKeys.TYPE,
//                                                AnalyticsConstants.ParamValues.RATE_US));
//
//                runnable = new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Uri marketUri = Uri
//                                .parse(AppConstants.PLAY_STORE_MARKET_LINK);
//                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
//                        startActivity(marketIntent);
//
//                    }
//                };

                break;
            }

            //Send feedback
            case 6: {

                break;
            }


            //About Us
            case 7: {


                break;
            }

            default: {
                runnable = null;
            }
        }

        return runnable;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof INavDrawerActionCallback)) {
            throw new IllegalArgumentException(
                    "Activity " + activity.toString() + " must implement INavDrawerActionCallback");
        }

        mNavDrawerActionCallback = (INavDrawerActionCallback) activity;
    }

    /**
     * Interface that is called when the Navigation Drawer performs an Action
     */
    public static interface INavDrawerActionCallback {
        public void onActionTaken();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        if (requestCode == AppConstants.RequestCodes.LOGIN || requestCode == AppConstants
                .RequestCodes.ONWARD) {

            if (resultCode == Activity.RESULT_OK) {

                boolean defaultFlow = true;
                if (data != null) {

                    final Intent onwardIntent = data.getParcelableExtra(
                            AppConstants.Keys.ONWARD_INTENT);
                    if (onwardIntent != null) {
                        defaultFlow = false;
                        startActivityForResult(onwardIntent, AppConstants.RequestCodes.ONWARD);
                    }
                }

                if (defaultFlow) {
                    launchCurrentUserProfile();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Launches the current user's profile in the Activity
     */
    private void launchCurrentUserProfile() {
        final Intent userProfileIntent = new Intent(getActivity(),
                                                    UserProfileActivity.class);
        userProfileIntent.putExtra(AppConstants.Keys.USER_ID,
                                   AppConstants.UserInfo.INSTANCE.getId());
        userProfileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(userProfileIntent);
    }
}
