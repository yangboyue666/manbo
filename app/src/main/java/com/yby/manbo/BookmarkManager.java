package com.yby.manbo;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class BookmarkManager {
    private static final String PREFS = "manbo_bookmarks";
    private static final String KEY_LIST = "list";
    private final SharedPreferences sp;

    public static class Bookmark {
        public String title;
        public String url;
        public Bookmark(String t, String u) { title = t; url = u; }
    }

    public BookmarkManager(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<Bookmark> getAll() {
        List<Bookmark> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_LIST, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Bookmark(o.getString("title"), o.getString("url")));
            }
        } catch (JSONException ignored) { }
        return list;
    }

    public void add(String title, String url) {
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_LIST, "[]"));
            JSONObject o = new JSONObject();
            o.put("title", title);
            o.put("url", url);
            arr.put(o);
            sp.edit().putString(KEY_LIST, arr.toString()).apply();
        } catch (JSONException ignored) { }
    }

    public void remove(int index) {
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY_LIST, "[]"));
            JSONArray newArr = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                if (i != index) newArr.put(arr.get(i));
            }
            sp.edit().putString(KEY_LIST, newArr.toString()).apply();
        } catch (JSONException ignored) { }
    }
}
