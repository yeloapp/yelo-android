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
import android.content.res.TypedArray;
import android.util.AttributeSet;

import red.yelo.R;

/**
 * Created by vinaysshenoy on 19/10/14.
 */
public class TypefaceUtils {

    private static final int[] TEXT_APPEARANCE_ATTRS = new int[]{android.R.attr.textAppearance};

    private static final int[] FONT_STYLE_ATTRS = new int[]{R.attr.fontStyle};

    private TypefaceUtils() {

    }

    /**
     * Gets the integer value for the fontStyle attribute from an attribute set
     *
     * @param context A reference to a Context
     * @param attrs   The set of attributes
     * @return The value read from the attributeset for the font style attribute
     */
    public static int typefaceCodeFromAttribute(final Context context, final AttributeSet attrs) {

        int typefaceCode = -1;
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, FONT_STYLE_ATTRS);
        if (typedArray != null) {
            try {
                // First defined attribute
                typefaceCode = typedArray.getInt(0, -1);
            } catch (Exception ignore) {
                // Failed for some reason.
            } finally {
                typedArray.recycle();
            }
        }
        return typefaceCode;
    }
}
