package com.greenmarscompany.mayoristacliente;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.widget.Toast;

import com.greenmarscompany.mayoristacliente.Login.LoginActivity;
import com.greenmarscompany.mayoristacliente.persistence.Session;
import com.google.android.material.navigation.NavigationView;

public class Cart extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        CartdetailFragment.OnFragmentInteractionListener,
        CategoriesFragment.OnFragmentInteractionListener,
        ProcesarpedidoFragment.OnFragmentInteractionListener,
        BrandsFragment.OnFragmentInteractionListener,
        GasFragment.OnFragmentInteractionListener,
        MainFragment.OnFragmentInteractionListener,
        ProductsFragment.OnFragmentInteractionListener,
        SettingFragment.OnFragmentInteractionListener {


    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    androidx.appcompat.widget.Toolbar toolbar;
    NavigationView navigationView;
    android.widget.TextView CerrarSecion;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        setContentView(R.layout.activity_cart);
        super.onCreate(savedInstanceState);

        //Validar informacion del usuario
        Session session = new Session(getApplicationContext());
        final int token = session.getToken();
        if (token == 0 || token < 0) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            System.out.println("LAS CREDENCIALES SON INVALIDAS");
        }
        //--

        toolbar = findViewById(R.id.navigationToolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.navigationDrawerCart);
        navigationView = findViewById(R.id.navigationView);

        // estaclecer el evento onclick de navigation
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        // FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.navigationContainer, new CartdetailFragment());
        fragmentTransaction.commit();

        /*
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.CartContainer,new CartdetailFragment()).commit();
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
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem menuItem) {
        drawerLayout.closeDrawer(GravityCompat.START);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Intent intent;
        if (menuItem.getItemId() == R.id.home) {

            intent = new Intent(this, MainActivity.class);
            //transaction.replace(R.id.navigationContainer,new MainFragment());
            startActivity(intent);
            finish();
            //transaction.commit();
            Toast.makeText(this, "ingreso a home", Toast.LENGTH_SHORT).show();
        }
        /*if (menuItem.getItemId() == R.id.account) {
            intent = new Intent(getBaseContext(), PerfilActivity.class);
            intent.putExtra("id", R.id.account);
            startActivity(intent);
            Toast.makeText(this, "ingreso a account", Toast.LENGTH_SHORT).show();
        }*/
        if (menuItem.getItemId() == R.id.Perfil) {
            transaction.replace(R.id.navigationContainer, new SettingFragment());
            transaction.addToBackStack(null);
            transaction.commit();
            Toast.makeText(this, "ingreso a configuracion", Toast.LENGTH_SHORT).show();
        }
        if (menuItem.getItemId() == R.id.MisPedidos) {
            intent = new Intent(this, PedidosActivity.class);
            startActivity(intent);
        }
        return false;
    }
}
