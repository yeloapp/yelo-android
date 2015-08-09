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
 */package red.yelo.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.StateSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EdgeEffect;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;

import red.yelo.YeloApplication;

public class AndroidUtilities {

    private static final Hashtable<String, Typeface> typefaceCache = new Hashtable<String, Typeface>();
    private static int prevOrientation = -10;
    private static boolean waitingForSms = false;
    private static final Object smsLock = new Object();

    public static int statusBarHeight = 0;
    public static float density = 1;
    public static Point displaySize = new Point();
    public static Integer photoSize = null;
    private static Boolean isTablet = null;

    static {
        density = YeloApplication.getStaticContext().getResources().getDisplayMetrics().density;
        checkDisplaySize();
    }


    public static Typeface getTypeface(String assetPath) {
        synchronized (typefaceCache) {
            if (!typefaceCache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(YeloApplication.getStaticContext().getAssets(), assetPath);
                    typefaceCache.put(assetPath, t);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return typefaceCache.get(assetPath);
        }
    }


    public static boolean isTablet() {
        if (isTablet == null) {
            isTablet = false;
        }
        return isTablet;
    }

    public static int dp(float value) {
        return (int)Math.ceil(density * value);
    }

    public static float dpf2(float value) {
        return density * value;
    }

    public static void checkDisplaySize() {
        try {
            WindowManager manager = (WindowManager)YeloApplication.getStaticContext().getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    if(Build.VERSION.SDK_INT < 13) {
                        displaySize.set(display.getWidth(), display.getHeight());
                    } else {
                        display.getSize(displaySize);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long makeBroadcastId(int id) {
        return 0x0000000100000000L | ((long)id & 0x00000000FFFFFFFFL);
    }

    public static int getMyLayerVersion(int layer) {
        return layer & 0xffff;
    }

    public static int getPeerLayerVersion(int layer) {
        return (layer >> 16) & 0xffff;
    }

    public static int setMyLayerVersion(int layer, int version) {
        return layer & 0xffff0000 | version;
    }

    public static int setPeerLayerVersion(int layer, int version) {
        return layer & 0x0000ffff | (version << 16);
    }

//    public static void runOnUIThread(Runnable runnable) {
//        runOnUIThread(runnable, 0);
//    }

//    public static void runOnUIThread(Runnable runnable, long delay) {
//        if (delay == 0) {
//            ApplicationLoader.applicationHandler.post(runnable);
//        } else {
//            ApplicationLoader.applicationHandler.postDelayed(runnable, delay);
//        }
//    }

    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static boolean isSmallTablet() {
        float minSide = Math.min(displaySize.x, displaySize.y) / density;
        return minSide <= 700;
    }

    public static int getMinTabletSide() {
        if (!isSmallTablet()) {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int leftSide = smallSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return smallSide - leftSide;
        } else {
            int smallSide = Math.min(displaySize.x, displaySize.y);
            int maxSide = Math.max(displaySize.x, displaySize.y);
            int leftSide = maxSide * 35 / 100;
            if (leftSide < dp(320)) {
                leftSide = dp(320);
            }
            return Math.min(smallSide, maxSide - leftSide);
        }
    }

    public static int getPhotoSize() {
        if (photoSize == null) {
            if (Build.VERSION.SDK_INT >= 16) {
                photoSize = 1280;
            } else {
                photoSize = 800;
            }
        }
        return photoSize;
    }



    public static int getCurrentActionBarHeight() {
         if (YeloApplication.getStaticContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return dp(48);
        } else {
            return dp(56);
        }
    }

    public static Point getRealScreenSize() {
        Point size = new Point();
        try {
            WindowManager windowManager = (WindowManager) YeloApplication.getStaticContext().getSystemService(Context.WINDOW_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                windowManager.getDefaultDisplay().getRealSize(size);
            } else {
                try {
                    Method mGetRawW = Display.class.getMethod("getRawWidth");
                    Method mGetRawH = Display.class.getMethod("getRawHeight");
                    size.set((Integer) mGetRawW.invoke(windowManager.getDefaultDisplay()), (Integer) mGetRawH.invoke(windowManager.getDefaultDisplay()));
                } catch (Exception e) {
                    size.set(windowManager.getDefaultDisplay().getWidth(), windowManager.getDefaultDisplay().getHeight());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public static void setListViewEdgeEffectColor(AbsListView listView, int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            try {
                Field field = AbsListView.class.getDeclaredField("mEdgeGlowTop");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowTop = (EdgeEffect) field.get(listView);
                if (mEdgeGlowTop != null) {
                    mEdgeGlowTop.setColor(color);
                }

                field = AbsListView.class.getDeclaredField("mEdgeGlowBottom");
                field.setAccessible(true);
                EdgeEffect mEdgeGlowBottom = (EdgeEffect) field.get(listView);
                if (mEdgeGlowBottom != null) {
                    mEdgeGlowBottom.setColor(color);
                }
            } catch (Exception e) {
              e.printStackTrace();
            }
        }
    }

    public static void clearDrawableAnimation(View view) {
        if (Build.VERSION.SDK_INT < 21 || view == null) {
            return;
        }
        Drawable drawable = null;
        if (view instanceof ListView) {
            drawable = ((ListView) view).getSelector();
            if (drawable != null) {
                drawable.setState(StateSet.NOTHING);
            }
        } else {
            drawable = view.getBackground();
            if (drawable != null) {
                drawable.setState(StateSet.NOTHING);
                drawable.jumpToCurrentState();
            }
        }
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            YeloApplication.applicationHandler.post(runnable);
        } else {
            YeloApplication.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        YeloApplication.applicationHandler.removeCallbacks(runnable);
    }

    public static void showKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

        ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(view, 0);
    }



}
