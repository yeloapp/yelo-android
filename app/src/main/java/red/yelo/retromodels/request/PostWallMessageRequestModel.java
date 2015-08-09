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

import java.util.List;

/**
 * Created by anshul1235 on 22/07/14.
 */
public class PostWallMessageRequestModel {

    @SerializedName("wall")
    public Wall wall=new Wall();

    public class Wall{

        @SerializedName("message")
        public String message;

        @SerializedName("latitude")
        public String latitude;

        @SerializedName("longitude")
        public String longitude;

        @SerializedName("city")
        public String city;

        @SerializedName("country")
        public String country;

        @SerializedName("state")
        public String state;

        @SerializedName("address")
        public String address;

        @SerializedName("tag_id")
        public String tag_id;

        @SerializedName("tmp_id")
        public String tmp_id;

        @SerializedName("group_id")
        public String group_id;

        @SerializedName("keywords")
        public List<String> keywords;

        public void setAddress(String address) {
            this.address = address;
        }

        public void setState(String state) {
            this.state = state;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setTag_id(String tag_id) {
            this.tag_id = tag_id;
        }

        public void setTmp_id(String tmp_id) {
            this.tmp_id = tmp_id;
        }

        public void setKeywords(List<String> keywords) {
            this.keywords = keywords;
        }

        public void setGroup_id(String group_id) {
            this.group_id = group_id;
        }
    }
}
