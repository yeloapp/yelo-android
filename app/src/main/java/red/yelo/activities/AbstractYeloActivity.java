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
 */package red.yelo.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Bus;

import java.util.List;

import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.data.DBInterface;
import red.yelo.fragments.AbstractYeloFragment;
import red.yelo.fragments.FragmentTransition;
import red.yelo.http.RetroCallback;
import red.yelo.http.api.ChatApi;
import red.yelo.http.api.FacebookApi;
import red.yelo.http.api.GoogleApi;
import red.yelo.http.api.GoogleUserApi;
import red.yelo.http.api.YeloApi;
import red.yelo.utils.AppConstants;
import red.yelo.utils.AppConstants.UserInfo;
import red.yelo.utils.Logger;
import red.yelo.widgets.TypefaceCache;
import red.yelo.widgets.TypefacedSpan;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by anshul1235 on 14/07/14.
 */
public abstract class AbstractYeloActivity extends AppCompatActivity implements Callback
        , DialogInterface.OnClickListener, DBInterface.AsyncDbQueryCallback {

    private static final String TAG = "AbstractYeloActivity";

    private static final int ACTION_BAR_DISPLAY_MASK = ActionBar.DISPLAY_HOME_AS_UP
            | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO | ActionBar
            .DISPLAY_SHOW_HOME;

    private ActivityTransition mActivityTransition;

    /**
     * this holds the reference for the Otto Bus which we declared in LavocalApplication
     */
    protected Bus mBus;

    protected YeloApi mYeloApi;

    protected ChatApi mChatApi;

    protected GoogleApi mGoogleApi;

    protected GoogleUserApi mGoogleUserApi;

    protected FacebookApi mFacebookApi;

    private String mAppName;

    private static boolean mMainActivityIsOpen;

    private Toolbar mToolbar;

    private static final int FADE_CROSSOVER_TIME_MILLIS = 300;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        /* Here, getClass() might show an Ambiguous method call bug. It's a bug in IntelliJ IDEA 13
        * http://youtrack.jetbrains.com/issue/IDEA-72835 */
        mActivityTransition = getClass()
                .getAnnotation(ActivityTransition.class);

        if (savedInstanceState != null && mActivityTransition != null) {
            overridePendingTransition(mActivityTransition.createEnterAnimation(),
                    mActivityTransition
                            .createExitAnimation()
            );
        }
        super.onCreate(savedInstanceState);
        mAppName = getString(R.string.app_name);
        setTitle(getTitle());

        mBus = ((YeloApplication) getApplication()).getBus();
        mYeloApi = ((YeloApplication) getApplication()).getYeloApi();
        mChatApi = ((YeloApplication) getApplication()).getChatApi();
        mGoogleApi = ((YeloApplication) getApplication()).getGoogleApi();
        mGoogleUserApi = ((YeloApplication) getApplication()).getGoogleUserApi();
        mFacebookApi = ((YeloApplication) getApplication()).getFacebookApi();

        long lastScreenTime = 0l;

        if (savedInstanceState == null) {

        } else {
            lastScreenTime = savedInstanceState.getLong(AppConstants.Keys.LAST_SCREEN_TIME);
        }


//        mToolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
//
//        setToolbar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ACTION_BAR_DISPLAY_MASK);
        }


    }

    public ActionBar setToolbar(Toolbar toolbar) {

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().
                setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back);

        return getSupportActionBar();
    }

    public ActionBar setToolbar(Toolbar toolbar, String title, boolean isNavigationWhite) {

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (isNavigationWhite) {
            getSupportActionBar().
                    setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back_white);
        } else {
            getSupportActionBar().
                    setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back);
        }
        getSupportActionBar().setTitle(title);

        return getSupportActionBar();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mBus.register(this);
    }

    public static boolean mainActivityIsOpen() {
        return mMainActivityIsOpen;
    }

    public static void setMainActivityIsOpen(boolean mainActivityIsOpen) {
        mMainActivityIsOpen = mainActivityIsOpen;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    public void setTitle(CharSequence title) {

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {

            SpannableString spannable;
            if (mAppName.equals(title)) {

                spannable = new SpannableString(getSpannableLogoTitle(title));
                spannable.setSpan(new ImageSpan(this, R.drawable.bullet_inset, ImageSpan.ALIGN_BASELINE), spannable.length() - 1, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //spannable.setSpan(new RelativeSizeSpan(1.75f), spannable.length() - 1, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.yelo_red)), spannable.length() - 1, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            } else {
                spannable = new SpannableString(title);
            }
            /*spannable.setSpan(new ScaleXSpan(0.85f), 0, spannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);*/
            spannable.setSpan(new TypefacedSpan(this, TypefaceCache.OPEN_SANS_BOLD), 0, spannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            actionBar.setTitle(spannable);
        } else {
            super.setTitle(title);
        }
    }

    public void setTitle(String title, Toolbar toolbar) {
        if (toolbar != null) {

            SpannableString spannable;
            if (mAppName.equals(title)) {

                spannable = new SpannableString(getSpannableLogoTitle(title));
                spannable.setSpan(new ImageSpan(this, R.drawable.bullet_inset, ImageSpan.ALIGN_BASELINE), spannable.length() - 1, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //spannable.setSpan(new RelativeSizeSpan(1.75f), spannable.length() - 1, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                //spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.yelo_red)), spannable.length() - 1, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            } else {
                spannable = new SpannableString(title);
            }
            /*spannable.setSpan(new ScaleXSpan(0.85f), 0, spannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);*/
            spannable.setSpan(new TypefacedSpan(this, TypefaceCache.OPEN_SANS_BOLD), 0, spannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            toolbar.setTitle(spannable);
        }

    }


    public void setLocationTitleWithRedDot(String cityName, Toolbar toolbar, TextView locationText) {
        if (toolbar != null) {

            SpannableString spannable;

            spannable = new SpannableString(getSpannableLogoTitle(cityName));
            spannable.setSpan(new ImageSpan(this, R.drawable.bullet_inset, ImageSpan.ALIGN_BASELINE), spannable.length() - 1, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //spannable.setSpan(new RelativeSizeSpan(1.75f), spannable.length() - 1, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            //spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.yelo_red)), spannable.length() - 1, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            /*spannable.setSpan(new ScaleXSpan(0.85f), 0, spannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);*/
            spannable.setSpan(new TypefacedSpan(this, TypefaceCache.OPEN_SANS_BOLD), 0, spannable.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Update the action bar title with the TypefaceSpan instance
            //toolbar.setTitle(spannable);
            locationText.setText(spannable);
            if (cityName.length() > 13) {
                locationText.setTextSize(20);
            } else {
                locationText.setTextSize(26);

            }
        }

    }

    private String getSpannableLogoTitle(CharSequence title) {

        final StringBuilder builder = new StringBuilder(title.length() + 1);
        final String bullet = getString(R.string.bullet);
        builder.append(title).append(bullet);
        return builder.toString();
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    public Bus getBus() {
        return mBus;
    }

    public YeloApi getApiService() {
        return mYeloApi;
    }

    public ChatApi getChatApi() {
        return mChatApi;
    }

    public GoogleApi getGoogleApi() {
        return mGoogleApi;
    }

    public GoogleUserApi getGoogleUserApi() {
        return mGoogleUserApi;
    }

    public FacebookApi getFacebookApi() {
        return mFacebookApi;
    }


    /**
     * A Tag to add to all async requests. This must be unique for all Activity types
     *
     * @return An Object that's the tag for this fragment
     */
    protected abstract Object getTaskTag();

    @Override
    protected void onStop() {
        super.onStop();

        setProgressBarIndeterminateVisibility(false);
    }

    public void setActionBarDisplayOptions(final int displayOptions) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(displayOptions, ACTION_BAR_DISPLAY_MASK);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {


        //Fetch the current primary fragment. If that will handle the Menu click,
        // pass it to that one
        final AbstractYeloFragment currentMainFragment = (AbstractYeloFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.frame_content);

        boolean handled = false;
        if (currentMainFragment != null) {
            handled = currentMainFragment.onOptionsItemSelected(item);
        }

        if (!handled) {
            // To provide Up navigation
            if (item.getItemId() == android.R.id.home) {

                doUpNavigation();
                return true;
            } else {
                return super.onOptionsItemSelected(item);
            }

        }

        return handled;


    }

    /**
     * Moves up in the hierarchy using the Support meta data specified in manifest
     */
    private void doUpNavigation() {
        final Intent upIntent = NavUtils.getParentActivityIntent(this);

        if (upIntent == null) {

            NavUtils.navigateUpFromSameTask(this);

        } else {
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                // This activity is NOT part of this app's task, so create a
                // new
                // task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                        // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                                // Navigate up to the closest parent
                        .startActivities();
            } else {
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                NavUtils.navigateUpTo(this, upIntent);
            }
        }

    }


    /**
     * Helper method to load fragments into layout
     *
     * @param containerResId The container resource Id in the content view into which to load the
     *                       fragment
     * @param fragment       The fragment to load
     * @param tag            The fragment tag
     * @param addToBackStack Whether the transaction should be addded to the backstack
     * @param backStackTag   The tag used for the backstack tag
     */
    public void loadFragment(final int containerResId,
                             final AbstractYeloFragment fragment, final String tag,
                             final boolean addToBackStack, final String backStackTag) {

        loadFragment(containerResId, fragment, tag, addToBackStack, backStackTag, false);
    }


    /**
     * Helper method to load fragments into layout
     *
     * @param containerResId The container resource Id in the content view into which to load the
     *                       fragment
     * @param fragment       The fragment to load
     * @param tag            The fragment tag
     * @param addToBackStack Whether the transaction should be addded to the backstack
     * @param backStackTag   The tag used for the backstack tag
     * @param customAnimate  Whether to provide a custom animation for the Fragment. If
     *                       <code>true</code>, the Fragment also needs to be annotated with a
     *                       {@linkplain red.yelo.fragments.FragmentTransition} annotation which
     *                       describes the transition to perform. If <code>false</code>, will use
     *                       default fragment transition
     */
    public void loadFragment(final int containerResId,
                             final AbstractYeloFragment fragment, final String tag,
                             final boolean addToBackStack, final String backStackTag,
                             final boolean customAnimate) {

        loadFragment(containerResId, fragment, tag, addToBackStack, backStackTag, customAnimate, false);

    }


    /**
     * Helper method to load fragments into layout
     *
     * @param containerResId The container resource Id in the content view into which to load the
     *                       fragment
     * @param fragment       The fragment to load
     * @param tag            The fragment tag
     * @param addToBackStack Whether the transaction should be addded to the backstack
     * @param backStackTag   The tag used for the backstack tag
     */
    public void loadFragment(final int containerResId,
                             final AbstractYeloFragment fragment, final String tag,
                             final boolean addToBackStack, final String backStackTag,
                             final boolean customAnimate, final boolean remove) {


        final FragmentManager fragmentManager = getSupportFragmentManager();

        if (remove) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.beginTransaction().remove(fragment).commit();
            fragmentManager.executePendingTransactions();
        }
        final FragmentTransaction transaction = fragmentManager
                .beginTransaction();

        if (customAnimate) {
            final FragmentTransition fragmentTransition = fragment.getClass()
                    .getAnnotation(
                            FragmentTransition.class);
            if (fragmentTransition != null) {

                transaction
                        .setCustomAnimations(fragmentTransition.enterAnimation(), fragmentTransition
                                .exitAnimation(), fragmentTransition
                                .popEnterAnimation(), fragmentTransition
                                .popExitAnimation());

            }
        }


        transaction.replace(containerResId, fragment, tag);

        if (addToBackStack) {
            transaction.addToBackStack(backStackTag);
        }
        transaction.commit();
//        final FragmentManager fragmentManager = getSupportFragmentManager();
//        final FragmentTransaction transaction = fragmentManager
//                .beginTransaction();
//        final FragmentTransition fragmentTransition = fragment.getClass()
//                .getAnnotation(
//                        FragmentTransition.class);
//        if (fragmentTransition != null) {
//
//            transaction.setCustomAnimations(fragmentTransition.enterAnimation(), fragmentTransition
//                    .exitAnimation(), fragmentTransition
//                    .popEnterAnimation(), fragmentTransition
//                    .popExitAnimation());
//
//        }
//
//        transaction.replace(containerResId, fragment, tag);
//
//        if (addToBackStack) {
//            transaction.addToBackStack(backStackTag);
//        }
//        transaction.commit();
    }

    /**
     * Returns the current master fragment. In single pane layout, this is the fragment in the main
     * content. In a multi-pane layout, returns the fragment in the master container, which is the
     * one responsible for coordination
     *
     * @return <code>null</code> If no fragment is loaded,the {@link red.yelo.fragments.AbstractYeloFragment}
     * implementation which is the current master fragment otherwise
     */
    public AbstractYeloFragment getCurrentMasterFragment() {

        return (AbstractYeloFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frame_content);

    }

    /**
     * Is the user logged in
     */
    protected boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getFirstName());
    }

    protected boolean isVerified() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getAuthToken());
    }

    protected boolean isActivated() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getId());
    }


    @Override
    public void success(Object o, Response response) {

    }

    @Override
    public void failure(RetrofitError error) {

    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {

        final AbstractYeloFragment fragment = getCurrentMasterFragment();

        if ((fragment != null) && fragment.isVisible()) {
            if (fragment.willHandleDialog(dialog)) {
                fragment.onDialogClick(dialog, which);
            }
        }


    }


    @Override
    public void onBackPressed() {

        /* Get the reference to the current master fragment and check if that will handle
        onBackPressed. If yes, do nothing. Else, let the Activity handle it. */
        final AbstractYeloFragment masterFragment = getCurrentMasterFragment();

        boolean handled = false;
        if (masterFragment != null && masterFragment.isResumed()) {
            handled = masterFragment.onBackPressed();
        }

        if (!handled) {
            super.onBackPressed();
        }
    }


    @Override
    public void onInsertComplete(int taskId, Object cookie, long insertRowId) {

    }

    @Override
    public void onDeleteComplete(int token, Object cookie, int deleteCount) {
        switch (token) {
            case AppConstants.QueryTokens.DELETE_CHAT_MESSAGES: {
                Logger.v(TAG, "Deleted %d messages", deleteCount);
                break;
            }

            case AppConstants.QueryTokens.DELETE_CHATS: {
                Logger.v(TAG, "Deleted %d chats", deleteCount);
                break;
            }

            case AppConstants.QueryTokens.DELETE_MY_SERVICES: {
                Logger.v(TAG, "Deleted %d services", deleteCount);
                break;
            }

            default:
                break;
        }
    }

    @Override
    public void onUpdateComplete(int taskId, Object cookie, int updateCount) {

    }

    @Override
    public void onQueryComplete(int taskId, Object cookie, Cursor cursor) {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        //Setting background drawable to null to reduce overdraw
        if (hasFocus) {
            getWindow().setBackgroundDrawable(null);
        }
        super.onWindowFocusChanged(hasFocus);
    }


    public void colorizeActionBar(int color, final Toolbar toolbar) {
        int oldColor = getResources().getColor(R.color.white);

        if (toolbar != null) {
            Drawable toolbarDrawable = toolbar.getBackground();
            if (toolbarDrawable != null && toolbarDrawable instanceof ColorDrawable) {
                oldColor = ((ColorDrawable) toolbarDrawable).getColor();
            }
        }

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, color);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int color = (Integer) valueAnimator.getAnimatedValue();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(darkenColor(color));
                }
                toolbar.setBackgroundColor(color);
            }
        });
        colorAnimation.setDuration(FADE_CROSSOVER_TIME_MILLIS);
        colorAnimation.start();
    }

    public void colorizeView(int color, final View view) {
        int oldColor = getResources().getColor(R.color.white);

        if (view != null) {
            Drawable toolbarDrawable = view.getBackground();
            if (toolbarDrawable != null && toolbarDrawable instanceof ColorDrawable) {
                oldColor = ((ColorDrawable) toolbarDrawable).getColor();
            }
        }

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, color);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int color = (Integer) valueAnimator.getAnimatedValue();

                view.setBackgroundColor(color);
            }
        });
        colorAnimation.setDuration(FADE_CROSSOVER_TIME_MILLIS);
        colorAnimation.start();
    }

    private int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }


    public void cancelAllCallbacks(List<RetroCallback> retroCallbackList) {
        for (RetroCallback aRetroCallbackList : retroCallbackList) {
            aRetroCallbackList.cancel();
        }
    }

    public ProgressDialog showProgressDialog() {


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("Loading");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgress(0);
        return mProgressDialog;
    }

}
