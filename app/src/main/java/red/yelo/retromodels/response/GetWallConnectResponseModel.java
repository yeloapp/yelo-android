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
 * Created by anshul1235 on 12/08/14.
 */
public class GetWallConnectResponseModel {

    @SerializedName("tag_users")
    public List<User> tag_users;

    @SerializedName("chat_users")
    public List<User> chat_users;

    public class User{

        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("mobile_number")
        public String mobile_number;

        @SerializedName("user_id")
        public String user_id;

        @SerializedName("image_url")
        public String image_url;
    }

}
