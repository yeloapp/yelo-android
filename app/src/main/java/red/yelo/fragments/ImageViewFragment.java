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

package red.yelo.fragments;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import red.yelo.R;
import red.yelo.utils.AppConstants;
import red.yelo.widgets.TypefaceCache;
import red.yelo.widgets.TypefacedSpan;

/**
 * Created by anshul1235 on 15/07/14.
 */
public class ImageViewFragment extends AbstractYeloFragment {

    public static final String TAG= "ImageViewFragment";
    private ImageView mWallImageExpand;
    private int mImageResourceId;

    private TextView mHelpText;
    private String mHelpString;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View contentView = inflater
                .inflate(R.layout.fragment_image_view, container, false);

        Bundle extras=getArguments();

        mImageResourceId = extras.getInt(AppConstants.Keys.IMAGE_RES_ID);
        mHelpString = extras.getString(AppConstants.Keys.HELP_TEXT);
        mWallImageExpand = (ImageView)contentView.findViewById(R.id.wall_image_expand);

        mHelpText = (TextView) contentView.findViewById(R.id.help_message);

        mWallImageExpand.setImageResource(mImageResourceId);


        SpannableString spannable;

        spannable = new SpannableString(getSpannableLogoTitle(mHelpString));
        spannable.setSpan(new ImageSpan(getActivity(), R.drawable.bullet_inset_tutorial, ImageSpan.ALIGN_BASELINE), spannable.length() - 1, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannable.setSpan(new TypefacedSpan(getActivity(), TypefaceCache.OPEN_SANS_BOLD), 0, spannable.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mHelpText.setText(spannable);

        return contentView;

    }

    private String getSpannableLogoTitle(CharSequence title) {

        final StringBuilder builder = new StringBuilder(title.length() + 1);
        final String bullet = getString(R.string.bullet);
        builder.append(title).append(bullet);
        return builder.toString();
    }



    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    public static ImageViewFragment newInstance(int resId,String helpText) {
        ImageViewFragment f = new ImageViewFragment();
        Bundle args = new Bundle(1);
        args.putInt(AppConstants.Keys.IMAGE_RES_ID,resId);
        args.putString(AppConstants.Keys.HELP_TEXT, helpText);
        f.setArguments(args);
        return f;
    }


}
