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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by imran on 08/11/14.
 */
public class ShareUtils {

    /*WhatsApp - com.whatsapp
    Facebook Messenger - com.facebook.orca
    Facebook - com.facebook.katana
    Hangouts - com.google.android.talk
    Twitter - com.twitter.android
    SMS - com.android.mms
    Google+ - com.google.android.apps.plus
    Gmail - com.google.android.gm*/

    static Map<String, String> mShareMaps;
    static List<Intent> sInitialShareIntents;
    static List<Intent> sMoreShareIntents;

    public ShareUtils() {

        mShareMaps = new HashMap<String, String>();
        mShareMaps.put("WhatsApp", "com.whatsapp");
        mShareMaps.put("Hangouts", "com.google.android.talk");
        mShareMaps.put("Facebook Messenger", "com.facebook.orca");
        mShareMaps.put("Message", "com.android.mms");
        mShareMaps.put("Twitter", "com.twitter.android");
        mShareMaps.put("Facebook", "com.facebook.katana");
        mShareMaps.put("Google+", "com.google.android.apps.plus");
        mShareMaps.put("Gmail", "com.google.android.gm");

        sInitialShareIntents = new ArrayList<Intent>();
        sMoreShareIntents = new ArrayList<Intent>();

    }

    public static void getShareIntents(Context context) {

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share with");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hello Yelo!");
        shareIntent.setType("text/plain");

        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(shareIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(Intent.ACTION_SEND);
            if (mShareMaps.containsValue(res.activityInfo.packageName) && sInitialShareIntents.size() < 4) {
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share with");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, "Hello Yelo!");
                intent.setType("text/plain");
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                sInitialShareIntents.add(intent);
            } else {
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share with");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, "Hello Yelo!");
                intent.setType("text/plain");
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                sMoreShareIntents.add(intent);
            }
        }

        final Intent intent = Intent.createChooser(sInitialShareIntents.remove(0), "Share with!");
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, sInitialShareIntents.toArray(new Parcelable[]{}));
        context.startActivity(intent);
    }

    public static void getMoreShareIntents(Context context) {
        final Intent intent = Intent.createChooser(sMoreShareIntents.remove(0), "Share with!");
        intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, sMoreShareIntents.toArray(new Parcelable[]{}));
        context.startActivity(intent);
    }
}
