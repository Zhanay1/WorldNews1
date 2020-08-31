package com.example.globalnews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.globalnews.db.FavoriteNews;
import com.example.globalnews.db.News;
import com.example.globalnews.db.NewsViewModel;

public class WebViewActivity extends AppCompatActivity {
    private WebView webView;
    private String title;
    private String sourceName;
    private String author;
    private String content;
    private String url;
    private String urlToImage;
    private String description;
    private String publishedAt;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private TextView textViewTitle;
    SharedPref mySharedPref ;
    private static int currentTheme;

    private NewsViewModel newsViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mySharedPref = new SharedPref(this);
        if(mySharedPref.loadNightModeState() == true){
            setTheme(R.style.DarkTheme);
            currentTheme = R.style.DarkTheme;
        }else{
            setTheme(R.style.AppTheme);
            currentTheme = R.style.AppTheme;
        }
        setContentView(R.layout.activity_web_view);
        progressBar = findViewById(R.id.progressBarLoading);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textViewTitle = findViewById(R.id.textViewTitleOfToolBar);
//        ActionBar actionBar = getSupportActionBar();
        webView = findViewById(R.id.webviewNews);
        Intent intent = getIntent();
        if (intent.hasExtra("url") && intent.hasExtra("title")) {
            title = intent.getStringExtra("title");
            sourceName = intent.getStringExtra("sourceName");
            author = intent.getStringExtra("author");
            content = intent.getStringExtra("content");
            url = intent.getStringExtra("url");
            urlToImage = intent.getStringExtra("urlToImage");
            publishedAt = intent.getStringExtra("published");
            description = intent.getStringExtra("description");

        } else {
            finish();
        }
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                // Do something on page loading started
                // Visible the progressbar
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url){
                // Do something when page loading finished
            }
        });
        newsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        webView.setWebChromeClient(new WebChromeClient(){
            /*
                public void onProgressChanged (WebView view, int newProgress)
                    Tell the host application the current progress of loading a page.
                Parameters
                    view : The WebView that initiated the callback.
                    newProgress : Current page loading progress, represented by an integer
                        between 0 and 100.
            */
            public void onProgressChanged(WebView view, int newProgress){
                // Update the progress bar with page loading progress
                progressBar.setProgress(newProgress);
                if(newProgress == 100){
                    // Hide the progressbar
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        if (savedInstanceState == null)
        {
            webView.loadUrl(url);
        }
        getSupportActionBar().setTitle(title);
        textViewTitle.setText("");
    }
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favorite, menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        FavoriteNews favoriteNews = newsViewModel.getFavoriteNewsByTitle(title);
        if (favoriteNews == null) {
            menu.findItem(R.id.Favorites).setIcon(ContextCompat.getDrawable(this, R.drawable.favorite_white));
        } else {
            menu.findItem(R.id.Favorites).setIcon(ContextCompat.getDrawable(this, R.drawable.favorite_remove));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.mShare) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareBody = url;
            String ShareSub = title;
            intent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
            intent.putExtra(Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(intent, getString(R.string.share_using)));
        }
        else if (id == R.id.Favorites) {
            FavoriteNews favoriteNews = newsViewModel.getFavoriteNewsByTitle(this.title);
            if(favoriteNews == null){
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.favorite_remove));
                newsViewModel.insertFavoriteNews(new FavoriteNews(sourceName, author, title, description, url, urlToImage, publishedAt, content));                Toast.makeText(this, R.string.add_to_favorites, Toast.LENGTH_SHORT).show();
            }
            else{
                newsViewModel.deleteFavoriteNews(favoriteNews);
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.favorite_white));
                Toast.makeText(this, R.string.delete_of_favorites, Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }
}
