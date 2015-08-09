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
 */package red.yelo.analytics;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;

import red.yelo.utils.Utils;

/**
 * Class that tracks app sessions and gives callbacks
 * when sessions begin and end.
 * <p/>
 * This class works by setting the {@link android.app.Application.ActivityLifecycleCallbacks} on
 * the Application class. If you need to provide your own Lifecycle callbacks, you can set an external
 * object which will have the callbacks forwarded to.
 * <p/>
 * The ideal place to initialize this is in the {@code onCreate()} of a custom Application class
 * <p/>
 * <p/>
 * Created by vinaysshenoy on 03/10/14.
 */
public class SessionTracker implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "SessionTracker";

    public static final long DEFAULT_SESSION_TIMEOUT_MILLIS = 5000L;

    /**
     * The session timeout(in millis)
     */
    private final long mSessionTimeout;

    /**
     * External callbacks to forward the events to
     */
    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks;

    /**
     * Callbacks that will be triggered when the sessions start and end
     */
    private SessionCallbacks mCallbacks;

    /**
     * Timer that is used to trigger when a session timeout occurs
     */
    private final CountDownTimer mCountDownTimer;

    /**
     * Whether sessions have started or not
     */
    private boolean mSessionStarted;

    /**
     * The time(in milliseconds) at which the session was started
     */
    private long mSessionStartTime;

    /**
     * Construct a Session Tracker with a timeout of five seconds.
     * <p/>
     * This needs to be constructed on the main thread.
     *
     * @param application      A reference to the Application
     * @param sessionCallbacks An instance of {@link red.yelo.analytics.SessionTracker.SessionCallbacks} to receive callbacks
     */
    public SessionTracker(final Application application, final SessionCallbacks sessionCallbacks) {
        this(application, sessionCallbacks, DEFAULT_SESSION_TIMEOUT_MILLIS);
    }

    /**
     * Construct a Session Tracker with a timeout session.
     * <p/>
     * This needs to be constructed on the main thread.
     *
     * @param application      A reference to the Application
     * @param sessionCallbacks An instance of {@link red.yelo.analytics.SessionTracker.SessionCallbacks} to receive callbacks
     * @param sessionTimeout   The timeout(in milliseconds) for sessions. When an app has been backgrounded, and doesn't resume after {@code sessionTimeout} duration, the session will be marked as ended
     */
    public SessionTracker(final Application application, final SessionCallbacks sessionCallbacks, final long sessionTimeout) {
        this(application, sessionCallbacks, sessionTimeout, null);
    }

    /**
     * Construct a Session Tracker with a timeout session.
     * <p/>
     * This needs to be constructed on the main thread.
     *
     * @param application                A reference to the Application
     * @param sessionCallbacks           An instance of {@link red.yelo.analytics.SessionTracker.SessionCallbacks} to receive callbacks
     * @param sessionTimeout             The timeout(in milliseconds) for sessions. When an app has been backgrounded, and doesn't resume after {@code sessionTimeout} duration, the session will be marked as ended
     * @param externalLifecycleCallbacks An instance of {@link android.app.Application.ActivityLifecycleCallbacks} to forward the Activity lifecycle events to
     */
    public SessionTracker(final Application application, final SessionCallbacks sessionCallbacks, final long sessionTimeout, final Application.ActivityLifecycleCallbacks externalLifecycleCallbacks) {

        if (!Utils.isMainThread()) {
            throw new RuntimeException("Needs to be initialized on the Main thread");
        }
        mSessionStarted = false;
        mCallbacks = sessionCallbacks;
        mSessionTimeout = sessionTimeout;
        mActivityLifecycleCallbacks = externalLifecycleCallbacks;

        mCountDownTimer = new CountDownTimer(mSessionTimeout, mSessionTimeout) {
            @Override
            public void onTick(final long millisUntilFinished) {
                //Nothing to do here
            }

            @Override
            public void onFinish() {
                endSession();
            }
        };
        application.registerActivityLifecycleCallbacks(this);
    }

    /**
     * Call to end the session
     */
    private void endSession() {

        mSessionStarted = false;
        if (mCallbacks != null) {
            final long sessionEndTime = SystemClock.elapsedRealtime();
            mCallbacks.onSessionEnded(sessionEndTime, sessionEndTime - mSessionStartTime);
        }
        mSessionStartTime = 0L;
    }

    /**
     * Call to start the session
     */
    private void startSession() {

        mSessionStartTime = SystemClock.elapsedRealtime();
        mSessionStarted = true;
        if (mCallbacks != null) {
            mCallbacks.onSessionStarted(mSessionStartTime);
        }
    }

    /**
     * Checks whether the session has been started or not
     *
     * @return {@code true} if the session has been started, {@code false} otherwise
     */
    public boolean isSessionStarted() {

        return mSessionStarted;
    }

    @Override
    public void onActivityCreated(final Activity activity, final Bundle bundle) {

        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityCreated(activity, bundle);
        }
    }

    @Override
    public void onActivityStarted(final Activity activity) {

        if (!isSessionStarted()) {
            startSession();
        }
        mCountDownTimer.cancel();
        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityStarted(activity);
        }
    }

    @Override
    public void onActivityResumed(final Activity activity) {

        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityResumed(activity);
        }
    }

    @Override
    public void onActivityPaused(final Activity activity) {

        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityPaused(activity);
        }
    }

    @Override
    public void onActivityStopped(final Activity activity) {

        mCountDownTimer.start();
        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityStopped(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(final Activity activity, final Bundle bundle) {

        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivitySaveInstanceState(activity, bundle);
        }
    }

    @Override
    public void onActivityDestroyed(final Activity activity) {

        if (mActivityLifecycleCallbacks != null) {
            mActivityLifecycleCallbacks.onActivityDestroyed(activity);
        }
    }

    public long getSessionTimeout() {
        return mSessionTimeout;
    }

    public long getSessionStartTime() {
        return mSessionStartTime;
    }

    public Application.ActivityLifecycleCallbacks getActivityLifecycleCallbacks() {
        return mActivityLifecycleCallbacks;
    }

    public void setActivityLifecycleCallbacks(final Application.ActivityLifecycleCallbacks activityLifecycleCallbacks) {
        mActivityLifecycleCallbacks = activityLifecycleCallbacks;
    }

    public SessionCallbacks getCallbacks() {
        return mCallbacks;
    }

    public void setCallbacks(SessionCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    /**
     * Interface that will receive callbacks when sessions start and end
     */
    public static interface SessionCallbacks {

        /**
         * Callback that will be called when the session starts
         *
         * @param startedAt The time, defined by {@link android.os.SystemClock#elapsedRealtime()}, when the session started
         */
        public void onSessionStarted(long startedAt);

        /**
         * Callback that will be called when the session ends
         *
         * @param endedAt               The time, defined by {@link android.os.SystemClock#elapsedRealtime()}, when the session started
         * @param sessionDurationMillis The duration of the session(in milliseconds)
         */
        public void onSessionEnded(long endedAt, long sessionDurationMillis);
    }
}
