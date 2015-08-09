
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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.bumptech.glide.Glide;

import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.activities.ChatScreenActivity;
import red.yelo.activities.HomeActivity;
import red.yelo.utils.AppConstants;
import red.yelo.utils.SharedPreferenceHelper;

/**
 * Class to help with sending chat notifications
 */
class ChatNotificationHelper {

    /**
     * Notification Id for notifications related to messages
     */
    private static final int MESSAGE_NOTIFICATION_ID = 1;

    private static final String TAG = "ChatNotificationHelper";

    private static final Object LOCK = new Object();

    /**
     * Vibration pattern for notifications
     */
    private static final long[] VIBRATION_PATTERN = new long[]{50, 250};

    /**
     * Holds the number of unread received messages
     */
    private int mUnreadMessageCount;

    /**
     * Id of the user with whom the user is currently chatting.
     */
    private String mCurrentChattingUserId;

    /**
     * Whether the chats screen is opened or not. If it is, we don't need to show notifications
     * since it is already visible to the user
     */
    private boolean mChatScreenVisible;

    private Builder mNotificationBuilder;

    private NotificationManager mNotificationManager;

    private Uri mNotificationSoundUri;

    private static ChatNotificationHelper sInstance;

    private boolean mNotificationsEnabled;

    private boolean mVibrationEnabled;

    /**
     * Listener to listen when Shared Preferences chages to update the settings
     */
    private final SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

