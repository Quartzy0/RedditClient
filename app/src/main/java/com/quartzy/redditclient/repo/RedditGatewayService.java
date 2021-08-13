package com.quartzy.redditclient.repo;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RedditGatewayService {
    @GET("desktopapi/v1/subreddits/{subreddit}?allow_over18=1")
    Call<JsonObject> getMorePosts(@Path("subreddit") String subreddit, @Query("after") String latest_post, @Query("sort") String params, @Query("t") String alternateParam, @Query("limit") int limit);
}
