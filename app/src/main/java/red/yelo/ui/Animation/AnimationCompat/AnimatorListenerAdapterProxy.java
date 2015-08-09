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

package red.yelo.ui.Animation.AnimationCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import red.yelo.ui.Animation.Animator10;
import red.yelo.ui.Animation.AnimatorListenerAdapter10;
import red.yelo.ui.Animation.View10;


public class AnimatorListenerAdapterProxy {
    protected Object animatorListenerAdapter;

    public AnimatorListenerAdapterProxy() {
        if (View10.NEED_PROXY) {
            animatorListenerAdapter = new AnimatorListenerAdapter10() {
                @Override
                public void onAnimationCancel(Animator10 animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationCancel(animation);
                }

                @Override
                public void onAnimationEnd(Animator10 animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animator10 animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationRepeat(animation);
                }

                @Override
                public void onAnimationStart(Animator10 animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationStart(animation);
                }

                @Override
                public void onAnimationPause(Animator10 animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationPause(animation);
                }

                @Override
                public void onAnimationResume(Animator10 animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationResume(animation);
                }
            };
        } else {
            animatorListenerAdapter = new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationCancel(animation);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationRepeat(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationStart(animation);
                }

                @Override
                public void onAnimationPause(Animator animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationPause(animation);
                }

                @Override
                public void onAnimationResume(Animator animation) {
                    AnimatorListenerAdapterProxy.this.onAnimationResume(animation);
                }
            };
        }
    }

    public void onAnimationCancel(Object animation) {

    }

    public void onAnimationEnd(Object animation) {

    }

    public void onAnimationRepeat(Object animation) {

    }

    public void onAnimationStart(Object animation) {

    }

    public void onAnimationPause(Object animation) {

    }

    public void onAnimationResume(Object animation) {

    }
}
