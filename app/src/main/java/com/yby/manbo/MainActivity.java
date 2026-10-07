package com.yby.manbo;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
    private View homeView;
    private View addressBar;
    private View toolbar;
    private EditText homeSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settings = new SettingsStore(this);
        adBlock = new AdBlockEngine();

        urlBar = findViewById(R.id.url_bar);
        progress = findViewById(R.id.progress);
        webView = findViewById(R.id.webview);
        homeView = findViewById(R.id.home_view);
        addressBar = findViewById(R.id.address_bar);
        toolbar = findViewById(R.id.toolbar);
        homeSearch = findViewById(R.id.home_search);

        ((android.widget.TextView) findViewById(R.id.home_logo)).setText(settings.getHomeTitle());

        setupWebView();
        setupHome();

        urlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) { loadInput(urlBar.getText().toString()); return true; }
            return false;
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        findViewById(R.id.btn_forward).setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        findViewById(R.id.btn_refresh).setOnClickListener(v -> webView.reload());
        findViewById(R.id.btn_home).setOnClickListener(v -> showHome());
        findViewById(R.id.btn_star).setOnClickListener(v -> addBookmark());
        findViewById(R.id.btn_menu).setOnClickListener(v -> showMenu());

        applyAddressBarPosition();

        webView.loadUrl("about:blank");

        String intentUrl = getIntent().getDataString();
        if (intentUrl != null) { showBrowser(); webView.loadUrl(intentUrl); }
        else showHome();
    }

    private void setupHome() {
        homeSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) { loadInput(homeSearch.getText().toString()); return true; }
            return false;
        });
        findViewById(R.id.home_book).setOnClickListener(v -> startActivity(new Intent(this, BookmarkActivity.class)));
        findViewById(R.id.home_chat).setOnClickListener(v -> startActivity(new Intent(this, ChatActivity.class)));
        findViewById(R.id.home_download).setOnClickListener(v -> startActivity(new Intent(this, DownloadHistoryActivity.class)));
        findViewById(R.id.home_password).setOnClickListener(v -> startActivity(new Intent(this, PasswordActivity.class)));
        findViewById(R.id.home_private).setOnClickListener(v -> openPrivate());
        findViewById(R.id.home_settings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void showHome() {
        homeView.setVisibility(View.VISIBLE);
        addressBar.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
    }

    private void showBrowser() {
        homeView.setVisibility(View.GONE);
        addressBar.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        webView.setVisibility(View.VISIBLE);
    }

    private void loadInput(String input) {
        input = input.trim();
        if (input.isEmpty()) return;
        String url;
        if (input.contains("://")) url = input;
        else if (input.contains(".") && !input.contains(" ")) url = "https://" + input;
        else url = "https://www.bing.com/search?q=" + Uri.encode(input);
        showBrowser();
        webView.loadUrl(url);
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

    private void applyUserAgent() {
        WebSettings ws = webView.getSettings();
        if (settings.isDesktopMode()) {
            ws.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        } else {
            ws.setUserAgentString(null);
        }
    }

    private void toggleDesktopMode() {
        boolean now = !settings.isDesktopMode();
        settings.setDesktopMode(now);
        applyUserAgent();
        webView.reload();
        Toast.makeText(this, now ? "已切换为电脑版" : "已切换为手机版", Toast.LENGTH_SHORT).show();
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
        applyUserAgent();

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
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    urlBar.setText(url);
                }
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

    private void showMenu() {
        final Dialog d = new Dialog(this);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.dialog_menu);
        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                try {
                    w.setBackgroundBlurRadius(50);
                    w.setDimAmount(0.12f);
                } catch (Exception ignore) {}
            }
        }
        android.widget.TextView deskLabel = d.findViewById(R.id.menu_desktop_label);
        deskLabel.setText(settings.isDesktopMode() ? "电脑版已开" : "电脑版");
        d.findViewById(R.id.menu_bookmark).setOnClickListener(v -> { d.dismiss(); startActivity(new Intent(this, BookmarkActivity.class)); });
        d.findViewById(R.id.menu_chat).setOnClickListener(v -> { d.dismiss(); startActivity(new Intent(this, ChatActivity.class)); });
        d.findViewById(R.id.menu_download).setOnClickListener(v -> { d.dismiss(); startActivity(new Intent(this, DownloadHistoryActivity.class)); });
        d.findViewById(R.id.menu_password).setOnClickListener(v -> { d.dismiss(); startActivity(new Intent(this, PasswordActivity.class)); });
        d.findViewById(R.id.menu_private).setOnClickListener(v -> { d.dismiss(); openPrivate(); });
        d.findViewById(R.id.menu_addbookmark).setOnClickListener(v -> { d.dismiss(); addBookmark(); });
        d.findViewById(R.id.menu_desktop).setOnClickListener(v -> { d.dismiss(); toggleDesktopMode(); });
        d.findViewById(R.id.menu_settings).setOnClickListener(v -> { d.dismiss(); startActivity(new Intent(this, SettingsActivity.class)); });
        d.show();
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
        if (homeView.getVisibility() == View.VISIBLE) super.onBackPressed();
        else if (webView.canGoBack()) webView.goBack();
        else showHome();
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
        ((android.widget.TextView) findViewById(R.id.home_logo)).setText(settings.getHomeTitle());
    }
}