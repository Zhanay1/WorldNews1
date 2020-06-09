package com.example.globalnews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.globalnews.Adapters.CategoryAdapter;
import com.example.globalnews.Adapters.NewsAdapter;
import com.example.globalnews.Adapters.SearchAdapter;
import com.example.globalnews.db.FavoriteNews;
import com.example.globalnews.db.News;
import com.example.globalnews.db.NewsViewModel;
import com.example.globalnews.utils.JSONUtils;
import com.example.globalnews.utils.NetworkUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.json.JSONObject;

import java.net.URL;
import java.util.List;

import static android.app.PendingIntent.getActivity;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {
    BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerViewSource;
    private SearchAdapter searchAdapter;
    private LoaderManager loaderManager;
    private EditText editText;
    private static String q;
    private static boolean isLoading = false;
    private static final int LOADER_ID = 2;
    private NewsViewModel newsViewModel;
    private static int page = 1;
    private Switch switchDark;
    SharedPref mySharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mySharedPref = new SharedPref(this);
        if(mySharedPref.loadNightModeState() == true){
            setTheme(R.style.DarkTheme);
        }else{
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
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
        editText = findViewById(R.id.editTextSearch);
        loaderManager = LoaderManager.getInstance(this);
        recyclerViewSource = findViewById(R.id.recycleViewSources);
        searchAdapter = new SearchAdapter();
        recyclerViewSource.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        recyclerViewSource.setAdapter(searchAdapter);
        newsViewModel = ViewModelProviders.of(this).get(NewsViewModel.class);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_search);
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
                        Intent intentFavorite = new Intent(getApplicationContext(), FavoriteActivity.class);
                        overridePendingTransition(0, 0);
                        intentFavorite.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intentFavorite);
                        return true;
                    case R.id.nav_search:
                        return true;
                }
                return false;
            }
        });
        KeyboardVisibilityEvent.setEventListener(
                SearchActivity.this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        // some code depending on keyboard visiblity status
                        if(isOpen){
                            bottomNavigationView.setVisibility(View.GONE);
                        }
                        else{
                            bottomNavigationView.setVisibility(View.VISIBLE);
                        }
                    }
                });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isLoading) {
                    page = 1;
                    q = s.toString();
                    searchAdapter.setQ(q);
                    DownloadData();
                    recyclerViewSource.smoothScrollToPosition(0);
                }
            }
        });

        searchAdapter.setClickOnCardListener(new NewsAdapter.ClickOnCardListener() {
            @Override
            public void ClickOnCard(int position) {
                News news = searchAdapter.news.get(position);
                Intent intent = new Intent(SearchActivity.this, DetailNewsActivity.class);
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

        searchAdapter.setClickOnShareListener(new NewsAdapter.ClickOnShareListener() {
            @Override
            public void ClickOnShare(int position) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String shareBody = searchAdapter.news.get(position).getUrl();
                intent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
                intent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(intent, "Share using"));
            }
        });

        searchAdapter.setClickOnAddToFavoriteListener(new NewsAdapter.ClickOnAddToFavoriteListener() {
            @Override
            public void ClickOnAddToFavorite(int position) {
                News news = searchAdapter.getNews().get(position);
                FavoriteNews favoriteNews = newsViewModel.getFavoriteNewsByTitle(news.getTitle());
                if (favoriteNews == null) {
                    newsViewModel.insertFavoriteNews(new FavoriteNews(news.getSourceName(),
                            news.getAuthor(),
                            news.getTitle(),
                            news.getDescription(),
                            news.getUrl(), news.getUrlToImage(), news.getPublishedAt(), news.getContent()));
                } else {
                    newsViewModel.deleteFavoriteNews(favoriteNews);
                }
                searchAdapter.notifyItemChanged(position);
            }
        });
        searchAdapter.setOnReachEndListener(new NewsAdapter.onReachEndListener() {
            @Override
            public void onReachEnd() {
                if (!isLoading) {
                    DownloadData();
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_search);
        overridePendingTransition(0, 0);
        searchAdapter.notifyDataSetChanged();
    }
    private void DownloadData(){
        if(checkInternet()) {
            URL url = NetworkUtils.buildSearchURl(q, page);
            Bundle bundle = new Bundle();
            bundle.putString("url", url.toString());
            loaderManager.restartLoader(LOADER_ID, bundle, this);
        }
        else{
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
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

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle args) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, args);
        jsonLoader.setOnStartLoadingListener(new NetworkUtils.JSONLoader.OnStartLoadingListener() {
            @Override
            public void OnStartLoading() {
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
                searchAdapter.setNews(news);
            }else{
                searchAdapter.addNews(news);
            }
            page++;
        }
        isLoading = false;
        loaderManager.destroyLoader(LOADER_ID);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

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
    private void restartActivity(){
        Intent i = new Intent(getApplicationContext(), SearchActivity.class);
        startActivity(i);
        finish();
    }
}
