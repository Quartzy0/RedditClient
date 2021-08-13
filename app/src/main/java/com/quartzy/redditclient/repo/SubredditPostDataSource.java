package com.quartzy.redditclient.repo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.ListenableFuturePagingSource;
import androidx.paging.PagingState;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.quartzy.redditclient.StateHandler;

import java.io.IOException;
import java.util.List;

import retrofit2.HttpException;

public class SubredditPostDataSource extends ListenableFuturePagingSource<String, RedditPost> {
    private final RedditService redditService;
    private final ListeningExecutorService mBgExecutor;

    public SubredditPostDataSource(RedditService redditService, ListeningExecutorService mBgExecutor) {
        this.redditService = redditService;
        this.mBgExecutor = mBgExecutor;
    }

    @Nullable
    @Override
    public String getRefreshKey(@NonNull PagingState<String, RedditPost> pagingState) {
        return null;
//        Integer anchorPosition = pagingState.getAnchorPosition();
//        if (anchorPosition==null)return null;
//
//        LoadResult.Page<String, RedditPost> closestPage = pagingState.closestPageToPosition(anchorPosition);
//        if (closestPage==null)return null;
//
//        String prevKey = closestPage.getPrevKey();
//        if (prevKey!=null){
//            return prevKey;
//        }
//
//        return closestPage.getNextKey();
    }

    @NonNull
    @Override
    public ListenableFuture<LoadResult<String, RedditPost>> loadFuture(@NonNull LoadParams<String> loadParams) {
        if (StateHandler.CURRENT_SUBREDDIT==null){
            return Futures.immediateFuture(null);
        }
        String key = loadParams.getKey();
        if (key==null) key = "";
        System.out.println("Sent more data");

        ListenableFuture<LoadResult<String, RedditPost>> pageFuture =
                Futures.transform(redditService.getPosts(key, StateHandler.CURRENT_SUBREDDIT, StateHandler.CURRENT_PARAM),
                        this::toLoadResult, mBgExecutor);

        ListenableFuture<LoadResult<String, RedditPost>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, mBgExecutor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, mBgExecutor);

    }

    private LoadResult<String, RedditPost> toLoadResult(List<RedditPost> posts) {
        if (posts.isEmpty()){
            return new LoadResult.Error<>(new RuntimeException("Couldn't get posts from subreddit '" + StateHandler.CURRENT_SUBREDDIT + "'"));
        }
        StateHandler.LAST_POST_ID = posts.get(posts.size()-1).id;
        return new LoadResult.Page<>(
                posts,
                null,
                posts.get(posts.size()-1).id,
                LoadResult.Page.COUNT_UNDEFINED,
                LoadResult.Page.COUNT_UNDEFINED
        );
    }
}
