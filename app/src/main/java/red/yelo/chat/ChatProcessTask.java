

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
 */package red.yelo.chat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.SQLConstants;
import red.yelo.data.TableChatMessages;
import red.yelo.data.TableChats;
import red.yelo.data.TableUsers;
import red.yelo.http.HttpConstants;
import red.yelo.http.JsonUtils;
import red.yelo.http.api.ChatApi;
import red.yelo.retromodels.GetUserModel;
import red.yelo.retromodels.request.SendChatRequestModel;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.ChatStatus;
import red.yelo.utils.AppConstants.ChatType;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.Logger;
import red.yelo.utils.Utils;

/**
 * Runnable implementation to process chat messages
 */
class ChatProcessTask implements Runnable {

    private static final String TAG = "ChatProcessTask";

    private static final String CHAT_SELECTION = DatabaseColumns.CHAT_ID
            + SQLConstants.EQUALS_ARG;


    private static final String USER_SELECTION = DatabaseColumns.USER_ID
            + SQLConstants.EQUALS_ARG;

    private static final String USER_SELECTION_TABLEUSERS = DatabaseColumns.ID
            + SQLConstants.EQUALS_ARG;


    public static final int PROCESS_SEND = 1;
    public static final int PROCESS_RECEIVE = 2;

    /**
     * The process type of the task, either {@linkplain #PROCESS_SEND} or
     * {@linkplain #PROCESS_RECEIVE}
     */
    private int mProcessType;

    /**
     * Reference to the context to prepare notifications
     */
    private Context mContext;

    /**
     * The message Model
     */
    private SendChatRequestModel mMessageModel;

    /**
     * Date formatter for formatting chat timestamps
     */
    private DateFormatter mChatDateFormatter;

    /**
     * Date formatter for formatting timestamps for messages
     */
    private DateFormatter mMessageDateFormatter;

    /**
     * Callback for receiving when it is ready to send the chat message
     */
    private SendChatCallback mSendChatCallback;

    /**
     * received message in json string
     */
    private String mMessage;

    private ChatApi mChatApi;

    /**
     * Callback defined for when the local chat has been saved to the database
     * and the request can be sent
     */
    public static interface SendChatCallback {

        /**
         * Send the chat request
         *
         * @param messageModel The request model
         * @param dbRowId      The row id of the inserted local chat message
         */
        public void sendChat(final SendChatRequestModel messageModel, final long dbRowId);
    }

    private ChatProcessTask() {
        //Private constructor
    }

    private ChatProcessTask(final Context context, final ChatApi chatApi) {
        mContext = context;
        mChatApi = chatApi;
    }

    public int getProcessType() {
        return mProcessType;
    }

    public SendChatRequestModel getMessage() {
        return mMessageModel;
    }

    public DateFormatter getChatDateFormatter() {
        return mChatDateFormatter;
    }

    public DateFormatter getMessageDateFormatter() {
        return mMessageDateFormatter;
    }

    public SendChatCallback getSendChatCallback() {
        return mSendChatCallback;
    }

    @Override
    public void run() {

        if (mProcessType == PROCESS_RECEIVE) {
            processReceivedMessage();
        } else if (mProcessType == PROCESS_SEND) {
            saveMessageAndCallback();
        }

    }

    /**
     * Save a local message in the database, and give a callback to make the
     * chat send request once it is saved
     */
    private void saveMessageAndCallback() {

        try {


            final String senderId = mMessageModel.sender_id;
            final String receiverId = mMessageModel.receiver_id;
            final String sentAtTime = mMessageModel.sent_at;
            final String messageText = mMessageModel.message;
            final String serverReplyId = mMessageModel.reply_id;


            final ContentValues chatValues = new ContentValues(10);

            final String chatId = Utils.generateChatId(senderId, receiverId);

            Logger.d(TAG, "chat id : " + chatId);

            chatValues.put(DatabaseColumns.CHAT_ID, chatId);
            chatValues.put(DatabaseColumns.SERVER_CHAT_ID, serverReplyId);
            chatValues.put(DatabaseColumns.SENDER_ID, senderId);
            chatValues.put(DatabaseColumns.RECEIVER_ID, receiverId);
            chatValues.put(DatabaseColumns.MESSAGE, messageText);
            chatValues.put(DatabaseColumns.TIMESTAMP, sentAtTime);
            chatValues.put(DatabaseColumns.SENT_AT, sentAtTime);
            chatValues.put(DatabaseColumns.CHAT_STATUS, ChatStatus.SENDING);
            chatValues.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter
                    //server's time is 15 seconds ahead which creates sorting bugs,
                    //so we added server lag time in seconds
                    .getEpoch(sentAtTime) + AppConstants.SERVER_LAG_TIME);
            chatValues.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter
                    .getOutputTimestamp(sentAtTime));

