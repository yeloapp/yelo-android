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

package red.yelo.http.api;

import java.util.List;
import java.util.Map;

import red.yelo.retromodels.ClaimRewardsModel;
import red.yelo.retromodels.GetUserModel;
import red.yelo.retromodels.ReferralScoreModel;
import red.yelo.retromodels.ServiceCardsResponseModel;
import red.yelo.retromodels.request.CloseWallRequestModel;
import red.yelo.retromodels.request.Comments;
import red.yelo.retromodels.request.CreateListingRequestModel;
import red.yelo.retromodels.request.PostWallCommentRequestModel;
import red.yelo.retromodels.request.PostWallMessageRequestModel;
import red.yelo.retromodels.request.RatingRequestModel;
import red.yelo.retromodels.request.ReportAbuseRequestModel;
import red.yelo.retromodels.request.SelectInterestRequestModel;
import red.yelo.retromodels.request.SendInviteRequestModel;
import red.yelo.retromodels.request.UploadContactRequestModel;
import red.yelo.retromodels.request.UserDetailsRequestModel;
import red.yelo.retromodels.request.UserDetailsWithoutImageRequestModel;
import red.yelo.retromodels.request.VerifyUserRequestModel;
import red.yelo.retromodels.response.CommentResponseModel;
import red.yelo.retromodels.response.CreateListingResponseModel;
import red.yelo.retromodels.response.CreateUserResponseModel;
import red.yelo.retromodels.response.GetCollectionResponseModel;
import red.yelo.retromodels.response.GetCreateServiceCardResponse;
import red.yelo.retromodels.response.GetCreateWallResponseModel;
import red.yelo.retromodels.response.GetNamesModel;
import red.yelo.retromodels.response.GetRecommendationResponseModel;
import red.yelo.retromodels.response.GetRefferalsResponseModel;
import red.yelo.retromodels.response.GetServiceCardResponseModel;
import red.yelo.retromodels.response.GetSubcategoryResponseModel;
import red.yelo.retromodels.response.GetUserListingsResponseModel;
import red.yelo.retromodels.response.GetWallConnectResponseModel;
import red.yelo.retromodels.response.GetWallItemResponseModel;
import red.yelo.retromodels.response.GetWallResponseModel;
import red.yelo.retromodels.response.GoogleProfileResponse;
import red.yelo.retromodels.response.KeywordSuggestionsResponseModel;
import red.yelo.retromodels.response.SelectInterestResponseModel;
import red.yelo.retromodels.response.SerialKeyResponseModel;
import red.yelo.retromodels.response.ServerStatus;
import red.yelo.retromodels.response.TagsRecommendationResponseModel;
import red.yelo.retromodels.response.TagsResponseModel;
import red.yelo.retromodels.response.TagsSuggestionsResponseModel;
import red.yelo.retromodels.response.UploadContactsModel;
import red.yelo.retromodels.response.VerifySmsResponseModel;
import red.yelo.retromodels.response.WeeklyLeaders;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import retrofit.mime.TypedFile;

/**
 * Api reference for Yelo REST api
 * Created by anshul1235 on 17/07/14.
 */
public interface YeloApi {


    // All the GET apis

    @GET("/")
    void getSubCats(@Path("category_id") String id, Callback<TagsResponseModel> cb);


    @GET("/")
    GetNamesModel getTagNames(@QueryMap Map<String, String> params);

    @GET("/")
    void getUsers(@QueryMap Map<String, String> params, Callback<GetUserModel> cb);

    @GET("/")
    GetNamesModel getTagNamesForSearch(@QueryMap Map<String, String> params);

    @GET("/")
    void getWallMessages(@QueryMap Map<String, String> params, Callback<GetWallResponseModel> cb);

    @GET("/")
    void getServiceCards(@QueryMap Map<String, String> params, Callback<GetServiceCardResponseModel> cb);

    @GET("/")
    void getWallMessagesWithModel(@Query("tag_ids[]") List<String> tag_ids, @QueryMap Map<String, String> params,Callback<GetWallResponseModel> cb);

