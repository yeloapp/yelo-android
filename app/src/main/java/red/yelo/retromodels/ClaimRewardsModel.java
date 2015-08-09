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

import java.util.List;


/**
 * @author Sharath Pandeshwar
 * @since 07/03/15.
 * Model representing result of claiming rewards
 */
public class ClaimRewardsModel {

    public static final String SUCCESS = "success";
    public static final String PROCESSED = "processed";
    public static final String PROCESSING = "processing";


    @SerializedName("status")
    private String status;

    @SerializedName("balance")
    private Long balancePoints;


    @SerializedName("claims")
    public List<Claim> claims;

    public ClaimRewardsModel(boolean result, Long points) {
        this.status = status;
        this.balancePoints = points;
    }


    public Long getBalancePoints() {
        return balancePoints;
    }


    public void setBalancePoints(Long balancePoints) {
        this.balancePoints = balancePoints;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "ClaimRewardsModel{" +
                "status=" + status +
                ", balancePoints=" + balancePoints +
                '}';
    }

    public class Claim{

        @SerializedName("id")
        public String id;

        @SerializedName("amount")
        public String amount;

        @SerializedName("status")
        public String status;

        @SerializedName("created_at")
        public String createdAt;

        @SerializedName("updated_at")
        public String updatedAt;
    }
}