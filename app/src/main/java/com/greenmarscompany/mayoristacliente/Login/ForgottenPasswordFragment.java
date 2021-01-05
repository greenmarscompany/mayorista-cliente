package com.greenmarscompany.mayoristacliente.Login;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.greenmarscompany.mayoristacliente.Global;
import com.greenmarscompany.mayoristacliente.R;
import com.greenmarscompany.mayoristacliente.utils.CustomToast;
import com.greenmarscompany.mayoristacliente.utils.Utilitarios;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgottenPasswordFragment extends androidx.fragment.app.Fragment implements android.view.View.OnClickListener {

    private android.view.View view;
    private FragmentManager fragmentManager;

    private EditText emailId;
    private android.widget.TextView submit, back;

    public ForgottenPasswordFragment() {

    }

    @Override
    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container,
                                          android.os.Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.forgottenpassword_layout, container, false);

        back = view.findViewById(R.id.BackLogin);
        emailId = view.findViewById(R.id.emailId);
        submit = view.findViewById(R.id.EnviarForgottenPassword);
        fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        setListeners();
        return view;
    }


    // Set Listeners over buttons
    private void setListeners() {
        back.setOnClickListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(android.view.View v) {
        switch (v.getId()) {
            case R.id.EnviarForgottenPassword:
                submitButtonTask();
                break;
            case R.id.BackLogin:
                Objects.requireNonNull(getActivity()).onBackPressed();
                break;

        }
    }

    private void submitButtonTask() {
        String getEmailId = emailId.getText().toString();
        Pattern p = Pattern.compile(Utilitarios.regEx);
        Matcher m = p.matcher(getEmailId);
        if (getEmailId.equals("") || getEmailId.length() == 0) {
            new CustomToast().Show_Toast(getActivity(), view,
                    "Ingrese su correo.");
        } else if (!m.find()) {
            new CustomToast().Show_Toast(getActivity(), view,
                    "Tu correo es invalido.");
        } else {

            RequestQueue queue = Volley.newRequestQueue(getContext());
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email", getEmailId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String urlBase = Global.URL_BASE;
            String url = urlBase + "/api/password_reset/";
            JsonObjectRequest jsonObjectRequest =
                    new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String status = response.getString("status");
                                if (status.equals("OK")) {
                                    fragmentManager
                                            .beginTransaction()
                                            .setCustomAnimations(R.anim.right_enter, R.anim.left_out)
                                            .replace(R.id.frameContainer,
                                                    new ResetPasswordFragment()).commit();
                                    Toast.makeText(getContext(), "Por favor revise su bandeja de entrada o spam", Toast.LENGTH_LONG)
                                            .show();
                                }
                            } catch (JSONException e) {
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
                                    System.out.println(res);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
            queue.add(jsonObjectRequest);
        }
    }
}
