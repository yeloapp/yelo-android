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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.internal.view.menu.ActionMenuItemView;
import android.support.v7.internal.widget.TintImageView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ActionMenuView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.melnykov.fab.FloatingActionButton;
import com.vinaysshenoy.okulus.OkulusImageView;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import red.yelo.R;
import red.yelo.YeloApplication;
import red.yelo.analytics.MixpanelAnalytics;
import red.yelo.data.DatabaseColumns;
import red.yelo.utils.AppConstants.DeviceInfo;

/**
 * Utility methods for yelo
 */
public class Utils {

    private static final String TAG = "Utils";

    public static float density = 1;

    public static final int SCALE_FACTOR = 30;


    /**
     * Checks if the current thread is the main thread or not
     *
     * @return <code>true</code> if the current thread is the main/UI thread, <code>false</code>
     * otherwise
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * Makes an SHA1 Hash of the given string
     *
     * @param string The string to shash
     * @return The hashed string
     * @throws java.security.NoSuchAlgorithmException
     */
    public static String sha1(final String string)
            throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        final byte[] data = digest.digest(string.getBytes());
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    /**
     * Registers the referral values in analytics
     */
    public static void registerReferralValuesInAnalytics() {

        final String utmSource = SharedPreferenceHelper.getString(R.string.pref_utm_source, "Google Play");
        final String utmCampaign = SharedPreferenceHelper.getString(R.string.pref_utm_campaign, "discovery");
        final String utmMedium = SharedPreferenceHelper.getString(R.string.pref_utm_medium, "App Store");
        final String utmContent = SharedPreferenceHelper.getString(R.string.pref_utm_content, "Store Listing");
        final String utmTerm = SharedPreferenceHelper.getString(R.string.pref_utm_term, "Store Listing");

        MixpanelAnalytics.getInstance().setReferralInfo(utmSource, utmCampaign, utmMedium, utmContent, utmTerm);
    }

    /**
     * Reads the network info from service and sets up the singleton
     * return typechanged flag
     */
    public static boolean setupNetworkInfo(final Context context) {

        boolean mTypeChanged;
        final ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (DeviceInfo.INSTANCE.getCurrentNetworkType() == activeNetwork.getType()) {
                mTypeChanged = false;
            } else {
                mTypeChanged = true;
            }
            DeviceInfo.INSTANCE.setNetworkConnected(activeNetwork
                    .isConnectedOrConnecting());
            DeviceInfo.INSTANCE.setCurrentNetworkType(activeNetwork.getType());
        } else {
            DeviceInfo.INSTANCE.setNetworkConnected(false);
            DeviceInfo.INSTANCE
                    .setCurrentNetworkType(ConnectivityManager.TYPE_DUMMY);
        }

        Logger.d(TAG, "Network State Updated Connected: %b Type: %d", DeviceInfo.INSTANCE.isNetworkConnected(), DeviceInfo.INSTANCE.getCurrentNetworkType());

