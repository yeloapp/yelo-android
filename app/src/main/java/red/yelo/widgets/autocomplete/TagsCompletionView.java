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
 */package red.yelo.widgets.autocomplete;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import red.yelo.R;
import red.yelo.widgets.MultiTagLayout;
import red.yelo.widgets.TypefaceCache;
import red.yelo.widgets.TypefaceUtils;

public class TagsCompletionView extends TokenCompleteTextView {

    private List<MultiTagLayout.Tag> mCurrentTags;

    public TagsCompletionView(Context context) {
        super(context);
        init(context, null);
    }

    public TagsCompletionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TagsCompletionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, new String[]{}));
        if (isInEditMode()) {
            return;
        }
        if (attrs != null) {
            final int typefaceCode = TypefaceUtils.typefaceCodeFromAttribute(context, attrs);

            final Typeface typeface = TypefaceCache
                    .get(context.getAssets(), typefaceCode);
            setTypeface(typeface);
        }
    }

    @Override
    protected View getViewForObject(Object object) {
        MultiTagLayout.Tag p = (MultiTagLayout.Tag) object;

        TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.tag_token, null, false);
        textView.setText(p.name);
        textView.setSelected(true);
        return textView;
    }

    @Override
    protected Object defaultObject(String completionText) {

        if (mCurrentTags == null || mCurrentTags.size() == 0) {
            return null;
        } else {
            return mCurrentTags.get(0);
        }
    }

    public void setCurrentTags(List<MultiTagLayout.Tag> tags) {
        mCurrentTags = tags;
    }

}
