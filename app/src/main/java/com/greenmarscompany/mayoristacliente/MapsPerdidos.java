package com.greenmarscompany.mayoristacliente;

import android.content.Context;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;

import android.widget.Toast;

import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.greenmarscompany.mayoristacliente.utils.DirectionJSONParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;


public class MapsPerdidos extends androidx.fragment.app.Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private GoogleMap map;
    private Polyline mPolyline;
    private ArrayList<LatLng> mMarkedPoints;

    private static final int DEFAULT_ZOOM = 16;

    private OnFragmentInteractionListener mListener;

    public MapsPerdidos() {

    }

    public static MapsPerdidos newInstance(String param1, String param2) {
        MapsPerdidos fragment = new MapsPerdidos();
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
        android.view.View view = inflater.inflate(R.layout.fragment_mapsperdidos, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.google_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        mMarkedPoints = new ArrayList<>();

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

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (getContext() == null) return;
        map = googleMap;

        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

            UiSettings uiSettings = map.getUiSettings();
            uiSettings.setZoomControlsEnabled(true);
            map.setMyLocationEnabled(true);

            llenarDatos();
        } else {
            Toast.makeText(getContext(), "Error de permisos", Toast.LENGTH_LONG).show();
        }


    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    private void llenarDatos() {
        android.os.Bundle bundle = getArguments();
        assert bundle != null;
        LatLng companyDirection = bundle.getParcelable("DCOMPANY");
        LatLng clientDirection = bundle.getParcelable("DCLIENT");

        mMarkedPoints = new ArrayList<>();


        assert companyDirection != null;
        LatLng company = new LatLng(companyDirection.latitude, companyDirection.longitude);

        assert clientDirection != null;
        LatLng destination = new LatLng(clientDirection.latitude, clientDirection.longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, DEFAULT_ZOOM));

        map.addMarker(new MarkerOptions()
                .position(company)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("Local de distribución")
        );

        Geocoder geocoder = new Geocoder(getContext(), java.util.Locale.getDefault());
        try {
            java.util.List<Address> addresses = geocoder.getFromLocation(destination.latitude, destination.longitude, 1);

            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                map.addMarker(new MarkerOptions()
                        .position(destination)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title("Mi direcion: " + address.getAddressLine(0))
                );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        mMarkedPoints.add(company);
        mMarkedPoints.add(destination);
        drawRoute(company, destination);
    }


    private void drawRoute(LatLng mOrigin, LatLng mDestination) {

        String url = getURL(mOrigin, mDestination);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
    }


    // Obtener la ruta
    private String getURL(LatLng origin, LatLng destination) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        String key = "key=" + getString(R.string.google_maps_key);
        String parameters = str_origin + "&" + str_dest + "&" + key;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    private String downloadURL(String URL) throws IOException {
        String data = "";
        java.io.InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            java.net.URL url = new java.net.URL(URL);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } finally {
            iStream.close();
            urlConnection.disconnect();

        }

        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadURL(url[0]);
                android.util.Log.d("Download Task", "DownloadTask: " + data);
            } catch (IOException e) {
                android.util.Log.d("Background Task ", e.toString());
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, java.util.List<java.util.List<HashMap<String, String>>>> {

        @Override
        protected java.util.List<java.util.List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject object;
            java.util.List<java.util.List<HashMap<String, String>>> routes = null;
            try {
                object = new JSONObject(jsonData[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(java.util.List<java.util.List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                java.util.List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(android.graphics.Color.RED);
            }

            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = map.addPolyline(lineOptions);

            } else
                Toast.makeText(getContext(), "No route is found", Toast.LENGTH_LONG).show();
        }
    }
}
