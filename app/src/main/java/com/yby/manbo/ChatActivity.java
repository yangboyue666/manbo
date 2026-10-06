package com.yby.manbo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.List;

public class ChatActivity extends Activity {
    private static final long POLL_INTERVAL = 3000;
    private SettingsStore settings;
    private MessageAdapter adapter;
    private ListView msgList;
    private EditText msgInput;
    private Handler handler;
    private boolean polling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        settings = new SettingsStore(this);
        handler = new Handler(Looper.getMainLooper());

        msgList = findViewById(R.id.msg_list);
        msgInput = findViewById(R.id.msg_input);
        adapter = new MessageAdapter(this, settings.getNickname());
        msgList.setAdapter(adapter);

        findViewById(R.id.btn_chat_settings).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        findViewById(R.id.btn_send).setOnClickListener(v -> send());
        msgInput.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) { send(); return true; }
            return false;
        });

        if (!settings.isConfigured()) {
            Toast.makeText(this, R.string.not_configured, Toast.LENGTH_LONG).show();
        }
    }

    private void send() {
        if (!settings.isConfigured()) {
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }
        String nick = settings.getNickname();
        if (nick == null || nick.trim().isEmpty()) {
            Toast.makeText(this, R.string.nickname_prompt, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }
        String content = msgInput.getText().toString().trim();
        if (content.isEmpty()) return;
        msgInput.setText("");
        ChatClient.send(settings.getBackendUrl(), settings.getMode(), nick, content,
                settings.getClientId(), settings.getSupabaseKey(),
                new ChatClient.Callback<Boolean>() {
                    @Override public void onResult(Boolean ok) {
                        handler.post(() -> {
                            if (!ok) Toast.makeText(ChatActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                            else fetchMessages();
                        });
                    }
                    @Override public void onError(String msg) {
                        handler.post(() -> Toast.makeText(ChatActivity.this, "发送错误: " + msg, Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private void fetchMessages() {
        if (!settings.isConfigured()) return;
        ChatClient.fetch(settings.getBackendUrl(), settings.getMode(), settings.getSupabaseKey(),
                new ChatClient.Callback<List<ChatClient.Message>>() {
                    @Override public void onResult(List<ChatClient.Message> list) {
                        handler.post(() -> adapter.setMessages(list));
                    }
                    @Override public void onError(String msg) { }
                });
    }

    private final Runnable pollRunnable = new Runnable() {
        @Override public void run() {
            if (polling) {
                fetchMessages();
                handler.postDelayed(this, POLL_INTERVAL);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        adapter = new MessageAdapter(this, settings.getNickname());
        msgList.setAdapter(adapter);
        polling = true;
        fetchMessages();
        handler.postDelayed(pollRunnable, POLL_INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        polling = false;
        handler.removeCallbacks(pollRunnable);
    }
}
