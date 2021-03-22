package com.greenmarscompany.cliente

import android.app.Application
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.greenmarscompany.cliente.Global
import com.greenmarscompany.cliente.persistence.DatabaseClient
import com.greenmarscompany.cliente.persistence.Session
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class SocketService : Application() {
    private var mSocket: Socket? = null

    override fun onCreate() {
        super.onCreate()
        try {
            if (applicationContext == null) return
            val id_user = Session(applicationContext).token
            val cuenta = DatabaseClient.getInstance(applicationContext)
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

            mSocket = IO.socket(Global.URL_NODE, opts)
        }catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    fun getMSocket(): Socket? {
        return mSocket
    }


}