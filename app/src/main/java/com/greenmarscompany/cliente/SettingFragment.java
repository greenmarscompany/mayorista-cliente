package com.greenmarscompany.cliente;

import android.content.Context;
import android.net.Uri;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.Session;
import com.greenmarscompany.cliente.persistence.entity.Acount;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class SettingFragment extends androidx.fragment.app.Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    TextInputEditText Dni;
    TextInputEditText Nombre;
    TextInputEditText Email;
    TextInputEditText Direccion;
    TextInputEditText Telefono1;
    TextInputEditText Telefono2;
    Button Editar, Guardar;

    private OnFragmentInteractionListener mListener;

    //--Variables temporales
    int idUsuarioTemp = 2;


    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
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
        android.view.View view = inflater.inflate(R.layout.fragment_setting, container, false);


        Dni = view.findViewById(R.id.DNIPerfil);
        Nombre = view.findViewById(R.id.NombrePerfil);
        Email = view.findViewById(R.id.EmailPerfil);
        Direccion = view.findViewById(R.id.DireccionPerfil);
        Telefono1 = view.findViewById(R.id.TelefonoPerfil);
        Telefono2 = view.findViewById(R.id.TelefonoPerfil1);
        Editar = view.findViewById(R.id.ButtonEditarPerfil);
        Guardar = view.findViewById(R.id.ButtonGuardarPerfil);

        Editar.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(android.view.View v) {
                Nombre.setEnabled(true);
                Email.setEnabled(true);
                Direccion.setEnabled(true);
                Telefono1.setEnabled(true);
                Telefono2.setEnabled(true);
                Guardar.setEnabled(true);
                Guardar.setBackgroundResource(R.drawable.custom_button);
            }
        });
        Guardar.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                actualizarInfoUsuario(
                        Objects.requireNonNull(Nombre.getText()).toString(),
                        Objects.requireNonNull(Email.getText()).toString(),
                        Objects.requireNonNull(Telefono1.getText()).toString(),
                        Objects.requireNonNull(Telefono2.getText()).toString()
                );

                Nombre.setEnabled(false);
                Email.setEnabled(false);
                Direccion.setEnabled(false);
                Telefono1.setEnabled(false);
                Telefono2.setEnabled(false);
                Guardar.setEnabled(false);
                Guardar.setBackgroundResource(R.drawable.custom_button);


            }
        });

        llenarInformacionUsuario();

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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void llenarInformacionUsuario() {

        Session session = new Session(getContext());
        int token = session.getToken();

        if (token != 0) {
            Acount cuenta = DatabaseClient.getInstance(getContext())
                    .getAppDatabase()
                    .getAcountDao()
                    .getUser(token);
            if (cuenta != null) {
                //Llenar la informacion
                Dni.setText(cuenta.getNumDocumento());
                Nombre.setText(cuenta.getNombre());
                Email.setText(cuenta.getEmail());
                Direccion.setText(cuenta.getDireccion());
                Telefono1.setText(cuenta.getPhoneOne());
                Telefono2.setText(cuenta.getPhoneTwo());
            }
        }

    }

    private void actualizarInfoUsuario(String nombre, String email, String phone1, String phone2) {
        Session session = new Session(getContext());
        int token = session.getToken();

        if (nombre.length() > 0 && email.length() > 0 && phone1.length() > 0) {
            if (token != 0) {
                Acount cuenta = DatabaseClient.getInstance(getContext())
                        .getAppDatabase()
                        .getAcountDao()
                        .getUser(token);

                cuenta.setNombre(nombre);
                cuenta.setEmail(email);
                cuenta.setPhoneOne(phone1);
                cuenta.setPhoneTwo(phone2);

                DatabaseClient.getInstance(getContext())
                        .getAppDatabase()
                        .getAcountDao()
                        .updateUser(cuenta);

                Toast.makeText(getContext(), "Editado con éxito", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getContext(), "ERROR :(",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "Por favor llene los campos (Telefono 2 - opcional)",
                    Toast.LENGTH_LONG).show();
        }
    }
}
