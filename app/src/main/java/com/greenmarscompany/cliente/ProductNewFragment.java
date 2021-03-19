package com.greenmarscompany.cliente;

import android.content.Context;
import android.net.Uri;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.greenmarscompany.cliente.pojo.Product;
import com.greenmarscompany.cliente.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ProductNewFragment extends androidx.fragment.app.Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    String urlBase = Global.URL_BASE;
    private OnFragmentInteractionListener mListener;


    private ProductAdapterNew productAdapterNew;
    private RecyclerView recyclerView;
    ArrayList<Product> products;

    public ProductNewFragment() {
        // Required empty public constructor
    }

    public static ProductNewFragment newInstance(String param1, String param2) {
        ProductNewFragment fragment = new ProductNewFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container,
                                          android.os.Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final android.view.View view = inflater.inflate(R.layout.fragment_product_new, container, false);
        recyclerView = view.findViewById(R.id.ProductsContainerNew);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        llenarDatos();

        return view;
    }

    private void llenarDatos() {
        String url = "";
        android.os.Bundle b = this.getArguments();
        if (b != null) {
            url = Global.URL_HOST + "/product/markes/" + b.getInt("IdMarke");
        } else {
            Toast.makeText(getContext(), "No se pudo cargar la Información", Toast.LENGTH_LONG).show();
        }

        products = new ArrayList<>();

        JSONArray jsonArray = new JSONArray();
        JsonArrayRequest arrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, jsonArray, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object = response.getJSONObject(i);
                                String imagen_url = urlBase + object.getString("image");
                                Product product = new Product(
                                        object.getInt("id"),
                                        object.getString("description"),
                                        "Precio U: S/." +
                                                object.getString("unit_price"),
                                        Float.parseFloat(object.getString("unit_price")),
                                        object.getInt("measurement"),
                                        1,
                                        imagen_url,
                                        "",
                                        object.getJSONObject("marke_id").getString("name")
                                );

                                products.add(product);
                            }

                            productAdapterNew = new ProductAdapterNew(products);
                            recyclerView.setAdapter(productAdapterNew);
                        } catch (Exception e) {
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
                                JSONObject obj = new JSONObject(res);
                                android.util.Log.d("Voley post", obj.toString());
                                String msj = obj.getString("message");
                                Toast.makeText(getContext(), msj, Toast.LENGTH_SHORT).show();

                            } catch (UnsupportedEncodingException | JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
        VolleySingleton.getInstance(getContext()).addToRequestQueue(arrayRequest);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
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
}
