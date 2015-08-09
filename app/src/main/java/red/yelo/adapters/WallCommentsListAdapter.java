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
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.vinaysshenoy.okulus.OkulusImageView;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.Utils;

/**
 * Adapter used to display wall comments
 * Created by anshul1235 on 17/07/14.
 */

public class WallCommentsListAdapter extends CursorAdapter {

    private static final String TAG = "WallCommentsListAdapter";

    /*
    listener to receive events on the fragment
     */
    private TagActionListener mTagActionListener;


    /**
     * @param context
     * @param tagActionListener listener to receive events on the main fragment
     */
    public WallCommentsListAdapter(final Context context, final TagActionListener tagActionListener) {
        super(context, null, 0);
        mTagActionListener = tagActionListener;
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {

        WallCommentHolder holder = (WallCommentHolder) view.getTag();

        holder.textTagUserName.setTag(R.string.tag_position, cursor.getPosition());
        holder.imageTagUsers.setTag(R.string.tag_position, cursor.getPosition());
        holder.textTagInfo.setTag(R.string.tag_position, cursor.getPosition());
        holder.dateStamp.setTag(R.string.tag_position, cursor.getPosition());
        holder.viewLayout.setTag(R.string.tag_position, cursor.getPosition());


        String comment = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.COMMENT));
        String taggedNames = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.TAGGED_NAMES));
        String userName = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.USER_NAME));

        String imageUrl = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.TAGGED_IMAGE_URLS));


        String tagInfo = "";


        //changing the view according to the comment type

        if (cursor.getString(cursor.getColumnIndex(DatabaseColumns.TYPE)).equals(AppConstants.CommentType.CHAT)) {
            tagInfo = "is chatting with " + userName;
            holder.textComment.setVisibility(View.GONE);
            holder.textTagInfo.setVisibility(View.VISIBLE);

        }
        else if(cursor.getString(cursor.getColumnIndex(DatabaseColumns.TYPE)).equals(AppConstants.CommentType.COMMENTS)){
            holder.textTagInfo.setVisibility(View.GONE);
            holder.textComment.setVisibility(View.VISIBLE);

        }
        else {
            tagInfo = "was referred by " + userName;
            holder.textTagInfo.setVisibility(View.VISIBLE);

        }

        if (!comment.equals("")) {
            holder.textComment.setText("\"" + comment + "\"");
            holder.textComment.setVisibility(View.VISIBLE);

        } else {
            holder.textComment.setText("");
            holder.textComment.setVisibility(View.GONE);
        }


        if (taggedNames.length() > 23) {
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            holder.textTagUserName.setLayoutParams(params);
        }
        else {
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            holder.textTagUserName.setLayoutParams(params);
        }
        holder.textTagUserName.setText(taggedNames);
        holder.textTagInfo.setText(tagInfo);

        if (!cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.TIMESTAMP_EPOCH)).equals("")) {
            final String dateStamp = Utils.getElapsedTimeFormat(Long.valueOf(cursor.getString(cursor
                    .getColumnIndex(DatabaseColumns.TIMESTAMP_EPOCH))), cursor, mContext);

            Spannable spannable = new SpannableString(" • " + dateStamp);
            TextAppearanceSpan textSpan = new TextAppearanceSpan(mContext, R.style.TextAppearance_Yelo_BulletStyle);
            spannable.setSpan(textSpan, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.dateStamp.setText(spannable, TextView.BufferType.NORMAL);
        } else {
            holder.dateStamp.setVisibility(View.GONE);
        }


        TextDrawable drawable = null;

        if (!TextUtils.isEmpty(taggedNames)) {
            ColorGenerator generator = ColorGenerator.DEFAULT;
            int color;
            if (!TextUtils.isEmpty(taggedNames)) {
                color = generator.getColor((taggedNames.charAt(0) + "").toUpperCase());
            } else {
                color = generator.getRandomColor();

            }
            Resources r = mContext.getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

            drawable = TextDrawable.builder()
                    .buildRoundRect((taggedNames.charAt(0) + "").toUpperCase(), color, Math.round(px));

        }


        if (!TextUtils.isEmpty(imageUrl) && !imageUrl.contains("assets/fallback/")) {
            Utils.loadCircularImage(context, (OkulusImageView) holder.imageTagUsers, imageUrl, AvatarBitmapTransformation.AvatarSize.NORMAL, drawable);
        } else {
            if (!TextUtils.isEmpty(taggedNames)) {

                Utils.loadCircularImage(context, (OkulusImageView) holder.imageTagUsers, "", AvatarBitmapTransformation.AvatarSize.NORMAL, drawable);
            } else {

                Utils.loadCircularImage(context, (OkulusImageView) holder.imageTagUsers, imageUrl, AvatarBitmapTransformation.AvatarSize.NORMAL, drawable);
            }
        }


        //set the listener for the view
        holder.viewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Cursor cursor = (Cursor) getItem((Integer) v.getTag(R.string.tag_position));


                boolean ownerProfile, isPresentFlag = false;

                if (AppConstants.UserInfo.INSTANCE.getId().equals(cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.WALL_USER_ID)))) {
                    ownerProfile = true;
                } else {
                    ownerProfile = false;
                }

                final String isPresent = cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.IS_PRESENT));

                if (isPresent.equals("false")) {
                    isPresentFlag = false;
                } else if (isPresent.equals("true")) {
                    isPresentFlag = true;
                }

                mTagActionListener.onWallCommentClicked(v, cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.TAGGED_NAMES)), cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.USER_NAME)), cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.TAGGED_USER_IDS)), cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.USER_ID)), ownerProfile,
                        cursor.getString(cursor.getColumnIndex(DatabaseColumns.TAGGED_USER_NUMBERS)),
                        cursor.getString(cursor.getColumnIndex(DatabaseColumns.TAGGED_IMAGE_URLS)),
                        isPresentFlag, cursor.getString(cursor.getColumnIndex(DatabaseColumns.TYPE)));
            }
        });

    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {

        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_wall_comments_list, parent, false);
        view.setTag(new WallCommentHolder(view));
        return view;
    }

    private class WallCommentHolder extends RecyclerView.ViewHolder {

        ImageView imageTagUsers;
        TextView textTagUserName;
        TextView textTagInfo;
        TextView textComment;
        TextView dateStamp;
        LinearLayout viewLayout;

        public WallCommentHolder(View itemView) {
            super(itemView);
            imageTagUsers = (ImageView) itemView.findViewById(R.id.image_tagged_users);
            textTagUserName = (TextView) itemView.findViewById(R.id.tagged_user_name);
            textTagInfo = (TextView) itemView.findViewById(R.id.tag_info);
            textComment = (TextView) itemView.findViewById(R.id.comment);
            textComment.setText("");
            dateStamp = (TextView) itemView.findViewById(R.id.date_stamp);
            viewLayout = (LinearLayout) itemView.findViewById(R.id.view_layout);

        }
    }

    /**
     * Interface that receives events when the actions on a tag options are clicked
     */
    public static interface TagActionListener {

        public void onShareClicked(View view, String userName, String category);

        public void onWallCommentClicked(View view, String user1Name, String user2Name, String user1Id, String user2Id,
                                         boolean isOwner, String number, String imageUrl, boolean isPresent,
                                         String type);


    }


}
