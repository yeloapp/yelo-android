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

package red.yelo.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import red.yelo.R;


/**
 * Class that transforms a Bitmap into a square based on the shortest dimension and a desired avatar
 * size.
 * <p/>
 * This class works by taking desired avatar size(in pixels) in the constructor. When the Bitmap is
 * passed to it, it scales the Bitmap's shortest dimension to the avatar size, maintaining the
 * aspect ratio. In then crops the Bitmap to form a square image
 * <p/>
 * Created by vinay.shenoy on 24/10/14.
 */
public class AvatarBitmapTransformation extends BitmapTransformation {

    private static final String TAG = "AvatarBitmapTransformation";

    private Context mContext;

    private static final Map<AvatarSize, AvatarBitmapTransformation> TRANSFORMATION_CACHE = new HashMap<AvatarSize, AvatarBitmapTransformation>((int) (AvatarSize.values().length * 1.33f));

    /**
     * We test if an image is square or not by comparing the width to the height. In some cases, the
     * image might be almost square in which case, it is okay not to crop it and just scale it.
     * <p/>
     * This field will be used to control how to decide whether an image is square or not. In case
     * the ratio of the shortest is >= this value, it is taken as a square image and not cropped
     */
    private static final float MAX_ALLOWED_SQUARE_FACTOR = 0.95f;


    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        final boolean alreadySquare = isSquareImage(toTransform.getWidth(), toTransform.getHeight());

        final int avatarSizeInPixels = mContext.getResources().getDimensionPixelSize(mAvatarSize.dimenResId);
        final int shortestWidth = Math.min(toTransform.getWidth(), toTransform.getHeight());
        final float scaleFactor = avatarSizeInPixels / (float) shortestWidth;

        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                toTransform,
                (int) (toTransform.getWidth() * scaleFactor),
                (int) (toTransform.getHeight() * scaleFactor),
                true);

        if (alreadySquare) {
            //Already square, no need to crop
            return scaledBitmap;
        } else {
            final int size = Math.min(scaledBitmap.getWidth(), scaledBitmap.getHeight());

            //Start x,y positions to crop the bitmap
            int x = 0;
            int y = 0;

            if (size == scaledBitmap.getWidth()) {
                /* Portrait picture, crop the bottom and top parts */
                y = (scaledBitmap.getHeight() - size) / 2;

            } else {

                /* Landscape picture, crop the right and left zones */
                x = (scaledBitmap.getWidth() - size) / 2;
            }
            return Bitmap.createBitmap(scaledBitmap, x, y, size, size);
        }
    }

    @Override
    public String getId() {
        return String.format(Locale.US, "red.yelo.AvatarBitmapTransformation.%s", mAvatarSize.toString());
    }

    /**
     * An enum that indicates the class of avatar this transformation generates
     */
    public enum AvatarSize {
        EDIT_PROFILE(R.dimen.avatar_editprofile),
        LARGE(R.dimen.avatar_large),
        BIG(R.dimen.avatar_big),
        NORMAL(R.dimen.avatar_normal),
        SMALL(R.dimen.avatar_small),
        TINY(R.dimen.avatar_tiny);


        public final int dimenResId;

        private AvatarSize(int dimenResId) {
            this.dimenResId = dimenResId;
        }
    }

    /**
     * Desired avatar size
     */
    private AvatarSize mAvatarSize;

    /**
     * Creates an Avatar bitmap transformation with a default avatar sise of {@link red.yelo.utils.AvatarBitmapTransformation.AvatarSize#NORMAL}
     *
     * @param context A reference to the Application Context
     */
    public AvatarBitmapTransformation(final Context context) {
        super(context);
        mContext = context;
        mAvatarSize = AvatarSize.NORMAL;
    }

    /**
     * Construct an instance of AvatarTransformation method, setting the desired avatar size
     *
     * @param context    A reference to the Application Context
     * @param avatarSize The class of avatar size you want to generate
     */
    public AvatarBitmapTransformation(final Context context, final AvatarSize avatarSize) {
        this(context);
        mAvatarSize = (avatarSize == null) ? AvatarSize.NORMAL : avatarSize;
    }

    /**
     * Checks if the given image is a square or not
     *
     * @param sourceWidth  The width of the source image
     * @param sourceHeight The height of the source image
     */
    private static boolean isSquareImage(final int sourceWidth, final int sourceHeight) {

        if (sourceHeight > sourceWidth) {
            return (sourceWidth / sourceHeight) >= MAX_ALLOWED_SQUARE_FACTOR;
        } else {
            return (sourceHeight / sourceWidth) >= MAX_ALLOWED_SQUARE_FACTOR;
        }

    }

    /**
     * Gets an Avatar Bitmap Transformation object for a particular size
     *
     * @param applicationContext A reference to the app context
     * @param avatarSize         The class of avatar size you want to generate
     */
    public static AvatarBitmapTransformation transformationFor(final Context applicationContext, final AvatarSize avatarSize) {

        if (TRANSFORMATION_CACHE.get(avatarSize) == null) {
            synchronized (AvatarBitmapTransformation.class) {
                if (TRANSFORMATION_CACHE.get(avatarSize) == null) {
                    TRANSFORMATION_CACHE.put(avatarSize, new AvatarBitmapTransformation(applicationContext, avatarSize));
                }
            }
        }

        return TRANSFORMATION_CACHE.get(avatarSize);
    }
}
