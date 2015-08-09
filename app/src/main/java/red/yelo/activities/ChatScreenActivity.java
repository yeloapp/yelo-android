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
import android.text.TextUtils;

import red.yelo.R;
import red.yelo.fragments.AbstractYeloFragment;
import red.yelo.fragments.ChatDetailsFragment;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.FragmentTags;

/**
 * This activity is alpha release , and has been depreciated (need to rebuild)
 */

public class ChatScreenActivity extends AbstractYeloActivity {

    public static final String TAG = "ChatScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        //initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);

        Intent intent = getIntent();


        // if(getIntent().hasExtra(AppConstants.Keys.FROM_WALL)){

            String chatId = intent.getStringExtra(AppConstants.Keys.CHAT_ID);
            String userId = intent.getStringExtra(AppConstants.Keys.USER_ID);
            String chatTitle = intent.getStringExtra(AppConstants.Keys.CHAT_TITLE);
            String profileImage = intent.getStringExtra(AppConstants.Keys.PROFILE_IMAGE);
            String wallId = intent.getStringExtra(AppConstants.Keys.WALL_ID);


        String tagName = "";
        if(intent.hasExtra(AppConstants.Keys.TAG_NAME)) {
             tagName = intent.getStringExtra(AppConstants.Keys.TAG_NAME);
        }


        String myId = AppConstants.UserInfo.INSTANCE.getId();

            //setTitle(chatTitle);



        if(intent.hasExtra(AppConstants.Keys.CONTACT_NUMBER)){
            String contactNumber = intent.getStringExtra(AppConstants.Keys.CONTACT_NUMBER);
            String servicePrice = intent.getStringExtra(AppConstants.Keys.SERVICE_PRICE);
            String serviceId = intent.getStringExtra(AppConstants.Keys.SERVICE_ID);
            String title = intent.getStringExtra(AppConstants.Keys.TITLE);
            String message = intent.getStringExtra(AppConstants.Keys.MESSAGE);
            String date = intent.getStringExtra(AppConstants.Keys.DATE_TIME);

            loadChatScreen(chatId, userId,myId,profileImage,chatTitle,tagName,contactNumber,servicePrice,title,serviceId,
                    message,date);

        }
        else {
            loadChatScreen(chatId, wallId, userId, myId, profileImage, chatTitle,tagName);
        }
     //this is for chat data collection, we are not doing it right now
       // }
//        else{
//            String chatId = intent.getStringExtra(AppConstants.Keys.CHAT_ID);
//            String userId = intent.getStringExtra(AppConstants.Keys.USER_ID);
//            String chatTitle = intent.getStringExtra(AppConstants.Keys.CHAT_TITLE);
//            String listId = intent.getStringExtra(AppConstants.Keys.LIST_ID);
//            String tagId = intent.getStringExtra(AppConstants.Keys.TAGS);
//            String listUserId = intent.getStringExtra(AppConstants.Keys.LIST_USER_ID);
//            String queryUserId = intent.getStringExtra(AppConstants.Keys.QUERY_USER_ID);
//            String profileImage = intent.getStringExtra(AppConstants.Keys.PROFILE_IMAGE);
//
//            String myId = AppConstants.UserInfo.INSTANCE.getId();
//
//            setActionBarTitle(chatTitle);
//
//            loadChatScreen(chatId, userId, myId, listId, tagId, listUserId, queryUserId, profileImage);
//        }

    }

    /**
     * Loads the {@link red.yelo.fragments.ChatDetailsFragment} into the fragment container
     */
    public void loadChatScreen(String chatId,String wallId, String userId, String myId
                               ,String profileImage,String userName,String tagName) {

        final Bundle args = new Bundle(4);


        args.putString(AppConstants.Keys.CHAT_ID, chatId);
        args.putString(AppConstants.Keys.USER_ID, userId);
        args.putString(AppConstants.Keys.MY_ID, myId);
        args.putString(AppConstants.Keys.WALL_ID,wallId);
        args.putString(AppConstants.Keys.PROFILE_IMAGE,profileImage);
        args.putString(AppConstants.Keys.USER_NAME,userName);
        args.putBoolean(AppConstants.Keys.FROM_NOTIFICATIONS,getIntent().
                getBooleanExtra(AppConstants.Keys.FROM_NOTIFICATIONS, false));
        if(!TextUtils.isEmpty(tagName)) {
            args.putString(AppConstants.Keys.TAG_NAME, tagName);
        }


        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, ChatDetailsFragment.class
                                .getName(), args), FragmentTags.CHAT_DETAILS, false,
                null
        );

    }

    /**
     * Loads the {@link red.yelo.fragments.ChatDetailsFragment} into the fragment container
     */
    public void loadChatScreen(String chatId, String userId, String myId
            ,String profileImage,String userName,String tagName,String contactNumber,String price,
                               String title,String serviceId,String message,String date) {

        final Bundle args = new Bundle(4);


        args.putString(AppConstants.Keys.CHAT_ID, chatId);
        args.putString(AppConstants.Keys.USER_ID, userId);
        args.putString(AppConstants.Keys.MY_ID, myId);
        args.putString(AppConstants.Keys.PROFILE_IMAGE,profileImage);
        args.putString(AppConstants.Keys.USER_NAME,userName);
        args.putString(AppConstants.Keys.CONTACT_NUMBER,contactNumber);
        args.putString(AppConstants.Keys.SERVICE_PRICE,price);
        args.putString(AppConstants.Keys.TITLE,title);
        args.putString(AppConstants.Keys.SERVICE_ID,serviceId);
        args.putString(AppConstants.Keys.MESSAGE,message);
        args.putString(AppConstants.Keys.DATE_TIME,date);



        args.putBoolean(AppConstants.Keys.FROM_NOTIFICATIONS,getIntent().
                getBooleanExtra(AppConstants.Keys.FROM_NOTIFICATIONS, false));
        if(!TextUtils.isEmpty(tagName)) {
            args.putString(AppConstants.Keys.TAG_NAME, tagName);
        }


        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(this, ChatDetailsFragment.class
                                .getName(), args), FragmentTags.CHAT_DETAILS, false,
                null
        );

    }



    @Override
    protected Object getTaskTag() {
        return null;
    }


}
