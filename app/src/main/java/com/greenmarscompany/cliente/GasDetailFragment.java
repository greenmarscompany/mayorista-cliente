package com.greenmarscompany.cliente;

import android.content.Context;
import android.net.Uri;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.Session;
import com.greenmarscompany.cliente.persistence.entity.Acount;
import com.greenmarscompany.cliente.pojo.ProductStaff;
import com.greenmarscompany.cliente.utils.MyJsonArrayRequest;
import com.greenmarscompany.cliente.utils.VolleySingleton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GasDetailFragment extends androidx.fragment.app.Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    public int product_id = 0;
    public String Description = "";
    private OnFragmentInteractionListener mListener;

    private GasProductDetailAdapter gasProductDetailAdapter;
    private RecyclerView recyclerView;
    ArrayList<ProductStaff> products;

    public GasDetailFragment() {
        // Required empty public constructor
    }

    public static GasDetailFragment newInstance(String param1, String param2) {
        GasDetailFragment fragment = new GasDetailFragment();
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
        android.view.View view = inflater.inflate(R.layout.fragment_gas_detail, container, false);
        recyclerView = view.findViewById(R.id.ProductGasDetailContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
/*
        products=new ArrayList<>();
        products.add(new Product(1,"solgas de 5kilos ","",2,2,2,"","",""));
        products.add(new Product(2,"solgas de 5kilos ","",2,2,2,"","",""));
        products.add(new Product(3,"solgas de 5kilos ","",2,2,2,"","",""));

        gasProductDetailAdapter=new GasProductDetailAdapter(products);
        recyclerView.setAdapter(gasProductDetailAdapter);
*/
        ListarDatos();
        return view;

    }

    public void onResume() {
        super.onResume();
        ListarDatos();

    }

    public void ListarDatos() {
        final int token = new Session(getContext()).getToken();
        final Acount acount = DatabaseClient.getInstance(getContext())
                .getAppDatabase()
                .getAcountDao()
                .getUser(token);
        JSONObject object = new JSONObject();
        try {
            object.put("product_id", product_id);
            object.put("latitude", acount.getLatitud());
            object.put("longitude", acount.getLongitud());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Global.URL_HOST + "/products/staff";
        MyJsonArrayRequest objectRequest = new MyJsonArrayRequest(Request.Method.POST,
                url,
                object,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            recyclerView.setAdapter(null);
                            Gson gson = new Gson();
                            products = gson.fromJson(response.toString(), new TypeToken<ArrayList<ProductStaff>>() {
                            }.getType());
                            for (ProductStaff item : products) {
                                item.getProductID().setImage(Global.URL_BASE + "/" + item.getProductID().getImage());

                                item.getProductID().setDescription(Description);
                            }
                            if (products.size() == 0) {
                                Toast.makeText(getContext(), "No hay proveedores con ese producto", Toast.LENGTH_SHORT).show();
                            }
                            gasProductDetailAdapter = new GasProductDetailAdapter(products);
                            recyclerView.setAdapter(gasProductDetailAdapter);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                });

        VolleySingleton.getInstance(getContext()).addToRequestQueue(objectRequest);
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
