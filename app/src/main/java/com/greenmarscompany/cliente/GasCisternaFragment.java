package com.greenmarscompany.cliente;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.*;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.greenmarscompany.cliente.login.LoginActivity;
import com.greenmarscompany.cliente.persistence.Session;
import com.greenmarscompany.cliente.pojo.Product;
import com.greenmarscompany.cliente.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;


public class GasCisternaFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private ProductAdapter productAdapter;
    private RecyclerView recyclerView;
    ArrayList<Product> products;
    //--
    private final String urlBase = Global.URL_BASE;
    private boolean isRestart = false;

    public GasCisternaFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
        if(isRestart && products != null) {
            productAdapter.products = this.products;
            productAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             android.os.Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gas_cisterna, container, false);
        recyclerView = view.findViewById(R.id.ProductsContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        products = new ArrayList<>();

        llenarDatos();
        return view;
    }


    @Override
    public void onPause() {
        super.onPause();
        this.isRestart = true;
    }

    private void llenarDatos() {
        String url = Global.URL_HOST + "/product/gas/gas-cisterna";

        JSONObject jsonArray = new JSONObject();
        JsonObjectRequest arrayRequest =
                new JsonObjectRequest(Request.Method.GET, url, jsonArray, response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject object = data.getJSONObject(i);
                            String imagen_url = urlBase + object.getString("image");
                            Product product = new Product(
                                    object.getInt("id"),
                                    object.getString("description"),
                                    "Precio UU: S/." +
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

                        productAdapter = new ProductAdapter(getActivity(), products);
                        recyclerView.setAdapter(productAdapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> {
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
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(arrayRequest);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