            final long insertRowId = DBInterface
                    .insert(TableChatMessages.NAME, null, chatValues, true);

            Logger.d(TAG,insertRowId+"");

            if (insertRowId >= 0) {

                final ContentValues values = new ContentValues(7);
                values.put(DatabaseColumns.CHAT_ID, chatId);
                values.put(DatabaseColumns.SERVER_CHAT_ID, serverReplyId);
                values.put(DatabaseColumns.LAST_MESSAGE_ID, insertRowId);

                values.put(DatabaseColumns.CHAT_TYPE, ChatType.PERSONAL);
                values.put(DatabaseColumns.TIMESTAMP, sentAtTime);
                try {
                    values.put(DatabaseColumns.TIMESTAMP_HUMAN, mChatDateFormatter
                            .getOutputTimestamp(sentAtTime));
                    values.put(DatabaseColumns.TIMESTAMP_EPOCH, mChatDateFormatter
                            .getEpoch(sentAtTime));
                } catch (ParseException e) {
                    //Shouldn't happen
                    e.printStackTrace();
                }

                values.put(DatabaseColumns.ID, receiverId);

                Logger.v(TAG, "Updating chats for Id %s", chatId);
                final int updateCount = DBInterface
                        .update(TableChats.NAME, values, CHAT_SELECTION, new String[]{
                                chatId
                        }, true);

                if (updateCount == 0) {
                    Logger.v(TAG, "INSERTED chats for Id %s", chatId);
                    //Insert the chat message
                    DBInterface.insert(TableChats.NAME, null, values, true);
                }
            }


