package com.github.catvod.utils;

public class Github {

    public static final String URL = "http://www.176868.xyz/fongmi";

    public static String getJson() {
        return URL + "/update.json";
    }

    public static String getApk(String url) {
        return URL + "/" + url; 
    }
}
