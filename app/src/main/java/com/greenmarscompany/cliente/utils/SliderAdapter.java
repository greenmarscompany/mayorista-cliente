package com.greenmarscompany.cliente.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.greenmarscompany.cliente.R;
import com.greenmarscompany.cliente.pojo.Mensaje;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> {

    private final Context context;
    private java.util.List<Mensaje> sliderItems = new ArrayList<>();

    public SliderAdapter(Context context) {
        this.context = context;
    }

    public void renewItems(java.util.List<Mensaje> sliderItems) {
        this.sliderItems = sliderItems;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        this.sliderItems.remove(position);
        notifyDataSetChanged();
    }

    public void addItem(Mensaje sliderItem) {
        this.sliderItems.add(sliderItem);
        notifyDataSetChanged();
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        android.view.View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(final SliderAdapterVH viewHolder, final int position) {
        Mensaje sItem = sliderItems.get(position);
        viewHolder.textDescripcion.setText(sItem.getNombre());
        viewHolder.textDescripcion.setTextSize(16);
        viewHolder.textDescripcion.setTextColor(android.graphics.Color.WHITE);
        Glide.with(viewHolder.itemview)
                .load(sItem.getImage())
                .fitCenter()
                .into(viewHolder.imageViewBackground);
    }

    @Override
    public int getCount() {
        return sliderItems.size();
    }

    static class SliderAdapterVH extends SliderViewAdapter.ViewHolder {
        android.view.View itemview;
        ImageView imageViewBackground;
        ImageView imageGirfContainer;
        android.widget.TextView textDescripcion;

        public SliderAdapterVH(android.view.View itemview) {
            super(itemview);
            this.itemview = itemview;
            imageViewBackground = itemview.findViewById(R.id.iv_auto_image_slider);
            imageGirfContainer = itemview.findViewById(R.id.iv_gif_container);
            textDescripcion = itemview.findViewById(R.id.tv_auto_image_slider);
        }
    }
}
