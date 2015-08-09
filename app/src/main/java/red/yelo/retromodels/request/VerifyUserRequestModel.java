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

package red.yelo.retromodels.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anshul1235 on 18/07/14.
 */
public class VerifyUserRequestModel {


    @SerializedName("user")
    public  User user=new User();



    public class User {

        @SerializedName("serial_code")
        public String serial_code;

        @SerializedName("missed_call_number")
        public String missed_call_number;

        @SerializedName("push_id")
        public String push_id;

        @SerializedName("platform")
        public String platform;

        @SerializedName("encrypt_device_id")
        public String encrypt_device_id;

        @SerializedName("mobile_number")
        public String mobile_number;


        public User() {}

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public void setEncrypt_device_id(String encrypt_device_id) {
            this.encrypt_device_id = encrypt_device_id;
        }

        public void setMissed_call_number(String missed_call_number) {
            this.missed_call_number = missed_call_number;
        }

        public void setPush_id(String push_id) {
            this.push_id = push_id;
        }

        public void setSerial_code(String serial_code) {
            this.serial_code = serial_code;
        }

        public String getSerial_code() {
            return serial_code;
        }

        public void setMobile_number(String mobile_number) {
            this.mobile_number = mobile_number;
        }
    }
}
