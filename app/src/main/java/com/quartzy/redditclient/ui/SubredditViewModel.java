package com.quartzy.redditclient.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import com.quartzy.redditclient.StateHandler;
import com.quartzy.redditclient.repo.RedditPost;
import com.quartzy.redditclient.repo.RedditService;
import com.quartzy.redditclient.repo.SubredditPostDataSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import kotlinx.coroutines.CoroutineScope;

public class SubredditViewModel extends ViewModel {
    private RedditService service;

    public void init(RedditService service) {
        this.service = service;
    }

    public LiveData<PagingData<RedditPost>> getPosts(){
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        Pager<String, RedditPost> pager = new Pager<>(
                new PagingConfig(/* pageSize = */ StateHandler.PAGE_SIZE),
                () -> new SubredditPostDataSource(service, service.getExecutor()));

        return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), viewModelScope);
    }

    public void clear(){
        try {
            Method clear = ViewModel.class.getDeclaredMethod("clear");
            clear.setAccessible(true);
            clear.invoke(this);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
