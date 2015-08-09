
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
 * View representation for chats, messages and users
 */
public class ViewGroupColorsWithCards {

    private static final String TAG = "ViewGroupColorsWithCards";

    //Aliases for the tables
    private static final String ALIAS_GROUP = "A";
    private static final String ALIAS_CARDS = "B";


    public static final String NAME = "VIEW_GROUP_COLORS_WITH_CARDS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                .join(",", new String[]{
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, BaseColumns._ID),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, DatabaseColumns.ID),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, DatabaseColumns.USER_ID),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, DatabaseColumns.REFERRAL_COUNT),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, DatabaseColumns.GROUP_NAME),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, DatabaseColumns.SUBGROUP_NAME),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, DatabaseColumns.GROUP_ID),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, DatabaseColumns.SUBGROUP_ID),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, DatabaseColumns.URL),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, DatabaseColumns.URL_NAME),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, DatabaseColumns.SUB_HEADING),
                        String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_GROUP, DatabaseColumns.COLOR),


                });

        Logger.d(TAG, "View Column Def: %s", columnDef);


        final String fromDef = TextUtils
                .join(",", new String[]{
                        String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableTags.NAME, ALIAS_GROUP),
                        String.format(Locale.US, SQLConstants.TABLE_ALIAS, TableProfileCards.NAME, ALIAS_CARDS)



                });
        Logger.d(TAG, "From Def: %s", fromDef);


        final String whereDef = String
                .format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_CARDS, DatabaseColumns.GROUP_ID)
                + SQLConstants.EQUALS
                + String.format(Locale.US, SQLConstants.ALIAS_COLUMN, ALIAS_GROUP, DatabaseColumns.ID);


        Logger.d(TAG, "Where Def: %s", whereDef);

        final String selectDef = String
                .format(Locale.US, SQLConstants.SELECT_FROM_WHERE, columnDef, fromDef, whereDef);



        Logger.d(TAG, "Select Def: %s", selectDef);
        db.execSQL(String
                .format(Locale.US, SQLConstants.CREATE_VIEW, NAME, selectDef));

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                               final int newVersion) {

        //Add any data migration code here. Default is to drop and rebuild the table
        if (oldVersion <7) {

            db.execSQL(String
                    .format(Locale.US, SQLConstants.DROP_TABLE_IF_EXISTS, NAME));
            create(db);

        }
    }
}
