package com.github.catvod.utils;

public class Github {

    // 修改为你的服务器基础地址
    private static final String URL = "http://www.176868.xyz/fongmi";

    private static String getUrl(boolean dev, String name) {
        // dev模式使用独立路径（如dev目录），非dev使用release目录
        String path = dev ? "dev" : "release";
        return URL + "/" + path + "/" + name;
    }

    public static String getJson(boolean dev, String name) {
        return getUrl(dev, name + ".json"); // 生成JSON路径
    }

    public static String getApk(boolean dev, String name) {
        return getUrl(dev, name + ".apk"); // 生成APK路径
    }
}
