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

import red.yelo.retromodels.Contacts;

/**
 * Created by vinaysshenoy on 09/11/14.
 */
public class UploadContactRequestModel {

    //public  List<String> hash_mobile_numbers;

    @SerializedName("contacts")
    public  List<Contacts> contacts;

    public void setContacts(List<Contacts> contacts) {
        this.contacts = contacts;
    }

    public UploadContactRequestModel() {
      //  this(50);
    }

//    public UploadContactRequestModel(int capacity) {
//        hash_mobile_numbers = new ArrayList<String>(capacity);
//    }
//
//    public List<String> getHash_mobile_numbers() {
//        return hash_mobile_numbers;
//    }
//
//    public void addNumber(String... numbers) {
//
//        if(numbers != null && numbers.length > 0) {
//            for (String number : numbers) {
//                if(!hash_mobile_numbers.contains(number)) {
//                    hash_mobile_numbers.add(number);
//                }
//            }
//        }
//    }

}
