package com.github.catvod.utils;

public class Github {

    public static final String BASE_URL = "http://www.176868.xyz/fongmi";

    public static String getJson(boolean dev, String type) {
        String channel = dev ? "dev" : "release";
        return BASE_URL + "/" + type + "/" + channel + "/update.json";
    }

    public static String getApk(String url) {
        return BASE_URL + "/" + url;
    }
}
