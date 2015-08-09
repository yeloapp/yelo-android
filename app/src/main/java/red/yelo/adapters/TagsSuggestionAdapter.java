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
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;

/**
 * Adapter used to display tags
 * Created by anshul1235 on 17/07/14.
 */

public class TagsSuggestionAdapter extends CursorAdapter {

    private static final String TAG = "TagsSuggestionAdapter";

    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public TagsSuggestionAdapter(final Context context) {
        super(context, null, 0);
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {
        LocationHolder holder = (LocationHolder) view.getTag();
        String tagName = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.NAME));
        holder.textTag.setText(tagName);
    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_tag_suggesstion_list, parent, false);
        view.setTag(new LocationHolder(view));
        return view;
    }

    private class LocationHolder extends RecyclerView.ViewHolder {

        TextView textTag;

        public LocationHolder(View itemView) {
            super(itemView);
            textTag = (TextView) itemView.findViewById(R.id.tag_name);
        }
    }
}
