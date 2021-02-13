package com.greenmarscompany.mayoristacliente.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.auth0.android.jwt.JWT
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.iid.FirebaseInstanceId
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
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: TextInputEditText
    private lateinit var progressBar: ProgressBar
    private lateinit var accountDao: AcountDao

    //--
    private var baseUrl = Global.URL_BASE

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //--
        accountDao = DatabaseClient.getInstance(applicationContext).appDatabase.acountDao

        email = findViewById(R.id.EmailLogin)
        password = findViewById(R.id.txtPassword)
        val newAccount = findViewById<TextView>(R.id.NewAccountLogin)
        val forgottedPassword = findViewById<TextView>(R.id.ForgottenPasswordLogin)
        progressBar = findViewById(R.id.pbLogin)
        progressBar.visibility = View.GONE
        val s = Session(applicationContext)
        if (s.token > 0) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        newAccount.setOnClickListener {
            val intent = Intent(applicationContext, NewAccountActivity::class.java)
            startActivity(intent)
        }
        forgottedPassword.setOnClickListener {
            val intent = Intent(application, ForgottenPasswordActivity::class.java)
            startActivity(intent)
        }


    }

    // login
    fun iniciarSesion(view: View?) {
        progressBar.visibility = View.VISIBLE
        val user = email.text.toString()
        val pass = password.text.toString()
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

        // Metodo de login si es que existe conexion a INTERNET
        val url = "$baseUrl/api/auth/obtain_token/"
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST, url, `object`, { response ->
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
                    } else {
                        insertarUsuario(token, idUser, pass)
                    }
                    progressBar.visibility = View.GONE
                    finish()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, { error ->
            Log.d("Volley post", "error voley $error")
            val response = error.networkResponse

            when (error) {
                is ServerError -> {
                    val res = String(response.data)
                    val obj = JSONObject(res)
                    Log.d(Global.TAG, "IniciarSesión: $obj")
                    Toast.makeText(applicationContext, "Credenciales Inválidas", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                }
                is NetworkError, is NoConnectionError, is TimeoutError -> {
                    Toast.makeText(this, "Por favor verifique su conexión a Internet", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                }
            }
        }) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<JSONObject> {
                return super.parseNetworkResponse(response)
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    fun insertarUsuario(token: String, id: Int, pass: String?) {
        val requestQueue = Volley.newRequestQueue(applicationContext)
        val a = accountDao.getUser(id)
        if (a == null) {
            val `object` = JSONObject()
            val url = "$baseUrl/api/clients/$id"
            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.GET, url, `object`, Response.Listener { response: JSONObject ->
                println(response)
                val cuenta = Acount()
                try {
                    val i = response.getJSONObject("client").getInt("client_id")
                    cuenta.id = response.getJSONObject("client").getInt("client_id")
                    cuenta.numDocumento = response.getJSONObject("client").getString("num_document")
                    cuenta.nombre = response.getJSONObject("client").getString("name")
                    cuenta.phoneOne = response.getJSONObject("client").getString("phone1")
                    cuenta.phoneTwo = response.getJSONObject("client").getString("phone2")
                    cuenta.direccion = response.getJSONObject("client").getString("address")
                    cuenta.email = response.getString("user")
                    cuenta.password = pass
                    cuenta.token = token
                    DatabaseClient.getInstance(applicationContext)
                            .appDatabase
                            .acountDao
                            .addUser(cuenta)
                    val session = Session(applicationContext)
                    session.token = i
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    saveTokenBackend(i, token)
                    finish()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error: VolleyError ->
                Log.d("Volley post", "error voley $error")
                val response = error.networkResponse
                when (error) {
                    is ServerError -> {
                        val res = String(response.data)
                        val obj = JSONObject(res)
                        Log.d(Global.TAG, "InsertarUsuario: $obj")
                    }
                    is NetworkError, is NoConnectionError, is TimeoutError -> {
                        Toast.makeText(this, "Por favor verifique su conexión a Internet", Toast.LENGTH_LONG).show()
                    }
                }
            }) {
                override fun getHeaders(): Map<String, String> {
                    val headers: MutableMap<String, String> = HashMap()
                    Log.d("Voley get", token)
                    headers["Authorization"] = "JWT $token"
                    headers["Content-Type"] = "application/json"
                    return headers
                }
            }
            requestQueue.add(jsonObjectRequest)
        } else {
            a.token = token
            a.password = pass
            accountDao.updateUser(a)
            val session = Session(applicationContext)
            session.token = a.id
            saveTokenBackend(a.id, token)
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveTokenBackend(id: Int, tokenClient: String) {
        FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task: Task<String?>? -> }
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w("Friibusiness", "getInstanceId failed", task.exception)
                        return@OnCompleteListener
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
                    val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST, url, `object`, { response ->
                        try {
                            val status = response.getInt("status")
                            if (status == 200) {
                                println("Mensaje: " + response.getString("message"))
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }, { error ->
                        Log.d("Volley post", "error voley$error")
                        val response = error.networkResponse
                        when (error) {
                            is ServerError -> {
                                val res = String(response.data)
                                val obj = JSONObject(res)
                                Toast.makeText(this, "No se guardo el token vuelva a iniciar sesión por favor", Toast.LENGTH_LONG).show()
                                Log.d(Global.TAG, "SaveTokenBackend: $obj")
                            }
                            is NetworkError, is NoConnectionError, is TimeoutError -> {
                                Toast.makeText(this, "Por favor verifique su conexión a Internet", Toast.LENGTH_LONG).show()
                            }
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
                })
    }
}