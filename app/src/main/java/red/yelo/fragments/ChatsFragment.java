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
 */

package red.yelo.fragments;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;
import red.yelo.activities.ChatScreenActivity;
import red.yelo.activities.HomeActivity;
import red.yelo.adapters.ChatsAdapter;
import red.yelo.chat.ChatService;
import red.yelo.chat.ChatService.ChatServiceBinder;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.SQLiteLoader;
import red.yelo.data.TableChatMessages;
import red.yelo.data.TableChats;
import red.yelo.data.ViewChatsWithMessagesAndUsers;
import red.yelo.fragments.dialogs.SingleChoiceDialogFragment;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.FragmentTags;
import red.yelo.utils.AppConstants.Keys;
import red.yelo.utils.AppConstants.Loaders;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;


/**
 * Created by anshul1235 on 15/07/14.
 */
public class ChatsFragment extends AbstractYeloFragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, ServiceConnection,
        DBInterface.AsyncDbQueryCallback, RetroCallback.RetroResponseListener {

    private static final String TAG = "ChatsFragment";

    private ChatsAdapter mChatsAdapter;
    private ListView mChatsListView;
    private ChatService mChatService;

    private boolean mBoundToChatService;
    private String mDeleteChatId, mDeleteUserId;

    private final String mChatSelectionForDelete = DatabaseColumns.CHAT_ID
            + SQLConstants.EQUALS_ARG;

    /**
     * list of callbacks to keep a record for cancelling in onPause
     */
    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();


    /**
     * cursor maintains the reference to all the values of the list which we will use in
     * ChatPagerFragment
     */
    private Cursor mCursor;
    private TextView mEmptyViewText;


    /**
     * Reference to the Dialog Fragment for selecting the chat options
     */
    private SingleChoiceDialogFragment mChatDialogFragment;


    private ArrayList<String> mTagId = new ArrayList<String>();

    private boolean mChatsPresent;

    private boolean mOpenedFromNotification = false;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(false);
        final View view = inflater
                .inflate(R.layout.fragment_chats, container, false);
        mChatsListView = (ListView) view.findViewById(R.id.list_chats);
        mChatsAdapter = new ChatsAdapter(getActivity(), null);
        mChatsListView.setOnItemClickListener(this);
        mChatsListView.setOnItemLongClickListener(this);

        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.padding_list, null);
        mChatsListView.addHeaderView(headerView);
        mChatsListView.setEmptyView(headerView);
        mChatsListView.setAdapter(mChatsAdapter);


        View emptyView = view.findViewById(R.id.empty_view);


        // if(!mChatsPresent)
        if (SharedPreferenceHelper.getInt(R.string.pref_empty_screen_check, 0) == 0) {
            mChatsListView.setEmptyView(emptyView);
        }

        mEmptyViewText = (TextView) emptyView.findViewById(R.id.empty_view_text);

        final Bundle args = getArguments();
        if (args != null) {
            mTagId = args.getStringArrayList(AppConstants.Keys.TAG_LIST);
            mOpenedFromNotification = args.getBoolean(Keys.FROM_NOTIFICATIONS);
        }
        loadChats();
        return view;
    }

    private void loadChats() {
        getLoaderManager().restartLoader(Loaders.ALL_CHATS, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAllCallbacks(retroCallbackList);
        if (mBoundToChatService) {
            //mChatService.setChatScreenVisible(true);
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
    public boolean onBackPressed() {
        getActivity().finish();
        final Intent homeActivity = new Intent(getActivity(),
                HomeActivity.class);
        startActivity(homeActivity);
        getActivity().finish();
        return true;

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }


    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

        if (id == Loaders.ALL_CHATS) {
            return new SQLiteLoader(getActivity(), true, ViewChatsWithMessagesAndUsers.NAME, null,
                    null, null, null, null, DatabaseColumns.TIMESTAMP_EPOCH
                    + SQLConstants.DESCENDING, null
            );

        }
        return null;
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        if (loader.getId() == Loaders.ALL_CHATS) {

            mCursor = cursor;
            Logger.d(TAG, "cursor loaded with " + cursor.getCount());
            mChatsAdapter.swapCursor(cursor);
            if (cursor.getCount() > 0) {
                SharedPreferenceHelper.set(R.string.pref_empty_screen_check, 1);
            } else {
                SharedPreferenceHelper.set(R.string.pref_empty_screen_check, 0);
            }
        }

    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {

        if (loader.getId() == Loaders.ALL_CHATS) {
            mChatsAdapter.swapCursor(null);
        }
    }

    public void onItemClick(final AdapterView<?> parent, final View view,
                            final int position, final long id) {

        if (parent.getId() == R.id.list_chats) {

            String userId, chatId, chatTitle, userProfile;
            userId = mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.ID));
            chatId = mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.CHAT_ID));
            chatTitle = mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.USER_NAME));
            userProfile = mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.USER_IMAGE));

            loadChat(userId, chatId, chatTitle, userProfile);

        }
    }


    /**
     * Loads the actual chat screen. This is used in the case where the user taps on an item in the
     * list of chats
     */


    private void loadChat(String userId, String chatId, String chatTitle
            , String image) {

        final Intent chatScreenActivity = new Intent(getActivity(),
                ChatScreenActivity.class);

        chatScreenActivity.putExtra(Keys.USER_ID, userId);
        chatScreenActivity.putExtra(Keys.CHAT_ID, chatId);
        chatScreenActivity.putExtra(Keys.CHAT_TITLE, chatTitle);
        chatScreenActivity.putExtra(Keys.PROFILE_IMAGE, image);

        startActivity(chatScreenActivity);

    }

    @Override
    public void onServiceConnected(final ComponentName name,
                                   final IBinder service) {

        mBoundToChatService = true;
        mChatService = ((ChatServiceBinder) service).getService();
        mChatService.clearChatNotifications();
        //mChatService.setChatScreenVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                getActivity().finish();
                final Intent homeActivity = new Intent(getActivity(),
                        HomeActivity.class);

                startActivity(homeActivity);
                getActivity().finish();

                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Loads the {@link red.yelo.fragments.HomeScreenFragment} into the fragment container
     */
    public void loadHomeScreen() {
        loadFragment(R.id.frame_content, (AbstractYeloFragment) Fragment
                        .instantiate(getActivity(), HomeScreenFragment.class
                                .getName(), null), FragmentTags.HOME_SCREEN, false,
                null
        );

    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mBoundToChatService = false;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                                   long id) {

        mDeleteChatId = mCursor.getString(mCursor
                .getColumnIndex(DatabaseColumns.CHAT_ID));

        mDeleteUserId = mCursor.getString(mCursor
                .getColumnIndex(DatabaseColumns.ID));

        showChatOptions();

        return true;
    }


    /**
     * Show dialog for chat options
     */
    private void showChatOptions() {

        new MaterialDialog.Builder(getActivity())
                .items(getResources().getStringArray(R.array.chat_longclick_choices))
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0) {


                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    getActivity());

                            // set title
                            alertDialogBuilder.setTitle("Confirm");

                            // set dialog message
                            alertDialogBuilder
                                    .setMessage(getResources().getString(R.string.block_chat_alert_message))
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                final DialogInterface dialog,
                                                final int id) {

                                            callDeleteApi(mDeleteChatId, mDeleteUserId);
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


                        } else {


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

                                            deleteChat(mDeleteChatId);
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


                        }
                    }
                })
                .show();


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


    }

