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
 */

package red.yelo.http.api;

import java.util.Locale;
import java.util.Map;

import red.yelo.retromodels.response.GetClickedPlaceModel;
import red.yelo.retromodels.response.GetPlacesModel;
import red.yelo.retromodels.response.GoogleGeocodeResponse;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

/**
 * Class for Google Location APIs
 * Created by vinaysshenoy on 23/10/14.
 */
public interface GoogleApi {

    //String address = String.format(Locale.ENGLISH, "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=false&language=" + Locale.getDefault().getCountry(), lat, lng);

    //get location from the google geocode api
    @GET("/geocode/json")
    void getMyAddress(@QueryMap Map<String, String> params, Callback<GoogleGeocodeResponse> cb);

    //get location from the google geocode api
    @GET("/place/autocomplete/json")
    void getAddressList(@QueryMap Map<String, String> params,Callback<GetPlacesModel> cb);

    //get location from the google geocode api
    @GET("/place/autocomplete/json")
    GetPlacesModel getAddressListModel(@QueryMap Map<String, String> params);

    //get  full location from the google geocode api
    @GET("/place/details/json")
    void getFullAddress(@QueryMap Map<String, String> params,Callback<GetClickedPlaceModel> cb);

}
