package com.yby.manbo;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class PasswordActivity extends Activity {
    private PasswordVault vault;
    private ListView listView;
    private TextView emptyHint;
    private List<PasswordVault.Entry> entries = new ArrayList<>();
    private BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_password);
        vault = new PasswordVault(this);
        listView = findViewById(R.id.pwd_list);
        emptyHint = findViewById(R.id.pwd_empty);
        adapter = new BaseAdapter() {
            @Override public int getCount() { return entries.size(); }
            @Override public Object getItem(int i) { return entries.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override
            public View getView(int i, View v, ViewGroup parent) {
                if (v == null) v = getLayoutInflater().inflate(R.layout.item_password, parent, false);
                PasswordVault.Entry e = entries.get(i);
                ((TextView) v.findViewById(R.id.pwd_site)).setText(e.site);
                ((TextView) v.findViewById(R.id.pwd_user)).setText(e.username);
                ((TextView) v.findViewById(R.id.pwd_pass)).setText(e.password);
                int position = i;
                v.findViewById(R.id.pwd_del).setOnClickListener(x -> {
                    vault.remove(position);
                    refresh();
                });
                return v;
            }
        };
        listView.setAdapter(adapter);
        findViewById(R.id.btn_add_pwd).setOnClickListener(v -> showAddDialog());
        refresh();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加密码");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText site = new EditText(this); site.setHint("网站");
        EditText user = new EditText(this); user.setHint("用户名");
        EditText pass = new EditText(this); pass.setHint("密码");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(site); layout.addView(user); layout.addView(pass);
        builder.setView(layout);
        builder.setPositiveButton("保存", (d, w) -> {
            String s = site.getText().toString().trim();
            String u = user.getText().toString().trim();
            String p = pass.getText().toString().trim();
            if (s.isEmpty() || p.isEmpty()) return;
            vault.add(s, u, p);
            refresh();
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void refresh() {
        entries.clear();
        entries.addAll(vault.getAll());
        adapter.notifyDataSetChanged();
        boolean empty = entries.isEmpty();
        emptyHint.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
