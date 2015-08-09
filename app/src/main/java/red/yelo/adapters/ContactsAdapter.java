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
import android.provider.ContactsContract;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.vinaysshenoy.okulus.OkulusImageView;

import java.util.ArrayList;
import java.util.HashMap;

import red.yelo.R;
import red.yelo.utils.AvatarBitmapTransformation;
import red.yelo.utils.Utils;
import red.yelo.widgets.CheckableRelativeLayout;

/**
 * Adapter used to display Contacts
 * Created by anshul1235 on 06/10/14.
 */

public class ContactsAdapter extends CursorAdapter implements CheckableRelativeLayout.OnCheckedChangeListener {

    private static final String TAG = "ContactsAdapter";

    /*
    listener to receive events on the fragment
     */
    private ContactsActionClickListener mContactsActionClickListener;

    private Cursor mContactCursor;

    /*
    flag to seperate two views (multiple selection, single selection)
     */
    private boolean mMultipleSelect;

    private int mPosition;

    /*
    to keep the memory for selected contacts for multiple selects
     */
    private boolean[] mBooleanMemory;

    /*
    to keep the record for selected phone numbers
     */
    private HashMap<String, Boolean> mSelectedContacts = new HashMap<String, Boolean>();


    /**
     * @param context A reference to the {@link android.content.Context}
     */
    public ContactsAdapter(final Context context, ContactsActionClickListener contactsActionClickListener, boolean isMultipleSelect) {
        super(context, null, 0);
        mContactsActionClickListener = contactsActionClickListener;
        this.mMultipleSelect = isMultipleSelect;

    }

    @Override
    public void bindView(final View view, final Context context,
                         final Cursor cursor) {

        final ContactHolder holder = (ContactHolder) view.getTag();


        String name = cursor.getString(cursor
                .getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
        String number = cursor.getString(cursor
                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1));
        String email = cursor.getString(cursor
                .getColumnIndex(ContactsContract.Data.DATA1));


        final String imageUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI));
        holder.textName.setText(name);
        holder.textNumber.setText(number);
        holder.textEmail.setText(email);
        holder.checkableView.setTag(R.string.tag_position, cursor.getPosition());

        if (!mMultipleSelect) {
            holder.checkableView.setOnClickListener(contactClickListener);
        } else {
            holder.checkableView.setOnCheckedChangeListener(this);
            holder.checkableView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Cursor cursor = (Cursor) getItem((Integer) v.getTag(R.string.tag_position));
                    mPosition = cursor.getPosition();

                    int limit = 0;
                    for (String key : mSelectedContacts.keySet()) {


                        if (mSelectedContacts.get(key)) {
                            limit++;
                        }

                    }


                    ContactHolder contactHolder = ((ContactHolder)((CheckableRelativeLayout) v).getTag());
                    if (limit < 5 && !(contactHolder.checkBox.isChecked())) {

                        contactHolder.checkBox.setChecked(true);

                        if ((contactHolder.checkBox.isChecked())) {
                            mBooleanMemory[mPosition] = true;
                            mSelectedContacts.put(cursor.getString(cursor
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1)), true);

                        } else {
                            mBooleanMemory[mPosition] = false;
                            mSelectedContacts.put(cursor.getString(cursor
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1)), false);


                        }
                    } else if ((contactHolder.checkBox.isChecked())) {
                        contactHolder.checkBox.setChecked(false);

                        mBooleanMemory[mPosition] = false;
                        mSelectedContacts.put(cursor.getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA1)), false);
                    }

                    else {
                        Toast.makeText(mContext, "You can send only 5 invites", Toast.LENGTH_SHORT).show();
                    }
                }

            });

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                }
            });

            if (mBooleanMemory[cursor.getPosition()]) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }


        }

        ColorGenerator generator = ColorGenerator.DEFAULT;

        int color = generator.getColor(name.toUpperCase());
        Resources r = mContext.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, r.getDisplayMetrics());

        TextDrawable drawable = TextDrawable.builder()
                .buildRoundRect((name.charAt(0) + "").toUpperCase(), color, Math.round(px));

        holder.imageContact.setImageDrawable(drawable);
        if (!TextUtils.isEmpty(imageUri)) {
            Utils.loadCircularImage(context, (OkulusImageView) holder.imageContact, imageUri, AvatarBitmapTransformation.AvatarSize.NORMAL,drawable);
        } else {

            holder.imageContact.setImageDrawable(drawable);
        }
    }


    @Override
    public View newView(final Context context, final Cursor cursor,
                        final ViewGroup parent) {


        View view = null;

        if (mMultipleSelect) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_contact_list_multiple_select, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_contact_list, parent, false);
        }
        view.setTag(new ContactHolder(view));
        return view;
    }

    @Override
    public void onCheckedChanged(View checkableView, boolean isChecked) {
        ((CheckBox) checkableView.findViewById(R.id.checkbox)).setChecked(isChecked);

    }


    private class ContactHolder extends RecyclerView.ViewHolder {

        ImageView imageContact;
        TextView textName;
        TextView textNumber;
        TextView textEmail;
        CheckBox checkBox;
        CheckableRelativeLayout checkableView;

        public ContactHolder(View itemView) {
            super(itemView);
            imageContact = (ImageView) itemView.findViewById(R.id.image_contact);
            textName = (TextView) itemView.findViewById(R.id.contact_name);
            textNumber = (TextView) itemView.findViewById(R.id.contact_number);
            textEmail = (TextView) itemView.findViewById(R.id.contact_email);
            checkableView = (CheckableRelativeLayout) itemView.findViewById(R.id.checkable_view);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }


    @Override
    public Cursor swapCursor(Cursor newCursor) {

        mContactCursor = newCursor;
        mBooleanMemory = new boolean[mContactCursor.getCount()];

        return super.swapCursor(newCursor);
    }

    private final View.OnClickListener contactClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mContactsActionClickListener != null) {


                mContactCursor.moveToPosition(((Integer) v.getTag(R.string.tag_position)));
                mContactsActionClickListener.onContactClicked(
                        v,
                        mContactCursor, ((Integer) v.getTag(R.string.tag_position)));
            }
        }
    };


    /**
     * Interface that receives events when the actions on a post are clicked
     */
    public static interface ContactsActionClickListener {

        public void onContactClicked(View view, Cursor cursor, int position);
    }


    public ArrayList<String> getSelectedContacts() {
        ArrayList<String> selectedItems = new ArrayList<String>();


        for (String key : mSelectedContacts.keySet()) {

            selectedItems.add(key);

        }

        return selectedItems;

    }
}
