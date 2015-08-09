
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

package red.yelo.widgets;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.HashMap;
import java.util.Map;

/**
 * Typeface cache to cache the typefaces
 */
public class TypefaceCache {

    //Expected Capacity is number of supported fonts
    private static final Map<String, Typeface> CACHE = new HashMap<String, Typeface>((int) (7 * 1.33f));

    public static final String REGULAR = "fonts/Roboto-Regular.ttf";
    public static final String SLAB_BOLD = "fonts/RobotoSlab-Bold.ttf";
    public static final String LIGHT = "fonts/Roboto-Light.ttf";
    public static final String MEDIUM = "fonts/Roboto-Medium.ttf";
    public static final String ITALIC = "fonts/Roboto-Italic.ttf";
    public static final String LIGHT_ITALIC = "fonts/Roboto-LightItalic.ttf";
    public static final String MEDIUM_ITALIC = "fonts/Roboto-MediumItalic.ttf";
    public static final String OPEN_SANS_BOLD = "fonts/OpenSans-Bold.ttf";
    public static final String BEBAS_BOLD = "fonts/BebasNeue-Bold.ttf";

    public static Typeface get(final AssetManager manager,
                               final int typefaceCode) {
        synchronized (CACHE) {

            final String typefaceName = getTypefaceName(typefaceCode);

            if (!CACHE.containsKey(typefaceName)) {
                final Typeface t = Typeface
                        .createFromAsset(manager, typefaceName);
                CACHE.put(typefaceName, t);
            }
            return CACHE.get(typefaceName);
        }
    }

    public static Typeface get(final AssetManager manager,
                               final String typefaceName) {
        return get(manager, getCodeForTypefaceName(typefaceName));
    }

    private static int getCodeForTypefaceName(final String typefaceName) {

        if (typefaceName.equals(REGULAR)) {
            return 0;
        } else if (typefaceName.equals(LIGHT)) {
            return 1;
        } else if (typefaceName.equals(MEDIUM)) {
            return 2;
        } else if (typefaceName.equals(LIGHT)) {
            return 3;
        } else if (typefaceName.equals(LIGHT_ITALIC)) {
            return 4;
        } else if (typefaceName.equals(MEDIUM_ITALIC)) {
            return 5;
        } else if (typefaceName.equals(SLAB_BOLD)) {
            return 6;
        } else if (typefaceName.equals(OPEN_SANS_BOLD)) {
            return 7;
        }
          else if (typefaceName.equals(BEBAS_BOLD)){
            return 8;
        }
        else {
            return 0;
        }
    }

    private static String getTypefaceName(final int typefaceCode) {
        switch (typefaceCode) {
            case 0: {
                return REGULAR;
            }

            case 1: {
                return LIGHT;
            }

            case 2: {
                return MEDIUM;
            }

            case 3: {
                return ITALIC;
            }

            case 4: {
                return LIGHT_ITALIC;
            }

            case 5: {
                return MEDIUM_ITALIC;
            }
            case 6: {
                return SLAB_BOLD;
            }
            case 7: {
                return OPEN_SANS_BOLD;
            }
            case 8: {
                return BEBAS_BOLD;
            }
            default: {
                return REGULAR;
            }
        }
    }

}
