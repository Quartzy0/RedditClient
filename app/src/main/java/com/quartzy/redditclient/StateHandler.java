package com.quartzy.redditclient;

public class StateHandler {

    public static String CURRENT_SUBREDDIT = null;
    public static String LAST_POST_ID;
    public static String CURRENT_POST_ID;
    public static int POST_COUNT;
    public static int CURRENT_POST_INDEX;

    public static Param CURRENT_PARAM = Param.HOT;

    public static int PAGE_SIZE = 20;

    public static int MAX_TITLE_SIZE = 100;
}
