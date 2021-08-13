package com.quartzy.redditclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookmarkManager {

    public static final String PREFERENCE_NAME = "BOOKMARKS";
    public static final String BOOKMARKS_NAME = "bookamrks";
    public final static BookmarkManager INSTANCE = new BookmarkManager();

    public void saveBookmarks(@NonNull Context context, @NonNull List<String> bookmarks){

        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(bookmarks);

        editor.putString(BOOKMARKS_NAME, jsonFavorites);

        editor.commit();
    }

    public void addBookmark(@NonNull Context context, @NonNull String bookmark) {
        List<String> bookmarks = getBookmarks(context);
        bookmarks.add(bookmark);
        saveBookmarks(context, bookmarks);
    }

    public void removeBookmark(@NonNull Context context, @NonNull String bookmark) {
        List<String> bookmarks = getBookmarks(context);
        bookmarks.remove(bookmark);
        saveBookmarks(context, bookmarks);
    }

    @NonNull
    public List<String> getBookmarks(@NonNull Context context) {
        List<String> bookmarks;

        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(BOOKMARKS_NAME)) {
            String jsonFavorites = settings.getString(BOOKMARKS_NAME, null);
            Gson gson = new Gson();
            String[] favoriteItems = gson.fromJson(jsonFavorites,
                    String[].class);

            bookmarks = new ArrayList<>(Arrays.asList(favoriteItems));
        } else
            return new ArrayList<>();

        return bookmarks;
    }

    public static class BookmarkAdapter extends BaseAdapter{

        private List<String> bookmarks;
        private Context context;

        public BookmarkAdapter(Context context) {
            this.context = context;
            this.bookmarks = BookmarkManager.INSTANCE.getBookmarks(this.context);
        }

        public void refresh(){
            this.bookmarks = BookmarkManager.INSTANCE.getBookmarks(this.context);
        }

        @Override
        public int getCount() {
            return this.bookmarks.size();
        }

        @Override
        public String getItem(int i) {
            return this.bookmarks.get(i);
        }

        @Override
        public long getItemId(int i) {
            return this.bookmarks.get(i).hashCode();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view==null){
                view = new TextView(viewGroup.getContext());
                ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
            }
            ((TextView) view).setText(this.bookmarks.get(i));
            return view;
        }
    }
}
