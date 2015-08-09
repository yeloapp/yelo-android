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
 */package red.yelo.adapters;


import android.content.Context;
import android.database.Cursor;
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
import red.yelo.utils.Utils;

/**
 * Adapter used to display claims
 * Created by anshul1235 on 11/03/15.
 */

public class ClaimsAdapter extends RecyclerView.Adapter<ClaimsAdapter.ClaimsViewHolder> {

    private static final String TAG = "ClaimsAdapter";


    private int mCount;

    /**
     * Cursor to the cursor
     */
    private Cursor mClaimsCursor;

    private Context mContext;


    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public ClaimsAdapter(final Context context) {
        this.mContext = context;

        mCount = 0;
    }


    @Override
    public int getItemCount() {
        return (mClaimsCursor != null && !mClaimsCursor.isClosed()) ? mClaimsCursor.getCount() : 0;
    }


    public static final class ClaimsViewHolder extends RecyclerView.ViewHolder {

        public final TextView serialNumber;
        public final TextView claimAmount;
        public final TextView status;
        public final TextView dateTime;

        public ClaimsViewHolder(final View view) {
            super(view);
            serialNumber = (TextView) view.findViewById(R.id.count);
            claimAmount = (TextView) view.findViewById(R.id.claim_amount);
            status = (TextView) view.findViewById(R.id.status);
            dateTime = (TextView) view.findViewById(R.id.updated_at);
        }
    }


    /**
     * Swaps out the cursor for a new one
     *
     * @param newCursor The new cursor to replace in the data set
     * @return The old cursor
     */
    public Cursor swapCursor(final Cursor newCursor) {

        final Cursor oldCursor = mClaimsCursor;
        mClaimsCursor = newCursor;
        notifyDataSetChanged();

        return oldCursor;
    }


    @Override
    public ClaimsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ClaimsViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_claim_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ClaimsViewHolder notificationViewHolder, int position) {
        mClaimsCursor.moveToPosition(position);

        notificationViewHolder.itemView.setTag(R.string.notification, position);

        notificationViewHolder.serialNumber.setTag(R.string.notification, position);
        notificationViewHolder.claimAmount.setTag(R.string.tag_notification, position);
        notificationViewHolder.status.setTag(R.string.notification, position);

        notificationViewHolder.serialNumber.setText(position + 1+".   ");


        notificationViewHolder.claimAmount.setText(" ₹"+mClaimsCursor.getString(mClaimsCursor
                .getColumnIndex(DatabaseColumns.AMOUNT))+"  ");


        notificationViewHolder.status.setText(mClaimsCursor.getString(mClaimsCursor
                .getColumnIndex(DatabaseColumns.STATUS)));

        final String timeString = getElapsedTimeFormat(Long.valueOf(mClaimsCursor.getString(mClaimsCursor
                .getColumnIndex(DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT))), mClaimsCursor, mContext);

        notificationViewHolder.dateTime.setText(timeString);


    }

    /**
     * This function returns the formatted time.
     *
     * @param timeEpoch
     * @param cursor
     * @param context
     * @return the formatted time
     */
    public static String getElapsedTimeFormat(long timeEpoch, Cursor cursor, Context context) {

        long timeElapsed = Utils.getCurrentEpochTime() - timeEpoch;

        int[] timeValues = Utils.getHoursMinsSecs(timeElapsed);

        final int hours = timeValues[0];
        final int minutes = timeValues[1];
        final int seconds = timeValues[2];
        final int days = hours / 24;
        final int weeks = days / 7;


        if (hours < 1) {
            if (minutes < 1) {
                if (seconds < 10) {
                    return context.getString(R.string.just_now);
                } else {
                    return context.getString(R.string.seconds_ago, seconds);
                }

            } else {
                return context.getString(R.string.minutes_ago, minutes);
            }
        } else if (hours < 23) {
            return context.getString(R.string.hours_ago, hours);

        } else if (hours > 23 && hours < 167) {

            return context.getString(R.string.days_ago, days);


        } else if (weeks > 0) {
            return context.getString(R.string.weeks_ago, weeks);
        } else {
            return cursor.getString(cursor
                    .getColumnIndex(DatabaseColumns.TIMESTAMP_HUMAN_UPDATED_AT));
        }
    }

}
