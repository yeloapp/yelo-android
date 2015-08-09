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
 */package red.yelo.retromodels.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anshul1235 on 16/07/14.
 */
public class UserDetailsWithoutImageRequestModel {


    @SerializedName("user")
    public User user = new User();


    public class User {

        @SerializedName("mobile_number")
        public String mobile_number;

        @SerializedName("name")
        public String name;

        @SerializedName("email")
        public String email;

        @SerializedName("description")
        public String description;

        @SerializedName("latitude")
        public String latitude;

        @SerializedName("longitude")
        public String longitude;

        @SerializedName("ext_image_url")
        public String ext_image_url;

        @SerializedName("state")
        public String state;

        @SerializedName("city")
        public String city;

        @SerializedName("country")
        public String country;

        @SerializedName("push_id")
        public String push_id;

        @SerializedName("platform")
        public String platform;

        @SerializedName("utc_offset")
        public String utc_offset;

        @SerializedName("platform_version")
        public String platform_version;




        public User() {
        }

        public void setExt_image_url(String ext_image_url) {
            this.ext_image_url = ext_image_url;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public void setPush_id(String push_id) {
            this.push_id = push_id;
        }

        public void setMobile_number(String mobile_number) {
            this.mobile_number = mobile_number;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public void setExt_img_url(String ext_img_url) {
            this.ext_image_url = ext_img_url;
        }

        public void setState(String state) {
            this.state = state;
        }

        public void setUtc_offset(String utc_offset) {
            this.utc_offset = utc_offset;
        }

        public void setPlatform_version(String platform_version) {
            this.platform_version = platform_version;
        }
    }

}

