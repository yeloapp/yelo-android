

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
 */package red.yelo.gcm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.activities.HomeActivity;
import red.yelo.activities.WallPostActivity;
import red.yelo.chat.ChatService;
import red.yelo.data.DBInterface;
import red.yelo.data.DatabaseColumns;
import red.yelo.data.TableNotifications;
import red.yelo.http.HttpConstants;
import red.yelo.http.JsonUtils;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;
import red.yelo.utils.SharedPreferenceHelper;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService implements DBInterface.AsyncDbQueryCallback {

    public static final int NOTIFICATION_ID = 2;
    private NotificationManager mNotificationManager;


    public GcmIntentService() {
        super("GcmIntentService");
    }

    /**
     * Vibration pattern for notifications
     */
    private static final long[] VIBRATION_PATTERN = new long[]{50, 250};

    public static final String TAG = "GcmIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);


        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                sendNotification(extras);
                Logger.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


    private void sendNotification(Bundle gcmBundle) {

        try {


            final JSONObject payload = new JSONObject(gcmBundle.getString(HttpConstants.PAYLOAD));

            final JSONObject resource = JsonUtils.readJSONObject(payload, HttpConstants.RESOURCE, false, false);

            final String collapseKey = JsonUtils
                    .readString(payload, HttpConstants.COLLAPSE_KEY, false, false);

            final String message = JsonUtils
                    .readString(payload, HttpConstants.MESSAGE, false, false);

            final String name = JsonUtils
                    .readString(resource, HttpConstants.NAME, false, false);

            JSONObject destObject = null;
            if (!collapseKey.equals(AppConstants.CollapseKey.ALERT)) {
                destObject = JsonUtils
                        .readJSONObject(resource, HttpConstants.DEST, false, false);
            }


            String tag = "";
            String wallId = "";
            if (destObject != null) {
                tag = JsonUtils
                        .readString(destObject, HttpConstants.TAG, false, false);

                wallId = JsonUtils
                        .readString(destObject, HttpConstants.WALL_ID, false, false);
            }

            if (!collapseKey.equals(AppConstants.CollapseKey.ALERT)) {
                ContentValues values = new ContentValues();
                values.put(DatabaseColumns.WALL_ID, wallId);
                values.put(DatabaseColumns.KEY, collapseKey);
                values.put(DatabaseColumns.MESSAGE, message);
                values.put(DatabaseColumns.NAME, name);
                values.put(DatabaseColumns.TAGS, tag);
                values.put(DatabaseColumns.NOTIFICATION_STATUS, AppConstants.NotificationStatus.UNREAD_NOT_OPENED);

                if (!collapseKey.equals(AppConstants.CollapseKey.WALL)) {
                    DBInterface.insertAsync(AppConstants.QueryTokens.INSERT_NOTIFICATIONS, null, null, TableNotifications.NAME,
                            null, values, true, this);
                }
                if (getNotificationsEnabled()) {

                    mNotificationManager = (NotificationManager)
                            this.getSystemService(Context.NOTIFICATION_SERVICE);

                    PendingIntent contentIntent = null;
                    Intent wallIntent = null;

                    if (collapseKey.equals(AppConstants.CollapseKey.WALL)) {

                        wallIntent = new Intent(this, WallPostActivity.class);
                        wallIntent.putExtra(AppConstants.Keys.ID, wallId);
                        wallIntent.putExtra(AppConstants.Keys.FROM_NOTIFICATIONS, true);
                        wallIntent.putExtra(AppConstants.Keys.NOTIFICATION_ID, message);

                    } else if (collapseKey.equals(AppConstants.CollapseKey.SUMMARY)) {

                        wallIntent = new Intent(this, HomeActivity.class);
                        wallIntent.putExtra(AppConstants.Keys.FROM_NOTIFICATIONS, true);


                    } else if (collapseKey.equals(AppConstants.CollapseKey.TAG)) {

                        wallIntent = new Intent(this, WallPostActivity.class);
                        wallIntent.putExtra(AppConstants.Keys.ID, wallId);
                        wallIntent.putExtra(AppConstants.Keys.FROM_NOTIFICATIONS, true);
                        wallIntent.putExtra(AppConstants.Keys.NOTIFICATION_ID, message);


                    } else if (collapseKey.equals(AppConstants.CollapseKey.PIN)) {

                        wallIntent = new Intent(this, WallPostActivity.class);
                        wallIntent.putExtra(AppConstants.Keys.ID, wallId);
                        wallIntent.putExtra(AppConstants.Keys.FROM_NOTIFICATIONS, true);
                        wallIntent.putExtra(AppConstants.Keys.NOTIFICATION_ID, message);

                    } else if (collapseKey.equals(AppConstants.CollapseKey.CONTACT_WALL)) {

                        wallIntent = new Intent(this, WallPostActivity.class);
                        wallIntent.putExtra(AppConstants.Keys.ID, wallId);
                        wallIntent.putExtra(AppConstants.Keys.FROM_NOTIFICATIONS, true);
                        wallIntent.putExtra(AppConstants.Keys.NOTIFICATION_ID, message);

                    }

                    else if (collapseKey.equals(AppConstants.CollapseKey.COMMENT)) {

                        wallIntent = new Intent(this, WallPostActivity.class);
                        wallIntent.putExtra(AppConstants.Keys.ID, wallId);
                        wallIntent.putExtra(AppConstants.Keys.FROM_NOTIFICATIONS, true);
                        wallIntent.putExtra(AppConstants.Keys.NOTIFICATION_ID, message);

                    }

                    final TaskStackBuilder taskStackBuilder = TaskStackBuilder
                            .create(this);
                    taskStackBuilder.addNextIntent(wallIntent);
                    contentIntent = taskStackBuilder
                            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setColor(getResources().getColor(R.color.primaryColor))
                                    .setContentTitle(name)
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(message))
                                    .setAutoCancel(true)
                                    .setContentText(message)
                                    .setVibrate(getVibrationEnabled() ? VIBRATION_PATTERN : null)
                                    .setSound(getUserSelectedSoundUri());

                    mBuilder.setContentIntent(contentIntent);

                    if (isVerified()) {
                        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                    }
                }
            } else {
                if (name.equals(HttpConstants.CHAT_ALERT)) {
                    if (!AppConstants.UserInfo.INSTANCE.getAuthToken().equals("")) {

                        ((YeloApplication) YeloApplication.getStaticContext()).getYeloApi().pingServer(new Callback<String>() {
                            @Override
                            public void success(String s, Response response) {

                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });

                        Logger.d(TAG, "STARTED CHAT SERVICE");
                        startChatService();
                    }
                }
            }

        } catch (JSONException e) {
            //should not happen
            e.printStackTrace();
        }
        catch (NullPointerException e){
            //should not happen, happens when i get empty gcms from the server
        }

    }

    /**
     * Start the chat service. The connection doesn't happen if the user isn't
     * logged in.
     */
    public static void startChatService() {


        final Intent intent = new Intent(YeloApplication.getStaticContext(), ChatService.class);
        intent.putExtra(AppConstants.Keys.HEART_BEAT, AppConstants.HEART_BEAT);
        YeloApplication.getStaticContext().startService(intent);
    }

    protected boolean isVerified() {
        return !TextUtils.isEmpty(AppConstants.UserInfo.INSTANCE.getAuthToken());
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int taskId, Object cookie, int deleteCount) {

    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }

    /**
     * Cancels any notifications being displayed. Call this if the relevant screen is opened within
     * the app
     */
    public void clearNotifications() {
        mNotificationManager.cancel(AppConstants.NOTIFICATION_ID_WALL);
    }

    /**
     * Returns whether the chat vibration is enabled or not
     */
    public boolean getVibrationEnabled() {
        return SharedPreferenceHelper.getBoolean(R.string.pref_enable_other_vibrate, true);
    }

    /**
     * Returns whether the chat notifications are enabled from settings or not
     */
    private boolean getNotificationsEnabled() {
        return SharedPreferenceHelper.getBoolean(R.string.pref_enable_other_notifications, true);
    }

    /**
     * Returns the Uri to the notification sound set in the settings
     */
    private Uri getUserSelectedSoundUri() {

        final String selectedRingtoneUri = SharedPreferenceHelper
                .getString(R.string.pref_other_ringtone);

        if (TextUtils.isEmpty(selectedRingtoneUri)) {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        return Uri.parse(selectedRingtoneUri);
    }
}
