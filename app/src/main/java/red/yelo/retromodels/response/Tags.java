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
 * Created by anshul1235 on 09/08/14.
 */
public class Tags {

    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private String id;

    @SerializedName("image_url")
    private String image_url;

    boolean checked=false;


    public Tags(String name,String id,String image_url) {
        this.name = name;
        this.id=id;
        this.image_url=image_url;
    }

    public Tags(String name) {
        this.name = name;
    }

    public Tags( String name, boolean checked ) {
        this.name = name ;
        this.checked = checked ;
    }

    public String getName() { return name; }

    public String getId() { return id; }

    public String getImage() { return image_url; }

    public boolean isChecked() {
        return checked;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void toggleChecked() {
        checked = !checked ;
    }



    @Override
    public String toString() { return name; }
}
