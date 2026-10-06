package com.yby.manbo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class BookmarkActivity extends Activity {
    private BookmarkManager bm;
    private BookmarkAdapter adapter;
    private ListView listView;
    private TextView emptyHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);
        bm = new BookmarkManager(this);

        listView = findViewById(R.id.bookmark_list);
        emptyHint = findViewById(R.id.empty_hint);
        adapter = new BookmarkAdapter(this, this::deleteBookmark);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            BookmarkManager.Bookmark b = adapter.getItem(position);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setData(Uri.parse(b.url));
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_add_bookmark).setOnClickListener(v -> showAddDialog());
        refresh();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加书签");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText titleEt = new EditText(this);
        titleEt.setHint("标题");
        final EditText urlEt = new EditText(this);
        urlEt.setHint("网址");
        urlEt.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        layout.addView(titleEt);
        layout.addView(urlEt);
        builder.setView(layout);
        builder.setPositiveButton("添加", (d, w) -> {
            String t = titleEt.getText().toString().trim();
            String u = urlEt.getText().toString().trim();
            if (t.isEmpty() || u.isEmpty()) return;
            if (!u.contains("://")) u = "https://" + u;
            bm.add(t, u);
            refresh();
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void deleteBookmark(int position) {
        bm.remove(position);
        refresh();
    }

    private void refresh() {
        adapter.setBookmarks(bm.getAll());
        boolean empty = adapter.getCount() == 0;
        emptyHint.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
}
