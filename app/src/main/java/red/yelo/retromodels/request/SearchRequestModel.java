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

/**
 * Created by anshul1235 on 07/10/14.
 */
public class SearchRequestModel {

    @SerializedName("latitude")
    public String latitude;

    @SerializedName("longitude")
    public String longitude;

    @SerializedName("city")
    public String city;

    @SerializedName("country")
    public String country;

    @SerializedName("radius")
    public String radius;

    @SerializedName("type")
    public String type;

    @SerializedName("tag_ids")
    public List<String> tag_ids;

    @SerializedName("page")
    public String page;

    @SerializedName("per")
    public String per;

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public void setPer(String per) {
        this.per = per;
    }

    public void setTag_ids(List<String> tag_ids) {
        this.tag_ids = tag_ids;
    }
}
