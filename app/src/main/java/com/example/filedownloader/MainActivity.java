package com.example.filedownloader;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;
import static android.view.KeyEvent.KEYCODE_BACK;

/*
 * Weview加载js、html文件
 *
 * */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private WebView webView;

    //todo 如果是assert不是下载文件的话直接用
    //webView.loadUrl("file:////android_asset/A4.html");

    @SuppressLint("JavascriptInterface")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        //支持放大网页功能
        webView.getSettings().setAllowFileAccess(true);
        //支持缩小网页功能
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDefaultTextEncodingName("GBK");//设置字符编码
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(MainActivity.this, "java");//name:android在网页里面可以用window.name.方法名调用java方法
        findViewById(R.id.tv_download).setOnClickListener(this);


//        //----------------webviewCookie---------------------------------------//
//        //todo cookie设置
//        CookieSyncManager.createInstance(this);
//        CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.setAcceptCookie(true);
//        String cookie = "name=xxx;age=18";
//        cookieManager.setCookie(URL, cookie);
//        CookieSyncManager.getInstance().sync();
//        //todo 获取cookie
//        CookieManager cookieManager1 = CookieManager.getInstance();
//        String cookie1 = cookieManager1.getCookie(URL);
//        //todo 清除cookie
//        CookieSyncManager.createInstance(getApplicationContext());
//        CookieManager cookieManager2 = CookieManager.getInstance();
//        cookieManager2.removeAllCookie();
//        CookieSyncManager.getInstance().sync();


//        //----------------webview缓存---------------------------------------//
//        //优先使用缓存:
//        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
////缓存模式如下：
////LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据
////LOAD_DEFAULT: （默认）根据cache-control决定是否从网络上取数据。
////LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
////LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，或者no-cache，都使用缓存中的数据。
//        //不使用缓存:
//        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//        //清除网页访问留下的缓存
////由于内核缓存是全局的因此这个方法不仅仅针对webview而是针对整个应用程序.
//        // TODO 清理缓存
//        webView.clearCache(true);
//
////清除当前webview访问的历史记录
////只会webview访问历史记录里的所有记录除了当前访问记录
//        webView.clearHistory();
//
////这个api仅仅清除自动完成填充的表单数据，并不会清除WebView存储到本地的数据
//        webView.clearFormData();
//
//        另外一种方式:
////删除缓存文件夹
//        File file = CacheManager.getCacheFileBaseDir();
//
//        if (file != null && file.exists() && file.isDirectory()) {
//            for (File item : file.listFiles()) {
//                item.delete();
//            }
//            file.delete();
//        }
//
////删除缓存数据库
//        getApplication().deleteDatabase("webview.db");
//        getApplication().deleteDatabase("webviewCache.db");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_download:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        downloadFile();
                    }
                }).start();
                break;
        }
    }

    private void downloadFile() {
        //下载路径，如果路径无效了，可换成你的下载路径
        final String url = "http://ganjiang.top:1005/log/ceshi.html";
        final long startTime = System.currentTimeMillis();
        Log.i("DOWNLOAD", "startTime=" + startTime);

        Request request = new Request.Builder().url(url).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // 下载失败
                e.printStackTrace();
                Log.i("DOWNLOAD", "download failed");
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                Sink sink = null;
                BufferedSink bufferedSink = null;
                try {
                    String mSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    File dest = new File(mSDCardPath, "web1.html");
                    sink = Okio.sink(dest);
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.writeAll(response.body().source());
                    bufferedSink.close();
                    Log.i("DOWNLOAD", "download success");
                    Log.i("DOWNLOAD", "totalTime=" + (System.currentTimeMillis() - startTime));
                    //todo 加载html文件
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            //todo 先加载网页html
                            webView.loadUrl("file:///mnt/sdcard/a1.html");
                            //todo 这是调用assert下的 下载无法到assert文件夹下，所以---
//                            webView.loadUrl("file:////android_asset/A4.html");
                            webView.setWebViewClient(new WebViewClient() {

                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    super.onPageFinished(view, url);
                                    //调用Js方法 可用
//                                    webView.loadUrl("javascript:javaToJS()");
                                }
                            });

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("DOWNLOAD", "download failed");
                } finally {
                    if (bufferedSink != null) {
                        bufferedSink.close();
                    }

                }
            }
        });
    }
//todo Back键控制网页后退
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KEYCODE_BACK) && webView.canGoBack()) {
//            webView.goBack();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
}
