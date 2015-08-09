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
 * Created by anshul1235 on 05/10/14.
 */
public class SelectInterestRequestModel {

    @SerializedName("user")
    public User user = new User();
    public class User{
        @SerializedName("interest_ids")
        public List<String> interest_ids;

        public void setInterest_ids(List<String> interest_ids) {
            this.interest_ids = interest_ids;
        }
    }
}
