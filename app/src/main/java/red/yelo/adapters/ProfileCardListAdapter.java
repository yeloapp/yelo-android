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
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;

/**
 * Adapter used to display profile cards
 * Created by anshul1235 on 24/10/14.
 */

public class ProfileCardListAdapter extends CursorAdapter {

    private static final String TAG = "ProfileCardListAdapter";

    private static final int VIEW_CARDS = 0;
    private static final int VIEW_GRAPHIC = 1;

    private boolean mAddGraphicEnabled,mEmptyCursor;

    private ProfileCardListener mProfileCardListener;


    private int mCount;

    /**
     * Cursor to the posts
     */
    private Cursor mCardCursor;


    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public ProfileCardListAdapter(final Context context, final boolean addGraphicEnabled,ProfileCardListener profileCardListener) {
        super(context, null, 0);
        mCount = 0;
        mAddGraphicEnabled = addGraphicEnabled;
        mProfileCardListener = profileCardListener;

    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {
        CollectionHolder holder = (CollectionHolder) view.getTag();

        String position = cursor.getPosition()+"";

        holder.heading.setTag(R.string.tag_position, position);
        holder.subHeading.setTag(R.string.tag_position, position);
        holder.referralCount.setTag(R.string.tag_position, position);
        holder.layout.setTag(R.string.tag_position, position);


        mCardCursor = cursor;
        String heading = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.GROUP_NAME));

        String subHeading = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.SUB_HEADING));

        String subName = cursor.getString(cursor.getColumnIndex(DatabaseColumns.SUBGROUP_NAME));

        String referralCount = cursor.getString(cursor.getColumnIndex(DatabaseColumns.REFERRAL_COUNT));


        if(TextUtils.isEmpty(subHeading)) {
            subHeading = subName.toUpperCase();
        }
        else {
            subHeading = subName.toUpperCase() + ": " + subHeading;
        }



        holder.heading.setText(heading);
        holder.subHeading.setText(subHeading);
        holder.referralCount.setText(referralCount + " referrals");
        holder.layout.setBackgroundColor(Color.parseColor(cursor.getString(cursor.getColumnIndex(DatabaseColumns.COLOR))));
        holder.layout.setOnClickListener(cardClickListener);



    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_profile_cards, parent, false);
        view.setTag(new CollectionHolder(view));
        return view;
    }

    private class CollectionHolder extends RecyclerView.ViewHolder {

        TextView heading, subHeading, referralCount;
        LinearLayout layout;

        public CollectionHolder(View itemView) {
            super(itemView);
            heading = (TextView) itemView.findViewById(R.id.text_card_heading);
            referralCount = (TextView) itemView.findViewById(R.id.text_card_referrals);
            subHeading = (TextView) itemView.findViewById(R.id.text_card_subsubheading);
            layout = (LinearLayout) itemView.findViewById(R.id.layout_card_list);
        }
    }



    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public void notifyDataSetChanged() {

        if ((mCursor == null) || mCursor.getCount() == 0) {
            if(mAddGraphicEnabled)
            mCount = 1;

        } else {
            mCount = mCursor.getCount() + (mAddGraphicEnabled ? 1 : 0);//Empty graphic
        }
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (mAddGraphicEnabled && (position == (mCount - 1))) ? VIEW_GRAPHIC
                : VIEW_CARDS;
    }

    @Override
    public int getViewTypeCount() {
        return mAddGraphicEnabled ? 2 : 1;
    }

    /**
     * @param parent
     * @return
     */
    private View inflateGraphicView(ViewGroup parent) {
        final View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_item_add_graphic, parent, false);
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int viewType = getItemViewType(position);
        View view = convertView;

        if (viewType == VIEW_CARDS) {

            if (view == null) {
                view = inflateCardView(parent);
            }

            mCursor.moveToPosition(position);
            bindView(view, parent.getContext(), mCursor);

        } else if (viewType == VIEW_GRAPHIC) {

            if (view == null) {
                view = inflateGraphicView(parent);
                view.setOnClickListener(cardCreateListener);
            }
        }

        return view;
    }


    /**
     * @param parent
     * @return
     */
    private View inflateCardView(ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_profile_cards, parent, false);

        view.setTag(new CollectionHolder(view));

        return view;
    }


    private final View.OnClickListener cardClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mProfileCardListener != null) {

//                mPostsCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position))-POSITION_MINUS_COUNT);
                mCardCursor.moveToPosition(Integer.parseInt( v.getTag(R.string.tag_position)+""));
                mProfileCardListener.onCardClicked(
                        v,
                        mCardCursor.getString(mCardCursor.getColumnIndex(DatabaseColumns.ID)),
                        mCardCursor.getString(mCardCursor.getColumnIndex(DatabaseColumns.USER_ID)),
                        mCardCursor.getString(mCardCursor.getColumnIndex(DatabaseColumns.GROUP_NAME)),
                        mCardCursor.getString(mCardCursor.getColumnIndex(DatabaseColumns.GROUP_ID)),
                        mCardCursor.getString(mCardCursor.getColumnIndex(DatabaseColumns.SUBGROUP_NAME)),
                        mCardCursor.getString(mCardCursor.getColumnIndex(DatabaseColumns.SUBGROUP_ID)),
                        mCardCursor.getString(mCardCursor.getColumnIndex(DatabaseColumns.COLOR))
                        );
            }
        }
    };


    private final View.OnClickListener cardCreateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mProfileCardListener != null) {

                mProfileCardListener.onCreateCard(
                        v
                );
            }
        }
    };



    /**
     * Interface that receives events when the actions on a profile card are clicked
     */
    public static interface ProfileCardListener {

        /**
         * Method callback when the chat action is clicked
         *
         * @param view      The View that was clicked
         * @param cardId    The id of the listing
         * @param userId    The user id
         * @param groupName The name of the group that card belongs to
         * @param groupId   The id of the group
         */
        public void onCardClicked(View view,String cardId, String userId, String groupName,
                                  String groupId,String subHeadingName,String subHeadingId, String Color);


        public void onCreateCard(View view);


    }


}
