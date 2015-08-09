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
public class GetCreateWallResponseModel {


    @SerializedName("wall")
    public Wall wall;

    public class Wall{

        @SerializedName("id")
        public String id;

        @SerializedName("tmp_id")
        public String tmp_id;

        @SerializedName("message")
        public String message;

        @SerializedName("comments_count")
        public String comments_count;

        @SerializedName("wall_image")
        public WallImage wall_image;

        @SerializedName("wall_owner")
        public WallOwner wall_owner;

        @SerializedName("wall_items")
        public List<GetWallItemResponseModel.WallItem> wall_items;

        @SerializedName("wall_chats")
        public List<WallChats> wall_chats;

        @SerializedName("tag_name")
        public String tag_name;

        @SerializedName("tag_id")
        public String tag_id;

        @SerializedName("chat_users_count")
        public String chat_users_count;

        @SerializedName("tagged_users_count")
        public String tagged_users_count;

        @SerializedName("created_at")
        public String created_at;

        @SerializedName("updated_at")
        public String updated_at;

        @SerializedName("status")
        public String status;

        @SerializedName("city")
        public String city;

        @SerializedName("address")
        public String address;

        @SerializedName("country")
        public String country;

        @SerializedName("state")
        public String state;

        @SerializedName("group_id")
        public String group_id;

        @SerializedName("group_name")
        public String group_name;

        @SerializedName("group_color")
        public String group_color;

        @SerializedName("comments")
        public List<Comments> comments;
    }

    public class WallOwner{

        @SerializedName("name")
        public String name;

        @SerializedName("image_url")
        public String image_url;

        @SerializedName("user_id")
        public String user_id;
    }

    public class WallChats{

        @SerializedName("name")
        public String name;

        @SerializedName("image_url")
        public String image_url;

        @SerializedName("id")
        public String user_id;

        @SerializedName("last_chat")
        public String last_chat;
    }

    public class WallImage{

        @SerializedName("id")
        public String id;

        @SerializedName("image_url")
        public String image_url;
    }


    public class Comments{

        @SerializedName("id")
        public String id;

        @SerializedName("message")
        public String message;

        @SerializedName("status")
        public String status;

        @SerializedName("spam_count")
        public String spamCount;

        @SerializedName("created_at")
        public String createdAt;

        @SerializedName("updated_at")
        public String updatedAt;


        @SerializedName("user_details")
        public User userDetails;

        public class User {
            @SerializedName("name")
            public String name;

            @SerializedName("image_url")
            public String image_url;

            @SerializedName("id")
            public String userId;
        }

    }



}
