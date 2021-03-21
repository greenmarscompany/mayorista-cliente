package com.greenmarscompany.cliente;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.greenmarscompany.cliente.persistence.AppDatabase;
import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.dao.CartDao;
import com.greenmarscompany.cliente.persistence.entity.ECart;
import com.greenmarscompany.cliente.pojo.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.viewHolder> implements
        android.view.View.OnClickListener {

    ArrayList<Product> products;
    private final Activity activity;
    private Context context;
    private View.OnClickListener listener;
    private boolean clickAdd = false;

    public ProductAdapter(Activity activity, ArrayList<Product> products) {
        this.products = products;
        this.activity = activity;
    }

    @Override
    public void onClick(android.view.View v) {
        if (listener != null) {
            listener.onClick(v);
        }
    }

    @androidx.annotation.NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_products, parent, false);
        view.setOnClickListener(this);
        context = parent.getContext();
        View view_badge = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_app_bar_main, parent, false);

        FloatingActionButton fla = view_badge.findViewById(R.id.fad_cart_order);
        fla.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.YELLOW));


        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }


    class viewHolder extends RecyclerView.ViewHolder {
        android.widget.TextView productTilte, productDescription, add_badge;
        ImageView productImage;
        Button productButtonAdd;
        EditText productCantidad;
        View view;

        viewHolder(@NonNull View itemView) {
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

            CartDao cartDao = DatabaseClient.getInstance(context).getAppDatabase().getCartDao();
            if (existeCart(product.getId()) != null) {
                String COLOR_ACTIVADO = "#1E82D9";
                productButtonAdd.getBackground().setColorFilter(Color.parseColor(COLOR_ACTIVADO), PorterDuff.Mode.SRC_ATOP);
                productButtonAdd.setText("Agregado");
                String COLOR_ACTIVADO_TEXTO = "#FFFFFF";
                productButtonAdd.setTextColor(Color.parseColor(COLOR_ACTIVADO_TEXTO));
            } else {
                String COLOR_DESACTIVADO = "#D5E5FF";
                productButtonAdd.getBackground().setColorFilter(Color.parseColor(COLOR_DESACTIVADO), PorterDuff.Mode.SRC_ATOP);
                productButtonAdd.setText("Agregar");
                String COLOR_DESACTIVADO_TEXTO = "#1469D9";
                productButtonAdd.setTextColor(Color.parseColor(COLOR_DESACTIVADO_TEXTO));
            }

            productButtonAdd.setOnClickListener(v -> {
                ECart itemCart = existeCart(product.getId());
                Log.d(Global.TAG, "bind: " + itemCart);
                if (itemCart == null) {
                    if (productCantidad.getText().length() > 0) {
                        ECart eCart = new ECart();
                        eCart.setName(product.getName());
                        eCart.setPrice(0);
                        eCart.setCantidad(Integer.parseInt(productCantidad.getText().toString()));
                        eCart.setTotal(0);
                        eCart.setProductRegister(product.getId());
                        cartDao.addCart(eCart);

                        notifyItemChanged(getAdapterPosition());
                        ((Activity) context).invalidateOptionsMenu();
                    } else {
                        Toast.makeText(context, "Ingrese una cantidad mayor a 0", Toast.LENGTH_LONG).show();
                    }
                } else {
                    cartDao.deleteCart(itemCart);

                    notifyItemChanged(getAdapterPosition());
                    ((Activity) context).invalidateOptionsMenu();
                }
            });
        }
    }

    private ECart existeCart(int id) {
        ECart eCart = null;
        for (ECart item : DatabaseClient.getInstance(context)
                .getAppDatabase()
                .getCartDao()
                .getCarts()) {
            if (item.getProductRegister() == id) {
                eCart = item;
                break;
            }
        }
        return eCart;
    }

}
