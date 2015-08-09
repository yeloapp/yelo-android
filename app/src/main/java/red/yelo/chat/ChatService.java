
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
package red.yelo.chat;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import com.squareup.otto.Bus;

import org.apache.http.protocol.HTTP;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import red.yelo.BuildConfig;
import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.chat.AbstractRabbitMQConnector.OnDisconnectCallback;
import red.yelo.chat.ChatRabbitMQConnector.OnReceiveMessageHandler;
import red.yelo.data.DBInterface;
import red.yelo.data.DBInterface.AsyncDbQueryCallback;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.TableChatMessages;
import red.yelo.data.TableWallComments;
import red.yelo.data.TableWallPosts;
import red.yelo.http.HttpConstants;
import red.yelo.http.RetroCallback;
import red.yelo.http.api.ChatApi;
import red.yelo.http.api.YeloApi;
import red.yelo.retromodels.request.SendChatRequestModel;
import red.yelo.retromodels.response.GetCreateWallResponseModel;
import red.yelo.retromodels.response.GetWallItemResponseModel;
import red.yelo.retromodels.response.GetWallResponseModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.DeviceInfo;
import red.yelo.utils.AppConstants.UserInfo;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Bound service to send and receive chat messages. The service will receive chat messages and
 * update them in the chats database. <br/> <br/> This service needs to be triggered in two cases -
 * <ol> <li>On application launch - This is done in {@link red.yelo.YeloApplication#onCreate()}</li>
 * <li>On network connectivity resumed(if it was lost) - This is done in {@link
 * red.yelo.http.NetworkChangeReceiver#onReceive(android.content.Context, android.content.Intent)}</li> </ol> <br/> This will take care of keeping
 * it tied to the chat server and listening for messages. <br/> <br/> For publishing messages,
 * however, you need to bind to this service, check if chat is connected and then publish the
 * message
 */
public class ChatService extends Service implements OnReceiveMessageHandler,
         OnDisconnectCallback, AsyncDbQueryCallback, RetroCallback.RetroResponseListener {
    private static final String TAG = "ChatService";
    private static final String QUEUE_NAME_FORMAT = "%squeue";
    private static final String VIRTUAL_HOST = "/";
    private static final String EXCHANGE_NAME_FORMAT = "%sexchange";

    private static final String USERNAME = BuildConfig.CHAT_USERNAME;
    private static final String PASSWORD = BuildConfig.CHAT_PASSWORD;

    /**
     * Minimum time interval(in seconds) to wait between subsequent connect attempts
     */
    private static final int CONNECT_BACKOFF_INTERVAL = 5;

    private static final int CHAT_STOPPAGE_TIME = 1000 * 60 * 5;

    /**
     * Maximum multiplier for the connect interval
     */
    private static final int MAX_CONNECT_MULTIPLIER = 180;

    private static final String MESSAGE_SELECT_BY_TIME_ID = DatabaseColumns.SENT_AT
            + SQLConstants.EQUALS_ARG;

    private final IBinder mChatServiceBinder = new ChatServiceBinder();

    /**
     * {@link ChatRabbitMQConnector} instance for listening to messages
     */
    private ChatRabbitMQConnector mMessageConsumer;

    private DateFormatter mChatDateFormatter;

    private DateFormatter mMessageDateFormatter;

    private String mQueueName;

    private List<RetroCallback> retroCallbackList = new ArrayList<RetroCallback>();


    /**
     * Current multiplier for connecting to chat. Can vary between 0 to {@link
     * #MAX_CONNECT_MULTIPLIER}
     */
    private int mCurrentConnectMultiplier;
    /**
     * Task to connect to Rabbit MQ Chat server
     */
    private ConnectToChatAsyncTask mConnectTask;

    private Handler mHandler;

    private Runnable mConnectRunnable;

    private ChatProcessTask.Builder mChatProcessTaskBuilder;

    private ChatApi mChatApiService;

    private YeloApi mYeloApi;

    /**
     * Single thread executor to process incoming chat messages in a queue
     */
    private ExecutorService mChatProcessor;

    /**
     * Holds the reference to the otto bus from LavocalApplication
     */

    private String mSentTimeId;

    private Bus mbus;

    private GetWallResponseModel mGetWallResponseModel;

    private Handler mHandlerStopChat;


    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferenceHelper.registerSharedPreferencesChangedListener(
                ChatNotificationHelper.getInstance(this).getOnSharedPreferenceChangeListener());
        mChatDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                AppConstants.CHAT_TIME_FORMAT);
        mMessageDateFormatter = new DateFormatter(AppConstants.TIMESTAMP_FORMAT,
                AppConstants.MESSAGE_TIME_FORMAT);
        mChatApiService = ((YeloApplication) getApplication()).getChatApi();
        mYeloApi = ((YeloApplication) getApplication()).getYeloApi();


        mCurrentConnectMultiplier = 0;
        mHandler = new Handler();
        mChatProcessor = Executors.newSingleThreadExecutor();
        mChatProcessTaskBuilder = new ChatProcessTask.Builder(this, mChatApiService);

        ((YeloApplication) getApplication().getApplicationContext()).getBus().register(this);

    }

    /**
     * Sets the id of the user the current chat is being done with. Set this to the user id when the
     * chat detail screen opens, and clear it when the screen is paused. It is used to hide
     * notifications when the chat message received is from the user currently being chatted with
     *
     * @param currentChattingUserId The id of the current user being chatted with
     */
    public void setCurrentChattingUserId(final String currentChattingUserId) {
        ChatNotificationHelper.getInstance(this)
                .setCurrentChattingUserId(currentChattingUserId);
    }


    @Override
    public void success(Object model, int requestId) {

        switch (requestId) {

            case HttpConstants.ApiResponseCodes.GET_ALL_WALLS: {

                //Doing it here because we don't want to block the wall api for uploading the contacts


                GetWallResponseModel wallResponseModel = ((GetWallResponseModel) model);

                mGetWallResponseModel = wallResponseModel;
                for (int i = 0; i < mGetWallResponseModel.search.size(); i++) {

                    ContentValues values = new ContentValues(6);
                    values.put(DatabaseColumns.ID, mGetWallResponseModel.search.get(i).id);
                    values.put(DatabaseColumns.MESSAGE, mGetWallResponseModel.search.get(i).message);
                    values.put(DatabaseColumns.TAG_NAME, mGetWallResponseModel.search.get(i).tag_name);
                    values.put(DatabaseColumns.TAG_ID, mGetWallResponseModel.search.get(i).tag_id);
                    values.put(DatabaseColumns.TAG_USER_COUNT, mGetWallResponseModel.search.get(i).tagged_users_count);
                    values.put(DatabaseColumns.CHAT_USER_COUNT, mGetWallResponseModel.search.get(i).chat_users_count);
                    values.put(DatabaseColumns.USER_NAME, mGetWallResponseModel.search.get(i).wall_owner.name);
                    values.put(DatabaseColumns.USER_ID, mGetWallResponseModel.search.get(i).wall_owner.user_id);
                    values.put(DatabaseColumns.DATE_TIME, mGetWallResponseModel.search.get(i).created_at);
                    values.put(DatabaseColumns.CITY, mGetWallResponseModel.search.get(i).city);
                    values.put(DatabaseColumns.COLOR, mGetWallResponseModel.search.get(i).group_color);
                    values.put(DatabaseColumns.COUNTRY, mGetWallResponseModel.search.get(i).country);
                    values.put(DatabaseColumns.GROUP_ID, mGetWallResponseModel.search.get(i).group_id);
                    values.put(DatabaseColumns.GROUP_NAME, mGetWallResponseModel.search.get(i).group_name);
                    if (TextUtils.isEmpty(mGetWallResponseModel.search.get(i).address)) {
                        values.put(DatabaseColumns.ADDRESS, Character.toUpperCase(mGetWallResponseModel.search.get(i).city.charAt(0)) + mGetWallResponseModel.search.get(i).city.substring(1));

                    } else {
                        values.put(DatabaseColumns.ADDRESS, mGetWallResponseModel.search.get(i).address);
                    }

                    try {
                        values.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(mGetWallResponseModel.search.get(i).created_at));
                        values.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(mGetWallResponseModel.search.get(i).created_at));

                    } catch (ParseException e) {
                        e.printStackTrace();
                        //should not happen
                    }
                    if (mGetWallResponseModel.search.get(i).wall_image != null) {
                        values.put(DatabaseColumns.WALL_IMAGES, mGetWallResponseModel.search.get(i).wall_image.image_url);
                    }
                    values.put(DatabaseColumns.USER_IMAGE, mGetWallResponseModel.search.get(i).wall_owner.image_url);


                    String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


                    DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_WALLPOST, getTaskTag(), values
                            , TableWallPosts.NAME, values, selection, new String[]{mGetWallResponseModel.search.get(i).id}, false, this);


                    if (mGetWallResponseModel.search.get(i).wall_items.size() != 0) {
                        for (int j = 0; j < mGetWallResponseModel.search.get(i).wall_items.size(); j++) {


                            GetWallItemResponseModel.WallItem wallItem = mGetWallResponseModel.search.get(i).wall_items.get(j);
                            ContentValues valuesComments = new ContentValues();
                            valuesComments.put(DatabaseColumns.WALL_ID, mGetWallResponseModel.search.get(i).id);
                            valuesComments.put(DatabaseColumns.ID, wallItem.id);
                            valuesComments.put(DatabaseColumns.COMMENT, wallItem.comment);
                            valuesComments.put(DatabaseColumns.WALL_USER_ID, mGetWallResponseModel.search.get(i).wall_owner.user_id);
                            valuesComments.put(DatabaseColumns.USER_ID, wallItem.user_id);
                            valuesComments.put(DatabaseColumns.TYPE, AppConstants.CommentType.REFER);
                            valuesComments.put(DatabaseColumns.USER_NAME, wallItem.name);
                            valuesComments.put(DatabaseColumns.IMAGE_URL, wallItem.image_url);
                            valuesComments.put(DatabaseColumns.DATE_TIME, wallItem.created_at);

                            Logger.d(TAG, wallItem.created_at);

                            try {
                                valuesComments.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(wallItem.created_at));
                                valuesComments.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(wallItem.created_at));

                            } catch (ParseException e) {
                                e.printStackTrace();
                                //should not happen
                            }

                            if (wallItem.tagged_users.size() > 0) {
                                valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.tagged_users.get(0).id);
                                valuesComments.put(DatabaseColumns.TAGGED_NAMES, wallItem.tagged_users.get(0).name);
                                valuesComments.put(DatabaseColumns.IS_PRESENT, wallItem.tagged_users.get(0).is_present + "");


                                if (wallItem.tagged_users.get(0).details != null) {
                                    valuesComments.put(DatabaseColumns.TAGGED_USER_NUMBERS, wallItem.tagged_users.get(0).details.mobile_number);
                                    valuesComments.put(DatabaseColumns.TAGGED_USER_EMAILS, wallItem.tagged_users.get(0).details.email);
                                }

                                valuesComments.put(DatabaseColumns.TAGGED_IMAGE_URLS, wallItem.tagged_users.get(0).image_url);
                                valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.tagged_users.get(0).user_id);
                                valuesComments.put(DatabaseColumns.TAGGED_IDS, wallItem.tagged_users.get(0).id);
                            }

                            String selectionWallId = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

                            DBInterface
                                    .updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, getTaskTag(),
                                            valuesComments, TableWallComments.NAME, valuesComments, selectionWallId,
                                            new String[]{wallItem.id}, false, this);
                        }
                    }

                    if (mGetWallResponseModel.search.get(i).wall_chats.size() != 0) {
                        for (int j = 0; j < mGetWallResponseModel.search.get(i).wall_chats.size(); j++) {


                            GetCreateWallResponseModel.WallChats wallItem = mGetWallResponseModel.search.get(i).wall_chats.get(j);
                            ContentValues valuesComments = new ContentValues();
                            valuesComments.put(DatabaseColumns.WALL_ID, mGetWallResponseModel.search.get(i).id);
                            valuesComments.put(DatabaseColumns.ID, mGetWallResponseModel.search.get(i).id+wallItem.user_id);
                            valuesComments.put(DatabaseColumns.WALL_USER_ID, mGetWallResponseModel.search.get(i).wall_owner.user_id);
                            valuesComments.put(DatabaseColumns.TAGGED_USER_IDS, wallItem.user_id);
                            valuesComments.put(DatabaseColumns.TAGGED_NAMES, wallItem.name);
                            valuesComments.put(DatabaseColumns.TYPE, AppConstants.CommentType.CHAT);
                            valuesComments.put(DatabaseColumns.USER_ID, mGetWallResponseModel.search.get(i).wall_owner.user_id);
                            valuesComments.put(DatabaseColumns.USER_NAME, mGetWallResponseModel.search.get(i).wall_owner.name);
                            valuesComments.put(DatabaseColumns.TAGGED_IMAGE_URLS, wallItem.image_url);
                            valuesComments.put(DatabaseColumns.TAGGED_IDS, wallItem.user_id);
                            valuesComments.put(DatabaseColumns.IS_PRESENT, "true");
                            valuesComments.put(DatabaseColumns.DATE_TIME, wallItem.last_chat);

                            try {
                                valuesComments.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter.getEpoch(wallItem.last_chat));
                                valuesComments.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter.getOutputTimestamp(wallItem.last_chat));

                            } catch (ParseException e) {
                                e.printStackTrace();
                                //should not happen
                            }


                            String selectionWallId = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;


                            DBInterface
                                    .updateAsync(AppConstants.QueryTokens.UPDATE_WALLCOMMENTS, getTaskTag(),
                                            valuesComments, TableWallComments.NAME, valuesComments, selectionWallId,
                                            new String[]{ mGetWallResponseModel.search.get(i).id+wallItem.user_id}, false, this);

                        }
                    }


                }


                break;
            }
        }
    }

    @Override
    public void failure(int requestId, int errorCode, String message) {

    }

    /**
     * Binder to connect to the Chat Service
     */
    public class ChatServiceBinder extends Binder {

        public ChatService getService() {
            return ChatService.this;
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return mChatServiceBinder;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {

        final String action = intent != null ? intent.getAction() : null;

        if ((action != null)
                && action.equals(AppConstants.ACTION_DISCONNECT_CHAT)) {

            if (isConnectedToChat()) {

                mMessageConsumer.dispose(true);
                mMessageConsumer = null;

            }
        } else {
            mCurrentConnectMultiplier = 0;
            initMessageConsumer();

            connectChatService();
        }

        return START_NOT_STICKY;
    }

    /**
     * Connects to the Chat Service
     */

    private void connectChatService() {

        //If there already is a pending connect task, remove it since we have a newer one
        if (mConnectRunnable != null) {
            mHandler.removeCallbacks(mConnectRunnable);
        }

        if (mMessageConsumer == null) {
            Logger.e(TAG, "should not happen");
        } else {

            if (isLoggedIn() && !mMessageConsumer.isRunning()) {

                mConnectRunnable = new Runnable() {

                    @Override
                    public void run() {

                        if (!isLoggedIn()
                                || !DeviceInfo.INSTANCE
                                .isNetworkConnected()) {

                            //If there is no internet connection or we are not logged in, we need not attempt to connect
                            mConnectRunnable = null;
                            return;
                        }

                        mQueueName = generateQueueNameFromUserId(UserInfo.INSTANCE.getId());

                        if (mConnectTask == null) {
                            mConnectTask = new ConnectToChatAsyncTask();
                            mConnectTask.execute(USERNAME, PASSWORD, mQueueName, UserInfo.INSTANCE
                                    .getId());
                        } else {
                            final Status connectingStatus = mConnectTask
                                    .getStatus();

                            if (connectingStatus != Status.RUNNING) {

                                // We are not already attempting to connect, let's try connecting
                                if (connectingStatus == Status.PENDING) {
                                    //Cancel a pending task
                                    mConnectTask.cancel(false);
                                }

                                mConnectTask = new ConnectToChatAsyncTask();
                                mConnectTask.execute(USERNAME, PASSWORD, mQueueName, UserInfo.INSTANCE
                                        .getId());
                            }
                        }
                        mConnectRunnable = null;

                    }

                };

                mHandler.postDelayed(mConnectRunnable, mCurrentConnectMultiplier
                        * CONNECT_BACKOFF_INTERVAL * 1000);
                mCurrentConnectMultiplier = (++mCurrentConnectMultiplier > MAX_CONNECT_MULTIPLIER) ? MAX_CONNECT_MULTIPLIER
                        : mCurrentConnectMultiplier;
            }
        }

    }

    /**
     * Check if user is logged in or not
     */
    private boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getAuthToken());
    }

    @Override
    public void onDestroy() {
        SharedPreferenceHelper.unregisterSharedPreferencesChangedListener(
                ChatNotificationHelper.getInstance(this).getOnSharedPreferenceChangeListener());
        if (isConnectedToChat()) {
            mMessageConsumer.dispose(true);
            mMessageConsumer = null;
        }
        mHandler.removeCallbacks(mConnectRunnable);
        mChatProcessor.shutdownNow();

        Logger.d(TAG, "chat stopped");
        super.onDestroy();
    }


    /**
     * Is the chat service connected or not
     */
    public boolean isConnectedToChat() {

        return (mMessageConsumer != null) && mMessageConsumer.isRunning();
    }


    /**
     * Send a message to a user
     *
     * @param
     */
    public void sendMessageToUser(final String sender_id, final String receiver_id,
                                  String wallId, boolean isWallIdSent,
                                  final String reply_id,
                                  final String message, final String sent_at) {


        if (!isLoggedIn()) {
            return;
        }


        SendChatRequestModel chatRequestModel = new SendChatRequestModel();

        chatRequestModel.setSender_id(sender_id);
        chatRequestModel.setReceiver_id(receiver_id);
        chatRequestModel.setReply_id(reply_id);
        chatRequestModel.setMessage(message);
        chatRequestModel.setSent_at(sent_at);
        if (!isWallIdSent) {
            chatRequestModel.setWall_id(wallId);
        }

        mSentTimeId = sent_at;


        final ChatProcessTask chatProcessTask = mChatProcessTaskBuilder
                .setProcessType(ChatProcessTask.PROCESS_SEND)
                .setMessageModel(chatRequestModel)
                .setMessageDateFormatter(mMessageDateFormatter)
                .setChatDateFormatter(mChatDateFormatter)
                .setSendChatCallback(new ChatProcessTask.SendChatCallback() {

                    @Override
                    public void sendChat(SendChatRequestModel messageModel,

                                         long dbRowId) {

                        final SendChatRequestModel model = messageModel;
                        //Post on main thread
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                mChatApiService.sendChat(model, new Callback<Object>() {
                                    @Override
                                    public void success(Object o, Response response) {
                                        markChatAsSent(mSentTimeId);
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        markChatAsFailed(mSentTimeId);
                                    }

                                });
                            }
                        });
                    }
                }).build();

        mChatProcessTaskBuilder.reset();
        mChatProcessor.submit(chatProcessTask);
    }


    /**
     * Cancels any notifications being displayed. Call this if the relevant screen is opened within
     * the app
     */
    public void clearChatNotifications() {

        ChatNotificationHelper.getInstance(this).clearChatNotifications();
    }

    /**
     * Set Chat screen currently visible to the user
     *
     * @param visible <code>true</code> to set chat screen visible to the user, <code>false</code>
     *                to disable them
     */
    public void setChatScreenVisible(final boolean visible) {
        ChatNotificationHelper.getInstance(this)
                .setChatScreenVisible(visible);
    }

    /**
     * Uses the portion of the user's email before the "@" to generate the queue name
     *
     * @param userId The user Id
     * @return The queue name for the user email
     */
    private String generateQueueNameFromUserId(
            final String userId) {

        return String.format(Locale.US, QUEUE_NAME_FORMAT, userId);

    }

    @Override
    public void onReceiveMessage(final byte[] message) {

        String text = "";
        try {
            text = new String(message, HTTP.UTF_8);

            Logger.d(TAG, "Received:" + text);

            final ChatProcessTask chatProcessTask = mChatProcessTaskBuilder
                    .setProcessType(ChatProcessTask.PROCESS_RECEIVE)
                    .setMessage(text)
                    .setChatDateFormatter(mChatDateFormatter)
                    .setMessageDateFormatter(mMessageDateFormatter)
                    .build();
            mChatProcessTaskBuilder.reset();
            mChatProcessor.submit(chatProcessTask);

        } catch (final UnsupportedEncodingException e) {
            //Shouldn't be happening
        } catch (final RejectedExecutionException e) {
            //Shouldn't be happnening
        }

    }

    /**
     * Asynchronously connect to Chat Server
     * Connector The execute() call requires 4 string params - The username, password, queue name in
     * the same order. All parameters should be passed. Send an EMPTY STRING if not required
     */
    private class ConnectToChatAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {

            //Validation
            assert (params != null);
            assert (params.length == 3);
            assert (params[0] != null);
            assert (params[1] != null);
            assert (params[2] != null);
            Logger.v(TAG, "Username %s, Password %s, Queue %s", params[0], params[1], params[2]);
            if (mMessageConsumer != null) {
                mMessageConsumer
                        .connectToRabbitMQ(params[0], params[1], params[2], true, false, false, null);
            }

            return null;
        }


        @Override
        protected void onPostExecute(final Void result) {
            if (!isConnectedToChat()) {
                /* If it's not connected, try connecting again */
                connectChatService();
            } else {

                mCurrentConnectMultiplier = 0;
            }
        }
    }


    @Override
    public void onDisconnect(final boolean manual) {
        if (!manual) {
            connectChatService();
        }
    }

    public Object getTaskTag() {
        return hashCode();
    }

    /**
     * Creates a new consumer
     */
    private void initMessageConsumer() {
        if ((mMessageConsumer == null) && isLoggedIn()) {
            mMessageConsumer = new ChatRabbitMQConnector(HttpConstants.getChatUrl(), HttpConstants
                    .getChatPort(), VIRTUAL_HOST, String
                    .format(Locale.US,
                            EXCHANGE_NAME_FORMAT,
                            UserInfo.INSTANCE
                                    .getId()
                    ),
                    AbstractRabbitMQConnector.ExchangeType.FANOUT
            );

            Logger.d(TAG, "consumer initialized");
            mMessageConsumer.setOnReceiveMessageHandler(ChatService.this);
            mMessageConsumer.setOnDisconnectCallback(ChatService.this);
        }

    }


    /*
     * (non-Javadoc)
     * @see
     * com.lovocal.data.DBInterface.AsyncDbQueryCallback#onInsertComplete(int,
     * java.lang.Object, long)
     */
    @Override
    public void onInsertComplete(int token, Object cookie, long insertRowId) {

    }

    /*
     * (non-Javadoc)
     * @see
     * com.lovocal.data.DBInterface.AsyncDbQueryCallback#onDeleteComplete(int,
     * java.lang.Object, int)
     */
    @Override
    public void onDeleteComplete(int token, Object cookie, int deleteCount) {

        if (token == AppConstants.QueryTokens.DELETE_WALL_POSTS_SEARCH_RESULTS) {
            fetchWallMessages("1");

        }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.lovocal.data.DBInterface.AsyncDbQueryCallback#onUpdateComplete(int,
     * java.lang.Object, int)
     */
    @Override
    public void onUpdateComplete(int token, Object cookie, int updateCount) {
        if (token == AppConstants.QueryTokens.UPDATE_WALLPOST) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;


                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLPOST, getTaskTag(), null
                        , TableWallPosts.NAME, null, values, true, this);
            }


        } else if (token == AppConstants.QueryTokens.UPDATE_WALLCOMMENTS) {
            if (updateCount == 0) {


                final ContentValues values = (ContentValues) cookie;
                DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_WALLCOMMENT, getTaskTag(), null
                        , TableWallComments.NAME, null, values, true, this);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.lovocal.data.DBInterface.AsyncDbQueryCallback#onQueryComplete(int,
     * java.lang.Object, android.database.Cursor)
     */
    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (token == AppConstants.QueryTokens.QUERY_SENDER_USER_DETAIL) {

        }
    }

    /**
     * @param messageSentId The database row of the locally inserted chat message
     */
    private void markChatAsFailed(String messageSentId) {
        final ContentValues values = new ContentValues(1);
        values.put(DatabaseColumns.CHAT_STATUS, AppConstants.ChatStatus.FAILED);

        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_MESSAGE_STATUS, hashCode(), null,
                TableChatMessages.NAME, values, MESSAGE_SELECT_BY_TIME_ID, new String[]{
                        String.valueOf(messageSentId)
                }, true, this
        );
    }

    /**
     * @param messageSentId The database row of the locally inserted chat message
     */
    private void markChatAsSent(String messageSentId) {
        final ContentValues values = new ContentValues(1);
        values.put(DatabaseColumns.CHAT_STATUS, AppConstants.ChatStatus.SENT);

        DBInterface.updateAsync(AppConstants.QueryTokens.UPDATE_MESSAGE_STATUS, hashCode(), null,
                TableChatMessages.NAME, values, MESSAGE_SELECT_BY_TIME_ID, new String[]{
                        String.valueOf(messageSentId)
                }, true, this
        );
    }
    private void fetchWallMessages(String pageNumber) {


        final Map<String, String> params = new HashMap<String, String>(3);

        if (!SharedPreferenceHelper.getString(R.string.pref_latitude).equals("")) {
            params.put(HttpConstants.LATITUDE, SharedPreferenceHelper.getString(R.string.pref_latitude));
            params.put(HttpConstants.LONGITUDE, SharedPreferenceHelper.getString(R.string.pref_longitude));

            params.put(HttpConstants.TYPE, HttpConstants.SearchType.WALL);
            params.put(HttpConstants.RADIUS, "50");
            params.put(HttpConstants.PER, "10");
            params.put(HttpConstants.PAGE, pageNumber + "");

            RetroCallback retroCallback;
            retroCallback = new RetroCallback(this);
            retroCallback.setRequestId(HttpConstants.ApiResponseCodes.GET_ALL_WALLS);
            retroCallbackList.add(retroCallback);

            mYeloApi.getWallMessages(params, retroCallback);

        }
    }

    /**
     * Stop the chat service after 5 minutes. This is done because gcm ping from the server is sent after 5 minutes of
     * app closing, so as to listen the message we should keep chat service active for
     * 5 minutes
     */

    public void stopChatServiceAfterFiveMinutes() {
        mHandlerStopChat= new Handler();
        mHandlerStopChat.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelf();
            }
        }, CHAT_STOPPAGE_TIME);


        //clearLocalWallCacheAndRefech();

    }

    public void clearLocalWallCacheAndRefech() {

        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_WALL_POSTS_SEARCH_RESULTS, getTaskTag(),
                null, TableWallPosts.NAME, null, null, true, this);

        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_WALL_COMMENTS, getTaskTag(),
                null,TableWallComments.NAME,null,null,true,this);

    }

}
