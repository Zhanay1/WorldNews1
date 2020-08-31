package com.example.globalnews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.example.globalnews.Adapters.NewsAdapter;
import com.example.globalnews.db.FavoriteNews;
import com.example.globalnews.db.News;
import com.example.globalnews.db.NewsViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    private RecyclerView recyclerViewFavoriteNews;
    private NewsAdapter newsAdapter;
    private NewsViewModel viewModel;
    private News news;
    private Toolbar toolbar;
    private Switch switchDark;
    SharedPref mySharedPref ;
    private static int currentTheme;

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

        setContentView(R.layout.activity_favorite);

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        getSupportActionBar().setCustomView(R.layout.action_bar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_favorite);
        recyclerViewFavoriteNews = findViewById(R.id.recycleFavoriteNews);
        newsAdapter = new NewsAdapter();
        recyclerViewFavoriteNews.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        recyclerViewFavoriteNews.setAdapter(newsAdapter);
        viewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        final LiveData<List<FavoriteNews>> favoriteNews = viewModel.getFavoriteNews();
        favoriteNews.observe(this, new Observer<List<FavoriteNews>>() {
            @Override
            public void onChanged(List<FavoriteNews> favoriteNews) {
                List<News> news = new ArrayList<>();
                if (favoriteNews != null) {
                    for(int i = favoriteNews.size() - 1; i >= 0; i--) {
                        news.add(favoriteNews.get(i));
                    }
                    newsAdapter.setNews(news);
                }
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        Intent intentHome = new Intent(getApplicationContext(), MainActivity.class);
                        overridePendingTransition(0, 0);
                        intentHome.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intentHome);
                        return true;
                    case R.id.nav_favorite:
                        return true;
                    case R.id.nav_search:
                        Intent intentSearch = new Intent(getApplicationContext(), SearchActivity.class);
                        overridePendingTransition(0, 0);
                        intentSearch.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intentSearch);
                        return true;
                }
                return false;
            }
        });
        newsAdapter.setClickOnCardListener(new NewsAdapter.ClickOnCardListener() {
            @Override
            public void ClickOnCard(int position) {
                News news = newsAdapter.news.get(position);
                Intent intent = new Intent(FavoriteActivity.this, DetailNewsActivity.class);
                intent.putExtra("title", news.getTitle());
                intent.putExtra("sourceName", news.getSourceName());
                intent.putExtra("author", news.getAuthor());
                intent.putExtra("urlToImage", news.getUrlToImage());
                intent.putExtra("content", news.getContent());
                intent.putExtra("url", news.getUrl());
                intent.putExtra("published", news.getPublishedAt());
                intent.putExtra("description", news.getDescription());
                startActivity(intent);
            }
        });
        newsAdapter.setClickOnShareListener(new NewsAdapter.ClickOnShareListener() {
            @Override
            public void ClickOnShare(int position) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String shareBody = newsAdapter.news.get(position).getUrl();
                intent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
                intent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(intent, getString(R.string.share_using)));
            }
        });
        newsAdapter.setClickOnAddToFavoriteListener(new NewsAdapter.ClickOnAddToFavoriteListener() {
            @Override
            public void ClickOnAddToFavorite(int position) {
                News news = newsAdapter.getNews().get(position);
                FavoriteNews favoriteNews = viewModel.getFavoriteNewsByTitle(news.getTitle());
                if (favoriteNews == null) {
                    viewModel.insertFavoriteNews(new FavoriteNews(news));
                } else {
                    viewModel.deleteFavoriteNews(favoriteNews);
                }
                newsAdapter.notifyItemChanged(position);
            }
        });
    }
    protected void onResume() {
        if ((currentTheme != R.style.DarkTheme && mySharedPref.loadNightModeState()) || (currentTheme == R.style.DarkTheme && !mySharedPref.loadNightModeState())) {
            recreate();
        }
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_favorite);
        overridePendingTransition(0, 0);
    }
    private int getColumnCount() {
        int orientation = getResources().getConfiguration().orientation;
        int columnNumber;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            columnNumber = 2;
        } else {
            // In portrait
            columnNumber = 1;
        }
        return columnNumber;
    }
}