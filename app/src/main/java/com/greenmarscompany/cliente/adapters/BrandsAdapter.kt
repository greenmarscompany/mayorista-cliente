package com.greenmarscompany.cliente.adapters;

import android.content.Context
import android.view.LayoutInflater;
import android.view.View
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.greenmarscompany.cliente.R;
import com.greenmarscompany.cliente.pojo.Brands;

data class BrandsAdapter(val context: Context, var brands: ArrayList<Brands>) : RecyclerView.Adapter<BrandsAdapter.ViewHolder>(), View.OnClickListener {

    lateinit var onClickListener: View.OnClickListener
    lateinit var listener: View.OnClickListener

    override fun onClick(v: View?) {
        listener.onClick(v)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.custom_brands, parent, false)
        view.setOnClickListener(this)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(brands[position])

    override fun getItemCount(): Int = brands.size

    fun setOnCliclListener(onClickListener: View.OnClickListener) {
        this.listener = onClickListener
    }


    // public static class ViewHolder extends RecyclerView.ViewHolder
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val brandsTitle: TextView = itemView.findViewById(R.id.BrandsTitle)
        private val brandsImage: ImageView = itemView.findViewById(R.id.BrandsImage)

        fun bind(brands: Brands) {
            brandsTitle.text = brands.name;
            Glide.with(context)
                    .load(brands.url)
                    .transform(FitCenter(), RoundedCorners(16))
                    .into(brandsImage);
        }
    }
}
