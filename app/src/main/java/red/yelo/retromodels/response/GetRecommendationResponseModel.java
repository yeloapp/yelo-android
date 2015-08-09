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
 * Created by anshul1235 on 14/12/14.
 */
public class GetRecommendationResponseModel {

    @SerializedName("recommends")
    public List<RecommendationList> recommends;

    @SerializedName("recommendations")
    public List<RecommendationReceiveList> recommendations;

    public class RecommendationList {

        @SerializedName("tagged_users")
        public List<TaggedUser> tagged_users;

        @SerializedName("wall_id")
        public String wall_id;


    }

    public class RecommendationReceiveList{


        @SerializedName("id")
        public String id;

        @SerializedName("tag_name")
        public String tag_name;

        @SerializedName("wall_id")
        public String wall_id;

        @SerializedName("comment")
        public String comment;

        @SerializedName("name")
        public String name;

        @SerializedName("user_id")
        public String user_id;

        @SerializedName("image_url")
        public String image_url;
    }

    public class TaggedUser {

        @SerializedName("comment")
        public String comment;

        @SerializedName("names")
        public List<String> names;
    }


}
