package com.yby.manbo;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class DownloadHistoryActivity extends Activity {
    private DownloadRecord record;
    private ListView listView;
    private TextView emptyHint;
    private List<DownloadRecord.Item> items = new ArrayList<>();
    private BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_download);
        record = new DownloadRecord(this);
        listView = findViewById(R.id.download_list);
        emptyHint = findViewById(R.id.download_empty);
        adapter = new BaseAdapter() {
            @Override public int getCount() { return items.size(); }
            @Override public Object getItem(int i) { return items.get(i); }
            @Override public long getItemId(int i) { return i; }
            @Override
            public View getView(int i, View v, ViewGroup parent) {
                if (v == null) v = getLayoutInflater().inflate(R.layout.item_download, parent, false);
                DownloadRecord.Item it = items.get(i);
                ((TextView) v.findViewById(R.id.dl_name)).setText(it.name);
                ((TextView) v.findViewById(R.id.dl_time)).setText(it.time);
                return v;
            }
        };
        listView.setAdapter(adapter);
        findViewById(R.id.btn_clear_download).setOnClickListener(v ->
            new AlertDialog.Builder(this).setTitle("清空").setMessage("确定清空下载历史？")
                .setPositiveButton("确定", (d, w) -> { record.clear(); refresh(); })
                .setNegativeButton("取消", null).show());
        refresh();
    }

    private void refresh() {
        items.clear();
        items.addAll(record.getAll());
        adapter.notifyDataSetChanged();
        boolean empty = items.isEmpty();
        emptyHint.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
