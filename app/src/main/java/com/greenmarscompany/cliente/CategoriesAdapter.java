package com.greenmarscompany.cliente;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.greenmarscompany.cliente.pojo.Categories;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.viewHolder>
        implements android.view.View.OnClickListener {

    java.util.List<Categories> categories;

    private android.view.View.OnClickListener listener;

    public CategoriesAdapter(java.util.List<Categories> categories) {
        this.categories = categories;
    }

    @Override
    public void onClick(android.view.View v) {
        if (listener != null) {
            listener.onClick(v);
        }

    }

    @androidx.annotation.NonNull
    @Override
    public viewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_categories, parent, false);
        view.setOnClickListener(this);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull viewHolder holder, int position) {
        //holder.categoriestitle.setText(categories.get(position).getName());
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setOnClickListener(android.view.View.OnClickListener listener) {
        this.listener = listener;
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        android.widget.TextView categoriestitle;
        ImageView categoriesImage;

        public viewHolder(@androidx.annotation.NonNull android.view.View itemView) {
            super(itemView);

            categoriestitle = itemView.findViewById(R.id.CategoriesName);
            categoriesImage = itemView.findViewById(R.id.CategoriesImage);
        }

        void bind(final Categories categories) {
            categoriestitle.setText(categories.getName());
            Glide.with(itemView.getContext())
                    .load(categories.getUrl())
                    .transform(new FitCenter(), new RoundedCorners(16))
                    .into(categoriesImage);
        }
    }
}
