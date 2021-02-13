package com.greenmarscompany.mayoristacliente.login;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.greenmarscompany.mayoristacliente.Global;
import com.greenmarscompany.mayoristacliente.R;
import com.greenmarscompany.mayoristacliente.utils.CustomToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


public class ResetPasswordFragment extends androidx.fragment.app.Fragment implements android.view.View.OnClickListener {
    private android.view.View view;

    private EditText txtCodigo;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private android.widget.TextView submit, back;

    public ResetPasswordFragment() {

    }

    @Override
    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container,
                                          android.os.Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.reset_password, container,
                false);
        initViews();
        setListeners();
        return view;
    }

    // Initialize the views
    private void initViews() {
        txtCodigo = view.findViewById(R.id.txtCodigo);
        txtPassword = view.findViewById(R.id.txtPassword);
        txtConfirmPassword = view.findViewById(R.id.txtConfirmPassword);
        submit = view.findViewById(R.id.btnResetearPassword);
        back = view.findViewById(R.id.backToLoginBtn);
    }

    // Set Listeners over buttons
    private void setListeners() {
        back.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(android.view.View v) {
        switch (v.getId()) {
            case R.id.backToLoginBtn:
                Objects.requireNonNull(getActivity()).onBackPressed();
                Objects.requireNonNull(getActivity()).finish();
                break;

            case R.id.btnResetearPassword:
                submitButtonTask();
                break;

        }

    }

    private void submitButtonTask() {
        String password = txtPassword.getText().toString();
        String codigo = txtCodigo.getText().toString();
        String confirmPassword = txtConfirmPassword.getText().toString();
        if (password.equals("") || password.length() == 0 ||
                confirmPassword.equals("") || confirmPassword.length() == 0 ||
                codigo.equals("") || codigo.length() == 0)

            new CustomToast().Show_Toast(Objects.requireNonNull(getActivity()), view,
                    "(*) Los campos de código y contraseña son requeridos");
        else if (!password.equals(confirmPassword)) {
            new CustomToast().Show_Toast(Objects.requireNonNull(getActivity()), view,
                    "(*) Los campos de contraseña no coinciden");
        } else {


            RequestQueue queue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("password", password);
                jsonObject.put("token", codigo);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String urlBase = Global.URL_BASE;
            String url = urlBase + "/api/password_reset/confirm/";
            JsonObjectRequest jsonObjectRequest =
                    new JsonObjectRequest(Request.Method.POST, url, jsonObject, response -> {
                        try {
                            String status = response.getString("status");
                            if (status.equals("OK")) {
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                startActivity(intent);
                                Toast.makeText(getContext(), "Contraseña cambiada correctamente :)", Toast.LENGTH_LONG)
                                        .show();
                                Objects.requireNonNull(getActivity()).finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> {
                        android.util.Log.d("Volley get", "error voley" + error.toString());
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                JSONObject obj = new JSONObject(res);
                                String msj = obj.getString("message");
                                android.util.Log.d("Mayorista", "onErrorResponse: " + msj);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            queue.add(jsonObjectRequest);
        }
    }
}
