package com.example.globalnews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.example.globalnews.Adapters.CategoryAdapter;
import com.example.globalnews.Adapters.NewsAdapter;
import com.example.globalnews.db.FavoriteNews;
import com.example.globalnews.db.News;
import com.example.globalnews.SharedPref;
import com.example.globalnews.db.NewsViewModel;
import com.example.globalnews.utils.JSONUtils;
import com.example.globalnews.utils.NetworkUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject>  {

    private RecyclerView recyclerViewNews;
    private NewsAdapter newsAdapter;
    private CategoryAdapter categoryAdapter;
    private BottomNavigationView bottomNavigationView;
    private static NewsViewModel newsViewModel;
    private ProgressBar progressBarLoading;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FavoriteNews favoriteNews;
    private Toolbar toolbar;
    private RecyclerView recyclerViewFilter;

    private News news;
    private static final int LOADER_ID = 1;
    private LoaderManager loaderManager;
    private static boolean isLoading = false;
    private static int page = 1;
    private static String category = "general";
    static boolean scroll_down;
    private Switch switchDark;
    SharedPref mySharedPref ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mySharedPref = new SharedPref(this);
        if(mySharedPref.loadNightModeState() == true){
            setTheme(R.style.DarkTheme);
        }else{
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        switchDark = findViewById(R.id.switchDark);
        if(mySharedPref.loadNightModeState() == true){
            switchDark.setChecked(true);
        }
        switchDark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mySharedPref.setNightModeState(true);
                    restartActivity();
                }else{
                    mySharedPref.setNightModeState(false);
                    restartActivity();
                }
            }
        });
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        categoryAdapter = new CategoryAdapter();
        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        recyclerViewFilter = findViewById(R.id.filterNews);
        recyclerViewFilter.setAdapter(categoryAdapter);
        recyclerViewFilter.setLayoutManager(layoutManager);

        loaderManager = LoaderManager.getInstance(this);
        //download data
        swipeRefreshLayout = findViewById(R.id.swipeToRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                DownloadData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        recyclerViewNews = findViewById(R.id.recycleNews);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        newsAdapter = new NewsAdapter();
        recyclerViewNews.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        recyclerViewNews.setAdapter(newsAdapter);
        newsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);

        //bottom - navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_home:
                        recyclerViewNews.smoothScrollToPosition(0);
                        return true;
                    case R.id.nav_favorite:
                        Intent intentFavorite = new Intent(getApplicationContext(), FavoriteActivity.class);
                        overridePendingTransition(0, 0);
                        intentFavorite.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intentFavorite);
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

        categoryAdapter.setClickOnCategoryListener(new CategoryAdapter.ClickOnCategoryListener() {
            @Override
            public void ClickOnCategory(int position) {
                if(category != categoryAdapter.getCategoryById(position)){
                    categoryAdapter.setSelectedCategoryPosition(position);
                    category = categoryAdapter.getCategoryById(position);
                    page = 1;
                    DownloadData();
                    categoryAdapter.notifyDataSetChanged();
                    recyclerViewNews.smoothScrollToPosition(0);
                    recyclerViewFilter.smoothScrollToPosition(position);
                    if (position >= 3) {
                        recyclerViewFilter.smoothScrollToPosition(6);
                    } else {
                        recyclerViewFilter.smoothScrollToPosition(0);
                    }
                }
            }
        });

        newsAdapter.setClickOnCardListener(new NewsAdapter.ClickOnCardListener() {
            @Override
            public void ClickOnCard(int position) {
                News news = newsAdapter.news.get(position);
                Intent intent = new Intent(MainActivity.this, DetailNewsActivity.class);
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
        newsAdapter.setOnReachEndListener(new NewsAdapter.onReachEndListener() {
            @Override
            public void onReachEnd() {
                if (!isLoading) {
                    DownloadData();
                }
            }
        });
        DownloadData();
        //data base
        LiveData<List<News>> newsFromLiveData = newsViewModel.getNews();
        newsFromLiveData.observe(this, new Observer<List<News>>() {
            @Override
            public void onChanged(List<News> news2) {
                    newsAdapter.setNews(news2);
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
                startActivity(Intent.createChooser(intent, "Share using"));
            }
        });

        newsAdapter.setClickOnAddToFavoriteListener(new NewsAdapter.ClickOnAddToFavoriteListener() {
            @Override
            public void ClickOnAddToFavorite(int position) {
                News news = newsAdapter.getNews().get(position);
                FavoriteNews favoriteNews = newsViewModel.getFavoriteNewsByTitle(news.getTitle());
                if (favoriteNews == null) {
                    newsViewModel.insertFavoriteNews(new FavoriteNews(news));
                } else {
                    newsViewModel.deleteFavoriteNews(favoriteNews);
                }
                newsAdapter.notifyItemChanged(position);
            }
        });
    }


    protected void onResume() {
        super.onResume();
        bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        overridePendingTransition(0, 0);
        newsAdapter.notifyDataSetChanged();
    }
    private boolean checkInternet() {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else {
            connected = false;
        }
        return connected;
    }
    private void DownloadData(){
        if(checkInternet()) {
            URL url = NetworkUtils.buildURl(page, category);
            Bundle bundle = new Bundle();
            bundle.putString("url", url.toString());
            loaderManager.restartLoader(LOADER_ID, bundle, this);
        }
        else{
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
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

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, args);
        jsonLoader.setOnStartLoadingListener(new NetworkUtils.JSONLoader.OnStartLoadingListener() {
            @Override
            public void OnStartLoading() {
                progressBarLoading.setVisibility(View.VISIBLE);
                isLoading = true;
            }
        });
        return jsonLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject data) {
        List<News> news = JSONUtils.getNewsFromJSON(data);
        if(news != null && !news.isEmpty()) {
            if(page == 1) {
                newsViewModel.DeleteAllNews();
                newsAdapter.clear();
            }
            for (News news1 : news) {
                newsViewModel.insertNews(news1);
            }
            newsAdapter.addNews(news);
            page++;
        }
        isLoading = false;
        progressBarLoading.setVisibility(View.INVISIBLE);
        loaderManager.destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        // Detect SearchView icon clicks
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // SearchView is being shown
                    bottomNavigationView.setVisibility(View.GONE);
                    swipeRefreshLayout.setEnabled(false);
                } else {
                    // SearchView was dismissed
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setEnabled(true);
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                newsAdapter.getFilter().filter(query);
                newsAdapter.setQueryText(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newsAdapter.getFilter().filter(newText);
                newsAdapter.setQueryText(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    public static boolean isFavorite(String title){
        FavoriteNews favoriteNews = newsViewModel.getFavoriteNewsByTitle(title);
        return favoriteNews != null;
    }
    private void restartActivity(){
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        finish();
    }
}
