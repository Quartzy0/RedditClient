package com.quartzy.redditclient.repo;

import androidx.annotation.NonNull;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

public class RedditPost {

    public static RedditPost parseFromJson(JsonObject obj, String postBefore){
        String id = obj.getAsJsonPrimitive("id").getAsString();
        String title = obj.getAsJsonPrimitive("title").getAsString();

        int thumbnailWidth = 0, thumbnailHeight = 0;
        String thumbnailUrl = "";

        JsonObject thumbnail = obj.getAsJsonObject("thumbnail");
        try {
            thumbnailUrl = thumbnail.getAsJsonPrimitive("url").getAsString();
            if (!thumbnailUrl.equals("self")) {
                thumbnailWidth = thumbnail.getAsJsonPrimitive("width").getAsInt();
                thumbnailHeight = thumbnail.getAsJsonPrimitive("height").getAsInt();
            }else if (obj.has("url")){
                thumbnailUrl = thumbnail.getAsJsonPrimitive("url").getAsString();
            }
        }catch (ClassCastException ignored){
            //Ignored
        }

        MediaType type = MediaType.UNRECOGNIZED;
        String content = null;
        String shareData = obj.getAsJsonPrimitive("permalink").getAsString();

        if (!obj.has("media") || obj.get("media") instanceof JsonNull){

            if (obj.has("selftext")){
                type = MediaType.TEXT;
                content = obj.getAsJsonPrimitive("selftext").getAsString();
            }

            return new RedditPost(id, title, thumbnailWidth, thumbnailHeight, thumbnailUrl, type, content, postBefore, null, content==null, obj, 0, shareData);
        }

        JsonObject media = obj.getAsJsonObject("media");

        if (obj.getAsJsonPrimitive("post_hint")!=null && !obj.getAsJsonPrimitive("post_hint").isJsonNull() && obj.getAsJsonPrimitive("post_hint").getAsString().equals("rich:video")){
            type = MediaType.VIDEO;
            content = obj.getAsJsonPrimitive("url").getAsString();
            return new RedditPost(id, title, thumbnailWidth, thumbnailHeight, thumbnailUrl, type, content, postBefore, null, false, obj, 0, shareData);
        }
        float mediaRation = 0;
        switch (media.getAsJsonPrimitive("type").getAsString()) {
            case "image":
                if (!media.has("content")) return new RedditPost(id, title, thumbnailWidth, thumbnailHeight, thumbnailUrl, type, null, postBefore, null, true, obj, 0, shareData);
                type = MediaType.IMAGE;
                content = media.getAsJsonPrimitive("content").getAsString();
//                shareData = content;
                break;
            case "text":
                if (!media.has("content")) return new RedditPost(id, title, thumbnailWidth, thumbnailHeight, thumbnailUrl, type, null, postBefore, null, true, obj, 0, shareData);
                type = MediaType.TEXT;
                if (media.getAsJsonPrimitive("rteMode").getAsString().equals("richtext")) {
                    JsonPrimitive markdownContent = media.getAsJsonPrimitive("markdownContent");
                    if (markdownContent == null || markdownContent.isJsonNull()) {
                        content = media.getAsJsonPrimitive("content").getAsString();
                    } else {
                        content = markdownContent.getAsString();
                    }
                } else {
                    content = media.getAsJsonPrimitive("content").getAsString();
                }
                break;
            case "video":
                type = media.getAsJsonPrimitive("isGif").getAsBoolean() ? MediaType.GIF : MediaType.VIDEO;
                content = media.getAsJsonPrimitive("hlsUrl").getAsString();
                mediaRation = ((float) media.getAsJsonPrimitive("width").getAsInt())/((float) media.getAsJsonPrimitive("height").getAsInt());
                break;
            case "youtube.com":
                type = MediaType.VIDEO_YOUTUBE;
                content = obj.getAsJsonPrimitive("url").getAsString();
                shareData = content;
                break;
            case "embed":
                String asString = obj.getAsJsonObject("source").getAsJsonPrimitive("url").getAsString();
                if (asString.contains("youtube.com") || asString.contains("youtu.be")){
                    type = MediaType.VIDEO_YOUTUBE;
                    content = asString;
                    shareData = content;
                    mediaRation = ((float) media.getAsJsonPrimitive("width").getAsInt())/((float) media.getAsJsonPrimitive("height").getAsInt());
                }else if (media.has("videoPreview")){
                    JsonObject videoPreview = media.getAsJsonObject("videoPreview");
                    type = videoPreview.getAsJsonPrimitive("isGif").getAsBoolean() ? MediaType.GIF : MediaType.VIDEO;
                    content = videoPreview.getAsJsonPrimitive("hlsUrl").getAsString();
                    mediaRation = ((float) videoPreview.getAsJsonPrimitive("width").getAsInt())/((float) videoPreview.getAsJsonPrimitive("height").getAsInt());
                }
                break;
            case "gifvideo":
                type = MediaType.GIF;
                content = media.getAsJsonPrimitive("content").getAsString();
                mediaRation = ((float) media.getAsJsonPrimitive("width").getAsInt())/((float) media.getAsJsonPrimitive("height").getAsInt());
                break;
        }
        return new RedditPost(id, title, thumbnailWidth, thumbnailHeight, thumbnailUrl, type, content, postBefore, null, false, obj, mediaRation, shareData);
    }

    public final String id;
    public final String title;

    public final int thumbnailWidth, thumbnailHeight;
    public final String thumbnailUrl;

    //Media
    public final MediaType type;
    private final String content;

    public String postBefore, postAfter;

    public final boolean blank;

    public final JsonObject sourceJson;

    public final float mediaRatio;

    public final String shareData;

    public RedditPost(String id, String title, int thumbnailWidth, int thumbnailHeight, String thumbnailUrl, MediaType type, String content, String postBefore, String postAfter, boolean blank, JsonObject sourceJson, float mediaRatio, String shareData) {
        this.id = id;
        this.title = title;
        this.thumbnailWidth = thumbnailWidth;
        this.thumbnailHeight = thumbnailHeight;
        this.thumbnailUrl = thumbnailUrl;
        this.type = type;
        this.content = content;
        this.postBefore = postBefore;
        this.postAfter = postAfter;
        this.blank = blank;
        this.sourceJson = sourceJson;
        this.mediaRatio = mediaRatio;
        this.shareData = shareData;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedditPost that = (RedditPost) o;
        return thumbnailWidth == that.thumbnailWidth && thumbnailHeight == that.thumbnailHeight && Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(thumbnailUrl, that.thumbnailUrl) && type == that.type && Objects.equals(content, that.content) && Objects.equals(postBefore, that.postBefore) && Objects.equals(postAfter, that.postAfter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, thumbnailWidth, thumbnailHeight, thumbnailUrl, type, content, postBefore, postAfter);
    }

    @Override
    @NonNull
    public String toString() {
        return "RedditPost{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", thumbnailWidth=" + thumbnailWidth +
                ", thumbnailHeight=" + thumbnailHeight +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", type=" + type +
                ", content=" + content +
                ", postBefore='" + postBefore + '\'' +
                ", postAfter='" + postAfter + '\'' +
                ", blank=" + blank +
                ", sourceJson=" + sourceJson.toString() +
                '}';
    }
}
