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

import red.yelo.retromodels.GetUserModel;
import red.yelo.retromodels.request.SendChatRequestModel;
import red.yelo.retromodels.request.UploadContactRequestModel;
import red.yelo.retromodels.response.UploadContactsModel;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Class that represents the Yelo Chat Api
 * Created by vinaysshenoy on 23/10/14.
 */
public interface ChatApi {

    @GET("/")
    GetUserModel getUserDetail(@Path("user_id") String user_id);

    @POST("/")
    void sendChat(@Body SendChatRequestModel chat, Callback<Object> cb);

    @POST("/")
    UploadContactsModel uploadContacts(@Body UploadContactRequestModel contacts);
}
