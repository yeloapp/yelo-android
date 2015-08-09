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
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.vinaysshenoy.okulus.OkulusImageView;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.Utils;

/**
 * Adapter used to display Wall Posts
 * Created by anshul1235 on 06/10/14.
 */

public class WallPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "WallPostAdapter";

    private Context mContext;

    private PostActionsClickListener mPostActionsClickListener;
    private View mHeaderView;

//    private static final int VIEW_TYPE_HEADER = 0;
//    private static final int VIEW_TYPE_ITEM = 1;
//
//    private static final int POSITION_MINUS_COUNT = 0;


    public static final String TAG_CHAT_STRING = "%s referrals  %s chats";
    public static final String TAG_CHAT_STRING_REFERAL = "%s referral  %s chats";
    public static final String TAG_CHAT_STRING_CHAT = "%s referrals  %s chat";
    public static final String TAG_CHAT_STRING_REFERRAL_CHAT = "%s referral  %s chat";


    /**
     * Cursor to the posts
     */
    private Cursor mPostsCursor;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        if (viewType == VIEW_TYPE_HEADER) {
//            return new HeaderViewHolder(mHeaderView);
//        } else {
        return new WallPostViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_wall_posts, parent, false));
        //  }
    }


    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View view) {
            super(view);
        }
    }


    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

        mPostActionsClickListener.onWallViewCreated(((WallPostViewHolder) holder));
    }

    @Override
    public int getItemCount() {
//        if(mHeaderView!=null) {
//            return (mPostsCursor != null && !mPostsCursor.isClosed()) ? mPostsCursor.getCount()+POSITION_MINUS_COUNT : 0;
//        }
//        else {
        return (mPostsCursor != null && !mPostsCursor.isClosed()) ? mPostsCursor.getCount() : 0;
        //       }
    }

