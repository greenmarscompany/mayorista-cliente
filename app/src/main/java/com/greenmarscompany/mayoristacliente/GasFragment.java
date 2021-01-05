package com.greenmarscompany.mayoristacliente;

import android.content.Intent;
import android.net.Uri;

import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.greenmarscompany.mayoristacliente.Login.LoginActivity;
import com.greenmarscompany.mayoristacliente.persistence.Session;
import com.greenmarscompany.mayoristacliente.pojo.Product;
import com.greenmarscompany.mayoristacliente.utils.VolleySingleton;
import com.todkars.shimmer.ShimmerRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;

public class GasFragment extends androidx.fragment.app.Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    private GasProductAdapter gasProductAdapter;
    private ShimmerRecyclerView recyclerView;
    ArrayList<Product> products;

    //--
    String urlBase = Global.URL_BASE;

    public GasFragment() {
        // Required empty public constructor
    }

    public static GasFragment newInstance(String param1, String param2) {
        GasFragment fragment = new GasFragment();
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
    }

    @Override
    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container,
                                          android.os.Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        android.view.View view = inflater.inflate(R.layout.fragment_gas, container, false);
        recyclerView = view.findViewById(R.id.ProductGasContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemViewType((type, position) -> {
            if (type == ShimmerRecyclerView.LAYOUT_GRID) {
                return position % 2 == 0
                        ? R.layout.list_item_shimmer_grid
                        : R.layout.list_item_shimmer_grid_alternate;
            }
            return position % 2 == 0
                    ? R.layout.list_item_shimmer
                    : R.layout.list_item_shimmer_alternate;
        });
        recyclerView.showShimmer();

        // llenarDatos();
        return view;
    }

    public void onResume() {
        super.onResume();
        llenarDatos();

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

                                        object.getInt("measurement"),
                                        1,
                                        imagen_url,
                                        finalType,
                                        object.getJSONObject("marke_id").getString("name"),
                                        object.getJSONObject("marke_id").getInt("id")
                                );

                                products.add(product);
                            }

                            gasProductAdapter = new GasProductAdapter(products);
                            recyclerView.setAdapter(gasProductAdapter);
                            recyclerView.hideShimmer();

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
}
