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
 */package red.yelo.http.api;

import java.util.Map;

import red.yelo.retromodels.response.GoogleUrl;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.QueryMap;

/**
 * Class for Google Location APIs
 * Created by vinaysshenoy on 30/11/14.
 */
public interface GoogleUrlShortenerApi {

    @POST("/url")
    void shortenUrl(@QueryMap Map<String, String> params, @Body GoogleUrl body, Callback<GoogleUrl> cb);

}