            //After finishing the local chat insertion, give a callback to do the actual network call
            mSendChatCallback.sendChat(mMessageModel, insertRowId);
        } catch (ParseException e) {
            Logger.e(TAG, e, "Invalid timestamp");
        }
    }

    /**
     * Processes a received message, stores it in the database
     */
    private void processReceivedMessage() {
        try {
            final JSONObject messageJson = new JSONObject(mMessage);
            //TODO we need some change in backend
            if (!mMessage.contains("Chatter not found")) {


                Logger.d(TAG, "1");


                final String messageText = JsonUtils
                        .readString(messageJson, HttpConstants.MESSAGE, false, false);

                final String serverReplyId = JsonUtils
                        .readString(messageJson, HttpConstants.REPLY_ID, false, false);


                String timestamp = JsonUtils
                        .readString(messageJson, HttpConstants.SERVER_SENT_AT, false, false);


                final String sentAtTime = JsonUtils
                        .readString(messageJson, HttpConstants.SENT_AT, false, false);

                String status = JsonUtils
                        .readString(messageJson, HttpConstants.STATUS, false, false);

                String senderId = "",
                        receiverId = "";
                if (status.equals(AppConstants.TRUE)) {
                    //sender values
                    senderId = JsonUtils
                            .readString(messageJson, HttpConstants.SENDER_ID, false, false);

                    //receiver values
                    receiverId = JsonUtils
                            .readString(messageJson, HttpConstants.RECEIVER_ID, false, false);
                } else if (status.equals(AppConstants.ERROR)) {

                    senderId = JsonUtils
                            .readString(messageJson, HttpConstants.SENDER_ID, false, false);

                    //receiver values
                    receiverId = JsonUtils
                            .readString(messageJson, HttpConstants.RECEIVER_ID, false, false);

                    timestamp = JsonUtils
                            .readString(messageJson, HttpConstants.SENT_AT, false, false);
                }

                final String chatId = Utils.generateChatId(receiverId, senderId);

                final ContentValues chatValues = new ContentValues(7);

                boolean isSenderCurrentUser = senderId
                        .equals(AppConstants.UserInfo.INSTANCE.getId());
                Logger.d(TAG, "2");

                Cursor cursorSender = getUserDetails(senderId);

                Cursor cursorReceiver = getUserDetails(receiverId);

                cursorSender.moveToFirst();
                cursorReceiver.moveToFirst();


                Logger.d(TAG, "chat id received : " + chatId);

                final String senderName = cursorSender.getString
                        (cursorSender.getColumnIndex(DatabaseColumns.USER_NAME));

                final String senderImage = cursorSender.getString
                        (cursorSender.getColumnIndex(DatabaseColumns.USER_IMAGE));

                chatValues.put(DatabaseColumns.CHAT_ID, chatId);
                chatValues.put(DatabaseColumns.SERVER_CHAT_ID, serverReplyId);
                chatValues.put(DatabaseColumns.SENDER_ID, senderId);
                chatValues.put(DatabaseColumns.SENDER_NAME, cursorSender.getString
                        (cursorSender.getColumnIndex(DatabaseColumns.USER_NAME)));
                chatValues.put(DatabaseColumns.SENDER_IMAGE, cursorSender.getString
                        (cursorSender.getColumnIndex(DatabaseColumns.USER_IMAGE)));
                chatValues.put(DatabaseColumns.RECEIVER_ID, receiverId);
                chatValues.put(DatabaseColumns.RECEIVER_NAME, cursorReceiver.getString
                        (cursorSender.getColumnIndex(DatabaseColumns.USER_NAME)));
                chatValues.put(DatabaseColumns.RECEIVER_IMAGE, cursorReceiver.getString
                        (cursorSender.getColumnIndex(DatabaseColumns.USER_IMAGE)));
                chatValues.put(DatabaseColumns.MESSAGE, messageText);
                chatValues.put(DatabaseColumns.TIMESTAMP, timestamp);
                chatValues.put(DatabaseColumns.SENT_AT, sentAtTime);
                chatValues.put(DatabaseColumns.CHAT_STATUS, isSenderCurrentUser ? ChatStatus.SENT
                        : ChatStatus.RECEIVED);
                chatValues.put(DatabaseColumns.TIMESTAMP_EPOCH, mMessageDateFormatter
                        .getEpoch(timestamp));
                chatValues.put(DatabaseColumns.TIMESTAMP_HUMAN, mMessageDateFormatter
                        .getOutputTimestamp(timestamp));

                Logger.d(TAG, "3");
                if (isSenderCurrentUser) {
                    //Update the locally saved message to mark it as sent

                    //Insert the chat message into DB
                    final String selection = DatabaseColumns.SENDER_ID
                            + SQLConstants.EQUALS_ARG + SQLConstants.AND
                            + DatabaseColumns.SENT_AT
                            + SQLConstants.EQUALS_ARG;

                    final String[] args = new String[]{
                            senderId, sentAtTime
                    };


                    Logger.d(TAG, "updated message in current user");

                    if (DBInterface.update(TableChatMessages.NAME, chatValues, selection, args, true) == 0) {
                        DBInterface.insert(TableChatMessages.NAME, null, chatValues, true);
                    }

                } else {

                    Logger.d(TAG, "inserted message");
                    //Insert the message in the db
                    final long insertRowId = DBInterface
                            .insert(TableChatMessages.NAME, null, chatValues, true);



                /*
                 * Parse and store sender info. We will receive messages both
                 * when we send and receive, so we need to check the sender id
                 * if it is our own id first to detect who sent the message
                 */


                    ChatNotificationHelper
                            .getInstance(mContext)
                            .showChatReceivedNotification(mContext, chatId, senderId, receiverId,
                                    senderName, messageText, senderImage);


                    final ContentValues values = new ContentValues(7);
                    values.put(DatabaseColumns.CHAT_ID, chatId);
                    values.put(DatabaseColumns.LAST_MESSAGE_ID, insertRowId);
                    values.put(DatabaseColumns.SERVER_CHAT_ID, serverReplyId);
                    values.put(DatabaseColumns.CHAT_TYPE, ChatType.PERSONAL);
                    values.put(DatabaseColumns.UNREAD_COUNT,1);

                    values.put(DatabaseColumns.ID, isSenderCurrentUser ? receiverId
                            : senderId);
                    values.put(DatabaseColumns.TIMESTAMP_HUMAN, mChatDateFormatter
                            .getOutputTimestamp(timestamp));
                    values.put(DatabaseColumns.TIMESTAMP, timestamp);
                    values.put(DatabaseColumns.TIMESTAMP_EPOCH, mChatDateFormatter
                            .getEpoch(timestamp));

                    final int updateCount = DBInterface
                            .update(TableChats.NAME, values, CHAT_SELECTION, new String[]{
                                    chatId
                            }, true);

                    Logger.v(TAG, "Updating chats for Id %s", chatId);


                    if (updateCount == 0) {
                        DBInterface.insert(TableChats.NAME, null, values, true);

                    }
                }
            } else {
                Logger.d(TAG, "some error in message sending");
            }

        } catch (JSONException e) {
            Logger.e(TAG, e, "Invalid message json");
        } catch (ParseException e) {
            Logger.e(TAG, e, "Invalid timestamp");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Builder for Chat Process tasks
     */
    public static class Builder {

        private Context mContext;

        private ChatProcessTask mChatProcessTask;

        private ChatApi mChatService;

        public Builder(Context context, ChatApi chatApi) {
            mContext = context;
            mChatProcessTask = new ChatProcessTask(mContext, chatApi);
            mChatService = chatApi;
        }

        public Builder setProcessType(int processType) {
            mChatProcessTask.mProcessType = processType;
            return this;
        }

        public Builder setMessage(String message) {
            mChatProcessTask.mMessage = message;
            return this;
        }

        public Builder setMessageModel(SendChatRequestModel messageModel) {
            mChatProcessTask.mMessageModel = messageModel;
            return this;
        }

        public Builder setChatDateFormatter(DateFormatter chatDateFormatter) {
            mChatProcessTask.mChatDateFormatter = chatDateFormatter;
            return this;
        }

        public Builder setMessageDateFormatter(
                DateFormatter messageDateFormatter) {
            mChatProcessTask.mMessageDateFormatter = messageDateFormatter;
            return this;
        }

        public Builder setSendChatCallback(SendChatCallback sendChatCallback) {
            mChatProcessTask.mSendChatCallback = sendChatCallback;
            return this;
        }

        /**
         * Builds the chat process task
         *
         * @return The complete chat process task
         * @throws IllegalStateException If the chat process task is invalid
         */
        public ChatProcessTask build() {

            if (mChatProcessTask.mProcessType != PROCESS_RECEIVE
                    && mChatProcessTask.mProcessType != PROCESS_SEND) {
                throw new IllegalStateException("Invalid process type");
            }


            if (mChatProcessTask.mMessageModel != null) {
                if (TextUtils.isEmpty(mChatProcessTask.mMessageModel.message)) {
                    throw new IllegalStateException("Empty or null message");
                }
            } else {
                if (TextUtils.isEmpty(mChatProcessTask.mMessage)) {
                    throw new IllegalStateException("Empty or null message");
                }
            }

            if (mChatProcessTask.mChatDateFormatter == null) {
                throw new IllegalStateException("No chat date formatter set");
            }

            if (mChatProcessTask.mMessageDateFormatter == null) {
                throw new IllegalStateException("No message date formatter set");
            }

            if (mChatProcessTask.mProcessType == PROCESS_SEND
                    && mChatProcessTask.mSendChatCallback == null) {
                throw new IllegalStateException("No send chat callback set for a send message");
            }

            return mChatProcessTask;
        }

        /**
         * Resets the builder for preparing another chat process task
         */
        public Builder reset() {
            mChatProcessTask = new ChatProcessTask(mContext, mChatService);
            return this;
        }
    }


    private Cursor getUserDetails(String userId) {

        String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
        Cursor cursor = DBInterface.query(true, TableUsers.NAME, null, selection, new String[]{userId}, null, null, null
                , null);

        if (cursor.getCount() != 0) {
            return cursor;
        } else {

            GetUserModel userDetail = mChatApi.getUserDetail(userId);

            ContentValues values = new ContentValues();
            values.put(DatabaseColumns.ID, userId);
            values.put(DatabaseColumns.USER_IMAGE, userDetail.user.image_url);
            values.put(DatabaseColumns.USER_NAME, userDetail.user.name);
            values.put(DatabaseColumns.USER_DESCRIPTION, userDetail.user.description);


            DBInterface.insert(TableUsers.NAME, null, values, false);

            String selection1 = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

            return DBInterface.query(true, TableUsers.NAME, null, selection1, new String[]{userId}, null, null, null
                    , null);
        }
    }

}
