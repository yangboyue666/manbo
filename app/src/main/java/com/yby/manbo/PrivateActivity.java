package com.yby.manbo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class PrivateActivity extends Activity {
    private WebView webView;
    private AdBlockEngine adBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private);
        adBlock = new AdBlockEngine();
        webView = findViewById(R.id.private_webview);

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);

        CookieManager.getInstance().setAcceptCookie(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String u = req.getUrl().toString();
                if (!u.startsWith("http://") && !u.startsWith("https://")) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, req.getUrl())); } catch (Exception ignore) {}
                    return true;
                }
                return false;
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (adBlock.isBlocked(request.getUrl().toString())) {
                    return new WebResourceResponse("text/plain", "utf-8",
                            new java.io.ByteArrayInputStream(new byte[0]));
                }
                return null;
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        findViewById(R.id.btn_private_back).setOnClickListener(v -> finish());

        String url = getIntent().getDataString();
        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) webView.loadUrl(url);
        else webView.loadUrl("https://www.bing.com");

        Toast.makeText(this, "隐私模式：退出将清除痕迹", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        webView.clearHistory();
        webView.clearCache(true);
        webView.clearFormData();
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        super.onDestroy();
    }
}