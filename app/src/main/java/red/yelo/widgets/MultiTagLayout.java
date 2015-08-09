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
 */package red.yelo.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wefika.flowlayout.FlowLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import red.yelo.R;

/**
 * Class that takes care of multiple tags in a flowing format
 * <p/>
 * Created by vinaysshenoy on 26/10/14.
 */
public class MultiTagLayout extends FlowLayout implements View.OnClickListener {

    private static final String TAG = "MultiTagLayout";

    private static final int LAYOUT_RES_ID = R.layout.layout_tag_grid;

    /**
     * List of tags currently being displayed
     */
    private List<Tag> mDisplayedTags;

    /**
     * List of tags that are selected
     */
    private List<String> mSelectedTags;

    /**
     * Map of the tags to the TextView currently displaying it
     */
    private Map<Tag, TextView> mTagViewMap;

    /**
     * Maintains a pool of TextViews that can be reused for tags
     */
    private List<TextView> mTextViewPool;

    /**
     * Whether tags should be selected on click
     */
    private boolean mShouldSelectOnClick;

    /**
     * Whether clicking on tags should be enabled
     */
    private boolean mTagsAreClickable;

    /**
     * Layout res id for the textview to inflate
     */
    private int mLayoutResId = LAYOUT_RES_ID;

    private OnTagClickListener mOnTagClickListener;

    /**
     * The selection mode of the layout
     */
    private SelectionMode mSelectionMode = SelectionMode.MULTIPLE;

    public MultiTagLayout(Context context) {
        this(context, null);
    }

    public MultiTagLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiTagLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setLayoutResId(final int layoutResId) {
        mLayoutResId = layoutResId;
    }

    private void init() {
        mTextViewPool = new ArrayList<TextView>();
        mDisplayedTags = new ArrayList<Tag>();
        mTagViewMap = new LinkedHashMap<Tag, TextView>();
        mSelectedTags = new ArrayList<String>();
        mShouldSelectOnClick = false;
        mTagsAreClickable = false;
    }

    /**
     * Adds a tag to the View
     *
     * @param tag       The tag to add
     * @param preSelect Whether to select the tag or not
     */
    public void addTag(final Tag tag, final boolean preSelect) {

        if (!mDisplayedTags.contains(tag)) {
            mDisplayedTags.add(tag);
        }

        if (preSelect && !mSelectedTags.contains(tag.id)) {
            mSelectedTags.add(tag.id);
        }

        if (!mTagViewMap.containsKey(tag)) {
            addTagToView(tag);
        }

    }

    /**
     * Adds the tag to the view
     */
    private void addTagToView(final Tag tag) {

        if (!mTagViewMap.containsKey(tag)) {

            final TextView textView = getTextView();
            textView.setText(tag.name);
            mTagViewMap.put(tag, textView);
            if (mTagsAreClickable) {
                textView.setOnClickListener(this);
            } else {
                textView.setOnClickListener(null);
            }
            textView.setTag(tag);

            if (mSelectedTags.contains(tag.id)) {
                textView.setSelected(true);
            } else {
                textView.setSelected(false);
            }
            addView(textView);
        }
    }

    /**
     * Removes a tag from the View
     *
     * @param tag The tag to remove
     */
    public void removeTag(final Tag tag) {

        if (mDisplayedTags != null && mDisplayedTags.contains(tag)) {
            mDisplayedTags.remove(tag);
            removeTagFromView(tag);
        }
    }

    /**
     * Removes a tag from the view
     */
    private void removeTagFromView(final Tag tag) {

        mDisplayedTags.remove(tag);
        final TextView view = mTagViewMap.get(tag);
        if (view != null) {
            removeView(view);
            view.setTag(null);
            view.setOnClickListener(null);
            view.setSelected(false);
            mTextViewPool.add(view);
        }

    }

