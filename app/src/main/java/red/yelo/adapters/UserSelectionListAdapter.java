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
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.vinaysshenoy.okulus.OkulusImageView;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.Utils;

/**
 * Adapter used to display Users
 * Created by anshul1235 on 24/10/14.
 */

public class UserSelectionListAdapter extends CursorAdapter {

    private static final String TAG = "UserSelectionListAdapter";

    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public UserSelectionListAdapter(final Context context) {
        super(context, null, 0);
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {
        UserSelectionHolder holder = (UserSelectionHolder) view.getTag();
        String name = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.USER_NAME));
        final String imageUri = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.USER_IMAGE));
        holder.textName.setText(name);

        ColorGenerator generator = ColorGenerator.DEFAULT;

        int color = generator.getColor((name.charAt(0) + "").toUpperCase());
        Resources r = mContext.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

        TextDrawable drawable = TextDrawable.builder()
                .buildRoundRect((name.charAt(0) + "").toUpperCase(), color, Math.round(px));

        if (!TextUtils.isEmpty(imageUri)) {
            Utils.loadCircularImage(context, (OkulusImageView) holder.imageUser, imageUri, AvatarBitmapTransformation.AvatarSize.NORMAL,drawable);
        } else {
            Utils.loadCircularImage(context, (OkulusImageView) holder.imageUser, "", AvatarBitmapTransformation.AvatarSize.NORMAL,drawable);
        }
    }

    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_users_list, parent, false);
        view.setTag(new UserSelectionHolder(view));
        return view;
    }

    private class UserSelectionHolder extends RecyclerView.ViewHolder {

        ImageView imageUser;
        TextView textName;
        CheckBox checkBoxSelect;
        View checkableView;

        public UserSelectionHolder(View itemView) {
            super(itemView);
            imageUser = (ImageView) itemView.findViewById(R.id.image_users);
            textName = (TextView) itemView.findViewById(R.id.user_name);
            checkBoxSelect = (CheckBox) itemView.findViewById(R.id.user_select);
            checkableView = itemView.findViewById(R.id.checkable_view);
        }
    }

}
