package com.quartzy.redditclient.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.quartzy.redditclient.repo.RedditPost;

public class PostComparator extends DiffUtil.ItemCallback<RedditPost> {
    @Override
    public boolean areItemsTheSame(@NonNull RedditPost oldItem, @NonNull RedditPost newItem) {
        return oldItem.id.equals(newItem.id);
    }

    @Override
    public boolean areContentsTheSame(@NonNull RedditPost oldItem, @NonNull RedditPost newItem) {
        return oldItem.equals(newItem);
    }
}