    @GET("/")
    void getWallPost(@Path("wall_id") String wall_id, Callback<GetCreateWallResponseModel> wallCallback);

    @GET("/")
    void getWallPostComments(@Path("wall_id") String wall_id, Callback<GetCreateWallResponseModel> wallCallback);

    //new
    @GET("/")
    void getTagSuggestions(@QueryMap Map<String, String> params, Callback<TagsSuggestionsResponseModel> cb);

    //new
    @GET("/")
    void getSuggestions(@QueryMap Map<String, String> params, Callback<KeywordSuggestionsResponseModel> cb);


    //new
    @GET("/")
    void getTagRecommendations(Callback<TagsRecommendationResponseModel> cb);

    //new
    @GET("/")
    void getUserDetailAsync(@Path("user_id") String user_id, Callback<GetUserModel> cb);

    //new
    @GET("/")
    void getUserListings(@Path("id") String user_id, Callback<GetUserListingsResponseModel> cb);

    //new
    @GET("/")
    void getUserWalls(@Path("user_id") String userId, Callback<GetWallResponseModel> cb);

    //new
    @GET("/")
    void getWallConnects(@Path("wall_id") String wallId, Callback<GetWallConnectResponseModel> cb);

    //new
    @GET("/")
    void getServerStatus(Callback<ServerStatus> cb);

    //new
    @GET("/")
    void getRecommendationsReceived(@Path("user_id") String userId, @QueryMap Map<String, String> params, Callback<GetRefferalsResponseModel> cb);


    //new
    @GET("/")
    void getRecommendationsRec(@Path("user_id") String userId, @QueryMap Map<String, String> params, Callback<GetRecommendationResponseModel> cb);

    //new
    @GET("/")
    void getRecommendationsMad(@Path("user_id") String userId, @QueryMap Map<String, String> params, Callback<GetRecommendationResponseModel> cb);

    //new
    @GET("/")
    void getRecommendationsMade(@Path("user_id") String userId, @QueryMap Map<String, String> params, Callback<GetRefferalsResponseModel> cb);

    //new
    @GET("/")
    void getGocs(Callback<GetCollectionResponseModel> cb);

    @GET("/")
    void getGocsForDiscover(@QueryMap Map<String,String> params,Callback<GetCollectionResponseModel> cb);

    @GET("/")
    void getSubCategories(@Path("group_id") String groupId,Callback<GetSubcategoryResponseModel> cb);

    @GET("/")
    void pingServer(Callback<String> cb);

    @GET("/")
    void getReferralScore(Callback<ReferralScoreModel> cb);

    @GET("/")
    void getWeeklyLeaders(Callback<WeeklyLeaders> cb);

    @GET("/")
    void claimReward(Callback<ClaimRewardsModel> cb);

    @GET("/")
    void getMyServiceCards(Callback<ServiceCardsResponseModel> cb);

    @GET("/")
    void getUsersServiceCards(@Path("user_id") String userId, Callback<GetServiceCardResponseModel> cb);

    // All the PUT apis
    @Multipart
    @PUT("/")
    void updateUserMultipart(@Path("id") String id,
                             @Part("image") TypedFile image,
                             @QueryMap Map<String, String> params,
                             Callback<CreateUserResponseModel> cb);

    @PUT("/")
    void updateCard(@Path("id") String id, @Body CreateListingRequestModel createListingRequestModel, Callback<CreateListingResponseModel> cb);

    @PUT("/")
    void updateServiceCard(@Path("id") String id,@QueryMap Map<String,String> params ,Callback<GetCreateServiceCardResponse> cb);


    @PUT("/")
    void updateUserNoImage(@Path("id") String id,
                           @Body UserDetailsWithoutImageRequestModel user
            , Callback<CreateUserResponseModel> cb);

    @PUT("/")
    void updateListing(@Path("id") String id, @Body CreateListingRequestModel createListingRequestModel, Callback<CreateListingResponseModel> cb);


    //new
    @PUT("/")
    void updateWallMessage(@Path("wall_id") String wallId, @Body PostWallMessageRequestModel postWallMessageRequestModel, Callback<GetCreateWallResponseModel> cb);


