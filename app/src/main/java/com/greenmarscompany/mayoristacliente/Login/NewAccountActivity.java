package com.greenmarscompany.mayoristacliente.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.greenmarscompany.mayoristacliente.Global;
import com.greenmarscompany.mayoristacliente.MainActivity;
import com.greenmarscompany.mayoristacliente.R;
import com.greenmarscompany.mayoristacliente.persistence.DatabaseClient;
import com.greenmarscompany.mayoristacliente.persistence.Session;
import com.greenmarscompany.mayoristacliente.persistence.entity.Acount;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class NewAccountActivity extends AppCompatActivity {

    private TextInputEditText txtName, txtNumDocumento, txtCorreo, txtTelefono, txtDireccion,
            txtPass, txtPassRepeat;
    //--
    public String baseUrl = Global.URL_BASE;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        txtName = findViewById(R.id.txtNombre);
        txtNumDocumento = findViewById(R.id.txtDni);
        txtCorreo = findViewById(R.id.txtEmail);
        txtTelefono = findViewById(R.id.txtTelefono);
        txtDireccion = findViewById(R.id.txtDireccion);
        txtPass = findViewById(R.id.txtPassword);
        txtPassRepeat = findViewById(R.id.txtPasswordRepeat);

        Button crearCuenta = findViewById(R.id.CrearCuenta);

        android.widget.TextView terminos_condiciones = findViewById(R.id.terminos_condiciones);
        terminos_condiciones.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Intent intent = new Intent(getApplicationContext(), TerminosCondicionesActivity.class);
                startActivity(intent);
            }
        });

    }

    public void crearCuenta(android.view.View view) {
        String nombre = txtName.getText().toString();
        String numDocumento = txtNumDocumento.getText().toString();
        String email = txtCorreo.getText().toString();
        String telefono = txtTelefono.getText().toString();
        String direccion = txtDireccion.getText().toString();
        String pass = txtPass.getText().toString();
        String passrepeat = txtPassRepeat.getText().toString();

        if (!pass.equals(passrepeat)) {
            Toast.makeText(getApplicationContext(), "Las contraseñas no coinciden, Intentelo nuevamente", Toast.LENGTH_LONG).show();
        } else {

            postRegister(nombre, numDocumento, email, telefono, direccion, pass);

        }

    }


    public void postRegister(final String nombre, final String num_documento, final String email, final String telefono,
                             final String direccion, final String pass) {
        String url = this.baseUrl + "/api/clients/create/";
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject object = new JSONObject();

        try {
            object.put("num_documento", num_documento);
            object.put("nombre", nombre);
            object.put("phone1", telefono);
            object.put("phone2", "");
            object.put("direccion", direccion);

            object.put("email", email);
            object.put("username", email);
            object.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, url, object, response -> {
                    try {

                        System.out.println(response);
                        int id = response.getJSONObject("data").getInt("company_id");

                        if (id != 0) {
                            Acount cuenta = new Acount();

                            cuenta.setId(id);
                            cuenta.setNombre(nombre);
                            cuenta.setNumDocumento(num_documento);
                            cuenta.setEmail(email);
                            cuenta.setPhoneOne(telefono);
                            cuenta.setPhoneTwo(null);
                            cuenta.setDireccion(direccion);
                            cuenta.setPassword(pass);

                            DatabaseClient.getInstance(getApplicationContext())
                                    .getAppDatabase()
                                    .getAcountDao()
                                    .addUser(cuenta);

                            iniciarSesion(email, pass);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    android.util.Log.d("Volley post", "error voley" + error.toString());
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && response != null) {
                        try {
                            String res = new String(response.data,
                                    HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            // Now you can use any deserializer to make sense of data
                            JSONObject jsonObject = new JSONObject(res);
                            String message = jsonObject.getString("message");
                            System.out.println(res);

                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        } catch (UnsupportedEncodingException | JSONException e1) {
                            // Couldn't properly decode data to string
                            Toast.makeText(getApplicationContext(), "Error en operacion", Toast.LENGTH_SHORT).show();
                            e1.printStackTrace();
                        }
                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    public void iniciarSesion(String user, String pass) {

        final Session session = new Session(getApplicationContext());
        int token_app = session.getToken();

        // Si es que existe el usuario en la DB - pasar al main activity
        // si no insertar el usuario  -> si es que el TOKEN es valido
        Acount cuenta = DatabaseClient.getInstance(getApplicationContext())
                .getAppDatabase()
                .getAcountDao()
                .login(user, pass);

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JSONObject object = new JSONObject();
        try {
            object.put("username", user);
            object.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Metodo de login si es que existe conexion a INTERNET
        String url = this.baseUrl + "/api/auth/obtain_token/";
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, url, object, response -> {
                    try {
                        String token = response.getString("token");
                        if (!token.equals("")) {

                            JWT parsedJWT = new JWT(token);
                            Claim subscriptionMetaData = parsedJWT.getClaim("user_id");

                            int id_user = subscriptionMetaData.asInt();

                            if (cuenta != null) {
                                cuenta.setToken(token);
                                DatabaseClient.getInstance(getApplicationContext())
                                        .getAppDatabase()
                                        .getAcountDao()
                                        .updateUser(cuenta);

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                session.setToken(cuenta.getId());
                                saveTokenBackend(id_user, token);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
                    android.util.Log.d("Volley post", "error voley" + error.toString());
                    NetworkResponse response = error.networkResponse;
                    if (error instanceof ServerError && response != null) {
                        try {
                            String res = new String(response.data,
                                    HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            JSONObject obj = new JSONObject(res);
                            System.out.println(obj.toString());
                        } catch (UnsupportedEncodingException | JSONException e1) {
                            e1.printStackTrace();
                        }
                    }

                }) {
                    @Override
                    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                        return super.parseNetworkResponse(response);
                    }
                };

        requestQueue.add(jsonObjectRequest);
    }

    private void saveTokenBackend(final int id, final String tokenClient) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {

                });
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        android.util.Log.w("Friibusiness", "getInstanceId failed", task.getException());
                        return;
                    }

                    String token = task.getResult().getToken();

                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    JSONObject object = new JSONObject();
                    try {
                        object.put("token", token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String url = Global.URL_HOST + "/device/save/";
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                            response -> {
                                try {
                                    int status = response.getInt("status");
                                    if (status == 200) {
                                        System.out.println("Mensaje: " + response.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }, error -> {
                        android.util.Log.d("Volley post", "error voley" + error.toString());
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                JSONObject obj = new JSONObject(res);
                                System.out.println(res);
                            } catch (UnsupportedEncodingException | JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }) {
                        @Override
                        public java.util.Map<String, String> getHeaders() {
                            java.util.Map<String, String> headers = new HashMap<>();
                            headers.put("Authorization", "JWT " + tokenClient);
                            headers.put("Content-Type", "application/json");
                            return headers;
                        }
                    };
                    requestQueue.add(jsonObjectRequest);
                });
    }
}
