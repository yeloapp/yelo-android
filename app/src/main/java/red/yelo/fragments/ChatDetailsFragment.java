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

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.vinaysshenoy.okulus.OkulusImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import red.yelo.R;
import red.yelo.activities.AbstractYeloActivity;
import red.yelo.activities.ChatScreenActivity;
import red.yelo.activities.HomeActivity;
import red.yelo.activities.UserProfileActivity;
import red.yelo.adapters.ChatDetailAdapter;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.chat.ChatService;
import red.yelo.chat.ChatService.ChatServiceBinder;
import red.yelo.data.DBInterface;
import red.yelo.data.DBInterface.AsyncDbQueryCallback;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableChatMessages;
import red.yelo.data.TableChats;
import red.yelo.data.TableUsers;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.retromodels.request.RatingRequestModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.Keys;
import red.yelo.utils.AppConstants.Loaders;
import red.yelo.utils.AppConstants.QueryTokens;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.Logger;
import red.yelo.utils.Utils;

/**
 * Activity for displaying Chat Messages
 */
public class ChatDetailsFragment extends AbstractYeloFragment implements
        ServiceConnection, LoaderCallbacks<Cursor>, OnClickListener,
        AsyncDbQueryCallback, OnItemClickListener, AdapterView.OnItemLongClickListener,
        RetroCallback.RetroResponseListener, TextView.OnEditorActionListener,
        ChatDetailAdapter.ChatListListener{

    private static final String TAG = "ChatDetailsFragment";

    private ChatDetailAdapter mChatDetailAdapter;

    private ListView mChatListView;

    private EditText mSubmitChatEditText;

    private View mSubmitChatButton;

    private ChatService mChatService;

    private boolean mBoundToChatService;

    private final String mChatSelection = DatabaseColumns.CHAT_ID
            + SQLConstants.EQUALS_ARG;

    private final String mUserSelection = DatabaseColumns.ID
            + SQLConstants.EQUALS_ARG;

    private final String mMessageSelection = BaseColumns._ID
            + SQLConstants.EQUALS_ARG;

    /**
     * The Id of the Chat
     */
    private String mChatId;

    /**
     * Id of the user with whom the current user is chatting
     */
    private String mWithUserId;

    /**
     * Profile image of the user with whom the current user is chatting
     */
    private String mWithUserImage;

    /**
     * Name of the user with whom the current user is chatting
     */
    private String mWithUserName;

    /**
     * User with whom the chat is happening
     */
    private OkulusImageView mWithImageView;

    private SimpleDateFormat mFormatter;

    /**
     * Bundle which contains the user info to load the chats for
     */
    private Bundle mUserInfo;

    private String myId;

    private String mWallId;

    private String mContactNumber,mServicePrice,mServiceTitle,mServiceId,mMessage,mDate;

    private boolean mIsWallIdSent,mSentServiceMessage;

    /**
     * Whether the Activity should be finished on Back press. This will be used in 2 cases
     * <p/>
     * <ol> <li>When the chat screen is opened directly from a user's profile page. In this case,
     * pressing back shouldn't open the Chats list</li> <li> When the chats screen is opened in a
     * multipane layout</li> </ol>
     */
    private boolean mFinishOnBack;


    private String mServerChatId;

    /**
     * whether the activaty is opened from the notifications then we need to handle back navigations
     */
    private boolean mOpenedFromNotification = false;

    private boolean mUserSentMessage, mFirstMessageWithTag,mFromServices;

    private int mPosition = 0;

    private Toolbar mToolbar;

    private OkulusImageView mImageUser;

    private TextView mUserName;

    private LinearLayout mImageNameHolder;

    private final String mChatSelectionForDelete = DatabaseColumns.CHAT_ID
            + SQLConstants.EQUALS_ARG;

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();

    private View mfragmentView;

    private View mRatingDialogView;


    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);

        setHasOptionsMenu(true);

        final View view = inflater
                .inflate(R.layout.fragment_chat_details, container, false);

        mfragmentView = view;


        mFormatter = new SimpleDateFormat(AppConstants.TIMESTAMP_FORMAT, Locale.getDefault());
        mChatListView = (ListView) view.findViewById(R.id.list_chats);
        mChatDetailAdapter = new ChatDetailAdapter(getActivity(), null,this);
        mChatListView.setAdapter(mChatDetailAdapter);
        mChatListView.setOnItemClickListener(this);
        mChatListView.setOnItemLongClickListener(this);
        mSubmitChatEditText = (EditText) view
                .findViewById(R.id.edit_text_chat_message);

        mSubmitChatEditText.setOnClickListener(this);
        mSubmitChatEditText.setOnEditorActionListener(this);

        mSubmitChatButton = view.findViewById(R.id.button_send);
        mSubmitChatButton.setOnClickListener(this);

        mSubmitChatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s != null && !s.equals("") && !TextUtils.isEmpty(s)) {
                    mSubmitChatButton.setEnabled(true);
                } else {
                    mSubmitChatButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        Bundle extras = getArguments();
        if (savedInstanceState == null) {

            mChatId = extras.getString(Keys.CHAT_ID);
            mWithUserId = extras.getString(Keys.USER_ID);
            myId = extras.getString(Keys.MY_ID);
            mWithUserImage = extras.getString(Keys.PROFILE_IMAGE);
            if(extras.containsKey(Keys.CONTACT_NUMBER)){
                mFromServices = true;
                mContactNumber = extras.getString(Keys.CONTACT_NUMBER);
                mServiceId = extras.getString(Keys.SERVICE_ID);
                mServicePrice = extras.getString(Keys.SERVICE_PRICE);
                mServiceTitle = extras.getString(Keys.TITLE);
                mMessage = extras.getString(Keys.MESSAGE);
                mDate = extras.getString(Keys.DATE_TIME);

            }
            if(TextUtils.isEmpty(mWithUserImage)){
                mWithUserImage = "";
            }
            if(mWithUserImage.contains("assets/fallback/")){
                mWithUserImage = "";
            }
            mWithUserName = extras.getString(Keys.USER_NAME);
            if (extras.containsKey(Keys.WALL_ID)) {
                mWallId = extras.getString(Keys.WALL_ID);
            }

            if (extras.containsKey(Keys.TAG_NAME)) {

                mFirstMessageWithTag = true;


            }


            if (extras.containsKey(Keys.FROM_NOTIFICATIONS)) {
                mOpenedFromNotification = extras.getBoolean(Keys.FROM_NOTIFICATIONS);
            } else {
                mFinishOnBack = true;
            }


        } else {
            mUserInfo = savedInstanceState.getBundle(Keys.USER_INFO);
            myId = savedInstanceState.getString(Keys.MY_ID);
            mChatId = savedInstanceState.getString(Keys.CHAT_ID);
            mWithUserId = savedInstanceState.getString(Keys.USER_ID);
            mWithUserImage = savedInstanceState.getString(Keys.PROFILE_IMAGE);
            mWithUserName = savedInstanceState.getString(Keys.USER_NAME);

        }

        mToolbar = (Toolbar) mfragmentView.findViewById(R.id.my_awesome_toolbar);
        ((ChatScreenActivity) getActivity()).setSupportActionBar(mToolbar);
        ((ChatScreenActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ChatScreenActivity) getActivity()).getSupportActionBar().setTitle("");

        ((ChatScreenActivity) getActivity()).getSupportActionBar().
                setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back);


        mImageUser = (OkulusImageView) mToolbar.findViewById(R.id.user_image);
        mUserName = (TextView) mToolbar.findViewById(R.id.user_name);
        mImageNameHolder = (LinearLayout) mToolbar.findViewById(R.id.user_name_image_holder);

        mImageNameHolder.setOnClickListener(this);

        mUserName.setText(mWithUserName);
        Utils.loadCircularImage(getActivity(), mImageUser, mWithUserImage, AvatarBitmapTransformation.AvatarSize.NORMAL);


        loadChatMessages();

        ContentValues values = new ContentValues();
        values.put(DatabaseColumns.UNREAD_COUNT, 0);
        String selection = DatabaseColumns.CHAT_ID + SQLConstants.EQUALS_ARG;
        DBInterface.updateAsync(QueryTokens.UPDATE_CHAT_READ, getTaskTag(), null, TableChats.NAME, values, selection, new String[]{mChatId},
                true, this);



        return view;
    }


    @Override
    public boolean onBackPressed() {

        if (mOpenedFromNotification) {
            getActivity().finish();
            final Intent homeActivity = new Intent(getActivity(),
                    HomeActivity.class);

            startActivity(homeActivity);
            return true;
        } else {
            getActivity().finish();
            return true;
        }

    }

    /**
     * Updates the chat details screen with a new user
     */
    public void updateUserInfo(Bundle userInfo) {
        mUserInfo = userInfo;
        loadChatMessages();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Keys.MY_ID, myId);
        outState.putString(Keys.CHAT_ID, mChatId);
        outState.putString(Keys.USER_ID, mWithUserId);
        outState.putBoolean(Keys.FINISH_ON_BACK, mFinishOnBack);
        outState.putString(Keys.PROFILE_IMAGE, mWithUserImage);
        outState.putString(Keys.USER_NAME, mWithUserName);

    }

    /**
     * Loads the chat messages based on the User info bundle
     */
    private void loadChatMessages() {


        getLoaderManager().restartLoader(Loaders.CHAT_DETAILS, null, this);
        getLoaderManager()
                .restartLoader(Loaders.USER_DETAILS_CHAT_DETAILS, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {

                if (mOpenedFromNotification) {
                    final Intent homeActivity = new Intent(getActivity(),
                            HomeActivity.class);

                    startActivity(homeActivity);
                }
                getActivity().finish();


                return true;
            }

            case R.id.action_block: {


                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());

                // set title
                alertDialogBuilder.setTitle("Confirm");

                // set dialog message
                alertDialogBuilder
                        .setMessage(getResources().getString(R.string.delete_chat_alert_message))
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    final DialogInterface dialog,
                                    final int id) {

                                callDeleteApi(mChatId, mWithUserId);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    final DialogInterface dialog,
                                    final int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                final AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                return true;
            }

            case R.id.action_delete: {


                deleteChat(mChatId);

                return true;
            }

            case R.id.action_call: {


                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mContactNumber));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);

                return true;
            }


            case R.id.action_rate: {

                rateService(mServiceId);

                return true;
            }



            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }


    private void callDeleteApi(String chatId, String userId) {

        final Map<String, String> params = new HashMap<String, String>(1);
        params.put(HttpConstants.CHATTER_ID, userId);
        params.put(HttpConstants.TYPE, AppConstants.ChatAction.BLOCK);
        RetroCallback retroCallback;
        retroCallback = new RetroCallback(this);
        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.DELETE_CHAT);
        retroCallbackList.add(retroCallback);
        mYeloApi.blockUser(params, retroCallback);

        getActivity().setProgressBarIndeterminateVisibility(true);


    }

    private void deleteChat(String chatId) {

        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_CHATS, getTaskTag(), null, TableChats.NAME,
                mChatSelectionForDelete, new String[]{
                        chatId
                }, true, this
        );
        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_CHAT_MESSAGES, getTaskTag(), null,
                TableChatMessages.NAME, mChatSelectionForDelete, new String[]{
                        chatId
                }, true, this
        );

    }


