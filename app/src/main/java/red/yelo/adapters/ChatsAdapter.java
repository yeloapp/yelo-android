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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.vinaysshenoy.okulus.OkulusImageView;

import java.text.SimpleDateFormat;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.DateFormatter;
import red.yelo.utils.Logger;
import red.yelo.utils.Utils;


/**
 * Adapter for displaying list of all ongoing chats
 */
public class ChatsAdapter extends CursorAdapter {

    private static final String TAG = "ChatsAdapter";

    private final String mUserNameFormat = "%s %s";
    private SimpleDateFormat mFormatter;
    private DateFormatter mMessageDateFormatter;

    public ChatsAdapter(final Context context, final Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {

        ChatUserHolder holder = (ChatUserHolder) view.getTag();
        String chatType = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.CHAT_TYPE));
        String userName = String.format(mUserNameFormat, cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.USER_NAME)), "");
        String chatMessage = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.MESSAGE));
        String userImage = cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_IMAGE));
        String timeString = Utils.getElapsedTimeFormat(Long.valueOf(cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.TIMESTAMP_EPOCH))), cursor,mContext);



        if (cursor.getString(cursor.getColumnIndex(DatabaseColumns.UNREAD_COUNT)).equals("1")) {
            holder.textChatMessage.setTextColor(context.getResources().getColor(R.color.textColorPrimary));
            holder.textChatMessage.setTextAppearance(mContext, R.style.Yelo_Text_Body2);

        } else {
            holder.textChatMessage.setTextColor(context.getResources().getColor(R.color.greyed_text));

        }

        if (cursor.getString(cursor.getColumnIndex(DatabaseColumns.CHAT_TYPE)).equals(AppConstants.ChatType.BLOCK)) {

            holder.textName.setTextColor(context.getResources().getColor(R.color.greyed_text));
        } else {
            holder.textName.setTextColor(context.getResources().getColor(R.color.textColorPrimary));
        }

        holder.textName.setText(userName);

        ColorGenerator generator = ColorGenerator.DEFAULT;

        int colorText = generator.getColor((userName.charAt(0) + "").toUpperCase());
        Resources r = mContext.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

        TextDrawable drawable = TextDrawable.builder()
                .buildRoundRect((userName.charAt(0) + "").toUpperCase(), colorText, Math.round(px));


        if (userImage.isEmpty()||userImage.contains("assets/fallback/")) {
            holder.imageUser.setImageDrawable(drawable);

        } else {
            Utils.loadCircularImage(context,
                    (OkulusImageView) holder.imageUser,
                    userImage,
                    AvatarBitmapTransformation.AvatarSize.NORMAL, drawable);
        }

        holder.textChatMessage.setText(chatMessage);
        // if sender image is empty that means user has sent the chat! but he has not received
        //so we show his image ;)
        holder.textChatTime.setText(timeString);
    }



    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {
        final View view = LayoutInflater.from(context)
                .inflate(R.layout.layout_chat_item, parent, false);

        view.setTag(new ChatUserHolder(view));
        return view;
    }

    private class ChatUserHolder extends RecyclerView.ViewHolder {

        public final ImageView imageUser;
        public final TextView textName;
        public final TextView textTags;
        public final TextView textChatMessage;
        public final TextView textChatTime;
        public final View chatFullView;

        public ChatUserHolder(View itemView) {
            super(itemView);

            imageUser = (ImageView) itemView.findViewById(R.id.image_user);
            textTags = (TextView) itemView.findViewById(R.id.text_tags);
            textName = (TextView) itemView.findViewById(R.id.text_user_name);
            textChatMessage = (TextView) itemView.findViewById(R.id.text_chat_message);
            textChatTime = (TextView) itemView.findViewById(R.id.text_chat_time);
            chatFullView = itemView.findViewById(R.id.chat_full_view);
        }

    }
}