        return true;

    }

    public static boolean copyFile(final File src, final File dst) {
        boolean returnValue = true;

        FileChannel inChannel = null, outChannel = null;

        try {

            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();

        } catch (final FileNotFoundException fnfe) {

            Logger.d(TAG, "inChannel/outChannel FileNotFoundException");
            fnfe.printStackTrace();
            return false;
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);

        } catch (final IllegalArgumentException iae) {

            Logger.d(TAG, "TransferTo IllegalArgumentException");
            iae.printStackTrace();
            returnValue = false;

        } catch (final NonReadableChannelException nrce) {

            Logger.d(TAG, "TransferTo NonReadableChannelException");
            nrce.printStackTrace();
            returnValue = false;

        } catch (final NonWritableChannelException nwce) {

            Logger.d(TAG, "TransferTo NonWritableChannelException");
            nwce.printStackTrace();
            returnValue = false;

        } catch (final ClosedByInterruptException cie) {

            Logger.d(TAG, "TransferTo ClosedByInterruptException");
            cie.printStackTrace();
            returnValue = false;

        } catch (final AsynchronousCloseException ace) {

            Logger.d(TAG, "TransferTo AsynchronousCloseException");
            ace.printStackTrace();
            returnValue = false;

        } catch (final ClosedChannelException cce) {

            Logger.d(TAG, "TransferTo ClosedChannelException");
            cce.printStackTrace();
            returnValue = false;

        } catch (final IOException ioe) {

            Logger.d(TAG, "TransferTo IOException");
            ioe.printStackTrace();
            returnValue = false;

        } finally {

            if (inChannel != null) {
                try {

                    inChannel.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return returnValue;
    }

    /**
     * Generate a user's name from the first name last name
     *
     * @param firstName
     * @param lastName
     * @return
     */
    public static String makeUserFullName(String firstName, String lastName) {

        if (TextUtils.isEmpty(firstName)) {
            return "";
        }

        final StringBuilder builder = new StringBuilder(firstName);

        if (!TextUtils.isEmpty(lastName)) {
            builder.append(" ").append(lastName);
        }
        return builder.toString();
    }


    /**
     * Generates as chat ID which will be unique for a given sender/receiver pair
     *
     * @param receiverId The receiver of the chat
     * @param senderId   The sender of the chat
     * @return The chat Id
     */
    public static String generateChatId(final String receiverId,
                                        final String senderId) {

        /*
         * Method of generating the chat ID is simple. First we compare the two
         * ids and combine them in ascending order separate by a '#'. Then we
         * SHA1 the result to make the chat id
         */

        String combined = null;
        if (receiverId.compareTo(senderId) < 0) {
            combined = String
                    .format(Locale.US, AppConstants.CHAT_ID_FORMAT, receiverId, senderId);
        } else {
            combined = String
                    .format(Locale.US, AppConstants.CHAT_ID_FORMAT, senderId, receiverId);
        }

        String hashed = null;

        try {
            hashed = Utils.sha1(combined);
        } catch (final NoSuchAlgorithmException e) {
            /*
             * Shouldn't happen sinch SHA-1 is standard, but in case it does use
             * the combined string directly since they are local chat IDs
             */
            hashed = combined;
        }

        return hashed;
    }

    /**
     * Gets the distance between two Locations(in metres)
     *
     * @param start The start location
     * @param end   The end location
     * @return The distance between two locations(in metres)
     */
    public static float distanceBetween(final Location start, final Location end) {

        final float[] results = new float[1];
        Location.distanceBetween(start.getLatitude(), start.getLongitude(), end
                .getLatitude(), end.getLongitude(), results);
        return results[0];
    }

    /**
     * Gets the current epoch time. Is dependent on the device's H/W time.
     */
    public static long getCurrentEpochTime() {
        return System.currentTimeMillis() / 1000;
    }


    /**
     * Converts a cursor to a bundle. Field datatypes will be maintained. Floats will be stored in
     * the Bundle as Doubles, and Integers will be stored as Longs due to Cursor limitationcs
     *
     * @param cursor The cursor to convert to a Bundle. This must be positioned to the row to be
     *               read
     * @return The converted bundle
     */
    public static Bundle cursorToBundle(Cursor cursor) {

        final int columnCount = cursor.getColumnCount();
        final Bundle bundle = new Bundle(columnCount);

        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {

            final String columnName = cursor.getColumnName(columnIndex);
            switch (cursor.getType(columnIndex)) {

                case Cursor.FIELD_TYPE_STRING: {
                    bundle.putString(columnName, cursor.getString(columnIndex));
                    break;
                }

                case Cursor.FIELD_TYPE_BLOB: {
                    bundle.putByteArray(columnName, cursor.getBlob(columnIndex));
                    break;
                }

                case Cursor.FIELD_TYPE_FLOAT: {
                    bundle.putDouble(columnName, cursor.getDouble(columnIndex));
                    break;
                }

                case Cursor.FIELD_TYPE_INTEGER: {
                    bundle.putLong(columnName, cursor.getLong(columnIndex));
                    break;
                }
            }
        }

        return bundle;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) {
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    /**
     * Creates an intent for sharing the app
     *
     * @param context
     * @return
     */
    public static Intent createAppShareIntent(Context context, String message, String appDownloadLink) {

        final String messageShare = context.getString(R.string.share_message_format, message, appDownloadLink);
        return createShareIntent(context, messageShare);
    }

    /**
     * Creates an intent for sharing the app
     *
     * @param context
     * @return
     */
    public static Intent createTagShareIntent(Context context, String tagName, String category, String appDownloadLink) {

        final String messageShare = context.getString(R.string.share_tag_message_format, tagName, category, appDownloadLink);
        return createShareIntentForTag(context, messageShare);
    }


    public static String getShareLink() {

        final StringBuilder shareLinkBuilder = new StringBuilder(384);
        shareLinkBuilder.append(AppConstants.PLAY_STORE_MARKET_LINK);

        String referrerValue = String.format(Locale.US, AppConstants.REFERRER_VALUE, AppConstants.POST_SHARE, AppConstants.APP_VIRALITY, AppConstants.ANDROID_APP, AppConstants.CHECK_THIS_OUT);

        final String shareToken = SharedPreferenceHelper.getString(R.string.pref_share_token);
        if (!TextUtils.isEmpty(shareToken)) {
            referrerValue = String.format(Locale.US, "%s&share_token=%s", referrerValue, shareToken);
        }

        String referrer;
        try {
            referrer = String.format(Locale.US, AppConstants.REFERRER_FORMAT, URLEncoder.encode(referrerValue, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            referrer = null;
        }

        if (!TextUtils.isEmpty(referrer)) {
            shareLinkBuilder.append('&').append(referrer);
        }

        return shareLinkBuilder.toString();
    }


    public static String getShareLinkForTag() {

        final StringBuilder shareLinkBuilder = new StringBuilder(384);
        shareLinkBuilder.append(AppConstants.PLAY_STORE_MARKET_LINK);

        //String referrerValue = String.format(Locale.US, AppConstants.REFERRER_VALUE, AppConstants.POST_SHARE, AppConstants.APP_VIRALITY, AppConstants.ANDROID_APP, AppConstants.CHECK_THIS_OUT);
        String referrerValue = "";
        final String shareToken = SharedPreferenceHelper.getString(R.string.pref_share_token);
        if (!TextUtils.isEmpty(shareToken)) {
            referrerValue = String.format(Locale.US, "%s", shareToken);
        }

        String referrer;
        try {
            referrer = String.format(Locale.US, AppConstants.REFERRER_FORMAT, URLEncoder.encode(referrerValue, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            referrer = null;
        }

        if (!TextUtils.isEmpty(referrer)) {
            shareLinkBuilder.append('&').append(referrer);
        }

        return shareLinkBuilder.toString();
    }

    /**
     * Creates a share intent
     *
     * @param context
     * @param shareText The text to share
     * @return
     */
    private static Intent createShareIntent(Context context,
                                            final String shareText) {

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject));

        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);


        shareIntent.setType("text/plain");

        return shareIntent;
    }

    /**
     * Creates a share intent
     *
     * @param context
     * @param shareText The text to share
     * @return
     */
    private static Intent createShareIntentForTag(Context context,
                                                  final String shareText) {

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);

        // try {
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, context.getString(R.string.share_tag_subject));

//        PackageManager pm=  context.getPackageManager();
//
//        PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
//        //Check if package exists or not. If not then code
//        //in catch block will be called
//            shareIntent.setPackage("com.whatsapp");

        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);


        shareIntent.setType("text/plain");
//    } catch (PackageManager.NameNotFoundException e) {
//        Toast.makeText(context, "WhatsApp not Installed", Toast.LENGTH_SHORT)
//                .show();
//    }
        return shareIntent;
    }

    /**
     * Splits a time interval into hours, minutes and seconds
     *
     * @param time The time interval to split(in seconds)
     * @return An array with capacity 3, with the different variables in each of the positions
     * <p/>
     * <ul>
     * <li>0 - Hours</li>
     * <li>1 - Minutes</li>
     * <li>2 - Seconds</li>
     * </ul>
     */
    public static int[] getHoursMinsSecs(long time) {
        int hours = (int) time / 3600;
        int remainder = (int) time - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        return new int[]{hours, mins, secs};
    }

    public static String getEmail(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = getAccount(accountManager);
        if (account == null) {
            return null;
        } else {
            return account.name;
        }
    }
    private static Account getAccount(AccountManager accountManager) {
        Account[] accounts = accountManager.getAccountsByType("com.google");
        Account account;
        if (accounts.length > 0) {
            account = accounts[0];
        } else {
            account = null;
        } return account;
    }

    /**
     * Checks whether the given user id is the current user or not
     *
     * @param userId The user id to check
     * @return {@code true} if the user is the current user, {@code false} otherwise
     */
    public static boolean isCurrentUser(final String userId) {

        final String currentUserId = AppConstants.UserInfo.INSTANCE.getId();
        return !TextUtils.isEmpty(currentUserId) && currentUserId.equals(userId);
    }


    /**
     * Loads an image into the OkulusImageView
     *
     * @param context    A reference to the context
     * @param imageView  The ImageView to load the bitmap into
     * @param imageUrl   The image url to load
     * @param avatarSize The size to scale the image to
     */
    public static void loadCircularImage(Context context, OkulusImageView imageView, String imageUrl, AvatarBitmapTransformation.AvatarSize avatarSize) {


        Glide.with(context)
                .load(imageUrl)
                .asBitmap()
                .animate(R.anim.fade_in)
                .transform(AvatarBitmapTransformation.transformationFor(context, avatarSize))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);

    }

    /**
     * Loads an image into the OkulusImageView
     *
     * @param context    A reference to the context
     * @param imageView  The ImageView to load the bitmap into
     * @param imageUrl   The image url to load
     * @param avatarSize The size to scale the image to
     */
    public static void loadCircularImageForEditProfile(final Context context, final OkulusImageView imageView, String imageUrl, AvatarBitmapTransformation.AvatarSize avatarSize, View view) {


        final ImageView addView = (ImageView) view.findViewById(R.id.gallery_ic);
        final TextView textView = (TextView) view.findViewById(R.id.add_image_text);

        imageView.setImageBitmap(null);

        Glide.with(context)
                .load(imageUrl)
                .asBitmap()
                .animate(R.anim.fade_in)
                .transform(AvatarBitmapTransformation.transformationFor(context, avatarSize))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        addView.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);


                        imageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.light_grey_image));

                        return true;
                    }


                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);

    }

    /**
     * Loads an image into the OkulusImageView
     *
     * @param context    A reference to the context
     * @param imageView  The ImageView to load the bitmap into
     * @param imageUrl   The image url to load
     * @param avatarSize The size to scale the image to
     */
    public static void loadCircularImage(Context context, OkulusImageView imageView, String imageUrl, AvatarBitmapTransformation.AvatarSize avatarSize, TextDrawable drawable) {



        Glide.with(context)
                .load(imageUrl)
                .asBitmap()
                .animate(R.anim.fade_in)
                .placeholder(drawable)
                .transform(AvatarBitmapTransformation.transformationFor(context, avatarSize))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(drawable)
                .into(imageView);
    }

    /**
     * Loads an image into the ImageView
     *
     * @param context   A reference to the context
     * @param imageView The ImageView to load the bitmap into
     * @param imageUrl  The image url to load
     */
    public static void loadImage(Context context, ImageView imageView, String imageUrl, TextDrawable drawable) {


        Glide.with(context)
                .load(imageUrl)
                .asBitmap()
                .animate(R.anim.fade_in)
                .placeholder(drawable)
                .centerCrop()
                .error(drawable)
                .into(imageView);
    }

    /**
     * Loads an image into the OkulusImageView
     *
     * @param context    A reference to the context
     * @param imageView  The ImageView to load the bitmap into
     * @param image      The drawable to load
     * @param avatarSize The size to scale the image to
     */
    public static void loadCircularImage(Context context, OkulusImageView imageView, Drawable image, AvatarBitmapTransformation.AvatarSize avatarSize) {

        Glide.with(context)
                .load(image)
                .asBitmap()
                .placeholder(R.drawable.ic_placeholder_profile)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(R.drawable.ic_placeholder_profile)
                .into(imageView);
    }

    /**
     * Loads an image into the OkulusImageView
     *
     * @param context         A reference to the context
     * @param imageView       The ImageView to load the bitmap into
     * @param imageResourceId The image resource to load
     * @param avatarSize      The size to scale the image to
     */
    public static void loadCircularImage(Context context, OkulusImageView imageView, int imageResourceId, AvatarBitmapTransformation.AvatarSize avatarSize) {

        Glide.with(context)
                .load(imageResourceId)
                .asBitmap()
                .transform(AvatarBitmapTransformation.transformationFor(context, avatarSize))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(new ViewTarget<OkulusImageView, Bitmap>(imageView) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation anim) {
                        OkulusImageView myView = this.view;
                        myView.setImageBitmap(resource);
                        // Set your resource on myView and/or start your animation here.
                    }

                });
    }

    /**
     * Sets the compound drawables for the TextViews
     *
     * @param textView The TextView to set compound drawables for
     * @param left     The left drawable resource id
     * @param top      The top drawable resource id
     * @param right    The right drawable resource id
     * @param bottom   The bottom drawable resource id
     */
    public static void setCompoundDrawables(final TextView textView, final int left, final int top, final int right, final int bottom) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(left, top, right, bottom);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        }
    }

    /**
     * Sets the compound drawables for the TextViews
     *
     * @param textView The TextView to set compound drawables for
     * @param left     The left drawable
     * @param top      The top drawable
     * @param right    The right drawable
     * @param bottom   The bottom drawable
     */
    public static void setCompoundDrawables(final TextView textView, final Drawable left, final Drawable top, final Drawable right, final Drawable bottom) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(left, top, right, bottom);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        }
    }

    /**
     * Checks whether a number is a valid phone number or not
     *
     * @param number The number to validate
     */
    public static boolean isValidPhoneNumber(String number) {

        final PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            final Phonenumber.PhoneNumber phoneNumber = util.parse(number, getSimCountryCode());
            //Using the | case is a quick hack to check validity of numbers which are returned wrongly as PhoneNumberUtil as invalid(fix for India)
            return (util.isValidNumber(phoneNumber)) | (String.valueOf(phoneNumber.getNationalNumber()).length() == 10);
        } catch (NumberParseException e) {
            return false;
        }
    }

    public static String getSimCountryCode() {

        TelephonyManager manager = (TelephonyManager) (YeloApplication.getStaticContext().getSystemService(Context.TELEPHONY_SERVICE));
        return manager.getSimCountryIso().toUpperCase();
    }

    public static String decrypt(String key, String encrypted) {
        try {


            encrypted.trim();
            Key k = new SecretKeySpec(key.getBytes(), "SHA256");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, k);
            byte[] decodedValue = Base64.decode(encrypted, Base64.DEFAULT);
            byte[] decValue = c.doFinal(decodedValue);
            String decryptedValue = new String(decValue);
            return decryptedValue;
        } catch (IllegalBlockSizeException ex) {
            ex.printStackTrace();
        } catch (BadPaddingException ex) {
            ex.printStackTrace();

        } catch (InvalidKeyException ex) {
            ex.printStackTrace();

        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();

        } catch (NoSuchPaddingException ex) {
            ex.printStackTrace();

        }
        return null;
    }


    public static Intent getPickImageIntent(final Context context) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
        }
        return Intent.createChooser(intent, "Select picture");
    }

    public static void setNetworkAvailableWithPing() {
        // ask fo message '0' (not connected) or '1' (connected) on 'handler'
        // the answer must be send before before within the 'timeout' (in milliseconds)

        final int timeout = 4000;

        final Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what != 1) { // code if not connected
                    DeviceInfo.INSTANCE.setNetworkConnected(false);
                    Logger.d("Handle", "PING Failed");

                } else { // code if connected
                    DeviceInfo.INSTANCE.setNetworkConnected(true);
                    if (DeviceInfo.INSTANCE.isNetworkConnected()) {
                        //YeloApplication.startChatService();
                    }
                    Logger.d("Handle", "PING Success");
                }
            }
        };

        new Thread() {
            private boolean responded = false;

            @Override
            public void run() {
                // set 'responded' to TRUE if is able to connect with google mobile (responds fast)
                new Thread() {
                    @Override
                    public void run() {
                        HttpGet requestForTest = new HttpGet("http://m.google.com");
                        try {
                            new DefaultHttpClient().execute(requestForTest); // can last...
                            responded = true;
                        } catch (Exception e) {
                        }
                    }
                }.start();

                try {
                    int waited = 0;
                    while (!responded && (waited < timeout)) {
                        sleep(100);
                        if (!responded) {
                            waited += 100;
                        }
                    }
                } catch (InterruptedException e) {
                } // do nothing
                finally {
                    if (!responded) {
                        h.sendEmptyMessage(0);
                    } else {
                        h.sendEmptyMessage(1);
                    }
                }
            }
        }.start();
    }

    public static int dp(float value) {
        return (int) Math.ceil(density * value);
    }

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

    public static void hideShowViewByScale(final View view, final int icon) {

        ViewPropertyAnimator propertyAnimator = view.animate().setStartDelay(SCALE_FACTOR)
                .scaleX(0).scaleY(0);

        propertyAnimator.setDuration(300);
        propertyAnimator.start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                ((FloatingActionButton) view).setImageResource(icon);
                showViewByScale(view);
            }
        }, 300);

    }

    public static void showViewByScale(View view) {

        ViewPropertyAnimator propertyAnimator = view.animate().setStartDelay(SCALE_FACTOR)
                .scaleX(1).scaleY(1);

        propertyAnimator.setDuration(300);

        propertyAnimator.start();
    }

    public static void openWhatsappContact(String number, Context context) {
        Uri uri = Uri.parse("smsto:" + "+91" + number);
        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
        i.setPackage("com.whatsapp");
        context.startActivity(Intent.createChooser(i, ""));

//        Intent whatsapp = new Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.contacts/data/" + number+"@s.whatsapp.net"));
//        //i.setPackage("com.whatsapp");
//        context.startActivity(whatsapp);
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }


    /**
     * Use this method to colorize toolbar icons to the desired target color
     *
     * @param toolbarView       toolbar view being colored
     * @param toolbarIconsColor the target color of toolbar icons
     * @param activity          reference to activity needed to register observers
     */
    public static void colorizeToolbar(Toolbar toolbarView, int toolbarIconsColor, Activity activity) {
        final PorterDuffColorFilter colorFilter
                = new PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.MULTIPLY);

        for (int i = 0; i < toolbarView.getChildCount(); i++) {
            final View v = toolbarView.getChildAt(i);

            //Step 1 : Changing the color of back button (or open drawer button).
            if (v instanceof ImageButton) {
                //Action Bar back button
                ((ImageButton) v).getDrawable().setColorFilter(colorFilter);
            }

            if (v instanceof ActionMenuView) {
                for (int j = 0; j < ((ActionMenuView) v).getChildCount(); j++) {

                    //Step 2: Changing the color of any ActionMenuViews - icons that
                    //are not back button, nor text, nor overflow menu icon.
                    final View innerView = ((ActionMenuView) v).getChildAt(j);

                    if (innerView instanceof ActionMenuItemView) {
                        int drawablesCount = ((ActionMenuItemView) innerView).getCompoundDrawables().length;
                        for (int k = 0; k < drawablesCount; k++) {
                            if (((ActionMenuItemView) innerView).getCompoundDrawables()[k] != null) {
                                final int finalK = k;

                                //Important to set the color filter in seperate thread,
                                //by adding it to the message queue
                                //Won't work otherwise.
                                innerView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((ActionMenuItemView) innerView).getCompoundDrawables()[finalK].setColorFilter(colorFilter);
                                    }
                                });
                            }
                        }
                    }
                }
            }

            //Step 3: Changing the color of title and subtitle.
            toolbarView.setTitleTextColor(toolbarIconsColor);
            toolbarView.setSubtitleTextColor(toolbarIconsColor);

            //Step 4: Changing the color of the Overflow Menu icon.
            setOverflowButtonColor(activity, colorFilter);
        }


    }

    /**
     * It's important to set overflowDescription atribute in styles, so we can grab the reference
     * to the overflow icon. Check: res/values/styles.xml
     *
     * @param activity
     * @param colorFilter
     */
    private static void setOverflowButtonColor(final Activity activity, final PorterDuffColorFilter colorFilter) {
        final String overflowDescription = activity.getString(R.string.abc_action_menu_overflow_description);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<View>();
                decorView.findViewsWithText(outViews, overflowDescription,
                        View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (outViews.isEmpty()) {
                    return;
                }
                TintImageView overflow = (TintImageView) outViews.get(0);
                overflow.setColorFilter(colorFilter);
                removeOnGlobalLayoutListener(decorView, this);
            }
        });
    }


    private static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }


    /**
     * This function returns the formatted time.
     *
     * @param timeEpoch
     * @param cursor
     * @param context
     * @return the formatted time
     */
    public static String getElapsedTimeFormat(long timeEpoch, Cursor cursor, Context context) {

        long timeElapsed = Utils.getCurrentEpochTime() - timeEpoch;

        int[] timeValues = Utils.getHoursMinsSecs(timeElapsed);

        final int hours = timeValues[0];
        final int minutes = timeValues[1];
        final int seconds = timeValues[2];
        final int days = hours / 24;
        final int weeks = days / 7;


        if (hours < 1) {
            if (minutes < 1) {
                if (seconds < 10) {
                    return context.getString(R.string.just_now);
                } else {
                    return context.getString(R.string.seconds_ago, seconds);
                }

            } else {
                return context.getString(R.string.minutes_ago, minutes);
            }
        } else if (hours < 23) {
            return context.getString(R.string.hours_ago, hours);

        } else if (hours > 23 && hours < 167) {

            return context.getString(R.string.days_ago, days);


        } else if (weeks > 0) {
            return context.getString(R.string.weeks_ago, weeks);
        } else {
            return cursor.getString(cursor
                    .getColumnIndex(DatabaseColumns.TIMESTAMP_HUMAN));
        }
    }

    /**
     * Function used to get the storage directory of the app based on memory card present or not.
     *
     * @param context Context of the current activity.
     */
    public static File getStorageDirectory(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return context.getExternalFilesDir(null);
        else
            return context.getFilesDir();
    }


    public static void hideViewByScale(final View view) {
        ViewPropertyAnimator propertyAnimator = view.animate().setStartDelay(SCALE_FACTOR)
                .scaleX(0).scaleY(0);
        propertyAnimator.setDuration(300);
        propertyAnimator.start();
    }


    public static void shareImageAsBitmap(Bitmap shareImage,Context context,String message){
        Bitmap icon = shareImage;


        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "title");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);


        OutputStream outstream;
        if(icon!=null) {
            try {
                outstream = context.getContentResolver().openOutputStream(uri);
                icon.compress(Bitmap.CompressFormat.PNG, 100, outstream);
                outstream.close();
            } catch (Exception e) {
                System.err.println(e.toString());
            }

            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.putExtra(Intent.EXTRA_TEXT, message);

            context.startActivity(Intent.createChooser(share, "Share Image"));
        }

    }

    public static void setDefaultLocation(Context context){
        String[] cityNames = context.getResources().getStringArray(R.array.city_names);
        String[] cityLatitudes = context.getResources().getStringArray(R.array.city_latitudes);
        String[] cityLongitudes = context.getResources().getStringArray(R.array.city_longitudes);

        SharedPreferenceHelper.set(R.string.pref_latitude, cityLatitudes[0]);
        SharedPreferenceHelper.set(R.string.pref_longitude, cityLongitudes[0]);
        SharedPreferenceHelper.set(R.string.pref_location, cityNames[0]);
        SharedPreferenceHelper.set(R.string.pref_city, cityNames[0]);
    }

}
