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
 */package red.yelo.retromodels.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import red.yelo.retromodels.Link;

/**
 * Created by anshul1235 on 09/08/14.
 */
public class CreateListingRequestModel {


    @SerializedName("listing")
    public Listing listing=new Listing();

    public class Listing {

        @SerializedName("latitude")
        public String latitude;

        @SerializedName("longitude")
        public String longitude;

        @SerializedName("city")
        public String city;

        @SerializedName("state")
        public String state;

        @SerializedName("country")
        public String country;

        @SerializedName("address")
        public String address;

        @SerializedName("description")
        public String description;

        @SerializedName("tag_id")
        public String tag_id;


        public Listing(){

        }



        public void setLatitude(String latitude){
            this.latitude=latitude;
        }

        public void setLongitude(String longitude){
            this.longitude=longitude;
        }

        public void setCity(String city){
            this.city=city;
        }

        public void setState(String state){
            this.state=state;
        }

        public void setCountry(String country){
            this.country=country;
        }

        public void setTag_id(String tag_id) {
            this.tag_id = tag_id;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setDescription(String description) {
            this.description = description;
        }


    }

    @SerializedName("keywords")
    public List<String> keywords;

    @SerializedName("links")
    public List<Link> links;

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

}


