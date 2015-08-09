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
 * Created by anshul1235 on 09/08/14.
 */
public class CreateListingResponseModel {

    @SerializedName("listing")
    public Listing listing;

    public class Listing {

        @SerializedName("id")
        public String id;

        @SerializedName("latitude")
        public String latitude;

        @SerializedName("longitude")
        public String longitude;

        @SerializedName("country")
        public String country;

        @SerializedName("state")
        public String state;

        @SerializedName("city")
        public String city;

        @SerializedName("address")
        public String address;

        @SerializedName("description")
        public String description;

        @SerializedName("tag_name")
        public String tag_name;

        @SerializedName("tag_id")
        public String tag_id;

        @SerializedName("group_name")
        public String group_name;

        @SerializedName("group_id")
        public String group_id;

        @SerializedName("group_color")
        public String group_color;

        @SerializedName("referral_count")
        public String referral_count;

        @SerializedName("listing_keywords")
        public List<ListingKeywords> listing_keywords;

        @SerializedName("listing_links")
        public List<ListingLinks> listing_links;


        public class ListingKeywords {

            @SerializedName("id")
            public String id;

            @SerializedName("name")
            public String name;

            @SerializedName("word_id")
            public String word_id;

            @SerializedName("keyword_id")
            public String keyword_id;

        }

        public class ListingLinks {

            @SerializedName("id")
            public String id;

            @SerializedName("name")
            public String name;

            @SerializedName("url")
            public String url;


        }

    }
}
