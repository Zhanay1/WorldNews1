package com.example.globalnews.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import com.example.globalnews.R;

import java.util.ArrayList;
import java.util.List;
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryHolder> {
    private List<String> categories;
    private int selectedCategoryPosition;
    private ClickOnCategoryListener clickOnCategoryListener;
    public CategoryAdapter() {
        this.categories = new ArrayList<>();
        categories.add("General");
        categories.add("Entertainment");
        categories.add("Business");
        categories.add("Health");
        categories.add("Science");
        categories.add("Sports");
        categories.add("Technology");
        selectedCategoryPosition = 0;
    }
    public void setSelectedCategoryPosition(int selectedCategoryPosition) {
        this.selectedCategoryPosition = selectedCategoryPosition;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public interface ClickOnCategoryListener{
        void ClickOnCategory(int position); //to click the poster image
    }

    public void setClickOnCategoryListener(ClickOnCategoryListener clickOnCategoryListener) {
        this.clickOnCategoryListener = clickOnCategoryListener;
    }



    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new CategoryAdapter.CategoryHolder(view);
    }
    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull CategoryHolder holder, int position) {
        holder.textViewCategory.setText(categories.get(position));
        if (position == selectedCategoryPosition) {
            holder.textViewCategory.setBackgroundResource(R.drawable.category_selected);
        }
        else{
            holder.textViewCategory.setBackgroundResource(R.drawable.card_radius);
        }

    }
    @Override
    public int getItemCount() {
        return categories.size();
    }
    public class CategoryHolder extends RecyclerView.ViewHolder {
        private TextView textViewCategory;
        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickOnCategoryListener != null){
                        clickOnCategoryListener.ClickOnCategory(getAdapterPosition());
                    }
                }
            });
        }
    }
    public String getCategoryById(int position){
        return categories.get(position);
    }
}