//    @Override
//    public int getItemViewType(int position) {
//       // return (position == 0) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
//        return VIEW_TYPE_ITEM;
//    }

    public static final class WallPostViewHolder extends RecyclerView.ViewHolder {

        public final OkulusImageView userImage;
        public final TextView userName;
        public final TextView postTime;
        public final TextView postMessage;
        public final TextView tagName;
        public final ImageView wallImage;
        // public final ImageView optionsImage;
        public final TextView chatButton;
        public final TextView tagButton;
        public final TextView commentButton;
        public final ProgressBar progressBar;
        public final TextView tagChatText;
        public final TextView locationText;

        public WallPostViewHolder(final View view) {
            super(view);
            userImage = (OkulusImageView) view.findViewById(R.id.image_user);
            userName = (TextView) view.findViewById(R.id.text_user_name);
            postTime = (TextView) view.findViewById(R.id.post_time);
            postMessage = (TextView) view.findViewById(R.id.text_post_message);
            tagName = (TextView) view.findViewById(R.id.tag_name);
            wallImage = (ImageView) view.findViewById(R.id.wall_image);
            // optionsImage = (ImageView) view.findViewById(R.id.options_image);
            chatButton = (TextView) view.findViewById(R.id.chat_button);
            tagButton = (TextView) view.findViewById(R.id.tag_button);
            progressBar = (ProgressBar) view.findViewById(R.id.progress_sync);
            tagChatText = (TextView) view.findViewById(R.id.wall_tag_chat_text);
            locationText = (TextView) view.findViewById(R.id.text_location);
            commentButton = (TextView) view.findViewById(R.id.comment_button);
        }
    }


    /**
     * @param context                  A reference to the {@link android.content.Context}
     * @param postActionsClickListener An implementation of {@link red.yelo.adapters.WallPostAdapter.PostActionsClickListener} to receive events when the wall actions are clicked
     */
    public WallPostAdapter(final Context context, final PostActionsClickListener postActionsClickListener, final View headerView) {

        mContext = context;
        mPostActionsClickListener = postActionsClickListener;
        mHeaderView = headerView;
    }

    /**
     * Swaps out the cursor for a new one
     *
     * @param newCursor The new cursor to replace in the data set
     * @return The old cursor
     */
    public Cursor swapCursor(final Cursor newCursor) {

        final Cursor oldCursor = mPostsCursor;
        mPostsCursor = newCursor;
        notifyDataSetChanged();
        return oldCursor;
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof WallPostViewHolder) {

            mPostsCursor.moveToPosition(position);


            WallPostViewHolder wallPostViewHolder = ((WallPostViewHolder) holder);
            wallPostViewHolder.itemView.setTag(R.string.tag_position, position);
            wallPostViewHolder.itemView.setOnClickListener(postClickListener);

            wallPostViewHolder.tagButton.setTag(R.string.tag_position, position);
            wallPostViewHolder.chatButton.setTag(R.string.tag_position, position);
            wallPostViewHolder.commentButton.setTag(R.string.tag_position,position);

            wallPostViewHolder.userImage.setTag(R.string.tag_position, position);
            wallPostViewHolder.userImage.setOnClickListener(userImageClickListener);

            wallPostViewHolder.userName.setText(mPostsCursor.getString(mPostsCursor
                    .getColumnIndex(DatabaseColumns.USER_NAME)));
            String tagName = mPostsCursor.getString(mPostsCursor
                    .getColumnIndex(DatabaseColumns.GROUP_NAME));

            if (tagName != null) {
                wallPostViewHolder.tagName.setText(tagName.toUpperCase());


                if (!TextUtils.isEmpty(mPostsCursor.getString(mPostsCursor
                        .getColumnIndex(DatabaseColumns.COLOR)))) {
                    wallPostViewHolder.tagName.setBackgroundColor(Color.parseColor(mPostsCursor.getString(mPostsCursor
                            .getColumnIndex(DatabaseColumns.COLOR))));
                }

            }

            final String timeString = Utils.getElapsedTimeFormat(Long.valueOf(mPostsCursor.getString(mPostsCursor
                    .getColumnIndex(DatabaseColumns.TIMESTAMP_EPOCH))), mPostsCursor, mContext);
            wallPostViewHolder.postTime.setText("" + timeString);

            wallPostViewHolder.postMessage.setText(mPostsCursor.getString(mPostsCursor
                    .getColumnIndex(DatabaseColumns.MESSAGE)));

            if (mPostsCursor.getString(mPostsCursor
                    .getColumnIndex(DatabaseColumns.ADDRESS)) == null) {

                wallPostViewHolder.locationText.setVisibility(View.INVISIBLE);
            } else {
                wallPostViewHolder.locationText.setVisibility(View.VISIBLE);
                wallPostViewHolder.locationText.setText(" at " + mPostsCursor.getString(mPostsCursor
                        .getColumnIndex(DatabaseColumns.ADDRESS)));
            }

            setLeftButton(wallPostViewHolder.chatButton, mPostsCursor);
            setRightButton(wallPostViewHolder.tagButton, mPostsCursor);
            ((View) wallPostViewHolder.commentButton.getParent()).setOnClickListener(commentClickListener);

            final String imageUrl = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.WALL_IMAGES));
            if (TextUtils.isEmpty(imageUrl)) {
                wallPostViewHolder.wallImage.setVisibility(View.GONE);
            } else {
                wallPostViewHolder.wallImage.setVisibility(View.VISIBLE);

                Glide.with(mContext)
                        .load(imageUrl)
                        .asBitmap()
                        .centerCrop()
                        .override(500, 300)
                        .animate(R.anim.fade_in)
                        .placeholder(R.color.snow_light)
                        .into(wallPostViewHolder.wallImage);

            }

            final String userImageUrl = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.USER_IMAGE));


            if (!TextUtils.isEmpty(userImageUrl) && !userImageUrl.contains("assets/fallback/")) {
                ColorGenerator generator = ColorGenerator.DEFAULT;
                int color = generator.getColor((mPostsCursor.getString(mPostsCursor
                        .getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase());
                Resources r = mContext.getResources();
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

                TextDrawable drawable = TextDrawable.builder()
                        .buildRoundRect((mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase(), color, Math.round(px));

                Utils.loadCircularImage(mContext, wallPostViewHolder.userImage, userImageUrl, AvatarBitmapTransformation.AvatarSize.NORMAL, drawable);
            } else {
                ColorGenerator generator = ColorGenerator.DEFAULT;

                int color = generator.getColor((mPostsCursor.getString(mPostsCursor
                        .getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase());

                Resources r = mContext.getResources();
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

                TextDrawable drawable = TextDrawable.builder()
                        .buildRoundRect((mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase(), color, Math.round(px));

                wallPostViewHolder.userImage.setImageDrawable(drawable);
            }


            if (mPostsCursor.getString(mPostsCursor
                    .getColumnIndex(DatabaseColumns.STATE)).equals(AppConstants.SyncStates.SYNCING + "")) {

                wallPostViewHolder.progressBar
                        .setVisibility(View.VISIBLE);
            } else {
                wallPostViewHolder.progressBar
                        .setVisibility(View.GONE);
            }


            int connectionCount = (Integer.parseInt(mPostsCursor.
                    getString(mPostsCursor.getColumnIndex(DatabaseColumns.TAG_USER_COUNT))) + Integer.parseInt(mPostsCursor.
                    getString(mPostsCursor.getColumnIndex(DatabaseColumns.CHAT_USER_COUNT))));

            int comments = (Integer.parseInt(mPostsCursor.
                    getString(mPostsCursor.getColumnIndex(DatabaseColumns.COMMENT_USER_COUNT))));

            if ((connectionCount == 0 || connectionCount > 1)&&(comments == 0 || comments > 1)) {
                wallPostViewHolder.tagChatText.setText(connectionCount + " connections "+comments+" comments");
            }
            else if((connectionCount == 1)&&(comments == 0 || comments > 1)){
                wallPostViewHolder.tagChatText.setText(connectionCount + " connection "+comments+" comments");

            }
            else if((connectionCount == 0 || connectionCount > 1)&&(comments ==1)){
                wallPostViewHolder.tagChatText.setText(connectionCount + " connections "+comments+" comment");

            }
            else if (connectionCount == 1&&comments == 1) {
                wallPostViewHolder.tagChatText.setText(connectionCount + " connection "+comments+" comment");

            }


        }
    }

    /**
     * Sets the text for the right button
     *
     * @param textView The TextView to set
     * @param cursor   The cursor forwarded to the post which is being binded
     */
    private void setRightButton(final TextView textView, final Cursor cursor) {

        final String totalTaggedUsers = cursor.getString(cursor.getColumnIndex(DatabaseColumns.TAG_USER_COUNT));

        textView.setText(R.string.tag);
        ((View) textView.getParent()).setOnClickListener(tagButtonClickListener);

    }

    /**
     * Sets the text for the left button as it can change based on whether the user is the current user or not
     *
     * @param textView The TextView to set
     * @param cursor   The cursor forwarded to the post which is being binded
     */
    private void setLeftButton(final TextView textView, final Cursor cursor) {

        final String userId = cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_ID));

        if (Utils.isCurrentUser(userId)) {
            textView.setText(R.string.close);
            Utils.setCompoundDrawables(textView, R.drawable.ic_done_grey600_18dp, 0, 0, 0);
            ((View) textView.getParent()).setOnClickListener(closeButtonClickListener);
        } else {
            final String totalChattingUsers = cursor.getString(cursor
                    .getColumnIndex(DatabaseColumns.CHAT_USER_COUNT));

            textView.setText(R.string.chat);
            Utils.setCompoundDrawables(textView, R.drawable.ic_chat_50, 0, 0, 0);
            ((View) textView.getParent()).setOnClickListener(chatButtonClickListener);
        }
    }


    /**
     * Click listener for chat buttons
     */
    private final View.OnClickListener chatButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mPostActionsClickListener != null) {
//                mPostsCursor.moveToPosition(((Integer) ((ViewGroup) v).getChildAt(0).getTag(R.string.tag_position))-POSITION_MINUS_COUNT);

                mPostsCursor.moveToPosition(((Integer) ((ViewGroup) v).getChildAt(0).getTag(R.string.tag_position)));

                mPostActionsClickListener.onChatClicked(
                        v,
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.USER_ID)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.USER_NAME)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.USER_IMAGE)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.ID)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.TAG_NAME)));
            }
        }
    };

    private final View.OnClickListener postClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mPostActionsClickListener != null) {

//                mPostsCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position))-POSITION_MINUS_COUNT);
                mPostsCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position)));
                mPostActionsClickListener.onPostClicked(
                        v,
                        mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.ID)),
                        mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.TAG_USER_COUNT)));
            }
        }
    };


    private final View.OnClickListener commentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mPostActionsClickListener != null) {

//                mPostsCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position))-POSITION_MINUS_COUNT);
                mPostsCursor.moveToPosition(((Integer) ((ViewGroup) v).getChildAt(0).getTag(R.string.tag_position)));
                mPostActionsClickListener.onCommentClicked(
                        v,
                        mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.ID)),
                        mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.TAG_USER_COUNT)));
            }
        }
    };


    private final View.OnClickListener userImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mPostActionsClickListener != null) {

//                mPostsCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position))-POSITION_MINUS_COUNT);
                mPostsCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position)));
                mPostActionsClickListener.onWallProfileImageClicked(
                        v,
                        mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.USER_ID)),
                        mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.USER_NAME)));
            }
        }
    };


    /**
     * Click listener for tag buttons
     */
    private final View.OnClickListener tagButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mPostActionsClickListener != null) {
//                mPostsCursor.moveToPosition(((Integer) ((ViewGroup) v).getChildAt(0).getTag(R.string.tag_position))-POSITION_MINUS_COUNT);
                mPostsCursor.moveToPosition(((Integer) ((ViewGroup) v).getChildAt(0).getTag(R.string.tag_position)));

                mPostActionsClickListener.onTagClicked(
                        v,
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.ID)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.USER_ID)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.TAG_USER_COUNT)));
            }
        }
    };

    /**
     * Click listener for close buttons
     */
    private final View.OnClickListener closeButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mPostActionsClickListener != null) {
//                mPostsCursor.moveToPosition(((Integer) ((ViewGroup) v).getChildAt(0).getTag(R.string.tag_position))-POSITION_MINUS_COUNT);
                mPostsCursor.moveToPosition(((Integer) ((ViewGroup) v).getChildAt(0).getTag(R.string.tag_position)));


                boolean usersPresent = false;
                if (mPostsCursor.getString(mPostsCursor
                        .getColumnIndex(DatabaseColumns.CHAT_USER_COUNT)).equals("0") && mPostsCursor.getString(mPostsCursor
                        .getColumnIndex(DatabaseColumns.TAG_USER_COUNT)).equals("0")) {
                    usersPresent = false;
                } else {
                    usersPresent = true;
                }

                mPostActionsClickListener.onCloseClicked(
                        v,
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.ID)), usersPresent);
            }
        }
    };

    /**
     * Click listener for options image
     */
    private final View.OnClickListener optionsButtonClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (mPostActionsClickListener != null) {

//                mPostsCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position))-POSITION_MINUS_COUNT);
                mPostsCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position)));

                mPostActionsClickListener.onWallOptionsClicked(v, mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.ID)),
                        mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.USER_ID)),
                        mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.TAG_ID)));
            }
        }
    };

    /**
     * Returns the Cursor forwarded to the right position, or {@code null} if it doesn't exist
     */
    public Cursor getItem(final int position) {

        if (mPostsCursor != null && !mPostsCursor.isClosed()) {
            mPostsCursor.moveToPosition(position);
            return mPostsCursor;
        }

        return null;
    }

    /**
     * Interface that receives events when the actions on a post are clicked
     */
    public static interface PostActionsClickListener {

        /**
         * Method callback when the chat action is clicked
         *
         * @param view      The View that was clicked
         * @param userId    The user id of the post author
         * @param userName  The name of the user
         * @param userImage The image url of the user
         */
        public void onChatClicked(View view, String userId, String userName, String userImage, String wallId, String tagName);

        /**
         * Method callback when the post itself is clicked
         *
         * @param view     The View that was clicked
         * @param wallId   The id of the post
         * @param tagCount the cout of tagged users
         */
        public void onCommentClicked(View view, String wallId, String tagCount);
        /**
         * Method callback when the tag action is clicked
         *
         * @param view     The View that was clicked
         * @param wallId   The id of the post
         * @param userId   The user id of the post author
         * @param tagCount the cout of tagged users
         */
        public void onTagClicked(View view, String wallId, String userId, String tagCount);

        /**
         * Method callback when the option for closing a post is clicked
         *
         * @param view   The View that was clicked
         * @param wallId The id of the post
         */
        public void onCloseClicked(View view, String wallId, boolean usersPresent);

        /**
         * Method callback when the options on a Wall post is clicked
         *
         * @param view   The View that was clicked
         * @param wallId The id of the post
         * @param userId The user id of the post author
         */
        public void onWallOptionsClicked(View view, String wallId, String userId, String selectedTagId);

        /**
         * Method callback when the post itself is clicked
         *
         * @param view     The View that was clicked
         * @param wallId   The id of the post
         * @param tagCount the cout of tagged users
         */
        public void onPostClicked(View view, String wallId, String tagCount);


        /**
         * Method callback when the userImage itself is clicked
         *
         * @param view     The View that was clicked
         * @param userId   The id of the user
         * @param userName the name of the user
         */
        public void onWallProfileImageClicked(View view, String userId, String userName);

        /**
         * Method callback when the view is created
         *
         * @param viewHolder The View that was clicked
         */
        public void onWallViewCreated(WallPostViewHolder viewHolder);
    }


}
