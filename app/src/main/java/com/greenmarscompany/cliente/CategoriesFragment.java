package com.greenmarscompany.cliente;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import com.greenmarscompany.cliente.login.LoginActivity;
import com.greenmarscompany.cliente.persistence.Session;
import com.greenmarscompany.cliente.pojo.Categories;
import com.greenmarscompany.cliente.utils.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;

public class CategoriesFragment extends androidx.fragment.app.Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    private CategoriesAdapter categoriesAdapter;
    private RecyclerView recyclerView;
    ArrayList<Categories> categories;

    private android.view.View last_view = null;

    //--
    String urlBase = Global.URL_BASE;

    public CategoriesFragment() {
        // Required empty public constructor
    }

    public static CategoriesFragment newInstance(String param1, String param2) {
        CategoriesFragment fragment = new CategoriesFragment();
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
        // Inflate the layout for this frag
        android.view.View view = inflater.inflate(R.layout.fragment_categories, container, false);
        recyclerView = view.findViewById(R.id.CategoriesContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

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

    // Llenado de datos categorias;
    public void llenarDatos() {
        categories = new ArrayList<>();

        //--
        String url = this.urlBase + "/api/categories";
        JSONArray jsonArray = new JSONArray();
        JsonArrayRequest arrayRequest =
                new JsonArrayRequest(Request.Method.GET, url, jsonArray, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object = response.getJSONObject(i);
                                String imagen_url = urlBase + object.getString("image");
                                Categories categoria = new Categories(
                                        object.getInt("id"),
                                        object.getString("name"),
                                        imagen_url);
                                if (!categoria.getName().equals("Gas"))
                                    categories.add(categoria);
                                else
                                    categories.add(0, categoria);
                            }

                            // Adaptador => para la persistencia de datos
                            categoriesAdapter = new CategoriesAdapter(categories);
                            recyclerView.setAdapter(categoriesAdapter);

                            categoriesAdapter.setOnClickListener(new android.view.View.OnClickListener() {

                                BrandsFragment brandsFragment;

                                @Override
                                public void onClick(android.view.View v) {
                                    if (getActivity() == null) return;

                                    android.os.Bundle bundle = new android.os.Bundle();
                                    bundle.putInt("idCategory", categories.get(recyclerView.getChildAdapterPosition(v)).getId());

                                    highlight(v);

                                    FragmentManager manager = getActivity().getSupportFragmentManager();
                                    FragmentTransaction transaction = manager.beginTransaction();
                                    brandsFragment = new BrandsFragment();
                                    brandsFragment.setArguments(bundle);
                                    String categoriesTitle = categories.get(recyclerView.getChildAdapterPosition(v)).getName();
                                    Toast.makeText(getContext(), categoriesTitle, Toast.LENGTH_SHORT).show();
                                    transaction.replace(R.id.mainContainer, brandsFragment);
                                    transaction.addToBackStack(null);
                                    transaction.commit();
                                }
                            });
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
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

    public void highlight(android.view.View v) {
        if (last_view == null) {
            last_view = v;
            //v.setBackgroundResource(R.drawable.custom_button_highlight);
            CardView linear = v.findViewById(R.id.CategoriesCardView);
            linear.setBackgroundResource(R.drawable.custom_button_highlight);
            android.widget.TextView title = v.findViewById(R.id.CategoriesName);
            title.setTextColor(getResources().getColor(R.color.frii_Background));
        } else {
            CardView linear = last_view.findViewById(R.id.CategoriesCardView);
            linear.setBackgroundResource(R.drawable.custom_button_highlight_white);
            //last_view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            android.widget.TextView last_text = last_view.findViewById(R.id.CategoriesName);
            last_text.setTextColor(android.graphics.Color.BLACK);
            last_view = v;
            //last_view.setBackgroundResource(R.drawable.custom_button_highlight);
            CardView lina = last_view.findViewById(R.id.CategoriesCardView);
            lina.setBackgroundResource(R.drawable.custom_button_highlight);
            android.widget.TextView title = last_view.findViewById(R.id.CategoriesName);
            title.setTextColor(getResources().getColor(R.color.frii_Background));
        }


    }


}
