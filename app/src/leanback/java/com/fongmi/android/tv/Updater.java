package com.fongmi.android.tv;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.fongmi.android.tv.databinding.DialogUpdateBinding;
import com.fongmi.android.tv.utils.Download;
import com.fongmi.android.tv.utils.FileUtil;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Github;
import com.github.catvod.utils.Path;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

public class Updater implements Download.Callback {

    private DialogUpdateBinding binding;
    private AlertDialog dialog;

    private static class Loader {
        static volatile Updater INSTANCE = new Updater();
    }

    public static Updater get() {
        return Loader.INSTANCE;
    }

    private File getFile() {
        return Path.cache("update.apk");
    }

    // 直接调用新的Github.getJson()
    private String getJson() {
        return Github.getJson();
    }

    // 通过JSON中的url字段获取APK地址
    private String getApk(String url) {
        return Github.getApk(url);
    }

    public Updater force() {
        Notify.show(R.string.update_check);
        Setting.putUpdate(true);
        return this;
    }

    private Updater check() {
        dismiss();
        return this;
    }

    public void start(Activity activity) {
        App.execute(() -> doInBackground(activity));
    }

    // 版本检查逻辑（根据versionCode判断）
    private boolean need(int remoteCode, int localCode) {
        return Setting.getUpdate() && remoteCode > localCode;
    }

    private void doInBackground(Activity activity) {
        try {
            // 获取JSON数据
            String json = OkHttp.string(getJson());
            JSONObject object = new JSONObject(json);
            
            // 解析字段（与你的JSON结构匹配）
            String versionName = object.optString("versionName");
            String description = object.optString("description");
            int versionCode = object.optInt("versionCode");
            String apkUrl = object.optString("url"); // APK下载路径
            
            // 获取本地版本号
            int localVersionCode = BuildConfig.VERSION_CODE;
            
            if (need(versionCode, localVersionCode)) {
                App.post(() -> show(activity, versionName, description, apkUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 添加apkUrl参数
    private void show(Activity activity, String version, String desc, String apkUrl) {
        binding = DialogUpdateBinding.inflate(LayoutInflater.from(activity));
        binding.version.setText(ResUtil.getString(R.string.update_version, version));
        binding.confirm.setOnClickListener(v -> confirm(apkUrl)); // 传递apkUrl
        binding.cancel.setOnClickListener(this::cancel);
        check().create(activity).show();
        binding.desc.setText(desc);
    }

    private AlertDialog create(Activity activity) {
        return dialog = new MaterialAlertDialogBuilder(activity)
                .setView(binding.getRoot())
                .setCancelable(false)
                .create();
    }

    private void cancel(View view) {
        Setting.putUpdate(false);
        dismiss();
    }

    // 使用动态APK地址
    private void confirm(String apkUrl) {
        binding.confirm.setEnabled(false);
        Download.create(getApk(apkUrl), getFile(), this).start();
    }

    private void dismiss() {
        try {
            if (dialog != null) dialog.dismiss();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void progress(int progress) {
        binding.confirm.setText(String.format(Locale.getDefault(), "%1$d%%", progress));
    }

    @Override
    public void error(String msg) {
        Notify.show(msg);
        dismiss();
    }

    @Override
    public void success(File file) {
        FileUtil.openFile(file);
        dismiss();
    }
}
