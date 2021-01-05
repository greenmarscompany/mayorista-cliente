package com.greenmarscompany.mayoristacliente;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.greenmarscompany.mayoristacliente.pojo.Product;

import java.util.ArrayList;

public class ProductAdapterNew extends RecyclerView.Adapter<ProductAdapterNew.viewHolder> implements android.view.View.OnClickListener {


    java.util.List<Product> products;
    private android.view.View.OnClickListener listener;

    public ProductAdapterNew(ArrayList<Product> products) {
        this.products = products;
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

        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_products_new, parent, false);
        view.setOnClickListener(this);
        Context context = parent.getContext();

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull viewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setOnClickListener(android.view.View.OnClickListener listener) {
        this.listener = listener;
    }




    static class viewHolder extends RecyclerView.ViewHolder {
        android.widget.TextView Name_product;
        ImageView productImage;
        Button Ver_Distribuidores;

        viewHolder(@androidx.annotation.NonNull android.view.View itemView) {
            super(itemView);
            Name_product = itemView.findViewById(R.id.Name_product_detail);
            Ver_Distribuidores=itemView.findViewById(R.id.Ver_Distribuidores);
            productImage=itemView.findViewById(R.id.ProductImage);

        }

        void bind(final Product product) {

            Name_product.setText(product.getName());

            Glide.with(itemView.getContext())
                    .load(product.getUrl())
                    .transform(new FitCenter(), new RoundedCorners(16))
                    .into(productImage);

            Ver_Distribuidores.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    AppCompatActivity activity=(InicioMapsActivity) v.getContext();
                    FragmentManager manager=activity.getSupportFragmentManager();
                    FragmentTransaction transaction=manager.beginTransaction();
                    ProductDetailFragment oProductDetailFragment=new ProductDetailFragment();
                    oProductDetailFragment.product_id=product.getId();
                    transaction.replace(R.id.mainContainer,oProductDetailFragment );
                    transaction.commit();
                    transaction.addToBackStack(null);
                }
            });
        }


    }


}
