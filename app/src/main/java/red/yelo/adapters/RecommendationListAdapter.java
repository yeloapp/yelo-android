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
 * Adapter used to display recommendations
 * Created by anshul1235 on 24/10/14.
 */

public class RecommendationListAdapter extends CursorAdapter {

    private static final String TAG = "RecommendationListAdapter";

    private Cursor  mRecommendationCursor;

    private RecommendationActionClickListener  mRecommendationActionsClickListener;

    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public RecommendationListAdapter(final Context context,RecommendationActionClickListener recommendationActionsClickListener) {
        super(context, null, 0);
        mRecommendationActionsClickListener = recommendationActionsClickListener;
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {
        UserSelectionHolder holder = (UserSelectionHolder) view.getTag();

        holder.listLayout.setTag(R.string.tag_position,cursor.getPosition());
        holder.listLayout.setOnClickListener(recommendationClickListener);

        String name = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.USER_NAME));

        String imageUrl = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.USER_IMAGE));

        String comment = cursor.getString(cursor.getColumnIndex(DatabaseColumns.COMMENT));
        holder.userName.setText(name);

        if (TextUtils.isEmpty(comment.trim())) {
            comment = name + " referred.";
        }
        holder.comment.setText(comment);

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
                .inflate(R.layout.layout_recommendations_list, parent, false);
        this.mRecommendationCursor = cursor;
        view.setTag(new UserSelectionHolder(view));
        return view;
    }

    private class UserSelectionHolder extends RecyclerView.ViewHolder {

        TextView userName, comment;
        OkulusImageView userImage;
        LinearLayout listLayout;

        public UserSelectionHolder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.username);
            comment = (TextView) itemView.findViewById(R.id.comment);
            userImage = (OkulusImageView) itemView.findViewById(R.id.image_user);
            listLayout = (LinearLayout) itemView.findViewById(R.id.list_layout);
        }
    }

    /**
     * Click listener for tag buttons
     */
    private final View.OnClickListener recommendationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mRecommendationActionsClickListener != null) {
//                mPostsCursor.moveToPosition(((Integer) ((ViewGroup) v).getChildAt(0).getTag(R.string.tag_position))-POSITION_MINUS_COUNT);
                mRecommendationCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position)));

                mRecommendationActionsClickListener.onRecommendationClicked(
                        v,
                        mRecommendationCursor.getString(mRecommendationCursor
                                .getColumnIndex(DatabaseColumns.WALL_ID)));
            }
        }
    };

    /**
     * Interface that receives events when the actions on list are clicked
     */
    public static interface RecommendationActionClickListener {

        /**
         * Method callback when the chat action is clicked
         *
         * @param view      The View that was clicked
         * @param wallId    The Wall id of the post
         */
        public void onRecommendationClicked(View view, String wallId);

    }


}
