package com.yby.manbo;

import android.app.Activity;
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

        RadioGroup modeGroup = findViewById(R.id.mode_group);
        EditText urlEt = findViewById(R.id.edit_backend_url);
        EditText nickEt = findViewById(R.id.edit_nickname);
        CheckBox adBlockCb = findViewById(R.id.cb_adblock);

        urlEt.setText(settings.getBackendUrl());
        nickEt.setText(settings.getNickname());
        if (settings.getMode() == 1) modeGroup.check(R.id.mode_supabase);
        else modeGroup.check(R.id.mode_php);
        adBlockCb.setChecked(settings.isAdBlockEnabled());

        Button save = findViewById(R.id.btn_save);
        save.setOnClickListener(v -> {
            String url = urlEt.getText().toString().trim();
            String nick = nickEt.getText().toString().trim();
            int mode = modeGroup.getCheckedRadioButtonId() == R.id.mode_supabase ? 1 : 0;
            settings.setBackendUrl(url);
            settings.setNickname(nick);
            settings.setMode(mode);
            settings.setAdBlockEnabled(adBlockCb.isChecked());
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
