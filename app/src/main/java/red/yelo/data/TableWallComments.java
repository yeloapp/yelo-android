

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
 * @author Anshul Kamboj Table representing all the wall comments
 */
public class TableWallComments {

    private static final String TAG = "TableWallComments";

    public static final String NAME = "WALL_COMMENTS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                .join(SQLConstants.COMMA, new String[]{
                        String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                        String.format(Locale.US, SQLConstants.DATA_TEXT_UK, DatabaseColumns.ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.WALL_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TEMP_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.DATE_TIME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP_EPOCH, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TIMESTAMP_HUMAN, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.STATE, AppConstants.SyncStates.SYNCING),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.COMMENT, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.WALL_USER_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.USER_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.USER_NAME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.IMAGE_URL, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TAGGED_USER_IDS, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TAGGED_IDS, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TAGGED_NAMES, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TAGGED_USER_EMAILS, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TAGGED_USER_NUMBERS, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TAGGED_IMAGE_URLS, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.IS_PRESENT, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TYPE, "")



                });
        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));


    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                               final int newVersion) {


        if (oldVersion <8 ) {

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
