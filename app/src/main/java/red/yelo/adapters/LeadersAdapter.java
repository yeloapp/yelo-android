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
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.vinaysshenoy.okulus.OkulusImageView;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.Utils;

/**
 * Adapter used to display weekly leaders
 * Created by anshul1235 on 12/03/15.
 */

public class LeadersAdapter extends CursorAdapter {

    private static final String TAG = "LeadersAdapter";

    private Cursor mLeadersCursor;

    private LeadersActionClickListener mLeadersActionClickListener;

    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public LeadersAdapter(final Context context, LeadersActionClickListener leadersActionClickListener) {
        super(context, null, 0);
        mLeadersActionClickListener = leadersActionClickListener;
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {
        UserSelectionHolder holder = (UserSelectionHolder) view.getTag();

        mLeadersCursor = cursor;
        holder.listLayout.setTag(R.string.tag_position, cursor.getPosition());
        holder.listLayout.setOnClickListener(leaderClickListener);

        String name = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.USER_NAME));

        String imageUrl = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.USER_IMAGE));

        String referralCount = cursor.getString(cursor.getColumnIndex(DatabaseColumns.REFERRAL_COUNT));

        holder.userName.setText(name);

        if (Integer.parseInt(referralCount) == 1) {
            holder.referralCount.setText("Referred " + referralCount + " contact this week");
        } else {
            holder.referralCount.setText("Referred " + referralCount + " contacts this week");
        }


        TextDrawable drawable = null;

        if (!TextUtils.isEmpty(name)) {
            ColorGenerator generator = ColorGenerator.DEFAULT;
            int color;
            if (!TextUtils.isEmpty(name)) {
                color = generator.getColor((name.charAt(0) + "").toUpperCase());
            } else {
                color = generator.getRandomColor();

            }
            Resources r = mContext.getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

            drawable = TextDrawable.builder()
                    .buildRoundRect((name.charAt(0) + "").toUpperCase(), color, Math.round(px));

        }

        if (!TextUtils.isEmpty(imageUrl) && !imageUrl.contains("assets/fallback/")) {
            Utils.loadCircularImage(context, holder.userImage, imageUrl, AvatarBitmapTransformation.AvatarSize.NORMAL, drawable);
        } else {
            Utils.loadCircularImage(context, holder.userImage, "", AvatarBitmapTransformation.AvatarSize.NORMAL, drawable);

        }


    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_leaders_list, parent, false);
        this.mLeadersCursor = cursor;
        view.setTag(new UserSelectionHolder(view));
        return view;
    }

    private class UserSelectionHolder extends RecyclerView.ViewHolder {

        TextView userName, referralCount;
        OkulusImageView userImage;
        LinearLayout listLayout;

        public UserSelectionHolder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.username);
            referralCount = (TextView) itemView.findViewById(R.id.referral_count);
            userImage = (OkulusImageView) itemView.findViewById(R.id.image_user);
            listLayout = (LinearLayout) itemView.findViewById(R.id.list_layout);
        }
    }


    /**
     * Click listener for tag buttons
     */
    private final View.OnClickListener leaderClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mLeadersActionClickListener != null) {
//                mPostsCursor.moveToPosition(((Integer) ((ViewGroup) v).getChildAt(0).getTag(R.string.tag_position))-POSITION_MINUS_COUNT);
                mLeadersCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position)));

                mLeadersActionClickListener.onLeaderClicked(
                        v,
                        mLeadersCursor.getString(mLeadersCursor
                                .getColumnIndex(DatabaseColumns.USER_ID)),
                        mLeadersCursor.getString(mLeadersCursor
                                .getColumnIndex(DatabaseColumns.USER_NAME)));
            }
        }
    };

    /**
     * Interface that receives events when the actions on list are clicked
     */
    public static interface LeadersActionClickListener {

        /**
         * Method callback when the leaders are clicked
         *
         * @param view     The View that was clicked
         * @param userId   The User id for the list
         * @param userName The User name for the list
         */
        public void onLeaderClicked(View view, String userId, String userName);

    }


}
