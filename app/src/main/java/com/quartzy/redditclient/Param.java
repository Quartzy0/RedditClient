package com.quartzy.redditclient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum Param {
    NEW("new", null, "New"), HOT("hot", null, "Hot"),
    TOP_DAY("top", "day", "Top - day"), TOP_WEEK("top", "week", "Top - week"),
    TOP_MONTH("top", "month", "Top - month"), TOP_YEAR("top", "year", "Top - year"),
    TOP_ALL("top", "all", "Top - all");
    @NonNull
    public final String sortParam;
    @Nullable
    public final String altTParam;
    @NonNull
    public final String name;

    Param(@NonNull String sortParam, @Nullable String altTParam, @NonNull String name) {
        this.sortParam = sortParam;
        this.altTParam = altTParam;
        this.name = name;
    }
}
