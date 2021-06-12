package com.greenmarscompany.cliente

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.greenmarscompany.cliente.persistence.DatabaseClient
import com.greenmarscompany.cliente.persistence.Session
import com.greenmarscompany.cliente.persistence.entity.ECart
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.util.*

class CartdetailFragment : Fragment(), CartDetailAdapter.EventListener {

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
        private const val TAG: String = Global.TAG
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cartdetail, container, false)
        initSocket()

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
                    0 -> tipoComprobante = "financiar-boleta"
                    1 -> tipoComprobante = "reciclaje-boleta"
                    2 -> tipoComprobante = "tunki-boleta"
                    3 -> tipoComprobante = "yape-boleta"
                    4 -> tipoComprobante = "tarjeta-boleta"
                    5 -> tipoComprobante = "boleta"
                }
            } else if (index == 1) {
                when (indexRadioTarejeta) {
                    0 -> tipoComprobante = "financiar-factura"
                    1 -> tipoComprobante = "reciclaje-factura"
                    2 -> tipoComprobante = "tunki-factura"
                    3 -> tipoComprobante = "yape-factura"
                    4 -> tipoComprobante = "tarjeta-factura"
                    5 -> tipoComprobante = "factura"
                }
            }
            Log.d(TAG, "Event Efectivo: $tipoComprobante")
        }
        groupMetodo.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            val radioButton = groupMetodo.findViewById<View>(checkedId)
            when (groupMetodo.indexOfChild(radioButton)) {
                0 -> {
                    indexRadioTarejeta = 0
                    tipoComprobante = if (voucher.isChecked) "financiar-boleta" else "financiar-factura"
                    isTarjeta = true
                }
                1 -> {
                    indexRadioTarejeta = 1
                    tipoComprobante = if (voucher.isChecked) "reciclaje-boleta" else "reciclaje-factura"
                    isTarjeta = true
                }
                2 -> {
                    indexRadioTarejeta = 2
                    tipoComprobante = if (voucher.isChecked) "tunki-boleta" else "tunki-factura"
                    isTarjeta = true
                }
                3 -> {
                    indexRadioTarejeta = 3
                    tipoComprobante = if (voucher.isChecked) "yape-boleta" else "yape-factura"
                    isTarjeta = true
                }
                4 -> {
                    indexRadioTarejeta = 4
                    tipoComprobante = if (voucher.isChecked) "tarjeta-boleta" else "tarjeta-factura"
                    isTarjeta = true
                }
                5 -> {
                    indexRadioTarejeta = 5
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
            procesarPedido.isEnabled = false
            procesarPedido.setBackgroundResource(R.drawable.custom_button_gray)
        } else {
            procesarPedido.isEnabled = true
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
        button.setOnClickListener {
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

                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("fragment", "pedidos")
                    startActivity(intent)
                    activity!!.finish()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, { error ->
            Log.d("Volley get", "error voley$error")
            val response = error.networkResponse
            if (response != null) {
                when (error) {
                    is ServerError -> {
                        val res = String(response.data)
                        Log.d(TAG, "ConfirmarPedido: $res")
                    }
                    is TimeoutError -> {
                        Toast.makeText(context, "Opsss Timeout", Toast.LENGTH_LONG).show()
                    }
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
        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                Global.MY_DEFAULT_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(jsonObjectRequest)
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
        val data = JSONObject()
        val iduser = Session(context).token
        val cuenta = DatabaseClient.getInstance(context)
                .appDatabase
                .acountDao
                .getUser(iduser)
        //-----
        val jsonObject = JSONObject()
        val opts: IO.Options = IO.Options()

        opts.reconnection = true
        opts.query = "auth_token=thisgo77"
        jsonObject.put("ID", "US01")

        socket = IO.socket(Global.URL_NODE, opts)
        socket.let {
            it.connect()
            it.on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "emitiendo new conect")
                try {
                    data.put("ID", cuenta.id)
                    data.put("type", "client")
                    Log.d(TAG, "conect $data")
                    socket.emit("new connect", data)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                val date = DateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
                Log.d(TAG, "SERVER connect $date")
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
        activity?.invalidateOptionsMenu()
    }

}