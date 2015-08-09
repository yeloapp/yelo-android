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

/**
 * Created by anshul1235 on 17/07/14.
 */
public class VerifySmsResponseModel {

    @SerializedName("is_present")
    public boolean is_present;

    @SerializedName("encrypted_device_id")
    public String encrypted_device_id;

    @SerializedName("auth_token")
    public String auth_token;

    @SerializedName("id")
    public String id;

    @SerializedName("share_token")
    public String share_token;

}
