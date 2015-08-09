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

import retrofit.mime.TypedFile;

/**
 * Created by anshul1235 on 16/07/14.
 */
public  class UserDetailsWithImageRequestModel {



    @SerializedName("user")
    public  User user=new User();



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

        @SerializedName("image")
        public TypedFile image;

        @SerializedName("state")
        public String state;

        @SerializedName("city")
        public String city;

        @SerializedName("country")
        public String country;

        public User() {}

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

        public void setImage(TypedFile image) {
            this.image = image;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setState(String state) {
            this.state = state;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }
    }

}

