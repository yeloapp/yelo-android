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
 * Created by anshul1235 on 20/03/15.
 */
public class GetCreateServiceCardResponse {

    @SerializedName("service_card")
    public ServiceCard serviceCard;


    public class ServiceCard{

        @SerializedName("id")
        public String id;

        @SerializedName("title")
        public String title;

        @SerializedName("description")
        public String description;

        @SerializedName("price")
        public String price;

        @SerializedName("currency")
        public String currency;

        @SerializedName("image_url")
        public String imageUrl;

        @SerializedName("group_name")
        public String groupName;

        @SerializedName("group_id")
        public String groupId;

        @SerializedName("tag_name")
        public String subgroupName;

        @SerializedName("tag_id")
        public String subgroupId;

        @SerializedName("group_color")
        public String color;

        @SerializedName("duration_time")
        public String durationTime;

        @SerializedName("avg_rating")
        public String avgRating;

        @SerializedName("ratings")
        public List<Rating> ratings;

        @SerializedName("note")
        public String note;

        @SerializedName("owner")
        public Owner owner;

        @SerializedName("views")
        public String views;

        @SerializedName("books")
        public String books;
    }

    public class Rating {
        @SerializedName("comment")
        public String comment;
    }

    public class Owner{

        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("image_url")
        public String imageUrl;

        @SerializedName("mobile_number")
        public String mobileNumber;

        @SerializedName("doc_verified")
        public String docVerified;

    }
}
