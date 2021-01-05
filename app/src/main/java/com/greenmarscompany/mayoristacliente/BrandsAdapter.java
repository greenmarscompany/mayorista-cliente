package com.greenmarscompany.mayoristacliente;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.greenmarscompany.mayoristacliente.pojo.Brands;

public class BrandsAdapter extends RecyclerView.Adapter<BrandsAdapter.viewHolder> implements android.view.View.OnClickListener {

    java.util.List<Brands> brands;

    private android.view.View.OnClickListener listener;

    public BrandsAdapter(java.util.List<Brands> brands) {
        this.brands = brands;
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

        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_brands, parent, false);
        view.setOnClickListener(this);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull viewHolder holder, int position) {
        holder.bind(brands.get(position));

    }

    @Override
    public int getItemCount() {
        return brands.size();
    }

    public void setOnClickListener(android.view.View.OnClickListener onClickListener) {

        this.listener = onClickListener;

    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        android.widget.TextView brandsTitle;
        ImageView brandsImage;

        public viewHolder(@androidx.annotation.NonNull android.view.View itemView) {
            super(itemView);
            brandsTitle = itemView.findViewById(R.id.BrandsTitle);
            brandsImage = itemView.findViewById(R.id.BrandsImage);
        }

        void bind(final Brands brands) {
            brandsTitle.setText(brands.getName());

            Glide.with(itemView.getContext())
                    .load(brands.getUrl())
                    .transform(new FitCenter(), new RoundedCorners(16))
                    .into(brandsImage);
        }
    }
}
