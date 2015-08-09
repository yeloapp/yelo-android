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
 */package red.yelo.ui.Animation.AnimationCompat;

import android.view.View;

import red.yelo.ui.Animation.View10;

public class ViewProxy {

    public static float getAlpha(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getAlpha();
        } else {
            return view.getAlpha();
        }
    }

    public static void setAlpha(View view, float alpha) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setAlpha(alpha);
        } else {
            view.setAlpha(alpha);
        }
    }

    public static float getPivotX(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getPivotX();
        } else {
            return view.getPivotX();
        }
    }

    public static void setPivotX(View view, float pivotX) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setPivotX(pivotX);
        } else {
            view.setPivotX(pivotX);
        }
    }

    public static float getPivotY(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getPivotY();
        } else {
            return view.getPivotY();
        }
    }

    public static void setPivotY(View view, float pivotY) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setPivotY(pivotY);
        } else {
            view.setPivotY(pivotY);
        }
    }

    public static float getRotation(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getRotation();
        } else {
            return view.getRotation();
        }
    }

    public static void setRotation(View view, float rotation) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setRotation(rotation);
        } else {
            view.setRotation(rotation);
        }
    }

    public static float getRotationX(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getRotationX();
        } else {
            return view.getRotationX();
        }
    }

    public void setRotationX(View view, float rotationX) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setRotationX(rotationX);
        } else {
            view.setRotationX(rotationX);
        }
    }

    public static float getRotationY(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getRotationY();
        } else {
            return view.getRotationY();
        }
    }

    public void setRotationY(View view, float rotationY) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setRotationY(rotationY);
        } else {
            view.setRotationY(rotationY);
        }
    }

    public static float getScaleX(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getScaleX();
        } else {
            return view.getScaleX();
        }
    }

    public static void setScaleX(View view, float scaleX) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setScaleX(scaleX);
        } else {
            view.setScaleX(scaleX);
        }
    }

    public static float getScaleY(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getScaleY();
        } else {
            return view.getScaleY();
        }
    }

    public static void setScaleY(View view, float scaleY) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setScaleY(scaleY);
        } else {
            view.setScaleY(scaleY);
        }
    }

    public static int getScrollX(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getScrollX();
        } else {
            return view.getScrollX();
        }
    }

    public static void setScrollX(View view, int value) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setScrollX(value);
        } else {
            view.setScrollX(value);
        }
    }

    public static int getScrollY(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getScrollY();
        } else {
            return view.getScrollY();
        }
    }

    public static void setScrollY(View view, int value) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setScrollY(value);
        } else {
            view.setScrollY(value);
        }
    }

    public static float getTranslationX(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getTranslationX();
        } else {
            return view.getTranslationX();
        }
    }

    public static void setTranslationX(View view, float translationX) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setTranslationX(translationX);
        } else {
            view.setTranslationX(translationX);
        }
    }

    public static float getTranslationY(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getTranslationY();
        } else {
            return view.getTranslationY();
        }
    }

    public static void setTranslationY(View view, float translationY) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setTranslationY(translationY);
        } else {
            view.setTranslationY(translationY);
        }
    }

    public static float getX(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getX();
        } else {
            return view.getX();
        }
    }

    public static void setX(View view, float x) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setX(x);
        } else {
            view.setX(x);
        }
    }

    public static float getY(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view).getY();
        } else {
            return view.getY();
        }
    }

    public static void setY(View view, float y) {
        if (View10.NEED_PROXY) {
            View10.wrap(view).setY(y);
        } else {
            view.setY(y);
        }
    }

    public static Object wrap(View view) {
        if (View10.NEED_PROXY) {
            return View10.wrap(view);
        } else {
            return view;
        }
    }
}
