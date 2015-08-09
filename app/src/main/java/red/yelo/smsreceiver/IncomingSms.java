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
 */package red.yelo.smsreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import red.yelo.YeloApplication;
import red.yelo.bus.SmsVerification;
import red.yelo.utils.AppConstants;
import red.yelo.utils.Logger;

/**
 * Created by anshul1235 on 18/07/14.
 */
public class IncomingSms extends BroadcastReceiver {

    /**
     * SmsManager to get the sms in the receiver
     */
    private String mMessage;


    private static final String TAG = "IncomingSms";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String senderNum = phoneNumber;
                    mMessage = currentMessage.getDisplayMessageBody();
                    mMessage = mMessage.replace(AppConstants.SMS_VERIFY_FORMAT,"");
                    Logger.d(TAG, "SmsReceiver : senderNum: " + senderNum + "; message: " + mMessage);

                    ((YeloApplication) context.getApplicationContext()).getBus().post(new SmsVerification(mMessage));
                    Logger.d(TAG, "SmsReceiver : senderNum: " + senderNum + "; message: " + mMessage);



                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Logger.e(TAG, "Exception smsReceiver" + e);

        }
    }
}
