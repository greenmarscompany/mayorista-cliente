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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.greenmarscompany.cliente.login.LoginActivity;
import com.greenmarscompany.cliente.login.NewAccountActivity;
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

    //--
    android.widget.TextView lblUsername, lblEmail, CerrarSecion;
    TextView textCartItemCount;
    public static int mCartItemCount = 0;
    private int token = 0;

    //e
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Session session = new Session(getApplicationContext());
        token = session.getToken();

        toolbar = findViewById(R.id.navigationToolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.navigationDrawer);
        navigationView = findViewById(R.id.navigationView);
        Menu menu = navigationView.getMenu();


        // establecer el evento onclick de navigation
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        cargarFragments();
        llenarInfoUsuario();

        CerrarSecion = findViewById(R.id.CerrarSesion);
        // next_pedidos = findViewById(R.id.siquiente_pedidos);

        updateToken();

        if (token == 0 || token < 0) {
            menu.findItem(R.id.Perfil).setVisible(false);
            menu.findItem(R.id.MisPedidos).setVisible(false);
        }

        //-- Cierra la sesión

        CerrarSecion.setOnClickListener(v -> {
            session.destroySession();
            DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .getCartDao()
                    .deleteAllCart();

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finishAffinity();
        });


        Log.d(Global.TAG, "onCreate | MainActivity: " + token);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_actions, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_carrito);
        MenuItem itemPedidos = menu.findItem(R.id.action_pedidos);
        View actionView = menuItem.getActionView();
        textCartItemCount = actionView.findViewById(R.id.cart_badge);
        actionView.setOnClickListener(v -> onOptionsItemSelected(menuItem));
        setupBadge();

        if (token == 0 || token < 0) {
            itemPedidos.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateCartBadge();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Intent intent;

        if (item.getItemId() == R.id.action_pedidos) {
            transaction.replace(R.id.navigationContainer, new MisPedidosFragment());
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        } else if (item.getItemId() == R.id.action_carrito) {
            if (token == 0 || token < 0) {
                Toast.makeText(this, "Por favor inicie sesión o registrese", Toast.LENGTH_LONG).show();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                intent = new Intent(this, Cart.class);
                startActivity(intent);
                /*transaction.replace(R.id.navigationContainer, new CartdetailFragment());
                transaction.addToBackStack(null);
                transaction.commit();*/
            }
            return true;
        }
        return true;
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
            transaction.replace(R.id.navigationContainer, new SettingFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (menuItem.getItemId() == R.id.MisPedidos) {
            transaction.replace(R.id.navigationContainer, new MisPedidosFragment());
            transaction.addToBackStack(null);
            transaction.commit();
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
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
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
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();


        if(getIntent().getExtras() != null) {
            if(getIntent().getStringExtra("fragment").equals("pedidos")) {
                fragmentTransaction.replace(R.id.navigationContainer, new MisPedidosFragment());
                fragmentTransaction.commit();
                return;
            }
        }

        if (token == 0 || token < 0) {
            fragmentTransaction.replace(R.id.navigationContainer, new MainFragment());
        } else {
            fragmentTransaction.replace(R.id.navigationContainer, new NewMapsFragment());
        }

        fragmentTransaction.commit();
    }


    private void llenarInfoUsuario() {
        Session session = new Session(getApplicationContext());
        final int token = session.getToken();


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
            } else {
                lblUsername.setText("Mayorista");
                lblEmail.setText("team@greenmarscompany.com");
            }
        } else {
            lblUsername.setText("Mayorista");
            lblEmail.setText("team@greenmarscompany.com");
        }

        updateCartBadge();

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

    public void updateCartBadge() {
        CartDao cartDao = DatabaseClient.getInstance(getBaseContext()).getAppDatabase().getCartDao();
        mCartItemCount = cartDao.getCountCart();
    }

}
