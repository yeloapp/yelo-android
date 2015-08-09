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
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;

/**
 * Adapter used to display collections
 * Created by anshul1235 on 24/10/14.
 */

public class CollectionListAdapter extends CursorAdapter {

    private static final String TAG = "CollectionListAdapter";

    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public CollectionListAdapter(final Context context) {
        super(context, null, 0);
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {
        CollectionHolder holder = (CollectionHolder) view.getTag();
        String heading = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.HEADING));
        String subHeading = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.SUB_HEADING));

        int color = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseColumns.COLOR)));

        int[] materialColors = mContext.getResources().getIntArray(R.array.collectionListColors);


        holder.heading.setText(heading);
        holder.subHeading.setText(subHeading);

        Random r = new Random();
        String referralCount  = ""+(r.nextInt(20 - 1 + 1) + 1);
        holder.referrals.setText(referralCount+" referrals");
        holder.layout.setBackgroundColor(materialColors[color]);

    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_collection_top_list, parent, false);
        view.setTag(new CollectionHolder(view));
        return view;
    }

    private class CollectionHolder extends RecyclerView.ViewHolder {

        TextView heading,subHeading,referrals;
        LinearLayout layout;

        public CollectionHolder(View itemView) {
            super(itemView);
            heading = (TextView) itemView.findViewById(R.id.text_card_heading);
            subHeading = (TextView) itemView.findViewById(R.id.text_card_subsubheading);
            layout = (LinearLayout) itemView.findViewById(R.id.layout_card_list);
            referrals = (TextView) itemView.findViewById(R.id.text_card_referrals);
        }
    }

}
