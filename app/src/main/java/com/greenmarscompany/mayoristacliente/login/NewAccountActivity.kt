package com.greenmarscompany.mayoristacliente.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.auth0.android.jwt.JWT
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.messaging.FirebaseMessaging
import com.greenmarscompany.mayoristacliente.Global
import com.greenmarscompany.mayoristacliente.MainActivity
import com.greenmarscompany.mayoristacliente.R
import com.greenmarscompany.mayoristacliente.persistence.DatabaseClient
import com.greenmarscompany.mayoristacliente.persistence.Session
import com.greenmarscompany.mayoristacliente.persistence.dao.AcountDao
import com.greenmarscompany.mayoristacliente.persistence.entity.Acount
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.util.*

class NewAccountActivity : AppCompatActivity() {
    private lateinit var txtName: TextInputEditText
    private lateinit var txtNumDocumento: TextInputEditText
    private lateinit var txtCorreo: TextInputEditText
    private lateinit var txtTelefono: TextInputEditText
    private lateinit var txtDireccion: TextInputEditText
    private lateinit var txtPass: TextInputEditText
    private lateinit var txtPassRepeat: TextInputEditText
    private lateinit var accountDao: AcountDao

    //--
    private var baseUrl = Global.URL_BASE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_account)

        //--
        accountDao = DatabaseClient.getInstance(applicationContext).appDatabase.acountDao

        //--
        txtName = findViewById(R.id.txtNombre)
        txtNumDocumento = findViewById(R.id.txtDni)
        txtCorreo = findViewById(R.id.txtEmail)
        txtTelefono = findViewById(R.id.txtTelefono)
        txtDireccion = findViewById(R.id.txtDireccion)
        txtPass = findViewById(R.id.txtPassword)
        txtPassRepeat = findViewById(R.id.txtPasswordRepeat)
        //--
        // val crearCuenta = findViewById<Button>(R.id.CrearCuenta)
        val terminosAndCondiciones = findViewById<TextView>(R.id.terminos_condiciones)
        terminosAndCondiciones.setOnClickListener {
            val intent = Intent(applicationContext, TerminosCondicionesActivity::class.java)
            startActivity(intent)
        }
    }

    fun crearCuenta(view: View?) {
        val nombre = txtName.text.toString()
        val numDocumento = txtNumDocumento.text.toString()
        val email = txtCorreo.text.toString()
        val telefono = txtTelefono.text.toString()
        val direccion = txtDireccion.text.toString()
        val pass = txtPass.text.toString()
        val passrepeat = txtPassRepeat.text.toString()
        if (pass != passrepeat) {
            Toast.makeText(applicationContext, "Las contraseñas no coinciden, Intente nuevamente", Toast.LENGTH_LONG).show()
        } else {
            postRegister(nombre, numDocumento, email, telefono, direccion, pass)
        }
    }

    //-- Registra una nueva cuenta e inicia la sesión
    private fun postRegister(nombre: String?, num_documento: String?, email: String?, telefono: String?,
                             direccion: String?, pass: String?) {
        val url = "$baseUrl/api/clients/create/"
        val requestQueue = Volley.newRequestQueue(applicationContext)
        val `object` = JSONObject()
        try {
            `object`.put("num_documento", num_documento)
            `object`.put("nombre", nombre)
            `object`.put("phone1", telefono)
            `object`.put("phone2", "")
            `object`.put("direccion", direccion)
            `object`.put("email", email)
            `object`.put("username", email)
            `object`.put("password", pass)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, `object`, { response ->
            try {
                println(response)
                val id = response.getJSONObject("data").getInt("company_id")
                if (id != 0) {
                    val cuenta = Acount()
                    cuenta.id = id
                    cuenta.nombre = nombre
                    cuenta.numDocumento = num_documento
                    cuenta.email = email
                    cuenta.phoneOne = telefono
                    cuenta.phoneTwo = null
                    cuenta.direccion = direccion
                    cuenta.password = pass
                    accountDao.addUser(cuenta)
                    iniciarSesion(email, pass)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }) { error ->
            Log.d("Volley post", "error voley $error")
            val response = error.networkResponse
            when (error) {
                is ServerError -> {
                    val res = String(response.data)
                    val jsonObject = JSONObject(res)
                    val message = jsonObject.getString("message")
                    Log.d(Global.TAG, "PostRegister: $jsonObject")
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
                is NetworkError, is NoConnectionError, is TimeoutError -> {
                    Toast.makeText(this, "Por favor verifique su conexión a Internet", Toast.LENGTH_LONG).show()
                }
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    private fun iniciarSesion(user: String?, pass: String?) {
        val session = Session(applicationContext)

        // Si es que existe el usuario en la DB - pasar al main activity
        // si no insertar el usuario  -> si es que el TOKEN es valido
        val cuenta = accountDao.login(user, pass)
        val requestQueue = Volley.newRequestQueue(applicationContext)
        val `object` = JSONObject()
        try {
            `object`.put("username", user)
            `object`.put("password", pass)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        // Método de login si es que existe conexión a INTERNET
        val url = "$baseUrl/api/auth/obtain_token/"
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST, url, `object`, Response.Listener { response: JSONObject ->
            try {
                val token = response.getString("token")
                if (token != "") {
                    val parsedJWT = JWT(token)
                    val subscriptionMetaData = parsedJWT.getClaim("user_id")
                    val idUser = subscriptionMetaData.asInt()!!
                    if (cuenta != null) {
                        cuenta.token = token
                        accountDao.updateUser(cuenta)
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        session.token = cuenta.id
                        saveTokenBackend(idUser, token)
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error: VolleyError ->
            Log.d("Volley post", "error voley$error")
            val response = error.networkResponse
            Log.d(Global.TAG, "iniciarSesion: $response")
            when (error) {
                is ServerError -> {
                    val res = String(response.data)
                    val obj = JSONObject(res)
                    Log.d(Global.TAG, "IniciarSesion: $obj")
                }
                is NetworkError, is NoConnectionError, is TimeoutError -> {
                    Toast.makeText(this, "Por favor verifique su conexión a Internet", Toast.LENGTH_LONG).show()
                }
            }
        }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
                return super.parseNetworkResponse(response)
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    //-- Guarda el token del dispositivo en el backend
    private fun saveTokenBackend(id: Int, tokenClient: String) {
        FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task: Task<String?>? -> }
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task: Task<InstanceIdResult> ->
                    if (!task.isSuccessful) {
                        Log.w("Friibusiness", "getInstanceId failed", task.exception)
                        return@addOnCompleteListener
                    }
                    val token = task.result.token
                    val requestQueue = Volley.newRequestQueue(applicationContext)
                    val `object` = JSONObject()
                    try {
                        `object`.put("token", token)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    val url = Global.URL_HOST + "/device/save/"
                    val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST, url, `object`,
                            Response.Listener { response: JSONObject ->
                                try {
                                    val status = response.getInt("status")
                                    if (status == 200) {
                                        println("Mensaje: " + response.getString("message"))
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            }, Response.ErrorListener { error: VolleyError ->
                        Log.d("Volley post", "error voley$error")
                        val response = error.networkResponse
                        if (error is ServerError && response != null) {
                            try {
                                val res = String(response.data)
                                val obj = JSONObject(res)
                                Log.d(Global.TAG, "SaveTokenBackend: $obj")
                            } catch (e1: UnsupportedEncodingException) {
                                e1.printStackTrace()
                            } catch (e1: JSONException) {
                                e1.printStackTrace()
                            }
                        } else if (error is NoConnectionError) {
                            Toast.makeText(this, "Por favor verifique su conexión a Internet", Toast.LENGTH_LONG).show()
                        }
                    }) {
                        override fun getHeaders(): Map<String, String> {
                            val headers: MutableMap<String, String> = HashMap()
                            headers["Authorization"] = "JWT $tokenClient"
                            headers["Content-Type"] = "application/json"
                            return headers
                        }
                    }
                    requestQueue.add(jsonObjectRequest)
                }
    }
}