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
 * Created by anshul1235 on 20/07/14.
 */

public class CreateServiceResponseModel {

    @SerializedName("service")
    public Service service=new Service();

    public class Service{

        @SerializedName("id")
        public String id;

        @SerializedName("business_name")
        public String business_name;

        @SerializedName("mobile_number")
        public String mobile_number;

        @SerializedName("listing_categories")
        public List<String> listing_categories;

        @SerializedName("latitude")
        public String latitude;

        @SerializedName("longitude")
        public String longitude;

        @SerializedName("country")
        public String country;

        @SerializedName("city")
        public String city;

        @SerializedName("state")
        public String state;

        @SerializedName("zip_code")
        public String zip_code;

        @SerializedName("description")
        public String description;

        @SerializedName("address")
        public String address;

        public class ServiceImage{
            @SerializedName("id")
            String id;
            String image;
            String is_main;
        }



    }
}
