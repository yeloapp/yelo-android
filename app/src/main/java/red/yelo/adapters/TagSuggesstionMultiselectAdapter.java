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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;

/**
 * Adapter used to display Location
 * Created by anshul1235 on 17/07/14.
 */

public class TagSuggesstionMultiselectAdapter extends CursorAdapter {

    private static final String TAG = "TagSuggesstionMultiselectAdapter";

    private int mCount;

    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public TagSuggesstionMultiselectAdapter(final Context context) {
        super(context, null, 0);
        this.mContext = context;
        mCount = 0;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public void notifyDataSetChanged() {

        if ((mCursor == null) || mCursor.getCount() == 0) {
            mCount = 0;
        } else {
            mCount = mCursor.getCount();
        }
        super.notifyDataSetChanged();
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = inflateCategoryView(parent);
        }

        mCursor.moveToPosition(position);

        bindView(view, parent.getContext(), mCursor);

        return view;
    }


    /**
     * @param parent
     * @return
     */
    private View inflateCategoryView(ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_tagmultiselect_suggesstion_list, parent, false);

        view.setTag(R.id.tag_name, view.findViewById(R.id.tag_name));


        return view;
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {



        ((TextView) view.getTag(R.id.tag_name))
                .setText(cursor.getString(cursor
                        .getColumnIndex(DatabaseColumns.NAME)));

    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {


        return null;
    }



}
