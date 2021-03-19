package com.greenmarscompany.cliente;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.entity.ECart;

public class CartDetailAdapter extends RecyclerView.Adapter<CartDetailAdapter.viewHolder> implements android.view.View.OnClickListener {

    private final java.util.List<ECart> cartDetails;
    private Context context;
    private EventListener eventListener;

    public CartDetailAdapter(java.util.List<ECart> cartDetails, EventListener eventListener) {
        this.eventListener = eventListener;
        this.cartDetails = cartDetails;
    }

    @Override
    public void onClick(android.view.View v) {

    }

    @androidx.annotation.NonNull
    @Override
    public viewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {

        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_cart_detail, parent, false);
        view.setOnClickListener(this);
        context = parent.getContext();
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull final viewHolder holder, final int position) {
        holder.cartTitle.setText(cartDetails.get(position).getName());
        holder.cartCantidad.setText(String.valueOf(cartDetails.get(position).getCantidad()));
        //holder.cartPrecioU.setText(String.valueOf(cartDetails.get(position).getPrice()));
        //holder.cartSubtotal.setText(String.valueOf(cartDetails.get(position).getTotal()));


        //calcularSubTotal(holder.cartCantidad, holder.cartPrecioU, holder.cartSubtotal, position);
        //eventListener.calcularTotal(calcularTotal());

        holder.cartCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (holder.cartCantidad.getText().length() > 0) {
                    //calcularSubTotal(holder.cartCantidad, holder.cartPrecioU, holder.cartSubtotal, position);
                    actualizarCantidad(holder.cartCantidad, holder.getAdapterPosition());
                    //eventListener.calcularTotal(calcularTotal());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.cartDeleteButton.setOnClickListener(v -> {
            ECart eCart = cartDetails.get(position);
            if (eCart.getUid().equals(cartDetails.get(position).getUid())) {
                DatabaseClient.getInstance(context)
                        .getAppDatabase()
                        .getCartDao()
                        .deleteCart(eCart);

                cartDetails.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                notifyDataSetChanged();

            }
        });


    }

    @Override
    public int getItemCount() {
        return cartDetails.size();
    }

    static class viewHolder extends RecyclerView.ViewHolder {

        android.widget.TextView cartTitle;
        EditText cartCantidad;
        ImageButton cartDeleteButton;

        viewHolder(@androidx.annotation.NonNull android.view.View itemView) {
            super(itemView);
            cartTitle = itemView.findViewById(R.id.cartProductTitle);
            cartCantidad = itemView.findViewById(R.id.cartEditcantidad);
            cartDeleteButton = itemView.findViewById(R.id.cartDeleteButton);

        }
    }

    private void actualizarCantidad(android.widget.TextView cantidad, int position) {
        ECart cart = DatabaseClient
                .getInstance(context)
                .getAppDatabase()
                .getCartDao()
                .getCart(cartDetails.get(position).getUid());

        int cant = Integer.parseInt(cantidad.getText().toString());
        if (cart != null) {
            cart.setCantidad(cant);
            cart.setTotal(cant * cart.getTotal());

            DatabaseClient.getInstance(context)
                    .getAppDatabase()
                    .getCartDao()
                    .updateCart(cart);
        }

    }

    public interface EventListener {
        //void calcularTotal(float total);
    }
}