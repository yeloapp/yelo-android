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
 * Created by anshul1235 on 13/09/14.
 */
public class GetWallItemResponseModel {

    @SerializedName("wall_item")
    public WallItem wall_item;

    @SerializedName("error_message")
    public Error error_message;

    public class Error{

        @SerializedName("mobile_number")
        public List<String> mobile_number;
    }
    public class WallItem{

        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("image_url")
        public String image_url;

        @SerializedName("user_id")
        public String user_id;

        @SerializedName("comment")
        public String comment;

        @SerializedName("tmp_id")
        public String tmp_id;

        @SerializedName("created_at")
        public String created_at;

        @SerializedName("tagged_users")
        public List<TaggedUsers> tagged_users;

        public class TaggedUsers{

            @SerializedName("id")
            public String id;

            @SerializedName("user_id")
            public String user_id;

            @SerializedName("name")
            public String name;

            @SerializedName("details")
            public Details details;

            @SerializedName("image_url")
            public String image_url;

            @SerializedName("is_present")
            public String is_present;

            public class Details{

                @SerializedName("mobile_number")
                public String mobile_number;

                @SerializedName("email")
                public String email;

            }

        }
    }
}
