package com.yby.manbo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class BookmarkAdapter extends BaseAdapter {
    private final Context ctx;
    private final List<BookmarkManager.Bookmark> list = new ArrayList<>();
    private final OnDeleteListener listener;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public BookmarkAdapter(Context ctx, OnDeleteListener l) {
        this.ctx = ctx;
        this.listener = l;
    }

    public void setBookmarks(List<BookmarkManager.Bookmark> bms) {
        list.clear();
        list.addAll(bms);
        notifyDataSetChanged();
    }

    @Override public int getCount() { return list.size(); }
    @Override public BookmarkManager.Bookmark getItem(int i) { return list.get(i); }
    @Override public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_bookmark, parent, false);
        }
        BookmarkManager.Bookmark b = list.get(i);
        ((TextView) convertView.findViewById(R.id.bm_title)).setText(b.title);
        ((TextView) convertView.findViewById(R.id.bm_url)).setText(b.url);
        Button del = convertView.findViewById(R.id.bm_delete);
        del.setOnClickListener(v -> { if (listener != null) listener.onDelete(i); });
        return convertView;
    }
}
