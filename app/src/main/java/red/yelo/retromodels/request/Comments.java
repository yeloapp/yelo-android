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

package red.yelo.retromodels.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anshul1235 on 03/05/15.
 */
public class Comments{


    @SerializedName("comment")
    public Comment comment;


    public class Comment {
        @SerializedName("message")
        public String message;

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }
}
