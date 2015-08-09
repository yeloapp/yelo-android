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
 */package red.yelo.retromodels;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import red.yelo.retromodels.response.CreateListingResponseModel;

/**
 * Created by anshul1235 on 09/09/14.
 */
public class GetUserModel {


    @SerializedName("user")
    public Users user;

    public class Users {

        Users() {

        }

        @SerializedName("id")
        public String id;

        @SerializedName("share_token")
        public String share_token;

        @SerializedName("rating_avg")
        public String rating_avg;

        @SerializedName("name")
        public String name;

        @SerializedName("description")
        public String description;

        @SerializedName("image_url")
        public String image_url;

        @SerializedName("total_tagged")
        public String total_tagged;

        @SerializedName("connects_count")
        public String connects_count;

        @SerializedName("total_ratings")
        public String total_ratings;

        @SerializedName("listings")
        public List<CreateListingResponseModel.Listing> listings;

        @SerializedName("ratings")
        public List<Ratings> ratings;


        public class Tag{

            @SerializedName("id")
            public String id;

            @SerializedName("name")
            public String name;
        }

        public class Ratings {
            public String id;
            public String comment;
            public String stars;
            public RatingOwner rating_owner;
            public List<RatingTags> rating_tags;

            public class RatingOwner {
                public String user_id;
                public String name;
                public String image_url;
            }

            public class RatingTags {
                public String tag_name;
                public String tag_id;
            }

        }
    }
}
