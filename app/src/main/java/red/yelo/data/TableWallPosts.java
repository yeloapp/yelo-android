

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
 */package red.yelo.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.Locale;

import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;


/**
 * @author Anshul Kamboj Table representing all the wall query messages
 */
public class TableWallPosts {

    private static final String TAG = "TableWallPosts";

    public static final String NAME = "WALL_POSTS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                .join(SQLConstants.COMMA, new String[]{
                        String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                        String.format(Locale.US, SQLConstants.DATA_TEXT_UK, DatabaseColumns.ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TEMP_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CITY, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.COUNTRY, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.ADDRESS, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.MESSAGE, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.USER_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.GROUP_NAME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.GROUP_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TAG_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.COLOR, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TAG_NAME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TAG_USER_COUNT, "0"),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CHAT_USER_COUNT, "0"),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.COMMENT_USER_COUNT, "0"),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.USER_NAME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.USER_IMAGE, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.WALL_IMAGES, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.DATE_TIME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP_EPOCH, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP_EPOCH_UPDATED_AT, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP_HUMAN, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.STATE, AppConstants.SyncStates.SYNCED + ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.STATUS, AppConstants.WallStatus.OPEN)


                });
        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                               final int newVersion) {

        //Add any data migration code here. Default is to drop and rebuild the table
        if (oldVersion < 15) {

            /*
             * Drop & recreate the table if upgrading from DB version 1(alpha
             * version)
             */
            db.execSQL(String
                    .format(Locale.US, SQLConstants.DROP_TABLE_IF_EXISTS, NAME));
            create(db);

        }
    }
}
