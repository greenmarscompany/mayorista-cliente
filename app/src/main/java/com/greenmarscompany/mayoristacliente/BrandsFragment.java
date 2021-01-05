package com.greenmarscompany.mayoristacliente;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;

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
import com.greenmarscompany.mayoristacliente.Login.LoginActivity;
import com.greenmarscompany.mayoristacliente.persistence.Session;
import com.greenmarscompany.mayoristacliente.pojo.Brands;
import com.greenmarscompany.mayoristacliente.utils.VolleySingleton;
import com.todkars.shimmer.ShimmerRecyclerView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;


public class BrandsFragment extends androidx.fragment.app.Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //--
    private final String urlBase = Global.URL_BASE;

    private BrandsAdapter brandsAdapter;
    private ShimmerRecyclerView recyclerView;
    private ArrayList<Brands> brandsList;


    public BrandsFragment() {
        // Required empty public constructor
    }

    public static BrandsFragment newInstance(String param1, String param2) {
        BrandsFragment fragment = new BrandsFragment();
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

        //Válidar información del usuario
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
        android.view.View view = inflater.inflate(R.layout.fragment_brands, container, false);
        brandsList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.BrandsContainer);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
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

    // Llenar datos
    private void llenarDatos() {
        final java.util.List<Brands> gas_categories = new ArrayList<>();
        android.os.Bundle bundle = this.getArguments();
        String url;

        if (bundle != null) {
            url = this.urlBase + "/api/markes/" + bundle.getInt("idCategory");
            if (bundle.getInt("idCategory") == 2) {
                gas_categories.add(new Brands(1, "Gas Normal", "", urlBase + "/media/images/gas-regular.png"));
                gas_categories.add(new Brands(2, "Gas Premium", "", urlBase + "/media/images/gas-premium.png"));
                gas_categories.add(new Brands(3, "Camion", "", urlBase + "/media/images/gas-cisterna.png"));

                brandsAdapter = new BrandsAdapter(gas_categories);
                recyclerView.setAdapter(brandsAdapter);

                brandsAdapter.setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View v) {
                        if (getActivity() == null) return;

                        FragmentManager manager = getActivity().getSupportFragmentManager();
                        FragmentTransaction transaction = manager.beginTransaction();
                        String brandsTitle = gas_categories.get(recyclerView.getChildAdapterPosition(v)).getName();
                        brandsTitle = brandsTitle.toLowerCase();
                        Toast.makeText(getContext(), brandsTitle, Toast.LENGTH_SHORT).show();
                        if (brandsTitle.equals("gas normal") || brandsTitle.equals("gas premium")) {
                            GasFragment gasNewFragment = new GasFragment();
                            android.os.Bundle b = new android.os.Bundle();
                            if (brandsTitle.equals("gas normal")) {
                                b.putString("type", "gas-normal");
                            } else {
                                b.putString("type", "gas-premium");

                            }
                            gasNewFragment.setArguments(b);
                            transaction.replace(R.id.mainContainer, gasNewFragment);
                        } else {
                            GasCisternaFragment gasCisternaFragment = new GasCisternaFragment();
                            transaction.replace(R.id.mainContainer, gasCisternaFragment);
                        }
                        transaction.addToBackStack(null);
                        transaction.commit();

                    }
                });

                return;
            }
        } else {
            url = this.urlBase + "/api/markes";
            brandsList.add(new Brands(1, "Gas Normal", "", urlBase + "/media/images/gas-regular.png"));
            brandsList.add(new Brands(2, "Gas Premium", "", urlBase + "/media/images/gas-premium.png"));
            brandsList.add(new Brands(3, "Camion", "", urlBase + "/media/images/gas-cisterna.png"));
        }

        JSONArray jsonArray = new JSONArray();
        JsonArrayRequest arrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, jsonArray, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object = response.getJSONObject(i);
                                String imagen_url = urlBase + object.getString("image");
                                Brands brands = new Brands(
                                        Integer.parseInt(object.getString("id")),
                                        object.getString("name"),
                                        "",
                                        imagen_url);

                                brandsList.add(brands);
                            }

                            brandsAdapter = new BrandsAdapter(brandsList);
                            recyclerView.setAdapter(brandsAdapter);

                            // shimmerFrameLayout.setVisibility(View.GONE);
                            recyclerView.hideShimmer();
                            brandsAdapter.setOnClickListener(new android.view.View.OnClickListener() {
                                @Override
                                public void onClick(android.view.View v) {

                                    if (getActivity() == null) return;
                                    FragmentManager manager = getActivity().getSupportFragmentManager();
                                    FragmentTransaction transaction = manager.beginTransaction();

                                    String brandsTitle = brandsList.get(recyclerView.getChildAdapterPosition(v)).getName();
                                    brandsTitle = brandsTitle.toLowerCase();
                                    if (brandsTitle.equals("gas normal") || brandsTitle.equals("gas premium")) {
                                        GasFragment gasNewFragment = new GasFragment();
                                        android.os.Bundle b = new android.os.Bundle();
                                        if (brandsTitle.equals("gas normal")) {
                                            b.putString("type", "gas-normal");
                                        } else {
                                            b.putString("type", "gas-premium");
                                        }
                                        gasNewFragment.setArguments(b);
                                        transaction.replace(R.id.mainContainer, gasNewFragment);
                                        transaction.addToBackStack(null);
                                        transaction.commit();
                                    } else if (brandsTitle.equals("camion")) {
                                        GasCisternaFragment gasCisternaFragment = new GasCisternaFragment();
                                        transaction.replace(R.id.mainContainer, gasCisternaFragment);
                                        transaction.addToBackStack(null);
                                        transaction.commit();
                                    } else {
                                        android.os.Bundle b = new android.os.Bundle();
                                        b.putInt("IdMarke", brandsList.get(recyclerView.getChildAdapterPosition(v)).getId());
                                        ProductsFragment productsFragment = new ProductsFragment();
                                        productsFragment.setArguments(b);


                                        transaction.replace(R.id.mainContainer, productsFragment);
                                        transaction.addToBackStack(null);
                                        transaction.commit();

                                    }

                                }
                            });
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

}
