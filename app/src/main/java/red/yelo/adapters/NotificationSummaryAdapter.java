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

package red.yelo.adapters;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.BaseColumns;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;
import red.yelo.fragments.NotificationSummaryFragment;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;

/**
 * Adapter used to display notifications
 * Created by anshul1235 on 17/07/14.
 */

public class NotificationSummaryAdapter extends RecyclerView.Adapter<NotificationSummaryAdapter.NotificationViewHolder> {

    private static final String TAG = "NotificationSummaryAdapter";


    private int mCount;

    private ViewClickListener mViewClickListener;

    /**
     * Cursor to the notifications
     */
    private Cursor mNotificationCursor;

    private Context mContext;

    private HashMap<String, String> mGroupColors = new HashMap<String, String>();


    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public NotificationSummaryAdapter(final Context context,  final ViewClickListener viewClickListener) {
        this.mContext = context;
        this.mViewClickListener = viewClickListener;

        mCount = 0;
    }


    @Override
    public int getItemCount() {
        return (mNotificationCursor != null && !mNotificationCursor.isClosed()) ? mNotificationCursor.getCount() : 0;
    }


    public static final class NotificationViewHolder extends RecyclerView.ViewHolder {

        public final TextView notificationMessage;
        public final TextView notificationTag;
        public final LinearLayout notificationLayout;

        public NotificationViewHolder(final View view) {
            super(view);
            notificationMessage = (TextView) view.findViewById(R.id.notification_message);
            notificationTag = (TextView) view.findViewById(R.id.tag);
            notificationLayout = (LinearLayout) view.findViewById(R.id.notification_layout);
        }
    }


    /**
     * Swaps out the cursor for a new one
     *
     * @param newCursor The new cursor to replace in the data set
     * @return The old cursor
     */
    public Cursor swapCursor(final Cursor newCursor) {

        final Cursor oldCursor = mNotificationCursor;
        mNotificationCursor = newCursor;
        notifyDataSetChanged();

        mGroupColors = ((NotificationSummaryFragment)mViewClickListener).getGroupColors();

        return oldCursor;
    }


    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NotificationViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_notification_list, parent, false));
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder notificationViewHolder, int position) {
        mNotificationCursor.moveToPosition(position);

        notificationViewHolder.itemView.setTag(R.string.notification, position);
        notificationViewHolder.itemView.setOnClickListener(mNotificationClickListener);

        notificationViewHolder.notificationMessage.setTag(R.string.notification, position);
        notificationViewHolder.notificationTag.setTag(R.string.tag_notification, position);
        notificationViewHolder.notificationLayout.setTag(R.string.notification, position);

        notificationViewHolder.notificationMessage.setText(mNotificationCursor.getString(mNotificationCursor
                .getColumnIndex(DatabaseColumns.MESSAGE)));

        if (!TextUtils.isEmpty(mNotificationCursor.getString(mNotificationCursor
                .getColumnIndex(DatabaseColumns.TAGS)))) {

            notificationViewHolder.notificationTag.setVisibility(View.VISIBLE);
            notificationViewHolder.notificationTag.setText(mNotificationCursor.getString(mNotificationCursor
                    .getColumnIndex(DatabaseColumns.TAGS)).toUpperCase());


        } else {
            notificationViewHolder.notificationTag.setVisibility(View.GONE);
        }

        if (mNotificationCursor.getString(mNotificationCursor
                .getColumnIndex(DatabaseColumns.NOTIFICATION_STATUS)).equals(AppConstants.NotificationStatus.READ)) {
            notificationViewHolder.notificationLayout.setBackgroundColor(mContext.getResources().getColor(R.color.primaryColorLight));
        } else {
            notificationViewHolder.notificationLayout.setBackgroundColor(mContext.getResources().getColor(R.color.white));

        }


    }


    private final View.OnClickListener mNotificationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mViewClickListener != null) {

                mNotificationCursor.moveToPosition((Integer) v.getTag(R.string.notification));
                mViewClickListener.onPostClicked(
                        v,
                        mNotificationCursor.getString(mNotificationCursor.getColumnIndex(BaseColumns._ID)),
                        mNotificationCursor.getString(mNotificationCursor.getColumnIndex(DatabaseColumns.WALL_ID)),
                        mNotificationCursor.getString(mNotificationCursor.getColumnIndex(DatabaseColumns.KEY)));
            }
        }
    };

    /**
     * Interface that receives events when the actions on a post are clicked
     */
    public static interface ViewClickListener {


        public void onPostClicked(View view, String notificationId, String wallId, String key);
    }

}
