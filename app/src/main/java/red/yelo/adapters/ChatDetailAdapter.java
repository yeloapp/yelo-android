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

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import red.yelo.R;
import red.yelo.data.DatabaseColumns;
import red.yelo.utils.AppConstants.ChatStatus;
import red.yelo.utils.Logger;

/**
 * Adapter to display Chat messages
 */
public class ChatDetailAdapter extends CursorAdapter {

    /*
     * View Types. If there are n types of views, these HAVE to be numbered from
     * 0 to n-1
     */
    private static final int INCOMING_MESSAGE = 0;
    private static final int OUTGOING_MESSAGE = 1;

    private final String TAG = "ChatDetailAdapter";

    /* Strings that indicate the chat status */
    private final String mSendingString;
    private final String mFailedString;

    private ChatListListener mChatListListener;

    private Cursor mChatCursor;

    @SuppressLint("UseSparseArrays")
    /* Sparse Array benefits are noticable only upwards of 10k items */
    public ChatDetailAdapter(final Context context, final Cursor cursor, final ChatListListener chatListListener) {
        super(context, cursor, 0);
        mSendingString = context.getString(R.string.sending);
        mFailedString = context.getString(R.string.failed);
        mChatListListener = chatListListener;
        mChatCursor = cursor;
    }

    @Override
    public int getItemViewType(final int position) {

        mCursor.moveToPosition(position);
        final int chatStatus = mCursor.getInt(mCursor
                .getColumnIndex(DatabaseColumns.CHAT_STATUS));

        switch (chatStatus) {

            case ChatStatus.FAILED:
            case ChatStatus.SENDING:
            case ChatStatus.SENT: {
                return OUTGOING_MESSAGE;
            }

            case ChatStatus.RECEIVED: {
                return INCOMING_MESSAGE;
            }

            default: {
                throw new IllegalStateException("Unknown chat status");
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        //Incoming and outgoing message
        return 2;
    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {

        ChatHolder holder = (ChatHolder) view.getTag();

        if (holder == null) {
            return;
        }

        final int viewType = getItemViewType(cursor.getPosition());
        holder.setChatType(viewType, cursor, view);

        holder.textMessage.setText(cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.MESSAGE)));

        final String timestamp = cursor.getString(cursor
                .getColumnIndex(DatabaseColumns.TIMESTAMP_HUMAN));

        view.setTag(R.string.tag_resend_on_click, false);

        holder.viewMessage.setTag(R.string.tag_position, cursor.getPosition());
        holder.viewMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mChatListListener.onChatItemClick(view, ((Integer) v.getTag(R.string.tag_position)));
            }
        });

        holder.viewMessage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                mChatListListener.onChatItemLongClick(view, ((Integer) v.getTag(R.string.tag_position)));

                return true;
            }
        });


        if (viewType == INCOMING_MESSAGE) {
            holder.textTimeStamp.setText(timestamp.toUpperCase());
            holder.textTimeStamp.setTextColor(mContext.getResources().getColor(R.color.dark_gray));
        } else if (viewType == OUTGOING_MESSAGE) {

            final int chatStatus = cursor.getInt(cursor
                    .getColumnIndex(DatabaseColumns.CHAT_STATUS));
            final TextView chatStatusTextView = holder.textTimeStamp;

            switch (chatStatus) {
                case ChatStatus.SENDING: {
                    //chatStatusTextView.setText(mSendingString);
                    chatStatusTextView.setText("");
                    chatStatusTextView.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(R.drawable.ic_action_query_builder)
                            , null, null, null);
                    break;

                }
                case ChatStatus.SENT: {
                    chatStatusTextView.setText(timestamp.toUpperCase());
                    chatStatusTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

                    break;
                }
                case ChatStatus.FAILED: {
                    chatStatusTextView.setText(mFailedString);
                    chatStatusTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    view.setTag(R.string.tag_resend_on_click, true);
                    break;
                }
            }
        }
    }


    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {

        View view = LayoutInflater
                .from(context)
                .inflate(R.layout.layout_item_chat, parent, false);
        ChatHolder holder = new ChatHolder(view);
        view.setTag(holder);

        return view;
    }

    private class ChatHolder extends RecyclerView.ViewHolder {

        RelativeLayout layout;
        View viewMessage;
        TextView textMessage;
        TextView textTimeStamp;

        public ChatHolder(View itemView) {
            super(itemView);
            layout = (RelativeLayout) itemView.findViewById(R.id.layout_chat);
            viewMessage = itemView.findViewById(R.id.layout_message);
            textMessage = (TextView) itemView.findViewById(R.id.text_chat_message);
            textTimeStamp = (TextView) itemView.findViewById(R.id.text_status);
        }

        public void setChatType(int type, final Cursor cursor, final View fullView) {
            if (type == INCOMING_MESSAGE) {
                layout.setGravity(Gravity.START | Gravity.LEFT);
                layout.setPadding(1, 0, 64, 0);
                viewMessage.setBackgroundResource(R.drawable.selector_click_bubble_incoming);
            } else if (type == OUTGOING_MESSAGE) {
                layout.setGravity(Gravity.END | Gravity.RIGHT);
                layout.setPadding(64, 0, 1, 0);
                viewMessage.setBackgroundResource(R.drawable.selector_click_bubble_outgoing);
            }


        }
    }


    /**
     * Interface that receives events when the actions on a chat options are clicked
     */
    public static interface ChatListListener {

        public void onChatItemClick(View view, int position);

        public void onChatItemLongClick(View view, int position);


    }


}
