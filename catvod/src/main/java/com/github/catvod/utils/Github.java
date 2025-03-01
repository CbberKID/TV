package com.github.catvod.utils;

public class Github {

    // 修改为你的服务器基础地址
    public static final String URL = "http://www.176868.xyz/fongmi";

    private static String getUrl(boolean dev, String type, String name) {
        String path = dev ? "dev" : "release"; // 开发版/正式版路径
        return URL + "/" + type + "/" + path + "/" + name; // 按设备类型分目录
    }

    public static String getJson(boolean dev, String type) {
        return getUrl(dev, type, "update.json");
    }

    public static String getApk(boolean dev, String name) {
        return getUrl(dev, name.split("-")[0], name + ".apk"); // 从name中提取类型（tv/mobile）
    }
}
