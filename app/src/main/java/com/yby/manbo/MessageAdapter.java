package com.yby.manbo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {
    private final Context ctx;
    private final List<ChatClient.Message> msgs = new ArrayList<>();
    private final String selfNick;

    public MessageAdapter(Context ctx, String selfNick) {
        this.ctx = ctx;
        this.selfNick = selfNick;
    }

    public void setMessages(List<ChatClient.Message> list) {
        msgs.clear();
        msgs.addAll(list);
        notifyDataSetChanged();
    }

    public void addMessage(ChatClient.Message m) {
        msgs.add(m);
        notifyDataSetChanged();
    }

    @Override public int getCount() { return msgs.size(); }
    @Override public ChatClient.Message getItem(int i) { return msgs.get(i); }
    @Override public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.item_message, parent, false);
        }
        ChatClient.Message m = msgs.get(i);
        TextView nick = convertView.findViewById(R.id.msg_nick);
        TextView content = convertView.findViewById(R.id.msg_content);
        TextView time = convertView.findViewById(R.id.msg_time);
        nick.setText(m.nickname);
        content.setText(m.content);
        time.setText(m.time != null ? m.time : "");
        boolean self = m.nickname != null && m.nickname.equals(selfNick);
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
