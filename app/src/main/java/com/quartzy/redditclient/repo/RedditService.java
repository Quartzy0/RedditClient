package com.quartzy.redditclient.repo;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.quartzy.redditclient.MainActivity;
import com.quartzy.redditclient.Param;
import com.quartzy.redditclient.StateHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RedditService {
    private final RedditGatewayService gatewayService;
    private final ListeningExecutorService lExecService;

    public RedditService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gateway.reddit.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        gatewayService = retrofit.create(RedditGatewayService.class);
        lExecService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(2));
    }

    @NonNull
    public ListenableFuture<List<RedditPost>> getPosts(String lastPost, String subreddit, Param param){
        if (subreddit==null)return null;
        return lExecService.submit(() -> {
            Call<JsonObject> morePosts = gatewayService.getMorePosts(subreddit, lastPost, param.sortParam, param.altTParam, StateHandler.PAGE_SIZE);

            Response<JsonObject> execute = morePosts.execute();

            JsonObject body = execute.body();
            if (body==null){
                if (execute.code()==404) MainActivity.INSTANCE.runOnUiThread(() -> Snackbar.make(MainActivity.INSTANCE.getContextView(), "Invalid subreddit '" + StateHandler.CURRENT_SUBREDDIT + "'", BaseTransientBottomBar.LENGTH_LONG).show());
                else MainActivity.INSTANCE.runOnUiThread(() -> Snackbar.make(MainActivity.INSTANCE.getContextView(), "Failed to load data", BaseTransientBottomBar.LENGTH_LONG).show());

                return Collections.emptyList();
            }
            try {
                JsonArray postIds = body.getAsJsonArray("postIds");
                JsonObject posts1 = body.getAsJsonObject("posts");

                if (postIds.size()<1){
                    MainActivity.INSTANCE.runOnUiThread(() -> Snackbar.make(MainActivity.INSTANCE.getContextView(), "Invalid subreddit '" + StateHandler.CURRENT_SUBREDDIT + "'", BaseTransientBottomBar.LENGTH_LONG).show());
                    return Collections.emptyList();
                }

                List<RedditPost> posts = new ArrayList<>(postIds.size());
                String prevId = null;
                RedditPost prevPost = null;


                for (JsonElement postIdE : postIds) {
                    String postId = postIdE.getAsString();
                    if (prevPost != null) prevPost.postAfter = postId;
                    RedditPost e = RedditPost.parseFromJson(posts1.getAsJsonObject(postId), prevId);
                    if (!e.blank) posts.add(e);
                    prevId = postId;
                    prevPost = e;
                }

                return posts;
            }catch (Exception e){
                e.printStackTrace();
                MainActivity.INSTANCE.runOnUiThread(() -> Snackbar.make(MainActivity.INSTANCE.getContextView(), "Failed to load data: " + e.getMessage(), BaseTransientBottomBar.LENGTH_LONG).show());
            }
            return Collections.emptyList();
        });
    }

    public ListeningExecutorService getExecutor() {
        return lExecService;
    }
}
