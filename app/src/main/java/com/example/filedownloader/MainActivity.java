package com.example.filedownloader;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import android.webkit.WebView;
import android.webkit.WebViewClient;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

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

/*
 *
 *
 * Weview加载js、html文件
 *
 * */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);
        webView.getSettings().setAllowFileAccess(true);
        //支持放大网页功能
        webView.getSettings().setSupportZoom(true);
        //支持缩小网页功能
        webView.getSettings().setBuiltInZoomControls(true);
        //支持JAVA
        webView.getSettings().setJavaScriptEnabled(true);
        findViewById(R.id.tv_download).setOnClickListener(this);

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
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);

                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        //getLocalStorageData(view);
                        //todo webView加载js文件
                        loadAssert("file:///mnt/sdcard/aa1.js", getApplicationContext());
                    }
                });

                break;
        }
    }


    public String loadAssert(String path, Context context) {
        if (context == null || TextUtils.isEmpty(path)) {
            return null;
        }

        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            File f = new File(path);
            inputStream = new FileInputStream(f);
            // inputStream = context.getAssets().open(path);
            StringBuilder builder = new StringBuilder(inputStream.available() + 10);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            char[] data = new char[4096];
            int len = -1;
            while ((len = bufferedReader.read(data)) > 0) {
                builder.append(data, 0, len);
            }

            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
            }
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
            }
        }

        return "";
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
                    File dest = new File(mSDCardPath, "A4.html");
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
                            webView.loadUrl("file:///mnt/sdcard/A4.html");
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
}
