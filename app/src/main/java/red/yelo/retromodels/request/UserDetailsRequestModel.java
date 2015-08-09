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

import java.util.List;

import red.yelo.retromodels.response.GetListingResponseModel;

/**
 * Created by anshul1235 on 16/07/14.
 */
public  class UserDetailsRequestModel {



    @SerializedName("user")
    public  User user=new User();



    public class User {

        @SerializedName("mobile_number")
        public String mobile_number;

        @SerializedName("name")
        public String name;

        @SerializedName("phone_id")
        public String phone_id;

        @SerializedName("push_token")
        public String push_token;

        @SerializedName("platform")
        public String platform;

        @SerializedName("image_url")
        public String image_url;

        @SerializedName("description")
        public String description;

        @SerializedName("country_code")
        public String country_code;

        @SerializedName("listings")
        public List<GetListingResponseModel.Listing> listings;
        public User() {}

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMobile_number() {
            return mobile_number;
        }

        public void setMobile_number(String mobile_number) {
            this.mobile_number = mobile_number;
        }

        public String getPhone_id() {
            return phone_id;
        }

        public void setPhone_id(String phone_id) {
            this.phone_id = phone_id;
        }

        public void setCountry_code(String country_code) {
            this.country_code = country_code;
        }

        public void setPush_token(String push_token) {
            this.push_token = push_token;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }
    }

}

