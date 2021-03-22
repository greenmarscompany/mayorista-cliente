package com.greenmarscompany.cliente

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.greenmarscompany.cliente.login.LoginActivity
import com.greenmarscompany.cliente.persistence.DatabaseClient
import com.greenmarscompany.cliente.persistence.Session
import com.greenmarscompany.cliente.persistence.entity.ECart
import com.greenmarscompany.cliente.services.SocketService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException
import java.text.DateFormat
import java.util.*

class CartdetailFragment : Fragment(), CartDetailAdapter.EventListener {

    private lateinit var mListener: OnFragmentInteractionListener
    private lateinit var socket: Socket
    private lateinit var recyclerView: RecyclerView
    private lateinit var procesarPedido: Button
    private lateinit var voucher: RadioButton
    private lateinit var groupMetodo: RadioGroup
    private var isTarjeta = false
    private var tipoComprobante: String? = null


    private var indexRadioTarejeta = 0
    private var cartDetails: List<ECart>? = null

    companion object {
        private const val TAG: String = "GAS"
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cartdetail, container, false)

        val socketService:SocketService = activity as SocketService
        socket = 


        recyclerView = view.findViewById(R.id.CartDetailContainer)
        recyclerView.layoutManager = LinearLayoutManager(context)

        //-- Métodos de pagos
        voucher = view.findViewById(R.id.RadioButtonBoleta)
        groupMetodo = view.findViewById(R.id.groupMetodo)
        val efectivoGroup = view.findViewById<RadioGroup>(R.id.groupMetodoEfectivo)
        efectivoGroup.setOnCheckedChangeListener { group: RadioGroup?, checkedId: Int ->
            val radioButton = efectivoGroup.findViewById<View>(checkedId)
            val index = efectivoGroup.indexOfChild(radioButton)
            if (index == 0) {
                when (indexRadioTarejeta) {
                    0 -> tipoComprobante = "tunki-boleta"
                    1 -> tipoComprobante = "yape-boleta"
                    2 -> tipoComprobante = "tarjeta-boleta"
                    3 -> tipoComprobante = "boleta"
                }
            } else if (index == 1) {
                when (indexRadioTarejeta) {
                    0 -> tipoComprobante = "tunki-factura"
                    1 -> tipoComprobante = "yape-factura"
                    2 -> tipoComprobante = "tarjeta-factura"
                    3 -> tipoComprobante = "factura"
                }
            }
            Log.d(TAG, "Event Efectivo: $tipoComprobante")
        }
        groupMetodo.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            val radioButton = groupMetodo.findViewById<View>(checkedId)
            when (groupMetodo.indexOfChild(radioButton)) {
                0 -> {
                    indexRadioTarejeta = 0
                    tipoComprobante = if (voucher.isChecked) "tunki-boleta" else "tunki-factura"
                    isTarjeta = true
                }
                1 -> {
                    indexRadioTarejeta = 1
                    tipoComprobante = if (voucher.isChecked) "yape-boleta" else "yape-factura"
                    isTarjeta = true
                }
                2 -> {
                    indexRadioTarejeta = 2
                    tipoComprobante = if (voucher.isChecked) "tarjeta-boleta" else "tarjeta-factura"
                    isTarjeta = true
                }
                3 -> {
                    indexRadioTarejeta = 3
                    tipoComprobante = if (voucher.isChecked) "boleta" else "factura"
                    isTarjeta = false
                }
                else -> {
                    isTarjeta = false
                    tipoComprobante = ""
                }
            }
            Log.d(TAG, "Radio: $tipoComprobante")
        }
        llenarCarrito()
        procesarPedido = view.findViewById(R.id.ButtonCartProcesarPedido)
        val eCarts = DatabaseClient.getInstance(context)
                .appDatabase
                .cartDao
                .carts!!
        if (eCarts.size == 0) {
            procesarPedido.setEnabled(false)
            procesarPedido.setBackgroundResource(R.drawable.custom_button_gray)
        } else {
            procesarPedido.setEnabled(true)
        }
        procesarPedido.setOnClickListener { confirmarPedido() }

        //--
        val button = view.findViewById<Button>(R.id.btnAbrirSheet)
        val linearLayout = view.findViewById<LinearLayout>(R.id.bottomSheet)
        val bottomSheetBehavior = BottomSheetBehavior.from<View>(linearLayout)
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    button.visibility = View.GONE
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    button.visibility = View.VISIBLE
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    button.visibility = View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        button.setOnClickListener { v: View? ->
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                button.visibility = View.GONE
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                button.visibility = View.VISIBLE
            }
        }
        return view
    }

    // Confirmar pedido
    private fun confirmarPedido() {
        Log.d(TAG, "confirmarPedido: click")
        //Obtener el token del cliente
        val acount = DatabaseClient.getInstance(context)
                .appDatabase
                .acountDao
                .getUser(Session(context).token)
        if (!isTarjeta) {
            tipoComprobante = if (voucher!!.isChecked) "boleta" else "factura"
        }
        val jsonObject = JSONObject()
        val queue = Volley.newRequestQueue(Objects.requireNonNull(context))
        try {
            jsonObject.put("voucher", tipoComprobante)
            jsonObject.put("latitud", acount.latitud.toString())
            jsonObject.put("longitud", acount.longitud.toString())
            jsonObject.put("client_id", Session(context).token)
            val jsonArray = JSONArray()
            val eCarts = DatabaseClient.getInstance(context)
                    .appDatabase
                    .cartDao
                    .carts
            if (eCarts != null) {
                for (e in eCarts) {
                    val orders_detal = JSONObject()
                    orders_detal.put("product_id", e.productRegister)
                    orders_detal.put("quantity", e.cantidad)
                    orders_detal.put("unit_price", e.price.toDouble())
                    jsonArray.put(orders_detal)
                }
            }
            jsonObject.put("detalle_orden", jsonArray)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val baseURL = Global.URL_HOST
        val url = "$baseURL/client/order/"
        println(jsonObject.toString())
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST, url, jsonObject, { response ->
            try {
                val status = response.getInt("status")
                if (status == 201) {
                    val message = response.getString("message")
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    val datas = JSONObject()
                    datas.put("id", response.getJSONObject("data").getInt("order_id"))
                    datas.put("latitude", acount.latitud)
                    datas.put("longitude", acount.longitud)
                    socket.emit("get orders", datas)
                    Log.d(TAG, "confirmarPedido: $datas")
                    DatabaseClient.getInstance(context)
                            .appDatabase
                            .cartDao
                            .deleteAllCart()
                    val intent = Intent(context, PedidosActivity::class.java)
                    startActivity(intent)
                    activity!!.finish()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, { error ->
            Log.d("Volley get", "error voley$error")
            val response = error.networkResponse
            if (error is ServerError && response != null) {
                try {
                    val res = String(response.data)
                    Log.d(TAG, "ConfirmarPedido: $res")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                Log.d("Voley get", acount.token)
                headers["Authorization"] = "JWT " + acount.token
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    fun onButtonPressed(uri: Uri?) {
        mListener.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener")
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri?)
    }

    private fun llenarCarrito() {
        cartDetails = DatabaseClient.getInstance(context)
                .appDatabase
                .cartDao
                .carts
        val cartDetailAdapter = CartDetailAdapter(cartDetails, this)
        recyclerView.adapter = cartDetailAdapter
    }

    private fun initSocket() {
        if (context == null) return
        val id_user = Session(context).token
        val cuenta = DatabaseClient.getInstance(context)
                .appDatabase
                .acountDao
                .getUser(id_user)
        val json_connect = JSONObject()
        val opts = IO.Options()
        // opts.forceNew = true;
        opts.reconnection = true
        opts.query = "auth_token=thisgo77"
        try {
            json_connect.put("ID", "US01")
            json_connect.put("TOKEN", cuenta.token)
            json_connect.put("ID_CLIENT", id_user)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        try {
            val HOST_NODEJS = Global.URL_NODE
            socket = IO.socket(HOST_NODEJS, opts)
            socket.connect()
            // SOCKET.io().reconnectionDelay(10000);
            Log.d(TAG, "Node connect ok")
            //conect();
        } catch (e: URISyntaxException) {
            Log.d(TAG, "Node connect error")
        }
        socket.on(Socket.EVENT_CONNECT) { args: Array<Any?>? ->
            Log.d(TAG, "emitiendo new conect")
            val data = JSONObject()
            if (context == null) return@on
            val id = Session(context).token
            val cuenta1 = DatabaseClient.getInstance(activity)
                    .appDatabase
                    .acountDao
                    .getUser(id)
            try {
                data.put("ID", cuenta1.id)
                data.put("type", "client")
                Log.d(TAG, "conect $data")
                socket.emit("new connect", data)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val date = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
            Log.d(TAG, "SERVER connect $date")
        }
        socket.on(Socket.EVENT_DISCONNECT) { args: Array<Any?>? ->
            val date = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
            Log.d(TAG, "SERVER disconnect $date")
        }
        socket.on(Socket.EVENT_RECONNECT) { args: Array<Any?>? ->
            val my_date = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
            Log.d(TAG, "SERVER reconnect $my_date")
        }
        socket.on(Socket.EVENT_CONNECT_TIMEOUT) { args: Array<Any?>? ->
            val my_date = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
            Log.d(TAG, "SERVER timeout $my_date")
        }
        socket.on(Socket.EVENT_RECONNECTING) { args: Array<Any?>? ->
            val my_date = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
            Log.d(TAG, "SERVER reconnecting $my_date")
        }
    }

}