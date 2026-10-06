package com.yby.manbo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView webView;
    private EditText urlBar;
    private ProgressBar progress;
    private SettingsStore settings;
    private AdBlockEngine adBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = new SettingsStore(this);
        adBlock = new AdBlockEngine();

        urlBar = findViewById(R.id.url_bar);
        progress = findViewById(R.id.progress);
        webView = findViewById(R.id.webview);

        setupWebView();

        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) { go(); return true; }
            return false;
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        findViewById(R.id.btn_forward).setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        findViewById(R.id.btn_refresh).setOnClickListener(v -> webView.reload());
        findViewById(R.id.btn_home).setOnClickListener(v -> webView.loadUrl(settings.getHomeUrl()));
        findViewById(R.id.btn_star).setOnClickListener(v -> addBookmark());
        findViewById(R.id.btn_menu).setOnClickListener(v -> showMenu());

        applyAddressBarPosition();

        String intentUrl = getIntent().getDataString();
        if (intentUrl != null) webView.loadUrl(intentUrl);
        else webView.loadUrl(settings.getHomeUrl());
    }

    private void applyAddressBarPosition() {
        LinearLayout root = findViewById(R.id.root);
        View addressBar = findViewById(R.id.address_bar);
        View toolbar = findViewById(R.id.toolbar);
        root.removeView(addressBar);
        if (settings.isAddressBarBottom()) {
            int idx = root.indexOfChild(toolbar);
            root.addView(addressBar, idx);
        } else {
            root.addView(addressBar, 0);
        }
    }

    private void setupWebView() {
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setAllowFileAccess(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        ws.setUserAgentString(ws.getUserAgentString() + " Manbo/1.0");

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                return false;
            }
            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (settings.isAdBlockEnabled() && adBlock.isBlocked(request.getUrl().toString())) {
                    return new android.webkit.WebResourceResponse("text/plain", "utf-8",
                            new java.io.ByteArrayInputStream(new byte[0]));
                }
                return null;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                urlBar.setText(url);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progress.setProgress(newProgress);
                progress.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                try {
                    DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
                    req.setMimeType(mimetype);
                    req.allowScanningByMediaScanner();
                    req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    String name = URLUtil.guessFileName(url, contentDisposition, mimetype);
                    req.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, name);
                    dm.enqueue(req);
                    new DownloadRecord(MainActivity.this).add(name, url);
                    Toast.makeText(MainActivity.this, "开始下载: " + name, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "下载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void go() {
        String input = urlBar.getText().toString().trim();
        if (input.isEmpty()) return;
        String url;
        if (input.contains("://")) url = input;
        else if (input.contains(".") && !input.contains(" ")) url = "https://" + input;
        else url = "https://www.bing.com/search?q=" + Uri.encode(input);
        webView.loadUrl(url);
    }

    private void showMenu() {
        String[] items = {"书签", "聊天", "隐私窗口", "下载历史", "密码库", "设置", "添加当前页为书签"};
        new AlertDialog.Builder(this)
            .setItems(items, (d, which) -> {
                switch (which) {
                    case 0: startActivity(new Intent(this, BookmarkActivity.class)); break;
                    case 1: startActivity(new Intent(this, ChatActivity.class)); break;
                    case 2: openPrivate(); break;
                    case 3: startActivity(new Intent(this, DownloadHistoryActivity.class)); break;
                    case 4: startActivity(new Intent(this, PasswordActivity.class)); break;
                    case 5: startActivity(new Intent(this, SettingsActivity.class)); break;
                    case 6: addBookmark(); break;
                }
            }).show();
    }

    private void openPrivate() {
        Intent intent = new Intent(this, PrivateActivity.class);
        String url = webView.getUrl();
        if (url != null) intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    private void addBookmark() {
        String url = webView.getUrl();
        if (url == null) return;
        String title = webView.getTitle();
        if (title == null || title.isEmpty()) title = url;
        new BookmarkManager(this).add(title, url);
        Toast.makeText(this, "已添加书签", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }
}