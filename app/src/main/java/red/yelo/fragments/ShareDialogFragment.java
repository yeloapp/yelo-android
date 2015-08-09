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
 */package red.yelo.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import red.yelo.R;

/**
 * Created by imran on 08/11/14.
 */
public class ShareDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private final static String TAG = "ShareDialogFragment.class";
    private static ShareDialogFragment sShareDialogFragment;

    public static ShareDialogFragment getInstance(Bundle bundle) {
        if (sShareDialogFragment == null) {
            sShareDialogFragment = new ShareDialogFragment();
            sShareDialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog_MinWidth);
        }
        sShareDialogFragment.setArguments(bundle);
        return sShareDialogFragment;
    }

    LinkedHashMap<String, ResolveInfo> mShareMaps;
    LinkedHashSet<ShareWith> mLinkedShare;
    List<Intent> sInitialShareIntents;
    List<Intent> sMoreShareIntents;

    public ShareDialogFragment() {

        mShareMaps = new LinkedHashMap<String, ResolveInfo>();
        mShareMaps.put("com.whatsapp", null);
        mShareMaps.put("com.google.android.talk", null);
        mShareMaps.put("com.facebook.orca", null);
        mShareMaps.put("com.android.mms", null);
        mShareMaps.put("com.twitter.android", null);
        mShareMaps.put("com.facebook.katana", null);
        mShareMaps.put("com.google.android.apps.plus", null);
        mShareMaps.put("com.google.android.gm", null);

        mLinkedShare = new LinkedHashSet<ShareWith>(mShareMaps.size());
        sInitialShareIntents = new ArrayList<Intent>();
        sMoreShareIntents = new ArrayList<Intent>();
    }


    private boolean isLinear = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (isLinear) {
            return inflater.inflate(R.layout.dialog_share_linear, container, false);
        }
        return inflater.inflate(R.layout.dialog_share, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isLinear) {
            ShareWithAdapter adapter = new ShareWithAdapter();
            LinearLayout layout = (LinearLayout) view.findViewById(R.id.layout_share);
            int size = adapter.getCount();
            int i = 0;
            while (size > 0) {
                layout.addView(adapter.getView(i, null, layout));
                i++;
                size--;
            }
        } else {
            GridView mGridViewShare;
            Button mButtonMore;
            mGridViewShare = (GridView) view.findViewById(R.id.grid_share);
            mButtonMore = (Button) view.findViewById(R.id.button_share_more);
            mGridViewShare.setAdapter(new ShareWithAdapter());
            mGridViewShare.setOnItemClickListener(this);

            mButtonMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    getMoreShareIntents(getActivity());
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ShareWith item = (ShareWith) view.getTag(R.string.tag_share_item);
        getActivity().startActivity(item.intent);
    }

    private class ShareWith {
        String name;
        Drawable icon;
        Intent intent;
    }

    List<ShareWith> mShareWithList;

    private class ShareWithAdapter extends BaseAdapter {

        ShareWithAdapter() {
            mShareWithList = getShareIntents(getActivity());
        }

        @Override
        public int getCount() {
            return mShareWithList.size();
        }

        @Override
        public ShareWith getItem(int position) {
            return mShareWithList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.layout_item_share, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(R.string.tag_share_holder, holder);
            } else {
                holder = (ViewHolder) convertView.getTag(R.string.tag_share_holder);
            }

            ShareWith item = getItem(position);
            holder.textViewLabel.setText(item.name);
            holder.imageViewIcon.setImageDrawable(item.icon);
            convertView.setTag(R.string.tag_share_item, item);
            return convertView;
        }

        private class ViewHolder {
            ImageView imageViewIcon;
            TextView textViewLabel;

            ViewHolder(View view) {
                textViewLabel = (TextView) view.findViewById(R.id.text_app_label);
                imageViewIcon = (ImageView) view.findViewById(R.id.image_app_icon);
            }
        }
    }


    public List<ShareWith> getShareIntents(Context context) {

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        mShareWithList = new ArrayList<ShareWith>();

        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share with");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hello Yelo!");
        shareIntent.setType("text/plain");

        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(shareIntent, 0);

        for (ResolveInfo info : listCam) {
            final String packageName = info.activityInfo.packageName;
            if (mShareMaps.containsKey(packageName)) {
                mShareMaps.put(packageName, info);
            } else {
                sMoreShareIntents.add(loadIntentFromResolve(info));
            }
        }

        for (ResolveInfo info : mShareMaps.values()) {
            if (info != null && mShareWithList.size() < 4) {
                ShareWith item = new ShareWith();
                item.name = info.loadLabel(packageManager).toString();
                item.icon = info.loadIcon(packageManager);
                item.intent = loadIntentFromResolve(info);
                mShareWithList.add(item);
            } else if (info != null) {
                sMoreShareIntents.add(0, loadIntentFromResolve(info));
            }
        }

        return mShareWithList;
    }

    private Intent loadIntentFromResolve(ResolveInfo info) {
        final String packageName = info.activityInfo.packageName;
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share with");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "Hello Yelo!");
        intent.setType("text/plain");
        intent.setComponent(new ComponentName(packageName, info.activityInfo.name));
        intent.setPackage(packageName);
        return intent;
    }

    public void getMoreShareIntents(Context context) {
        final Intent intent = Intent.createChooser(sMoreShareIntents.remove(0), "Share with!");
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, sMoreShareIntents.toArray(new Parcelable[]{}));
        context.startActivity(intent);
    }

}