//    @Override
//    public void success(Object o, Response response) {
//
//    }


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

    private void blockChat(String chatId) {

        ContentValues values = new ContentValues();

        values.put(DatabaseColumns.CHAT_TYPE, AppConstants.ChatType.BLOCK);

        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_CHATS, getTaskTag(), null,
                TableChats.NAME, values, mChatSelectionForDelete, new String[]{chatId}, true, this);

    }


    @Override
    public void onInsertComplete(int token, Object cookie, long insertRowId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }


    @Override
    public void onUpdateComplete(int token, Object cookie, int updateCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        // TODO Auto-generated method stub

    }

    public static ChatsFragment newInstance(Bundle args) {
        ChatsFragment chats = new ChatsFragment();
        chats.setArguments(args);
        return chats;
    }

    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {
            case HttpConstants.ApiResponseCodes.DELETE_CHAT: {
                Toast.makeText(getActivity(), getResources().getString(R.string.block_user_message), Toast.LENGTH_SHORT).show();
                blockChat(mDeleteChatId);
                //deleteChat(mDeleteChatId);
                break;
            }
            default: {
                break;
            }
        }

    }

    @Override
    public void failure(int requestId, int errorCode, String message) {

    }

}

//message{"message":"You cant chat with this user","receiver_id":"543c23e679656c38b9000000","reply_id":"54f56de972616970cb330000","sender_id":"543b93fc79656c3997000000","sent_at":"2015-03-03 14:14:02 +0530","format":"json","controller":"api/v1/chats","action":"send_chat","status":false,"server_sent_at":"2015-03-03 08:44:03 +0000"}
//        03-03 14:14:03.225    3607-3607/red.yelo D/ChatService﹕ [1] ChatService.onReceiveMessage: Received:{"message":"You cant chat with this user","receiver_id":"543c23e679656c38b9000000","reply_id":"54f56de972616970cb330000","sender_id":"543b93fc79656c3997000000","sent_at":"2015-03-03 14:14:02 +0530","format":"json","controller":"api/v1/chats","action":"send_chat","status":false,"server_sent_at":"2015-03-03 08:44:03 +0000"}


//message{"message":"Message originated from your #marketing post.\n\nTest","receiver_id":"543b972d79656c39970c0000","sender_id":"543b93fc79656c3997000000","sent_at":"2015-03-03 14:16:11 +0530","wall_id":"54f184e4726169077cb50000","format":"json","controller":"api/v1/chats","action":"send_chat","status":true,"reply_id":"54f574d472616970e1230000","created_at":"2015-03-03T14:16:12.818+05:30","server_sent_at":"2015-03-03 08:46:12 +0000"}
//        03-03 14:16:12.211    3607-3607/red.yelo D/ChatService﹕ [1] ChatService.onReceiveMessage: Received:{"message":"Message originated from your #marketing post.\n\nTest","receiver_id":"543b972d79656c39970c0000","sender_id":"543b93fc79656c3997000000","sent_at":"2015-03-03 14:16:11 +0530","wall_id":"54f184e4726169077cb50000","format":"json","controller":"api/v1/chats","action":"send_chat","status":true,"reply_id":"54f574d472616970e1230000","created_at":"2015-03-03T14:16:12.818+05:30","server_sent_at":"2015-03-03 08:46:12 +0000"}