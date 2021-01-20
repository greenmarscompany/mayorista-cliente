package com.greenmarscompany.mayoristacliente;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;


import com.greenmarscompany.mayoristacliente.Login.LoginActivity;
import com.greenmarscompany.mayoristacliente.persistence.DatabaseClient;
import com.greenmarscompany.mayoristacliente.persistence.Session;
import com.greenmarscompany.mayoristacliente.persistence.entity.Acount;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;

/**
 * @author Orlando: orlandomora963@gmail.com
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        MainFragment.OnFragmentInteractionListener,
        CategoriesFragment.OnFragmentInteractionListener,
        BrandsFragment.OnFragmentInteractionListener,
        ProductsFragment.OnFragmentInteractionListener,
        GasFragment.OnFragmentInteractionListener,
        SettingFragment.OnFragmentInteractionListener,
        MisPedidosFragment.OnFragmentInteractionListener,
        GasNewFragment.OnFragmentInteractionListener,
        GasDetailFragment.OnFragmentInteractionListener,
        ProductNewFragment.OnFragmentInteractionListener,
        ProductDetailFragment.OnFragmentInteractionListener,
        NewMapsFragment.OnFragmentInteractionListener {


    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    androidx.appcompat.widget.Toolbar toolbar;
    NavigationView navigationView;


    com.google.android.material.floatingactionbutton.FloatingActionButton flo_cart;
    com.google.android.material.floatingactionbutton.FloatingActionButton flo_order_pedido;

    Button next_pedidos;

    //--
    android.widget.TextView lblUsername, lblEmail, CerrarSecion, ProbandoID;
    int id = 3;

    private android.widget.TextView badge_count;

    Boolean inicio = true;

    //e
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = findViewById(R.id.navigationToolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.navigationDrawer);
        navigationView = findViewById(R.id.navigationView);

        // estaclecer el evento onclick de navigation
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        cargarFragments();
        llenarInfoUsuario();

        next_pedidos = findViewById(R.id.siquiente_pedidos);

        next_pedidos.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Intent intent = new Intent(getBaseContext(), InicioMapsActivity.class);
                startActivity(intent);
                System.out.println("saliendo de");
            }
        });





        /*
        //carito de comprar
        flo_cart = findViewById(R.id.fad_cart_order);
        flo_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Cart.class);
                startActivity(intent);
                //finish();
            }
        });

        flo_order_pedido = findViewById(R.id.siquiente_pedidos);
        flo_order_pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), PedidosActivity.class);
                startActivity(intent);
            }
        });

         */


        CerrarSecion = findViewById(R.id.CerrarSesion);
        CerrarSecion.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(android.view.View v) {
                Session session = new Session(getApplicationContext());
                session.destroySession();

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        updateToken();
    }

    public void badge_visible() {
        //TextView badge_count=findViewById(R.id.badge_count);
        badge_count.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        llenarInfoUsuario();

/*
        if (inicio){
            Intent intent=new Intent(getBaseContext(),InicioMapsActivity.class);
            startActivity(intent);
            System.out.println("inresnasp normal");
            inicio=false;

        }

 */


    }

    @Override
    public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem menuItem) {

        drawerLayout.closeDrawer(GravityCompat.START);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (menuItem.getItemId() == R.id.home) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        }
        if (menuItem.getItemId() == R.id.account) {
            //transaction.replace(R.id.navigationContainer,new AccountFragment());

            Intent intent = new Intent(getBaseContext(), PerfilActivity.class);
            intent.putExtra("id", R.id.account);
            startActivity(intent);

            Toast.makeText(this, "Account", Toast.LENGTH_SHORT).show();
        }
        if (menuItem.getItemId() == R.id.Perfil) {

            Intent intent = new Intent(getBaseContext(), PerfilActivity.class);
            String title = menuItem.getTitle().toString();
            intent.putExtra("name", title);
            intent.putExtra("id", R.id.Perfil);
            startActivity(intent);
        }
        if (menuItem.getItemId() == R.id.MisPedidos) {

            Intent intent = new Intent(getBaseContext(), PedidosActivity.class);
            startActivity(intent);
        }

        if (menuItem.getItemId() == R.id.mVaciarCache) {

            deleteCache(getBaseContext());
            Toast.makeText(getBaseContext(), "Caché vaciadas correctamente", Toast.LENGTH_SHORT).show();
        }

        return false;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void updateToken() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        android.util.Log.w("Mayorista Cliente", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token1 = task.getResult();
                    android.util.Log.d("Mayorista Cliente", "TOKEN FIREBASE: " + token1);

                });
    }

    public void cargarFragments() {
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        //inicializando al fragment que contendra alos fragments categories y brands
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.navigationContainer, new NewMapsFragment());
        fragmentTransaction.commit();
    }


    private void llenarInfoUsuario() {
        Session session = new Session(getApplicationContext());
        final int token = session.getToken();

        if (token != 0) {
            Acount acount = DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .getAcountDao()
                    .getUser(token);

            if (acount != null) {
                android.view.View view = navigationView.getHeaderView(0);
                lblUsername = view.findViewById(R.id.lblNombreUsuario);
                lblEmail = view.findViewById(R.id.lblEmailUsuario);

                lblUsername.setText(acount.getNombre());
                lblEmail.setText(acount.getEmail());
            } else {
                Toast.makeText(getApplicationContext(), "Error de  loggeado", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }

        } else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            System.out.println("LAS CREDENCIALES SON INVALIDAS");
        }


    }

    //-- Vaciar las caches
    public void deleteCache(Context context) {
        try {
            File file = context.getCacheDir();
            deleteDir(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteDir(File file) {
        if (file != null && file.isDirectory()) {
            String[] children = file.list();
            if (children == null) return false;
            for (String child : children) {
                boolean success = deleteDir(new File(file, child));
                if (!success) {
                    return false;
                }
            }
            return file.delete();
        } else if (file != null && file.isFile()) {
            return file.delete();
        } else {
            return false;
        }
    }


}
