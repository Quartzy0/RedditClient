package com.quartzy.redditclient;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.quartzy.redditclient.repo.ImageLoadTask;
import com.quartzy.redditclient.repo.MediaType;
import com.quartzy.redditclient.repo.RedditPost;

import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.styles.Github;

public class ContentViewActivity extends YouTubeBaseActivity {

    public static final String LOG_TAG = "ContentViewReddit";

    private WifiManager.WifiLock wifiLock;
    private ConstraintLayout constraintLayout;

    private View previousView;

    private RedditPost currentPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_view);

        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, LOG_TAG);
        wifiLock.acquire();

        constraintLayout = findViewById(R.id.constraintLayout);

        FloatingActionButton fabNext = findViewById(R.id.fabNext);
        FloatingActionButton fabPrev = findViewById(R.id.fabPrev);
        FloatingActionButton fabShare = findViewById(R.id.fabShare);

        fabNext.setOnClickListener(view -> loadMedia(1));
        fabPrev.setOnClickListener(view -> loadMedia(-1));
        fabShare.setOnClickListener(view -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, currentPost.shareData);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, "Share post");
            startActivity(shareIntent);
        });

        constraintLayout.post(() -> loadMedia(0));
    }

    public void loadMedia(int mod){
        if (StateHandler.CURRENT_POST_INDEX+mod<0)return;

        final RedditPost itemAt = MainActivity.INSTANCE.getItemAt(StateHandler.CURRENT_POST_INDEX+mod);

        if (itemAt==null)return;
        currentPost = itemAt;
        StateHandler.CURRENT_POST_INDEX+=mod;
        StateHandler.CURRENT_POST_ID = itemAt.id;


        loadMedia(itemAt.getContent(), itemAt.mediaRatio, itemAt.type);
    }

    public void loadMedia(String content, float mediaRatio, MediaType type){
        if (previousView!=null){
            constraintLayout.removeView(previousView);
        }

        switch (type){
            case IMAGE:
                ImageView imageView = new ImageView(this);
                imageView.setId(View.generateViewId());
                imageView.setForegroundGravity(Gravity.CENTER_VERTICAL);
                previousView = imageView;
                new ImageLoadTask(content, imageView).execute();
                matchLayouts(imageView, constraintLayout);
                break;
            case VIDEO_YOUTUBE:
                YouTubePlayerView youTubePlayerView = new YouTubePlayerView(this);
                youTubePlayerView.setId(View.generateViewId());
                youTubePlayerView.setForegroundGravity(Gravity.CENTER_VERTICAL);
                previousView = youTubePlayerView;
                youTubePlayerView.initialize(BuildConfig.API_YOUTUBE, new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
                        if (!wasRestored) {
                            String s = extractYoutubeId(content);
                            System.out.println(content + " " + s);
                            youTubePlayer.cueVideo(s);
                            youTubePlayer.play();
                        }
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                        Toast.makeText(youTubePlayerView.getContext(), "Could not load video", Toast.LENGTH_LONG).show();
                    }
                });

                matchLayouts(youTubePlayerView, constraintLayout);
                break;
            case VIDEO:
            case GIF:
                VideoView videoView = new VideoView(this);
                videoView.setId(View.generateViewId());
                previousView = videoView;
                MediaController controller = new MediaController(this);
                videoView.setMediaController(controller);
                videoView.setForegroundGravity(Gravity.CENTER_VERTICAL);


                if (mediaRatio!=0) {
                    int w = constraintLayout.getWidth();
                    int height = (int) (w / mediaRatio);
                    int totalHeight = constraintLayout.getHeight();
                    int margin = (totalHeight -height)/2;
                    constraintLayout.addView(videoView);
                    ConstraintSet set = new ConstraintSet();
                    set.setGuidelinePercent(R.id.guideline2, 0.5f-(height/2)/totalHeight);
                    set.setGuidelinePercent(R.id.guideline3, 0.5f+(height/2)/totalHeight);
                    set.clone(constraintLayout);
                    set.connect(videoView.getId(), ConstraintSet.TOP, R.id.guideline2, ConstraintSet.TOP);
                    set.connect(videoView.getId(), ConstraintSet.BOTTOM, R.id.guideline3, ConstraintSet.BOTTOM);
                    set.applyTo(constraintLayout);
                    if (videoView.getLayoutParams()!=null) {
                        System.out.println("Already exist");
                        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
                        layoutParams.width = w;
                        layoutParams.height = height;
                        videoView.setLayoutParams(layoutParams);
                    }else{
                        System.out.println("Dont exist");
                        videoView.setLayoutParams(new ConstraintLayout.LayoutParams(w, height));
                    }
                }else{
                    matchLayouts(videoView, constraintLayout);
                }

                videoView.setVideoURI(Uri.parse(content));
                videoView.setOnPreparedListener(mediaPlayer -> {
                    mediaPlayer.setLooping(type==MediaType.GIF);
                    mediaPlayer.start();
                });
                break;
            case TEXT:
                MarkdownView markdownView = new MarkdownView(this);
                markdownView.setId(View.generateViewId());
                previousView = markdownView;

                matchLayouts(markdownView, constraintLayout);

                markdownView.addStyleSheet(new Github());
                markdownView.loadMarkdown(content);
                break;
        }
    }

    private static void matchLayouts(View view, ViewGroup parentView){
        ViewGroup.LayoutParams layoutVideoView = view.getLayoutParams();
        if (layoutVideoView==null){
            layoutVideoView = new ViewGroup.LayoutParams(parentView.getLayoutParams().width, parentView.getLayoutParams().height);
        }else {
            layoutVideoView.width = parentView.getLayoutParams().width;
            layoutVideoView.height = parentView.getLayoutParams().height;
        }
        view.setLayoutParams(layoutVideoView);
        parentView.addView(view);
    }

    private static String extractYoutubeId(String url) {
        if (url.startsWith("https://youtu.be/")){
            int length = "https://youtu.be/".length();
            int endIndex = url.indexOf("?", length);
            return url.substring(length, endIndex==-1 ? url.length() : endIndex);
        }else if (url.startsWith("https://www.youtube.com/watch?v=")){
            int length = "https://www.youtube.com/watch?v=".length();
            int endIndex = url.indexOf("&", length);
            return url.substring(length, endIndex==-1 ? url.length() : endIndex);
        }else if (url.startsWith("https://youtube.com/watch?v=")){
            int length = "https://youtube.com/watch?v=".length();
            int endIndex = url.indexOf("&", length);
            return url.substring(length, endIndex==-1 ? url.length() : endIndex);
        }else if (url.startsWith("https://www.youtube.com/embed/")){
            int length = "https://www.youtube.com/embed/".length();
            int endIndex = url.indexOf("?", length);
            return url.substring(length, endIndex==-1 ? url.length() : endIndex);
        }
        return null;
    }

    @Override
    protected void onStop() {
        if (wifiLock.isHeld())
            wifiLock.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (wifiLock.isHeld())
            wifiLock.release();
        super.onPause();
    }
}