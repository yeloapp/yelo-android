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

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by anshul1235 on 17/07/14.
 */
public class CreateUserResponseModel {


    @SerializedName("user")
    public User user;

    public Context context;

    public void setContext(Context context){
        this.context=context;
    }

    public static class User
    {
        @SerializedName("id")
        public String id;

        @SerializedName("mobile_number")
        public String mobile_number;

        @SerializedName("name")
        public String name;

        @SerializedName("email")
        public String email;

        @SerializedName("image_url")
        public String image_url;

        @SerializedName("description")
        public String description;

        @SerializedName("address")
        public String address;

        @SerializedName("city")
        public String city;

        @SerializedName("country")
        public String country;

        @SerializedName("listings")
        public List<CreateListingResponseModel.Listing> listings;
    }

}
