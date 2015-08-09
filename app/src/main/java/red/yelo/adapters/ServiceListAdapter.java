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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;


/**
 * @author Sharath Pandeshwar
 * @since 19/03/2015
 */
public class ServiceListAdapter extends CursorAdapter {

    private static final String TAG = "ServiceListAdapter";


    /**
     * @param context A reference to the {@link Context}
     */
    public ServiceListAdapter(final Context context) {
        super(context, null, 0);
    }


    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        ServiceItemHolder holder = (ServiceItemHolder) view.getTag();
        String heading = cursor.getString(cursor.getColumnIndex(DatabaseColumns.NAME));
        String color = cursor.getString(cursor.getColumnIndex(DatabaseColumns.COLOR));

        holder.heading.setText(heading);
        holder.layout.setBackgroundColor(Color.parseColor(color));
    }


    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_service_row, parent, false);
        view.setTag(new ServiceItemHolder(view));
        return view;
    }


    private class ServiceItemHolder extends RecyclerView.ViewHolder {

        TextView heading;
        LinearLayout layout;


        public ServiceItemHolder(View itemView) {
            super(itemView);
            heading = (TextView) itemView.findViewById(R.id.text_card_heading);
            layout = (LinearLayout) itemView.findViewById(R.id.layout_card_list);
        }
    }

}
