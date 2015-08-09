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


/**
 * @author Sharath Pandeshwar
 * @since 22/03/15.
 * Model representing list of user's service cards
 */
public class ServiceCardsResponseModel {


    @SerializedName("service_cards")
    public List<ServiceCard> ServiceCards;


    public class ServiceCard {

        @SerializedName("id")
        private String id;

        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("price")
        private String price;

        @SerializedName("image_url")
        private String imageUrl;

        @SerializedName("group_name")
        private String groupName;

        @SerializedName("group_id")
        private String groupId;


        @SerializedName("owner")
        public ServiceCardOwner serviceCardOwner;


        public ServiceCard(String id, String title, String description, String price, String imageUrl, String groupName, String groupId) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.price = price;
            this.imageUrl = imageUrl;
            this.groupName = groupName;
            this.groupId = groupId;
        }


        public String getGroupName() {
            return groupName;
        }


        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }


        public String getGroupId() {
            return groupId;
        }


        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }


        public String getId() {
            return id;
        }


        public void setId(String id) {
            this.id = id;
        }


        public String getTitle() {
            return title;
        }


        public void setTitle(String title) {
            this.title = title;
        }


        public String getDescription() {
            return description;
        }


        public void setDescription(String description) {
            this.description = description;
        }


        public String getPrice() {
            return price;
        }


        public void setPrice(String price) {
            this.price = price;
        }


        public String getImageUrl() {
            return imageUrl;
        }


        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }


        public ServiceCardOwner getServiceCardOwner() {
            return serviceCardOwner;
        }


        public void setServiceCardOwner(ServiceCardOwner serviceCardOwner) {
            this.serviceCardOwner = serviceCardOwner;
        }


        @Override
        public String toString() {
            return "ServiceCardsResponseModel{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", price=" + price +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", serviceCardOwner=" + serviceCardOwner +
                    '}';
        }


        public class ServiceCardOwner {

            @SerializedName("id")
            public String id;

            @SerializedName("name")
            public String name;

            @SerializedName("image_url")
            public String image_url;

            @SerializedName("mobile_number")
            public String mobileNumber;

            @SerializedName("doc_verified")
            public int isDocVerified;


            @Override
            public String toString() {
                return "ServiceCardOwner{" +
                        "id='" + id + '\'' +
                        ", name='" + name + '\'' +
                        ", image_url='" + image_url + '\'' +
                        ", mobileNumber='" + mobileNumber + '\'' +
                        ", isDocVerified=" + isDocVerified +
                        '}';
            }
        }
    }


    @Override
    public String toString() {
        return "ServiceCardsResponseModel{" +
                "ServiceCards=" + ServiceCards +
                '}';
    }
}