package com.example.globalnews.Adapters;

import android.graphics.Color;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.globalnews.MainActivity;
import com.example.globalnews.R;
import com.example.globalnews.db.News;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchHolder>  {

    public List<News> news;
    private List<News> newsAll;
    private String q;
    private NewsAdapter.ClickOnCardListener clickOnCardListener;
    private NewsAdapter.onReachEndListener onReachEndListener;
    private NewsAdapter.ClickOnShareListener clickOnShareListener;
    private NewsAdapter.ClickOnAddToFavoriteListener clickOnAddToFavoriteListener;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public SearchAdapter() {
        news = new ArrayList<News>();
    }

    public void setQ(String q) {
        this.q = q;
    }

    public List<News> getNews() {
        return news;
    }

    public interface ClickOnCardListener{
        void ClickOnCard(int position); //to click the poster image
    }

    public void setClickOnCardListener(NewsAdapter.ClickOnCardListener clickOnCardListener) {
        this.clickOnCardListener = clickOnCardListener;
    }
    public interface ClickOnAddToFavoriteListener{
        void ClickOnAddToFavorite(int position); //to click the poster image
    }

    public void setClickOnAddToFavoriteListener(NewsAdapter.ClickOnAddToFavoriteListener clickOnAddToFavoriteListener) {
        this.clickOnAddToFavoriteListener = clickOnAddToFavoriteListener;
    }

    public interface ClickOnShareListener{
        void ClickOnShare(int position); //to click the poster image
    }

    public void setClickOnShareListener(NewsAdapter.ClickOnShareListener clickOnShareListener) {
        this.clickOnShareListener = clickOnShareListener;
    }

    public interface onReachEndListener{
        void onReachEnd(); // method call when we come end of page
    }

    public void setOnReachEndListener(NewsAdapter.onReachEndListener onReachEndListener) {
        this.onReachEndListener = onReachEndListener;
    }


    @NonNull
    @Override
    public SearchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sources_item, parent, false);
        return new SearchAdapter.SearchHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHolder holder, int position) {
        if (news.size() >= 20 && position > news.size() - 2 && onReachEndListener != null){///when we come end of page and onReachEndListener not null
            onReachEndListener.onReachEnd();
        }
        News news1 = news.get(position);
        String title = news1.getTitle();
        if (title.toLowerCase().contains(q.toLowerCase()) && q != null) {
            int start = title.toLowerCase().indexOf(q.toLowerCase());
            int end = start + q.length();
            Spannable span  = new SpannableString(title);
            span.setSpan(new BackgroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.textViewTitle.setText(span);
        } else {
            holder.textViewTitle.setText(title);
        }
        holder.textViewSourceName.setText(news1.getSourceName());
        ImageView imageViewFavorite = holder.imageViewAddToFavorite;
        if(MainActivity.isFavorite(news1.getTitle())){
            imageViewFavorite.setImageResource(R.drawable.favorite_remove);
        }
        else{
            imageViewFavorite.setImageResource(R.drawable.favorite_add);
        }
        TextView textViewPublishedAt = holder.textViewPublishedAt;
        String data = news1.getPublishedAt().replace("-", "/").replace("T", " ").replace("Z", "");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        long timeInMillis = date.getTime();
        textViewPublishedAt.setText(getTimeAgo(timeInMillis));
    }

    @Override
    public int getItemCount() {
        return news.size();
    }

    public class SearchHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewSourceName;
        private ImageView imageViewAddToFavorite;
        private ImageView imageViewShare;
        private TextView textViewPublishedAt;

        public SearchHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitleOfSource);
            textViewSourceName = itemView.findViewById(R.id.textViewSourceNameOfSearch);
            imageViewShare = itemView.findViewById(R.id.imageViewShare);
            imageViewAddToFavorite = itemView.findViewById(R.id.imageViewFavorite);
            textViewPublishedAt = itemView.findViewById(R.id.textViewPublishedAt);

            itemView.setOnClickListener(new View.OnClickListener() { // when the click image
                @Override
                public void onClick(View v) {
                    if(clickOnCardListener != null){
                        clickOnCardListener.ClickOnCard(getAdapterPosition());
                    }
                }
            });
            imageViewShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickOnShareListener != null){
                        clickOnShareListener.ClickOnShare(getAdapterPosition());
                    }
                }
            });
            imageViewAddToFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickOnAddToFavoriteListener != null){
                        clickOnAddToFavoriteListener.ClickOnAddToFavorite(getAdapterPosition());
                    }
                }
            });
        }
    }
    public void addNews(List<News> news){
        this.news.addAll(news);
        notifyDataSetChanged();
    }
    public void setNews(List<News> news) {
        this.news = news;
        this.newsAll = new ArrayList<>(this.news);
        notifyDataSetChanged();
    }
    public void clear(){
        this.news.clear();
        notifyDataSetChanged();
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

}
