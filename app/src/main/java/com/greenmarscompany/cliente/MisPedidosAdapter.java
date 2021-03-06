package com.greenmarscompany.cliente;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.Session;
import com.greenmarscompany.cliente.persistence.entity.Acount;
import com.greenmarscompany.cliente.pojo.Order;

import com.greenmarscompany.cliente.utils.AgendarPedido;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class MisPedidosAdapter extends RecyclerView.Adapter<MisPedidosAdapter.viewHolder> implements android.view.View.OnClickListener {

    private final java.util.List<Order> data;
    private Context context;
    private android.view.View.OnClickListener listener;
    ViewGroup viewGroup;
    private Socket socket;
    private final MisPedidosFragment oMisPedidosFragment;

    public String HOST_NODEJS = Global.URL_NODE;

    public static final String TAG = "firebase";
    private final static int NOTIFICATION_ID = 0;
    private final static String CHANNEL_ID = "NOTIFICACION";

    public MisPedidosAdapter(java.util.List<Order> data, MisPedidosFragment oMisPedidosFragment) {
        this.data = data;
        this.oMisPedidosFragment = oMisPedidosFragment;
    }


    @Override
    public void onClick(android.view.View v) {
        if (listener != null) listener.onClick(v);
    }

    public void setOnClickListener(android.view.View.OnClickListener listener) {
        this.listener = listener;
    }

    @androidx.annotation.NonNull
    @Override
    public viewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
        android.view.View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_mispedidos, parent, false);
        view.setOnClickListener(this);
        context = parent.getContext();
        viewGroup = parent;

        return new viewHolder(view);
    }


    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull final viewHolder holder, final int position) {
        holder.titlePedido.setText(data.get(position).getDate());
        holder.btnEliminar.setEnabled(false);

        initSocket();

        switch (data.get(position).getStatus()) {
            case "wait":
                holder.estadoPedido.setText("Buscando proveedor...");
                holder.estadoPedido.setTextColor(android.graphics.Color.parseColor("#2979ff"));
                holder.mensaje.setVisibility(android.view.View.GONE);
                holder.llamar.setVisibility(android.view.View.GONE);
                holder.cancelar.setText("Cancelar");
                break;
            case "refuse":
                holder.estadoPedido.setText("Rechazado");
                break;
            case "confirm":
                holder.estadoPedido.setText("Pedido en camino");
                holder.cancelar.setEnabled(true);
                holder.cancelar.setText("Cancelar");

                break;
            case "delivered":
                holder.estadoPedido.setText("Entregado");
                holder.cancelar.setText("Calificar");
                holder.cancelar.setBackgroundResource(R.drawable.custom_button_calificar);
                break;
            default:
                holder.estadoPedido.setText("Cancelado");
                holder.cancelar.setText("Repedir");
                holder.cancelar.setBackgroundResource(R.drawable.custom_button_repedir);
                holder.mensaje.setVisibility(android.view.View.GONE);
                holder.llamar.setVisibility(android.view.View.GONE);
                holder.btnEliminar.setEnabled(true);
                break;
        }
        final StringBuilder details = new StringBuilder();
        StringBuilder precioUnitario = new StringBuilder();
        StringBuilder subTotal = new StringBuilder();
        for (int i = 0; i < data.get(position).getDetalles().size(); i++) {
            details.append(data.get(position).getDetalles().get(i)).append("\n");
            precioUnitario.append(data.get(position).getListPrecioUnitario().get(i)).append("\n");
            subTotal.append(data.get(position).getListSubTotal().get(i)).append("\n");
        }
        holder.detallePedido.setText(details.toString());
        holder.precioUnitario.setText(precioUnitario.toString());
        holder.subTotal.setText(subTotal.toString());
        holder.totalFinal.setText("S/. " + data.get(position).getTotalFinal());
        if (data.get(position).getPhone() != null && !data.get(position).getPhone().equals("")) {
            holder.llamar.setEnabled(true);
            holder.mensaje.setEnabled(true);
            holder.mensaje.setVisibility(android.view.View.VISIBLE);
            holder.llamar.setVisibility(android.view.View.VISIBLE);

            if (data.get(position).getStatus().equals("delivered") && details.toString().contains("gas") || details.toString().contains("cisterna")) {
                holder.llamar.setText("Emergencia");
                holder.llamar.setBackgroundResource(R.drawable.custom_button_delete);
            }

            holder.llamar.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    String dial = "tel: " + data.get(position).getPhone();
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(dial));
                    context.startActivity(intent);
                }
            });

            holder.mensaje.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto: " + data.get(position).getPhone()));
                    context.startActivity(intent);
                }
            });

            holder.timerAuto.setText("Confirmado por: " + data.get(position).getCompanyName());
            holder.textCancelado.setVisibility(android.view.View.GONE);

        } else {
            holder.llamar.setEnabled(false);
            holder.llamar.setBackgroundResource(R.drawable.custom_button_gray);
            holder.mensaje.setEnabled(false);
            holder.mensaje.setBackgroundResource(R.drawable.custom_button_gray);
            holder.mensaje.setVisibility(android.view.View.GONE);
            holder.llamar.setVisibility(android.view.View.GONE);
        }
        holder.btnEliminar.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                final Acount acount = DatabaseClient.getInstance(context)
                        .getAppDatabase()
                        .getAcountDao()
                        .getUser(new Session(context).getToken());
                JSONObject jsonObject = new JSONObject();
                RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(context));
                try {
                    jsonObject.put("order_id", data.get(position).getId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String url = Global.URL_HOST + "/order/delete";
                System.out.println(jsonObject.toString());
                JsonObjectRequest jsonObjectRequest =
                        new JsonObjectRequest(Request.Method.PUT, url, jsonObject, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                oMisPedidosFragment.llenarPedidos();
                                Toast.makeText(context, "Se elimino el pedido", Toast.LENGTH_SHORT).show();

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                android.util.Log.d("Volley get", "error voley" + error.toString());
                                NetworkResponse response = error.networkResponse;
                                if (error instanceof ServerError && response != null) {
                                    try {
                                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                        System.out.println(res);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }) {
                            @Override
                            public java.util.Map<String, String> getHeaders() {
                                java.util.Map<String, String> headers = new HashMap<>();
                                headers.put("Authorization", "JWT " + acount.getToken());
                                headers.put("Content-Type", "application/json");
                                return headers;
                            }
                        };

                queue.add(jsonObjectRequest);
            }
        });
        if (data.get(position).getStatus().equals("cancel")) {
            holder.cancelar.setText("Repedir");
            holder.cancelar.setBackgroundResource(R.drawable.custom_button_repedir);
            if (holder.timer != null) {
                holder.timer.cancel();
            }
        } else {
            if (data.get(position).getStatus().equals("wait")) {
                java.sql.Time time = java.sql.Time.valueOf(data.get(position).getTime());
                String timeNow = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + ":" +
                        Calendar.getInstance().get(Calendar.MINUTE) + ":" +
                        Calendar.getInstance().get(Calendar.SECOND);
                long timer = time.getTime() + 120000 - java.sql.Time.valueOf(timeNow).getTime();
                //  timer =120000;
                if (timer > 0) {

                    if (holder.timer != null) {
                        holder.timer.cancel();
                    }
                    holder.timerflag = true;
                    holder.timer = new CountDownTimer(timer, 1000) {
                        public void onTick(long millisUntilFinished) {
                            NumberFormat f = new DecimalFormat("00");
                            long hour = (millisUntilFinished / 3600000) % 24;
                            long min = (millisUntilFinished / 60000) % 60;
                            long sec = (millisUntilFinished / 1000) % 60;

                            holder.timerAuto.setText(f.format(hour) + ":" + f.format(min) + ":" + f.format(sec));
                        }

                        public void onFinish() {
                            holder.timerflag = false;

                            tiempoCancelar(data.get(position).getId(), data.get(position).getStatus(), position);
                            oMisPedidosFragment.llenarPedidos();


                            //    notificacionCancelado("Pedido cancelado", "El tiempo expiro");
                            //      notifyDataSetChanged();
                            //    notifyItemChanged(position);
                        }
                    };
                } else {
                    //   if(!timerAuto.contains("Confirmado) ) {
                    tiempoCancelar(data.get(position).getId(), data.get(position).getStatus(), position);
                    oMisPedidosFragment.llenarPedidos();

//                    notifyDataSetChanged();
                    //     notifyItemChanged(position);
                    //    holder.timerflag = false;
                }
            }
            holder.cancelar.setOnClickListener(new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    if (holder.cancelar.getText().equals("Cancelar")) {

                        /*mensajeConfirmacion(data.get(position).getId(), data.get(position).getStatus(), position);
                        notifyDataSetChanged();
                        notifyItemChanged(position);*/

                        // initSocket();
                        emitirCancelar(data.get(position).getId());

                        holder.timerflag = false;
                    } else if (holder.cancelar.getText().equals("Calificar")) {
                        boolean AgendarPedido = false;
                        if (data.get(position).getStatus().equals("delivered") && details.toString().contains("gas") || details.toString().contains("cisterna"))
                            AgendarPedido = true;
                        AgendarPedido agendarPedido = new AgendarPedido(context, data.get(position).getId(),
                                data.get(position).getCalification(), AgendarPedido);
                        agendarPedido.show();
                    }
                }
            });
        }


        holder.cancelar.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {

                if (holder.cancelar.getText().equals("Cancelar")) {
                    mensajeConfirmacion(data.get(position).getId(), data.get(position).getStatus(), position);

                    notifyDataSetChanged();
                    notifyItemChanged(position);

                    holder.timerflag = false;
                } else if (holder.cancelar.getText().equals("Calificar")) {
                    boolean AgendarPedido = false;
                    if (data.get(position).getStatus().equals("delivered") && details.toString().contains("gas") || details.toString().contains("cisterna"))
                        AgendarPedido = true;
                    AgendarPedido agendarPedido = new AgendarPedido(context, data.get(position).getId(),
                            data.get(position).getCalification(), AgendarPedido);
                    agendarPedido.show();
                } else if (holder.cancelar.getText().equals("Repedir")) {
                    //FUNCION( DATA.GET(POSITION).GETID )*/
                    // initSocket();
                    emitirReorder(data.get(position).getId());
                    notifyDataSetChanged();
                    notifyItemChanged(position);
                }
            }
        });

        if (holder.timerflag) {
            holder.timer.start();
        } else {
            if (holder.timer != null) {
                holder.timer.cancel();
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class viewHolder extends RecyclerView.ViewHolder {
        android.widget.TextView titlePedido, estadoPedido, detallePedido, precioUnitario, subTotal, textCancelado;
        Button llamar, mensaje, cancelar, btnRepedir;
        CountDownTimer timer;
        boolean timerflag = false;
        android.widget.TextView timerAuto, totalFinal;
        ImageButton btnEliminar;

        viewHolder(@androidx.annotation.NonNull android.view.View itemView) {
            super(itemView);
            titlePedido = itemView.findViewById(R.id.TitlePedidos);
            estadoPedido = itemView.findViewById(R.id.EstadoPedido);
            detallePedido = itemView.findViewById(R.id.txtDetallePedido);
            precioUnitario = itemView.findViewById(R.id.txtPrecioUnitario);
            subTotal = itemView.findViewById(R.id.txtSubTotal);
            llamar = itemView.findViewById(R.id.ButtonLLamarPedido);
            mensaje = itemView.findViewById(R.id.ButtonMensajePedido);
            cancelar = itemView.findViewById(R.id.ButtonCancelarPedido);
           /* btnRepedir = itemView.findViewById(R.id.buttonRepedir);*/
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            timerAuto = itemView.findViewById(R.id.timerAuto);
            textCancelado = itemView.findViewById(R.id.textCancelado);
            totalFinal = itemView.findViewById(R.id.lblTotal);
        }
    }

    private void mensajeConfirmacion(final int idOrden, final String status, final int position) {


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Desea realmente cancelar el pedido")
                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*tiempoCancelar(idOrden, status, position);*/
                        // initSocket();
                        emitirCancelar(idOrden);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        builder.show();
    }

    private void tiempoCancelar(int idOrden, String status, final int position) {
        if (data.isEmpty()) return;
        if (data.get(position).getStatus().equals("wait")) {
            final String url = Global.URL_HOST + "/order/client/cancel/";
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", idOrden);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestQueue queue = Volley.newRequestQueue(context);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                int status = response.getInt("status");
                                if (status == 200) {
                                    Toast.makeText(context, response.getString("message"), Toast.LENGTH_LONG).show();
                                    /*initSocket();*/
                                    Order order = data.get(position);
                                    order.setStatus("cancel");
                                    data.set(position, order);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            android.util.Log.d("Volley get", "error voley" + error.toString());
                            NetworkResponse response = error.networkResponse;
                            if (error instanceof ServerError && response != null) {
                                try {
                                    String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                    System.out.println(res);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
            queue.add(jsonObjectRequest);
        }
    }

    private void initSocket() {

        int id_user = new Session(context).getToken();
        JSONObject data = new JSONObject();
        Acount cuenta = DatabaseClient.getInstance(context)
                .getAppDatabase()
                .getAcountDao()
                .getUser(id_user);

        final JSONObject json_connect = new JSONObject();
        IO.Options opts = new IO.Options();
        // opts.forceNew = true;
        opts.reconnection = true;
        opts.query = "auth_token=thisgo77";
        try {
            json_connect.put("ID", "US01");
            json_connect.put("TOKEN", cuenta.getToken());
            json_connect.put("ID_CLIENT", 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            socket = IO.socket(HOST_NODEJS, opts);
            socket.connect();
            // SOCKET.io().reconnectionDelay(10000);
            android.util.Log.d(TAG, "Node connect ok");
            //conect();
        } catch (URISyntaxException e) {
            android.util.Log.d(TAG, "Node connect error");
        }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                android.util.Log.d(TAG, "emitiendo new conect");
                JSONObject data = new JSONObject();
                int id = new Session(context).getToken();
                Acount cuenta = DatabaseClient.getInstance(context)
                        .getAppDatabase()
                        .getAcountDao()
                        .getUser(id);
                try {
                    data.put("ID", cuenta.getId());
                    data.put("type", "client");
                    android.util.Log.d(TAG, "conect " + data.toString());
                    socket.emit("new connect", data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                android.util.Log.d(TAG, "SERVER connect " + date);


            }
        });

        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                android.util.Log.d(TAG, "SERVER disconnect " + date);
            }
        });

        socket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String my_date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                android.util.Log.d(TAG, "SERVER reconnect " + my_date);
            }
        });

        socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String my_date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                android.util.Log.d(TAG, "SERVER timeout " + my_date);
            }
        });

        socket.on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String my_date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                android.util.Log.d(TAG, "SERVER reconnecting " + my_date);
            }
        });

        JSONObject datas = new JSONObject();
        try {
            datas.put("id", id_user);
            datas.put("token", cuenta.getToken());
            android.util.Log.d(TAG, "conect " + datas.toString());
            socket.emit("status order", datas);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    //FUNCION(IDORDEN)
    private void emitirCancelar(int idorden) {
        JSONObject datas = new JSONObject();
        int id_user = new Session(context).getToken();
        try {
            datas.put("id", idorden);
            datas.put("id_user", id_user);
            socket.emit("order cancel client", datas);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void emitirReorder(int idorden) {
        JSONObject datas = new JSONObject();
        int id_user = new Session(context).getToken();
        try {
            datas.put("id", idorden);
            datas.put("id_user", id_user);
            socket.emit("reorder client", datas);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
