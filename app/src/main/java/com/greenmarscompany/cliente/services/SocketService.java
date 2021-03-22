package com.greenmarscompany.cliente.services;

import android.accounts.Account;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;
import com.greenmarscompany.cliente.Global;
import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.Session;
import com.greenmarscompany.cliente.persistence.dao.AcountDao;
import com.greenmarscompany.cliente.persistence.entity.Acount;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


public class SocketService extends Application {
    private Socket socket;
    @Override
    public void onCreate() {
        super.onCreate();
        initSocket();
    }

    private void initSocket() {
        if (getApplicationContext() == null) return;
        int iduser = new Session(getApplicationContext()).getToken();
        AcountDao acountDao = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().getAcountDao();
        Acount cuenta = acountDao.getUser(iduser);
        JSONObject json_connect = new JSONObject();
        IO.Options opts = new IO.Options();
        // opts.forceNew = true;
        opts.reconnection = true;
        opts.query = "auth_token=thisgo77";
        try {
            json_connect.put("ID", "US01");
            json_connect.put("TOKEN", cuenta.getToken());
            json_connect.put("ID_CLIENT", iduser);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            String URL = Global.URL_NODE;
            socket = IO.socket(URL, opts);
            socket.connect();

            Log.d(Global.TAG, "Node connect ok");
        } catch (URISyntaxException e) {
            Log.d(Global.TAG, "Node connect error" + e);
        }

        /*socket.on(Socket.EVENT_CONNECT, args -> {
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
        });*/
    }

    public Socket getSocket() {
        return socket;
    }
}
