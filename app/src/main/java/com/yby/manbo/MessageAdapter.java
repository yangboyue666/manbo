package com.yby.manbo;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MessageAdapter extends BaseAdapter {
    private final Context ctx;
    private final List<ChatClient.Message> msgs = new ArrayList<>();
    private final String myClientId;

    public MessageAdapter(Context ctx, String myClientId) {
        this.ctx = ctx;
        this.myClientId = myClientId;
    }

    public void setMessages(List<ChatClient.Message> list) {
        msgs.clear();
        msgs.addAll(list);
        notifyDataSetChanged();
    }

    @Override public int getCount() { return msgs.size(); }
    @Override public ChatClient.Message getItem(int i) { return msgs.get(i); }
    @Override public long getItemId(int i) { return i; }

    private String formatTime(String t) {
        if (t == null || t.isEmpty()) return "";
        try {
            int dot = t.indexOf('.');
            if (dot > 0) {
                int tz = t.indexOf('+', dot);
                if (tz < 0) tz = t.indexOf('-', dot);
                if (tz > 0) t = t.substring(0, dot) + t.substring(tz);
            }
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
            in.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = in.parse(t);
            SimpleDateFormat out = new SimpleDateFormat("MM-dd HH:mm", Locale.US);
            out.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            return out.format(d);
        } catch (Exception e) {
            return t;
        }
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_message, parent, false);
        }
        ChatClient.Message m = msgs.get(i);
        LinearLayout root = (LinearLayout) convertView;
        TextView nick = convertView.findViewById(R.id.msg_nick);
        TextView content = convertView.findViewById(R.id.msg_content);
        TextView time = convertView.findViewById(R.id.msg_time);
        nick.setText(m.nickname);
        content.setText(m.content);
        time.setText(formatTime(m.time));
        boolean self = m.clientId != null && myClientId != null && m.clientId.equals(myClientId);
        root.setGravity(self ? Gravity.END : Gravity.START);
        nick.setGravity(self ? Gravity.END : Gravity.START);
        time.setGravity(self ? Gravity.END : Gravity.START);
        if (self) {
            content.setBackgroundResource(R.drawable.bg_bubble_self);
            content.setTextColor(ctx.getResources().getColor(R.color.white));
        } else {
            content.setBackgroundResource(R.drawable.bg_bubble_other);
            content.setTextColor(ctx.getResources().getColor(R.color.text));
        }
        return convertView;
    }
}