    /**
     * Sets the tags to the view. This will clear all current tags
     *
     * @param tags The tags to set
     */
    public void setTags(final List<Tag> tags) {

        removeAllTags();
        mDisplayedTags = tags;
        addAllTagsToView();
    }

    /**
     * Adds all the current tags to the View
     */
    private void addAllTagsToView() {

        if (mDisplayedTags != null && !mDisplayedTags.isEmpty()) {

            for (final Tag tag : mDisplayedTags) {
                addTag(tag, mSelectedTags.contains(tag.id));
            }
        }
    }

    /**
     * Removes all the tags from the View
     */
    public void removeAllTags() {

        removeAllViews();
        clearTagViewMap();
    }

    /**
     * This clears the TagView map and restores the TextViews to the pool
     */
    private void clearTagViewMap() {

        TextView view;
        for (final Tag tag : mTagViewMap.keySet()) {
            view = mTagViewMap.get(tag);
            view.setTag(null);
            view.setOnClickListener(null);
            view.setSelected(false);
            mTextViewPool.add(view);
        }
        mTagViewMap.clear();
    }

    /**
     * Gets a TextView for displaying the tag
     */
    public TextView getTextView() {

        if (!mTextViewPool.isEmpty()) {
            return mTextViewPool.remove(0);
        }
        return (TextView) LayoutInflater.from(getContext()).inflate(mLayoutResId, this, false);
    }

    @Override
    public void onClick(View v) {

        if (mShouldSelectOnClick) {

            if (mSelectionMode == SelectionMode.SINGLE && !v.isSelected()) {
                clearSelections();
            }

            //Toggle the selection state
            v.setSelected(!v.isSelected());

            final Tag tag = (Tag) v.getTag();
            if (v.isSelected()) {
                mSelectedTags.add(tag.id);
            } else {
                mSelectedTags.remove(tag.id);
            }
        }

        if (mOnTagClickListener != null) {
            mOnTagClickListener.onTagClicked(v, (Tag) v.getTag());
        }
    }

    /**
     * Class representing a Tag
     */
    public static final class Tag {

        public final String id;

        public final String name;

        public Tag(final String id, final String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            if (!(o instanceof Tag)) {
                return false;
            }

            Tag other = (Tag) o;

            return (id == null ? other.id == null : id.equals(other.id)) &&
                    (name == null ? other.name == null : name.equals(other.name));
        }

        @Override
        public int hashCode() {
            int result = 5;

            result = (31 * result) + (id == null ? 0 : id.hashCode());
            result = (31 * result) + (name == null ? 0 : name.hashCode());
            return result;
        }
    }

    /**
     * Gets the tag for a particular View
     *
     * @param tag The tag to fetch the View for
     * @return The view associated with the tag, or {@code null} if it doesn't exist
     */
    public TextView getViewForTag(final Tag tag) {

        if (tag == null) {
            return null;
        }

        return mTagViewMap.get(tag);
    }

