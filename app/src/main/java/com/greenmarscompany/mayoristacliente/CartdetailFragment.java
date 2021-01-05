package com.greenmarscompany.mayoristacliente;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.greenmarscompany.mayoristacliente.Login.LoginActivity;
import com.greenmarscompany.mayoristacliente.persistence.DatabaseClient;
import com.greenmarscompany.mayoristacliente.persistence.Session;
import com.greenmarscompany.mayoristacliente.persistence.entity.Acount;
import com.greenmarscompany.mayoristacliente.persistence.entity.ECart;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class CartdetailFragment extends androidx.fragment.app.Fragment implements CartDetailAdapter.EventListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private OnFragmentInteractionListener mListener;
    private static final String TAG = "GAS";
    private Socket socket;
    private RecyclerView recyclerView;
    java.util.List<ECart> cartDetails;
    Button procesarPedido;
    private RadioButton voucher;
    RadioGroup groupMetodo;
    private Boolean isTarjeta = false;
    private String tipoComprobante;
    int indexRadioTarejeta = 0;

    public static CartdetailFragment newInstance(String param1, String param2) {
        CartdetailFragment fragment = new CartdetailFragment();
        android.os.Bundle args = new android.os.Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //Validar informacion del usuario
        Session session = new Session(getContext());
        final int token = session.getToken();
        if (token == 0 || token < 0) {
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            Objects.requireNonNull(getActivity()).finish();
            System.out.println("LAS CREDENCIALES SON INVALIDAS");
        }
        //--
        initSocket();
    }

    @Override
    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container, android.os.Bundle savedInstanceState) {

        android.view.View view = inflater.inflate(R.layout.fragment_cartdetail, container, false);
        recyclerView = view.findViewById(R.id.CartDetailContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //--Metodos de pagos
        voucher = view.findViewById(R.id.RadioButtonBoleta);
        groupMetodo = view.findViewById(R.id.groupMetodo);
        RadioGroup efectivoGroup = view.findViewById(R.id.groupMetodoEfectivo);

        efectivoGroup.setOnCheckedChangeListener((group, checkedId) -> {
            android.view.View radioButton = efectivoGroup.findViewById(checkedId);
            int index = efectivoGroup.indexOfChild(radioButton);
            if (index == 0) {
                switch (indexRadioTarejeta) {
                    case 0:
                        tipoComprobante = "tunki-boleta";
                        break;
                    case 1:
                        tipoComprobante = "yape-boleta";
                        break;
                    case 2:
                        tipoComprobante = "tarjeta-boleta";
                        break;
                    case 3:
                        tipoComprobante = "boleta";
                        break;
                }
            } else if (index == 1) {
                switch (indexRadioTarejeta) {
                    case 0:
                        tipoComprobante = "tunki-factura";
                        break;
                    case 1:
                        tipoComprobante = "yape-factura";
                        break;
                    case 2:
                        tipoComprobante = "tarjeta-factura";
                        break;
                    case 3:
                        tipoComprobante = "factura";
                        break;
                }
            }
            android.util.Log.d(TAG, "Event Efectivo: " + tipoComprobante);
        });
        groupMetodo.setOnCheckedChangeListener((group, checkedId) -> {
            android.view.View radioButton = groupMetodo.findViewById(checkedId);
            int index = groupMetodo.indexOfChild(radioButton);
            switch (index) {
                case 0:
                    indexRadioTarejeta = 0;
                    if (voucher.isChecked()) tipoComprobante = "tunki-boleta";
                    else tipoComprobante = "tunki-factura";
                    isTarjeta = true;
                    break;
                case 1:
                    indexRadioTarejeta = 1;
                    if (voucher.isChecked()) tipoComprobante = "yape-boleta";
                    else tipoComprobante = "yape-factura";
                    isTarjeta = true;
                    break;
                case 2:
                    indexRadioTarejeta = 2;
                    if (voucher.isChecked()) tipoComprobante = "tarjeta-boleta";
                    else tipoComprobante = "tarjeta-factura";
                    isTarjeta = true;
                    break;
                case 3:
                    indexRadioTarejeta = 3;
                    if (voucher.isChecked()) tipoComprobante = "boleta";
                    else tipoComprobante = "factura";
                    isTarjeta = false;
                    break;
                default:
                    isTarjeta = false;
                    tipoComprobante = "";
            }

            android.util.Log.d(TAG, "Radio: " + tipoComprobante);
        });
        llenarCarrito();

        procesarPedido = view.findViewById(R.id.ButtonCartProcesarPedido);
        java.util.List<ECart> eCarts = DatabaseClient.getInstance(getContext())
                .getAppDatabase()
                .getCartDao()
                .getCarts();

        assert eCarts != null;
        if (eCarts.size() == 0) {
            procesarPedido.setEnabled(false);
            procesarPedido.setBackgroundResource(R.drawable.custom_button_gray);
        } else {
            procesarPedido.setEnabled(true);
        }
        procesarPedido.setOnClickListener(v -> confirmarPedido());

        //--
        Button button = view.findViewById(R.id.btnAbrirSheet);
        LinearLayout linearLayout = view.findViewById(R.id.bottomSheet);
        BottomSheetBehavior<android.view.View> bottomSheetBehavior = BottomSheetBehavior.from(linearLayout);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@androidx.annotation.NonNull android.view.View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    button.setVisibility(android.view.View.GONE);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    button.setVisibility(android.view.View.VISIBLE);
                } else if(newState == BottomSheetBehavior.STATE_HIDDEN) {
                    button.setVisibility(android.view.View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@androidx.annotation.NonNull android.view.View bottomSheet, float slideOffset) {
            }
        });

        button.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                button.setVisibility(android.view.View.GONE);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                button.setVisibility(android.view.View.VISIBLE);
            }
        });

        return view;
    }

    // Confirmar pedido
    private void confirmarPedido() {
        android.util.Log.d(TAG, "confirmarPedido: click");
        //Obtener el token del cliente
        final Acount acount = DatabaseClient.getInstance(getContext())
                .getAppDatabase()
                .getAcountDao()
                .getUser(new Session(getContext()).getToken());

        if (!isTarjeta) {
            if (voucher.isChecked()) tipoComprobante = "boleta";
            else tipoComprobante = "factura";
        }

        JSONObject jsonObject = new JSONObject();
        RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        try {
            jsonObject.put("voucher", tipoComprobante);
            jsonObject.put("latitud", String.valueOf(acount.getLatitud()));
            jsonObject.put("longitud", String.valueOf(acount.getLongitud()));
            jsonObject.put("client_id", new Session(getContext()).getToken());

            JSONArray jsonArray = new JSONArray();
            java.util.List<ECart> eCarts = DatabaseClient.getInstance(getContext())
                    .getAppDatabase()
                    .getCartDao()
                    .getCarts();

            if (eCarts != null) {
                for (ECart e : eCarts) {
                    JSONObject orders_detal = new JSONObject();
                    orders_detal.put("product_id", e.getProductRegister());
                    orders_detal.put("quantity", e.getCantidad());
                    orders_detal.put("unit_price", e.getPrice());
                    jsonArray.put(orders_detal);
                }
            }

            jsonObject.put("detalle_orden", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String baseURL = Global.URL_HOST;
        String url = baseURL + "/client/order/";
        System.out.println(jsonObject.toString());
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
                    if (getActivity() == null) return;
                    try {
                        int status = response.getInt("status");
                        if (status == 201) {
                            String message = response.getString("message");
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();


                            JSONObject datas = new JSONObject();
                            datas.put("id", response.getJSONObject("data").getInt("order_id"));
                            datas.put("latitude", acount.getLatitud());
                            datas.put("longitude", acount.getLongitud());
                            socket.emit("get orders", datas);
                            android.util.Log.d(TAG, "confirmarPedido: " + datas);

                            DatabaseClient.getInstance(getContext())
                                    .getAppDatabase()
                                    .getCartDao()
                                    .deleteAllCart();

                            Intent intent = new Intent(getContext(), PedidosActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> {
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
                }) {
                    @Override
                    public java.util.Map<String, String> getHeaders() {
                        java.util.Map<String, String> headers = new HashMap<>();
                        android.util.Log.d("Voley get", acount.getToken());
                        headers.put("Authorization", "JWT " + acount.getToken());
                        headers.put("Content-Type", "application/json");
                        return headers;
                    }
                };

        queue.add(jsonObjectRequest);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(@androidx.annotation.NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void llenarCarrito() {
        cartDetails = DatabaseClient.getInstance(getContext())
                .getAppDatabase()
                .getCartDao()
                .getCarts();

        CartDetailAdapter cartDetailAdapter = new CartDetailAdapter(cartDetails, this);
        recyclerView.setAdapter(cartDetailAdapter);


    }

    private void initSocket() {

        if (getContext() == null) return;
        int id_user = new Session(getContext()).getToken();
        Acount cuenta = DatabaseClient.getInstance(getContext())
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
            json_connect.put("ID_CLIENT", id_user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            String HOST_NODEJS = Global.URL_NODE;
            socket = IO.socket(HOST_NODEJS, opts);
            socket.connect();
            // SOCKET.io().reconnectionDelay(10000);
            android.util.Log.d(TAG, "Node connect ok");
            //conect();
        } catch (URISyntaxException e) {
            android.util.Log.d(TAG, "Node connect error");
        }

        socket.on(Socket.EVENT_CONNECT, args -> {
            android.util.Log.d(TAG, "emitiendo new conect");
            JSONObject data = new JSONObject();

            if (getContext() == null) return;
            int id = new Session(getContext()).getToken();
            Acount cuenta1 = DatabaseClient.getInstance(getActivity())
                    .getAppDatabase()
                    .getAcountDao()
                    .getUser(id);
            try {
                data.put("ID", cuenta1.getId());
                data.put("type", "client");
                android.util.Log.d(TAG, "conect " + data.toString());
                socket.emit("new connect", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            android.util.Log.d(TAG, "SERVER connect " + date);


        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            String date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            android.util.Log.d(TAG, "SERVER disconnect " + date);
        });

        socket.on(Socket.EVENT_RECONNECT, args -> {
            String my_date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            android.util.Log.d(TAG, "SERVER reconnect " + my_date);
        });

        socket.on(Socket.EVENT_CONNECT_TIMEOUT, args -> {
            String my_date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            android.util.Log.d(TAG, "SERVER timeout " + my_date);
        });

        socket.on(Socket.EVENT_RECONNECTING, args -> {
            String my_date = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
            android.util.Log.d(TAG, "SERVER reconnecting " + my_date);
        });
    }
}
