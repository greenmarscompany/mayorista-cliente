package com.greenmarscompany.mayoristacliente;

import android.content.Context;
import android.content.res.ColorStateList;
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
import com.greenmarscompany.mayoristacliente.persistence.DatabaseClient;
import com.greenmarscompany.mayoristacliente.persistence.entity.ECart;
import com.greenmarscompany.mayoristacliente.pojo.ProductStaff;
import com.greenmarscompany.mayoristacliente.utils.CartChangeColor;

import java.util.ArrayList;

public class ProductDetailAdapter extends RecyclerView.Adapter<ProductDetailAdapter.viewHolder> implements android.view.View.OnClickListener {


    java.util.List<ProductStaff> products;
    private Context context;
    private android.view.View.OnClickListener listener;

    private android.widget.TextView badge_count;
    private android.view.View view_badge;

    public ProductDetailAdapter(ArrayList<ProductStaff> products) {
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

        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_products_detail, parent, false);
        view.setOnClickListener(this);
        context = parent.getContext();


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
        android.widget.TextView productTilte_detail;
        ImageView productImage;
        Button productButtonAdd;
        EditText productCantidad;
        android.widget.TextView txtDistribuidor;
        android.widget.TextView txtPrecio;

        viewHolder(@androidx.annotation.NonNull android.view.View itemView) {
            super(itemView);
            productTilte_detail = itemView.findViewById(R.id.Name_product_detail);
            productImage = itemView.findViewById(R.id.ProductImage);
            productCantidad = itemView.findViewById(R.id.ProductCantidad);
            txtDistribuidor = itemView.findViewById(R.id.txtDistribuidor);
            txtPrecio = itemView.findViewById(R.id.txtPrecio);
            productButtonAdd = itemView.findViewById(R.id.productButtonAdd);
        }

        void bind(final ProductStaff product) {

            productTilte_detail.setText(product.getProductID().getDescription());

            Glide.with(itemView.getContext())
                    .load(product.getProductID().getImage())
                    .transform(new FitCenter(), new RoundedCorners(16))
                    .into(productImage);

            productCantidad.setText("1");
            txtPrecio.setText(String.valueOf(product.getPrice()));
            txtDistribuidor.setText(product.getCompanyID().getName());
            boolean IsExistsButton = false;
            for (ECart item : DatabaseClient.getInstance(context)
                    .getAppDatabase()
                    .getCartDao()
                    .getCarts()) {
                if (item.getProductRegister() == product.getID())
                    IsExistsButton = true;
                break;
            }
            if (IsExistsButton) {
                productButtonAdd.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.parseColor("#64dd17")));
                productButtonAdd.setText("Agregado");
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
                        if (item.getProductRegister() == product.getID())
                            oECart = item;
                        IsAdd = !item.getName().toLowerCase().contains("gas") && !item.getName().toLowerCase().contains("cisterna");
                    }
                    String frii_Background = String.format("#%06x", ContextCompat.getColor(context, R.color.frii_Background) & 0xffffff);
                    if (oECart == null) {
                        if (productCantidad.getText().length() > 0) {
                            ECart eCart = new ECart();
                            eCart.setName(product.getProductID().getDescription());
                            eCart.setPrice(product.getPrice());
                            eCart.setCantidad(Integer.parseInt(productCantidad.getText().toString()));
                            eCart.setTotal((Float.parseFloat(productCantidad.getText().toString()) * product.getPrice()));
                            eCart.setProductRegister(product.getID());
                            DatabaseClient.getInstance(context)
                                    .getAppDatabase()
                                    .getCartDao()
                                    .addCart(eCart);
                            productButtonAdd.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.parseColor("#64dd17")));
                            productButtonAdd.setText("Agregado");
                        } else {
                            Toast.makeText(context, "Ingrese una cantidad mayor a 0", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        DatabaseClient.getInstance(context)
                                .getAppDatabase()
                                .getCartDao()
                                .deleteCart(oECart);
                        productButtonAdd.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.parseColor(frii_Background)));
                        productButtonAdd.setText("Agregar");
                        Toast.makeText(context, "Eliminado del Carrito", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }


}
