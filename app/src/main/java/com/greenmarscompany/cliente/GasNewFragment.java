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
import com.android.volley.toolbox.JsonObjectRequest;
import com.greenmarscompany.cliente.pojo.Product;
import com.greenmarscompany.cliente.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

// TODO NO ESTA EN USO
public class GasNewFragment extends androidx.fragment.app.Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    String urlBase = Global.URL_BASE;
    private OnFragmentInteractionListener mListener;


    private GasProductAdapterNew gasProductAdapter;
    private RecyclerView recyclerView;
    ArrayList<Product> products;


    public GasNewFragment() {
    }

    public static GasNewFragment newInstance(String param1, String param2) {
        GasNewFragment fragment = new GasNewFragment();
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
        android.view.View view = inflater.inflate(R.layout.fragment_gas_new, container, false);
        recyclerView = view.findViewById(R.id.ProductGasContainer_new);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        llenarDatos();

        return view;
    }

    private void llenarDatos() {
        products = new ArrayList<>();
        android.os.Bundle b = this.getArguments();
        String type = "";
        if (b != null) {
            type = b.getString("type");
        }
        String url = Global.URL_HOST + "/product/gas/" + type;
        JSONObject jsonArray = new JSONObject();
        final String finalType = type;
        JsonObjectRequest arrayRequest =
                new JsonObjectRequest(Request.Method.GET, url, jsonArray, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray array = response.getJSONArray("data");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                String imagen_url = urlBase + object.getString("image");
                                Product product = new Product(
                                        Integer.parseInt(object.getString("id")),
                                        object.getJSONObject("marke_id").getString("name") + " " +
                                                object.getJSONObject("detail_measurement_id").getString("name"),
                                        "",
                                        Float.parseFloat(object.getString("unit_price")),
                                        Float.parseFloat(object.getString("measurement")),

                                        1,
                                        imagen_url,
                                        finalType,
                                        object.getJSONObject("marke_id").getString("name")
                                        , Integer.parseInt(object.getJSONObject("marke_id").getString("id")));

                                products.add(product);
                            }

                            gasProductAdapter = new GasProductAdapterNew(products, finalType);
                            recyclerView.setAdapter(gasProductAdapter);
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
                                Toast.makeText(getContext(), "No hay productos :(", Toast.LENGTH_SHORT).show();

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
