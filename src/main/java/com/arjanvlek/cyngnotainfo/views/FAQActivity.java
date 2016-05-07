package com.arjanvlek.cyngnotainfo.views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.arjanvlek.cyngnotainfo.BuildConfig;
import com.arjanvlek.cyngnotainfo.R;
import com.arjanvlek.cyngnotainfo.Support.NetworkConnectionManager;

public class FAQActivity extends AppCompatActivity {

    private NetworkConnectionManager networkConnectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        networkConnectionManager = new NetworkConnectionManager(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.faq_refresh_layout);
        if(refreshLayout != null) {
            refreshLayout.setColorSchemeResources(R.color.lightBlue, R.color.holo_orange_light, R.color.holo_red_light);
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadFaqPage();
                    try {
                        refreshLayout.setRefreshing(false);
                    } catch (Exception ignored) {

                    }
                }
            });
        }
        loadFaqPage();
    }

    @SuppressLint("SetJavaScriptEnabled") // JavaScript is required to toggle the FAQ Item boxes.
    private void loadFaqPage() {
        if(networkConnectionManager != null && networkConnectionManager.checkNetworkConnection()) {
            WebView FAQPageView = (WebView) findViewById(R.id.faqWebView);
            if(FAQPageView != null) {
                FAQPageView.getSettings().setJavaScriptEnabled(true);
                FAQPageView.getSettings().setUserAgentString("Cyanogen_update_tracker_" + BuildConfig.VERSION_NAME);
                FAQPageView.loadUrl("https://cyanogenupdatetracker.com/inappfaq");
            }
        } else {
            LayoutInflater inflater = getLayoutInflater();
            inflater.inflate(R.layout.activity_faq_no_network, (ViewGroup)findViewById(R.id.faq_refresh_layout), false);
        }
    }

    public void onRetryButtonClick(View v) {
        LayoutInflater inflater = getLayoutInflater();
        inflater.inflate(R.layout.activity_faq, (ViewGroup)findViewById(R.id.faq_no_network_view), false);
        loadFaqPage();
    }
}
