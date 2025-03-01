package com.fongmi.android.tv;

import android.app.Activity;
import android.content.DialogInterface;
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

public class MobileUpdater implements Download.Callback {

    private DialogUpdateBinding binding;
    private AlertDialog dialog;

    private static class Loader {
        static volatile MobileUpdater INSTANCE = new MobileUpdater();
    }

    public static MobileUpdater get() {
        return Loader.INSTANCE;
    }

    private File getFile() {
        return Path.cache("mobile_update.apk");
    }

    // 直接获取自定义服务器JSON地址
    private String getJson() {
        return Github.getJson();
    }

    // 从JSON中动态获取APK地址
    private String getApk(String url) {
        return Github.getApk(url);
    }

    public MobileUpdater force() {
        Notify.show(R.string.update_check);
        Setting.putUpdate(true);
        return this;
    }

    private MobileUpdater check() {
        dismiss();
        return this;
    }

    public void start(Activity activity) {
        App.execute(() -> doInBackground(activity));
    }

    // 简化版本检查逻辑（仅根据versionCode）
    private boolean need(int remoteCode) {
        return Setting.getUpdate() && remoteCode > BuildConfig.VERSION_CODE;
    }

    private void doInBackground(Activity activity) {
        try {
            String jsonStr = OkHttp.string(getJson());
            JSONObject json = new JSONObject(jsonStr);
            
            // 解析新JSON字段
            String versionName = json.optString("versionName");
            String description = json.optString("description");
            int versionCode = json.optInt("versionCode");
            String apkUrl = json.optString("url");
            
            if (need(versionCode)) {
                App.post(() -> show(activity, versionName, description, apkUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 添加apkUrl参数传递
    private void show(Activity activity, String version, String desc, String apkUrl) {
        binding = DialogUpdateBinding.inflate(LayoutInflater.from(activity));
        check().create(activity, ResUtil.getString(R.string.update_version, version)).show();
        
        // 设置带参数的点击监听器
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> confirm(apkUrl));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(this::cancel);
        binding.desc.setText(desc);
    }

    private AlertDialog create(Activity activity, String title) {
        return dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.update_confirm, null)
                .setNegativeButton(R.string.dialog_negative, null)
                .setCancelable(false)
                .create();
    }

    private void cancel(View view) {
        Setting.putUpdate(false);
        dialog.dismiss();
    }

    // 使用动态APK地址
    private void confirm(String apkUrl) {
        Download.create(getApk(apkUrl), getFile(), this).start();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
    }

    private void dismiss() {
        try {
            if (dialog != null) dialog.dismiss();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void progress(int progress) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
                String.format(Locale.getDefault(), "%1$d%%", progress)
        );
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
