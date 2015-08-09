

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
 * @author Anshul Kamboj Table representing all the notifications
 */
public class TableNotifications {

    private static final String TAG  = "TableNotifications";

    public static final String  NAME = "NOTIFICATIONS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                        .join(SQLConstants.COMMA, new String[] {
                                String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.WALL_ID, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.KEY, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.MESSAGE, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.NAME, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.TAGS, ""),
                                String.format(Locale.US, SQLConstants.DATA_TEXT,
                                        DatabaseColumns.NOTIFICATION_STATUS,
                                        AppConstants.NotificationStatus.UNREAD_NOT_OPENED)

                        });

        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                        .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                    final int newVersion) {

      //Add any data migration code here. Default is to drop and rebuild the table
        if (oldVersion < 7) {

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
