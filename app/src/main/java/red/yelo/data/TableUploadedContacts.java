

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

package red.yelo.data;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.Locale;

import red.yelo.utils.Logger;


/**
 * Table representing a list of chats
 */
public class TableUploadedContacts {

    private static final String TAG = "TableUploadedContacts";

    public static final String NAME = "UPLOADED_CONTACTS";

    public static void create(final SQLiteDatabase db) {

        final String columnDef = TextUtils
                .join(SQLConstants.COMMA, new String[]{
                        String.format(Locale.US, SQLConstants.DATA_INTEGER_PK, BaseColumns._ID),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.NUMBER, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.NAME, ""),
                        String.format(Locale.US, SQLConstants.DATA_TEXT, DatabaseColumns.EMAIL, "")

                });

        Logger.d(TAG, "Column Def: %s", columnDef);
        db.execSQL(String
                .format(Locale.US, SQLConstants.CREATE_TABLE, NAME, columnDef));

    }

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                               final int newVersion) {

        //Add any data migration code here. Default is to drop and rebuild the table

        if (oldVersion == 1) {
            create(db);
        } else if(oldVersion < 6) {
            //Dropping because in older version, inconsistent data might've been written
            db.execSQL(String.format(Locale.US, SQLConstants.DROP_TABLE_IF_EXISTS, NAME));
            create(db);
        }

    }
}
