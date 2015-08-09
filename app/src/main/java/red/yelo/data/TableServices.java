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

import red.yelo.utils.Logger;

/**
 * Created by anshul1235 on 19/03/15.
 */
public class TableServices {


    private static final String TAG  = "TableServices";

    public static final String  NAME = "SERVICES";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                .join(SQLConstants.COMMA, new String[]{
                        String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                        String.format(Locale.US, SQLConstants.DATA_TEXT_UK, DatabaseColumns.ID, ""),

                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.USER_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.USER_NAME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.USER_IMAGE, ""),

                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.RATING, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.RATING_COUNT, ""),

                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.GROUP_NAME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SUBGROUP_NAME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.GROUP_ID, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SUBGROUP_ID, ""),

                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.URL, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.DURATION, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.DELIVERABLE, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.URL_NAME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TITLE, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.COLOR, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SERVICE_IMAGE, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.LATITUDE, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.LONGITUDE, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SERVICE_DESCRIPTION, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.SERVICE_PRICE, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.CURRENCY, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.VERIFIED, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.USER_NUMBER, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.VIEW_COUNT, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.BOOK_COUNT, ""),



                });

        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                               final int newVersion) {

        //Add any data migration code here. Default is to drop and rebuild the table

        //Add any data migration code here. Default is to drop and rebuild the table
        if (oldVersion < 14) {

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
