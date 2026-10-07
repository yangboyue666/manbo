package com.yby.manbo;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private SettingsStore settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = new SettingsStore(this);

        EditText urlEt = findViewById(R.id.edit_backend_url);
        EditText nickEt = findViewById(R.id.edit_nickname);
        EditText homeTitleEt = findViewById(R.id.edit_home_title);
        CheckBox adBlockCb = findViewById(R.id.cb_adblock);
        CheckBox desktopCb = findViewById(R.id.cb_desktop);
        RadioGroup addrGroup = findViewById(R.id.addr_group);

        urlEt.setText(settings.getBackendUrl());
        nickEt.setText(settings.getNickname());
        homeTitleEt.setText(settings.getHomeTitle());
        adBlockCb.setChecked(settings.isAdBlockEnabled());
        desktopCb.setChecked(settings.isDesktopMode());
        if (settings.isAddressBarBottom()) addrGroup.check(R.id.addr_bottom);
        else addrGroup.check(R.id.addr_top);

        Button save = findViewById(R.id.btn_save);
        save.setOnClickListener(v -> {
            String url = urlEt.getText().toString().trim();
            String nick = nickEt.getText().toString().trim();
            String homeTitle = homeTitleEt.getText().toString().trim();
            if (homeTitle.isEmpty()) homeTitle = "曼波";
            settings.setBackendUrl(url);
            settings.setNickname(nick);
            settings.setHomeTitle(homeTitle);
            settings.setMode(1);
            settings.setAdBlockEnabled(adBlockCb.isChecked());
            settings.setDesktopMode(desktopCb.isChecked());
            settings.setAddressBarBottom(addrGroup.getCheckedRadioButtonId() == R.id.addr_bottom);
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
            finish();
        });

        Button checkUpdate = findViewById(R.id.btn_check_update);
        checkUpdate.setOnClickListener(v -> {
            Toast.makeText(this, "正在检查...", Toast.LENGTH_SHORT).show();
            UpdateChecker.check(new UpdateChecker.Callback() {
                @Override public void onResult(boolean ok, String url, long size, String msg) {
                    runOnUiThread(() -> {
                        if (!ok) { Toast.makeText(SettingsActivity.this, "检查失败: " + msg, Toast.LENGTH_SHORT).show(); return; }
                        long local = settings.getLastApkSize();
                        if (local == 0 || size == local) {
                            settings.setLastApkSize(size);
                            Toast.makeText(SettingsActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show();
                        } else {
                            new AlertDialog.Builder(SettingsActivity.this)
                                .setTitle("发现新版本")
                                .setMessage("新版本 " + size / 1024 + "KB，是否更新？")
                                .setPositiveButton("更新", (d, w) -> {
                                    Toast.makeText(SettingsActivity.this, "下载中...", Toast.LENGTH_SHORT).show();
                                    UpdateChecker.downloadAndInstall(SettingsActivity.this, url);
                                    settings.setLastApkSize(size);
                                })
                                .setNegativeButton("取消", null).show();
                        }
                    });
                }
            });
        });
    }
}
