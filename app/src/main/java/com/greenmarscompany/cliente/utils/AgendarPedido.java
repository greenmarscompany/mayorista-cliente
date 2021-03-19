package com.greenmarscompany.cliente.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.fragment.app.FragmentActivity;

import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.greenmarscompany.cliente.Global;
import com.greenmarscompany.cliente.R;
import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.Session;
import com.greenmarscompany.cliente.persistence.entity.Acount;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AgendarPedido extends android.app.Dialog implements android.view.View.OnClickListener {

    public Context context;
    public android.app.Dialog d;
    public Button btnAgendar, no;
    RatingBar calificacion;
    EditText fecha_notify;
    private int id_orden;
    private float calificationNumber;
    Boolean AgendarPedido;

    public AgendarPedido(Context context, int id, float cal, boolean AgendarPedido) {
        super(context);
        this.context = context;
        this.id_orden = id;
        this.calificationNumber = cal;
        this.AgendarPedido = AgendarPedido;
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_agendar_pedido);
        no = (Button) findViewById(R.id.btnCancelar);
        calificacion = findViewById(R.id.ratingBar);
        fecha_notify = findViewById(R.id.seleccionarfecha);
        btnAgendar = findViewById(R.id.btnAgenda);
        android.widget.TextView txtAgendarPedido = findViewById(R.id.txtAgendarPedido);
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String FechaActual = df.format(Calendar.getInstance().getTime());
        fecha_notify.setText(FechaActual);
        no.setOnClickListener(this);
        btnAgendar.setOnClickListener(this);
        if (!AgendarPedido) {
            txtAgendarPedido.setVisibility(android.view.View.INVISIBLE);
            fecha_notify.setVisibility(android.view.View.INVISIBLE);
        } else {
            fecha_notify.setOnClickListener(new android.view.View.OnClickListener() {
                public void onClick(android.view.View v) {
                    DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            // +1 because January is zero
                            final String selectedDate = day + "-" + (month + 1) + "-" + year;
                            fecha_notify.setText(selectedDate);
                        }
                    });
                    newFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "Theme");
                }
            });
        }
        if (calificationNumber > 0) {
            calificacion.setRating(calificationNumber);
            calificacion.setEnabled(false);
        }

    }

    @Override
    public void onClick(android.view.View v) {
        switch (v.getId()) {
            case R.id.btnCancelar:
                dismiss();
                break;

            case R.id.btnAgenda:
                agendarPedido();
                break;
            default:
                break;
        }
        dismiss();
    }

    private void agendarPedido() {
        int token = new Session(getContext()).getToken();

        final Acount acount = DatabaseClient.getInstance(context)
                .getAppDatabase()
                .getAcountDao()
                .getUser(token);

        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject object = new JSONObject();
        try {
            object.put("id_orden", id_orden);
            object.put("id_client", token);

            if (fecha_notify.getText().length() > 0) {
                object.put("fecha_notify", fecha_notify.getText().toString());
            } else {
                object.put("fecha_notify", "");
            }

            if (calificacion.getRating() > 0) {
                object.put("calificacion", calificacion.getRating());
            } else {
                object.put("calificacion", "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = Global.URL_HOST + "/diary/";

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST,
                url, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int status = response.getInt("status");
                    if (status == 200) {
                        System.out.println(response.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                android.util.Log.d("Voley get", acount.getToken());
                headers.put("Authorization", "JWT " + acount.getToken());
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(objectRequest);
    }
}
