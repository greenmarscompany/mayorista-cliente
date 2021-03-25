package com.greenmarscompany.cliente

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.google.android.gms.maps.model.LatLng
import com.greenmarscompany.cliente.login.LoginActivity
import com.greenmarscompany.cliente.persistence.DatabaseClient
import com.greenmarscompany.cliente.persistence.Session
import com.greenmarscompany.cliente.persistence.entity.Acount
import com.greenmarscompany.cliente.pojo.Order
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MisPedidosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var socket: Socket
    private lateinit var cuenta: Acount

    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mListener: OnFragmentInteractionListener? = null
    private var orders: MutableList<Order>? = null

    private var misPedidosAdapter: MisPedidosAdapter? = null
    private var HOST_NODEJS = Global.URL_NODE
    private val TAG = Global.TAG
    private var queue: RequestQueue? = null
    private var token = 0

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): MisPedidosFragment {
            val fragment = MisPedidosFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }

        setHasOptionsMenu(true)

        //Validar informacion del usuario
        val session = Session(getContext())
        token = session.token
        if (token == 0 || token < 0) {
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
            Objects.requireNonNull(activity)!!.finish()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mispedidos, container, false)

        //-----
        queue = Volley.newRequestQueue(context)
        val iduser = Session(context).token
        cuenta = DatabaseClient.getInstance(context)
                .appDatabase
                .acountDao
                .getUser(iduser)

        initSocketIO()

        //Iniciamos el socket para traer los pedidos
        orders = ArrayList()
        llenarPedidos()
        recyclerView = view.findViewById(R.id.MisPedidosContainer)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager

        //--
        swipeRefreshLayout.setOnRefreshListener { llenarPedidos() }

        return view
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_carrito).isVisible = false
        menu.findItem(R.id.action_pedidos).isVisible = false
    }

    fun onButtonPressed(uri: Uri?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri?)
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

    // Llenar información de pedidos
    fun llenarPedidos() {

        val baseURL = Global.URL_HOST
        val url = "$baseURL/client/order/$token"
        val jsonObject = JSONObject()
        val request: JsonObjectRequest = object : JsonObjectRequest(Method.GET, url, jsonObject, Response.Listener { response: JSONObject ->
            try {
                recyclerView.adapter = null
                orders!!.clear()
                val status = response.getInt("status")
                if (status == 200) {
                    val jsonArray = response.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val order = Order()
                        order.id = obj.getJSONObject("orden").getInt("id")
                        order.date = (obj.getJSONObject("orden").getString("date") + " | " +
                                obj.getJSONObject("orden").getString("time").substring(0, 8))
                        order.time = obj.getJSONObject("orden").getString("time").substring(0, 8)
                        order.status = obj.getJSONObject("orden").getString("status")
                        order.calification = obj.getJSONObject("orden").getDouble("calification").toFloat()
                        order.clientDirection = LatLng(
                                obj.getJSONObject("orden").getDouble("latitude"),
                                obj.getJSONObject("orden").getDouble("longitude")
                        )
                        if (obj.getJSONObject("company").length() > 0) {
                            order.phone = obj.getJSONObject("company").getString("phone")
                            order.companyName = obj.getJSONObject("company").getString("name")
                            val latLng = LatLng(
                                    obj.getJSONObject("company").getDouble("latitude"),
                                    obj.getJSONObject("company").getDouble("longitude")
                            )
                            order.companyDirection = latLng
                            order.status = obj.getJSONObject("orden").getString("status")
                            order.status = obj.getJSONObject("orden").getString("status")
                        }
                        val details_data = obj.getJSONArray("order_detail")
                        val details: MutableList<String> = ArrayList()
                        val ListPrecios: MutableList<String> = ArrayList()
                        val ListSubTotal: MutableList<String> = ArrayList()
                        var totalF = 0.0
                        for (j in 0 until details_data.length()) {
                            val jsonObject1 = details_data.getJSONObject(j)
                            details.add(jsonObject1.getJSONObject("product_id").getString("description"))
                            if (obj.getJSONObject("company").length() > 0) {
                                ListPrecios.add("S/. " + jsonObject1.getDouble("unit_price"))
                                ListSubTotal.add("S/. " + jsonObject1.getDouble("unit_price")
                                        * jsonObject1.getDouble("quantity"))
                                totalF += jsonObject1.getDouble("unit_price") * jsonObject1.getDouble("quantity")
                            } else {
                                ListPrecios.add("")
                                ListSubTotal.add("")
                                totalF = 0.0
                            }
                        }
                        order.totalFinal = totalF
                        order.detalles = details
                        order.listPrecioUnitario = ListPrecios
                        order.listSubTotal = ListSubTotal
                        orders!!.add(order)
                    }
                    //        Toast.makeText(getContext(),response.toString(),Toast.LENGTH_LONG).show();
                    misPedidosAdapter = MisPedidosAdapter(orders, socket, this@MisPedidosFragment)
                    recyclerView.adapter = misPedidosAdapter
                    misPedidosAdapter!!.setOnClickListener { v: View? ->
                        val manager: FragmentManager = Objects.requireNonNull(activity)!!.supportFragmentManager
                        val transaction: FragmentTransaction = manager.beginTransaction()
                        val status1: String = orders!!.get(recyclerView!!.getChildAdapterPosition((v)!!)).status
                        if ((status1 == "confirm")) {
                            val misPedidosFragment: MapsPerdidos = MapsPerdidos()
                            val bundle: Bundle = Bundle()
                            val d_company: LatLng = orders!!.get(recyclerView!!.getChildAdapterPosition((v))).companyDirection
                            val d_client: LatLng = orders!!.get(recyclerView!!.getChildAdapterPosition((v))).clientDirection
                            bundle.putParcelable("DCOMPANY", d_company)
                            bundle.putParcelable("DCLIENT", d_client)
                            misPedidosFragment.arguments = bundle
                            transaction.add(R.id.navigationContainer, misPedidosFragment)
                            transaction.addToBackStack(null)
                            transaction.commit()
                        }
                    }
                    swipeRefreshLayout.isRefreshing = false
                    recyclerView.post { misPedidosAdapter!!.notifyDataSetChanged() }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, Response.ErrorListener { error: VolleyError ->
            Log.d("Volley get", "error voley$error")
            val response = error.networkResponse
            if (error is ServerError && response != null) {
                try {
                    val res = String(response.data)
                    println(res)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["Authorization"] = "JWT " + cuenta.token
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        queue!!.add(request)
    }

    private fun initSocketIO() {
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
                val data = JSONObject()
                if (context == null) return@on
                data.put("ID", cuenta.id)
                data.put("type", "client")
                socket.emit("new connect", data)
            }

            val dataStatus = JSONObject()
            dataStatus.put("id", token)
            dataStatus.put("token", cuenta.token)
            Log.d(TAG, "conect Fragment$dataStatus")
            it.emit("status order", dataStatus)

            it.on("confirm order client") { args: Array<Any> ->
                val response = args[0] as JSONObject
                try {
                    if (response.getJSONObject("data").getInt("order_id") > 0) {
                        if (activity == null) return@on
                        activity!!.runOnUiThread(Runnable { //    misPedidosAdapter.notifyDataSetChanged();
                            val datas1 = JSONObject()
                            try {
                                datas1.put("id", token)
                                datas1.put("token", cuenta.token)
                                Log.d(TAG, "conect $datas1")
                                val CHANNEL_ID = "channel1"
                                orders!!.clear()
                                llenarPedidos()
                                if (getContext() == null) {
                                    return@Runnable
                                }
                                val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val channel = NotificationChannel(
                                            CHANNEL_ID,
                                            "Channel 1",
                                            NotificationManager.IMPORTANCE_HIGH
                                    )
                                    notificationManager.createNotificationChannel(channel)
                                }
                                val notification = NotificationCompat.Builder((context)!!, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_cart)
                                        .setContentTitle("Pedido Confirmado")
                                        .setContentText("Su pedido está en camino 😃")
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                        .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                        .setColor(Color.BLUE)
                                        .setAutoCancel(true).build()
                                notificationManager.notify(1, notification)
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }
                        })
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                println(response.toString())
            }
            it.on("delivered order client", Emitter.Listener { args ->
                val response = args[0] as JSONObject
                try {
                    if (response.getJSONObject("data").getInt("order_id") > 0) {
                        if (activity == null) return@Listener
                        activity!!.runOnUiThread(Runnable {
                            val datas = JSONObject()
                            try {
                                datas.put("id", token)
                                datas.put("token", cuenta.token)
                                Log.d(TAG, "conect $datas")
                                orders!!.clear()
                                llenarPedidos()
                                val CHANNEL_ID = "channel1"
                                if (context == null) {
                                    return@Runnable
                                }
                                val notificationManager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val channel = NotificationChannel(
                                            CHANNEL_ID,
                                            "Channel 1",
                                            NotificationManager.IMPORTANCE_HIGH
                                    )
                                    notificationManager.createNotificationChannel(channel)
                                }
                                val notification = NotificationCompat.Builder(activity!!, CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_cart)
                                        .setContentTitle("Pedido Entregado")
                                        .setContentText("Gracias por confiar en Mayorista 😃")
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                        .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000)) //   .setLights(Color.WHITE, 3000, 3000)
                                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                                        .setColor(Color.BLUE)
                                        .setAutoCancel(true).build()
                                notificationManager.notify(1, notification)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        })
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                println(response.toString())
            })
            it.on("order cancelar client", Emitter.Listener {
                if (activity == null) return@Listener
                activity!!.runOnUiThread {
                    orders!!.clear()
                    llenarPedidos()
                }
            })
            it.on("reorder client", Emitter.Listener {
                if (activity == null) return@Listener
                activity!!.runOnUiThread {
                    orders!!.clear()
                    llenarPedidos()
                }
            })
            it.on("send alert", Emitter.Listener { args ->
                val response = args[0] as JSONObject
                Log.i(TAG, response.toString())
                var mensaje: String? = ""
                try {
                    mensaje = response.getJSONObject("data").getString("message")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                if (getContext() == null) {
                    return@Listener
                }
                val CHANNEL_ID = "channel1"
                val notificationManager = getContext()!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                            CHANNEL_ID,
                            "Channel 1",
                            NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)
                }
                val notification = NotificationCompat.Builder(getContext()!!, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_cart)
                        .setContentTitle("Mayorista")
                        .setContentText(mensaje)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                        .setLights(Color.WHITE, 3000, 3000)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setColor(Color.BLUE)
                        .setAutoCancel(true).build()
                notificationManager.notify(1, notification)
            })
        }


    }


}