//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//
//        inflater.inflate(R.menu.menu_chat_details, menu);
//        final MenuItem menuItem = menu.findItem(R.id.action_user);
//
//        final View actionView = MenuItemCompat.getActionView(menuItem);
//        if (actionView != null) {
//            mWithImageView = (CircleImageView) actionView
//                    .findViewById(R.id.image_user);
//            mWithImageView.setOnClickListener(this);
//            loadUserInfoIntoActionBar();
//        }
//    }

//    /**
//     * Load the screen with whom the user is chatting
//     */
//    private void loadChattingWithUser() {
//
//        final Intent userProfileIntent = new Intent(getActivity(), UserProfileActivity.class);
//        userProfileIntent.putExtra(Keys.USER_ID, mWithUserId);
//        startActivity(userProfileIntent);
//    }

    @Override
    public void onPause() {
        super.onPause();

        cancelAllCallbacks(retroCallbackList);

        if (mBoundToChatService) {
            mChatService.setCurrentChattingUserId(null);
            getActivity().unbindService(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Bind to chat service
        final Intent chatServiceBindIntent = new Intent(getActivity(), ChatService.class);
        getActivity().bindService(chatServiceBindIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onServiceConnected(final ComponentName name,
                                   final IBinder service) {

        mBoundToChatService = true;
        mChatService = ((ChatServiceBinder) service).getService();
        mChatService.setCurrentChattingUserId(mWithUserId);

        if(mFromServices&&!mSentServiceMessage){
            sendChatMessage(mServiceTitle.toUpperCase()+"\n"+
                    "\n"+mMessage+"\nTentative start date: "+mDate+"\n\n"+
                    "Can you provide me this service? Please confirm!"
                    , mServerChatId);
            mSentServiceMessage = true;
        }
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mBoundToChatService = false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

        if (id == Loaders.CHAT_DETAILS) {
            return new SQLiteLoader(getActivity(), false, TableChatMessages.NAME, null,
                    mChatSelection, new String[]{
                    mChatId
            }, null, null, DatabaseColumns.TIMESTAMP_EPOCH
                    + SQLConstants.ASCENDING, null
            );
        } else if (id == Loaders.USER_DETAILS_CHAT_DETAILS) {
            return new SQLiteLoader(getActivity(), false, TableUsers.NAME, null, mUserSelection,
                    new String[]{
                            mWithUserId
                    }, null, null, null, null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {

        final int id = loader.getId();
        if (id == Loaders.CHAT_DETAILS) {

            Logger.d(TAG, "Cursor loaded with : " + cursor.getCount());

            if ((mChatDetailAdapter.getCount() == 0) && (cursor.getCount() > 0)) {
                //Initial load. Swap cursor AND set position to last
                mChatDetailAdapter.swapCursor(cursor);
                mChatListView.setSelection(mChatDetailAdapter.getCount() - 1);
            } else {
                mChatDetailAdapter.swapCursor(cursor);
                if (mChatDetailAdapter.getCount() > 0) {

                    final int lastAdapterPosition = mChatDetailAdapter
                            .getCount() - 1;

                    /*
                     * Smooth scroll only if there's already some data AND the
                     * last visible position is the last item in the adapter,
                     * i.e, don't scroll if a new message arrives while the user
                     * has scrolled down to view earlier messages
                     */
                    if (mUserSentMessage) {
                        mChatListView.smoothScrollToPosition(lastAdapterPosition);
                    }

                    mUserSentMessage = false;
                }
            }

            if (cursor.moveToLast()) {
                mServerChatId = cursor.getString(cursor.getColumnIndex(DatabaseColumns.SERVER_CHAT_ID));
            }
        } else if (id == Loaders.USER_DETAILS_CHAT_DETAILS) {
            if (cursor.moveToFirst()) {
//                mWithUserImage = cursor
//                        .getString(cursor
//                                           .getColumnIndex(DatabaseColumns.PROFILE_PICTURE));

                final String firstName = cursor
                        .getString(cursor.getColumnIndex(DatabaseColumns.USER_NAME));

                mWithUserName = firstName;
                // loadUserInfoIntoActionBar();

            }
        }
    }

//    /**
//     * Loads the user image into the Action Bar profile pic
//     */
//    private void loadUserInfoIntoActionBar() {
//
//        if (mWithImageView != null) {
//
//            if (!TextUtils.isEmpty(mWithUserImage)) {
//                Picasso.with(getActivity())
//                       .load(mWithUserImage)
//                       .error(R.drawable.pic_avatar)
//                       .resizeDimen(R.dimen.ab_user_image_size, R.dimen.ab_user_image_size)
//                       .centerCrop().into(mWithImageView.getTarget());
//            }
//        }
//
//        if (!TextUtils.isEmpty(mWithUserName)) {
//            setActionBarTitle(mWithUserName);
//        }
//    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {

        if (loader.getId() == Loaders.CHAT_DETAILS) {
            mChatDetailAdapter.swapCursor(null);
        }
    }

    @Override
    public void onClick(final View v) {

        final int id = v.getId();

        if (id == R.id.button_send) {

            String message = "";
            if (mFirstMessageWithTag&&!mFromServices) {
                message = "Message originated from your #" + getArguments().getString(Keys.TAG_NAME) + " post.\n\n" +
                        mSubmitChatEditText.getText().toString();
                mFirstMessageWithTag = false;
            } else {
                message = mSubmitChatEditText.getText().toString();
                mFirstMessageWithTag = false;
            }
            if (sendChatMessage(message, mServerChatId)) {
                mUserSentMessage = true;
                mSubmitChatEditText.setText(null);
            } else {
            }
        } else if (id == R.id.image_user) {
            loadChattingWithUser(mWithUserId, mWithUserName);
        } else if (id == R.id.edit_text_chat_message) {

            mChatListView.smoothScrollToPosition(mChatDetailAdapter
                    .getCount() - 1);
        } else if (id == R.id.user_name_image_holder) {
            loadChattingWithUser(mWithUserId, mWithUserName);
        }
    }

    /**
     * Load the screen with whom the user is chatting
     */
    private void loadChattingWithUser(String userId, String name) {
        final Intent userProfileIntent = new Intent(getActivity(),
                UserProfileActivity.class);

        userProfileIntent.putExtra(AppConstants.Keys.USER_ID, userId);
        userProfileIntent.putExtra(AppConstants.Keys.USER_NAME, name);
        userProfileIntent.putExtra(AppConstants.Keys.SERVICE_SCREEN_TYPE, AppConstants.ServiceScreenType.PROFILE);
        startActivity(userProfileIntent);

    }


    /**
     * Send a chat message to the user the current user is chatting with
     *
     * @param message The message to send.
     * @return <code>true</code> If the message was sent, <code>false</code> otherwise
     */
    private boolean sendChatMessage(String message, String serverChatId) {

        boolean sent = true;
        if (!TextUtils.isEmpty(message)) {
            if (mBoundToChatService) {
                final String sentAt = mFormatter.format(new Date());


                mChatService.sendMessageToUser(myId, mWithUserId, mWallId, mIsWallIdSent, serverChatId, message, sentAt);
                MixpanelAnalytics.getInstance().onChatMessageSent();

                mIsWallIdSent = true;


            } else {
                sent = false;
            }
        }
        return sent;

    }

    @Override
    public void onInsertComplete(int token, Object cookie, long insertRowId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteComplete(int token, Object cookie, int deleteCount) {

        if (token == QueryTokens.DELETE_CHATS) {
            //Do nothing for now
            getActivity().finish();
        }
    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int updateCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        if (parent.getId() == R.id.list_chats) {

            final boolean resendOnClick = (Boolean) view
                    .getTag(R.string.tag_resend_on_click);

            if (resendOnClick) {
                final Cursor cursor = (Cursor) mChatDetailAdapter
                        .getItem(position);

                final int dbRowId = cursor.getInt(cursor
                        .getColumnIndex(BaseColumns._ID));
                final String message = cursor.getString(cursor
                        .getColumnIndex(
                                DatabaseColumns.MESSAGE));
                view.setTag(R.string.tag_resend_on_click, false);

                if (sendChatMessage(message, mServerChatId)) {
                    //Delete the older message from the table
                    DBInterface.deleteAsync(QueryTokens.DELETE_CHAT_MESSAGE, getTaskTag(), null,
                            TableChatMessages.NAME, mMessageSelection, new String[]{
                                    String.valueOf(dbRowId)
                            }, true, this
                    );
                } else {
                    view
                            .setTag(R.string.tag_resend_on_click);
                    Toast.makeText(getActivity(), "error", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if(mFromServices){
            inflater.inflate(R.menu.services_chat_menu, menu);

        }
        else {
            inflater.inflate(R.menu.chat_menu, menu);
        }

        mImageUser = (OkulusImageView) mToolbar.findViewById(R.id.user_image);
        mUserName = (TextView) mToolbar.findViewById(R.id.user_name);
        mImageNameHolder = (LinearLayout) mToolbar.findViewById(R.id.user_name_image_holder);

        mImageNameHolder.setOnClickListener(this);

        mUserName.setText(mWithUserName);

        if (!TextUtils.isEmpty(mWithUserName)) {
            ColorGenerator generator = ColorGenerator.DEFAULT;

            int colorText = generator.getColor((mWithUserName.charAt(0) + "").toUpperCase());
            Resources r = getActivity().getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());


            TextDrawable drawable = TextDrawable.builder()
                    .buildRoundRect((mWithUserName.charAt(0) + "").toUpperCase(), colorText, Math.round(px));


            Utils.loadCircularImage(getActivity(), mImageUser, mWithUserImage, AvatarBitmapTransformation.AvatarSize.NORMAL, drawable);

        }

//        final MenuItem menuItem = menu.findItem(R.id.action_user);
//        final View actionView = menuItem.getActionView();
//
//        if (actionView != null) {
//            mWithImageView = (OkulusImageView) actionView
//                    .findViewById(R.id.image_user);
//            mWithImageView.setOnClickListener(this);
//            loadUserInfoIntoActionBar();
//        }
    }


    /**
     * Loads the user image into the Action Bar profile pic
     */
    private void loadUserInfoIntoActionBar() {


        if (!TextUtils.isEmpty(mWithUserImage)) {

            Utils.loadCircularImage(getActivity(), mWithImageView, mWithUserImage, AvatarBitmapTransformation.AvatarSize.NORMAL);
        }


    }

    /**
     * Updates the home icon of the Action Bar with the image provided
     */
    private void updateActionBarHomeIcon(Bitmap resource) {

        if (isAttached()) {
            final AbstractYeloActivity activity = (AbstractYeloActivity) getActivity();

            if (activity.getActionBar() != null) {

                activity.getActionBar().setIcon(new BitmapDrawable(getResources(), resource));
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        Cursor cursor = ((Cursor) mChatDetailAdapter.getItem(position));
        final int chatStatus = cursor.getInt(cursor
                .getColumnIndex(DatabaseColumns.CHAT_STATUS));

        mPosition = position;
        showChatOptions(chatStatus);


        return true;
    }

    /**
     * Show dialog for chat options
     */
    private void showChatOptions(int chatStatus) {

        if (chatStatus == AppConstants.ChatStatus.SENT||chatStatus == AppConstants.ChatStatus.SENDING) {
            new MaterialDialog.Builder(getActivity())
                    .items(getResources().getStringArray(R.array.chat_detail_longclick_choices))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (which == 0) {
                                //resend
                                final Cursor cursor = (Cursor) mChatDetailAdapter
                                        .getItem(mPosition);

                                final int dbRowId = cursor.getInt(cursor
                                        .getColumnIndex(BaseColumns._ID));
                                final String message = cursor.getString(cursor
                                        .getColumnIndex(
                                                DatabaseColumns.MESSAGE));

                                if (sendChatMessage(message, mServerChatId)) {
                                    //Delete the older message from the table
                                    DBInterface.deleteAsync(QueryTokens.DELETE_CHAT_MESSAGE, getTaskTag(), null,
                                            TableChatMessages.NAME, mMessageSelection, new String[]{
                                                    String.valueOf(dbRowId)
                                            }, true, ChatDetailsFragment.this
                                    );
                                } else {
                                    Toast.makeText(getActivity(), "error", Toast.LENGTH_LONG).show();
                                }
                            } else if (which == 1) {
                                final Cursor cursor = (Cursor) mChatDetailAdapter
                                        .getItem(mPosition);

                                final String message = cursor.getString(cursor
                                        .getColumnIndex(
                                                DatabaseColumns.MESSAGE));

                                copyToClipBoard(message);
                                Toast.makeText(getActivity(), "message copied", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .show();

        } else if (chatStatus == AppConstants.ChatStatus.RECEIVED) {
            new MaterialDialog.Builder(getActivity())
                    .items(getResources().getStringArray(R.array.chat_detail_longclick_choices_for_received))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (which == 0) {
                                final Cursor cursor = (Cursor) mChatDetailAdapter
                                        .getItem(mPosition);

                                final String message = cursor.getString(cursor
                                        .getColumnIndex(
                                                DatabaseColumns.MESSAGE));

                                copyToClipBoard(message);
                                Toast.makeText(getActivity(), "message copied", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .show();
        }
    }


    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.DELETE_CHAT: {
                Toast.makeText(getActivity(), getResources().getString(R.string.block_user_message), Toast.LENGTH_SHORT).show();
                blockChat(mChatId);
                //deleteChat(mDeleteChatId);
                break;
            }
            default: {
                break;
            }
        }

    }

    private void blockChat(String chatId) {

        ContentValues values = new ContentValues();

        values.put(DatabaseColumns.CHAT_TYPE, AppConstants.ChatType.BLOCK);

        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_CHATS, getTaskTag(), null,
                TableChats.NAME, values, mChatSelectionForDelete, new String[]{chatId}, true, this);

    }

    @Override
    public void failure(int requestId, int errorCode, String message) {

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            String message = "";
            if (mFirstMessageWithTag) {
                message = "Message originated from your #" + getArguments().getString(Keys.TAG_NAME) + " post.\n\n" +
                        mSubmitChatEditText.getText().toString();
                mFirstMessageWithTag = false;
            } else {
                message = mSubmitChatEditText.getText().toString();
                mFirstMessageWithTag = false;
            }
            if (sendChatMessage(message, mServerChatId)) {
                mUserSentMessage = true;
                mSubmitChatEditText.setText(null);
            } else {
            }
        }
        return true;
    }

    @Override
    public void onChatItemClick(View view, int position) {
        final boolean resendOnClick = (Boolean) view
                .getTag(R.string.tag_resend_on_click);

        if (resendOnClick) {
            final Cursor cursor = (Cursor) mChatDetailAdapter
                    .getItem(position);

            final int dbRowId = cursor.getInt(cursor
                    .getColumnIndex(BaseColumns._ID));
            final String message = cursor.getString(cursor
                    .getColumnIndex(
                            DatabaseColumns.MESSAGE));
            view.setTag(R.string.tag_resend_on_click, false);

            if (sendChatMessage(message, mServerChatId)) {
                //Delete the older message from the table
                DBInterface.deleteAsync(QueryTokens.DELETE_CHAT_MESSAGE, getTaskTag(), null,
                        TableChatMessages.NAME, mMessageSelection, new String[]{
                                String.valueOf(dbRowId)
                        }, true, this
                );
            } else {
                view
                        .setTag(R.string.tag_resend_on_click);
                Toast.makeText(getActivity(), "error", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onChatItemLongClick(View view, int position) {
        Cursor cursor = ((Cursor) mChatDetailAdapter.getItem(position));
        final int chatStatus = cursor.getInt(cursor
                .getColumnIndex(DatabaseColumns.CHAT_STATUS));

        mPosition = position;
        showChatOptions(chatStatus);


    }

    private void rateService(final String serviceId) {


        boolean wrapInScrollView = true;

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mRatingDialogView = inflater
                .inflate(R.layout.dialog_rating, null);

        final TextView comment = (TextView) mRatingDialogView.findViewById(R.id.comment_edit);
        final RatingBar rating = (RatingBar) mRatingDialogView.findViewById(R.id.ratingBar);
        new MaterialDialog.Builder(getActivity())
                .title("Rate Service")
                .customView(mRatingDialogView, wrapInScrollView)
                .positiveText("Send")
                .positiveColor(getResources().getColor(R.color.blue_link))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        RatingRequestModel ratingRequestModel = new RatingRequestModel();

                        red.yelo.retromodels.request.Rating ratingValue = new red.yelo.retromodels.request.Rating();

                        ratingValue.setComment(comment.getText().toString());
                        ratingValue.setStars(rating.getRating() + "");

                        ratingRequestModel.setRating(ratingValue);
                        ratingRequestModel.setServiceCardId(serviceId);

                        RetroCallback retroCallback;
                        retroCallback = new RetroCallback(ChatDetailsFragment.this);
                        retroCallback.setRequestId(HttpConstants.ApiResponseCodes.RATE_SERVICE);
                        retroCallbackList.add(retroCallback);

                        mYeloApi.rate(ratingRequestModel, retroCallback);



                    }


                })
                .build()
                .show();
    }



}
