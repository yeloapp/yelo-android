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
 */package red.yelo.retromodels.response;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by anshul1235 on 29/07/14.
 */
public class GoogleGeocodeResponse {


    @SerializedName("results")
    public List<Results> results;

    public class Results {

        @SerializedName("formatted_address")
        public String formatted_address;

        /**
         * Index
         * 0=locality
         * 1=city
         * 2=state
         * 3=country
         */

        public String[] getAddress() {
            String[] address = TextUtils.split(this.formatted_address, ",");
            String[] addressSplit = new String[4];
            int length = address.length;
            //gives country
            addressSplit[3] = address[length - 1];
            //gives state
            if (length - 2 >= 0)
                addressSplit[2] = address[length - 2];
            else {
                addressSplit[2] = address[length - 1];
            }
            //gives city
            if (length - 3 >= 0)
                addressSplit[1] = address[length - 3];
            else if (length - 2 >= 0) {
                addressSplit[1] = address[length - 2];
            } else {
                addressSplit[1] = address[length - 1];
            }
            //gives locality
            if (length - 4 >= 0)
                addressSplit[0] = address[length - 4];
            else if (length - 3 >= 0) {
                addressSplit[0] = address[length - 3];
            } else if (length - 2 >= 0) {
                addressSplit[0] = address[length - 2];
            } else {
                addressSplit[0] = address[length - 1];
            }

            return addressSplit;
        }

    }

}
