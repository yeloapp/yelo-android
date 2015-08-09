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

/**
 * Created by anshul1235 on 27/01/15.
 */

public class Contacts {

    @SerializedName("hash_mobile_number")
    public String hash_mobile_number;

    @SerializedName("name")
    public String name;


    public void setHash_mobile_numbers(String hash_mobile_numbers) {
        this.hash_mobile_number = hash_mobile_numbers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHash_mobile_numbers() {
        return hash_mobile_number;
    }

    public String getName() {
        return name;
    }
}
