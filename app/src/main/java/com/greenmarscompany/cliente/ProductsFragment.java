package com.greenmarscompany.cliente;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.greenmarscompany.cliente.login.LoginActivity;
import com.greenmarscompany.cliente.persistence.Session;
import com.greenmarscompany.cliente.pojo.Product;
import com.greenmarscompany.cliente.utils.VolleySingleton;
import com.todkars.shimmer.ShimmerRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;

public class ProductsFragment extends androidx.fragment.app.Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ProductAdapter productAdapter;
    private ShimmerRecyclerView recyclerView;
    ArrayList<Product> products;

    //--
    String urlBase = Global.URL_BASE;
    private boolean isRestart = false;

    public ProductsFragment() {
        // Required empty public constructor
    }

    public static ProductsFragment newInstance(String param1, String param2) {
        ProductsFragment fragment = new ProductsFragment();
        android.os.Bundle args = new android.os.Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (isRestart && products != null) {
            productAdapter.products = products;
            productAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final android.view.View view = inflater.inflate(R.layout.fragment_products, container, false);

        recyclerView = view.findViewById(R.id.ProductsContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemViewType((type, position) -> {
            switch (type) {
                case ShimmerRecyclerView.LAYOUT_GRID:
                    return position % 2 == 0
                            ? R.layout.list_item_shimmer_grid
                            : R.layout.list_item_shimmer_grid_alternate;
                default:
                case ShimmerRecyclerView.LAYOUT_LIST:
                    return position == 0 || position % 2 == 0
                            ? R.layout.list_item_shimmer
                            : R.layout.list_item_shimmer_alternate;
            }
        });
        recyclerView.showShimmer();
        llenarDatos();

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        isRestart = true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
                new JsonArrayRequest(Request.Method.GET, url, jsonArray, response -> {
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
                                    object.getJSONObject("marke_id").getString("name"),
                                    object.getJSONObject("marke_id").getInt("id")
                            );

                            products.add(product);
                        }

                        productAdapter = new ProductAdapter(getActivity(), products);
                        recyclerView.setAdapter(productAdapter);
                        recyclerView.hideShimmer();
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
}
