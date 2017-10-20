package com.gmail.jorgegilcavazos.androidjrawsample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthHelper;

import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Log in to Reddit");

        webView = (WebView) findViewById(R.id.login_webview);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.contains("code=")) {
                    webView.stopLoading();
                    setResult(RESULT_OK, new Intent().putExtra("RESULT_URL", url));
                    finish();
                } else if (url.contains("error=")) {
                    Toast.makeText(
                            LoginActivity.this,
                            getString(R.string.login_access_denied_error),
                            Toast.LENGTH_LONG
                    ).show();
                    webView.loadUrl(getAuthorizationUrl().toExternalForm());
                }
            }
        });
        webView.loadUrl(getAuthorizationUrl().toExternalForm());
    }

    private  URL getAuthorizationUrl() {
        OAuthHelper oAuthHelper = AuthenticationManager.get().getRedditClient().getOAuthHelper();
        Credentials credentials = ((MyApplication) getApplication()).getInstalledAppCredentials();
        String[] scopes = {"identity", "edit", "flair", "mysubreddits", "read", "vote",
                "submit", "subscribe", "history", "save"};
        return oAuthHelper.getAuthorizationUrl(credentials, true, true, scopes);
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
