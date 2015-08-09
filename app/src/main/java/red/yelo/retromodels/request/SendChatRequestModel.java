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
 * Created by anshul1235 on 22/07/14.
 */
public class SendChatRequestModel {

    @SerializedName("sender_id")
    public String sender_id;

    @SerializedName("receiver_id")
    public String receiver_id;

    @SerializedName("message")
    public String message;

    @SerializedName("reply_id")
    public String reply_id;

    @SerializedName("sent_at")
    public String sent_at;

    @SerializedName("wall_id")
    public String wall_id;

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReply_id(String reply_id) {
        this.reply_id = reply_id;
    }

    public void setSent_at(String sent_at) {
        this.sent_at = sent_at;
    }

    public void setWall_id(String wall_id) {
        this.wall_id = wall_id;
    }
}