    /**
     * Returns the tag associated with a view.
     *
     * @param view The view for which to get the Tag
     * @return The tag with which the view is associated, or {@code null} if none exist
     */
    public Tag getTagForView(final View view) {

        if (view == null) {
            return null;
        }

        for (final Map.Entry<Tag, TextView> entry : mTagViewMap.entrySet()) {
            if (view.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;

    }

    /**
     * Whether tags should be selected on click
     *
     * @return {@code true} if tags should be selected on clicking, {@code false} if tags should not be selected
     */
    public boolean shouldSelectOnClick() {
        return mShouldSelectOnClick;
    }

    /**
     * Toggle whether tags should be selected to clicking
     * <p/>
     * <i>Note:</i> Setting this to {@code true} also sets tags to be clickable. but setting these to {@code false} doesn't change tags to not be clickable
     */
    public void setShouldSelectOnClick(boolean shouldSelectOnClick) {

        mShouldSelectOnClick = shouldSelectOnClick;

        if (shouldSelectOnClick()) {
            setTagsClickable(true);
        }
    }

    /**
     * Whether tags are clickable or not
     *
     * @return {@code true} if tags are clickable, {@code false} otherwise
     */
    public boolean areTagsClickable() {
        return mTagsAreClickable;
    }

    /**
     * Sets tags to be clickable or not
     *
     * @param tagsAreClickable {@code true} to make tags clicka
     */
    public void setTagsClickable(boolean tagsAreClickable) {

        mTagsAreClickable = tagsAreClickable;
        updateListenerOnCurrentTextViews();
    }

    /**
     * Interface that defines callback methods for when tags are clicked
     */
    public static interface OnTagClickListener {

        public void onTagClicked(final View view, final Tag tag);
    }

    /**
     * Sets a listener for when tags are clicked.
     */
    public void setOnTagClickListener(final OnTagClickListener listener) {

        mOnTagClickListener = listener;
        updateListenerOnCurrentTextViews();
    }

    /**
     * If a listener is set/unset, updates the current textviews with the listener
     */
    private void updateListenerOnCurrentTextViews() {

        if (!mTagViewMap.isEmpty()) {

            for (TextView textView : mTagViewMap.values()) {

                if (mTagsAreClickable) {
                    textView.setOnClickListener(this);
                } else {
                    textView.setOnClickListener(null);
                }
            }
        }
    }

    /**
     * Sets a tag as selected or unselected
     *
     * @param tagId    The id of the tag to change selection state
     * @param selected {@code true} to set it as selected, {@code false} otherwise
     */
    public void setTagSelected(final String tagId, final boolean selected) {

        if (selected) {
            if (!mSelectedTags.contains(tagId)) {
                mSelectedTags.add(tagId);
            }
        } else {
            mSelectedTags.remove(tagId);
        }

        final TextView textView = findTextViewByTagId(tagId);
        if (textView != null) {
            textView.setSelected(selected);
        }
    }

    /**
     * Clears all the selected tags
     */
    public void clearSelections() {

        mSelectedTags.clear();
        refreshSelectedTags();
    }

    /**
     * Finds a TextView by the tag id
     *
     * @param tagId The id of the tag to get the TextView for
     * @return The TextView, or {@code null} if none exist
     */
    private TextView findTextViewByTagId(final String tagId) {

        for (final TextView view : mTagViewMap.values()) {
            final Tag tag = (Tag) view.getTag();
            if (tagId.equals(tag.id)) {
                return view;
            }
        }

        return null;
    }

    /**
     * Sets a list of tag ids as selected
     *
     * @param tagsToSelect The ids of tags to select
     */
    public void setSelectedTags(final String... tagsToSelect) {

        if (tagsToSelect != null && tagsToSelect.length > 0) {

            for (String tagId : tagsToSelect) {

                if (!mSelectedTags.contains(tagId)) {
                    mSelectedTags.add(tagId);
                }
            }

        }

        refreshSelectedTags();
    }

    /**
     * Iterates through the displayed TextViews and sets them as selected based on whether
     * the associated tag is present in the selected tags list or not
     */
    private void refreshSelectedTags() {

        final int countOfSelectedTags = mSelectedTags.size();

        Tag eachTag;
        for (final TextView eachView : mTagViewMap.values()) {

            eachTag = (Tag) eachView.getTag();
            if (mSelectedTags.contains(eachTag.id)) {
                eachView.setSelected(true);
            } else {
                eachView.setSelected(false);
            }
        }
    }

    /**
     * Gets a list of selected tag ids, or {@code null} if none are selected
     */
    public List<String> getSelectedTags() {
        return mSelectedTags;
    }

    public SelectionMode getSelectionMode() {
        return mSelectionMode;
    }

    public void setSelectionMode(final SelectionMode selectionMode) {
        mSelectionMode = selectionMode;
        //TODO: Update lo cal selections if mode switched from multiple to single
    }


    /**
     * Enum that indicates how tags should be selected
     */
    public enum SelectionMode {

        SINGLE,
        MULTIPLE
    }
}
