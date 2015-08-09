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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Mayank Gautam
 *
 * This {@link android.content.ContentProvider} is used to provide access to the image
 * file for the camera to capture when SDCard is not present. This can also be used to
 * provide access when the SDCard is present.
 */
public class InternalFileContentProvider extends ContentProvider {

    public static final String CONTENT_URI_STR = "content://red.yelo/";
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_STR);
    public static final Uri PROFILE_PIC_URI = Uri.parse(CONTENT_URI_STR+AppConstants.AVATOR_PROFILE_NAME);
    public static final Uri POST_PIC_URI = Uri.parse(CONTENT_URI_STR+AppConstants.WALL_IMAGE_NAME);
    public static final Uri SERVICE_PIC_URI = Uri.parse(CONTENT_URI_STR+AppConstants.SERVICE_IMAGE);


    private static final HashMap<String, String> MIME_TYPES = new HashMap<String, String>();

    static {
        MIME_TYPES.put(".jpg", "image/jpeg");
        MIME_TYPES.put(".jpeg", "image/jpeg");
    }

    private static final String TAG = "InternalContentProvider";

    @Override
    public boolean onCreate() {
    /*    try{
            File mFile;
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                mFile = new File(getContext().getExternalFilesDir(null), AppConstants.AVATOR_PROFILE_NAME);
            }
            else
                mFile = new File(getContext().getFilesDir(),AppConstants.AVATOR_PROFILE_NAME);
            if(!mFile.exists())
            {
                mFile.createNewFile();
            }
        } catch (IOException e) {
            Log.e(TAG,"Unable to create the file.");
        }
        return false;
        */

        getContext().getContentResolver().notifyChange(CONTENT_URI,null);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public String getType(Uri uri) {
        String path = uri.toString();
        for(String ext : MIME_TYPES.keySet())
        {
            if(path.endsWith(ext))
            {
                return MIME_TYPES.get(ext);
            }
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {

        File mFile;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            mFile = new File(getContext().getExternalFilesDir(null),uri.getLastPathSegment());
        else
            mFile = new File(getContext().getFilesDir(),uri.getLastPathSegment());
        if(mFile.exists())
            return ParcelFileDescriptor.open(mFile,ParcelFileDescriptor.MODE_READ_WRITE);
        else
        {
            try{
                mFile.createNewFile();
                return ParcelFileDescriptor.open(mFile,ParcelFileDescriptor.MODE_READ_WRITE);
            } catch (IOException e) {
                Logger.e(TAG,"Unable to create file.");
            }
        }
        throw new FileNotFoundException(uri.getPath());
    }
}
