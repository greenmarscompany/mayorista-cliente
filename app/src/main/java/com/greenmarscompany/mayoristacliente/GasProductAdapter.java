package com.greenmarscompany.mayoristacliente;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.greenmarscompany.mayoristacliente.persistence.DatabaseClient;
import com.greenmarscompany.mayoristacliente.persistence.entity.ECart;
import com.greenmarscompany.mayoristacliente.pojo.Product;
import com.greenmarscompany.mayoristacliente.pojo.ProductGas;

import com.greenmarscompany.mayoristacliente.utils.CartChangeColor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class GasProductAdapter extends RecyclerView.Adapter<GasProductAdapter.viewHolder> implements android.view.View.OnClickListener {

    private final String COLOR_ACTIVADO = "#1E82D9";
    private final String COLOR_DESACTIVADO = "#D5E5FF";

    private final java.util.List<Product> products;
    private Context context;


    public GasProductAdapter(ArrayList<Product> products) {
        this.products = products;
    }

    @Override
    public void onClick(android.view.View v) {

    }

    @androidx.annotation.NonNull
    @Override
    public viewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {

        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_gas_product, parent, false);
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

    public void setChangeRadioButton(final Product product, final String peso, final ImageButton productGasAddCart) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = Global.URL_HOST + "/product/markes/" + product.getMarkeId();
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onResponse(String response) {

                        int ProductId = 0;
                        Gson gson = new Gson();
                        ArrayList<ProductGas> productDetails = gson.fromJson(response,
                                new TypeToken<ArrayList<ProductGas>>() {
                                }.getType());
                        for (ProductGas item : productDetails) {
                            if (item.getDetail_measurement_id().getName().split(" ")[1].equals(product.getType().split("-")[1]) &&
                                    item.getMeasurement() == Float.parseFloat(peso)) {
                                ProductId = item.getId();
                                break;
                            }
                        }
                        boolean IsExistsButton = false;
                        for (ECart item : DatabaseClient.getInstance(context)
                                .getAppDatabase()
                                .getCartDao()
                                .getCarts()) {
                            if (item.getProductRegister() == ProductId) {
                                IsExistsButton = true;
                                break;
                            }
                        }
                        String frii_Background = String.format("#%06x", ContextCompat.getColor(context, R.color.frii_Background) & 0xffffff);

                        if (IsExistsButton) {
                            productGasAddCart.getBackground().setColorFilter(Color.parseColor(COLOR_ACTIVADO), PorterDuff.Mode.SRC_ATOP);
                            productGasAddCart.setImageResource(R.drawable.ic_cart_card);
                        } else {
                            productGasAddCart.getBackground().setColorFilter(Color.parseColor(COLOR_DESACTIVADO), PorterDuff.Mode.SRC_ATOP);
                            productGasAddCart.setImageResource(R.drawable.ic_shopping_cart);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "That didn't work!", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }

    class viewHolder extends RecyclerView.ViewHolder {
        android.widget.TextView gasProductTitle;
        ImageView gasProductImage;
        EditText productGasCantidad;
        ImageButton productGasAddCart;
        RadioGroup radioGroup;
        RadioButton peso;

        viewHolder(@androidx.annotation.NonNull final android.view.View itemView) {
            super(itemView);
            gasProductTitle = itemView.findViewById(R.id.GasDetailName);
            gasProductImage = itemView.findViewById(R.id.ProductGasImage);
            productGasCantidad = itemView.findViewById(R.id.ProductGasCantidad);
            productGasAddCart = itemView.findViewById(R.id.Button_45_Kilos);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            peso = itemView.findViewById(R.id.gas10kl);
        }

        void bind(final Product product) {
            productGasCantidad.setText("1");
            gasProductTitle.setText(product.getName());

            Glide.with(itemView.getContext())
                    .load(product.getUrl())
                    .transform(new FitCenter(), new RoundedCorners(16))
                    .into(gasProductImage);

            RequestQueue queue = Volley.newRequestQueue(context);
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.gas5kl:
                            peso = itemView.findViewById(R.id.gas5kl);
                            setChangeRadioButton(product, peso.getText().toString(), productGasAddCart);
                            break;
                        case R.id.gas10kl:
                            peso = itemView.findViewById(R.id.gas10kl);
                            setChangeRadioButton(product, peso.getText().toString(), productGasAddCart);
                            break;
                        case R.id.gas15kl:
                            peso = itemView.findViewById(R.id.gas15kl);
                            setChangeRadioButton(product, peso.getText().toString(), productGasAddCart);
                            break;

                        case R.id.gas45kl:
                            peso = itemView.findViewById(R.id.gas45kl);
                            setChangeRadioButton(product, peso.getText().toString(), productGasAddCart);
                            break;
                    }
                }
            });
            StringBuilder sb = new StringBuilder();
            sb.append(Global.URL_HOST + "/product/markes/" + product.getMarkeId());
            String url = sb.toString();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            int ProductId = 0;
                            Gson gson = new Gson();
                            ArrayList<ProductGas> productDetails = gson.fromJson(response,
                                    new TypeToken<ArrayList<ProductGas>>() {
                                    }.getType());
                            for (ProductGas item : productDetails) {
                                if (item.getDetail_measurement_id().getName().split(" ")[1].equals(product.getType().split("-")[1]) &&
                                        item.getMeasurement() == Float.parseFloat(peso.getText().toString())) {
                                    ProductId = item.getId();
                                    break;
                                }
                            }
                            boolean IsExistsButton = false;
                            for (ECart item : DatabaseClient.getInstance(context)
                                    .getAppDatabase()
                                    .getCartDao()
                                    .getCarts()) {
                                if (item.getProductRegister() == ProductId) {
                                    IsExistsButton = true;
                                    break;
                                }
                            }
                            if (IsExistsButton) {
                                productGasAddCart.getBackground().setColorFilter(Color.parseColor(COLOR_ACTIVADO), PorterDuff.Mode.SRC_ATOP);
                                productGasAddCart.setImageResource(R.drawable.ic_cart_card);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context, "That didn't work!", Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(stringRequest);

            productGasAddCart.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    RequestQueue queue = Volley.newRequestQueue(context);
                    StringBuilder sb = new StringBuilder();
                    sb.append(Global.URL_HOST + "/product/markes/" + product.getMarkeId());
                    String url = sb.toString();
                    final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @SuppressLint("RestrictedApi")
                                @Override
                                public void onResponse(String response) {
                                    int ProductId = 0;
                                    Gson gson = new Gson();
                                    ArrayList<ProductGas> productDetails = gson.fromJson(response,
                                            new TypeToken<ArrayList<ProductGas>>() {
                                            }.getType());
                                    for (ProductGas item : productDetails) {
                                        if (item.getDetail_measurement_id().getName().split(" ")[1].equals(product.getType().split("-")[1]) &&
                                                item.getMeasurement() == Float.parseFloat(peso.getText().toString())) {
                                            ProductId = item.getId();
                                            break;
                                        }
                                    }
                                    ECart oECart = null;
                                    boolean IsAdd = false;
                                    for (ECart item : DatabaseClient.getInstance(context)
                                            .getAppDatabase()
                                            .getCartDao()
                                            .getCarts()) {
                                        if (item.getProductRegister() == ProductId)
                                            oECart = item;
                                        if (item.getName().toLowerCase().contains("agua") && item.getName().toLowerCase().contains("gas"))
                                            IsAdd = false;
                                        else if (!item.getName().toLowerCase().contains("gas") || item.getName().toLowerCase().contains("cisterna"))
                                            IsAdd = false;
                                        else
                                            IsAdd = true;
                                    }
                                    if (DatabaseClient.getInstance(context)
                                            .getAppDatabase()
                                            .getCartDao()
                                            .getCarts().size() == 0)
                                        IsAdd = true;
                                    if (ProductId != 0) {
                                        String frii_Background = String.format("#%06x", ContextCompat.getColor(context, R.color.frii_Background) & 0xffffff);
                                        if (oECart == null) {
                                            if (productGasCantidad.getText().length() > 0) {
                                                ECart eCart = new ECart();
                                                eCart.setName(product.getName() + " " + peso.getText().toString() + " kilos");
                                                eCart.setPrice(0);
                                                eCart.setCantidad(Integer.parseInt(productGasCantidad.getText().toString()));
                                                eCart.setTotal(0);
                                                // eCart.setTotal(Float.parseFloat(productGasCantidad.getText().toString()) * product.getPrice());
                                                eCart.setProductRegister(ProductId);
                                                DatabaseClient.getInstance(context)
                                                        .getAppDatabase()
                                                        .getCartDao()
                                                        .addCart(eCart);

                                                productGasAddCart.getBackground().setColorFilter(Color.parseColor(COLOR_ACTIVADO), PorterDuff.Mode.SRC_ATOP);
                                                productGasAddCart.setImageResource(R.drawable.ic_cart_card);
                                                ((Activity) context).invalidateOptionsMenu();
                                            } else {
                                                Toast.makeText(context, "Ingrese una cantidad mayor a 0", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            DatabaseClient.getInstance(context)
                                                    .getAppDatabase()
                                                    .getCartDao()
                                                    .deleteCart(oECart);

                                            ((Activity) context).invalidateOptionsMenu();
                                            productGasAddCart.getBackground().setColorFilter(Color.parseColor(COLOR_DESACTIVADO), PorterDuff.Mode.SRC_ATOP);
                                            productGasAddCart.setImageResource(R.drawable.ic_shopping_cart);
                                        }
                                    } else
                                        Toast.makeText(context, "El producto no esta en la base de datos", Toast.LENGTH_SHORT).show();
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context, "That didn't work!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    queue.add(stringRequest);
                }
            });
        }
    }

}
