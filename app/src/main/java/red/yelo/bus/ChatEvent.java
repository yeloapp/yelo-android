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
 */package red.yelo.bus;

/**
 * Created by anshul1235 on 14/10/14.
 */
public class ChatEvent {
    public boolean chatClicked;
    public String userId;
    public String userName;
    public String userImage;


    public ChatEvent(boolean chatClicked, String userId, String userName, String userImage) {
        this.chatClicked = chatClicked;
        this.userId = userId;
        this.userName = userName;
        this.userImage = userImage;
    }
}