            final Context context = YeloApplication.getStaticContext();
            if (key.equals(context.getString(R.string.pref_chat_ringtone))) {
                mNotificationSoundUri = getUserSelectedSoundUri();
            } else if (key.equals(context.getString(R.string.pref_enable_chat_notifications))) {
                mNotificationsEnabled = getNotificationsEnabled();
            } else if (key.equals(context.getString(R.string.pref_enable_chat_vibrate))) {
                mVibrationEnabled = getVibrationEnabled();
            }
        }
    };

    private ChatNotificationHelper() {
        //Private constructor
        mCurrentChattingUserId = null;
        mChatScreenVisible = true;
        mUnreadMessageCount = 0;
    }

    private ChatNotificationHelper(Context context) {
        //Private Constructor
        this();
        mNotificationBuilder = new Builder(context);
        mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationSoundUri = getUserSelectedSoundUri();
        mNotificationsEnabled = getNotificationsEnabled();
        mVibrationEnabled = getVibrationEnabled();

    }

    /**
     * Returns whether the chat vibration is enabled or not
     */
    public boolean getVibrationEnabled() {
        return SharedPreferenceHelper.getBoolean(R.string.pref_enable_chat_vibrate, true);
    }

    /**
     * Returns whether the chat notifications are enabled from settings or not
     */
    private boolean getNotificationsEnabled() {
        return SharedPreferenceHelper.getBoolean(R.string.pref_enable_chat_notifications, true);
    }

    /**
     * Returns the Uri to the notification sound set in the settings
     */
    private Uri getUserSelectedSoundUri() {

        final String selectedRingtoneUri = SharedPreferenceHelper
                .getString(R.string.pref_chat_ringtone);

        if (TextUtils.isEmpty(selectedRingtoneUri)) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        return Uri.parse(selectedRingtoneUri);
    }

    /**
     * Returns an instance of the {@link ChatNotificationHelper}
     *
     * @param context
     * @return
     */
    public static ChatNotificationHelper getInstance(Context context) {

        synchronized (LOCK) {
            if (sInstance == null) {
                synchronized (LOCK) {
                    sInstance = new ChatNotificationHelper(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * Sets the id of the user the current chat is being done with. Set this to the user id when the
     * chat detail screen opens, and clear it when the screen is paused. It is used to hide
     * notifications when the chat message received is from the user currently being chatted with
     *
     * @param currentChattingUserId The id of the current user being chatted with
     */
    public void setCurrentChattingUserId(final String currentChattingUserId) {
        mCurrentChattingUserId = currentChattingUserId;
    }

    /**
     * Sets whether the chat screen is currently open
     *
     * @param visible <code>true</code> to set chat screen opened, <code>false</code> to set it
     *                closed
     */
    public void setChatScreenVisible(final boolean visible) {
        mChatScreenVisible = visible;
    }

    /**
     * Cancels any notifications being displayed. Call this if the relevant screen is opened within
     * the app
     */
    public void clearChatNotifications() {

        mNotificationManager.cancel(MESSAGE_NOTIFICATION_ID);
        mUnreadMessageCount = 0;
    }

    /**
     * Displays a notification for a received chat message
     *
     * @param context
     * @param chatId      The ID of the chat. This is so that the right chat detail fragment can be
     *                    launched when the notification is tapped
     * @param withUserId  The id of the user who sent the notification
     * @param messageText The message body
     */
    public void showChatReceivedNotification(final Context context,
                                             final String chatId, final String withUserId
            , final String senderId, final String senderName, final String messageText
            , String withUserImage
    ) {

        //Don't show notifications if they have been disabled from settings
        if (!mNotificationsEnabled) {
            return;
        }

        //Don't show notification if we are currently chatting with the same user
        if (mCurrentChattingUserId != null
                && mCurrentChattingUserId.equals(withUserId)) {
            return;
        }

        if (mChatScreenVisible) {
            mUnreadMessageCount++;
            Intent resultIntent = null;

            if (mUnreadMessageCount == 1) {

                resultIntent = new Intent(context, ChatScreenActivity.class);
                mNotificationBuilder.setSmallIcon(R.drawable.ic_notification)
                        .setColor(context.getResources().getColor(R.color.primaryColor))
                        .setContentTitle(senderName)
                        .setContentText(messageText)
                        .setAutoCancel(true);
                resultIntent.setAction(AppConstants.ACTION_SHOW_CHAT_DETAIL);
                resultIntent.putExtra(AppConstants.Keys.CHAT_ID, chatId);
                resultIntent.putExtra(AppConstants.Keys.MY_ID, senderId);
                resultIntent.putExtra(AppConstants.Keys.USER_ID, withUserId);
                resultIntent.putExtra(AppConstants.Keys.PROFILE_IMAGE, withUserImage);
                resultIntent.putExtra(AppConstants.Keys.FROM_WALL, true);
                resultIntent.putExtra(AppConstants.Keys.CHAT_TITLE, senderName);
                resultIntent.putExtra(AppConstants.Keys.FROM_NOTIFICATIONS, true);


            } else {
                resultIntent = new Intent(context, HomeActivity.class);
                mNotificationBuilder
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(context.getResources().getColor(R.color.primaryColor))
                        .setContentTitle(
                                context.getString(R.string.new_messages, mUnreadMessageCount))
                        .setContentText(messageText)
                        .setAutoCancel(true);
                resultIntent.setAction(AppConstants.ACTION_SHOW_ALL_CHATS);
            }

            mNotificationBuilder.setSound(mNotificationSoundUri);
            if (mVibrationEnabled) {
                mNotificationBuilder.setVibrate(VIBRATION_PATTERN);
            } else {
                mNotificationBuilder.setVibrate(null);
            }
            final TaskStackBuilder taskStackBuilder = TaskStackBuilder
                    .create(context);
            taskStackBuilder.addNextIntent(resultIntent);
            final PendingIntent pendingIntent = taskStackBuilder
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotificationBuilder.setContentIntent(pendingIntent);
            mNotificationManager
                    .notify(MESSAGE_NOTIFICATION_ID, mNotificationBuilder
                            .build());
        }

    }

    /**
     * Gets a reference to the Shared Preferences listener to use for updating notifications
     * settings when it is changed
     */
    public SharedPreferences.OnSharedPreferenceChangeListener getOnSharedPreferenceChangeListener() {
        return onSharedPreferenceChangeListener;
    }


}
