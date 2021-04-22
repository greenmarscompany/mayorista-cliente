package com.greenmarscompany.cliente;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.Session;
import com.greenmarscompany.cliente.persistence.entity.Acount;
import com.greenmarscompany.cliente.pojo.Mensaje;
import com.greenmarscompany.cliente.utils.GpsUtils;
import com.greenmarscompany.cliente.utils.SliderAdapter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Objects;

public class NewMapsFragment extends androidx.fragment.app.Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private static final String TAG = "GAS";
    private static final int DEFAULT_ZOOM = 16;
    private Location mLastKnownLocation;
    private GoogleMap map;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private Marker currentMarker;
    private LatLng startLng;

    private Context context;

    private LatLng point_move;
    private Socket socket;
    // private int RamdomHelp = -1;
    private RadioGroup groupMetodo, groupMetodoEfectivo;
    private RadioButton voucher;
    private android.widget.TextView lblDireccion;
    private OnFragmentInteractionListener mListener;
    private ArrayList<Mensaje> ListMessage;
    // private ImageView imgvMensajes;
    private int Contador = 0;
    private SliderAdapter sliderAdapter;
    SliderView sliderView;

    public NewMapsFragment() {
        // Required empty public constructor
    }

    public static NewMapsFragment newInstance(String param1, String param2) {
        NewMapsFragment fragment = new NewMapsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             android.os.Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final android.view.View view = inflater.inflate(R.layout.fragment_new_maps, container, false);
        lblDireccion = view.findViewById(R.id.lblDireccion);
        // imgvMensajes = view.findViewById(R.id.imgvMensajes);
        // imgvMensajes.setBackground(null);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.google_map_pedidos);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Button btnSiguiente = view.findViewById(R.id.btnSiguienteComprar);
        btnSiguiente.setOnClickListener(v -> {
            FragmentManager manager = getFragmentManager();
            assert manager != null;
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.navigationContainer, new MainFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
        //-- Slider de imagenes
        sliderView = view.findViewById(R.id.imageSlider);

        Listar();

        return view;
    }

    private void Listar() {
        if (getContext() == null)
            Toast.makeText(getContext(), "That didn't work!", Toast.LENGTH_SHORT).show();

        RequestQueue queue = Volley.newRequestQueue(getContext());
        StringBuilder sb = new StringBuilder();
        sb.append(Global.URL_HOST + "/messages/cliente");
        String url = sb.toString();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        ListMessage = gson.fromJson(response, new TypeToken<ArrayList<Mensaje>>() {
                        }.getType());

                        sliderAdapter = new SliderAdapter(context);
                        for (Mensaje value : ListMessage) {
                            Mensaje mensaje = new Mensaje();
                            mensaje.setImage(Global.URL_BASE + "/" + value.getImage());
                            sliderAdapter.addItem(mensaje);
                            System.out.println(Global.URL_BASE + "/" + value.getImage());
                        }

                        sliderView.setSliderAdapter(sliderAdapter);
                        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
                        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
                        sliderView.setScrollTimeInSec(4); //set scroll delay in seconds :
                        sliderView.startAutoCycle();


                        /*if (ListMessage.size() > 0) {
                            final Handler handler = new Handler();

                            handler.postDelayed(new Runnable() {
                                public void run() {

                                    Picasso.get().load(ListMessage.get(Contador).getImage()).into(imgvMensajes);
                                    //   RamdomHelp = randomIndex;
                                    Contador++;
                                    if (ListMessage.size() == Contador)
                                        Contador = 0;
                                    if (ListMessage.size() > 1)
                                        handler.postDelayed(this, 5000);

                                }
                            }, 100);
                        }*/
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "That didn't work!", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
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

    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnMarkerClickListener(this);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        getLocationPermission();
        getDeviceLocation();
        updateLocationUI();

        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                if (currentMarker != null) {
                    currentMarker.remove();
                }

                LatLng position = map.getCameraPosition().target;
                startLng = new LatLng(position.latitude, position.longitude);
                String direccion = getStringAddress(startLng.latitude, startLng.longitude);
                int token = new Session(getContext()).getToken();
                //Todo actualizar
                Acount acount = DatabaseClient.getInstance(getContext())
                        .getAppDatabase()
                        .getAcountDao()
                        .getUser(token);
                acount.setLatitud(startLng.latitude);
                acount.setLongitud(startLng.longitude);
                DatabaseClient.getInstance(getContext())
                        .getAppDatabase()
                        .getAcountDao()
                        .updateUser(acount);
                //     MapSelection.latitud =startLng.latitude;
                //  MapSelection.longitud= startLng.longitude;

                lblDireccion.setText(direccion);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @androidx.annotation.NonNull String[] permissions,
                                           @androidx.annotation.NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;


            }
        }
        updateLocationUI();
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                if (currentMarker != null) currentMarker.remove();

                                LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                                startLng = latLng;
                                String direccion = getStringAddress(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                                int token = new Session(getContext()).getToken();
                                Acount acount = DatabaseClient.getInstance(getContext())
                                        .getAppDatabase()
                                        .getAcountDao()
                                        .getUser(token);
                                acount.setLatitud(startLng.latitude);
                                acount.setLongitud(startLng.longitude);
                                DatabaseClient.getInstance(getContext())
                                        .getAppDatabase()
                                        .getAcountDao()
                                        .updateUser(acount);
                                //   MapSelection.latitud =mLastKnownLocation.getLatitude();
                                //   MapSelection.longitud= mLastKnownLocation.getLongitude();
                                lblDireccion.setText(direccion);

                            } else {
                                new GpsUtils(Objects.requireNonNull(getActivity())).turnGPSOn(new GpsUtils.onGpsListener() {
                                    @Override
                                    public void gpsStatus(boolean isGPSEnable) {
                                        if (isGPSEnable) {
                                            //---------------------------------
                                            updateLocationUI();
                                            getDeviceLocation();
                                        }
                                    }
                                });
                            }
                        } else {
                            android.util.Log.d(TAG, "Current location is null. Using defaults.");
                            android.util.Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            } else {
                updateLocationUI();
                getDeviceLocation();
            }
        } catch (SecurityException e) {
            android.util.Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()).getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }


    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            android.util.Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
        }
    }

    private String getStringAddress(Double lat, Double lng) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getContext(), java.util.Locale.getDefault());
        try {
            java.util.List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                if (strReturnedAddress.toString().contains(","))
                    strAdd = strReturnedAddress.toString().substring(0, strReturnedAddress.toString().lastIndexOf(","));
                android.util.Log.w(TAG, strReturnedAddress.toString());
            } else {
                android.util.Log.w(TAG, "No Address returned!");
            }
            if (strAdd.contains(","))
                return strAdd.substring(0, strAdd.lastIndexOf(","));
            else
                return "";

        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.w(TAG, "Canont get Address!");
            LatLng cusco = new LatLng(-13.5179145, -71.9771895);
            map.moveCamera(CameraUpdateFactory.newLatLng(cusco));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-13.5179145, -71.9771895), 16));

            return "No se le puede ubicar, por favor active su GPS";
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
