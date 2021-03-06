package com.greenmarscompany.cliente;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.entity.ECart;
import com.greenmarscompany.cliente.pojo.Product;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.viewHolder> implements
        android.view.View.OnClickListener {

    private final String COLOR_ACTIVADO = "#1E82D9";
    private final String COLOR_DESACTIVADO = "#D5E5FF";
    private final String COLOR_ACTIVADO_TEXTO = "#FFFFFF";
    private final String COLOR_DESACTIVADO_TEXTO = "#1469D9";

    ArrayList<Product> products;
    private Context context;
    private android.view.View.OnClickListener listener;

    private android.widget.TextView badge_count;

    public ProductAdapter(ArrayList<Product> products) {
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

        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_products, parent, false);
        view.setOnClickListener(this);
        context = parent.getContext();


        android.view.View view_badge = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_app_bar_main, parent, false);

        com.google.android.material.floatingactionbutton.FloatingActionButton fla = view_badge.findViewById(R.id.fad_cart_order);
        fla.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.YELLOW));


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


    class viewHolder extends RecyclerView.ViewHolder {
        android.widget.TextView productTilte, productDescription, add_badge;
        ImageView productImage;
        Button productButtonAdd;
        EditText productCantidad;
        android.view.View view;

        viewHolder(@androidx.annotation.NonNull android.view.View itemView) {
            super(itemView);
            productTilte = itemView.findViewById(R.id.Name_product_detail);
            productImage = itemView.findViewById(R.id.ProductImage);
            productButtonAdd = itemView.findViewById(R.id.Ver_Distribuidores);
            productCantidad = itemView.findViewById(R.id.ProductCantidad);
            //productDescription = itemView.findViewById(R.id.ProductDescription);


        }

        void bind(final Product product) {
            productCantidad.setText("1");
            productTilte.setText(product.getName());
            //productDescription.setText(product.getDescription());

            Glide.with(itemView.getContext())
                    .load(product.getUrl())
                    .transform(new FitCenter(), new RoundedCorners(16))
                    .into(productImage);


            boolean IsExistsButton = false;
            for (ECart item : DatabaseClient.getInstance(context)
                    .getAppDatabase()
                    .getCartDao()
                    .getCarts()) {
                if (item.getProductRegister() == product.getId()) {
                    IsExistsButton = true;
                    break;
                }
            }
            if (IsExistsButton) {
                productButtonAdd.getBackground().setColorFilter(Color.parseColor(COLOR_ACTIVADO), PorterDuff.Mode.SRC_ATOP);
                productButtonAdd.setText("Agregado");
                productButtonAdd.setTextColor(Color.parseColor(COLOR_ACTIVADO_TEXTO));
            }
            productButtonAdd.setOnClickListener(new android.view.View.OnClickListener() {

                @Override
                public void onClick(android.view.View v) {

                    ECart oECart = null;
                    boolean IsAdd = false;
                    for (ECart item : DatabaseClient.getInstance(context)
                            .getAppDatabase()
                            .getCartDao()
                            .getCarts()) {
                        if (item.getProductRegister() == product.getId())
                            oECart = item;
                        if (product.getName().toLowerCase().contains("cisterna")) {
                            IsAdd = item.getName().toLowerCase().contains("cisterna");
                        } else {


                            if (item.getName().toLowerCase().contains("agua") && item.getName().toLowerCase().contains("gas"))
                                IsAdd = true;
                            else if (item.getName().toLowerCase().contains("gas") || item.getName().toLowerCase().contains("cisterna"))
                                IsAdd = false;
                            else
                                IsAdd = true;


                        }
                    }
                    if (DatabaseClient.getInstance(context)
                            .getAppDatabase()
                            .getCartDao()
                            .getCarts().size() == 0)
                        IsAdd = true;
                    String frii_Background = String.format("#%06x", ContextCompat.getColor(context, R.color.frii_Background) & 0xffffff);
                    if (oECart == null) {
                        if (productCantidad.getText().length() > 0) {
                            ECart eCart = new ECart();
                            eCart.setName(product.getName());
                            eCart.setPrice(0);
                            eCart.setCantidad(Integer.parseInt(productCantidad.getText().toString()));
                            eCart.setTotal(0);
                            eCart.setProductRegister(product.getId());
                            DatabaseClient.getInstance(context)
                                    .getAppDatabase()
                                    .getCartDao()
                                    .addCart(eCart);

                            productButtonAdd.getBackground().setColorFilter(Color.parseColor(COLOR_ACTIVADO), PorterDuff.Mode.SRC_ATOP);
                            productButtonAdd.setText("Agregado");
                            productButtonAdd.setTextColor(Color.parseColor(COLOR_ACTIVADO_TEXTO));
                            ((Activity) context).invalidateOptionsMenu();
                        } else {
                            Toast.makeText(context, "Ingrese una cantidad mayor a 0", Toast.LENGTH_LONG).show();
                        }

                    } else {
                        DatabaseClient.getInstance(context)
                                .getAppDatabase()
                                .getCartDao()
                                .deleteCart(oECart);

                        productButtonAdd.getBackground().setColorFilter(Color.parseColor(COLOR_DESACTIVADO), PorterDuff.Mode.SRC_ATOP);
                        productButtonAdd.setText("Agregar");
                        productButtonAdd.setTextColor(Color.parseColor(COLOR_DESACTIVADO_TEXTO));
                    }
                }
            });
        }
    }
}
