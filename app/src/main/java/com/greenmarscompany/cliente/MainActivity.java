package com.greenmarscompany.cliente;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.greenmarscompany.cliente.login.LoginActivity;
import com.greenmarscompany.cliente.persistence.DatabaseClient;
import com.greenmarscompany.cliente.persistence.Session;
import com.greenmarscompany.cliente.persistence.dao.CartDao;
import com.greenmarscompany.cliente.persistence.entity.Acount;

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

    TextView textCartItemCount;
    int mCartItemCount = 0;


    //e
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = findViewById(R.id.navigationToolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.navigationDrawer);
        navigationView = findViewById(R.id.navigationView);

        // establecer el evento onclick de navigation
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

        //-- Cierra la sesión
        CerrarSecion = findViewById(R.id.CerrarSesion);
        CerrarSecion.setOnClickListener(v -> {
            Session session = new Session(getApplicationContext());
            session.destroySession();

            DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .getCartDao()
                    .deleteAllCart();

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });

        updateToken();
    }

    @Override
    protected void onStart() {
        super.onStart();
        llenarInfoUsuario();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_actions, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_carrito);

        View actionView = menuItem.getActionView();
        textCartItemCount = actionView.findViewById(R.id.cart_badge);
        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));
        setupBadge();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_pedidos) {
            Intent intent = new Intent(getBaseContext(), PedidosActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_carrito) {
            Intent intent = new Intent(getApplicationContext(), Cart.class);
            startActivity(intent);
            return true;
        }
        return true;
        //return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (menuItem.getItemId() == R.id.home) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.Perfil) {
            Intent intent = new Intent(getBaseContext(), PerfilActivity.class);
            String title = menuItem.getTitle().toString();
            intent.putExtra("name", title);
            intent.putExtra("id", R.id.Perfil);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.MisPedidos) {
            Intent intent = new Intent(getBaseContext(), PedidosActivity.class);
            startActivity(intent);
        } else if (menuItem.getItemId() == R.id.mVaciarCache) {
            deleteCache(getBaseContext());
            Toast.makeText(getBaseContext(), "Caché vaciadas correctamente", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        invalidateOptionsMenu();
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
        fragmentTransaction.add(R.id.navigationContainer, new MainFragment());
        fragmentTransaction.commit();
    }


    private void llenarInfoUsuario() {
        Session session = new Session(getApplicationContext());
        final int token = session.getToken();
        CartDao cartDao = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().getCartDao();

        View view = navigationView.getHeaderView(0);
        lblUsername = view.findViewById(R.id.lblNombreUsuario);
        lblEmail = view.findViewById(R.id.lblEmailUsuario);

        if (token != 0) {
            Acount acount = DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .getAcountDao()
                    .getUser(token);

            if (acount != null) {
                lblUsername.setText(acount.getNombre());
                lblEmail.setText(acount.getEmail());
                //-- Llenamos el carrito de compras
                mCartItemCount = cartDao.getCountCart();
            } else {
                lblUsername.setText("Mayorista");
                lblEmail.setText("team@greenmarscompany.com");
            }
        } else {
            lblUsername.setText("Mayorista");
            lblEmail.setText("team@greenmarscompany.com");
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

    //--Ocultar en un inicio el badge
    private void setupBadge() {
        if (textCartItemCount != null) {
            if (mCartItemCount == 0) {
                if (textCartItemCount.getVisibility() != View.GONE) {
                    textCartItemCount.setVisibility(View.GONE);
                }
            } else {
                textCartItemCount.setText(String.valueOf(Math.min(mCartItemCount, 99)));
                if (textCartItemCount.getVisibility() != View.VISIBLE) {
                    textCartItemCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }


}
