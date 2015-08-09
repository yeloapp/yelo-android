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
 */package red.yelo.widgets.ActionBar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import red.yelo.utils.AndroidUtilities;


public class ActionBarMenu extends LinearLayout {

    protected ActionBar parentActionBar;

    public ActionBarMenu(Context context, ActionBar layer) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
        parentActionBar = layer;
    }

    public ActionBarMenu(Context context) {
        super(context);
    }

    public ActionBarMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionBarMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public View addItemResource(int id, int resourceId) {
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = li.inflate(resourceId, null);
        view.setTag(id);
        addView(view);
        LayoutParams layoutParams = (LayoutParams)view.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.FILL_PARENT;
        view.setBackgroundResource(parentActionBar.itemsBackgroundResourceId);
        view.setLayoutParams(layoutParams);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClick((Integer)view.getTag());
            }
        });
        return view;
    }

    public ActionBarMenuItem addItem(int id, Drawable drawable) {
        return addItem(id, 0, parentActionBar.itemsBackgroundResourceId, drawable, AndroidUtilities.dp(48));
    }

    public ActionBarMenuItem addItem(int id, int icon) {
        return addItem(id, icon, parentActionBar.itemsBackgroundResourceId);
    }

    public ActionBarMenuItem addItem(int id, int icon, int backgroundResource) {
        return addItem(id, icon, backgroundResource, null, AndroidUtilities.dp(48));
    }

    public ActionBarMenuItem addItemWithWidth(int id, int icon, int width) {
        return addItem(id, icon, parentActionBar.itemsBackgroundResourceId, null, width);
    }

    public ActionBarMenuItem addItem(int id, int icon, int backgroundResource, Drawable drawable, int width) {
        ActionBarMenuItem menuItem = new ActionBarMenuItem(getContext(), this, backgroundResource);
        menuItem.setTag(id);
        menuItem.setScaleType(ImageView.ScaleType.CENTER);
        if (drawable != null) {
            menuItem.setImageDrawable(drawable);
        } else {
            menuItem.setImageResource(icon);
        }
        addView(menuItem);
        LayoutParams layoutParams = (LayoutParams)menuItem.getLayoutParams();
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width = width;
        menuItem.setLayoutParams(layoutParams);
        menuItem.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionBarMenuItem item = (ActionBarMenuItem)view;
                if (item.hasSubMenu()) {
                    if (parentActionBar.actionBarMenuOnItemClick.canOpenMenu()) {
                        item.toggleSubMenu();
                    }
                } else if (item.isSearchField()) {
                    parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch());
                } else {
                    onItemClick((Integer)view.getTag());
                }
            }
        });
        return menuItem;
    }

    public void hideAllPopupMenus() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ((ActionBarMenuItem)view).closeSubMenu();
            }
        }
    }

    public void onItemClick(int id) {
        if (parentActionBar.actionBarMenuOnItemClick != null) {
            parentActionBar.actionBarMenuOnItemClick.onItemClick(id);
        }
    }

    public void clearItems() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            removeView(view);
        }
    }

    public void onMenuButtonPressed() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ActionBarMenuItem item = (ActionBarMenuItem)view;
                if (item.hasSubMenu() && item.getVisibility() == VISIBLE) {
                    item.toggleSubMenu();
                    break;
                }
            }
        }
    }

    public void closeSearchField() {
        for (int a = 0; a < getChildCount(); a++) {
            View view = getChildAt(a);
            if (view instanceof ActionBarMenuItem) {
                ActionBarMenuItem item = (ActionBarMenuItem)view;
                if (item.isSearchField()) {
                    parentActionBar.onSearchFieldVisibilityChanged(item.toggleSearch());
                }
            }
        }
    }

    public ActionBarMenuItem getItem(int id) {
        View v = findViewWithTag(id);
        if (v instanceof ActionBarMenuItem) {
            return (ActionBarMenuItem)v;
        }
        return null;
    }
}
