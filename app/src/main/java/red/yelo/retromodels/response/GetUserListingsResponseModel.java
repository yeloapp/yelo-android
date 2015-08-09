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

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by anshul1235 on 13/10/14.
 */
public class GetUserListingsResponseModel {

    @SerializedName("user")
    public User user;

    @SerializedName("listings")
    public List<Listing> listings;

    public class User{

        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("description")
        public String description;

        @SerializedName("image_url")
        public String image_url;
    }

    public class Listing{

        @SerializedName("id")
        public String id;

        @SerializedName("latitude")
        public String latitude;

        @SerializedName("longitude")
        public String longitude;

        @SerializedName("city")
        public String city;

        @SerializedName("country")
        public String country;

        @SerializedName("listing_tags")
        public List<ListingTags> listing_tags;

        public class ListingTags{

            @SerializedName("id")
            public String id;

            @SerializedName("tag_id")
            public String tag_id;

            @SerializedName("tag_name")
            public String tag_name;
        }
    }
}
