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
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.vinaysshenoy.okulus.OkulusImageView;

import red.yelo.R;
import red.yelo.activities.CreateServiceCardActivity;
import red.yelo.data.DatabaseColumns;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.Utils;

/**
 * Adapter used to display Service Cards
 * Created by anshul1235 on 19/03/15.
 */

public class ServiceCardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ServiceCardsAdapter";

    private Context mContext;

    private CardActionsClickListener mCardActionsClickListener;

    private boolean mAddGraphic;

    /**
     * Cursor to the cards
     */
    private Cursor mPostsCursor;

    public static final int VIEW_TYPE_GRAPHIC = 1;
    public static final int VIEW_TYPE_CARD = 2;

    private int mCount = 0;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        if (viewType == VIEW_TYPE_GRAPHIC) {

            return new FooterViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_item_add_graphic_service, parent, false));
        } else {
        return new ServiceCardViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_service_card, parent, false));
          }
    }


    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);

    }

    @Override
    public int getItemViewType(int position) {
        if((position == mCount-1)&&mAddGraphic) {
            return VIEW_TYPE_GRAPHIC;
        }
        else {
            return VIEW_TYPE_CARD;
        }
    }
    @Override
    public int getItemCount() {
        if(!mAddGraphic) {

            mCount = (mPostsCursor != null && !mPostsCursor.isClosed()) ? mPostsCursor.getCount() : 0;
            return mCount;
        }
        else {
            mCount = (mPostsCursor != null && !mPostsCursor.isClosed()) ? mPostsCursor.getCount() + 1 : 1;
            return mCount;

        }
    }

    public static final class ServiceCardViewHolder extends RecyclerView.ViewHolder {

        public final ImageView serviceImage;
        public final TextView titleText;
        public final TextView descriptionText;
        public final TextView priceText;
        public final TextView tagName;
        public final TextView ratingCount;
        public final RatingBar ratingBar;
        public final OkulusImageView imageUser;
        public final TextView userName;
        public final TextView viewCount;
        public final TextView bookCount;



        public ServiceCardViewHolder(final View view) {
            super(view);
            serviceImage = (ImageView) view.findViewById(R.id.service_card_image);
            titleText = (TextView) view.findViewById(R.id.service_card_title);
            descriptionText = (TextView) view.findViewById(R.id.description_text);
            priceText = (TextView) view.findViewById(R.id.price_service);
            tagName = (TextView) view.findViewById(R.id.tag_name);
            ratingCount = (TextView) view.findViewById(R.id.rating_count);
            ratingBar = (RatingBar) view.findViewById(R.id.ratingBar2);
            userName = (TextView) view.findViewById(R.id.text_user_name);
            imageUser = (OkulusImageView) view.findViewById(R.id.image_user);
            viewCount = (TextView) view.findViewById(R.id.view_count);
            bookCount = (TextView) view.findViewById(R.id.book_count);



        }
    }

    public static final class FooterViewHolder extends RecyclerView.ViewHolder {

        public final FrameLayout layoutFrame;


        public FooterViewHolder(final View view) {
            super(view);

            layoutFrame = (FrameLayout) view.findViewById(R.id.layout_frame);
        }
    }


    /**
     * @param context                  A reference to the {@link Context}
     * @param cardActionsClickListener An implementation of {@link red.yelo.adapters.ServiceCardsAdapter.CardActionsClickListener} to receive events when the card actions are clicked
     */
    public ServiceCardsAdapter(final Context context, final CardActionsClickListener cardActionsClickListener,
                               final View headerView,final boolean isUserOwner) {

        mContext = context;
        mCardActionsClickListener = cardActionsClickListener;
        mAddGraphic = isUserOwner;
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

        if((position==mCount-1)&&mAddGraphic) {
            FooterViewHolder footerViewHolder = ((FooterViewHolder) holder);
            footerViewHolder.layoutFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent createServiceCard = new Intent(mContext, CreateServiceCardActivity.class);
                    createServiceCard.putExtra(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE.getId());

                    mContext.startActivity(createServiceCard);
                }
            });
        }
        else {
            if (holder instanceof ServiceCardViewHolder) {

                mPostsCursor.moveToPosition(position);


                ServiceCardViewHolder serviceCardViewHolder = ((ServiceCardViewHolder) holder);
                serviceCardViewHolder.itemView.setTag(R.string.tag_position, position);
                serviceCardViewHolder.titleText.setTag(R.string.tag_position, position);
                serviceCardViewHolder.descriptionText.setTag(R.string.tag_position, position);
                serviceCardViewHolder.priceText.setTag(R.string.tag_position, position);
                serviceCardViewHolder.tagName.setTag(R.string.tag_position, position);



                final String imageUrl = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.SERVICE_IMAGE));

                final String title = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.TITLE));
                final String description = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.SERVICE_DESCRIPTION));
                final String price = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.SERVICE_PRICE));
                final String tagName = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.GROUP_NAME));
                final String color = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.COLOR));
                final String averageRating = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.RATING));
                final String ratingCount = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.RATING_COUNT));
                final String userImage = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.USER_IMAGE));
                final String userName = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.USER_NAME));
                final String viewCount = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.VIEW_COUNT));
                final String bookCount = mPostsCursor.getString(mPostsCursor.getColumnIndex(DatabaseColumns.BOOK_COUNT));





                serviceCardViewHolder.serviceImage.setVisibility(View.VISIBLE);

                serviceCardViewHolder.titleText.setText(title);
                serviceCardViewHolder.descriptionText.setText(description);
                if(price.equals("0")){
                    serviceCardViewHolder.priceText.setText("Cup of Coffee");
                    serviceCardViewHolder.priceText.setTextColor(mContext.getResources().getColor(R.color.coffee_color));

                }
                else {
                    serviceCardViewHolder.priceText.setText("\u20B9" + price);
                    serviceCardViewHolder.priceText.setTextColor(mContext.getResources().getColor(R.color.grey));

                }
                serviceCardViewHolder.tagName.setText(tagName);
                serviceCardViewHolder.tagName.setBackgroundColor(Color.parseColor(color));

                serviceCardViewHolder.userName.setText(userName);

                serviceCardViewHolder.viewCount.setText(viewCount);
                serviceCardViewHolder.bookCount.setText(bookCount);



                serviceCardViewHolder.ratingCount.setText("(" + ratingCount + ")");
                if(!TextUtils.isEmpty(averageRating))
                serviceCardViewHolder.ratingBar.setRating(Float.parseFloat(averageRating));



                Glide.with(mContext)
                        .load(imageUrl)
                        .asBitmap()
                        .centerCrop()
                        .animate(R.anim.fade_in)
                        .placeholder(R.color.snow_light)
                        .into(serviceCardViewHolder.serviceImage);


                if (!TextUtils.isEmpty(userImage) && !userImage.contains("assets/fallback/")) {
                    ColorGenerator generator = ColorGenerator.DEFAULT;
                    int color1 = generator.getColor((mPostsCursor.getString(mPostsCursor
                            .getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase());
                    Resources r = mContext.getResources();
                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

                    TextDrawable drawable = TextDrawable.builder()
                            .buildRoundRect((mPostsCursor.getString(mPostsCursor
                                    .getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase(), color1, Math.round(px));

                    Utils.loadCircularImage(mContext, serviceCardViewHolder.imageUser, userImage, AvatarBitmapTransformation.AvatarSize.SMALL, drawable);
                } else {
                    ColorGenerator generator = ColorGenerator.DEFAULT;

                    int color2 = generator.getColor((mPostsCursor.getString(mPostsCursor
                            .getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase());

                    Resources r = mContext.getResources();
                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

                    TextDrawable drawable = TextDrawable.builder()
                            .buildRoundRect((mPostsCursor.getString(mPostsCursor
                                    .getColumnIndex(DatabaseColumns.USER_NAME)).charAt(0) + "").toUpperCase(), color2, Math.round(px));

                    serviceCardViewHolder.imageUser.setImageDrawable(drawable);
                }



                serviceCardViewHolder.itemView.setOnClickListener(cardClickListener);


            }
        }
    }


    /**
     * Click listener for chat buttons
     */
    private final View.OnClickListener bookButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mCardActionsClickListener != null) {
//                mPostsCursor.moveToPosition(((Integer) ((ViewGroup) v).getChildAt(0).getTag(R.string.tag_position))-POSITION_MINUS_COUNT);

                mPostsCursor.moveToPosition(((Integer) ((Button) v).getTag(R.string.tag_position)));

                mCardActionsClickListener.onBookClicked(
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
                                .getColumnIndex(DatabaseColumns.SUBGROUP_NAME)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.USER_NUMBER)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.SERVICE_PRICE)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.TITLE)));
            }
        }
    };

    private final View.OnClickListener cardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mCardActionsClickListener != null) {

//                mPostsCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position))-POSITION_MINUS_COUNT);
                mPostsCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position)));
                mCardActionsClickListener.onPostClicked(
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
                                .getColumnIndex(DatabaseColumns.SUBGROUP_NAME)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.USER_NUMBER)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.SERVICE_PRICE)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.TITLE)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.SERVICE_IMAGE)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.SERVICE_DESCRIPTION)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.GROUP_NAME)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.DURATION)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.DELIVERABLE)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.GROUP_ID)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.SUBGROUP_ID)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.RATING)),
                        mPostsCursor.getString(mPostsCursor
                                .getColumnIndex(DatabaseColumns.RATING_COUNT)));
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
     * Interface that receives events when the actions on a card are clicked
     */
    public static interface CardActionsClickListener {

        /**
         * Method callback when the chat action is clicked
         *
         * @param view          The View that was clicked
         * @param userId        The user id of the post author
         * @param userName      The name of the user
         * @param userImage     The image url of the user
         * @param contactNumber The contact number of the user who own the service card
         */
        public void onBookClicked(View view, String userId, String userName, String userImage,
                                  String cardId, String tagName, String contactNumber,String price,
                                  String title);


        /**
         * Method callback when the post itself is clicked
         *
         * @param view          The View that was clicked
         * @param userId        The user id of the post author
         * @param userName      The name of the user
         * @param userImage     The image url of the user
         * @param contactNumber The contact number of the user who own the service card
         */
        public void onPostClicked(View view, String userId, String userName, String userImage,
                                  String cardId, String tagName, String contactNumber,String price,
                                  String title,String serviceImage,String description,String groupName,
                                  String duration,String deliverable,String groupId,String subCtegoryId,
                                  String rating,String ratingCount
                                  );


    }


}
