package com.quartzy.redditclient.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;

import com.quartzy.redditclient.R;
import com.quartzy.redditclient.StateHandler;
import com.quartzy.redditclient.repo.RedditPost;

public class PostAdapter extends PagingDataAdapter<RedditPost, RedditPostViewHolder> {
    public PostAdapter(@NonNull DiffUtil.ItemCallback<RedditPost> diffCallback) {
        super(diffCallback);
    }

    public RedditPost getItemAt(int i){
        return getItem(i);
    }

    @NonNull
    @Override
    public RedditPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_post, parent, false);
        return new RedditPostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RedditPostViewHolder holder, int position) {
        RedditPost item = getItem(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        StateHandler.POST_COUNT = itemCount;
        return itemCount;
    }
}