    @Multipart
    @PUT("/")
    void updateWallMessageWithImage(@Path("wall_id") String wallId,
                                    @Part("image") TypedFile image,
                                    @QueryMap Map<String, String> params,
                                    Callback<GetCreateWallResponseModel> cb);

    @Multipart
    @PUT("/")
    void updateServiceCardWithImage(@Path("id") String cardId,
                                    @Part("image") TypedFile image,
                                    @QueryMap Map<String, String> params,
                                    Callback<GetCreateServiceCardResponse> cb);


    // All the POST apis

    //new
    @POST("/")
    void createUser(@Body UserDetailsRequestModel user, Callback<SerialKeyResponseModel> cb);

    @POST("/")
    UploadContactsModel uploadContacts(@Body UploadContactRequestModel contacts);

    @POST("/")
    void getSerialCode(@Body UserDetailsRequestModel user, Callback<SerialKeyResponseModel> cb);

    @POST("/")
    void getMissCall(@Body UserDetailsRequestModel user, Callback<Object> cb);

    @POST("/")
    void verifyUserSerialCode(@Body VerifyUserRequestModel user, Callback<VerifySmsResponseModel> cb);

    @POST("/")
    void verifyUserPhoneNumber(@Body VerifyUserRequestModel user, Callback<VerifySmsResponseModel> cb);

    @POST("/")
    void commentOnWall(@Path("wall_id") String wallId,@QueryMap Map<String,String> params
            , Callback<CommentResponseModel> cb);

    //new
    @POST("/")
    void postWallMessage(@Body PostWallMessageRequestModel postWallMessageRequestModel, Callback<GetCreateWallResponseModel> cb);

    //new
    @POST("/")
    void closeWall(@Path("wall_id") String wallId, @Body CloseWallRequestModel closeWallRequestModel, Callback<Object> cb);

    //new
    @POST("/")
    void reportAbuse(@Body ReportAbuseRequestModel reportAbuseRequestModel, Callback<Object> cb);


    @POST("/")
    void bookService(@Path("id") String serviceId, Callback<Object> cb);

    @POST("/")
    void viewedServicePing(@Path("id") String serviceId, Callback<Object> cb);




    @Multipart
    @POST("/")
    void createServiceCardWithImage(
            @Part("image") TypedFile image,
            @QueryMap Map<String, String> params,
            Callback<GetCreateServiceCardResponse> cb);

    @Multipart
    @POST("/")
    void postWallMessageWithImage(
            @Part("image") TypedFile image,
            @QueryMap Map<String, String> params,
            Callback<GetCreateWallResponseModel> cb);



    @POST("/")
    void postComment(@Path("wall_id") String wallid, @Path("user_id") String user_id,
                     @Body PostWallCommentRequestModel postWallCommentRequestModel
            , Callback<GetWallItemResponseModel> cb);

    //new
    @POST("/")
    void tagUser(@Path("wall_id") String wallid,
                 @Body PostWallCommentRequestModel postWallCommentRequestModel
            , Callback<GetWallItemResponseModel> cb);

    @POST("/")
    void blockUser(@QueryMap Map<String, String> params, Callback<Object> cb);

    @POST("/")
    void postReferrer(@QueryMap Map<String, String> params, Callback<String> cb);

    @POST("/")
    void createListing(@Body CreateListingRequestModel createListingRequestModel, Callback<CreateListingResponseModel> cb);


    //new
    @POST("/")
    void selectInterests(@Body SelectInterestRequestModel selectInterestRequestModel, Callback<SelectInterestResponseModel> cb);


    //delete services
    @POST("/")
    void deleteWall(@Path("wall_id") String wall_id, Callback<Object> cb);

    @POST("/")
    void deleteService(@Path("id") String serviceId, Callback<Object> cb);

    @POST("/")
    void sendFiveInvites(@Body SendInviteRequestModel sendInviteRequestModel, Callback<String> cb);

    @POST("/")
    void rate(@Body RatingRequestModel ratingRequestModel, Callback<Object> cb);


    @GET("/")
    void getGoogleProfile(@QueryMap Map<String, String> params, Callback<GoogleProfileResponse> cb);

}
