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
 */package red.yelo.analytics;

/**
 * Direction of the connection
 * <p/>
 * Created by vinaysshenoy on 05/10/14.
 */
public enum ConnectionDirection {

    DIRECT_FROM_OP("Directly from Post Author"),
    DIRECT_TO_OP("Directly to Post Author"),
    INDIRECT_VIA_POST("Indirectly via Post");

    public final String code;

    private ConnectionDirection(final String code) {
        this.code = code;
    }

}
