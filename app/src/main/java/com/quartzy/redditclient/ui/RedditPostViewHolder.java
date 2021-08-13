package com.quartzy.redditclient.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quartzy.redditclient.ContentViewActivity;
import com.quartzy.redditclient.R;
import com.quartzy.redditclient.StateHandler;
import com.quartzy.redditclient.repo.ImageLoadTask;
import com.quartzy.redditclient.repo.RedditPost;

public class RedditPostViewHolder extends RecyclerView.ViewHolder {
    private RedditPost post;
    private final TextView titleText;
    private final ImageView thumbnailImage;

    public RedditPostViewHolder(@NonNull View itemView) {
        super(itemView);
        this.titleText = itemView.findViewById(R.id.titleText);
        this.thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
    }

    public void bind(RedditPost item, int index) {
        this.post = item;
        if (item==null)return;
        this.titleText.setText(shortenText(item.title, StateHandler.MAX_TITLE_SIZE));
        if (URLUtil.isValidUrl(item.thumbnailUrl)) {
            new ImageLoadTask(item.thumbnailUrl, thumbnailImage).execute();
        }
        itemView.setOnClickListener(view -> {
            System.out.println("Clicked on me! " + post.toString());

            StateHandler.CURRENT_POST_ID = post.id;
            StateHandler.CURRENT_POST_INDEX = index;

            Intent intent = new Intent(itemView.getContext(), ContentViewActivity.class);
            Bundle b = new Bundle();
            b.putString("content", item.getContent());
            b.putByte("type", (byte) item.type.ordinal());
            b.putFloat("mediaRatio", item.mediaRatio);
            intent.putExtras(b);
            itemView.getContext().startActivity(intent);
        });
    }

    public static String shortenText(String in, int maxLen){
        return in.length() > maxLen ? in.substring(0, maxLen-3) + "..." : in;
    }
}
