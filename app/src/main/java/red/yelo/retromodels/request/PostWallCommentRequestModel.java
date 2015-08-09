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

import android.os.Bundle;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import red.yelo.retromodels.TaggedUser;

/**
 * Created by anshul1235 on 13/09/14.
 */
public class PostWallCommentRequestModel {

    @SerializedName("wall_item")
    public WallItem wall_item = new WallItem();

    @SerializedName("tag_users")
    public List<TaggedUser> tag_users;

    @SerializedName("args")
    public Bundle args;

    public class WallItem {

        @SerializedName("comment")
        public String comment;

        @SerializedName("tmp_id")
        public String tmp_id;

        public void setTmp_id(String tmp_id) {
            this.tmp_id = tmp_id;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

    }

    public void setTag_users(List<TaggedUser> tag_users) {
        this.tag_users = tag_users;
    }

    public void setArgs(Bundle args) {
        this.args = args;
    }
}
