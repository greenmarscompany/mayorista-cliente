package com.greenmarscompany.cliente;

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
import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.entity.ECart;
import com.greenmarscompany.cliente.pojo.ProductStaff;

import java.util.ArrayList;

public class GasProductDetailAdapter extends RecyclerView.Adapter<GasProductDetailAdapter.viewHolder> implements android.view.View.OnClickListener {


    // TODO ELIMINAR NO ESTA EN USO
    private final java.util.List<ProductStaff> products;
    private Context context;


    public GasProductDetailAdapter(ArrayList<ProductStaff> products) {
        this.products = products;
    }

    @Override
    public void onClick(android.view.View v) {

    }

    @androidx.annotation.NonNull
    @Override
    public viewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {

        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_gas_product_detail, parent, false);
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

    class viewHolder extends RecyclerView.ViewHolder {

        android.widget.TextView gas_name_detail;
        ImageView productImage;
        Button productButtonAdd;
        EditText ProductGasCantidad;
        android.widget.TextView txtDistribuidor;
        android.widget.TextView txtPrecio;

        viewHolder(@androidx.annotation.NonNull final android.view.View itemView) {
            super(itemView);


            gas_name_detail = itemView.findViewById(R.id.GasDetailName);
            productImage = itemView.findViewById(R.id.ProductGasImage);
            ProductGasCantidad = itemView.findViewById(R.id.ProductGasCantidad);
            txtDistribuidor = itemView.findViewById(R.id.txtDistribuidor);
            txtPrecio = itemView.findViewById(R.id.txtPrecio);
            productButtonAdd = itemView.findViewById(R.id.productButtonAdd);


        }

        void bind(final ProductStaff product) {
            gas_name_detail.setText(product.getProductID().getDescription());

            Glide.with(itemView.getContext())
                    .load(product.getProductID().getImage())
                    .transform(new FitCenter(), new RoundedCorners(16))
                    .into(productImage);

            ProductGasCantidad.setText("1");
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
                        if (!item.getName().toLowerCase().contains("gas") || item.getName().toLowerCase().contains("cisterna"))
                            IsAdd = false;
                        else
                            IsAdd = true;
                    }
                    if (DatabaseClient.getInstance(context)
                            .getAppDatabase()
                            .getCartDao()
                            .getCarts().size() == 0)
                        IsAdd = true;
                    String frii_Background = String.format("#%06x", ContextCompat.getColor(context, R.color.frii_Background) & 0xffffff);
                    if (oECart == null) {
                        if (ProductGasCantidad.getText().length() > 0) {
                            ECart eCart = new ECart();
                            eCart.setName(product.getProductID().getDescription());
                            eCart.setPrice(product.getPrice());
                            eCart.setProductRegister(product.getID());
                            eCart.setCantidad(Integer.parseInt(ProductGasCantidad.getText().toString()));
                            eCart.setTotal((Float.parseFloat(ProductGasCantidad.getText().toString()) * product.getPrice()));
                            eCart.setProductRegister(product.getID());
                            DatabaseClient.getInstance(context)
                                    .getAppDatabase()
                                    .getCartDao()
                                    .addCart(eCart);
                            productButtonAdd.setBackgroundTintList(ColorStateList.valueOf(android.graphics.Color.parseColor("#64dd17")));
                            productButtonAdd.setText("Agregado");
                        } else {
                            Toast.makeText(context, "Ingrese una cantidad mayor a 0", Toast.LENGTH_LONG).show();
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
