package com.yby.manbo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {
    public static class Message {
        public String nickname;
        public String content;
        public String time;
        public Message(String n, String c, String t) { nickname = n; content = c; time = t; }
    }

    public interface Callback<T> {
        void onResult(T result);
        void onError(String msg);
    }

    public static void send(final String baseUrl, final int mode, final String nickname,
                            final String content, final String clientId, final String supaKey,
                            final Callback<Boolean> cb) {
        new Thread(() -> {
            try {
                boolean ok;
                if (mode == 1) ok = sendSupabase(baseUrl, nickname, content, clientId, supaKey);
                else ok = sendPhp(baseUrl, nickname, content, clientId);
                cb.onResult(ok);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        }).start();
    }

    public static void fetch(final String baseUrl, final int mode, final String supaKey,
                             final Callback<List<Message>> cb) {
        new Thread(() -> {
            try {
                List<Message> list;
                if (mode == 1) list = fetchSupabase(baseUrl, supaKey);
                else list = fetchPhp(baseUrl);
                cb.onResult(list);
            } catch (Exception e) {
                cb.onError(e.getMessage());
            }
        }).start();
    }

    private static boolean sendPhp(String baseUrl, String nickname, String content, String clientId) throws IOException {
        String url = baseUrl.endsWith("/") ? baseUrl + "send.php" : baseUrl + "/send.php";
        String params = "nickname=" + URLEncoder.encode(nickname, "UTF-8")
                + "&content=" + URLEncoder.encode(content, "UTF-8")
                + "&client_id=" + URLEncoder.encode(clientId, "UTF-8");
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream os = conn.getOutputStream();
        os.write(params.getBytes("UTF-8"));
        os.flush();
        os.close();
        int code = conn.getResponseCode();
        conn.disconnect();
        return code == 200;
    }

    private static List<Message> fetchPhp(String baseUrl) throws IOException, JSONException {
        String url = baseUrl.endsWith("/") ? baseUrl + "get.php" : baseUrl + "/get.php";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        conn.disconnect();
        JSONArray arr = new JSONArray(sb.toString());
        List<Message> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            list.add(new Message(
                    o.optString("nickname", "?"),
                    o.optString("content", ""),
                    o.optString("create_time", o.optString("time", ""))));
        }
        return list;
    }

    private static boolean sendSupabase(String baseUrl, String nickname, String content,
                                        String clientId, String apiKey) throws IOException, JSONException {
        String url = baseUrl.endsWith("/") ? baseUrl + "rest/v1/messages" : baseUrl + "/rest/v1/messages";
        JSONObject body = new JSONObject();
        body.put("nickname", nickname);
        body.put("content", content);
        body.put("client_id", clientId);
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("apikey", apiKey);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Prefer", "return=minimal");
        OutputStream os = conn.getOutputStream();
        os.write(body.toString().getBytes("UTF-8"));
        os.flush();
        os.close();
        int code = conn.getResponseCode();
        conn.disconnect();
        return code >= 200 && code < 300;
    }

    private static List<Message> fetchSupabase(String baseUrl, String apiKey) throws IOException, JSONException {
        String url = baseUrl.endsWith("/")
                ? baseUrl + "rest/v1/messages?select=nickname,content,create_time&order=create_time.asc&limit=100"
                : baseUrl + "/rest/v1/messages?select=nickname,content,create_time&order=create_time.asc&limit=100";
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestProperty("apikey", apiKey);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        conn.disconnect();
        JSONArray arr = new JSONArray(sb.toString());
        List<Message> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            list.add(new Message(
                    o.optString("nickname", "?"),
                    o.optString("content", ""),
                    o.optString("create_time", "")));
        }
        return list;
    }
}
